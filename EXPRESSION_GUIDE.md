# Индикаторы-выражения (`ExpressionIndicator`) — инструкция

Документ описывает, как писать строки-выражения для `org.ta4j.core.indicators.expression.ExpressionIndicator`: синтаксис, как разрешаются имена и параметры, какие функции доступны и какие аргументы каждая требует.

---

## 1. Java API

```java
BarSeries series = ...;

// 1) простая строка
Indicator<Num> ind = new ExpressionIndicator(series, "SMA(Close, 10)");

// 2) строка + словарь параметров (имя -> число)
Map<String, Number> params = Map.of("rsiPeriod", 5, "smaPeriod", 10, "threshold", 50);
Indicator<Num> ind2 = new ExpressionIndicator(series, "(> RSI(Close, rsiPeriod) threshold)", params);

// 3) валидация без серии (синтаксис, имена, арность) — до вычислений
ExpressionIndicator.validate("(> RSI(Close, rsiPeriod) threshold)", params);

// 4) свои функции и свои пакеты индикаторов
ExpressionIndicator.registerFunction("MyInd", 2, 2,
        (args, s) -> new MyIndicator(args.get(0).indicator(), args.get(1).positiveInt()));
ExpressionIndicator.autoRegisterFunctions("com.mycompany.indicators");
```

Значение выражения — всегда число `Num` (целые периоды — положительные целые числа). Результат сравнения/логики — `1` (истина) или `0` (ложь). Любое ненулевое число считается «истиной» в `And/Or/Not/Xor/Nand/Nor`.

Ошибки (неизвестное имя, неверная арность, незакрытая скобка, не-целый период и т.п.) — `ExpressionValidationException` с позицией символа в строке. Проверка делится на две фазы: `parse + validate` (структура, арности, имена) и `compile` (типы аргументов и построение дерева индикаторов) — обе выполняются в конструкторе, до первого `getValue`.

---

## 2. Синтаксис

Две взаимозаменяемые нотации, можно смешивать в одной строке:

| Нотация | Пример |
|---|---|
| функциональная (скобки сразу после имени, аргументы через запятую) | `SMA(RSI(AVG(High, Low), 5), 10)` |
| lisp-подобная (префиксная, аргументы через пробел, скобки вокруг всего вызова) | `(SMA (RSI (AVG High Low) 5) 10)` |

Правила:

- **Пример из ТЗ** `A>B(SMA(RSI(AVG(High,Low), 5), 10), RSI(AVG(High,Low), 5))` записывается так:
  ```
  Greater(SMA(RSI(AVG(High, Low), 5), 10), RSI(AVG(High, Low), 5))
  (> (SMA (RSI (AVG High Low) 5) 10) (RSI (AVG High Low) 5))
  ```
  где `A>B` — это `>` / `Greater` / `GreaterThan` (или любой другой оператор из разделов 4–5).
- **Регистр имён функций не важен**: `rsi(...)`, `RSI(...)`, `Rsi(...)` — одно и то же.
- **Числовые литералы**: целые, дробные, экспоненциальные, отрицательные (`5`, `2.5`, `-1.5`, `1e3`).
- **Имена параметров** из словаря подставляются везде, где ожидается число: `SMA(Close, smaPeriod)`. Разрешение имён: сначала функции (в т.ч. данные `High`...), затем словарь параметров; литералы и параметры взаимозаменяемы.
- **Пробелы и запятые** — свободные разделители; запятая необязательна в lisp-нотации.
- **Важно про смешивание**: функциональная нотация распознана только если `(` стоит вплотную к имени (`SMA(x,5)`). Если между именем и `(` есть пробел, имя — атом, а скобочный узел — отдельный аргумент (lisp-стиль): `(SMA (RSI Close 5) 10)` — SMA с аргументами `(RSI Close 5)` и `10`.
- **Вложенность не ограничена**: `SMA(SMA(SMA(...,2),2),2)` любой глубины.
- Одиночную пару скобок без имени можно использовать как группировку: `((SMA Close 5))`.

Примеры «правильно/неправильно»:

```
SMA(Close, 3)                        // верно
(> (EMA Close 5) (EMA Close 20))     // верно (lisp)
(Foo 1)                              // НЕВЕРНО: неизвестная функция 'Foo'
SMA(Close)                           // НЕВЕРНО: SMA требует 2 аргумента
SMA(Close, 0)                        // НЕВЕРНО: период должен быть целым >= 1
SMA(Close, 2)                        // ошибка: лишняя закрывающая скобка — ) (SMA(Close, 2))
```

---

## 3. Данные (атомы)

Серия свечей доступна через атомы (без аргументов). Канонические имена и авто-алиасы:

| Атом | Значение | Алиасы (авто) |
|---|---|---|
| `High` | max цены свечи | `HighPrice` |
| `Low` | min цены свечи | `LowPrice` |
| `Open` | цена открытия | `OpenPrice` |
| `Close` | цена закрытия | `ClosePrice` |
| `Volume` | объём | |
| `Amount` | денежный объём | |
| `TypicalPrice` | (H+L+C)/3 | |
| `MedianPrice` | (H+L)/2 | |

Пример: `(SMA (AVG High Low) 10)` ≡ `SMA(MedianPrice, 10)` — оба дают среднее (H+L)/2.

---

## 4. Арифметика и математика

| Функция | Аргументы | Пример | Результат |
|---|---|---|---|
| `Add` | a, b, ... (2+) | `Add(1, 2, 3)` | 6 |
| `Subtract` | a, b (2) | `Subtract(Close, SMA(Close,5))` | отклонение от SMA |
| `Multiply` | a, b, ... (2+) | `Multiply(Close, 1.05)` | Close +5% |
| `Divide` | a, b (2) | `Divide(Close, Lag(Close, 1))` | отношение к прошлому закрытию |
| `Min` / `Max` | 2+ | `Max(1, 2, 3)` | 3 |
| `Avg` | 2+ | `Avg(High, Low)` | середина диапазона |
| `Abs`, `Sqrt`, `Ln` (натуральный), `Log` (десятичный), `Exp`, `Neg` | x (1) | `Abs(Subtract(Close, Open))` | |
| `Pow` | x, n (2) | `Pow(Close, 2)` | квадрат |
| `Modulo` | a, b (2) | `Modulo(Close, 10)` | остаток |

Тригонометрия (аргумент 1): `Sin Cos Tan ASin ACos ATan Sinh Cosh TanH` — в радианах; версии с суффиксом `D` (`SinD`, `ATanD`, `SinhD`, ...) — в градусах (у обратных в градусах результат; у гиперболических `*D` аргумент конвертируется deg→rad).

```
ASinD(0.5)          // 30
CosD(60)            // 0.5
ATan(Divide(Subtract(Close, PreviousValue(Close)), Close))  // угол движения в радианах
```

---

## 5. Сравнения и логика (результат 1/0)

| Оператор | Синонимы | Аргументы |
|---|---|---|
| `>` | `Greater`, `GreaterThan` | 2 |
| `>=` | `GreaterOrEqual` | 2 |
| `<` | `Less`, `LessThan` | 2 |
| `<=` | `LessOrEqual` | 2 |
| `=` | `Equal` | 2 (точное равенство double) |
| `!=` | `NotEqual` | 2 |
| `And` | | 2+ (все ненулевые) |
| `Or` | | 2+ (хотя бы один ненулевой) |
| `Not` | | 1 |
| `Xor`, `Nand`, `Nor` | | 2 |

Примеры сигналов:

```
// «золотое пересечение»: медленная SMA выше быстрой EMA
(> (SMA Close 50) (EMA Close 20))

// RSI-фильтр: RSI(14) > 50 И MACD-сигнал положителен
(And (> (RSI Close 14) 50) (> (MACD Close) 0))

// свеча закрылась выше максимума предыдущего дня (учитывается накопленный DayHigh со сдвигом)
Subtract(Close, Lag(DayHigh, 1))
```

---

## 6. Индикаторы: группы и параметры

Обозначения: `x` — любая числовая серия (атом данных, вызов индикатора, арифметика, константа); `n`, `s`, `l` — целые периоды (≥1); аргументы в скобках `[...]` — необязательные.

### Скользящие средние
| Функция | Параметры |
|---|---|
| `SMA(x, n)` | простое среднее |
| `EMA(x, n)` | экспоненциальная |
| `WMA(x, n)` / авто `LWMA` | линейно-взвешенная |
| `HMA(x, n)`, `DEMA(x, n)`, `TEMA(x, n)`, `ZLEMA(x, n)`, `TMA(x, n)`, `SMMA(x, n)`, `LSMA(x, n)` | сглаженные |
| `VWMA(x, n)` | volume-weighted (объём берётся из серии) |
| `KAMA(x)`, `KAMA(x, n)`, `KAMA(x, n, fastSlow)` | адаптивная Кауфмана |
| авто: `MMA(x, n)`, `WILDERSMA(x, n)`, `ATMA(x, n)`, `MCGINLEYMA(x, n)`, `SGMA(x, n1, n2)` (3 арг.), `DMA(x, n1, n2)` (3 арг.), `EDMA(x, n1, n2)` (3 арг.), `VIDYA(x, n1, n2)` (3 арг.) | см. приложение |

### Осцилляторы и каналы
| Функция | Параметры |
|---|---|
| `RSI(x, n)` | |
| `CMO(x, n)`, `ROC(x, n)`, `Momentum(x, n)`, `UlcerIndex(x, n)`, `Fisher(x, n)` | |
| `MACD(x)`, `MACD(x, short, long)` | стандарт 12/26 |
| `MACDSignal(x, signal)` , `MACDSignal(x, signal, short, long)` | сигнал = EMA от MACD |
| `PPO(x)`, `PPO(x, short, long)` | %MACD |
| `BollingerUpper(x, n[, k])`, `BollingerLower(x, n[, k])`, `BollingerBandWidth(x, n[, k])` | k — множитель σ, по умолчанию 2 |
| `DonchianUpper(n)`, `DonchianLower(n)`, `DonchianMiddle(n)` | по High/Low серии |
| `SuperTrend()`, `SuperTrend(n, multiplier)` | |
| авто: `ADX(n)`, `DX(n)`, `PlusDI(n)`, `MinusDI(n)`, `CCI(n)`, `ATR(n)`, `WilliamsR(n)`, `Stochastic(x, n)`, `StochasticOscillatorK(n)`, `TrueStrengthIndex(x)`, `ConnorsRSI(x)`, `KST(x)`, `SchaffTrendCycle(x)`, `DPO(x, n)`, `RAVI(x, n1, n2)`, `CoppockCurve(x)`, `MACDV(x)`, `SqueezePro(x)`, `Chop(n, scaleTo)`, `MassIndex(x, n)`, `KRI(x)`, `IntraDayMomentumIndex(x)` | |

### Объёмные / поток денег
`OBV()`, `MFI(n)`, авто: `AccumulationDistribution()`, `ChaikinMoneyFlow(n)`, `ChaikinOscillator()`, `OnBalanceVolume()`, `NVI()`, `PVI()`, `VWAP(n)`, `MVWAP(n)`, `ROCV(n)`, `TimeSegmentedVolume(n)`, `RelativeVolumeStandardDeviation(n)`, `III()`.

### Статистика и вспомогательные
| Функция | Параметры | Синоним (авто) |
|---|---|---|
| `StdDev(x, n)` | станд. отклонение | `StandardDeviation` |
| `Variance(x, n)` | | |
| `MeanDev(x, n)` | среднее отклонение | `MeanDeviation` |
| `StdErr(x, n)` | станд. ошибка среднего | `StandardError` |
| `MaxValue(x, n)` / `MinValue(x, n)` | макс/мин за окно | `HighestValue`/`LowestValue` |
| `Lag(x)`, `Lag(x, n)` | значение N баров назад | `PreviousValue` |
| `Sum(x, n)` | сумма за окно | `RunningTotal` |
| `CumulativeProduct(x)` | накопительное произведение | |
| `Difference(x)`, `DifferencePercentage(x)`, `PercentageChange(x)`, `Gain(x)`, `Loss(x)`, `Sigma(x, n)`, `SimpleLinearRegression(x, n)`, `PeriodicalGrowthRate(x, n)`, `PercentRank(x, n)`, `DistanceFromMA(x, n)` | | |

XY-регрессия двух серий (все 3 аргумента — `x, y, n`):
`XYRegressionR`, `XYRegressionRSquared`, `XYRegressionSlope`, `XYRegressionIntercept` (Y), `XYRegressionXIntercept`, `XYRegressionStdError`.

```
// коррляция нашего инструмента с бенчмарком-серией (через параметр-серию нельзя:
// вторая серия подаётся отдельным индикатором из вашего кода или NumericIndicator)
XYRegressionR(Close, TypicalPrice, 20)
```

### Календарь, время, intraday (все — без аргументов)
`DayOfWeek` (ISO 1=Пн..7=Вс), `DayOfMonth`, `MonthOfYear`, `HourOfDay`, `MinuteOfHour`, `TimeOfDay` (секунд от полуночи), `DaysUntilThirdFriday`; флаги `MondayFlag...SundayFlag`, `JanuaryFlag...DecemberFlag` (1/0); intraday-накопительные `DayOpen`, `DayHigh`, `DayLow`, `DayClose`, `DayVolume`, `DayRange` (в текущей зоне серии; на дневных данных равны самой свече).

```
// торговать только по вторникам–четвергам (Or принимает 2+ аргумента)
(Or TuesdayFlag WednesdayFlag ThursdayFlag)

// и только в первом квартале
(And (Or TuesdayFlag WednesdayFlag ThursdayFlag) (Add JanuaryFlag FebruaryFlag MarchFlag))
```

### Примеры сигналов целиком

```
// пример из ТЗ: сглаженный RSI выше несглаженного
(> (SMA (RSI (AVG High Low) 5) 10) (RSI (AVG High Low) 5))

// то же через словарь параметров {"rsiPeriod":5,"smaPeriod":10}
(> (SMA (RSI (AVG High Low) rsiPeriod) smaPeriod) (RSI (AVG High Low) rsiPeriod))

// цена выше Боллинджер-верхней при RSI<30 (перепроданность + breakout — пример комбинирования)
(And (> Close (BollingerUpper Close 20)) (< (RSI Close 14) 30))

// дневной диапазон растёт 3 дня подряд
(And (> DayRange (Lag DayRange 1)) (> Lag(DayRange,1) Lag(DayRange,2)))

// импульс: Close минус Close 5 баров назад, нормированный на Close
Divide(Momentum(Close, 5), Lag(Close, 5))
```

---

## 7. Авто-регистрация (рефлексия)

При первом обращении к `ExpressionIndicator` (static-init, один раз) пакет `org.ta4j.core.indicators` рефлексивно сканируется и в словарь добавляются все классы-индикаторы `Indicator<Num>` с каноническими конструкторами:

| Конструктор класса | Функция |
|---|---|
| `(BarSeries)` | имя() |
| `(BarSeries, int)` | имя(n) |
| `(BarSeries, int, int)` | имя(n1, n2) |
| `(Indicator<Num>)` | имя(x) |
| `(Indicator<Num>, int)` | имя(x, n) |
| `(Indicator<Num>, int, int)` | имя(x, n1, n2) |

Имя = имя класса без суффикса `Indicator`, регистронезависимо. Уже зарегистрированные ручные имена (SMA, RSI, ...) никогда не перезаписываются. Классы с нестандартными конструкторами, а также возвращающие не `Num` (булевы свечные паттерны `Doji`, `Hammer` и т.п., `DateTimeIndicator`) — не регистрируются; для них используйте `registerFunction`.

Ваш код: `ExpressionIndicator.autoRegisterFunctions("com.myco.indicators")` — тем же правилом подхватятся ваши классы после добавления в classpath (повторный вызов безопасно дозаполняет словарь).

Нюансы авто-записей: у `x`-аргументов может быть более узкий ожидаемый тип (например, `StochasticOscillatorD` ожидает на входе K-стохастик) — несовместимость проявится ошибкой валидации при компиляции выражения с именем функции в сообщении.

---

## 8. Приложение: полный список функций (252) и их арность

Регистр имён в строке не важен. `x` — числовая серия, `n/s/l` — целые периоды ≥1. Имена сгруппированы по количеству аргументов; для функций, добавленных авто-сканом, аргументы соответствуют конструктору класса (перечислены роли).

### Без аргументов — данные, календарь, индикаторы по серии (79)

Данные и их производные:
`Close ClosePrice Open OpenPrice High HighPrice Low LowPrice Volume Amount TypicalPrice MedianPrice TR RealBody UpperShadow LowerShadow CloseLocationValue ClosePriceDifference ClosePriceRatio PlusDM MinusDM`

Календарь и intraday (раздел 6):
`DayOfWeek DayOfMonth MonthOfYear HourOfDay MinuteOfHour TimeOfDay DaysUntilThirdFriday DayOpen DayHigh DayLow DayClose DayVolume DayRange MondayFlag TuesdayFlag WednesdayFlag ThursdayFlag FridayFlag SaturdayFlag SundayFlag JanuaryFlag...DecemberFlag`

Серийные индикаторы (берут OHLCV сами):
`AccumulationDistribution AwesomeOscillator ChaikinOscillator ChandelierExitLong ChandelierExitShort III NVI OBV OnBalanceVolume PVI PVO ParabolicSar AccelerationDeceleration IchimokuTenkanSen IchimokuKijunSen IchimokuSenkouSpanA IchimokuSenkouSpanB IchimokuChikouSpan RecentFractalSwingHigh RecentFractalSwingLow RecentZigZagSwingHigh RecentZigZagSwingLow SuperTrendUpperBand SuperTrendLowerBand TrendLineResistance TrendLineSupport`

### 1 аргумент (77 + варианты)

Период `(n)` — индикатор берёт OHLCV серии сам: `ADX CCI DX PlusDI MinusDI MFI MoneyFlowIndex WilliamsR StochasticOscillatorK AroonUp AroonDown AroonOscillator RWIHigh RWILow ChaikinMoneyFlow ROCV TimeSegmentedVolume RelativeVolumeStandardDeviation VWAP DonchianUpper DonchianLower DonchianMiddle DonchianChannelUpper DonchianChannelLower DonchianChannelMiddle`

Серия `(x)` — обработка входной последовательности: `Abs Sqrt Ln Log Exp Neg Not Sin Cos Tan ASin ACos ATan Sinh Cosh TanH SinD CosD TanD ASinD ACosD ATanD SinhD CoshD TanhD Gain Loss Difference DifferencePercentage PercentageChange PreviousValue CumulativeProduct BollingerBandsMiddle StochasticRsi ConnorsRsi Kst CoppockCurve MacdV TrueStrengthIndex SchaffTrendCycle SqueezePro IntraDayMomentumIndex Kri`

Прочие 1-аргументные (роль аргумента — по javadoc класса; несовместимый аргумент даст ошибку валидации с именем функции): `StochasticOscillatorD* ElliottProjection ElliottInvalidationLevel IchimokuLine KalmanFilter KeltnerChannelMiddle Streak SwingPointMarker`
(* `StochasticOscillatorD` ожидает на входе K-стохастик; `Streak` — счётчик продолжительности по булеву правилу внутри класса)

Частичные арности: `Lag(1..2)`, `KAMA(1..3)`, `MACD(1..3)`, `PPO(1..3)`, `SuperTrend(0..2)`

### 2 аргумента (69)

Операторы: `> >= < <= = != Greater GreaterThan GreaterOrEqual Less LessThan LessOrEqual Equal NotEqual Xor Nand Nor Subtract Divide Modulo Pow`

`(x, n)` — «серия + период»: `SMA EMA WMA LWMA HMA DEMA DoubleEMA TEMA TripleEMA ZLEMA TMA SMMA MMA WildersMA LSMA ATMA McGinleyMA RSI CMO ROC Momentum UlcerIndex Fisher DPO Stochastic(x,n) StdDev StandardDeviation Variance MeanDev MeanDeviation StdErr StandardError Sigma SimpleLinearRegression PeriodicalGrowthRate PercentRank MaxValue MinValue HighestValue LowestValue Sum RunningTotal Unstable(x, nBars)`

По серии, 1–2 периода: `ATR(n) MVWAP(n) MassIndex(n1, n2) Chop(n1, n2)`

### 2–3 аргумента: `BollingerUpper(x, n[, k])`, `BollingerLower(x, n[, k])`, `BollingerBandWidth(x, n[, k])`
### 2–4: `MACDSignal(x, signal[, short, long])`
### Varargs (2+): `Add Multiply Min Max Avg And Or`
### 3 аргумента: `XYRegressionR/Slope/Intercept/XIntercept/RSquared/StdError(x, y, n)`, `DMA/EDMA/SGMA/VIDYA(x, n1, n2)`, `RAVI(x, n1, n2)`

Машиночитаемый список в рантайме: `ExpressionIndicator.getFunctionNames()`.
