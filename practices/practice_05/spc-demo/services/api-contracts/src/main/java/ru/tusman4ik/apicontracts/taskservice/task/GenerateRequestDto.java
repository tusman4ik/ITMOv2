package ru.tusman4ik.apicontracts.taskservice.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.apicontracts.taskservice.task.enums.RenderMode;

import java.util.List;

@Schema(description = "Request to generate new task instances")
public record GenerateRequestDto(
        @Schema(description = "Rendering mode") @NotNull RenderMode mode,
        @Schema(description = "Ordered prototypes with instance counts") @NotEmpty List<@Valid CreatePrototypeOrderDto> items,
        @Schema(description = "Generator settings") @NotNull @Valid GeneratorSettings settings
) {
}
