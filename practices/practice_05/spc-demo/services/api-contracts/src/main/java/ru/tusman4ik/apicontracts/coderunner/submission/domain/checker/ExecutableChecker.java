package ru.tusman4ik.apicontracts.coderunner.submission.domain.checker;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.limits.Limits;

import java.util.List;

@Builder
public record ExecutableChecker(
        Limits lim,
        List<String> args,
        String internalName

) implements Checker {
}

