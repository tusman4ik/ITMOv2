package ru.tusman4ik.apicontracts.coderunner.submission.feeder;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Target;

@Builder
public record FileFeederDto(
        String path,
        Target target
) implements FeederDto {
    public static final String DISC = "file";
}