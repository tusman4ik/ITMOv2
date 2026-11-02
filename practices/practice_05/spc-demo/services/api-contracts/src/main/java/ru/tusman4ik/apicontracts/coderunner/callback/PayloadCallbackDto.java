package ru.tusman4ik.apicontracts.coderunner.callback;

import lombok.Builder;

import java.util.List;

@Builder
public record PayloadCallbackDto(
        int seq,
        boolean isLast,
        List<CaseResultDto> testResults
) implements SubmissionCallbackDto {
    public static final String DISC = "payload";
}
