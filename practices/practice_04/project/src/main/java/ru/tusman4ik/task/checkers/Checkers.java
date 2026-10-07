package ru.tusman4ik.task.checkers;

import ru.tusman4ik.task.nodes.Node;
import ru.tusman4ik.task.templates.CheckResult;
import ru.tusman4ik.task.templates.CheckerFactory;
import ru.tusman4ik.task.templates.Criterion;

import java.util.Map;
import java.util.function.Supplier;

public final class Checkers {

    private Checkers() {
    }

    public static CheckerFactory<Integer> exactInt(Supplier<Node<Integer>> expected) {
        return ctx -> given -> {
            Node<Integer> node = expected.get();
            Integer exp = node == null ? null : ctx.get(node);
            int ok = (given != null && given.equals(exp)) ? 1 : 0;
            return new CheckResult(Map.of("single-criterion", new Criterion(1, ok)), ok);
        };
    }

    public static CheckerFactory<String> trimmedString(Supplier<Node<String>> expected) {
        return ctx -> given -> {
            Node<String> node = expected.get();
            String exp = node == null ? null : ctx.get(node);
            int ok = (given != null && exp != null && given.trim().equals(exp.trim())) ? 1 : 0;
            return new CheckResult(Map.of("single-criterion", new Criterion(1, ok)), ok);
        };
    }
}
