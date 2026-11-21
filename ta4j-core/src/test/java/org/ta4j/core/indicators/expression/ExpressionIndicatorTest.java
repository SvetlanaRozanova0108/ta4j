/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.AverageIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class ExpressionIndicatorTest extends AbstractIndicatorTest<Indicator<Num>, Num> {

    private BarSeries series;

    public ExpressionIndicatorTest(NumFactory numFactory) {
        super(numFactory);
    }

    @Before
    public void setUp() {
        series = new BaseBarSeriesBuilder().withNumFactory(numFactory).withName("expr").build();
        double[] closes = { 10, 11, 12, 11, 13, 14, 13, 15, 16, 15, 17, 18, 17, 19, 20, 19, 21, 22, 23, 22 };
        Instant begin = Instant.parse("2024-01-02T00:00:00Z");
        double previousClose = 10;
        for (int i = 0; i < closes.length; i++) {
            double open = previousClose;
            double close = closes[i];
            double high = Math.max(open, close) + 1;
            double low = Math.min(open, close) - 1;
            Instant barBegin = begin.plus(Duration.ofDays(i));
            series.barBuilder().beginTime(barBegin).endTime(barBegin.plus(Duration.ofDays(1)))
                    .openPrice(open).highPrice(high).lowPrice(low).closePrice(close)
                    .volume(100 + i).amount(close * (100 + i)).trades(1).add();
            previousClose = close;
        }
    }

    // ---------------------------------------------------------- equivalence

    @Test
    public void funcCallNotationMatchesDirectIndicator() {
        Indicator<Num> expected = new SMAIndicator(new ClosePriceIndicator(series), 3);
        Indicator<Num> actual = new ExpressionIndicator(series, "SMA(Close, 3)");
        for (int i = 2; i <= series.getEndIndex(); i++) {
            assertNumEquals(expected.getValue(i), actual.getValue(i));
        }
    }

    @Test
    public void lispNotationMatchesFuncCallNotation() {
        Indicator<Num> funcStyle = new ExpressionIndicator(series, "SMA(Close, 3)");
        Indicator<Num> lispStyle = new ExpressionIndicator(series, "(SMA Close 3)");
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertNumEquals(funcStyle.getValue(i), lispStyle.getValue(i));
        }
    }

    @Test
    public void avgOfHighLowMatchesAverageIndicator() {
        Indicator<Num> expected = new AverageIndicator(new HighPriceIndicator(series),
                new LowPriceIndicator(series));
        Indicator<Num> actual = new ExpressionIndicator(series, "Avg(High, Low)");
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertNumEquals(expected.getValue(i), actual.getValue(i));
        }
    }

    @Test
    public void userExampleComputesComparisonOfSmoothedRsi() {
        Indicator<Num> mid = new AverageIndicator(new HighPriceIndicator(series), new LowPriceIndicator(series));
        Indicator<Num> rsi = new RSIIndicator(mid, 5);
        Indicator<Num> smoothed = new SMAIndicator(rsi, 10);

        Indicator<Num> lisp = new ExpressionIndicator(series,
                "(> (SMA (RSI (AVG High Low) 5) 10) (RSI (AVG High Low) 5))");
        Indicator<Num> call = new ExpressionIndicator(series,
                "Greater(SMA(RSI(AVG(High, Low), 5), 10), RSI(AVG(High, Low), 5))");

        for (int i = 0; i <= series.getEndIndex(); i++) {
            boolean expectedValue = smoothed.getValue(i).doubleValue() > rsi.getValue(i).doubleValue();
            double expected = expectedValue ? 1 : 0;
            assertNumEquals(expected, lisp.getValue(i));
            assertNumEquals(expected, call.getValue(i));
        }
    }

    @Test
    public void parametersReplaceLiterals() {
        Map<String, Number> parameters = Map.of("rsiPeriod", 5, "smaPeriod", 10);
        Indicator<Num> withParams = new ExpressionIndicator(series,
                "SMA(RSI(AVG(High, Low), rsiPeriod), smaPeriod)", parameters);
        Indicator<Num> withLiterals = new ExpressionIndicator(series,
                "SMA(RSI(AVG(High, Low), 5), 10)");
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertNumEquals(withLiterals.getValue(i), withParams.getValue(i));
        }
    }

    // ------------------------------------------------------- arithmetic/logic

    @Test
    public void arithmeticFunctions() {
        assertNumEquals(6, new ExpressionIndicator(series, "Add(1, 2, 3)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Subtract(Divide(10, 2), Multiply(2, 2))").getValue(0));
        assertNumEquals(2.5, new ExpressionIndicator(series, "(Divide (Add 2 3) 2)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Min(1, 2)").getValue(0));
        assertNumEquals(2, new ExpressionIndicator(series, "Max(1, 2)").getValue(0));
        assertNumEquals(2, new ExpressionIndicator(series, "Abs(-2)").getValue(0));
        assertNumEquals(3, new ExpressionIndicator(series, "Sqrt(9)").getValue(0));
        assertNumEquals(8, new ExpressionIndicator(series, "Pow(2, 3)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Modulo(7, 3)").getValue(0));
    }

    @Test
    public void comparisonsYieldOneOrZero() {
        assertNumEquals(1, new ExpressionIndicator(series, "GreaterThan(2, 1)").getValue(0));
        assertNumEquals(0, new ExpressionIndicator(series, "(> 1 2)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "(<= 1 2)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "(= 2 2)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "(!= 2 3)").getValue(0));
    }

    @Test
    public void logicFunctions() {
        assertNumEquals(1, new ExpressionIndicator(series, "And(1, 2)").getValue(0));
        assertNumEquals(0, new ExpressionIndicator(series, "And(1, 0)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Or(0, 3)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Not(0)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Not(GreaterThan(1, 2))").getValue(0));
    }

    @Test
    public void comparisonAgainstIndicator() {
        Indicator<Num> cmp = new ExpressionIndicator(series, "(> Close (SMA Close 5))");
        Indicator<Num> close = new ClosePriceIndicator(series);
        Indicator<Num> sma = new SMAIndicator(close, 5);
        for (int i = 4; i <= series.getEndIndex(); i++) {
            double expected = close.getValue(i).doubleValue() > sma.getValue(i).doubleValue() ? 1 : 0;
            assertNumEquals(expected, cmp.getValue(i));
        }
    }

    // ------------------------------------------------------------- nesting

    @Test
    public void deepNestingIsNotLimited() {
        StringBuilder expression = new StringBuilder("Close");
        for (int i = 0; i < 50; i++) {
            expression.insert(0, "SMA(").append(", 2)");
        }
        Indicator<Num> indicator = new ExpressionIndicator(series, expression.toString());
        Indicator<Num> expected = new ClosePriceIndicator(series);
        for (int i = 0; i < 50; i++) {
            expected = new SMAIndicator(expected, 2);
        }
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertNumEquals(expected.getValue(i), indicator.getValue(i));
        }
    }

    @Test
    public void mixedNotations() {
        Indicator<Num> mixed = new ExpressionIndicator(series, "(> (SMA Close, 3) (EMA Close 3))");
        Indicator<Num> sma = new SMAIndicator(new ClosePriceIndicator(series), 3);
        Indicator<Num> ema = new EMAIndicator(new ClosePriceIndicator(series), 3);
        for (int i = 0; i <= series.getEndIndex(); i++) {
            double expected = sma.getValue(i).doubleValue() > ema.getValue(i).doubleValue() ? 1 : 0;
            assertNumEquals(expected, mixed.getValue(i));
        }
    }

    // ----------------------------------------------------------- validation

    @Test
    public void validateAcceptsKnownSyntax() {
        ExpressionIndicator.validate("SMA(RSI(AVG(High, Low), 5), 10)", Map.of());
        ExpressionIndicator.validate("(> (SMA (RSI (AVG High Low) rsiPeriod) 10) (RSI (AVG High Low) rsiPeriod))",
                Map.of("rsiPeriod", 5));
    }

    @Test
    public void validateRejectsUnknownFunction() {
        ExpressionValidationException e = assertThrows(ExpressionValidationException.class,
                () -> ExpressionIndicator.validate("FOO(Close)", Map.of()));
        assertTrue(e.getMessage().contains("FOO"));
        assertEquals(0, e.getPosition());
    }

    @Test
    public void validateRejectsUnknownIdentifier() {
        assertThrows(ExpressionValidationException.class,
                () -> ExpressionIndicator.validate("SMA(Nope, 2)", Map.of()));
    }

    @Test
    public void validateRejectsWrongArity() {
        assertThrows(ExpressionValidationException.class,
                () -> ExpressionIndicator.validate("SMA(Close)", Map.of()));
        assertThrows(ExpressionValidationException.class,
                () -> ExpressionIndicator.validate("Subtract(1, 2, 3)", Map.of()));
    }

    @Test
    public void validateRejectsUnbalancedParentheses() {
        assertThrows(ExpressionValidationException.class,
                () -> ExpressionIndicator.validate("SMA(Close, 2", Map.of()));
        assertThrows(ExpressionValidationException.class,
                () -> ExpressionIndicator.validate("SMA(Close, 2))", Map.of()));
    }

    @Test
    public void validateRejectsMissingParameter() {
        assertThrows(ExpressionValidationException.class,
                () -> ExpressionIndicator.validate("SMA(RSI(AVG(High, Low), rsiPeriod), 10)", Map.of()));
    }

    @Test
    public void compileRejectsInvalidPeriod() {
        assertThrows(ExpressionValidationException.class,
                () -> new ExpressionIndicator(series, "SMA(Close, 0)"));
        assertThrows(ExpressionValidationException.class,
                () -> new ExpressionIndicator(series, "SMA(Close, 2.5)"));
    }

    // --------------------------------------------------------- extensibility

    @Test
    public void autoRegisteredFunctions() {
        // (BarSeries, int) signature indicator auto-registered at startup
        assertTrue(ExpressionIndicator.getFunctionNames().contains("WILLIAMSR"));
        // (BarSeries) signature calendar indicators and flags
        assertTrue(ExpressionIndicator.getFunctionNames().contains("DAYOFWEEK"));
        assertTrue(ExpressionIndicator.getFunctionNames().contains("SUNDAYFLAG"));

        Indicator<Num> auto = new ExpressionIndicator(series, "(WilliamsR 5)");
        Indicator<Num> expected = new org.ta4j.core.indicators.WilliamsRIndicator(series, 5);
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertNumEquals(expected.getValue(i), auto.getValue(i));
        }

        // 2024-01-02 is a Tuesday
        assertNumEquals(0, new ExpressionIndicator(series, "MondayFlag").getValue(0));
    }

    @Test
    public void trigLogicAndRegressionFunctions() {
        assertNumEquals(0.0, new ExpressionIndicator(series, "Sin(0)").getValue(0));
        assertNumEquals(1.0, new ExpressionIndicator(series, "CosD(0)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Xor(1, 0)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "Nand(0, 1)").getValue(0));
        assertNumEquals(1, new ExpressionIndicator(series, "(Nor 0 0)").getValue(0));
        // a series correlated with itself has R = 1
        assertNumEquals(1.0, new ExpressionIndicator(series, "XYRegressionR(Close, Close, 5)").getValue(4));
    }

    @Test
    public void customFunctionRegistration() {
        try {
            ExpressionIndicator.registerFunction("Double", 1, 1,
                    (args, s) -> new org.ta4j.core.indicators.numeric.BinaryOperationIndicator(
                            org.ta4j.core.indicators.numeric.BinaryOperationIndicator.Operation.PLUS,
                            args.get(0).indicator(), args.get(0).indicator()));
            assertNumEquals(20, new ExpressionIndicator(series, "Double(Close)").getValue(0));
            assertNumEquals(20, new ExpressionIndicator(series, "(Double Close)").getValue(0));
            assertTrue(ExpressionIndicator.getFunctionNames().contains("DOUBLE"));
        } catch (IllegalArgumentException alreadyRegistered) {
            // another parameterized test case already registered the function
            assertNumEquals(20, new ExpressionIndicator(series, "Double(Close)").getValue(0));
        }
    }

    // ---------------------------------------------------------- NSTRDIND functional forms

    @Test
    public void nstrdindOperatorFunctionForms() {
        // "A>B" is one function-name token with its two operands in parentheses; equals the word form.
        Indicator<Num> gt = new ExpressionIndicator(series, "A>B(Close, SMA(Close, 3))");
        Indicator<Num> gtRef = new ExpressionIndicator(series, "Greater(Close, SMA(Close, 3))");
        Indicator<Num> ge = new ExpressionIndicator(series, "A>=B(Close, SMA(Close, 3))");
        Indicator<Num> geRef = new ExpressionIndicator(series, "GreaterOrEqual(Close, SMA(Close, 3))");
        Indicator<Num> ne = new ExpressionIndicator(series, "A!=B(Close, SMA(Close, 3))");
        Indicator<Num> neRef = new ExpressionIndicator(series, "NotEqual(Close, SMA(Close, 3))");
        Indicator<Num> sum = new ExpressionIndicator(series, "A+B(Close, Close)");
        Indicator<Num> sumRef = new ExpressionIndicator(series, "Add(Close, Close)");
        Indicator<Num> prod = new ExpressionIndicator(series, "A*B(Close, 2)");
        Indicator<Num> prodRef = new ExpressionIndicator(series, "Multiply(Close, 2)");
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertNumEquals(gtRef.getValue(i), gt.getValue(i));
            assertNumEquals(geRef.getValue(i), ge.getValue(i));
            assertNumEquals(neRef.getValue(i), ne.getValue(i));
            assertNumEquals(sumRef.getValue(i), sum.getValue(i));
            assertNumEquals(prodRef.getValue(i), prod.getValue(i));
        }
        // logic stays And/Or/Not and nests the A>B comparisons; must equal the word-function form
        Indicator<Num> logic = new ExpressionIndicator(series,
                "And(A>B(Close, SMA(Close, 3)), A<B(Close, 1000))");
        Indicator<Num> logicRef = new ExpressionIndicator(series,
                "And(Greater(Close, SMA(Close, 3)), Less(Close, 1000))");
        for (int i = 3; i <= series.getEndIndex(); i++) {
            assertNumEquals(logicRef.getValue(i), logic.getValue(i));
        }
    }

    @Test
    public void nstrdindNameAliasesMatchTa4jIndicators() {
        Indicator<Num> ema = new ExpressionIndicator(series, "ExpAvg(Close, 3)");
        Indicator<Num> sma = new ExpressionIndicator(series, "MovAvg(Close, 3)");
        Indicator<Num> wma = new ExpressionIndicator(series, "LinWgtAvg(Close, 3)");
        Indicator<Num> vwma = new ExpressionIndicator(series, "VolWgtAvg(Close, 3)");
        Indicator<Num> emaRef = new ExpressionIndicator(series, "EMA(Close, 3)");
        Indicator<Num> smaRef = new ExpressionIndicator(series, "SMA(Close, 3)");
        Indicator<Num> wmaRef = new ExpressionIndicator(series, "WMA(Close, 3)");
        Indicator<Num> vwmaRef = new ExpressionIndicator(series, "VWMA(Close, 3)");
        for (int i = 3; i <= series.getEndIndex(); i++) {
            assertNumEquals(emaRef.getValue(i), ema.getValue(i));
            assertNumEquals(smaRef.getValue(i), sma.getValue(i));
            assertNumEquals(wmaRef.getValue(i), wma.getValue(i));
            assertNumEquals(vwmaRef.getValue(i), vwma.getValue(i));
        }
    }
}
