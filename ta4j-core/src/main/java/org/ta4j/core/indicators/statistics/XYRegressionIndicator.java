/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.statistics;

import static org.ta4j.core.num.NaN.NaN;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * XY linear regression indicator.
 *
 * <p>
 * Ordinary least squares regression of two indicator series (x-values from one
 * series, y-values from another) over a rolling time frame. Unlike
 * {@link SimpleLinearRegressionIndicator}, which always regresses against the
 * bar index (time), this indicator regresses one series against another.
 *
 * <pre>
 * y = slope * x + yIntercept
 * </pre>
 *
 * Depending on the type, it returns:
 * <ul>
 * <li>{@code SLOPE}: regression coefficient</li>
 * <li>{@code Y_INTERCEPT}: y value where the regression line crosses x = 0</li>
 * <li>{@code X_INTERCEPT}: x value where the regression line crosses y = 0</li>
 * <li>{@code R}: Pearson correlation coefficient</li>
 * <li>{@code R_SQUARED}: coefficient of determination</li>
 * <li>{@code STD_ERROR}: standard error of the estimate
 * {@code sqrt(SSE / (n - 2))}</li>
 * </ul>
 */
public class XYRegressionIndicator extends CachedIndicator<Num> {

    /**
     * The type of the outcome of the {@link XYRegressionIndicator}.
     */
    public enum XYRegressionType {
        SLOPE, Y_INTERCEPT, X_INTERCEPT, R, R_SQUARED, STD_ERROR
    }

    private final Indicator<Num> xIndicator;
    private final Indicator<Num> yIndicator;
    private final int barCount;
    private final XYRegressionType type;

    /**
     * Constructor.
     *
     * @param xIndicator the indicator providing the x-values
     * @param yIndicator the indicator providing the y-values
     * @param barCount   the time frame
     * @param type       the type of the outcome value
     */
    public XYRegressionIndicator(Indicator<Num> xIndicator, Indicator<Num> yIndicator, int barCount,
            XYRegressionType type) {
        super(xIndicator);
        this.xIndicator = xIndicator;
        this.yIndicator = yIndicator;
        this.barCount = barCount;
        this.type = type;
    }

    @Override
    protected Num calculate(int index) {
        final int startIndex = Math.max(getBarSeries().getBeginIndex(), index - barCount + 1);
        final int n = index - startIndex + 1;
        if (n < 2) {
            // Not enough observations to compute a regression line
            return NaN;
        }
        if (type == XYRegressionType.STD_ERROR && n < 3) {
            // Degrees of freedom (n - 2) not available
            return NaN;
        }

        final var numFactory = getBarSeries().numFactory();
        Num zero = numFactory.zero();
        Num sumX = zero;
        Num sumY = zero;
        Num sumXX = zero;
        Num sumYY = zero;
        Num sumXY = zero;
        for (int i = startIndex; i <= index; i++) {
            Num x = xIndicator.getValue(i);
            Num y = yIndicator.getValue(i);
            if (x.isNaN() || y.isNaN()) {
                return NaN;
            }
            sumX = sumX.plus(x);
            sumY = sumY.plus(y);
            sumXX = sumXX.plus(x.multipliedBy(x));
            sumYY = sumYY.plus(y.multipliedBy(y));
            sumXY = sumXY.plus(x.multipliedBy(y));
        }
        Num nbObservations = numFactory.numOf(n);

        Num covTerm = nbObservations.multipliedBy(sumXY).minus(sumX.multipliedBy(sumY));
        Num varXTerm = nbObservations.multipliedBy(sumXX).minus(sumX.multipliedBy(sumX));
        Num varYTerm = nbObservations.multipliedBy(sumYY).minus(sumY.multipliedBy(sumY));

        switch (type) {
        case R: {
            Num denominator = varXTerm.multipliedBy(varYTerm).sqrt();
            if (denominator.isZero()) {
                return NaN;
            }
            return covTerm.dividedBy(denominator);
        }
        case R_SQUARED: {
            Num denominator = varXTerm.multipliedBy(varYTerm);
            if (denominator.isZero()) {
                return NaN;
            }
            Num r = covTerm.dividedBy(denominator.sqrt());
            return r.multipliedBy(r);
        }
        case STD_ERROR: {
            Num slope = covTerm.dividedBy(varXTerm);
            Num intercept = sumY.minus(slope.multipliedBy(sumX)).dividedBy(nbObservations);
            Num sse = zero;
            for (int i = startIndex; i <= index; i++) {
                Num residual = yIndicator.getValue(i)
                        .minus(slope.multipliedBy(xIndicator.getValue(i)).plus(intercept));
                sse = sse.plus(residual.multipliedBy(residual));
            }
            return sse.dividedBy(numFactory.numOf(n - 2)).sqrt();
        }
        default: {
            Num slope = covTerm.dividedBy(varXTerm);
            Num intercept = sumY.minus(slope.multipliedBy(sumX)).dividedBy(nbObservations);
            if (type == XYRegressionType.SLOPE) {
                return slope;
            }
            if (type == XYRegressionType.Y_INTERCEPT) {
                return intercept;
            }
            // X_INTERCEPT
            if (slope.isZero()) {
                return NaN;
            }
            return zero.minus(intercept).dividedBy(slope);
        }
        }
    }

    @Override
    public int getCountOfUnstableBars() {
        return Math.max(xIndicator.getCountOfUnstableBars(), yIndicator.getCountOfUnstableBars())
                + Math.max(1, barCount - 1);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount + ", type: " + type;
    }
}
