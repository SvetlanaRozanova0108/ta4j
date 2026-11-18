/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import java.util.function.UnaryOperator;

/**
 * Objects of this class defer the evaluation of a unary operator, like sqrt().
 * <p>
 * There may be other unary operations on Num that could be added here.
 */
public class UnaryOperationIndicator implements Indicator<Num> {

    /**
     * Enumeration of supported unary operations.
     *
     * <p>
     * Trigonometric operations without suffix expect and return radians. The
     * {@code *D} operations work with degrees: for {@code SIN, COS, TAN} the
     * argument is interpreted as degrees, for {@code ASIN, ACOS, ATAN} the result
     * is expressed in degrees; for the hyperbolic {@code *D} variants the argument
     * is converted from degrees to radians before the function is applied.
     */
    public enum Operation {
        ABS, SQRT, LOG, LOG10, EXP, INVERSE, POW, SUBSTITUTE,
        SIN, COS, TAN, ASIN, ACOS, ATAN, SINH, COSH, TANH,
        SIND, COSD, TAND, ASIND, ACOSD, ATAND, SINHD, COSHD, TANHD
    }

    @SuppressWarnings("unused") // Used by serialization via reflection
    private final Operation operation;
    private final UnaryOperator<Num> operator;
    private final Indicator<Num> operand;
    @SuppressWarnings("unused") // Used by serialization via reflection
    private final Num exponent; // For POW operation
    @SuppressWarnings("unused") // Used by serialization via reflection
    private final Num valueToReplace; // For SUBSTITUTE operation
    @SuppressWarnings("unused") // Used by serialization via reflection
    private final Num replacementValue; // For SUBSTITUTE operation

    /**
     * Constructor for serialization support.
     *
     * @param operation the operation type
     * @param operand   the operand indicator
     */
    public UnaryOperationIndicator(final Operation operation, final Indicator<Num> operand) {
        this(operation, operand, null, null, null);
    }

    /**
     * Constructor for POW operation with exponent.
     *
     * @param operation the operation type (must be POW)
     * @param operand   the operand indicator
     * @param exponent  the exponent for POW operation
     */
    public UnaryOperationIndicator(final Operation operation, final Indicator<Num> operand, final Number exponent) {
        if (operation != Operation.POW) {
            throw new IllegalArgumentException("Exponent parameter is only valid for POW operation");
        }
        this.operation = operation;
        this.operand = operand;
        this.exponent = operand.getBarSeries().numFactory().numOf(exponent);
        this.valueToReplace = null;
        this.replacementValue = null;
        this.operator = getOperator(operation, operand.getBarSeries().numFactory(), this.exponent, null, null);
    }

    /**
     * Constructor for SUBSTITUTE operation with replacement values.
     *
     * @param operation        the operation type (must be SUBSTITUTE)
     * @param operand          the operand indicator
     * @param valueToReplace   the value to replace
     * @param replacementValue the replacement value
     */
    public UnaryOperationIndicator(final Operation operation, final Indicator<Num> operand, final Num valueToReplace,
            final Num replacementValue) {
        if (operation != Operation.SUBSTITUTE) {
            throw new IllegalArgumentException("Replacement values are only valid for SUBSTITUTE operation");
        }
        this.operation = operation;
        this.operand = operand;
        this.exponent = null;
        this.valueToReplace = valueToReplace;
        this.replacementValue = replacementValue;
        this.operator = getOperator(operation, operand.getBarSeries().numFactory(), null, valueToReplace,
                replacementValue);
    }

    private UnaryOperationIndicator(final Operation operation, final Indicator<Num> operand, final Num exponent,
            final Num valueToReplace, final Num replacementValue) {
        if (operation == null || operand == null) {
            throw new IllegalArgumentException("Operation and operand must not be null");
        }
        this.operation = operation;
        this.operand = operand;
        this.exponent = exponent;
        this.valueToReplace = valueToReplace;
        this.replacementValue = replacementValue;
        this.operator = getOperator(operation, operand.getBarSeries().numFactory(), exponent, valueToReplace,
                replacementValue);
    }

    private static UnaryOperator<Num> getOperator(Operation operation, NumFactory factory, Num exponent,
            Num valueToReplace, Num replacementValue) {
        return switch (operation) {
        case ABS -> Num::abs;
        case SQRT -> Num::sqrt;
        case LOG -> Num::log;
        case LOG10 -> {
            Num ln10 = factory.numOf(10).log();
            yield val -> val.log().dividedBy(ln10);
        }
        case EXP -> Num::exp;
        case INVERSE -> {
            Num one = factory.one();
            yield val -> one.dividedBy(val);
        }
        case SIN -> val -> factory.numOf(Math.sin(val.doubleValue()));
        case COS -> val -> factory.numOf(Math.cos(val.doubleValue()));
        case TAN -> val -> factory.numOf(Math.tan(val.doubleValue()));
        case ASIN -> val -> factory.numOf(Math.asin(val.doubleValue()));
        case ACOS -> val -> factory.numOf(Math.acos(val.doubleValue()));
        case ATAN -> val -> factory.numOf(Math.atan(val.doubleValue()));
        case SINH -> val -> factory.numOf(Math.sinh(val.doubleValue()));
        case COSH -> val -> factory.numOf(Math.cosh(val.doubleValue()));
        case TANH -> val -> factory.numOf(Math.tanh(val.doubleValue()));
        case SIND -> val -> factory.numOf(Math.sin(Math.toRadians(val.doubleValue())));
        case COSD -> val -> factory.numOf(Math.cos(Math.toRadians(val.doubleValue())));
        case TAND -> val -> factory.numOf(Math.tan(Math.toRadians(val.doubleValue())));
        case ASIND -> val -> factory.numOf(Math.toDegrees(Math.asin(val.doubleValue())));
        case ACOSD -> val -> factory.numOf(Math.toDegrees(Math.acos(val.doubleValue())));
        case ATAND -> val -> factory.numOf(Math.toDegrees(Math.atan(val.doubleValue())));
        case SINHD -> val -> factory.numOf(Math.sinh(Math.toRadians(val.doubleValue())));
        case COSHD -> val -> factory.numOf(Math.cosh(Math.toRadians(val.doubleValue())));
        case TANHD -> val -> factory.numOf(Math.tanh(Math.toRadians(val.doubleValue())));
        case POW -> {
            if (exponent == null) {
                throw new IllegalArgumentException("Exponent is required for POW operation");
            }
            yield val -> val.pow(exponent);
        }
        case SUBSTITUTE -> {
            if (valueToReplace == null || replacementValue == null) {
                throw new IllegalArgumentException(
                        "ValueToReplace and replacementValue are required for SUBSTITUTE operation");
            }
            yield operandValue -> operandValue.equals(valueToReplace) ? replacementValue : operandValue;
        }
        };
    }

    /**
     * Returns an {@code Indicator} whose value is {@code √(operand)}.
     *
     * @param operand the operand indicator
     * @return {@code √(operand)}
     * @see Num#sqrt
     */
    public static UnaryOperationIndicator sqrt(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.SQRT, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is the absolute value of
     * {@code operand}.
     *
     * @param operand the operand indicator
     * @return {@code abs(operand)}
     * @see Num#abs
     */
    public static UnaryOperationIndicator abs(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.ABS, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code operand^exponent}.
     *
     * @param operand  the operand indicator
     * @param exponent the power exponent
     * @return {@code operand^exponent}
     * @see Num#pow
     */
    public static UnaryOperationIndicator pow(Indicator<Num> operand, Number exponent) {
        return new UnaryOperationIndicator(Operation.POW, operand, exponent);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code log(operand)}.
     *
     * @param operand the operand indicator
     * @return {@code log(operand)}
     */
    public static UnaryOperationIndicator log(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.LOG, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code log10(operand)}.
     *
     * @param operand the operand indicator
     * @return {@code log10(operand)}
     */
    public static UnaryOperationIndicator log10(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.LOG10, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code e^operand}.
     *
     * @param operand the operand indicator
     * @return {@code exp(operand)}
     * @see Num#exp
     */
    public static UnaryOperationIndicator exp(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.EXP, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code 1 / operand}.
     *
     * @param operand the operand indicator
     * @return {@code 1 / operand}
     */
    public static UnaryOperationIndicator inverse(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.INVERSE, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code sin(operand)} (radians).
     *
     * @param operand the operand indicator, in radians
     * @return {@code sin(operand)}
     */
    public static UnaryOperationIndicator sin(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.SIN, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code cos(operand)} (radians).
     *
     * @param operand the operand indicator, in radians
     * @return {@code cos(operand)}
     */
    public static UnaryOperationIndicator cos(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.COS, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code tan(operand)} (radians).
     *
     * @param operand the operand indicator, in radians
     * @return {@code tan(operand)}
     */
    public static UnaryOperationIndicator tan(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.TAN, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code asin(operand)} (radians).
     *
     * @param operand the operand indicator
     * @return {@code asin(operand)}
     */
    public static UnaryOperationIndicator asin(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.ASIN, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code acos(operand)} (radians).
     *
     * @param operand the operand indicator
     * @return {@code acos(operand)}
     */
    public static UnaryOperationIndicator acos(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.ACOS, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code atan(operand)} (radians).
     *
     * @param operand the operand indicator
     * @return {@code atan(operand)}
     */
    public static UnaryOperationIndicator atan(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.ATAN, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code sinh(operand)}.
     *
     * @param operand the operand indicator
     * @return {@code sinh(operand)}
     */
    public static UnaryOperationIndicator sinh(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.SINH, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code cosh(operand)}.
     *
     * @param operand the operand indicator
     * @return {@code cosh(operand)}
     */
    public static UnaryOperationIndicator cosh(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.COSH, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code tanh(operand)}.
     *
     * @param operand the operand indicator
     * @return {@code tanh(operand)}
     */
    public static UnaryOperationIndicator tanh(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.TANH, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code sin(operand degrees)}.
     *
     * @param operand the operand indicator, in degrees
     * @return {@code sin(operand)} with a degree argument
     */
    public static UnaryOperationIndicator sinD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.SIND, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code cos(operand degrees)}.
     *
     * @param operand the operand indicator, in degrees
     * @return {@code cos(operand)} with a degree argument
     */
    public static UnaryOperationIndicator cosD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.COSD, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is {@code tan(operand degrees)}.
     *
     * @param operand the operand indicator, in degrees
     * @return {@code tan(operand)} with a degree argument
     */
    public static UnaryOperationIndicator tanD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.TAND, operand);
    }

    /**
     * Returns an {@code Indicator} whose {@code asin} result is expressed in
     * degrees.
     *
     * @param operand the operand indicator
     * @return {@code asin(operand)} in degrees
     */
    public static UnaryOperationIndicator asinD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.ASIND, operand);
    }

    /**
     * Returns an {@code Indicator} whose {@code acos} result is expressed in
     * degrees.
     *
     * @param operand the operand indicator
     * @return {@code acos(operand)} in degrees
     */
    public static UnaryOperationIndicator acosD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.ACOSD, operand);
    }

    /**
     * Returns an {@code Indicator} whose {@code atan} result is expressed in
     * degrees.
     *
     * @param operand the operand indicator
     * @return {@code atan(operand)} in degrees
     */
    public static UnaryOperationIndicator atanD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.ATAND, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is
     * {@code sinh(operand degrees converted to radians)}.
     *
     * @param operand the operand indicator, in degrees
     * @return {@code sinh(deg2rad(operand))}
     */
    public static UnaryOperationIndicator sinhD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.SINHD, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is
     * {@code cosh(operand degrees converted to radians)}.
     *
     * @param operand the operand indicator, in degrees
     * @return {@code cosh(deg2rad(operand))}
     */
    public static UnaryOperationIndicator coshD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.COSHD, operand);
    }

    /**
     * Returns an {@code Indicator} whose value is
     * {@code tanh(operand degrees converted to radians)}.
     *
     * @param operand the operand indicator, in degrees
     * @return {@code tanh(deg2rad(operand))}
     */
    public static UnaryOperationIndicator tanhD(Indicator<Num> operand) {
        return new UnaryOperationIndicator(Operation.TANHD, operand);
    }

    /**
     * Returns an {@code Indicator} that replaces a given operand value with a
     * substitute.
     *
     * @param operand          the indicator supplying the original values to
     *                         inspect
     * @param valueToReplace   the value that, when matched exactly, triggers
     *                         substitution
     * @param replacementValue the value that replaces {@code valueToReplace} in the
     *                         resulting indicator
     * @return a unary operation indicator reflecting the original values with any
     *         exact matches substituted
     */
    public static UnaryOperationIndicator substitute(final Indicator<Num> operand, final Num valueToReplace,
            final Num replacementValue) {
        return new UnaryOperationIndicator(Operation.SUBSTITUTE, operand, valueToReplace, replacementValue);
    }

    @Override
    public Num getValue(int index) {
        Num n = operand.getValue(index);
        return operator.apply(n);
    }

    @Override
    public int getCountOfUnstableBars() {
        return operand.getCountOfUnstableBars();
    }

    @Override
    public BarSeries getBarSeries() {
        return operand.getBarSeries();
    }

}
