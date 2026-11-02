package ru.tusman4ik.apicontracts.taskservice.check;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SuccessCheck.class, name = "success"),
        @JsonSubTypes.Type(value = FailedCheck.class, name = "failed"),
        @JsonSubTypes.Type(value = PendingCheck.class, name = "pending")
})
@Schema(description = "Polymorphic check result")
public sealed interface CheckResult permits SuccessCheck, FailedCheck, PendingCheck {
}
