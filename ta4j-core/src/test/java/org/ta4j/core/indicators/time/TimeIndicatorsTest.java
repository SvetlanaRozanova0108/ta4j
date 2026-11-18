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

public class TimeIndicatorsTest extends AbstractIndicatorTest<Indicator<Num>, Num> {

    private BarSeries series;

    public TimeIndicatorsTest(NumFactory numFactory) {
        super(numFactory);
    }

    @Before
    public void setUp() {
        series = new BaseBarSeriesBuilder().withNumFactory(numFactory).withName("timeSeries").build();
        // Friday, 2008-12-19 00:00 UTC (third Friday of December 2008)
        addBar("2008-12-19T00:00:00Z");
        // Thursday, 2008-12-25 10:30 UTC
        addBar("2008-12-25T10:30:00Z");
        // Friday, 2009-01-16 00:00 UTC (third Friday of January 2009)
        addBar("2009-01-16T00:00:00Z");
    }

    private void addBar(String beginTime) {
        Instant begin = Instant.parse(beginTime);
        series.barBuilder().beginTime(begin).endTime(begin.plus(Duration.ofHours(1)))
                .openPrice(1).highPrice(1).lowPrice(1).closePrice(1).volume(1).amount(1).trades(1).add();
    }

    @Test
    public void dayOfWeek() {
        Indicator<Num> dow = new DayOfWeekIndicator(series, "UTC");
        assertNumEquals(5, dow.getValue(0)); // Friday
        assertNumEquals(4, dow.getValue(1)); // Thursday
        assertNumEquals(5, dow.getValue(2)); // Friday
    }

    @Test
    public void dayOfMonth() {
        Indicator<Num> dom = new DayOfMonthIndicator(series, "UTC");
        assertNumEquals(19, dom.getValue(0));
        assertNumEquals(25, dom.getValue(1));
        assertNumEquals(16, dom.getValue(2));
    }

    @Test
    public void monthOfYear() {
        Indicator<Num> moy = new MonthOfYearIndicator(series, "UTC");
        assertNumEquals(12, moy.getValue(0));
        assertNumEquals(12, moy.getValue(1));
        assertNumEquals(1, moy.getValue(2));
    }

    @Test
    public void hourOfDay() {
        Indicator<Num> hod = new HourOfDayIndicator(series, "UTC");
        assertNumEquals(0, hod.getValue(0));
        assertNumEquals(10, hod.getValue(1));
    }

    @Test
    public void minuteOfHour() {
        Indicator<Num> moh = new MinuteOfHourIndicator(series, "UTC");
        assertNumEquals(0, moh.getValue(0));
        assertNumEquals(30, moh.getValue(1));
    }

    @Test
    public void timeOfDay() {
        Indicator<Num> tod = new TimeOfDayIndicator(series, "UTC");
        assertNumEquals(0, tod.getValue(0));
        assertNumEquals(10 * 3600 + 30 * 60, tod.getValue(1));
    }

    @Test
    public void daysUntilThirdFriday() {
        Indicator<Num> days = new DaysUntilThirdFridayIndicator(series, "UTC");
        // the third Friday itself -> 0
        assertNumEquals(0, days.getValue(0));
        // Dec 19, 2008 (third Friday) already passed on Dec 25 -> Jan 16, 2009: 22 days
        assertNumEquals(22, days.getValue(1));
        assertNumEquals(0, days.getValue(2));
    }

    @Test
    public void dayOfWeekFlags() {
        assertNumEquals(1, new FridayFlagIndicator(series, "UTC").getValue(0));
        assertNumEquals(0, new FridayFlagIndicator(series, "UTC").getValue(1));
        assertNumEquals(1, new ThursdayFlagIndicator(series, "UTC").getValue(1));
        assertNumEquals(0, new MondayFlagIndicator(series, "UTC").getValue(0));
        assertNumEquals(0, new SundayFlagIndicator(series, "UTC").getValue(0));
    }

    @Test
    public void monthFlags() {
        assertNumEquals(1, new DecemberFlagIndicator(series, "UTC").getValue(0));
        assertNumEquals(1, new DecemberFlagIndicator(series, "UTC").getValue(1));
        assertNumEquals(0, new DecemberFlagIndicator(series, "UTC").getValue(2));
        assertNumEquals(1, new JanuaryFlagIndicator(series, "UTC").getValue(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void serializationRoundTrip() {
        Indicator<Num> original = new DayOfWeekIndicator(series, "UTC");

        String json = original.toJson();
        Indicator<Num> restored = (Indicator<Num>) Indicator.fromJson(series, json);

        assertEquals(original.toDescriptor(), restored.toDescriptor());
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertNumEquals(original.getValue(i), restored.getValue(i));
        }
    }

    @Test
    public void serializationKeepsZone() {
        Indicator<Num> original = new DayOfWeekIndicator(series, "Europe/Moscow");
        Indicator<Num> restored = (Indicator<Num>) Indicator.fromJson(series, original.toJson());
        assertEquals("Europe/Moscow", ((DayOfWeekIndicator) restored).getZoneId());
    }
}
