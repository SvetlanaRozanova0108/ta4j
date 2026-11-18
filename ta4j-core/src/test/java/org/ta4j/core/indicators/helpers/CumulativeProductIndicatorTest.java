/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers;

import static org.junit.Assert.assertEquals;
import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.mocks.MockBarSeriesBuilder;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class CumulativeProductIndicatorTest extends AbstractIndicatorTest<Indicator<Num>, Num> {

    private BarSeries data;

    public CumulativeProductIndicatorTest(NumFactory numFactory) {
        super(numFactory);
    }

    @Before
    public void setUp() {
        data = new MockBarSeriesBuilder().withNumFactory(numFactory).withData(1, 2, 3, 4, 5).build();
    }

    @Test
    public void calculate() {
        Indicator<Num> product = new CumulativeProductIndicator(new ClosePriceIndicator(data));
        double[] expected = { 1.0, 2.0, 6.0, 24.0, 120.0 };
        for (int i = 0; i < expected.length; i++) {
            assertNumEquals(expected[i], product.getValue(i));
        }
    }

    @Test
    public void nonSerialAccessYieldsSameResult() {
        Indicator<Num> product = new CumulativeProductIndicator(new ClosePriceIndicator(data));
        assertNumEquals(120.0, product.getValue(4));
        assertNumEquals(2.0, product.getValue(1));
        assertNumEquals(120.0, product.getValue(4));
        assertNumEquals(6.0, product.getValue(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void serializationRoundTrip() {
        Indicator<Num> base = new ClosePriceIndicator(data);
        CumulativeProductIndicator indicator = new CumulativeProductIndicator(base);

        String json = indicator.toJson();
        Indicator<Num> restored = (Indicator<Num>) Indicator.fromJson(data, json);

        assertEquals(indicator.toDescriptor(), restored.toDescriptor());
        for (int i = data.getBeginIndex(); i <= data.getEndIndex(); i++) {
            assertNumEquals(indicator.getValue(i), restored.getValue(i));
        }
    }
}
