package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.apicontracts.taskservice.id.AttemptIdDto;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;

@Schema(description = "Single answer to check")
public record CheckItemDto(
        @Schema(description = "Attempt identity") @NotNull @Valid AttemptIdDto attempt,
        @Schema(description = "Parsed user answer, structure depends on task type") @NotNull @Valid InputSchema answer
) {
}
