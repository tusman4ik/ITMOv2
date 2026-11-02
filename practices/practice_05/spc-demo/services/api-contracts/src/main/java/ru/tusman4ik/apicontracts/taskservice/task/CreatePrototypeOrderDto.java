package ru.tusman4ik.apicontracts.taskservice.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.tusman4ik.apicontracts.taskservice.id.PrototypeIdDto;

@Schema(description = "Prototype order: how many instances to generate")
public record CreatePrototypeOrderDto(
        @Schema(description = "Prototype identity") @NotNull @Valid PrototypeIdDto prototype,
        @Schema(description = "Number of instances to generate") @NotNull @Positive Integer count
) {
}
