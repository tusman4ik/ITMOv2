package ru.tusman4ik.task.nodes;

import java.util.function.Predicate;

public final class Constraints {

    private Constraints() {
    }

    public static <T> ConstraintSpec<T> of(Predicate<T> first) {
        return new ConstraintSpec<>(first);
    }
}
