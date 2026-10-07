package ru.tusman4ik.task.morphy;

import java.util.List;

public class AmbiguousMeaningException extends IllegalArgumentException {

    public AmbiguousMeaningException(String normalForm, List<Long> meaningIds, java.util.Set<String> candidates) {
        super("Ambiguous forms for '%s': %s (meanings %s). Resolve via scripts/curate-morphy.py".formatted(normalForm, candidates, meaningIds));
    }
}
