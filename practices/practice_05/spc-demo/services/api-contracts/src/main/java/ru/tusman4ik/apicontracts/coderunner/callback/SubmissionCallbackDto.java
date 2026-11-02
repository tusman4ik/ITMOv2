package ru.tusman4ik.apicontracts.coderunner.callback;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PayloadCallbackDto.class, name = PayloadCallbackDto.DISC),
        @JsonSubTypes.Type(value = ErrorCallbackDto.class, name = ErrorCallbackDto.DISC),
        @JsonSubTypes.Type(value = CompilerStatusCallbackDto.class, name = CompilerStatusCallbackDto.DISC),
})
public sealed interface SubmissionCallbackDto permits PayloadCallbackDto, ErrorCallbackDto, CompilerStatusCallbackDto {

    int seq();

    boolean isLast();
}
