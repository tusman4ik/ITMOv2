package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import org.junit.jupiter.api.Test;
import ru.tusman4ik.apicontracts.coderunner.common.exceptions.ValidationException;
import ru.tusman4ik.apicontracts.coderunner.submission.*;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.CheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.TokenCheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.ExecutionPolicy;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Language;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.FeederDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.TextFeederDto;
import ru.tusman4ik.apicontracts.taskservice.id.AttemptIdDto;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;
import ru.tusman4ik.coderunner.models.domain.SubmissionRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SubmissionRequestMapperTest {

    private static final SubmissionRequestDto SUBMISSION_WITH_META = SubmissionRequestDto.builder()
            .id(new AttemptIdDto(Exam.OGE, Subject.CS, 1, 1, 1L, 1L, UUID.randomUUID()))
            .callbackUrl("https://bla-bla-bla.ru")
            .lang(Language.PYTHON)
            .executionPolicy(ExecutionPolicy.RUN_ALL)
            .src("# empty script")
            .priority(1)
            .build();

    private static final Map<String, LimitsDto> MAX_LIMITS_MAP = Map.of("l-1", LimitsDto.MAX_LIM);

    private static final Map<String, FeederDto> TEXT_FEEDER_MAP = Map.of(
            "f-1",
            TextFeederDto.builder().stdin("in").build()
    );

    private static final Map<String, CheckerDto> TOKEN_CHECKER_MAP = Map.of(
            "c-1",
            TokenCheckerDto.builder().build()
    );

    @Test
    void minimalViableExample() {
        var dto = SUBMISSION_WITH_META.toBuilder()
                .limits(MAX_LIMITS_MAP)
                .feeders(TEXT_FEEDER_MAP)
                .checkers(TOKEN_CHECKER_MAP)
                .groups(List.of(
                        TestGroupDto.builder()
                                .id("some-id")
                                .tests(List.of(
                                        TestCaseDto.builder()
                                                .id("some-id")
                                                .payload(
                                                        PayloadDto.builder()
                                                                .feeder("f-1")
                                                                .lim("l-1")
                                                                .checker("c-1")
                                                                .build()
                                                )
                                                .build()
                                ))
                                .build()
                )).build();

        SubmissionRequest submissionRequest = SubmissionRequestMapper.fromDto(dto);

    }

    @Test
    void shouldPropagatePayloadFromSubmissionToTestCases() {
        var dto = SUBMISSION_WITH_META.toBuilder()
                .limits(MAX_LIMITS_MAP)
                .feeders(TEXT_FEEDER_MAP)
                .checkers(TOKEN_CHECKER_MAP)
                .payload(
                        PayloadDto.builder()
                                .feeder("f-1")
                                .lim("l-1")
                                .checker("c-1")
                                .build()
                )
                .groups(List.of(
                        TestGroupDto.builder()
                                .id("g-1")
                                .tests(List.of(
                                        TestCaseDto.builder()
                                                .id("tc-1")
                                                .build()
                                ))
                                .build()
                )).build();

        SubmissionRequest submissionRequest = SubmissionRequestMapper.fromDto(dto);

        var testCase = submissionRequest.groups().getFirst().tests().getFirst();
        assertThat(testCase.feeder()).isNotNull();
        assertThat(testCase.lim()).isNotNull();
        assertThat(testCase.checker()).isNotNull();
    }

    @Test
    void shouldThrowValidationExceptionWhenLimitsAreOutOfRange() {
        var invalidLimitsMap = Map.of(
                "l-invalid",
                LimitsDto.builder()
                        .cpuTimeLimitSec(0.1) // Under MIN_CPU_TIME_LIMIT_SEC (0.25)
                        .realTimeLimitSec(2.0)
                        .memoryLimitMb(1024)  // Over MAX_MEMORY_LIMIT_MB (512)
                        .fileSizeMb(10)
                        .processCnt(1)
                        .build()
        );

        var dto = SUBMISSION_WITH_META.toBuilder()
                .limits(invalidLimitsMap)
                .feeders(TEXT_FEEDER_MAP)
                .checkers(TOKEN_CHECKER_MAP)
                .groups(List.of(
                        TestGroupDto.builder()
                                .id("g-1")
                                .tests(List.of(
                                        TestCaseDto.builder()
                                                .id("tc-1")
                                                .payload(
                                                        PayloadDto.builder()
                                                                .feeder("f-1")
                                                                .lim("l-invalid")
                                                                .checker("c-1")
                                                                .build()
                                                )
                                                .build()
                                ))
                                .build()
                )).build();

        assertThatThrownBy(() -> SubmissionRequestMapper.fromDto(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation error");
    }
}