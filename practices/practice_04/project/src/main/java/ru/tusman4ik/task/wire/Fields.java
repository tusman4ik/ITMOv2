package ru.tusman4ik.task.wire;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class Fields {

    private Fields() {
    }

    public static List<Field> allOf(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    public static Object read(Object target, Field field) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot read field " + field, e);
        }
    }

    public static void write(Object target, Field field, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot write field " + field, e);
        }
    }

    public static void inject(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("cannot inject " + name + " into " + target.getClass(), e);
        }
    }
}
