package ru.tusman4ik.taskservice.task.node;

import ru.tusman4ik.taskservice.task.generator.ContextHolder;

public final class Nodes {

    private Nodes() {}

    public static <T> Node<T> memoized(Node<T> raw) {
        return new Memoized<>(raw);
    }

    public record Memoized<T>(Node<T> delegate) implements Node<T> {

        @Override
        public T get() {
            return ContextHolder.current().cached(this, delegate::get);
        }
    }
}
