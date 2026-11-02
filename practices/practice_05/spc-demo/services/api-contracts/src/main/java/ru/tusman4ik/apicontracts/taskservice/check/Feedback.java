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
        @JsonSubTypes.Type(value = FictitiousFeedback.class, name = "fictitious-feedback"),
        @JsonSubTypes.Type(value = CodeSubmissionFeedback.class, name = "code-submission-feedback")
})
@Schema(description = "Polymorphic feedback")
public sealed interface Feedback permits FictitiousFeedback, CodeSubmissionFeedback {
}
