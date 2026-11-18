/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * June flag indicator.
 *
 * <p>
 * Returns 1 when the trading month of the bar is June, otherwise 0.
 */
public class JuneFlagIndicator extends AbstractMonthFlagIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public JuneFlagIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public JuneFlagIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public JuneFlagIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected int monthValue() {
        return 6;
    }
}
