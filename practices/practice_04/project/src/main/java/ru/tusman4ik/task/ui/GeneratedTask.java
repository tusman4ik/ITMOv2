package ru.tusman4ik.task.ui;

import ru.tusman4ik.task.ui.forms.FormDescriptor;

import java.util.Map;

public record GeneratedTask(
        String statement,
        Map<String, Object> data,
        FormDescriptor inputForm
) {

    public GeneratedTask {
        data = Map.copyOf(data);
    }
}
