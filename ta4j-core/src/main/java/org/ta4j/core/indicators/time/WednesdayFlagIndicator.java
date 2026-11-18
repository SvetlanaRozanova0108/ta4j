/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Wednesday flag indicator.
 *
 * <p>
 * Returns 1 when the trading day of the bar is a Wednesday, otherwise 0.
 */
public class WednesdayFlagIndicator extends AbstractDayOfWeekFlagIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public WednesdayFlagIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public WednesdayFlagIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public WednesdayFlagIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected int isoDayOfWeek() {
        return 3;
    }
}
