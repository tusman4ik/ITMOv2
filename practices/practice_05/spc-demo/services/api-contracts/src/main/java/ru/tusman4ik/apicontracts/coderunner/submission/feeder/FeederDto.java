package ru.tusman4ik.apicontracts.coderunner.submission.feeder;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CompositeFeederDto.class, name = CompositeFeederDto.DISC),
    @JsonSubTypes.Type(value = TextFeederDto.class, name = TextFeederDto.DISC),
    @JsonSubTypes.Type(value = FileFeederDto.class, name = FileFeederDto.DISC),
    @JsonSubTypes.Type(value = ExecutableFeederDto.class, name = ExecutableFeederDto.DISC)
})
public sealed interface FeederDto permits CompositeFeederDto, TextFeederDto, FileFeederDto, ExecutableFeederDto {}
