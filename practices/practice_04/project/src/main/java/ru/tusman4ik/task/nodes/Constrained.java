package ru.tusman4ik.task.nodes;

import java.util.List;
import java.util.function.Predicate;

public interface Constrained<T> {

    List<Predicate<T>> predicates();
}
