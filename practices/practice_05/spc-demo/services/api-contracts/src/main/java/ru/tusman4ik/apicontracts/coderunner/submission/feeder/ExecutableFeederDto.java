package ru.tusman4ik.apicontracts.coderunner.submission.feeder;

import lombok.Builder;

import ru.tusman4ik.apicontracts.coderunner.submission.LimitsDto;

import java.util.List;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.or;

@Builder
public record ExecutableFeederDto(
        LimitsDto lim,
        List<String> args,
        List<String> additionalArgs,
        String internalName
) implements FeederDto {
    public static final String DISC = "exec";

    private static final LimitsDto DEFAULT_LIMITS = LimitsDto.MAX_LIM;
    private static final List<String> DEFAULTS_ARGS = List.of();
    private static final List<String> DEFAULTS_ADDITIONAL_ARGS = List.of();

    public ExecutableFeederDto {
        lim = or(lim, DEFAULT_LIMITS);
        args = or(args, DEFAULTS_ARGS);
        additionalArgs = or(additionalArgs, DEFAULTS_ADDITIONAL_ARGS);
    }
}


