package ru.tusman4ik.task.resources.pick;

import ru.tusman4ik.task.nodes.DataSourceSelectionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public final class PickSampler {

    private PickSampler() {}

    public static <T> T pick(Random random, List<T> data, List<Predicate<T>> predicates) {
        List<T> filtered = applyPredicates(data, predicates);
        if (filtered.isEmpty()) {
            throw new DataSourceSelectionException(
                    "No elements satisfy predicates: requested 1, available " + data.size()
                            + ", after filter 0, predicates=" + predicates.size());
        }
        return filtered.get(random.nextInt(filtered.size()));
    }

    public static <T> List<T> pickN(Random random, List<T> data, int n, List<Predicate<T>> predicates) {
        List<T> filtered = applyPredicates(data, predicates);
        if (n > filtered.size()) {
            throw new DataSourceSelectionException(
                    "Not enough elements: requested " + n + ", available " + filtered.size()
                            + " (source " + data.size() + "), predicates=" + predicates.size());
        }
        List<T> result = new ArrayList<>(filtered);
        Collections.shuffle(result, random);
        return List.copyOf(result.subList(0, n));
    }

    public static <T> List<T> pickDistinctN(Random random, List<T> data, int n, List<Predicate<T>> predicates) {
        List<T> distinct = applyPredicates(data, predicates).stream().distinct().toList();
        return pickN(random, distinct, n, List.of());
    }

    public static <T> List<T> applyPredicates(List<T> data, List<Predicate<T>> predicates) {
        if (predicates.isEmpty()) {
            return data;
        }
        return data.stream()
                .filter(row -> predicates.stream().allMatch(predicate -> predicate.test(row)))
                .toList();
    }
}
