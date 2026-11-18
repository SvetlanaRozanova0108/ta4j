/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CMOIndicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.FisherIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.UlcerIndexIndicator;
import org.ta4j.core.indicators.averages.DoubleEMAIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.HMAIndicator;
import org.ta4j.core.indicators.averages.KAMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.averages.SMMAIndicator;
import org.ta4j.core.indicators.averages.TMAIndicator;
import org.ta4j.core.indicators.averages.TripleEMAIndicator;
import org.ta4j.core.indicators.averages.VWMAIndicator;
import org.ta4j.core.indicators.averages.WMAIndicator;
import org.ta4j.core.indicators.averages.ZLEMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.donchian.DonchianChannelLowerIndicator;
import org.ta4j.core.indicators.donchian.DonchianChannelMiddleIndicator;
import org.ta4j.core.indicators.donchian.DonchianChannelUpperIndicator;
import org.ta4j.core.indicators.helpers.AmountIndicator;
import org.ta4j.core.indicators.helpers.AverageIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.RunningTotalIndicator;
import org.ta4j.core.indicators.helpers.TypicalPriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.numeric.BinaryOperationIndicator;
import org.ta4j.core.indicators.numeric.UnaryOperationIndicator;
import org.ta4j.core.indicators.statistics.MeanDeviationIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.statistics.StandardErrorIndicator;
import org.ta4j.core.indicators.statistics.VarianceIndicator;
import org.ta4j.core.indicators.supertrend.SuperTrendIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndexIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

/**
 * Indicator that is computed from a text expression.
 *
 * <p>
 * The expression is first parsed and validated (function names, arity,
 * identifiers, parentheses) and then compiled into a tree of ta4j indicators;
 * evaluation afterwards only walks that tree, so the cost of parsing/validation
 * is paid once.
 *
 * <p>
 * Two interchangeable notations are supported (and may be mixed):
 *
 * <pre>
 * SMA(Close, 10)
 * (SMA Close 10)
 * A&gt;B: (&gt; (SMA (RSI (AVG High Low) 5) 10) (RSI (AVG High Low) 5))
 *      Greater(SMA(RSI(AVG(High, Low), 5), 10), RSI(AVG(High, Low), 5))
 * </pre>
 *
 * <p>
 * Names are resolved (case-insensitive for functions) in the following order:
 * registered functions (data fields like {@code High}, {@code Low},
 * {@code Close}, indicator functions, arithmetic/logic operators) first, then
 * the parameters map (keys are parameter names, values numbers). Numeric
 * literals (e.g. the {@code 5} in {@code RSI(x, 5)}) and parameter references
 * can be used interchangeably wherever a number is expected.
 *
 * <p>
 * The value of a comparison ({@code >}, {@code >=}, {@code <}, {@code <=},
 * {@code =}, {@code !=}) or logic operator ({@code And}, {@code Or},
 * {@code Not}) is {@code 1} (true) or {@code 0} (false); any non-zero value is
 * considered true. Arithmetic is available through the named functions
 * {@code Add}, {@code Subtract}, {@code Multiply}, {@code Divide},
 * {@code Min}, {@code Max}, {@code Abs}, {@code Sqrt}, {@code Ln},
 * {@code Log}, {@code Exp}, {@code Pow}, {@code Neg}.
 *
 * <p>
 * Nesting depth is unlimited. New functions (wrapping any custom indicator)
 * can be added via {@link #registerFunction(String, int, int,
 * ExpressionFunction)}.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/S-expression">S-expression</a>
 */
public class ExpressionIndicator extends CachedIndicator<Num> {

    /**
     * Registry entry: declared arity and the function implementation.
     */
    private static final FunctionRegistry REGISTRY = new FunctionRegistry();

    static {
        // built-in functions first, so reflective auto-registration (which never
        // overwrites existing names) can only add new ones - runs once at startup
        BuiltinFunctions.register(REGISTRY);
        REGISTRY.autoRegister("org.ta4j.core.indicators");
    }

    private final String expression;
    private final Indicator<Num> compiled;

    /**
     * Constructor without parameters map.
     *
     * @param series     the bar series
     * @param expression the expression string
     * @throws ExpressionValidationException if the expression is invalid
     */
    public ExpressionIndicator(BarSeries series, String expression) {
        this(series, expression, Map.of());
    }

    /**
     * Constructor.
     *
     * @param series     the bar series
     * @param expression the expression string, e.g.
     *                   {@code "SMA(RSI(AVG(High, Low), rsiPeriod), smaPeriod)"}
     * @param parameters map of parameter name to numeric value; may be empty but
     *                   not null
     * @throws ExpressionValidationException if the expression or the parameters
     *                                       are invalid
     */
    public ExpressionIndicator(BarSeries series, String expression, Map<String, Number> parameters) {
        super(series);
        this.expression = expression;
        ExpressionParser.Node ast = ExpressionParser.parse(expression);
        this.compiled = new Compiler(getBarSeries(), parameters == null ? Map.of() : parameters).compile(ast);
    }

    /**
     * Validates the syntax of an expression, the arity of every call and that
     * every identifier is a known nullary function or a key of the parameters
     * map. Full argument type checks happen when an
     * {@link ExpressionIndicator} is compiled against a real series.
     *
     * @param expression the expression string
     * @param parameters the parameters map (may be empty)
     * @throws ExpressionValidationException on any validation error
     */
    public static void validate(String expression, Map<String, Number> parameters) {
        ExpressionParser.Node ast = ExpressionParser.parse(expression);
        new Compiler(null, parameters == null ? Map.of() : parameters).validate(ast);
    }

    /**
     * Registers a new function for all {@code ExpressionIndicator} instances.
     *
     * @param name     the function name (case-insensitive), e.g. {@code "MyInd"}
     * @param minArgs  the minimum number of arguments
     * @param maxArgs  the maximum number of arguments
     * @param function the factory building the indicator for one call
     * @throws IllegalArgumentException if the name is already taken
     */
    public static void registerFunction(String name, int minArgs, int maxArgs, ExpressionFunction function) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("function name must not be blank");
        }
        if (function == null || minArgs < 0 || maxArgs < minArgs) {
            throw new IllegalArgumentException("invalid function registration for '" + name + "'");
        }
        REGISTRY.register(name, minArgs, maxArgs, function);
    }

    /**
     * Reflectively scans additional packages (recursively) for indicator classes
     * with canonical constructor signatures and registers them as functions,
     * without overwriting existing names. The default package
     * {@code org.ta4j.core.indicators} is already scanned once at startup; use
     * this method for indicator classes in your own packages.
     *
     * @param packages the packages to scan
     * @return the number of newly registered functions
     */
    public static int autoRegisterFunctions(String... packages) {
        return REGISTRY.autoRegister((String[]) packages.clone());
    }

    /** @return all currently registered function names */
    public static Set<String> getFunctionNames() {
        return REGISTRY.names();
    }

    /** @return the original expression string */
    public String getExpression() {
        return expression;
    }

    @Override
    protected Num calculate(int index) {
        return compiled.getValue(index);
    }

    @Override
    public int getCountOfUnstableBars() {
        return compiled.getCountOfUnstableBars();
    }

    @Override
    public String toString() {
        return "ExpressionIndicator: " + expression;
    }

    // ------------------------------------------------------------- compiler

    /**
     * Compiles the parsed AST into a tree of ta4j indicators, validating names,
     * arity and argument types on the way.
     */
    private static final class Compiler {

        private final BarSeries series;
        private final Map<String, Number> parameters;

        Compiler(BarSeries series, Map<String, Number> parameters) {
            this.series = series;
            this.parameters = parameters;
        }

        /** Structural validation only (no series required). */
        void validate(ExpressionParser.Node node) {
            if (node instanceof ExpressionParser.NumberNode) {
                return;
            }
            if (node instanceof ExpressionParser.NameNode name) {
                resolveName(name);
                return;
            }
            ExpressionParser.CallNode call = (ExpressionParser.CallNode) node;
            requireFunction(call);
            for (ExpressionParser.Node arg : call.args) {
                validate(arg);
            }
        }

        Indicator<Num> compile(ExpressionParser.Node node) {
            if (series == null) {
                throw new IllegalStateException("compilation requires a bar series");
            }
            if (node instanceof ExpressionParser.NumberNode number) {
                return new ConstantIndicator<>(series, series.numFactory().numOf(number.value));
            }
            if (node instanceof ExpressionParser.NameNode name) {
                return resolveNameToIndicator(name);
            }
            ExpressionParser.CallNode call = (ExpressionParser.CallNode) node;
            requireFunction(call);
            List<ExpressionArgument> args = new ArrayList<>(call.args.size());
            for (ExpressionParser.Node arg : call.args) {
                if (arg instanceof ExpressionParser.NumberNode number) {
                    args.add(ExpressionArgument.ofConstant(series.numFactory().numOf(number.value), series));
                } else if (arg instanceof ExpressionParser.NameNode name && isParameterReference(name)) {
                    args.add(ExpressionArgument.ofConstant(
                            series.numFactory().numOf(findParameter(name.name)), series));
                } else {
                    args.add(ExpressionArgument.ofIndicator(compile(arg)));
                }
            }
            FunctionRegistry.Entry builtin = REGISTRY.get(call.name);
            try {
                return builtin.function().apply(args, series);
            } catch (ExpressionValidationException e) {
                throw new ExpressionValidationException(
                        e.getMessage() + " [in call to '" + call.name + "']", call.namePosition);
            } catch (IllegalArgumentException e) {
                throw new ExpressionValidationException(
                        e.getMessage() + " [in call to '" + call.name + "']", call.namePosition);
            }
        }

        private FunctionRegistry.Entry requireFunction(ExpressionParser.CallNode call) {
            FunctionRegistry.Entry builtin = REGISTRY.get(call.name);
            if (builtin == null) {
                throw new ExpressionValidationException("unknown function '" + call.name + "'", call.namePosition);
            }
            int count = call.args.size();
            if (count < builtin.minArgs() || count > builtin.maxArgs()) {
                throw new ExpressionValidationException("function '" + call.name + "' expects "
                        + (builtin.minArgs() == builtin.maxArgs() ? builtin.minArgs()
                                : "between " + builtin.minArgs() + " and " + builtin.maxArgs())
                        + " argument(s) but got " + count, call.namePosition);
            }
            return builtin;
        }

        /** Validates a bare name: nullary function or parameter. */
        private void resolveName(ExpressionParser.NameNode name) {
            FunctionRegistry.Entry builtin = REGISTRY.get(name.name);
            if (builtin != null) {
                if (builtin.maxArgs() > 0 && findParameter(name.name) == null) {
                    throw new ExpressionValidationException(
                            "function '" + name.name + "' requires arguments (write it as '" + name.name
                                    + "(...)') or define a parameter named '" + name.name + "'",
                            name.position);
                }
                return;
            }
            if (findParameter(name.name) == null) {
                throw new ExpressionValidationException("unknown identifier '" + name.name + "'", name.position);
            }
        }

        private Indicator<Num> resolveNameToIndicator(ExpressionParser.NameNode name) {
            FunctionRegistry.Entry builtin = REGISTRY.get(name.name);
            if (builtin != null && builtin.minArgs() == 0 && builtin.maxArgs() == 0) {
                return buildNullary(builtin, name);
            }
            Number parameter = findParameter(name.name);
            if (parameter != null) {
                return new ConstantIndicator<>(series, series.numFactory().numOf(parameter));
            }
            if (builtin != null) {
                throw new ExpressionValidationException(
                        "function '" + name.name + "' requires arguments but none were given", name.position);
            }
            throw new ExpressionValidationException("unknown identifier '" + name.name + "'", name.position);
        }

        private Indicator<Num> buildNullary(FunctionRegistry.Entry builtin, ExpressionParser.NameNode name) {
            try {
                return builtin.function().apply(List.of(), series);
            } catch (IllegalArgumentException e) {
                throw new ExpressionValidationException(e.getMessage() + " [in '" + name.name + "']", name.position);
            }
        }

        private Number findParameter(String name) {
            Number value = parameters.get(name);
            if (value != null) {
                return value;
            }
            for (Map.Entry<String, Number> entry : parameters.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        /** A bare name that is a parameter and not a registered function. */
        private boolean isParameterReference(ExpressionParser.NameNode name) {
            return REGISTRY.get(name.name) == null && findParameter(name.name) != null;
        }
    }

    // --------------------------------------------------- generic indicators

    /**
     * Applies a double unary operator (used for functions not covered by the
     * built-in unary/binary operations, e.g. trig, exp, comparisons).
     */
    static final class Map1Indicator extends CachedIndicator<Num> {
        private final Indicator<Num> operand;
        private final DoubleUnaryOperator operator;

        Map1Indicator(Indicator<Num> operand, DoubleUnaryOperator operator) {
            super(operand);
            this.operand = operand;
            this.operator = operator;
        }

        @Override
        protected Num calculate(int index) {
            double result = operator.applyAsDouble(operand.getValue(index).doubleValue());
            return toNum(result);
        }

        private Num toNum(double result) {
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return NaN.NaN;
            }
            return getBarSeries().numFactory().numOf(result);
        }

        @Override
        public int getCountOfUnstableBars() {
            return operand.getCountOfUnstableBars();
        }
    }

    /**
     * Applies a double binary operator (comparisons, logic, div, etc.).
     */
    static final class Map2Indicator extends CachedIndicator<Num> {
        private final Indicator<Num> left;
        private final Indicator<Num> right;
        private final DoubleBinaryOperator operator;

        Map2Indicator(Indicator<Num> left, Indicator<Num> right, DoubleBinaryOperator operator) {
            super(left);
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        protected Num calculate(int index) {
            double result = operator.applyAsDouble(left.getValue(index).doubleValue(),
                    right.getValue(index).doubleValue());
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return NaN.NaN;
            }
            return getBarSeries().numFactory().numOf(result);
        }

        @Override
        public int getCountOfUnstableBars() {
            return Math.max(left.getCountOfUnstableBars(), right.getCountOfUnstableBars());
        }
    }

    // ------------------------------------------------------------ arguments

    private static Indicator<Num> indicator(List<ExpressionArgument> args, int i) {
        return args.get(i).indicator();
    }

    private static int period(List<ExpressionArgument> args, int i) {
        return args.get(i).positiveInt();
    }

    /**
     * Definition and registration of all built-in functions.
     */
    private static final class BuiltinFunctions {

        private static final int UNBOUNDED = Integer.MAX_VALUE;

        static void register(FunctionRegistry registry) {
            FunctionRegistry functions = registry;

            // --- data fields -------------------------------------------------
            reg(functions, "High", 0, 0, (args, s) -> new HighPriceIndicator(s));
            reg(functions, "Low", 0, 0, (args, s) -> new LowPriceIndicator(s));
            reg(functions, "Open", 0, 0, (args, s) -> new OpenPriceIndicator(s));
            reg(functions, "Close", 0, 0, (args, s) -> new ClosePriceIndicator(s));
            reg(functions, "Volume", 0, 0, (args, s) -> new VolumeIndicator(s));
            reg(functions, "Amount", 0, 0, (args, s) -> new AmountIndicator(s));
            reg(functions, "TypicalPrice", 0, 0, (args, s) -> new TypicalPriceIndicator(s));
            reg(functions, "MedianPrice", 0, 0, (args, s) -> new MedianPriceIndicator(s));

            // --- arithmetic --------------------------------------------------
            reg(functions, "Add", 2, UNBOUNDED, (args, s) -> fold(args, BinaryOperationIndicator::sum));
            reg(functions, "Subtract", 2, 2,
                    (args, s) -> BinaryOperationIndicator.difference(indicator(args, 0), indicator(args, 1)));
            reg(functions, "Multiply", 2, UNBOUNDED, (args, s) -> fold(args, BinaryOperationIndicator::product));
            reg(functions, "Divide", 2, 2,
                    (args, s) -> BinaryOperationIndicator.quotient(indicator(args, 0), indicator(args, 1)));
            reg(functions, "Min", 2, UNBOUNDED, (args, s) -> fold(args, BinaryOperationIndicator::min));
            reg(functions, "Max", 2, UNBOUNDED, (args, s) -> fold(args, BinaryOperationIndicator::max));
            reg(functions, "Avg", 2, UNBOUNDED, (args, s) -> new AverageIndicator(
                    args.stream().map(ExpressionArgument::indicator).toList()));
            reg(functions, "Abs", 1, 1, (args, s) -> UnaryOperationIndicator.abs(indicator(args, 0)));
            reg(functions, "Sqrt", 1, 1, (args, s) -> UnaryOperationIndicator.sqrt(indicator(args, 0)));
            reg(functions, "Ln", 1, 1, (args, s) -> UnaryOperationIndicator.log(indicator(args, 0)));
            reg(functions, "Neg", 1, 1, (args, s) -> new Map1Indicator(indicator(args, 0), v -> -v));
            reg(functions, "Exp", 1, 1, (args, s) -> new Map1Indicator(indicator(args, 0), Math::exp));
            reg(functions, "Log", 1, 1, (args, s) -> new Map1Indicator(indicator(args, 0), Math::log10));
            reg(functions, "Pow", 2, 2, (args, s) -> {
                int exponent = period(args, 1);
                return UnaryOperationIndicator.pow(indicator(args, 0), exponent);
            });
            reg(functions, "Modulo", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a % b));

            // --- comparisons (yield 1/0) -------------------------------------
            reg(functions, ">", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a > b ? 1 : 0));
            reg(functions, "Greater", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a > b ? 1 : 0));
            reg(functions, "GreaterThan", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0),
                    indicator(args, 1), (a, b) -> a > b ? 1 : 0));
            reg(functions, ">=", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a >= b ? 1 : 0));
            reg(functions, "GreaterOrEqual", 2, 2,
                    (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1), (a, b) -> a >= b ? 1 : 0));
            reg(functions, "<", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a < b ? 1 : 0));
            reg(functions, "Less", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a < b ? 1 : 0));
            reg(functions, "LessThan", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a < b ? 1 : 0));
            reg(functions, "<=", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a <= b ? 1 : 0));
            reg(functions, "LessOrEqual", 2, 2,
                    (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1), (a, b) -> a <= b ? 1 : 0));
            reg(functions, "=", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a == b ? 1 : 0));
            reg(functions, "Equal", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a == b ? 1 : 0));
            reg(functions, "!=", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a != b ? 1 : 0));
            reg(functions, "NotEqual", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> a != b ? 1 : 0));

            // --- logic (non-zero == true, yield 1/0) --------------------------
            reg(functions, "And", 2, UNBOUNDED, (args, s) -> foldTruth(args, (a, b) -> a && b));
            reg(functions, "Or", 2, UNBOUNDED, (args, s) -> foldTruth(args, (a, b) -> a || b));
            reg(functions, "Not", 1, 1, (args, s) -> new Map1Indicator(indicator(args, 0), v -> v == 0 ? 1 : 0));

            // --- moving averages ----------------------------------------------
            reg(functions, "SMA", 2, 2, (args, s) -> new SMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "EMA", 2, 2, (args, s) -> new EMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "WMA", 2, 2, (args, s) -> new WMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "HMA", 2, 2, (args, s) -> new HMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "DEMA", 2, 2, (args, s) -> new DoubleEMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "TEMA", 2, 2, (args, s) -> new TripleEMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "ZLEMA", 2, 2, (args, s) -> new ZLEMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "TMA", 2, 2, (args, s) -> new TMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "SMMA", 2, 2, (args, s) -> new SMMAIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "KAMA", 1, 3, (args, s) -> switch (args.size()) {
            case 1 -> new KAMAIndicator(indicator(args, 0));
            case 2 -> new KAMAIndicator(indicator(args, 0), period(args, 1), period(args, 1), period(args, 1));
            default -> new KAMAIndicator(indicator(args, 0), period(args, 1), period(args, 2), period(args, 2));
            });
            reg(functions, "VWMA", 2, 2, (args, s) -> new VWMAIndicator(indicator(args, 0), period(args, 1)));

            // --- oscillators / channels ----------------------------------------
            reg(functions, "RSI", 2, 2, (args, s) -> new RSIIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "CMO", 2, 2, (args, s) -> new CMOIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "ROC", 2, 2, (args, s) -> new ROCIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "Momentum", 2, 2, (args, s) -> BinaryOperationIndicator.difference(indicator(args, 0),
                    new PreviousValueIndicator(indicator(args, 0), period(args, 1))));
            reg(functions, "UlcerIndex", 2, 2,
                    (args, s) -> new UlcerIndexIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "Fisher", 2, 2, (args, s) -> new FisherIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "MACD", 1, 3, (args, s) -> switch (args.size()) {
            case 1 -> new MACDIndicator(indicator(args, 0));
            case 3 -> new MACDIndicator(indicator(args, 0), period(args, 1), period(args, 2));
            default -> throw new ExpressionValidationException(
                    "MACD expects (price) or (price, shortPeriod, longPeriod)");
            });
            reg(functions, "MACDSignal", 2, 4, (args, s) -> switch (args.size()) {
            case 2 -> new EMAIndicator(new MACDIndicator(indicator(args, 0)), period(args, 1));
            case 4 -> new EMAIndicator(
                    new MACDIndicator(indicator(args, 0), period(args, 2), period(args, 3)), period(args, 1));
            default -> throw new ExpressionValidationException(
                    "MACDSignal expects (price, signalPeriod) or (price, signalPeriod, shortPeriod, longPeriod)");
            });
            reg(functions, "PPO", 1, 3, (args, s) -> switch (args.size()) {
            case 1 -> new PPOIndicator(indicator(args, 0));
            case 3 -> new PPOIndicator(indicator(args, 0), period(args, 1), period(args, 2));
            default -> throw new ExpressionValidationException(
                    "PPO expects (price) or (price, shortPeriod, longPeriod)");
            });
            reg(functions, "BollingerUpper", 2, 3, (args, s) -> bollingerUpper(args));
            reg(functions, "BollingerLower", 2, 3, (args, s) -> bollingerLower(args));
            reg(functions, "BollingerBandWidth", 2, 3, (args, s) -> new BollingerBandWidthIndicator(
                    bollingerUpper(args), bollingerMiddle(args), bollingerLower(args)));
            reg(functions, "DonchianUpper", 1, 1, (args, s) -> new DonchianChannelUpperIndicator(s, period(args, 0)));
            reg(functions, "DonchianLower", 1, 1, (args, s) -> new DonchianChannelLowerIndicator(s, period(args, 0)));
            reg(functions, "DonchianMiddle", 1, 1,
                    (args, s) -> new DonchianChannelMiddleIndicator(s, period(args, 0)));
            reg(functions, "SuperTrend", 0, 2, (args, s) -> switch (args.size()) {
            case 0 -> new SuperTrendIndicator(s);
            case 2 -> new SuperTrendIndicator(s, period(args, 0), args.get(1).number().doubleValue());
            default -> throw new ExpressionValidationException(
                    "SuperTrend expects () or (barCount, multiplier)");
            });

            // --- volume ----------------------------------------------------------
            reg(functions, "OBV", 0, 0, (args, s) -> new OnBalanceVolumeIndicator(s));
            reg(functions, "MFI", 1, 1, (args, s) -> new MoneyFlowIndexIndicator(s, period(args, 0)));

            // --- statistics / helpers ---------------------------------------------
            reg(functions, "StdDev", 2, 2,
                    (args, s) -> new StandardDeviationIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "Variance", 2, 2, (args, s) -> new VarianceIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "MeanDev", 2, 2,
                    (args, s) -> new MeanDeviationIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "StdErr", 2, 2, (args, s) -> new StandardErrorIndicator(indicator(args, 0),
                    period(args, 1)));
            reg(functions, "MaxValue", 2, 2, (args, s) -> new HighestValueIndicator(indicator(args, 0),
                    period(args, 1)));
            reg(functions, "MinValue", 2, 2, (args, s) -> new LowestValueIndicator(indicator(args, 0),
                    period(args, 1)));
            reg(functions, "Lag", 1, 2, (args, s) -> args.size() == 1
                            ? new PreviousValueIndicator(indicator(args, 0))
                            : new PreviousValueIndicator(indicator(args, 0), period(args, 1)));
            reg(functions, "Sum", 2, 2, (args, s) -> new RunningTotalIndicator(indicator(args, 0), period(args, 1)));

            // --- trigonometry (NSTRDIND: degrees + radians) ---------------------
            reg(functions, "Sin", 1, 1, (args, s) -> UnaryOperationIndicator.sin(indicator(args, 0)));
            reg(functions, "Cos", 1, 1, (args, s) -> UnaryOperationIndicator.cos(indicator(args, 0)));
            reg(functions, "Tan", 1, 1, (args, s) -> UnaryOperationIndicator.tan(indicator(args, 0)));
            reg(functions, "ASin", 1, 1, (args, s) -> UnaryOperationIndicator.asin(indicator(args, 0)));
            reg(functions, "ACos", 1, 1, (args, s) -> UnaryOperationIndicator.acos(indicator(args, 0)));
            reg(functions, "ATan", 1, 1, (args, s) -> UnaryOperationIndicator.atan(indicator(args, 0)));
            reg(functions, "Sinh", 1, 1, (args, s) -> UnaryOperationIndicator.sinh(indicator(args, 0)));
            reg(functions, "Cosh", 1, 1, (args, s) -> UnaryOperationIndicator.cosh(indicator(args, 0)));
            reg(functions, "Tanh", 1, 1, (args, s) -> UnaryOperationIndicator.tanh(indicator(args, 0)));
            reg(functions, "SinD", 1, 1, (args, s) -> UnaryOperationIndicator.sinD(indicator(args, 0)));
            reg(functions, "CosD", 1, 1, (args, s) -> UnaryOperationIndicator.cosD(indicator(args, 0)));
            reg(functions, "TanD", 1, 1, (args, s) -> UnaryOperationIndicator.tanD(indicator(args, 0)));
            reg(functions, "ASinD", 1, 1, (args, s) -> UnaryOperationIndicator.asinD(indicator(args, 0)));
            reg(functions, "ACosD", 1, 1, (args, s) -> UnaryOperationIndicator.acosD(indicator(args, 0)));
            reg(functions, "ATanD", 1, 1, (args, s) -> UnaryOperationIndicator.atanD(indicator(args, 0)));
            reg(functions, "SinhD", 1, 1, (args, s) -> UnaryOperationIndicator.sinhD(indicator(args, 0)));
            reg(functions, "CoshD", 1, 1, (args, s) -> UnaryOperationIndicator.coshD(indicator(args, 0)));
            reg(functions, "TanhD", 1, 1, (args, s) -> UnaryOperationIndicator.tanhD(indicator(args, 0)));

            // --- remaining NSTRDIND logic operators -----------------------------
            reg(functions, "Xor", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> truthOf(a) ^ truthOf(b) ? 1 : 0));
            reg(functions, "Nand", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> !(truthOf(a) && truthOf(b)) ? 1 : 0));
            reg(functions, "Nor", 2, 2, (args, s) -> new Map2Indicator(indicator(args, 0), indicator(args, 1),
                    (a, b) -> !(truthOf(a) || truthOf(b)) ? 1 : 0));

            // --- XY regression (NSTRDIND: Linear XY Regression) ------------------
            reg(functions, "XYRegressionR", 3, 3, (args, s) -> xyRegression(args, s,
                    org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType.R));
            reg(functions, "XYRegressionRSquared", 3, 3, (args, s) -> xyRegression(args, s,
                    org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType.R_SQUARED));
            reg(functions, "XYRegressionSlope", 3, 3, (args, s) -> xyRegression(args, s,
                    org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType.SLOPE));
            reg(functions, "XYRegressionIntercept", 3, 3, (args, s) -> xyRegression(args, s,
                    org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType.Y_INTERCEPT));
            reg(functions, "XYRegressionXIntercept", 3, 3, (args, s) -> xyRegression(args, s,
                    org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType.X_INTERCEPT));
            reg(functions, "XYRegressionStdError", 3, 3, (args, s) -> xyRegression(args, s,
                    org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType.STD_ERROR));
        }

        private static Indicator<Num> xyRegression(List<ExpressionArgument> args, BarSeries series,
                org.ta4j.core.indicators.statistics.XYRegressionIndicator.XYRegressionType type) {
            return new org.ta4j.core.indicators.statistics.XYRegressionIndicator(indicator(args, 0),
                    indicator(args, 1), period(args, 2), type);
        }

        private static void reg(FunctionRegistry functions, String name, int minArgs, int maxArgs,
                ExpressionFunction function) {
            functions.register(name, minArgs, maxArgs, function);
        }

        private static Indicator<Num> fold(List<ExpressionArgument> args,
                java.util.function.BinaryOperator<Indicator<Num>> operator) {
            Indicator<Num> result = args.get(0).indicator();
            for (int i = 1; i < args.size(); i++) {
                result = operator.apply(result, args.get(i).indicator());
            }
            return result;
        }

        private static Indicator<Num> foldTruth(List<ExpressionArgument> args,
                java.util.function.BinaryOperator<Boolean> operator) {
            Indicator<Num> result = args.get(0).indicator();
            for (int i = 1; i < args.size(); i++) {
                Indicator<Num> right = args.get(i).indicator();
                Indicator<Num> left = result;
                result = new Map2Indicator(left, right, (a, b) -> operator.apply(truthOf(a), truthOf(b)) ? 1 : 0);
            }
            return result;
        }

        private static boolean truthOf(double value) {
            return !Double.isNaN(value) && value != 0;
        }

        private static BollingerBandsMiddleIndicator bollingerMiddle(List<ExpressionArgument> args) {
            // the middle band of a Bollinger band of (x, n) is SMA(x, n)
            return new BollingerBandsMiddleIndicator(
                    new SMAIndicator(args.get(0).indicator(), args.get(1).positiveInt()));
        }

        private static Indicator<Num> deviation(List<ExpressionArgument> args) {
            return new StandardDeviationIndicator(args.get(0).indicator(), args.get(1).positiveInt());
        }

        private static Num multiplier(List<ExpressionArgument> args, BarSeries series) {
            double k = args.size() > 2 ? args.get(2).number().doubleValue() : 2;
            return series.numFactory().numOf(k);
        }

        private static BollingerBandsUpperIndicator bollingerUpper(List<ExpressionArgument> args) {
            return new BollingerBandsUpperIndicator(bollingerMiddle(args), deviation(args),
                    multiplier(args, args.get(0).indicator().getBarSeries()));
        }

        private static BollingerBandsLowerIndicator bollingerLower(List<ExpressionArgument> args) {
            return new BollingerBandsLowerIndicator(bollingerMiddle(args), deviation(args),
                    multiplier(args, args.get(0).indicator().getBarSeries()));
        }
    }
}
