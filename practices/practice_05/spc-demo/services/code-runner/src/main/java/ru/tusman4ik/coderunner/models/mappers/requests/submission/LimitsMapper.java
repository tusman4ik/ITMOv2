package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import ru.tusman4ik.apicontracts.coderunner.submission.LimitsDto;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.limits.Limits;
import ru.tusman4ik.coderunner.models.mappers.IContext;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.*;
import static ru.tusman4ik.coderunner.models.mappers.ValidationUtils.shouldBeInRange;

public final class LimitsMapper {
    private LimitsMapper() {}

    public static Limits fromDto(IContext ctx, LimitsDto dto) {
        if (dto == null) {
            return null;
        }

        shouldBeInRange(dto.cpuTimeLimitSec(),
                MIN_CPU_TIME_LIMIT_SEC,
                MAX_CPU_TIME_LIMIT_SEC,
                ctx);

        shouldBeInRange(dto.realTimeLimitSec(),
                MIN_REAL_TIME_LIMIT_SEC,
                MAX_REAL_TIME_LIMIT_SEC,
                ctx);

        shouldBeInRange(dto.processCnt(),
                MIN_PROCESS_CNT,
                MAX_PROCESS_CNT,
                ctx);

        shouldBeInRange(dto.fileSizeMb(),
                MIN_FILE_SIZE_MB,
                MAX_FILE_SIZE_MB,
                ctx);

        shouldBeInRange(dto.memoryLimitMb(),
                MIN_MEMORY_LIMIT_MB,
                MAX_MEMORY_LIMIT_MB,
                ctx);

        return Limits.builder()
                .cpuTimeLimitSec(dto.cpuTimeLimitSec())
                .realTimeLimitSec(dto.realTimeLimitSec())
                .memoryLimitKb(dto.memoryLimitMb() != null ? dto.memoryLimitMb() * 1024 : null)
                .fileSizeKb(dto.fileSizeMb() != null ? dto.fileSizeMb() * 1024 : null)
                .processCnt(dto.processCnt())
                .build();
    }
}
