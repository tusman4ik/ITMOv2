package ru.tusman4ik.apicontracts.taskservice.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.apicontracts.taskservice.id.InstanceIdDto;
import ru.tusman4ik.apicontracts.taskservice.task.enums.RenderMode;

import java.util.List;

@Schema(description = "Request to recreate task instances keeping their identities")
public record RecreateRequestDto(
        @Schema(description = "Rendering mode") @NotNull RenderMode mode,
        @Schema(description = "Instances to recreate") @NotEmpty List<@Valid InstanceIdDto> items,
        @Schema(description = "Generator settings") @NotNull @Valid GeneratorSettings settings
) {
}
