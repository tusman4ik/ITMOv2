package ru.tusman4ik.task.resources.pick;

import org.jspecify.annotations.NonNull;
import ru.tusman4ik.task.nodes.annotations.PickNodeDef;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public record PickShape(Class<?> row, int n, boolean distinct) {

    public static PickShape of(Field field, PickNodeDef def) {
        nShouldBePositive(field, def);

        ParameterizedType parameterized = getParameterizedType(field);

        Type argument = parameterized.getActualTypeArguments()[0];

        if (argument instanceof Class<?> row) {
            return handleClass(field, def, row);
        }

        if (argument instanceof ParameterizedType listType && listType.getRawType() == List.class) {
            return handleList(field, def, listType);
        }

        throw new IllegalStateException("Unsupported type argument: " + field);
    }

    private static @NonNull PickShape handleList(Field field, PickNodeDef def, ParameterizedType listType) {
        if (!(listType.getActualTypeArguments()[0] instanceof Class<?> row)) {
            throw new IllegalStateException("Unsupported list element type: " + field);
        }
        if (def.n() < 2) {
            throw new IllegalStateException("List pick requires n >= 2: " + field);
        }
        return new PickShape(row, def.n(), def.distinct());
    }

    private static @NonNull PickShape handleClass(Field field, PickNodeDef def, Class<?> row) {
        if (def.distinct()) {
            throw new IllegalStateException(
                    "distinct requires a list pick (Node<List<T>> and n >= 2): " + field
            );
        }
        if (def.n() != 1) {
            throw new IllegalStateException("Scalar pick requires n == 1: " + field);
        }
        return new PickShape(row, def.n(), false);
    }

    private static @NonNull ParameterizedType getParameterizedType(Field field) {
        Type generic = field.getGenericType();
        if (!(generic instanceof ParameterizedType parameterized)
                || parameterized.getRawType() != ru.tusman4ik.task.nodes.Node.class) {
            throw new IllegalStateException("@PickNodeDef field must be declared as Node<...>: " + field);
        }
        return parameterized;
    }

    private static void nShouldBePositive(Field field, PickNodeDef def) {
        if (def.n() < 1) {
            throw new IllegalStateException("@PickNodeDef requires n >= 1: " + field);
        }
    }
}
