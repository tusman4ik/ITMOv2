package ru.tusman4ik.task.morphy;

public class MissingWordException extends IllegalArgumentException {

    public MissingWordException(String normalForm, String allowedLocation) {
        super("Word '%s' is not in %s: curate it with scripts/curate-morphy.py".formatted(normalForm, allowedLocation));
    }
}
