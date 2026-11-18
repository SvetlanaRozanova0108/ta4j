/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Day Volume indicator.
 *
 * <p>
 * On an intraday series, returns the accumulated volume of the current trading day up to and including the current bar.
 */
public class DayVolumeIndicator extends AbstractDayIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public DayVolumeIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public DayVolumeIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public DayVolumeIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected Num calculate(int index) {
        int start = dayStartIndex(index);
        Num volume = getBarSeries().numFactory().zero();
        for (int i = start; i <= index; i++) {
            volume = volume.plus(getBarSeries().getBar(i).getVolume());
        }
        return volume;
    }
}
