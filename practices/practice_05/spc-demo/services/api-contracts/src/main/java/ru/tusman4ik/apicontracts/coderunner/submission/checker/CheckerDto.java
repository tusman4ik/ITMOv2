package ru.tusman4ik.apicontracts.coderunner.submission.checker;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExecutableCheckerDto.class, name = ExecutableCheckerDto.DISC),
        @JsonSubTypes.Type(value = TokenCheckerDto.class, name = TokenCheckerDto.DISC)
})
public sealed interface CheckerDto permits ExecutableCheckerDto, TokenCheckerDto {
}

