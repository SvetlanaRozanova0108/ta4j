/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.expression;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;

/**
 * A single (already compiled) argument of an {@link ExpressionFunction}.
 *
 * <p>
 * An argument is either a numeric constant (literal from the expression or a
 * value from the parameters map) or a sub-indicator (data field, nested
 * function call, or a series-valued parameter).
 */
public final class ExpressionArgument {

    private final Indicator<Num> indicator;
    private final Num number;

    private ExpressionArgument(Indicator<Num> indicator, Num number) {
        this.indicator = indicator;
        this.number = number;
    }

    /**
     * Creates an indicator argument.
     *
     * @param indicator the sub-indicator
     * @return the argument
     */
    public static ExpressionArgument ofIndicator(Indicator<Num> indicator) {
        if (indicator == null) {
            throw new IllegalArgumentException("indicator must not be null");
        }
        return new ExpressionArgument(indicator, null);
    }

    /**
     * Creates a numeric constant argument. The constant is also wrapped into a
     * constant indicator so it can be passed to any ta4j indicator expecting a
     * {@code Num} series.
     *
     * @param number the constant value
     * @param series the bar series (for the constant indicator wrapper)
     * @return the argument
     */
    public static ExpressionArgument ofConstant(Num number, BarSeries series) {
        if (number == null) {
            throw new IllegalArgumentException("number must not be null");
        }
        return new ExpressionArgument(new ConstantIndicator<>(series, number), number);
    }

    /** @return true when this argument is a numeric constant */
    public boolean isNumber() {
        return number != null;
    }

    /** @return true when this argument is a sub-indicator */
    public boolean isIndicator() {
        return indicator != null;
    }

    /**
     * Returns the numeric constant.
     *
     * @return the value
     * @throws ExpressionValidationException if this argument is not a number
     */
    public Num number() {
        if (number == null) {
            throw new ExpressionValidationException("expected a numeric constant but got a sub-indicator", -1);
        }
        return number;
    }

    /**
     * Returns the sub-indicator. A numeric constant argument is backed by a
     * constant indicator, so this never fails.
     *
     * @return the indicator
     */
    public Indicator<Num> indicator() {
        return indicator;
    }

    /**
     * Returns the numeric constant as a positive integer (typical for indicator
     * bar counts/periods).
     *
     * @return the integer value
     * @throws ExpressionValidationException if this argument is not a number, is
     *                                       not integer, or is smaller than 1
     */
    public int positiveInt() {
        Num value = number();
        double d = value.doubleValue();
        if (Double.isNaN(d) || d != Math.rint(d)) {
            throw new ExpressionValidationException("expected an integer value but got " + value, -1);
        }
        int intValue = (int) d;
        if (intValue < 1) {
            throw new ExpressionValidationException("expected a positive integer value but got " + value, -1);
        }
        return intValue;
    }

    @Override
    public String toString() {
        return isNumber() ? number.toString() : indicator.toString();
    }
}
