package ru.tusman4ik.task.nodes;

import ru.tusman4ik.task.context.Context;

import java.util.function.UnaryOperator;

public final class Nodes {

    private Nodes() {}

    public static <T> Node<T> memoized(Node<T> raw) {
        return new Memoized<>(raw);
    }

    public static <T> Node<T> mutated(Node<T> source, double probability, UnaryOperator<T> mutator) {
        if (source == null) {
            throw new IllegalArgumentException("mutated source must be not null. Use lambda");
        }
        if (mutator == null) {
            throw new IllegalArgumentException("mutated mutator must be not null. Use lambda");
        }
        if (Double.isNaN(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                    "mutated probability must be in [0,1], got " + probability);
        }
        return () -> {
            T value = source.get();
            if (Context.current().random().nextDouble() < probability) {
                return mutator.apply(value);
            }
            return value;
        };
    }

    public record Memoized<T>(Node<T> delegate) implements Node<T> {
        @Override
        public T get() {
            Context ctx = Context.current();
            return ctx.cached(this, delegate::get);
        }
    }
}
