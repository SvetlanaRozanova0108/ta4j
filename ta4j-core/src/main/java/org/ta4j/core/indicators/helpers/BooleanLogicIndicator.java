/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.helpers;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;

/**
 * Boolean logic indicator.
 *
 * <p>
 * Applies a binary boolean logic operation (AND, OR, XOR, NAND, NOR) between
 * two boolean indicators. Boolean conditions can be built with e.g.
 * {@link BooleanTransformIndicator} or {@link CrossIndicator}.
 */
public class BooleanLogicIndicator extends CachedIndicator<Boolean> {

    /**
     * Enumeration of supported binary boolean operations.
     */
    public enum Operation {
        AND, OR, XOR, NAND, NOR
    }

    private final Indicator<Boolean> left;
    private final Indicator<Boolean> right;
    private final Operation operation;

    /**
     * Constructor.
     *
     * @param left      the left operand indicator
     * @param right     the right operand indicator
     * @param operation the logic operation
     */
    public BooleanLogicIndicator(Indicator<Boolean> left, Indicator<Boolean> right, Operation operation) {
        super(left.getBarSeries());
        if (left == null || right == null || operation == null) {
            throw new IllegalArgumentException("Operands and operation must not be null");
        }
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    @Override
    protected Boolean calculate(int index) {
        boolean a = left.getValue(index);
        boolean b = right.getValue(index);
        return switch (operation) {
        case AND -> a && b;
        case OR -> a || b;
        case XOR -> a ^ b;
        case NAND -> !(a && b);
        case NOR -> !(a || b);
        };
    }

    /** @return {@code 0} */
    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " operation: " + operation;
    }
}
