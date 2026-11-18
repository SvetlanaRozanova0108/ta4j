/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.expression;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

/**
 * Registry of the functions available to {@link ExpressionIndicator}.
 *
 * <p>
 * Besides explicitly registered functions (built-ins and user registrations)
 * the registry can be filled by a one-time reflective scan
 * ({@link #autoRegister(String...)}) over indicator classes with canonical
 * constructor signatures:
 * <ul>
 * <li>{@code (BarSeries)}</li>
 * <li>{@code (BarSeries, int)} / {@code (BarSeries, int, int)}</li>
 * <li>{@code (Indicator<Num>)}</li>
 * <li>{@code (Indicator<Num>, int)} / {@code (Indicator<Num>, int, int)}</li>
 * </ul>
 * The function name is the class simple name without the trailing
 * {@code Indicator}, uppercased. Names that are already registered (built-ins,
 * aliases) are never overwritten.
 */
final class FunctionRegistry {

    /** Registry entry: declared arity and the function implementation. */
    record Entry(int minArgs, int maxArgs, ExpressionFunction function) {
    }

    private final Map<String, Entry> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    synchronized Entry get(String name) {
        return functions.get(name.toUpperCase(Locale.ROOT));
    }

    synchronized void register(String name, int minArgs, int maxArgs, ExpressionFunction function) {
        String key = name.toUpperCase(Locale.ROOT);
        if (functions.containsKey(key)) {
            throw new IllegalArgumentException("function '" + name + "' is already registered");
        }
        functions.put(key, new Entry(minArgs, maxArgs, function));
    }

    synchronized boolean registerIfAbsent(String name, int minArgs, int maxArgs, ExpressionFunction function) {
        String key = name.toUpperCase(Locale.ROOT);
        if (functions.containsKey(key)) {
            return false;
        }
        functions.put(key, new Entry(minArgs, maxArgs, function));
        return true;
    }

    synchronized Set<String> names() {
        return Set.copyOf(functions.keySet());
    }

    synchronized int size() {
        return functions.size();
    }

    // ------------------------------------------------------------- scanning

    /**
     * Reflectively scans the given packages for concrete indicator classes with
     * canonical constructor signatures and registers them under their derived
     * names. Already registered names (built-ins, aliases, user functions) are
     * never overwritten. Intended to run once at startup.
     *
     * @param packageNames the packages to scan recursively, e.g.
     *                     {@code "org.ta4j.core.indicators"}
     * @return the number of newly registered functions
     */
    int autoRegister(String... packageNames) {
        Set<String> classNames = new LinkedHashSet<>();
        for (String packageName : packageNames) {
            classNames.addAll(findClasses(packageName));
        }
        int added = 0;
        for (String className : classNames) {
            Class<?> candidate = load(className);
            if (!isRegistrable(candidate)) {
                continue;
            }
            String name = deriveName(candidate);
            Entry entry = buildEntry(candidate);
            if (entry != null && registerIfAbsent(name, entry.minArgs(), entry.maxArgs(), entry.function())) {
                added++;
            }
        }
        return added;
    }

    private static Class<?> load(String className) {
        for (ClassLoader loader : classLoaders()) {
            try {
                return Class.forName(className, false, loader);
            } catch (Throwable ignored) {
                // try the next loader
            }
        }
        return null;
    }

    private static Iterable<ClassLoader> classLoaders() {
        LinkedHashSet<ClassLoader> loaders = new LinkedHashSet<>();
        try {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                loaders.add(contextLoader);
            }
        } catch (SecurityException ignored) {
            // ignore
        }
        loaders.add(FunctionRegistry.class.getClassLoader());
        return loaders;
    }

    private static Set<String> findClasses(String packageName) {
        Set<String> classNames = new LinkedHashSet<>();
        String path = packageName.replace('.', '/');
        for (ClassLoader loader : classLoaders()) {
            try {
                Enumeration<URL> resources = loader.getResources(path);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    if ("file".equals(url.getProtocol())) {
                        collectFromDirectory(new File(url.toURI()), packageName.replace('.', '/'), classNames);
                    } else if ("jar".equals(url.getProtocol())) {
                        collectFromJar(url, path, classNames);
                    }
                }
            } catch (Exception ignored) {
                // a missing/broken package root simply contributes nothing
            }
        }
        return classNames;
    }

    private static void collectFromDirectory(File dir, String packagePath, Set<String> out) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectFromDirectory(file, packagePath + '/' + file.getName(), out);
            } else if (file.getName().endsWith(".class")) {
                String relative = packagePath + '/' + file.getName().substring(0, file.getName().length() - 6);
                out.add(relative.replace('/', '.'));
            }
        }
    }

    private static void collectFromJar(URL url, String path, Set<String> out) {
        JarFile jarFile = null;
        try {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            connection.setUseCaches(false);
            jarFile = connection.getJarFile();
            Iterator<JarEntry> entries = jarFile.entries().asIterator();
            while (entries.hasNext()) {
                JarEntry entry = entries.next();
                String entryName = entry.getName();
                if (entryName.endsWith(".class") && entryName.startsWith(path + "/")) {
                    out.add(entryName.substring(0, entryName.length() - 6).replace('/', '.'));
                }
            }
        } catch (Exception ignored) {
            // unreadable jar contributes nothing
        }
    }

    private static boolean isRegistrable(Class<?> c) {
        if (c == null || c.isInterface() || java.lang.reflect.Modifier.isAbstract(c.getModifiers())
                || !java.lang.reflect.Modifier.isPublic(c.getModifiers())) {
            return false;
        }
        String simple = c.getSimpleName();
        if (!simple.endsWith("Indicator") || simple.startsWith("Abstract") || simple.contains("Test")) {
            return false;
        }
        return Indicator.class.isAssignableFrom(c) && indicatorTypeArgument(c) == Num.class;
    }

    private static String deriveName(Class<?> c) {
        String simple = c.getSimpleName();
        return simple.substring(0, simple.length() - "Indicator".length()).toUpperCase(Locale.ROOT);
    }

    /**
     * Builds a function entry for the first supported canonical constructor of
     * the class, or {@code null} when there is none.
     */
    private static Entry buildEntry(Class<?> c) {
        Constructor<?> best = null;
        for (Constructor<?> ctor : c.getConstructors()) {
            if (supportedSignature(ctor.getParameterTypes())
                    && (best == null || ctor.getParameterCount() < best.getParameterCount())) {
                best = ctor; // prefer the simplest signature
            }
        }
        if (best == null) {
            return null;
        }
        Constructor<?> ctor = best;
        Class<?>[] parameterTypes = ctor.getParameterTypes();
        int numIndicatorArgs = 0;
        int intArgs = 0;
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType == BarSeries.class) {
                continue;
            }
            if (Indicator.class.isAssignableFrom(parameterType)) {
                numIndicatorArgs++;
            } else if (parameterType == int.class) {
                intArgs++;
            } else {
                return null;
            }
        }
        ExpressionFunction function = (args, series) -> {
            Object[] initArgs = new Object[parameterTypes.length];
            int argIndex = 0;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == BarSeries.class) {
                    initArgs[i] = series;
                } else if (Indicator.class.isAssignableFrom(parameterTypes[i])) {
                    initArgs[i] = args.get(argIndex++).indicator();
                } else {
                    initArgs[i] = args.get(argIndex++).positiveInt();
                }
            }
            try {
                return (Indicator<Num>) ctor.newInstance(initArgs);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ExpressionValidationException("cannot instantiate " + c.getSimpleName() + ": "
                        + e.getMessage());
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause() == null ? e : e.getCause();
                throw new ExpressionValidationException(
                        c.getSimpleName() + ": " + cause.getMessage());
            } catch (IllegalArgumentException e) {
                throw new ExpressionValidationException(
                        c.getSimpleName() + ": incompatible arguments (" + e.getMessage() + ")");
            }
        };
        return new Entry(numIndicatorArgs + intArgs, numIndicatorArgs + intArgs, function);
    }

    private static boolean supportedSignature(Class<?>[] parameterTypes) {
        boolean hasSeries = false;
        boolean hasIndicator = false;
        boolean hasInt = false;
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType == BarSeries.class) {
                if (hasSeries) {
                    return false;
                }
                hasSeries = true;
            } else if (Indicator.class.isAssignableFrom(parameterType)) {
                if (hasIndicator) {
                    return false; // only a single indicator argument is supported
                }
                hasIndicator = true;
            } else if (parameterType == int.class) {
                hasInt = true;
            } else {
                return false;
            }
        }
        if (!hasSeries && !hasIndicator) {
            return false; // a constructor without series and without indicator input
        }
        // at most two int parameters (e.g. diBarCount, adxBarCount)
        long intCount = java.util.Arrays.stream(parameterTypes).filter(t -> t == int.class).count();
        return intCount <= 2 && (!hasInt || hasSeries || hasIndicator);
    }

    // --------------------------------------------------- generic resolution

    /**
     * Resolves the type argument {@code X} of the implemented
     * {@code Indicator<X} through the whole class hierarchy (with type-variable
     * substitution), or {@code null} when it cannot be resolved.
     */
    static Class<?> indicatorTypeArgument(Class<?> c) {
        Map<TypeVariable<?>, Class<?>> bindings = new HashMap<>();
        Class<?> current = c;
        while (current != null && current != Object.class) {
            for (Type iface : current.getGenericInterfaces()) {
                if (rawType(iface) == Indicator.class) {
                    return resolveArgument(iface, bindings);
                }
            }
            Type supertype = current.getGenericSuperclass();
            if (supertype == null) {
                return null;
            }
            Class<?> superRaw = rawType(supertype);
            if (superRaw == Indicator.class) {
                return resolveArgument(supertype, bindings);
            }
            if (supertype instanceof ParameterizedType parameterized && superRaw != null) {
                TypeVariable<?>[] superParams = superRaw.getTypeParameters();
                Type[] actuals = parameterized.getActualTypeArguments();
                for (int i = 0; i < Math.min(superParams.length, actuals.length); i++) {
                    Class<?> bound = resolveClass(actuals[i], bindings);
                    if (bound != null) {
                        bindings.putIfAbsent(superParams[i], bound);
                    }
                }
            }
            current = superRaw;
        }
        return null;
    }

    private static Class<?> resolveArgument(Type parameterized, Map<TypeVariable<?>, Class<?>> bindings) {
        if (parameterized instanceof ParameterizedType type) {
            return resolveClass(type.getActualTypeArguments()[0], bindings);
        }
        return null;
    }

    private static Class<?> resolveClass(Type type, Map<TypeVariable<?>, Class<?>> bindings) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof TypeVariable<?> variable) {
            return bindings.get(variable);
        }
        return null;
    }

    private static Class<?> rawType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterized) {
            return (Class<?>) parameterized.getRawType();
        }
        return null;
    }
}
