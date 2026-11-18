/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Day Low indicator.
 *
 * <p>
 * On an intraday series, returns the lowest low price of the current trading day, accumulated from the bars of that day up to the current bar.
 */
public class DayLowIndicator extends AbstractDayIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public DayLowIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public DayLowIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public DayLowIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected Num calculate(int index) {
        int start = dayStartIndex(index);
        Num lowest = getBarSeries().getBar(start).getLowPrice();
        for (int i = start + 1; i <= index; i++) {
            Num low = getBarSeries().getBar(i).getLowPrice();
            if (lowest.isGreaterThan(low)) {
                lowest = low;
            }
        }
        return lowest;
    }
}
