package ru.tusman4ik.apicontracts.coderunner.submission;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record TestGroupDto(
        String id,
        PayloadDto payload,
        List<TestCaseDto> tests
) {
}