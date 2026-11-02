package ru.tusman4ik.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.task.registry.PrototypeId;
import tools.jackson.databind.JsonNode;

public record CheckRequest(
        @Valid @NotNull PrototypeId id,
        @NotNull Long seed,
        @NotNull JsonNode answer
) {
}
