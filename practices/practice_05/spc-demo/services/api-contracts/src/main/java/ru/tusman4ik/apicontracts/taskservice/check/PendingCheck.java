package ru.tusman4ik.apicontracts.taskservice.check;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.DeliveryMode;

import java.util.Objects;

@Schema(description = "Deferred check")
public record PendingCheck(
        @Schema(description = "How often client should expect updates") DeliveryMode deliveryMode
) implements CheckResult {
    public PendingCheck {
        Objects.requireNonNull(deliveryMode, "deliveryMode must be not null");
    }
}
