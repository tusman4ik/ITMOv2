package ru.tusman4ik.apicontracts.coderunner.submission.feeder;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Target;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.or;

@Builder
public record TextFeederDto(
        String stdin,
        Target target
) implements FeederDto {
    public static final String DISC = "text";

    public static final TextFeederDto EMPTY = TextFeederDto.builder().build();

    private static final String DEFAULT_STDIN = "";

    public TextFeederDto {
        stdin = or(stdin, DEFAULT_STDIN);
    }
}