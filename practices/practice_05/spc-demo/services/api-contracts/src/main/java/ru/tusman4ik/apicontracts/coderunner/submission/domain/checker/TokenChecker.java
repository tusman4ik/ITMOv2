package ru.tusman4ik.apicontracts.coderunner.submission.domain.checker;

import lombok.Builder;

@Builder
public record TokenChecker(
    Boolean caseSensitive,
    Double numericTolerance,
    String regexp
) implements Checker {
}
