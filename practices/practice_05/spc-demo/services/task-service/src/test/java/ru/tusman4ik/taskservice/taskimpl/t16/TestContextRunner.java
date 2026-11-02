package ru.tusman4ik.taskservice.taskimpl.t16;

import ru.tusman4ik.taskservice.task.generator.Context;
import ru.tusman4ik.taskservice.task.generator.ContextHolder;
import ru.tusman4ik.taskservice.task.id.InstanceId;
import ru.tusman4ik.taskservice.task.node.Node;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public final class TestContextRunner {

    private static final ScopedValue<Context> CONTEXT;

    static {
        try {
            Field field = ContextHolder.class.getDeclaredField("CONTEXT");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ScopedValue<Context> scopedValue = (ScopedValue<Context>) field.get(null);
            CONTEXT = scopedValue;
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private TestContextRunner() {
    }

    public static <T> T runInContext(InstanceId id, Supplier<T> supplier) {
        return ScopedValue.where(CONTEXT, new Context(id)).call(supplier::get);
    }

    public static void runInContext(InstanceId id, Runnable runnable) {
        ScopedValue.where(CONTEXT, new Context(id)).run(runnable);
    }

    public static void memoizeNodes(Object generator) {
        for (Field field : generator.getClass().getDeclaredFields()) {
            if (Node.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    Node<?> node = (Node<?>) field.get(generator);
                    if (node != null && !(node instanceof ru.tusman4ik.taskservice.task.node.Nodes.Memoized<?>)) {
                        field.set(generator, ru.tusman4ik.taskservice.task.node.Nodes.memoized(node));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
