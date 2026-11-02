package ru.tusman4ik.coderunner.models.domain.group;

import lombok.Builder;

import java.util.List;

@Builder
public record TestGroup(
        String id,
        List<TestCase> tests
) {
}
