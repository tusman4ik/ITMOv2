package ru.tusman4ik.coderunner.models.mappers;

import java.util.Collection;

public final class ValidationUtils {
    private ValidationUtils() {}

    public static void addError(String msg, IContext ctx) {
        ctx.getErrors().add(String.format("%s, %s", ctx.getCurrentPos(), msg));
    }

    public static void shouldBeNotNull(Object obj, IContext ctx) {
        if (obj == null) {
            ctx.getErrors().add(String.format("%s: should not be null", ctx.getCurrentPos()));
        }
    }

    public static void shoulNotContainsPlaceholders(String pattern, String str, IContext ctx) {
        if (str == null || str.isBlank()) {
            ctx.getErrors().add(String.format("%s: should not be empty or blank", ctx.getCurrentPos()));
            return;
        }
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern can not be null");
        }
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        if (p.matcher(str).find()) {
            ctx.getErrors().add(String.format("%s: should not contain placeholder pattern '%s' (got '%s')", ctx.getCurrentPos(), pattern, str));
        }
    }

    public static void shouldBeNotBlank(String str, IContext ctx) {
        if (str == null || str.isBlank()) {
            ctx.getErrors().add(String.format("%s: should not be empty or blank", ctx.getCurrentPos()));
        }
    }

    public static <N extends Number> void shouldBePositive(N val, IContext ctx) {
        if (val == null) {
            ctx.getErrors().add(String.format("%s: should not be null", ctx.getCurrentPos()));
            return;
        }
        if (val.doubleValue() <= 0) {
            ctx.getErrors().add(String.format("%s: must be positive (was %s)", ctx.getCurrentPos(), val));
        }
    }

    public static <N extends Number> void shouldBeNonNegative(N val, IContext ctx) {
        if (val == null) {
            ctx.getErrors().add(String.format("%s: should not be null", ctx.getCurrentPos()));
            return;
        }
        if (val.doubleValue() < 0) {
            ctx.getErrors().add(String.format("%s: must be non-negative (was %s)", ctx.getCurrentPos(), val));
        }
    }

    public static <N extends Number> void shouldBeInRange(N val, N min, N max, IContext ctx) {
        if (val == null) {
            ctx.getErrors().add(String.format("%s: should not be null", ctx.getCurrentPos()));
            return;
        }
        double dVal = val.doubleValue();
        if (dVal < min.doubleValue() || dVal > max.doubleValue()) {
            ctx.getErrors().add(String.format("%s: must be in range [%s, %s] (was %s)", ctx.getCurrentPos(), min, max, val));
        }
    }

    public static void shouldNotBeEmpty(Collection<?> col, IContext ctx) {
        if (col == null || col.isEmpty()) {
            ctx.getErrors().add(String.format("%s: should not be empty", ctx.getCurrentPos()));
        }
    }

    public static void shouldBeValidUrlOrPath(String str, IContext ctx) {
        if (str == null || str.isBlank()) {
            ctx.getErrors().add(String.format("%s: should be a valid URL or path, but was empty", ctx.getCurrentPos()));
            return;
        }
        try {
            new java.net.URI(str);
        } catch (Exception e) {
            ctx.getErrors().add(String.format("%s: '%s' is not a valid URL or path format", ctx.getCurrentPos(), str));
        }
    }

    public static <T> void shouldBeOneOf(T val, Collection<T> allowed, IContext ctx) {
        if (val == null) {
            ctx.getErrors().add(String.format("%s: should not be null", ctx.getCurrentPos()));
            return;
        }
        if (allowed == null || !allowed.contains(val)) {
            ctx.getErrors().add(String.format("%s: value '%s' is not allowed", ctx.getCurrentPos(), val));
        }
    }

    public static void shouldNotContainNulls(Collection<?> col, IContext ctx) {
        if (col == null) {
            ctx.getErrors().add(String.format("%s: should not be null", ctx.getCurrentPos()));
            return;
        }
        int index = 0;
        for (Object item : col) {
            if (item == null) {
                ctx.getErrors().add(String.format("%s[%d]: item should not be null", ctx.getCurrentPos(), index));
            }
            index++;
        }
    }
}
