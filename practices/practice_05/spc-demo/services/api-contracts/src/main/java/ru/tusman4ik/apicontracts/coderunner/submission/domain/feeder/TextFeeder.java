package ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Target;

@Builder
public record TextFeeder(
        String stdin,
        Target target
) implements Feeder {
}