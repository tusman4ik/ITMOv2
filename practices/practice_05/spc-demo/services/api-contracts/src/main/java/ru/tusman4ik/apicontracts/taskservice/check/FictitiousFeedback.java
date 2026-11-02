package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Test feedback with answers")
public record FictitiousFeedback(
        @Schema(description = "User answer") String userAnswer,
        @Schema(description = "Correct answer") String correctAnswer
) implements Feedback {
    public FictitiousFeedback {
        Objects.requireNonNull(userAnswer, "userAnswer must be not null");
        Objects.requireNonNull(correctAnswer, "correctAnswer must be not null");
    }
}
