/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Abstract base class for calendar indicators.
 *
 * <p>
 * Converts the {@link Bar#getBeginTime()} of a bar into a
 * {@link ZonedDateTime} in the configured zone (defaults to the system zone)
 * so that calendar fields can be derived from it.
 */
public abstract class AbstractCalendarIndicator extends CachedIndicator<Num> {

    private final String zoneId;

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    protected AbstractCalendarIndicator(BarSeries barSeries) {
        this(barSeries, ZoneId.systemDefault());
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    protected AbstractCalendarIndicator(BarSeries barSeries, ZoneId zoneId) {
        this(barSeries, zoneId.getId());
    }

    /**
     * Constructor for serialization support.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    protected AbstractCalendarIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries);
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    /**
     * Returns the date-time of the bar at the given index in the configured zone.
     *
     * @param index the index
     * @return the zoned date-time of the bar's begin time
     */
    protected ZonedDateTime zonedDateTime(int index) {
        return getBarSeries().getBar(index).getBeginTime().atZone(ZoneId.of(zoneId));
    }

    /** @return the time zone id */
    public String getZoneId() {
        return zoneId;
    }

    /** @return {@code 0} */
    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
