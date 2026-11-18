/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

/**
 * Days until third Friday of month indicator.
 *
 * <p>
 * Returns the number of days from the bar's date until the third Friday of the
 * month (0 if the bar's date is that Friday). If the third Friday of the
 * current month has already passed, the third Friday of the next month is
 * used, searched up to 12 months ahead.
 */
public class DaysUntilThirdFridayIndicator extends AbstractCalendarIndicator {

    /**
     * Constructor using the system default time zone.
     *
     * @param barSeries the bar series
     */
    public DaysUntilThirdFridayIndicator(BarSeries barSeries) {
        super(barSeries);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone used to interpret the bar time
     */
    public DaysUntilThirdFridayIndicator(BarSeries barSeries, java.time.ZoneId zoneId) {
        super(barSeries, zoneId);
    }

    /**
     * Constructor.
     *
     * @param barSeries the bar series
     * @param zoneId    the time zone id used to interpret the bar time
     */
    public DaysUntilThirdFridayIndicator(BarSeries barSeries, String zoneId) {
        super(barSeries, zoneId);
    }

    @Override
    protected Num calculate(int index) {
        LocalDate date = zonedDateTime(index).toLocalDate();
        YearMonth month = YearMonth.from(date);
        LocalDate thirdFriday = thirdFriday(month);
        for (int i = 0; thirdFriday.isBefore(date) && i < 12; i++) {
            month = month.plusMonths(1);
            thirdFriday = thirdFriday(month);
        }
        return getBarSeries().numFactory().numOf(date.until(thirdFriday, ChronoUnit.DAYS));
    }

    private static LocalDate thirdFriday(YearMonth month) {
        return month.atDay(1).with(TemporalAdjusters.firstInMonth(DayOfWeek.FRIDAY)).plusWeeks(2);
    }
}
