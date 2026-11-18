/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Abstract base class for the month flag indicators ({@code JanuaryFlag} ...
 * {@code DecemberFlag}).
 *
 * <p>
 * Returns 1 when the trading month of the bar equals the configured month of
 * year, otherwise 0.
 */
public abstract class AbstractMonthFlagIndicator extends AbstractCalendarIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    protected AbstractMonthFlagIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    protected AbstractMonthFlagIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    protected AbstractMonthFlagIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    /** @return the month of year (1 = January, ..., 12 = December) */
    protected abstract int monthValue();

    @Override
    protected Num calculate(int index) {
        boolean match = zonedDateTime(index).getMonthValue() == monthValue();
        return getBarSeries().numFactory().numOf(match ? 1 : 0);
    }
}
