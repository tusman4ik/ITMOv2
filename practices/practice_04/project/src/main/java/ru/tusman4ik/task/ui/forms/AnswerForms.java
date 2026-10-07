package ru.tusman4ik.task.ui.forms;

import ru.tusman4ik.task.templates.FormFactory;

public final class AnswerForms {

    private AnswerForms() {
    }

    public static FormFactory<Integer> intForm() {
        return ctx -> IntForm.INSTANCE;
    }

    public static FormFactory<Double> doubleForm() {
        return ctx -> DoubleForm.INSTANCE;
    }

    public static FormFactory<Boolean> boolForm() {
        return ctx -> BoolForm.INSTANCE;
    }

    public static FormFactory<String> stringForm() {
        return ctx -> StringForm.INSTANCE;
    }
}
