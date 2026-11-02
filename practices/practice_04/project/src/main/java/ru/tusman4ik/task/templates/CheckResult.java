package ru.tusman4ik.task.templates;

import java.util.Map;

public record CheckResult(Map<String, Criterion> criteria, int total) {

    public CheckResult {
        criteria = Map.copyOf(criteria);
    }
}
