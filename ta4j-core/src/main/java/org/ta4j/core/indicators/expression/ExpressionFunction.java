/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.expression;

import java.util.List;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

/**
 * A function usable inside an {@link ExpressionIndicator} expression.
 *
 * <p>
 * A function receives its already compiled arguments (numbers and
 * sub-indicators) and returns the resulting indicator. Functions are looked up
 * by (case-insensitive) name; see
 * {@link ExpressionIndicator#registerFunction(String, int, int, ExpressionFunction)}
 * to add custom ones.
 */
@FunctionalInterface
public interface ExpressionFunction {

    /**
     * Builds the indicator for one call of this function.
     *
     * @param args   the compiled arguments (guaranteed to match the declared
     *               arity; argument types must be checked by the function itself
     *               via {@link ExpressionArgument}, throwing
     *               {@link ExpressionValidationException} on mismatch)
     * @param series the bar series the expression is evaluated on
     * @return the resulting indicator
     */
    Indicator<Num> apply(List<ExpressionArgument> args, BarSeries series);
}
