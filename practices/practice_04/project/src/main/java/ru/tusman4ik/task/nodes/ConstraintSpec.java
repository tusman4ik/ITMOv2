package ru.tusman4ik.task.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ConstraintSpec<T> implements Node<T>, Constrained<T> {

    private final List<Predicate<T>> predicates;

    ConstraintSpec(Predicate<T> first) {
        this.predicates = List.of(first);
    }

    private ConstraintSpec(List<Predicate<T>> predicates) {
        this.predicates = predicates;
    }

    public ConstraintSpec<T> and(Predicate<T> next) {
        List<Predicate<T>> merged = new ArrayList<>(predicates);
        merged.add(next);
        return new ConstraintSpec<>(List.copyOf(merged));
    }

    @Override
    public List<Predicate<T>> predicates() {
        return predicates;
    }

    @Override
    public T get() {
        throw new IllegalStateException(
                "Unwired constraint spec: pick node was not processed by the wire phase"
        );
    }
}
