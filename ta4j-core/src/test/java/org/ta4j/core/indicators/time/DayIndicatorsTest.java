/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import static org.junit.Assert.assertEquals;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.time.Duration;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class DayIndicatorsTest extends AbstractIndicatorTest<Indicator<Num>, Num> {

    private BarSeries series;

    public DayIndicatorsTest(NumFactory numFactory) {
        super(numFactory);
    }

    @Before
    public void setUp() {
        series = new BaseBarSeriesBuilder().withNumFactory(numFactory).withName("intraday").build();
        // Monday 2024-03-04 (UTC)
        addBar("2024-03-04T09:00:00Z", 100, 105, 98, 102, 10);
        addBar("2024-03-04T10:00:00Z", 105, 110, 103, 107, 20);
        addBar("2024-03-04T11:00:00Z", 103, 104, 100, 101, 30);
        // Tuesday 2024-03-05 (UTC)
        addBar("2024-03-05T09:00:00Z", 106, 112, 106, 111, 40);
        addBar("2024-03-05T10:00:00Z", 112, 120, 110, 118, 50);
    }

    private void addBar(String beginTime, double open, double high, double low, double close, double volume) {
        Instant begin = Instant.parse(beginTime);
        series.barBuilder().beginTime(begin).endTime(begin.plus(Duration.ofHours(1)))
                .openPrice(open).highPrice(high).lowPrice(low).closePrice(close)
                .volume(volume).amount(open * volume).trades(1).add();
    }

    @Test
    public void dayOpen() {
        Indicator<Num> open = new DayOpenIndicator(series, "UTC");
        assertNumEquals(100, open.getValue(0));
        assertNumEquals(100, open.getValue(1));
        assertNumEquals(100, open.getValue(2));
        assertNumEquals(106, open.getValue(3));
        assertNumEquals(106, open.getValue(4));
    }

    @Test
    public void dayHigh() {
        Indicator<Num> high = new DayHighIndicator(series, "UTC");
        assertNumEquals(105, high.getValue(0));
        assertNumEquals(110, high.getValue(1));
        assertNumEquals(110, high.getValue(2));
        assertNumEquals(112, high.getValue(3));
        assertNumEquals(120, high.getValue(4));
    }

    @Test
    public void dayLow() {
        Indicator<Num> low = new DayLowIndicator(series, "UTC");
        assertNumEquals(98, low.getValue(0));
        assertNumEquals(98, low.getValue(1));
        assertNumEquals(98, low.getValue(2));
        assertNumEquals(106, low.getValue(3));
        assertNumEquals(106, low.getValue(4));
    }

    @Test
    public void dayClose() {
        Indicator<Num> close = new DayCloseIndicator(series, "UTC");
        assertNumEquals(102, close.getValue(0));
        assertNumEquals(101, close.getValue(2));
        assertNumEquals(118, close.getValue(4));
    }

    @Test
    public void dayVolume() {
        Indicator<Num> volume = new DayVolumeIndicator(series, "UTC");
        assertNumEquals(10, volume.getValue(0));
        assertNumEquals(30, volume.getValue(1));
        assertNumEquals(60, volume.getValue(2));
        assertNumEquals(40, volume.getValue(3));
        assertNumEquals(90, volume.getValue(4));
    }

    @Test
    public void dayRange() {
        Indicator<Num> range = new DayRangeIndicator(series, "UTC");
        assertNumEquals(7, range.getValue(0));
        assertNumEquals(12, range.getValue(1));
        assertNumEquals(12, range.getValue(2));
        assertNumEquals(6, range.getValue(3));
        assertNumEquals(14, range.getValue(4));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void serializationRoundTrip() {
        Indicator<Num> original = new DayHighIndicator(series, "UTC");

        String json = original.toJson();
        Indicator<Num> restored = (Indicator<Num>) Indicator.fromJson(series, json);

        assertEquals(original.toDescriptor(), restored.toDescriptor());
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertNumEquals(original.getValue(i), restored.getValue(i));
        }
    }
}
