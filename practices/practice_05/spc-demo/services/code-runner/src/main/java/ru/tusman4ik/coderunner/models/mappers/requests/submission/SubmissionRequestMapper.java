package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import ru.tusman4ik.apicontracts.coderunner.submission.TestGroupDto;
import ru.tusman4ik.coderunner.models.domain.SubmissionRequest;
import ru.tusman4ik.coderunner.models.domain.group.TestGroup;
import ru.tusman4ik.apicontracts.coderunner.common.exceptions.ValidationException;
import ru.tusman4ik.apicontracts.coderunner.submission.SubmissionRequestDto;

import java.util.List;

import ru.tusman4ik.apicontracts.coderunner.submission.OutputLimitsDto;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.MAX_PRIORITY;
import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.MIN_PRIORITY;
import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.MAX_OUTPUT_LIMIT_CHARS;
import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.MIN_OUTPUT_LIMIT_CHARS;
import static ru.tusman4ik.coderunner.Constants.MAX_CODE_SIZE_KB;
import static ru.tusman4ik.coderunner.models.mappers.ValidationUtils.*;

public final class SubmissionRequestMapper {
    private SubmissionRequestMapper() {}

    @NotNull
    public static SubmissionRequest fromDto(@NonNull SubmissionRequestDto dto) {
        Context ctx = new Context().goTo("submission");
        ctx.setSubmissionRequestDto(dto);

        shouldBeNotNull(dto.id(), ctx.goTo("id"));
        shouldBeValidUrlOrPath(dto.callbackUrl(), ctx.goTo("callbackUrl"));
        shouldBeInRange(dto.priority(), MIN_PRIORITY, MAX_PRIORITY, ctx.goTo("priority"));
        shouldNotBeEmpty(dto.groups(), ctx.goTo("groups"));
        shouldBeInRange(dto.src().length(), 0, MAX_CODE_SIZE_KB, ctx.goTo("src.size()"));
        validateOutputLimits(dto.outputLimits(), ctx.goTo("outputLimits"));

        OutputLimitsDto outputLimits = dto.outputLimits() == null
                ? OutputLimitsDto.DEFAULT
                : dto.outputLimits();

        var res = SubmissionRequest.builder()
                .id(dto.id())
                .callbackUrl(dto.callbackUrl())
                .callbackTrigger(dto.callbackTrigger())
                .deliveryMode(dto.deliveryMode())
                .snapshotPolicy(dto.snapshotPolicy())
                .priority(dto.priority())
                .responseFields(dto.responseFields())
                .outputLimits(outputLimits)
                .executionPolicy(dto.executionPolicy())
                .lang(dto.lang())
                .src(dto.src())
                .groups(mapGroups(ctx))
                .build();

        if (!ctx.getErrors().isEmpty()) {
            throw new ValidationException(ctx.getErrors());
        }

        return res;
    }

    private static @NotNull List<TestGroup> mapGroups(Context ctx) {
        SubmissionRequestDto dto = ctx.getSubmissionRequestDto();
        if (dto.groups() == null || dto.groups().isEmpty()) return List.of();

        return dto
                .groups()
                .stream()
                .map(tg -> mapGroup(ctx, tg))
                .toList();
    }

    private static TestGroup mapGroup(Context ctx, TestGroupDto tg) {
        ctx.setTestGroupDto(tg);
        String nextPart = String.format("group[%s]", tg.id());
        return TestGroupMapper.fromDto(ctx.goTo(nextPart));
    }

    private static void validateOutputLimits(OutputLimitsDto limits, Context ctx) {
        if (limits == null) {
            return;
        }
        shouldBeInRange(limits.maxStdoutChars(), MIN_OUTPUT_LIMIT_CHARS, MAX_OUTPUT_LIMIT_CHARS, ctx.goTo("maxStdoutChars"));
        shouldBeInRange(limits.maxStderrChars(), MIN_OUTPUT_LIMIT_CHARS, MAX_OUTPUT_LIMIT_CHARS, ctx.goTo("maxStderrChars"));
        shouldBeInRange(limits.maxInputChars(), MIN_OUTPUT_LIMIT_CHARS, MAX_OUTPUT_LIMIT_CHARS, ctx.goTo("maxInputChars"));
        shouldBeInRange(limits.maxCheckerMsgChars(), MIN_OUTPUT_LIMIT_CHARS, MAX_OUTPUT_LIMIT_CHARS, ctx.goTo("maxCheckerMsgChars"));
        shouldBeInRange(limits.maxCompilerLogChars(), MIN_OUTPUT_LIMIT_CHARS, MAX_OUTPUT_LIMIT_CHARS, ctx.goTo("maxCompilerLogChars"));
    }

}
