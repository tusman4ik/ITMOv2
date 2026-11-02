package ru.tusman4ik.task.resources.pick;

import ru.tusman4ik.task.context.Context;
import ru.tusman4ik.task.nodes.Constrained;
import ru.tusman4ik.task.nodes.Node;

import java.util.List;
import java.util.function.Predicate;

public final class PickNodeFactory {

    private PickNodeFactory() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Node<?> build(List<?> data, PickShape shape, List<? extends Predicate<?>> predicates) {
        if (shape.n() > 1 && shape.distinct()) {
            return () -> PickSampler.pickDistinctN(Context.current().random(), (List) data, shape.n(), (List) predicates);
        }
        if (shape.n() > 1) {
            return () -> PickSampler.pickN(Context.current().random(), (List) data, shape.n(), (List) predicates);
        }
        return () -> PickSampler.pick(Context.current().random(), (List) data, (List) predicates);
    }

    public static List<? extends Predicate<?>> predicatesOf(Object fieldValue) {
        if (fieldValue instanceof Constrained<?> constrained) {
            return constrained.predicates();
        }
        return List.of();
    }

    public static String samplerMode(PickShape shape) {
        if (shape.n() > 1 && shape.distinct()) {
            return "distinct";
        }
        if (shape.n() > 1) {
            return "n";
        }
        return "single";
    }
}
