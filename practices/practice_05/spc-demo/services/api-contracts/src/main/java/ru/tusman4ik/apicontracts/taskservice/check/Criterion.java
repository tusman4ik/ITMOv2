package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Grading criterion")
public record Criterion(
        @Schema(description = "Criterion name") String name,
        @Schema(description = "Maximum score") int maxScore,
        @Schema(description = "User score") int userScore,
        @Schema(description = "Criterion description") String description
) {
    public Criterion {
        Objects.requireNonNull(name, "name must be not null");
        Objects.requireNonNull(description, "description must be not null");
        if (userScore < 0 || userScore > maxScore) {
            throw new IllegalArgumentException(
                    "illegal criterion: maxScore=" + maxScore + ", userScore=" + userScore);
        }
    }
}
