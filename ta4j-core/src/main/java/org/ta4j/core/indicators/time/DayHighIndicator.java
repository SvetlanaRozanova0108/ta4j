/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Day High indicator.
 *
 * <p>
 * On an intraday series, returns the highest high price of the current trading day, accumulated from the bars of that day up to the current bar.
 */
public class DayHighIndicator extends AbstractDayIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public DayHighIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public DayHighIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public DayHighIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected Num calculate(int index) {
        int start = dayStartIndex(index);
        Num highest = getBarSeries().getBar(start).getHighPrice();
        for (int i = start + 1; i <= index; i++) {
            Num high = getBarSeries().getBar(i).getHighPrice();
            if (highest.isLessThan(high)) {
                highest = high;
            }
        }
        return highest;
    }
}
