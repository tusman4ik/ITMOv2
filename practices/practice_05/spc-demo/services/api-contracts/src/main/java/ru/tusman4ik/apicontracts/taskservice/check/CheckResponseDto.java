package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

@Schema(description = "Check results")
public record CheckResponseDto(
        @Schema(description = "Per-item results") @NotNull List<@Valid CheckedItemDto> items
) {
    public CheckResponseDto {
        Objects.requireNonNull(items, "items must be not null");
        items = List.copyOf(items);
    }
}
