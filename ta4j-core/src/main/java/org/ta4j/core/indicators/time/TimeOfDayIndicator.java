/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Time of day indicator.
 *
 * <p>
 * Returns the number of seconds since midnight of the bar's begin time, in the configured zone. Together with comparison operations this implements tradetime filters like {@code Time >= X} or {@code X <= Time <= Y}.
 */
public class TimeOfDayIndicator extends AbstractCalendarIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public TimeOfDayIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public TimeOfDayIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public TimeOfDayIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected Num calculate(int index) {
        return getBarSeries().numFactory().numOf(zonedDateTime(index).toLocalTime().toSecondOfDay());
    }
}
