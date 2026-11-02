package ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Target;

@Builder
public record FileFeeder(
        String path,
        Target target
) implements Feeder {
}