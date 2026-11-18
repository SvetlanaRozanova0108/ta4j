/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Day Range indicator.
 *
 * <p>
 * On an intraday series, returns the difference between the day high and the day low of the current trading day (accumulated up to the current bar).
 */
public class DayRangeIndicator extends AbstractDayIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public DayRangeIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public DayRangeIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public DayRangeIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected Num calculate(int index) {
        int start = dayStartIndex(index);
        Num highest = getBarSeries().getBar(start).getHighPrice();
        Num lowest = getBarSeries().getBar(start).getLowPrice();
        for (int i = start + 1; i <= index; i++) {
            Num high = getBarSeries().getBar(i).getHighPrice();
            Num low = getBarSeries().getBar(i).getLowPrice();
            if (highest.isLessThan(high)) {
                highest = high;
            }
            if (lowest.isGreaterThan(low)) {
                lowest = low;
            }
        }
        return highest.minus(lowest);
    }
}
