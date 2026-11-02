package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Score totals")
public record ScoresDto(
        @Schema(description = "Maximum score") int max,
        @Schema(description = "Earned score") int earned
) {
    public ScoresDto {
        if (max < 0) {
            throw new IllegalArgumentException("max must be >= 0, got: " + max);
        }
        if (earned < 0 || earned > max) {
            throw new IllegalArgumentException(
                    "earned must be between 0 and max=" + max + ", got: " + earned);
        }
    }
}
