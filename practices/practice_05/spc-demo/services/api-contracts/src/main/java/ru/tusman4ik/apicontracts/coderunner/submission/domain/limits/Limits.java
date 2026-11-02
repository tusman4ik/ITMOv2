package ru.tusman4ik.apicontracts.coderunner.submission.domain.limits;

import lombok.Builder;

@Builder
public record Limits(
        Double cpuTimeLimitSec,
        Double realTimeLimitSec,
        Integer memoryLimitKb,
        Integer fileSizeKb,
        Integer processCnt
) {

}