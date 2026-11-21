# Индикаторы ta4j (форк) — полный список

Сгенерировано 2026-11-21 из исходников `ta4j-core` (пакет `org.ta4j.core.indicators`). Всего индикаторов: **258** (из них **41** новых, помечены ✨).

Описания взяты из JavaDoc классов. Абстрактные классы (`Abstract*`, `CachedIndicator`, `RecursiveCachedIndicator` и т.п.) — базовые для наследования. Служебные (не-indicator) классы — в приложении.

## Основные индикаторы (корневой пакет) (46)

| Индикатор | Описание |
|---|---|
| `ATRIndicator` | Average true range indicator. |
| `AbstractIndicator` | Abstract Indicator indicator. |
| `AbstractRecentSwingIndicator` | Base class for swing-point indicators that exposes both swing values and confirmed swing indexes. Subclasses implement the swing-detection logic via detectLatestSwingIndex(int). |
| `AccelerationDecelerationIndicator` | Acceleration-deceleration indicator. |
| `AwesomeOscillatorIndicator` | Awesome oscillator (AO) indicator. |
| `CCIIndicator` | Commodity Channel Index (CCI) indicator. |
| `CMOIndicator` | Chande Momentum Oscillator (CMO) indicator. |
| `CachedIndicator` | Cached Indicator indicator. Caches the calculated results of the indicator to avoid calculating the same index of the indicator twice. |
| `ChandelierExitLongIndicator` | The Chandelier Exit (long) Indicator. |
| `ChandelierExitShortIndicator` | The Chandelier Exit (short) Indicator. |
| `ChopIndicator` | The "CHOP" index is used to indicate side-ways markets. 100++ * LOG10( SUM(ATR(1), n) / ( MaxHi(n) - MinLo(n) ) ) / LOG10(n), with n = User defined period length. |
| `ConnorsRSIIndicator` | Connors RSI indicator. Combines a short-term RSI, RSI of streak length, and the percentile rank of daily price changes. |
| `CoppockCurveIndicator` | Coppock Curve indicator. |
| `DPOIndicator` | The Detrended Price Oscillator (DPO) indicator. The Detrended Price Oscillator (DPO) is an indicator designed to remove trend from price and make it easier to identify cycles. |
| `DistanceFromMAIndicator` | Distance From Moving Average (close - MA) / MA |
| `FisherIndicator` | The Fisher Indicator. |
| `IntraDayMomentumIndexIndicator` | IntraDay Momentum Index Indicator. The IntraDay Momentum Index is a measure of the security's strength of trend. |
| `KRIIndicator` | Kairi Relative Index (KRI) indicator by LazyBear. |
| `KSTIndicator` | Know Sure Thing (KST) indicator. RCMA1 = X1-Period SMA of Y1-Period Rate-of-Change RCMA2 = X2-Period SMA of Y2-Period Rate-of-Change RCMA3 = X3-Period SMA of Y3-Period Rate-of-Change RCMA4 = X4-Period SMA of Y4-Period Rate-of-Change KST = (RCMA1 x 1) + (RCMA2 x 2) + (RCMA3 x 3)… |
| `KalmanFilterIndicator` | KalmanFilterIndicator is a technical analysis indicator that uses the Kalman filter to smooth the values of an underlying indicator. The Kalman filter is a recursive algorithm that estimates the state of a dynamic system from a series of noisy measurements. |
| `MACDIndicator` | Moving average convergence divergence (MACDIndicator) indicator (also called "MACD Absolute Price Oscillator (APO)"). |
| `MACDVIndicator` | Moving Average Convergence Divergence Volume (MACD-V) indicator. MACD-V applies the standard MACD calculation to volume and volatility weighted exponential moving averages. |
| `MassIndexIndicator` | Mass index indicator. |
| `NetMomentumIndicator` | Net Momentum Indicator. This indicator measures the cumulative deviation of an oscillating indicator from its neutral pivot point over a specified timeframe. |
| `PPOIndicator` | Percentage price oscillator (PPO) indicator (also called "MACD Percentage Price Oscillator (MACD-PPO)"). |
| `PVOIndicator` | Percentage Volume Oscillator (PVO) indicator. ((12-day EMA of Volume - 26-day EMA of Volume) / 26-day EMA of Volume) x 100 |
| `ParabolicSarIndicator` | Parabolic SAR indicator. |
| `RAVIIndicator` | Chande's Range Action Verification Index (RAVI) indicator. To preserve trend direction, default calculation does not use absolute value. |
| `ROCIndicator` | Rate of change (ROCIndicator) indicator (also called "Momentum"). The ROCIndicator calculation compares the current value with the value "n" periods ago. |
| `RSIIndicator` | Relative strength index indicator. Computed using the original Welles Wilder formula. |
| `RWIHighIndicator` | The RandomWalkIndexHighIndicator. |
| `RWILowIndicator` | The Class RandomWalkIndexLowIndicator. |
| `RecentFractalSwingHighIndicator` | Recent Fractal Swing High Indicator. Identifies the price of the most recently confirmed swing high, i.e., a bar whose high is greater than the surrounding bars as defined by the supplied look-back and look-forward windows. |
| `RecentFractalSwingLowIndicator` | Recent Fractal Swing Low Indicator. Identifies the price of the most recently confirmed swing low, defined as a bar whose low is lower than its surrounding bars within the configured windows. |
| `RecentSwingIndicator` | Interface for indicators that identify the most recently confirmed swing point (either a swing high or swing low). A swing point is a price point that is higher or lower than the surrounding price points. |
| `RecursiveCachedIndicator` | Recursive cached Indicator indicator. Recursive indicators should extend this class. |
| `SchaffTrendCycleIndicator` | Schaff Trend Cycle (STC) indicator. Combines MACD momentum with a stochastic calculation to accelerate trend detection. |
| `SqueezeProIndicator` | Mobius/TTM Squeeze Pro momentum indicator (LazyBear/TradingView parity). The "Pro" variant exposes three compression levels, measured by how tightly the Bollinger Bands fit inside progressively wider Keltner Channels: HIGH: Bollinger Bands fully inside KC using kcHigh MID:… |
| `StochasticIndicator` | Stochastic indicator. Calculates the stochastic value of an indicator by comparing its current value to its highest and lowest values over a look-back period. |
| `StochasticOscillatorDIndicator` | Stochastic oscillator D. |
| `StochasticOscillatorKIndicator` | Stochastic oscillator K. |
| `StochasticRSIIndicator` | The Stochastic RSI Indicator. Stoch RSI = (RSI - MinimumRSIn) / (MaximumRSIn - MinimumRSIn) |
| `SwingPointMarkerIndicator` | Indicator that returns price values only at swing point indexes, and NaN elsewhere. This allows swing points to be displayed as markers on charts. |
| `TrueStrengthIndexIndicator` | True strength index (TSI) indicator. This oscillator double smooths price changes to quantify trend direction and momentum. |
| `UlcerIndexIndicator` | Ulcer index indicator. |
| `WilliamsRIndicator` | William's R indicator. |

## Скользящие средние — `averages` (24)

| Индикатор | Описание |
|---|---|
| `ATMAIndicator` | Asymmetric Triangular Moving Average (ATMA) Indicator. ATMA is a double-smoothing of the Simple Moving Average (SMA), however unlike the Triangular Moving Average (TMA) the modified equation creates a smoother and slightly lagged moving average compared to the traditional TMA. |
| `AbstractEMAIndicator` | Base class for Exponential Moving Average implementations. Provides robust NaN handling to prevent contamination of future values when invalid data is encountered. |
| `DMAIndicator` | Displaced Moving Average (DMA) indicator. Displaced Moving Average (DMA) is a variation of traditional moving averages, where the average is shifted forward or backward in time. |
| `DoubleEMAIndicator` | Double exponential moving average indicator. |
| `EDMAIndicator` | Exponential Displaced Moving Average (EDMA) Exponential Displaced Moving Average (EDMA) is a variation of the traditional Exponential Moving Average (EMA) where the calculated average is shifted in time by a specified number of periods. This displacement enhances trend… |
| `EMAIndicator` | Exponential moving average (EMA) indicator. The EMA gives more weight to recent prices and reacts more quickly to price changes than a simple moving average. |
| `HMAIndicator` | Hull moving average (HMA) indicator. |
| `JMAIndicator` | Jurik Moving Average (JMA) Indicator. JMA, or Jurik Moving Average, is a type of moving average developed by Mark Jurik. |
| `KAMAIndicator` | The Kaufman's Adaptive Moving Average (KAMA) Indicator. |
| `KiJunV2Indicator` | Kihon Moving Average Indicator (KiJunV2). Kihon Moving Average (KiJunV2) is a technical indicator derived from Ichimoku Kinko Hyo, a popular Japanese trading strategy. |
| `LSMAIndicator` | Least Squares Moving Average (LSMA) Indicator. Least Squares Moving Average (LSMA), also known as the Linear Regression Line or End Point Moving Average, is a unique type of moving average that minimizes the sum of squared differences between data points and a regression line. |
| `LWMAIndicator` | Linearly Weighted Moving Average (LWMA) indicator. |
| `MCGinleyMAIndicator` | McGinley Moving Average (McGinleyMA) Indicator. The McGinley Moving Average is a technical analysis tool developed by John McGinley to address issues with traditional moving averages such as lag and responsiveness to market volatility. |
| `MMAIndicator` | Modified moving average indicator. It is similar to exponential moving average but smooths more slowly. |
| `SGMAIndicator` | Savitzky-Golay Moving Average (SGMA) Indicator. Applies polynomial regression over a moving window to smooth data while preserving key features like peaks and trends. |
| `SMAIndicator` | Simple moving average (SMA) indicator. |
| `SMMAIndicator` | Smoothed Moving Average (SMMA) Indicator. Smoothed Moving Average (SMMA) is a type of moving average that applies exponential smoothing over a longer period. |
| `TMAIndicator` | Triangular Moving Average (TMA) Indicator. TMA is a double-smoothing of the Simple Moving Average (SMA). |
| `TripleEMAIndicator` | Triple exponential moving average indicator (also called "TRIX"). TEMA needs "3 * period - 2" of data to start producing values in contrast to the period samples needed by a regular EMA. |
| `VIDYAIndicator` | Chande's Variable Index Dynamic Moving Average (VIDYA) Indicator Chande’s Variable Index Dynamic Moving Average (VIDYA) is a sophisticated moving average developed by Tushar Chande. Unlike traditional moving averages, VIDYA adapts to market volatility by incorporating the Chande… |
| `VWMAIndicator` | Variable Weighted Moving Average (VWMA) indicator. Variable Weighted Moving Average (VWMA) is a type of moving average where each data point's weight is dynamically adjusted based on market conditions or other factors such as price, volume, or volatility. |
| `WMAIndicator` | WMA indicator. |
| `WildersMAIndicator` | Wilder's moving average indicator. The Wilder’s Moving Average indicator (Wilder’s Smoothed Moving Average ) was developed by Welles Wilder and introduced in his 1978 book, “New Concepts in Technical Trading Systems.” The WMA inherently "remembers" past values due to its… |
| `ZLEMAIndicator` | Zero-lag exponential moving average indicator. |

## Вспомогательные: ценовые ряды, операции, условия — `helpers` (40)

| Индикатор | Описание |
|---|---|
| `AmountIndicator` | Amount indicator. Returns the amount of a bar. |
| `AverageIndicator` | Indicator to calculate the average of a list of other indicators. |
| `BooleanLogicIndicator` ✨ | Boolean logic indicator. Applies a binary boolean logic operation (AND, OR, XOR, NAND, NOR) between two boolean indicators. |
| `BooleanNotIndicator` ✨ | Boolean NOT indicator. Negates the boolean value of the given indicator. |
| `BooleanTransformIndicator` | Transforms any indicator to a boolean indicator. |
| `CloseLocationValueIndicator` | Close Location Value (CLV) indicator. |
| `ClosePriceDifferenceIndicator` | Calculates the difference between the current and the previous close price. ClosePriceDifference = currentBarClosePrice - previousBarClosePrice |
| `ClosePriceIndicator` | Close price indicator. Returns the close price of a bar. |
| `ClosePriceRatioIndicator` | Calculates the ratio between the current and the previous close price. ClosePriceRatio = currentBarClosePrice / previousBarClosePrice |
| `CombineIndicator` | Combine indicator. Combines two Num indicators by using common math operations. |
| `ConstantIndicator` | Constant indicator. Returns a constant value for a bar. |
| `ConvergenceDivergenceIndicator` | Convergence-Divergence indicator. |
| `CrossIndicator` | Cross indicator. Boolean indicator that monitors the crossing of two indicators. |
| `CumulativeProductIndicator` ✨ | Cumulative Product indicator. Returns the running product (multiplicative accumulation) of all values of the given indicator from the beginning of the bar series up to the current index. |
| `DateTimeIndicator` | DateTime indicator. Returns a Instant of (or for) a bar. |
| `DifferenceIndicator` | Calculates the difference between the current and the previous value of an indicator. Difference = currentValue - previousValue |
| `DifferencePercentageIndicator` | Difference Percentage Indicator. Returns the percentage difference from the last time the percentageThreshold threshold was reached. |
| `FixedBooleanIndicator` | A fixed boolean indicator. Returns constant Boolean values for a bar. |
| `FixedIndicator` | A fixed indicator. Returns constant values for a bar. |
| `FixedNumIndicator` | A fixed Num indicator. Returns constant Num values for a bar. |
| `GainIndicator` | Gain indicator. Returns the difference of the indicator value of a bar and its previous bar if the indicator value of the current bar is greater than the indicator value of the previous bar (otherwise, Num.zero() is returned). |
| `HighPriceIndicator` | The high price indicator. Returns the high price of a bar. |
| `HighestValueIndicator` | Highest value indicator. Returns the highest indicator value from the bar series within the bar count. |
| `LossIndicator` | Loss indicator. Returns the difference of the indicator value of a bar and its previous bar if the indicator value of the current bar is less than the indicator value of the previous bar (otherwise, Num.zero() is returned). |
| `LowPriceIndicator` | Low price indicator. Returns the low price of a bar. |
| `LowestValueIndicator` | Lowest value indicator. Returns the lowest indicator value from the bar series within the bar count. |
| `MedianPriceIndicator` | Average high-low indicator. Returns the median price of a bar using the following formula: MedianPrice = (highPrice + lowPrice) / 2 |
| `NumIndicator` | Num indicator. Returns a Num of (or for) a bar. |
| `OpenPriceIndicator` | Open price indicator. Returns the open price of a bar. |
| `PercentRankIndicator` | Percent Rank indicator. Calculates the percentile rank of the current value within a rolling window of the specified period. |
| `PercentageChangeIndicator` | Calculates the percentage change between the current and the previous value of an indicator. This indicator uses rate of return representation (additive). |
| `PreviousValueIndicator` | Returns the (n-th) previous value of an indicator. If the (n-th) previous index is below the first index from the bar series, then NaN#NaN is returned. |
| `RunningTotalIndicator` | Running Total aka Cumulative Sum indicator |
| `StreakIndicator` | Streak indicator. Tracks consecutive up or down movements in the indicator values. |
| `SumIndicator` | Calculates the sum of all indicator values. Sum = summand0 + summand1 + ... |
| `TRIndicator` | True range indicator. TrueRange = MAX(high - low, high - previousClose, previousClose - low) |
| `TradeCountIndicator` | Trade count indicator. Returns the number of trades of a bar. |
| `TypicalPriceIndicator` | Typical price indicator. Returns the typical price of a bar using the following formula: TypicalPrice = (highPrice + lowPrice + closePrice) / 3 |
| `UnstableIndicator` | Indicator that returns NaN#NaN NaN in unstable bars. |
| `VolumeIndicator` | Volume indicator. Returns the sum of the total traded volumes from the bar series within the bar count. |

## Арифметические операции над рядами — `numeric` (3)

| Индикатор | Описание |
|---|---|
| `BinaryOperationIndicator` | Objects of this class defer evaluation of an arithmetic operation. This is a lightweight, non-cached implementation for binary operations between two indicators. |
| `NumericIndicator` | NumericIndicator is a "fluent decorator" for Indicator . It provides methods to create rules and other "lightweight" indicators, using a (hopefully) natural-looking and expressive series of method calls. |
| `UnaryOperationIndicator` | Objects of this class defer the evaluation of a unary operator, like sqrt(). There may be other unary operations on Num that could be added here. |

## Статистические индикаторы — `statistics` (11)

| Индикатор | Описание |
|---|---|
| `CorrelationCoefficientIndicator` | Correlation coefficient indicator. |
| `CovarianceIndicator` | Covariance indicator. |
| `MeanDeviationIndicator` | Mean deviation indicator. |
| `PearsonCorrelationIndicator` | Indicator-Pearson-Correlation |
| `PeriodicalGrowthRateIndicator` | Periodical Growth Rate indicator. In general the 'Growth Rate' is useful for comparing the average returns of investments in stocks or funds and can be used to compare the performance e.g. |
| `SigmaIndicator` | Sigma-Indicator (also called, "z-score" or "standard score"). |
| `SimpleLinearRegressionIndicator` | Simple linear regression indicator. A moving (i.e. |
| `StandardDeviationIndicator` | Standard deviation indicator. |
| `StandardErrorIndicator` | Standard error indicator. |
| `VarianceIndicator` | Variance indicator. |
| `XYRegressionIndicator` ✨ | XY linear regression indicator. Ordinary least squares regression of two indicator series (x-values from one series, y-values from another) over a rolling time frame. |

## Время и календарь (новый пакет) — `time` (36)

| Индикатор | Описание |
|---|---|
| `AbstractCalendarIndicator` ✨ | Abstract base class for calendar indicators. Converts the Bar#getBeginTime() of a bar into a ZonedDateTime in the configured zone (defaults to the system zone) so that calendar fields can be derived from it. |
| `AbstractDayIndicator` ✨ | Abstract base class for intraday trading-day indicators. All subclasses accumulate values of the intraday bars belonging to the same trading day (in the configured zone) as the current bar, i.e. they return the open, high, low, close, volume or range of the day that contains the current bar. On daily or coarser series… |
| `AbstractDayOfWeekFlagIndicator` ✨ | Abstract base class for the day-of-week flag indicators (SundayFlag ... SaturdayFlag). Returns 1 when the trading day of the bar equals the configured ISO day-of-week, otherwise 0. |
| `AbstractMonthFlagIndicator` ✨ | Abstract base class for the month flag indicators (JanuaryFlag ... DecemberFlag). Returns 1 when the trading month of the bar equals the configured month of year, otherwise 0. |
| `AprilFlagIndicator` ✨ | April flag indicator. Returns 1 when the trading month of the bar is April, otherwise 0. |
| `AugustFlagIndicator` ✨ | August flag indicator. Returns 1 when the trading month of the bar is August, otherwise 0. |
| `DayCloseIndicator` ✨ | Day Close indicator. On an intraday series, returns the close price of the current bar, i.e. the running "close to date" of the current trading day. |
| `DayHighIndicator` ✨ | Day High indicator. On an intraday series, returns the highest high price of the current trading day, accumulated from the bars of that day up to the current bar. |
| `DayLowIndicator` ✨ | Day Low indicator. On an intraday series, returns the lowest low price of the current trading day, accumulated from the bars of that day up to the current bar. |
| `DayOfMonthIndicator` ✨ | Day of month indicator. Returns the day-of-month (1..31) of the bar's begin time. |
| `DayOfWeekIndicator` ✨ | Day of week indicator. Returns the ISO day-of-week number of the bar's begin time: 1 = Monday, ..., 7 = Sunday. |
| `DayOpenIndicator` ✨ | Day Open indicator. On an intraday series, returns the open price of the current trading day (the open of the first bar of that day). |
| `DayRangeIndicator` ✨ | Day Range indicator. On an intraday series, returns the difference between the day high and the day low of the current trading day (accumulated up to the current bar). |
| `DayVolumeIndicator` ✨ | Day Volume indicator. On an intraday series, returns the accumulated volume of the current trading day up to and including the current bar. |
| `DaysUntilThirdFridayIndicator` ✨ | Days until third Friday of month indicator. Returns the number of days from the bar's date until the third Friday of the month (0 if the bar's date is that Friday). If the third Friday of the current month has already passed, the third Friday of the next month is used, searched up to 12 months ahead. |
| `DecemberFlagIndicator` ✨ | December flag indicator. Returns 1 when the trading month of the bar is December, otherwise 0. |
| `FebruaryFlagIndicator` ✨ | February flag indicator. Returns 1 when the trading month of the bar is February, otherwise 0. |
| `FridayFlagIndicator` ✨ | Friday flag indicator. Returns 1 when the trading day of the bar is a Friday, otherwise 0. |
| `HourOfDayIndicator` ✨ | Hour of day indicator. Returns the hour-of-day (0..23) of the bar's begin time. |
| `JanuaryFlagIndicator` ✨ | January flag indicator. Returns 1 when the trading month of the bar is January, otherwise 0. |
| `JulyFlagIndicator` ✨ | July flag indicator. Returns 1 when the trading month of the bar is July, otherwise 0. |
| `JuneFlagIndicator` ✨ | June flag indicator. Returns 1 when the trading month of the bar is June, otherwise 0. |
| `MarchFlagIndicator` ✨ | March flag indicator. Returns 1 when the trading month of the bar is March, otherwise 0. |
| `MayFlagIndicator` ✨ | May flag indicator. Returns 1 when the trading month of the bar is May, otherwise 0. |
| `MinuteOfHourIndicator` ✨ | Minute of hour indicator. Returns the minute-of-hour (0..59) of the bar's begin time. |
| `MondayFlagIndicator` ✨ | Monday flag indicator. Returns 1 when the trading day of the bar is a Monday, otherwise 0. |
| `MonthOfYearIndicator` ✨ | Month of year indicator. Returns the month-of-year (1 = January, ..., 12 = December) of the bar's begin time. |
| `NovemberFlagIndicator` ✨ | November flag indicator. Returns 1 when the trading month of the bar is November, otherwise 0. |
| `OctoberFlagIndicator` ✨ | October flag indicator. Returns 1 when the trading month of the bar is October, otherwise 0. |
| `SaturdayFlagIndicator` ✨ | Saturday flag indicator. Returns 1 when the trading day of the bar is a Saturday, otherwise 0. |
| `SeptemberFlagIndicator` ✨ | September flag indicator. Returns 1 when the trading month of the bar is September, otherwise 0. |
| `SundayFlagIndicator` ✨ | Sunday flag indicator. Returns 1 when the trading day of the bar is a Sunday, otherwise 0. |
| `ThursdayFlagIndicator` ✨ | Thursday flag indicator. Returns 1 when the trading day of the bar is a Thursday, otherwise 0. |
| `TimeOfDayIndicator` ✨ | Time of day indicator. Returns the number of seconds since midnight of the bar's begin time, in the configured zone. Together with comparison operations this implements tradetime filters like Time >= X or X <= Time <= Y. |
| `TuesdayFlagIndicator` ✨ | Tuesday flag indicator. Returns 1 when the trading day of the bar is a Tuesday, otherwise 0. |
| `WednesdayFlagIndicator` ✨ | Wednesday flag indicator. Returns 1 when the trading day of the bar is a Wednesday, otherwise 0. |

## Определение тренда — `trend` (2)

| Индикатор | Описание |
|---|---|
| `DownTrendIndicator` | Boolean indicator: true if the close price of the bar is lower than the close of the previous bar. |
| `UpTrendIndicator` | Boolean indicator: true if the close price of the bar is higher than the close of the previous bar. |

## ADX / DMI / DX (Wilder) — `adx` (6)

| Индикатор | Описание |
|---|---|
| `ADXIndicator` | ADX indicator. Part of the Directional Movement System. |
| `DXIndicator` | DX indicator. Part of the Directional Movement System. |
| `MinusDIIndicator` | -DI indicator. Part of the Directional Movement System. |
| `MinusDMIndicator` | -DM indicator. Part of the Directional Movement System. |
| `PlusDIIndicator` | +DI indicator. Part of the Directional Movement System. |
| `PlusDMIndicator` | +DM indicator. Part of the Directional Movement System. |

## Aroon — `aroon` (3)

| Индикатор | Описание |
|---|---|
| `AroonDownIndicator` | Aroon down indicator. |
| `AroonOscillatorIndicator` | Aroon Oscillator. |
| `AroonUpIndicator` | Aroon up indicator. |

## Полосы Боллинджера — `bollinger` (5)

| Индикатор | Описание |
|---|---|
| `BollingerBandWidthIndicator` | Bollinger BandWidth indicator. |
| `BollingerBandsLowerIndicator` | Buy - Occurs when the price line crosses from below to above the Lower Bollinger Band. Sell - Occurs when the price line crosses from above to below the Upper Bollinger Band. |
| `BollingerBandsMiddleIndicator` | Buy - Occurs when the price line crosses from below to above the Lower Bollinger Band. Sell - Occurs when the price line crosses from above to below the Upper Bollinger Band. |
| `BollingerBandsUpperIndicator` | Buy - Occurs when the price line crosses from below to above the Lower Bollinger Band. Sell - Occurs when the price line crosses from above to below the Upper Bollinger Band. |
| `PercentBIndicator` | %B indicator. |

## Канал Дончиана — `donchian` (3)

| Индикатор | Описание |
|---|---|
| `DonchianChannelLowerIndicator` | https://www.investopedia.com/terms/d/donchianchannels.asp |
| `DonchianChannelMiddleIndicator` | * https://www.investopedia.com/terms/d/donchianchannels.asp |
| `DonchianChannelUpperIndicator` | * https://www.investopedia.com/terms/d/donchianchannels.asp |

## Канал Кельтнера — `keltner` (3)

| Индикатор | Описание |
|---|---|
| `KeltnerChannelLowerIndicator` | Keltner Channel (lower line) indicator. |
| `KeltnerChannelMiddleIndicator` | Keltner Channel (middle line) indicator. |
| `KeltnerChannelUpperIndicator` | Keltner Channel (upper line) indicator. |

## SuperTrend — `supertrend` (3)

| Индикатор | Описание |
|---|---|
| `SuperTrendIndicator` | The SuperTrend indicator. SuperTrend is a trend-following indicator that uses Average True Range (ATR) to calculate dynamic support and resistance levels. |
| `SuperTrendLowerBandIndicator` | The lower band of the SuperTrend indicator. The lower band acts as dynamic support during an uptrend. |
| `SuperTrendUpperBandIndicator` | The upper band of the SuperTrend indicator. The upper band acts as dynamic resistance during a downtrend. |

## ZigZag — `zigzag` (5)

| Индикатор | Описание |
|---|---|
| `RecentZigZagSwingHighIndicator` | Recent ZigZag Swing High Indicator. Returns the price value of the most recently confirmed ZigZag swing high at each bar. |
| `RecentZigZagSwingLowIndicator` | Recent ZigZag Swing Low Indicator. Returns the price value of the most recently confirmed ZigZag swing low at each bar. |
| `ZigZagPivotHighIndicator` | ZigZag Pivot High Indicator. This indicator returns true at the bar where a new ZigZag swing high is confirmed. |
| `ZigZagPivotLowIndicator` | ZigZag Pivot Low Indicator. This indicator returns true at the bar where a new ZigZag swing low is confirmed. |
| `ZigZagStateIndicator` | ZigZag State Indicator. The ZigZag indicator filters out price movements below a specified threshold to identify significant price reversals and swing points. |

## Свечные паттерны — `candles` (25)

| Индикатор | Описание |
|---|---|
| `AbstractMarubozuIndicator` | Shared logic for Marubozu-style candlestick pattern indicators. A Marubozu candle is characterised by a long real body with very small upper and lower shadows. |
| `BearishEngulfingIndicator` | Bearish engulfing pattern indicator. |
| `BearishHaramiIndicator` | Bearish Harami pattern indicator. |
| `BearishKickerIndicator` | Bearish kicker candle indicator. |
| `BearishMarubozuIndicator` | Bearish Marubozu candlestick pattern indicator. A bearish Marubozu is a long-bodied bearish candle where both shadows are very small compared to the body, signalling strong downward momentum. |
| `BullishEngulfingIndicator` | Bullish engulfing pattern indicator. |
| `BullishHaramiIndicator` | Bearish Harami pattern indicator. |
| `BullishKickerIndicator` | Bullish kicker candle indicator. |
| `BullishMarubozuIndicator` | Bullish Marubozu candlestick pattern indicator. A bullish Marubozu is a long-bodied bullish candle where both shadows are very small compared to the body, signalling strong upward momentum. |
| `DarkCloudIndicator` | Dark cloud candle indicator. |
| `DojiIndicator` | Doji indicator. A candle/bar is considered Doji if its body height is lower than the average multiplied by a factor. |
| `EveningStarIndicator` | Evening star candle indicator. |
| `HammerIndicator` | Hammer candle indicator. |
| `HangingManIndicator` | Hanging man candle indicator. |
| `InvertedHammerIndicator` | Inverted hammer candle indicator. |
| `LowerShadowIndicator` | Lower shadow height indicator. Provides the (absolute) difference between the low price and the lowest price of the candle body. |
| `MorningStarIndicator` | Morning star candle indicator. |
| `PiercingIndicator` | Piercing candle indicator. |
| `RealBodyIndicator` | Real (candle) body height indicator. Provides the (relative) difference between the open price and the close price of a bar. |
| `ShootingStarIndicator` | Shooting star candle indicator. |
| `ThreeBlackCrowsIndicator` | Three black crows indicator. |
| `ThreeInsideDownIndicator` | Three inside down candle indicator. |
| `ThreeInsideUpIndicator` | Three inside up candle indicator. |
| `ThreeWhiteSoldiersIndicator` | Three white soldiers indicator. |
| `UpperShadowIndicator` | Upper shadow height indicator. Provides the (absolute) difference between the high price and the highest price of the candle body. |

## Ichimoku — `ichimoku` (6)

| Индикатор | Описание |
|---|---|
| `IchimokuChikouSpanIndicator` | Ichimoku clouds: Chikou Span indicator. |
| `IchimokuKijunSenIndicator` | Ichimoku clouds: Kijun-sen (Base line) indicator. |
| `IchimokuLineIndicator` | An abstract class for Ichimoku clouds indicators. |
| `IchimokuSenkouSpanAIndicator` | Ichimoku clouds: Senkou Span A (Leading Span A) indicator. |
| `IchimokuSenkouSpanBIndicator` | Ichimoku clouds: Senkou Span B (Leading Span B) indicator. |
| `IchimokuTenkanSenIndicator` | Ichimoku clouds: Tenkan-sen (Conversion line) indicator. |

## Пивот-пойнты — `pivotpoints` (6)

| Индикатор | Описание |
|---|---|
| `AbstractPivotPointIndicator` | Abstract base class for pivot point indicators that calculate pivot points based on time periods. This class provides common functionality for determining which bars belong to the previous period (day, week, month, year) based on the configured TimeLevel. |
| `DeMarkPivotPointIndicator` | DeMark Pivot Point indicator. The java.time.Instant UTC represents a point in time on the time-line, typically measured in milliseconds. |
| `DeMarkReversalIndicator` | DeMark Reversal Indicator. |
| `FibonacciReversalIndicator` | Fibonacci Reversal Indicator. |
| `PivotPointIndicator` | Pivot Point indicator. The java.time.Instant UTC represents a point in time on the time-line, typically measured in milliseconds. |
| `StandardReversalIndicator` | Pivot Reversal Indicator. |

## Renko — `renko` (3)

| Индикатор | Описание |
|---|---|
| `RenkoDownIndicator` | Detects bearish Renko sequences where price has fallen by a configured number of bricks since the previous close. A Renko brick is produced whenever price moves by the configured point size. |
| `RenkoUpIndicator` | Detects bullish Renko sequences where the price has advanced by a configured number of bricks since the previous close. A Renko brick is produced whenever price moves by the configured point size. |
| `RenkoXIndicator` | Detects exhaustion moves where price prints a configured number of Renko bricks consecutively in either direction. Renko traders often look for three or more bricks in the same direction to confirm momentum before anticipating a potential reversal (see Investopedia ). |

## Линии поддержки/сопротивления — `supportresistance` (3)

| Индикатор | Описание |
|---|---|
| `AbstractTrendLineIndicator` | Abstract base for trend line indicators that rely on previously confirmed swing highs or lows. The indicator computes the best trend line for the latest window of barCount bars anchored at the series end. |
| `TrendLineResistanceIndicator` | Projects a resistance trend line that spans a configurable look-back window of swing highs. The indicator selects the straight line that maximizes a scoring function across confirmed swing highs within the window. |
| `TrendLineSupportIndicator` | Projects a rising or falling support trend line that spans a configurable look-back window of swing lows. The indicator selects the straight line that maximizes a scoring function across confirmed swing lows within the window. |

## Объёмные индикаторы — `volume` (13)

| Индикатор | Описание |
|---|---|
| `AccumulationDistributionIndicator` | Accumulation-distribution indicator. |
| `ChaikinMoneyFlowIndicator` | Chaikin Money Flow (CMF) indicator. |
| `ChaikinOscillatorIndicator` | Chaikin Oscillator. |
| `IIIIndicator` | Intraday Intensity Index. |
| `MVWAPIndicator` | The Moving volume weighted average price (MVWAP) Indicator. |
| `MoneyFlowIndexIndicator` | Money Flow Index (MFI) indicator. MFI is a volume-weighted version of RSI that shows shifts in buying and selling pressure. |
| `NVIIndicator` | Negative Volume Index (NVI) indicator. |
| `OnBalanceVolumeIndicator` | On-balance volume indicator. |
| `PVIIndicator` | Positive Volume Index (PVI) indicator. |
| `ROCVIndicator` | Rate of change of volume (ROCVIndicator) indicator (also called "Momentum of Volume"). The ROCVIndicator calculation compares the current volume with the volume "n" periods ago. |
| `RelativeVolumeStandardDeviationIndicator` | Relative Volume Standard Deviation Indicator. This class is an indicator that calculates the standard deviation of the relative volume. |
| `TimeSegmentedVolumeIndicator` | Time Segmented Volume (TSV) indicator. This class calculates the Time Segmented Volume (TSV). |
| `VWAPIndicator` | The volume-weighted average price (VWAP) Indicator. |

## Анализ волн Эллиотта — `elliott` (11)

| Индикатор | Описание |
|---|---|
| `ElliottChannelIndicator` | Projects an Elliott price channel based on the most recent swing highs and lows. The indicator mirrors the rolling-window design of org.ta4j.core.indicators.pivotpoints.PivotPointIndicator by recalculating the projected channel on each bar using the latest two swing highs and… |
| `ElliottConfluenceIndicator` | Aggregates Fibonacci ratio and channel alignment checks into a confluence score suitable for trading rules. The score increases when the current price is near multiple Fibonacci retracement/extension levels and aligned with the projected Elliott channel. |
| `ElliottInvalidationIndicator` | Flags when the current Elliott wave count breaks canonical invalidation levels. Typical invalidations include wave two exceeding the start of wave one or wave four overlapping wave one territory. |
| `ElliottInvalidationLevelIndicator` | Calculates the invalidation price level for Elliott wave scenarios. Returns the price that, if breached, would invalidate the primary wave count. |
| `ElliottPhaseIndicator` | Tracks the current Elliott wave phase by reusing cached swing history. The indicator produces ElliottPhase values representing the latest impulsive or corrective wave. |
| `ElliottProjectionIndicator` | Calculates Fibonacci-based price projections for Elliott wave structures. Returns the primary target price based on the current wave phase. |
| `ElliottRatioIndicator` | Calculates Fibonacci-style ratios between consecutive Elliott swings. The indicator classifies the latest swing as either a retracement or an extension: Retracement : amplitude of the latest swing divided by the amplitude of the most recent prior swing with the opposite… |
| `ElliottScenarioIndicator` | Indicator that produces a set of ranked Elliott wave scenarios for each bar. Unlike ElliottPhaseIndicator which returns a single deterministic phase, this indicator generates multiple alternative interpretations ranked by confidence score. |
| `ElliottSwingIndicator` | Detects Elliott swings (alternating pivots) based on confirmed swing highs and lows. This indicator composes two RecentSwingIndicator instances: one that reports swing highs and one that reports swing lows. |
| `ElliottTrendBiasIndicator` | Computes the aggregate directional bias across Elliott wave scenarios. Use this indicator to derive a single bullish/bearish/neutral signal from a scenario set. |
| `ElliottWaveCountIndicator` | Counts swings provided by an ElliottSwingIndicator instance. Use this indicator when you need a quick measure of how many swings are in the current structure. |

## DSL выражений — `expression` (1)

Индикатор, собирающий дерево ta4j-индикаторов из строки-выражения. Синтаксис, разрешённые имена и арности — в [`EXPRESSION_GUIDE.md`](EXPRESSION_GUIDE.md).

| Индикатор | Описание |
|---|---|
| `ExpressionIndicator` ✨ | Indicator that is computed from a text expression. The expression is first parsed and validated (function names, arity, identifiers, parentheses) and then compiled into a tree of ta4j indicators; evaluation afterwards only walks that tree, so the cost of parsing/validation is paid once. Two interchangeable notations… |

## Приложение: служебные классы (не индикаторы)

- **`aroon`**: AroonFacade
- **`bollinger`**: BollingerBandFacade
- **`core`**: CachedBuffer, PriceChannel
- **`donchian`**: DonchianChannelFacade
- **`expression`**: ExpressionArgument, ExpressionFunction, ExpressionParser, ExpressionValidationException, FunctionRegistry
- **`elliott`**: ElliottAnalysisResult, ElliottChannel, ElliottConfidence, ElliottConfidenceScorer, ElliottDegree, ElliottFibonacciValidator, ElliottPhase, ElliottRatio, ElliottScenario, ElliottScenarioComparator, ElliottScenarioGenerator, ElliottScenarioSet, ElliottSwing, ElliottSwingCompressor, ElliottSwingMetadata, ElliottTrendBias, ElliottWaveAnalyzer, ElliottWaveFacade, PatternSet, ScenarioType, ChannelAdherenceFactor, ConfidenceFactor, ConfidenceFactorCategory, ConfidenceFactorResult, ConfidenceModel, ConfidenceProfile, ConfidenceProfiles, ElliottConfidenceBreakdown, ElliottConfidenceContext, FibonacciRelationshipFactor, ScenarioTypeConfidenceModel, StructureCompletenessFactor, TimeAlternationFactor, TimeProportionFactor, AdaptiveZigZagConfig, AdaptiveZigZagSwingDetector, CompositeSwingDetector, FractalSwingDetector, MinMagnitudeSwingFilter, SwingDetector, SwingDetectorResult, SwingDetectorSupport, SwingDetectors, SwingFilter, SwingPivot, SwingPivotType, ZigZagSwingDetector
- **`keltner`**: KeltnerChannelFacade
- **`pivotpoints`**: PivotLevel, TimeLevel
- **`renko`**: RenkoCounter
- **`zigzag`**: ZigZagState, ZigZagTrend
