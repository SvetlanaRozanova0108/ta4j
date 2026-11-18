/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Abstract base class for the day-of-week flag indicators ({@code SundayFlag}
 * ... {@code SaturdayFlag}).
 *
 * <p>
 * Returns 1 when the trading day of the bar equals the configured ISO
 * day-of-week, otherwise 0.
 */
public abstract class AbstractDayOfWeekFlagIndicator extends AbstractCalendarIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    protected AbstractDayOfWeekFlagIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    protected AbstractDayOfWeekFlagIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    protected AbstractDayOfWeekFlagIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    /** @return the ISO day-of-week number (1 = Monday, ..., 7 = Sunday) */
    protected abstract int isoDayOfWeek();

    @Override
    protected Num calculate(int index) {
        boolean match = zonedDateTime(index).getDayOfWeek().getValue() == isoDayOfWeek();
        return getBarSeries().numFactory().numOf(match ? 1 : 0);
    }
}
