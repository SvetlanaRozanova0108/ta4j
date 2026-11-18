/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.statistics;

import static org.junit.Assert.assertTrue;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.indicators.helpers.FixedIndicator;
import org.ta4j.core.mocks.MockBarSeriesBuilder;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class XYRegressionIndicatorTest extends AbstractIndicatorTest<Indicator<Num>, Num> {

    private BarSeries series;
    private Indicator<Num> x;
    private Indicator<Num> y;

    public XYRegressionIndicatorTest(NumFactory numFactory) {
        super(numFactory);
    }

    @Before
    public void setUp() {
        series = new MockBarSeriesBuilder().withNumFactory(numFactory).withData(1, 2, 3, 4, 5).build();
        x = new FixedIndicator(series, nums(1, 2, 3, 4, 5));
        // y = 3x + 1
        y = new FixedIndicator(series, nums(4, 7, 10, 13, 16));
    }

    private Num[] nums(double... values) {
        Num[] nums = new Num[values.length];
        for (int i = 0; i < values.length; i++) {
            nums[i] = numFactory.numOf(values[i]);
        }
        return nums;
    }

    @Test
    public void perfectPositiveLine() {
        assertNumEquals(3, new XYRegressionIndicator(x, y, 5, XYRegressionType.SLOPE).getValue(4));
        assertNumEquals(1, new XYRegressionIndicator(x, y, 5, XYRegressionType.Y_INTERCEPT).getValue(4));
        assertNumEquals(1, new XYRegressionIndicator(x, y, 5, XYRegressionType.R).getValue(4));
        assertNumEquals(1, new XYRegressionIndicator(x, y, 5, XYRegressionType.R_SQUARED).getValue(4));
        assertNumEquals(-1.0 / 3.0, new XYRegressionIndicator(x, y, 5, XYRegressionType.X_INTERCEPT).getValue(4));
        assertNumEquals(0, new XYRegressionIndicator(x, y, 5, XYRegressionType.STD_ERROR).getValue(4));
    }

    @Test
    public void rollingWindow() {
        // window over last 3 points still perfectly on y = 3x + 1
        assertNumEquals(3, new XYRegressionIndicator(x, y, 3, XYRegressionType.SLOPE).getValue(4));
        assertNumEquals(1, new XYRegressionIndicator(x, y, 3, XYRegressionType.Y_INTERCEPT).getValue(4));
    }

    @Test
    public void perfectNegativeLine() {
        Indicator<Num> negativeY = new FixedIndicator(series, nums(10, 8, 6, 4, 2));
        assertNumEquals(-2, new XYRegressionIndicator(x, negativeY, 5, XYRegressionType.SLOPE).getValue(4));
        assertNumEquals(-1, new XYRegressionIndicator(x, negativeY, 5, XYRegressionType.R).getValue(4));
    }

    @Test
    public void standardErrorOfNoisyLine() {
        Indicator<Num> noisyY = new FixedIndicator(series, nums(4.5, 6.5, 10.5, 12.5, 16.5));
        // least-squares fit: y = 3x + 1.1 -> residuals 0.4, -0.6, 0.4, -0.6, 0.4
        // SSE = 1.2 -> stdErr = sqrt(1.2 / 3)
        assertNumEquals(Math.sqrt(1.2 / 3),
                new XYRegressionIndicator(x, noisyY, 5, XYRegressionType.STD_ERROR).getValue(4));
    }

    @Test
    public void notEnoughObservationsReturnsNaN() {
        Indicator<Num> slope = new XYRegressionIndicator(x, y, 5, XYRegressionType.SLOPE);
        assertTrue(slope.getValue(0).isNaN());
        Indicator<Num> stdErr = new XYRegressionIndicator(x, y, 5, XYRegressionType.STD_ERROR);
        assertTrue(stdErr.getValue(1).isNaN());
    }
}
