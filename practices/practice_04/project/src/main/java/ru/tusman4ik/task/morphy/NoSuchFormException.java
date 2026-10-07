package ru.tusman4ik.task.morphy;

import java.util.List;

public class NoSuchFormException extends IllegalArgumentException {

    public NoSuchFormException(String normalForm, List<String> tags) {
        super("No form of '%s' matches tags %s".formatted(normalForm, tags));
    }
}
