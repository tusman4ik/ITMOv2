package ru.tusman4ik.controllers;

import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.ui.forms.FormDescriptor;

import java.util.Map;

public record GenTaskResponse(
        PrototypeId id,
        long seed,
        String statement,
        Map<String, Object> data,
        FormDescriptor inputForm
) {
}
