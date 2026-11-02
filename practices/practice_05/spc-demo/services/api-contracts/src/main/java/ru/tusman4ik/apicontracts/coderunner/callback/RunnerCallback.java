package ru.tusman4ik.apicontracts.coderunner.callback;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.tusman4ik.apicontracts.taskservice.id.AttemptIdDto;

/**
 * Wire envelope for runner → task-service callbacks.
 *
 * <p>Carries the attempt identity alongside the polymorphic payload, so the
 * receiver can route callbacks from many attempts arriving at a single URL.
 * {@code seq}/{@code isLast} live on the payload; the envelope itself is
 * intentionally free of delivery semantics.
 */
public record RunnerCallback(
        @NotNull @Valid AttemptIdDto attempt,
        @NotNull SubmissionCallbackDto payload
) {
}
