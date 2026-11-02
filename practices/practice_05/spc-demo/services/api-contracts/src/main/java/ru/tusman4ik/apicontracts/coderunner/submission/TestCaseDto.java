package ru.tusman4ik.apicontracts.coderunner.submission;

import lombok.Builder;

@Builder(toBuilder = true)
public record TestCaseDto(
        String id,
        PayloadDto payload
) {
}
