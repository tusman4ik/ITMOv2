package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request to check answers")
public record CheckRequestDto(
        @Schema(description = "Answers to check") @NotEmpty List<@Valid CheckItemDto> items
) {
}
