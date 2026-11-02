package ru.tusman4ik.apicontracts.coderunner.submission;

import lombok.Builder;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.*;
import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.or;

@Builder
public record LimitsDto(
        Double cpuTimeLimitSec,
        Double realTimeLimitSec,
        Integer memoryLimitMb,
        Integer fileSizeMb,
        Integer processCnt
) {

    private static final int DEFAULT_MEMORY_LIMIT_MB = 128;
    private static final int DEFAULT_FILE_SIZE_MB = 0;
    private static final int DEFAULT_PROCESS_CNT = 1;


    public static final LimitsDto MAX_LIM = LimitsDto.builder()
            .cpuTimeLimitSec(MAX_CPU_TIME_LIMIT_SEC)
            .realTimeLimitSec(MAX_REAL_TIME_LIMIT_SEC)
            .memoryLimitMb(MAX_MEMORY_LIMIT_MB)
            .fileSizeMb(MAX_FILE_SIZE_MB)
            .processCnt(MAX_PROCESS_CNT)
            .build();

    public static final LimitsDto MIN_LIM = LimitsDto.builder()
            .cpuTimeLimitSec(MIN_CPU_TIME_LIMIT_SEC)
            .realTimeLimitSec(MIN_REAL_TIME_LIMIT_SEC)
            .memoryLimitMb(MIN_MEMORY_LIMIT_MB)
            .fileSizeMb(MIN_FILE_SIZE_MB)
            .processCnt(MIN_PROCESS_CNT)
            .build();


    public LimitsDto {
        memoryLimitMb = or(memoryLimitMb, DEFAULT_MEMORY_LIMIT_MB);
        fileSizeMb = or(fileSizeMb, DEFAULT_FILE_SIZE_MB);
        processCnt = or(processCnt, DEFAULT_PROCESS_CNT);
    }
}