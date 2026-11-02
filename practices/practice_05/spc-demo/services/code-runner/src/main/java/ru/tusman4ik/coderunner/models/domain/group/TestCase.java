package ru.tusman4ik.coderunner.models.domain.group;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.checker.Checker;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.Feeder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.limits.Limits;

@Builder
public record TestCase(
        String id,

        Feeder feeder,
        Limits lim,
        Checker checker
) {
}
