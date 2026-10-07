package ru.tusman4ik.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.task.registry.PrototypeId;

public record GenRequest(
        @Valid @NotNull PrototypeId id,
        Long seed
) {
}
