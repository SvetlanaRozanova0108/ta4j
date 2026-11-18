/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;

/**
 * Boolean NOT indicator.
 *
 * <p>
 * Negates the boolean value of the given indicator.
 */
public class BooleanNotIndicator extends CachedIndicator<Boolean> {

    private final Indicator<Boolean> indicator;

    /**
     * Constructor.
     *
     * @param indicator the boolean indicator to negate
     */
    public BooleanNotIndicator(Indicator<Boolean> indicator) {
        super(indicator);
        this.indicator = indicator;
    }

    @Override
    protected Boolean calculate(int index) {
        return !indicator.getValue(index);
    }

    /** @return {@code 0} */
    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
