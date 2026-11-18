/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.expression;

/**
 * Thrown when an expression string or its parameters are invalid (unknown
 * function, unknown identifier, wrong arity, wrong argument type, unbalanced
 * parentheses, ...).
 *
 * <p>
 * The message contains the character position of the offending token.
 */
public class ExpressionValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int position;

    /**
     * Constructor.
     *
     * @param message   the error message
     * @param position  the character position in the expression (0-based), or -1
     *                  when unknown
     */
    public ExpressionValidationException(String message, int position) {
        super(position >= 0 ? message + " (at position " + position + ")" : message);
        this.position = position;
    }

    /**
     * Constructor without position information.
     *
     * @param message the error message
     */
    public ExpressionValidationException(String message) {
        this(message, -1);
    }

    /** @return the character position in the expression, or -1 when unknown */
    public int getPosition() {
        return position;
    }
}
