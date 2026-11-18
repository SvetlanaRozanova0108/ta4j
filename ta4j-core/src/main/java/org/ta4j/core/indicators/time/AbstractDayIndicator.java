/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Abstract base class for intraday trading-day indicators.
 *
 * <p>
 * All subclasses accumulate values of the intraday bars belonging to the same
 * trading day (in the configured zone) as the current bar, i.e. they return
 * the open, high, low, close, volume or range of the day that contains the
 * current bar. On daily or coarser series they simply return the values of the
 * bar itself.
 */
public abstract class AbstractDayIndicator extends CachedIndicator<Num> {

    private final String zoneId;

    // serial access detection: cache of the current day's start index
    private transient int cachedBarIndex = -1;
    private transient int cachedDayStartIndex = -1;

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    protected AbstractDayIndicator(BarSeries barSeries) {
        this(barSeries, ZoneId.systemDefault());
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone that defines the trading day
     */
    protected AbstractDayIndicator(BarSeries barSeries, ZoneId zoneId) {
        this(barSeries, zoneId.getId());
    }

    /**
     * Constructor for serialization support.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id that defines the trading day
     */
    protected AbstractDayIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries);
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    /**
     * Returns the index of the first bar of the trading day containing the bar at
     * the given index.
     *
     * @param index the index
     * @return the first bar index of the same trading day
     */
    protected int dayStartIndex(int index) {
        if (cachedBarIndex == index) {
            return cachedDayStartIndex;
        }
        BarSeries series = getBarSeries();
        LocalDate day = dayOf(index);
        int start = index;
        while (start > series.getBeginIndex() && dayOf(start - 1).equals(day)) {
            start--;
        }
        cachedBarIndex = index;
        cachedDayStartIndex = start;
        return start;
    }

    /**
     * Returns the trading date of the bar at the given index.
     *
     * @param index the index
     * @return the local date of the bar's begin time in the configured zone
     */
    protected LocalDate dayOf(int index) {
        return getBarSeries().getBar(index).getBeginTime().atZone(ZoneId.of(zoneId)).toLocalDate();
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
