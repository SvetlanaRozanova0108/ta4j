/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Cumulative Product indicator.
 *
 * <p>
 * Returns the running product (multiplicative accumulation) of all values of
 * the given indicator from the beginning of the bar series up to the current
 * index. It is the multiplicative counterpart of
 * {@link RunningTotalIndicator}.
 *
 * <p>
 * Can be used for compounding growth rates: applied to a growth factor series
 * {@code (1 + r)} it yields the cumulative growth of the series.
 */
public class CumulativeProductIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> indicator;

    // serial access detection
    private transient Num previousProduct;
    private transient int previousIndex = -1;

    /**
     * Constructor.
     *
     * @param indicator the indicator to accumulate multiplicatively
     */
    public CumulativeProductIndicator(Indicator<Num> indicator) {
        super(indicator);
        this.indicator = indicator;
    }

    @Override
    protected Num calculate(int index) {
        // serial access can benefit from the previous partial product,
        // which saves a lot of CPU work for long series
        if (previousIndex != -1 && previousIndex == index - 1) {
            Num newProduct = previousProduct.multipliedBy(indicator.getValue(index));
            updatePreviousProduct(index, newProduct);
            return newProduct;
        }

        Num product = getBarSeries().numFactory().one();
        for (int i = getBarSeries().getBeginIndex(); i <= index; i++) {
            product = product.multipliedBy(indicator.getValue(i));
        }
        updatePreviousProduct(index, product);
        return product;
    }

    private void updatePreviousProduct(final int index, final Num product) {
        previousIndex = index;
        previousProduct = product;
    }

    /** @return {@code 0} */
    @Override
    public int getCountOfUnstableBars() {
        return indicator.getCountOfUnstableBars();
    }
}
