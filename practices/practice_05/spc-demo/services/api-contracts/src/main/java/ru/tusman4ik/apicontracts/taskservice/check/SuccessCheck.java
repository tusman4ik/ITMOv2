package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;
import ru.tusman4ik.apicontracts.taskservice.schema.IntegerSchema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Successful check with scores")
public record SuccessCheck(
        @Schema(description = "Score totals") ScoresDto scores,
        @Schema(description = "Per-criterion breakdown") List<Criterion> criteria,
        @Schema(description = "Polymorphic feedback") Feedback feedback,
        @Schema(description = "Parsed user answer (structure depends on task type)") InputSchema answer,
        @Schema(description = "Reference (correct) answer") ReferenceDto reference
) implements CheckResult {
    public SuccessCheck {
        Objects.requireNonNull(scores, "scores must be not null");
        Objects.requireNonNull(criteria, "criteria must be not null");
        criteria = List.copyOf(criteria);
        Objects.requireNonNull(feedback, "feedback must be not null");
        Objects.requireNonNull(answer, "answer must be not null");
        Objects.requireNonNull(reference, "reference must be not null");
    }
}