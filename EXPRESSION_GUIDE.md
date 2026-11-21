# Индикаторы-выражения (`ExpressionIndicator`) — инструкция

Документ описывает, как писать строки-выражения для `org.ta4j.core.indicators.expression.ExpressionIndicator`: синтаксис, как разрешаются имена и параметры, какие функции доступны и какие аргументы каждая требует.

Списки функций и арности в разделе 8 сверены с фактическим реестром движка (`ExpressionIndicator.getFunctionNames()`): **268 функций**.

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

// 5) машинный список всех доступных имён
Set<String> names = ExpressionIndicator.getFunctionNames();
```

Значение выражения — всегда число `Num` (целые периоды — положительные целые числа). Результат сравнения/логики — `1` (истина) или `0` (ложь). Любое ненулевое число считается «истиной» в `And/Or/Not/Xor/Nand/Nor` (`NaN` — ложь).

Ошибки (неизвестное имя, неверная арность, незакрытая скобка, не-целый период и т.п.) — `ExpressionValidationException` с позицией символа в строке. Проверка делится на две фазы: `parse + validate` (структура, арности, имена) и `compile` (типы аргументов и построение дерева индикаторов) — обе выполняются в конструкторе, до первого `getValue`. Поэтому «валидное» по `validate()` выражение может всё ещё упасть при компиляции — например, если в позицию периода подставлена серия вместо числа (см. раздел 7).

---

## 2. Синтаксис

Три взаимозаменяемые нотации, можно смешивать в одной строке:

| Нотация | Пример |
|---|---|
| функциональная (скобки сразу после имени, аргументы через запятую) | `SMA(RSI(AVG(High, Low), 5), 10)` |
| оператор-функция (имя-токен содержит оператор, операнды в скобках) | `A>B(MovAvg(Close, 10), RSI(Close, 14))` |
| lisp-подобная (префиксная, аргументы через пробел, скобки вокруг всего вызова) | `(SMA (RSI (AVG High Low) 5) 10)` |

Правила:

- **Пример из ТЗ** `A>B(SMA(RSI(AVG(High,Low), 5), 10), RSI(AVG(High,Low), 5))` — это **готовое валидное выражение**: `A>B` — одно имя функции (его символы допустимы в идентификаторе), `A` и `B` внутри него — часть имени, а не переменные. Эквиваленты:
  ```
  Greater(SMA(RSI(AVG(High, Low), 5), 10), RSI(AVG(High, Low), 5))
  (> (SMA (RSI (AVG High Low) 5) 10) (RSI (AVG High Low) 5))
  ```
  Полный набор токенов-операторов — разделы 4–5.
- **Регистр имён функций не важен**: `rsi(...)`, `RSI(...)`, `Rsi(...)` — одно и то же.
- **Числовые литералы**: целые, дробные, экспоненциальные, отрицательные (`5`, `2.5`, `-1.5`, `1e3`).
- **Имена параметров** из словаря подставляются везде, где ожидается число: `SMA(Close, smaPeriod)`. Разрешение имён: сначала функции (в т.ч. данные `High`...), затем словарь параметров; литералы и параметры взаимозаменяемы. Имя параметра не должно совпадать с именем функции — функция имеет приоритет.
- **Пробелы и запятые** — свободные разделители; запятая необязательна в lisp-нотации: `SMA(Close 5)` ≡ `SMA(Close, 5)`.
- **Важно про скобки**: `(` должно стоять вплотную к имени, иначе имя разбирается как отдельный атом-идентификатор. Для функции с аргументами это ошибка: `SMA (Close, 5)` → `unexpected token '('`, а в позиции аргумента (`Add(SMA (Close, 5), 1)`) — `function 'SMA' requires arguments (write it as 'SMA(...)')`. Lisp-нотация работает только со скобкой вокруг всего вызова: `(SMA Close 5)`, `(> Close 1)`; голое `>` / `SMA` без скобок — ошибка (если только имя не зарегистрировано как 0-аргументное или не передано в словаре параметров).
- **Вложенность не ограничена**: `SMA(SMA(SMA(...,2),2),2)` любой глубины.
- Одиночную пару скобок без имени можно использовать как группировку: `((SMA Close 5))`.

Примеры «правильно/неправильно»:

```
SMA(Close, 3)                        // верно
(> (EMA Close 5) (EMA Close 20))     // верно (lisp)
A>B(EMA(Close, 5), EMA(Close, 20))   // верно (оператор-функция)
(Foo 1)                              // НЕВЕРНО: неизвестная функция 'Foo'
SMA(Close)                           // НЕВЕРНО: SMA требует 2 аргумента
SMA (Close, 3)                       // НЕВЕРНО: пробел перед скобкой — 'SMA' читается как атом
SMA(Close, 0)                        // НЕВЕРНО: период должен быть целым >= 1
SMA(Close, 2))                       // НЕВЕРНО: лишняя закрывающая скобка (at position 13)
SMA(Close, 5                         // НЕВЕРНО: missing ')' after 'SMA'
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

Дополнительно доступны (авто-регистрация, все без аргументов): `TR`, `RealBody`, `UpperShadow`, `LowerShadow`, `CloseLocationValue`, `ClosePriceDifference`, `ClosePriceRatio`, `PlusDM`, `MinusDM`.

Пример: `(SMA (AVG High Low) 10)` ≡ `SMA(MedianPrice, 10)` — оба дают среднее (H+L)/2.

---

## 4. Арифметика и математика

| Функция | Форма оператора | Аргументы | Пример | Результат |
|---|---|---|---|---|
| `Add` | `A+B` | a, b, ... (2+) | `Add(1, 2, 3)` | 6 |
| `Subtract` | `A-B` | a, b (2) | `Subtract(Close, SMA(Close,5))` | отклонение от SMA |
| `Multiply` | `A*B` | a, b, ... (2+) | `Multiply(Close, 1.05)` | Close +5% |
| `Divide` | `A/B` | a, b (2) | `Divide(Close, Lag(Close, 1))` | отношение к прошлому закрытию |
| `Modulo` | `A%B` | a, b (2) | `Modulo(Close, 10)` | остаток |
| `Min` / `Max` | — | 2+ | `Max(1, 2, 3)` | 3 |
| `Avg` | — | 2+ | `Avg(High, Low)` | середина диапазона |
| `Abs`, `Sqrt`, `Ln` (натуральный), `Log` (десятичный), `Exp`, `Neg` | — | x (1) | `Abs(Subtract(Close, Open))` | |
| `Pow` | — | x, n (2), `n` — целое ≥ 1 | `Pow(Close, 2)` | квадрат |

Голый `Avg` — среднее арифметическое, а не скользящая средняя; скользящая — `MovAvg`/`SMA` (см. раздел 6).

Тригонометрия (аргумент 1): `Sin Cos Tan ASin ACos ATan Sinh Cosh Tanh` — в радианах; версии с суффиксом `D` (`SinD`, `ATanD`, `SinhD`, ...) — в градусах (у обратных в градусах результат; у гиперболических `*D` аргумент конвертируется deg→rad).

```
ASinD(0.5)          // 30
CosD(60)            // 0.5
ATan(Divide(Subtract(Close, PreviousValue(Close)), Close))  // угол движения в радианах
A-B(MaxValue(High, 10), A*B(ATR(14), 2))                     // уровень трейлинг-стопа
```

---

## 5. Сравнения и логика (результат 1/0)

| Оператор | Синонимы | Арность |
|---|---|---|
| `>` | `Greater`, `GreaterThan`, `A>B` | 2 |
| `>=` | `GreaterOrEqual`, `A>=B` | 2 |
| `<` | `Less`, `LessThan`, `A<B` | 2 |
| `<=` | `LessOrEqual`, `A<=B` | 2 |
| `=` | `Equal`, `A=B` | 2 (точное равенство double) |
| `!=` | `NotEqual`, `A!=B`, `A<>B` | 2 |
| `And` | | 2+ (все ненулевые) |
| `Or` | | 2+ (хотя бы один ненулевой) |
| `Not` | | 1 |
| `Xor`, `Nand`, `Nor` | | 2 |

Скобочная форма `>`/`>=`/`<`/`<=`/`=`/`!=` работает и как функция (`>(Close, 1)`), и в lisp-нотации (`(> Close 1)`), но только со скобками вокруг вызова. Стандарт для правил — форма `A<op>B(...)`: она односложна и не ломается при смешении нотаций.

Пересечение уровня (crossover) отдельным оператором не задаётся — эмулируется через `Lag`:

```
// X пересёк уровень L снизу вверх на текущем баре
And(A>B(X, L), A<=B(Lag(X, 1), L))
```

Примеры сигналов:

```
// «золотое пересечение»: медленная SMA выше быстрой EMA
(> (SMA Close 50) (EMA Close 20))
A>B(SMA(Close, 50), EMA(Close, 20))

// RSI-фильтр: RSI(14) > 50 И MACD-сигнал положителен
(And (> (RSI Close 14) 50) (> (MACD Close) 0))
And(A>B(RSI(Close, 14), 50), A>B(MACD(Close), 0))

// свеча закрылась выше максимума предыдущего дня (учитывается накопленный DayHigh со сдвигом)
A-B(Close, Lag(DayHigh, 1))
```

---

## 6. Индикаторы: группы и параметры

Обозначения: `x` — любая числовая серия (атом данных, вызов индикатора, арифметика); `n`, `s`, `l` — целые периоды (≥1), которые обязаны быть **числовым литералом или параметром** (серия в этой позиции — ошибка компиляции); аргументы в скобках `[...]` — необязательные.

### Скользящие средние
| Функция | Параметры |
|---|---|
| `SMA(x, n)` | простое среднее |
| `EMA(x, n)` | экспоненциальная |
| `WMA(x, n)` / авто `LWMA` | линейно-взвешенная |
| `HMA(x, n)`, `DEMA`/`DoubleEMA(x, n)`, `TEMA`/`TripleEMA(x, n)`, `ZLEMA(x, n)`, `TMA(x, n)`, `SMMA(x, n)`, `LSMA(x, n)` | сглаженные |
| `VWMA(x, n)` | volume-weighted (объём берётся из серии) |
| `KAMA(x)` = (10, 2, 30); `KAMA(x, n)` = (n, n, n); `KAMA(x, n, m)` = (n, m, m) | адаптивная Кауфмана (effectiveRatio, fast, slow) |
| алиасы NSTRDIND: `MovAvg(x, n)`→SMA, `ExpAvg(x, n)`→EMA, `LinWgtAvg(x, n)`→WMA, `VolWgtAvg(x, n)`→VWMA | |
| авто: `MMA(x, n)`, `WILDERSMA(x, n)`, `ATMA(x, n)`, `MCGINLEYMA(x, n)`, `SGMA(x, n1, n2)`, `DMA(x, n1, n2)`, `EDMA(x, n1, n2)`, `VIDYA(x, n1, n2)` | см. приложение |

### Осцилляторы и каналы
| Функция | Параметры |
|---|---|
| `RSI(x, n)` | |
| `CMO(x, n)`, `ROC(x, n)`, `Momentum(x, n)`, `UlcerIndex(x, n)`, `Fisher(x, n)` | |
| `DPO(n)` | только период, серия берётся из серии (Close) |
| `Stochastic(x, n)` | базовый стохастик |
| `StochasticOscillatorK(n)` → `StochasticOscillatorD(K)` | D принимает только K-стохастик |
| `StochasticRsi(RSI(x, n), n)` | первый аргумент — именно `RSIIndicator` |
| `MACD(x)`, `MACD(x, short, long)` | стандарт 12/26; арность 2 (`MACD(x, n)`) запрещена |
| `MACDSignal(x, signal)`, `MACDSignal(x, signal, short, long)` | сигнал = EMA от MACD; арность 3 запрещена |
| `PPO(x)`, `PPO(x, short, long)` | %MACD; арность 2 запрещена |
| `BollingerUpper(x, n[, k])`, `BollingerLower(x, n[, k])`, `BollingerBandWidth(x, n[, k])` | k — множитель σ, по умолчанию 2 |
| `BollingerBandsMiddle(x)` | средняя полоса (SMA) от входной серии |
| `DonchianUpper(n)`, `DonchianLower(n)`, `DonchianMiddle(n)` | по High/Low серии; есть авто-двойники `DonchianChannelUpper/Lower/Middle(n)` |
| `SuperTrend()`, `SuperTrend(n, multiplier)` | арность 1 запрещена; `SuperTrendUpperBand()`, `SuperTrendLowerBand()` |
| `KeltnerChannelMiddle(n)` | только период |
| `ADX(n)`, `DX(n)`, `PlusDI(n)`, `MinusDI(n)`, `CCI(n)`, `ATR(n)`, `WilliamsR(n)`, `RWIHigh(n)`, `RWILow(n)` | авто, только периоды |
| `AroonUp(x, n)`, `AroonDown(x, n)`, `AroonOscillator(n)` | |
| `TrueStrengthIndex(x)`, `ConnorsRsi(x)`, `KST(x)`, `SchaffTrendCycle(x)`, `CoppockCurve(x)`, `MacdV(x)`, `Streak(x)` | |
| `SqueezePro(n)`, `Chop(n, scaleTo)`, `IntraDayMomentumIndex(n)`, `KRI(n)`, `MassIndex(n1, n2)`, `RAVI(x, n1, n2)` | у первых четырёх аргументы — только периоды |
| `IchimokuTenkanSen()`, `IchimokuKijunSen()`, `IchimokuSenkouSpanA()`, `IchimokuSenkouSpanB()`, `IchimokuChikouSpan()`, `IchimokuLine(n)` | |
| `ParabolicSar()`, `ChandelierExitLong()`, `ChandelierExitShort()`, `AwesomeOscillator()`, `AccelerationDeceleration()`, `TrendLineSupport()`, `TrendLineResistance()`, `RecentFractalSwingHigh()`, `RecentFractalSwingLow()`, `RecentZigZagSwingHigh()`, `RecentZigZagSwingLow()` | без аргументов |

### Объёмные / поток денег
`OBV()` (он же `OnBalanceVolume()`), `MFI(n)` (он же `MoneyFlowIndex(n)`), авто: `AccumulationDistribution()`, `ChaikinMoneyFlow(n)`, `ChaikinOscillator()`, `NVI()`, `PVI()`, `PVO()`, `VWAP(n)`, `MVWAP(VWAP(n), n)` (первый аргумент — именно `VWAPIndicator`), `ROCV(n)`, `TimeSegmentedVolume(n)`, `RelativeVolumeStandardDeviation(n)`, `III()`.

### Статистика и вспомогательные
| Функция | Параметры | Синоним (авто) |
|---|---|---|
| `StdDev(x, n)` | станд. отклонение | `StandardDeviation` |
| `Variance(x, n)` | | |
| `MeanDev(x, n)` | среднее отклонение | `MeanDeviation` |
| `StdErr(x, n)` | станд. ошибка среднего | `StandardError` |
| `MaxValue(x, n)` / `MinValue(x, n)` | макс/мин за окно | `HighestValue`/`LowestValue` |
| `Lag(x)`, `Lag(x, n)` | значение N баров назад | `PreviousValue` (только 1 аргумент) |
| `Sum(x, n)` | сумма за окно | `RunningTotal` |
| `CumulativeProduct(x)` | накопительное произведение | |
| `Difference(x)`, `DifferencePercentage(x)`, `PercentageChange(x)`, `Gain(x)`, `Loss(x)` | | |
| `Sigma(x, n)`, `SimpleLinearRegression(x, n)`, `PeriodicalGrowthRate(x, n)`, `PercentRank(x, n)` | | |
| `DistanceFromMA(ma)` | (цена − MA)/MA, аргумент — готовая скользящая | |
| `Unstable(x, nBars)` | обнуляет (NaN) первые nBars баров | |
| `KalmanFilter(x)` | сглаживание Калмана | |
| `SwingPointMarker(swingIndicator)` | аргумент — индикатор свинга (`RecentZigZagSwingHigh` и т.п. имеют другой тип, чаще нужна `registerFunction`) | |

XY-регрессия двух серий (все 3 аргумента — `x, y, n`):
`XYRegressionR`, `XYRegressionRSquared`, `XYRegressionSlope`, `XYRegressionIntercept` (Y), `XYRegressionXIntercept`, `XYRegressionStdError`.

```
// коррляция закрытия с типичной ценой за 20 баров
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
A>B(MovAvg(RSI(AVG(High, Low), rsiPeriod), smaPeriod), RSI(AVG(High, Low), rsiPeriod))

// то же через словарь параметров {"rsiPeriod":5,"smaPeriod":10,"threshold":30}
(And (> Close (BollingerUpper Close 20)) (< (RSI Close 14) threshold))

// дневной диапазон растёт 3 дня подряд
(And (> DayRange (Lag DayRange 1)) (> Lag(DayRange, 1) Lag(DayRange, 2)))

// импульс: Close минус Close 5 баров назад, нормированный на Close
Divide(Momentum(Close, 5), Lag(Close, 5))

// уровень трейлинг-стопа лонга: максимум 10 баров минус 2 ATR
A-B(MaxValue(High, 10), A*B(ATR(14), 2))
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

Нюансы авто-записей:

- **Выбирается самый простой поддерживаемый конструктор.** Если у класса есть и `(BarSeries, int)`, и `(Indicator<Num>, int)`, в реестр попадёт первая форма — функция будет принимать только период, а не серию. Так `KRI(14)` работает, а `KRI(Close, 14)` — ошибка арности, хотя Java-конструктор `(Indicator<Num>, int)` у класса есть.
- **Позиции `int` требуют числовой константы** (литерал или параметр). Подвыражение-индикатор там даст `expected a numeric constant but got a sub-indicator`.
- **Тип входной серии проверяется по фактически выбранному конструктору.** Узкие типы (`StochasticRsi` → `RSIIndicator`, `MVWAP` → `VWAPIndicator`, `StochasticOscillatorD` → `StochasticOscillatorKIndicator`, `ElliottProjection`/`ElliottInvalidationLevel` → `ElliottScenarioIndicator`) совпадают только если вложенный вызов действительно собирает этот класс; иначе — `... incompatible arguments (argument type mismatch)` с именем функции в сообщении. `ElliottScenarioIndicator` в реестре нет, поэтому эллиоттические функции из выражений недоступны — только через `registerFunction`.

---

## 8. Приложение: полный список функций (268) и их арность

Регистр имён в строке не важен. `x` — числовая серия, `n/s/l` — целые периоды ≥1 (литерал или параметр). Имена сгруппированы по количеству аргументов; для функций, добавленных авто-сканом, аргументы соответствуют выбранному конструктору класса (перечислены роли).

### Без аргументов — данные, календарь, индикаторы по серии (79)

Данные и их производные (21):
`Close ClosePrice Open OpenPrice High HighPrice Low LowPrice Volume Amount TypicalPrice MedianPrice TR RealBody UpperShadow LowerShadow CloseLocationValue ClosePriceDifference ClosePriceRatio PlusDM MinusDM`

Календарь и intraday (32):
`DayOfWeek DayOfMonth MonthOfYear HourOfDay MinuteOfHour TimeOfDay DaysUntilThirdFriday DayOpen DayHigh DayLow DayClose DayVolume DayRange MondayFlag TuesdayFlag WednesdayFlag ThursdayFlag FridayFlag SaturdayFlag SundayFlag JanuaryFlag FebruaryFlag MarchFlag AprilFlag MayFlag JuneFlag JulyFlag AugustFlag SeptemberFlag OctoberFlag NovemberFlag DecemberFlag`

Серийные индикаторы (берут OHLCV сами, 26):
`AccumulationDistribution AwesomeOscillator ChaikinOscillator ChandelierExitLong ChandelierExitShort III NVI OBV OnBalanceVolume PVI PVO ParabolicSar AccelerationDeceleration IchimokuTenkanSen IchimokuKijunSen IchimokuSenkouSpanA IchimokuSenkouSpanB IchimokuChikouSpan RecentFractalSwingHigh RecentFractalSwingLow RecentZigZagSwingHigh RecentZigZagSwingLow SuperTrendUpperBand SuperTrendLowerBand TrendLineResistance TrendLineSupport`

### 1 аргумент (76 + частичные арности)

Только период `(n)` — серия берётся автоматически, аргумент обязан быть числом (30):
`ADX AroonOscillator ATR CCI ChaikinMoneyFlow DonchianChannelLower DonchianChannelMiddle DonchianChannelUpper DonchianLower DonchianMiddle DonchianUpper DPO DX IchimokuLine IntraDayMomentumIndex KeltnerChannelMiddle KRI MFI MinusDI MoneyFlowIndex PlusDI RelativeVolumeStandardDeviation ROCV RWIHigh RWILow SqueezePro StochasticOscillatorK TimeSegmentedVolume VWAP WilliamsR`

Серия `(x)` — обработка входной последовательности (43):
`Abs Sqrt Ln Log Exp Neg Not Sin Cos Tan ASin ACos ATan Sinh Cosh Tanh SinD CosD TanD ASinD ACosD ATanD SinhD CoshD TanhD Gain Loss Difference DifferencePercentage PercentageChange PreviousValue CumulativeProduct BollingerBandsMiddle ConnorsRsi Kst CoppockCurve MacdV TrueStrengthIndex SchaffTrendCycle Streak KalmanFilter DistanceFromMA StochasticOscillatorD`
(`StochasticOscillatorD` принимает только K-стохастик; `DistanceFromMA` — готовую скользящую)

Прочие 1-аргументные с узким типом входа (3) — несовместимый аргумент даст ошибку компиляции с именем функции: `SwingPointMarker` (ожидает swing-индикатор), `ElliottProjection` и `ElliottInvalidationLevel` (ожидают `ElliottScenarioIndicator`, которого нет в реестре, — из выражений недоступны).

Частичные арности (объявлены диапазонами, но не все значения допустимы):
`Lag(1..2)` · `KAMA(1..3)` · `MACD(1 или 3; 2 — ошибка)` · `PPO(1 или 3; 2 — ошибка)` · `SuperTrend(0 или 2; 1 — ошибка)`

### 2 аргумента (86)

Операторы и их синонимы (33):
`> >= < <= = != Greater GreaterThan GreaterOrEqual Less LessThan LessOrEqual Equal NotEqual Xor Nand Nor A>B A>=B A<B A<=B A=B A!=B A<>B A+B A-B A*B A/B A%B Subtract Divide Modulo Pow`

`(x, n)` — «серия + период» (51):
`AroonDown AroonUp ATMA CMO DEMA DoubleEMA EMA ExpAvg Fisher HMA HighestValue LinWgtAvg LowestValue LSMA LWMA MaxValue McGinleyMA MeanDev MeanDeviation MinValue MMA Momentum MovAvg MVWAP PercentRank PeriodicalGrowthRate ROC RSI RunningTotal Sigma SimpleLinearRegression SMA SMMA StandardDeviation StandardError StdDev StdErr Stochastic StochasticRsi Sum TEMA TMA TripleEMA UlcerIndex Unstable Variance VolWgtAvg VWMA WildersMA WMA ZLEMA`
(у `MVWAP` первый аргумент — `VWAPIndicator`, у `StochasticRsi` — `RSIIndicator`, у `AroonUp`/`AroonDown` — серия High/Low)

`(n1, n2)` — два периода по серии (2): `Chop(n, scaleTo)`, `MassIndex(n1, n2)`

### 2–3 аргумента: `BollingerUpper(x, n[, k])`, `BollingerLower(x, n[, k])`, `BollingerBandWidth(x, n[, k])`
### 2–4 аргумента: `MACDSignal(x, signal[, short, long])` (3 — ошибка)
### Varargs (2+): `Add Multiply Min Max Avg And Or`
### 3 аргумента (11): `XYRegressionR(x, y, n)`, `XYRegressionRSquared`, `XYRegressionSlope`, `XYRegressionIntercept`, `XYRegressionXIntercept`, `XYRegressionStdError`, `DMA(x, n1, n2)`, `EDMA`, `SGMA`, `VIDYA`, `RAVI(x, n1, n2)`

Машиночитаемый список в рантайме: `ExpressionIndicator.getFunctionNames()`.
