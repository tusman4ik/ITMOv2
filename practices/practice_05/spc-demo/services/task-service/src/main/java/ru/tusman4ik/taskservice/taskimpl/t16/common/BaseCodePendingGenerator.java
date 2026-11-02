package ru.tusman4ik.taskservice.taskimpl.t16.common;

import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.apicontracts.coderunner.submission.*;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.TokenCheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.DeliveryMode;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.ExecutionPolicy;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Language;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Target;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.CompositeFeederDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.FeederDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.TextFeederDto;
import ru.tusman4ik.apicontracts.taskservice.api.RunnerCallbackApi;
import ru.tusman4ik.apicontracts.taskservice.check.*;
import ru.tusman4ik.apicontracts.taskservice.check.enums.ReferenceKind;
import ru.tusman4ik.apicontracts.taskservice.form.CodeInputForm;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;
import ru.tusman4ik.apicontracts.taskservice.schema.TextInputSchema;
import ru.tusman4ik.taskservice.external.coderunner.CodeFeedbackTable;
import ru.tusman4ik.taskservice.external.coderunner.CodeRunnerClient;
import ru.tusman4ik.taskservice.task.generator.CodePendingChecker;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.id.AttemptId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCodePendingGenerator extends PrototypeGenerator<TextInputSchema>
        implements CodePendingChecker {

    protected final CodeRunnerClient codeRunnerClient;
    protected final String callbacksBaseUrl;

    public BaseCodePendingGenerator(CodeRunnerClient codeRunnerClient, String callbacksBaseUrl) {
        this.codeRunnerClient = codeRunnerClient;
        this.callbacksBaseUrl = callbacksBaseUrl;
    }

    @Override
    public InputForm<? extends TextInputSchema> form() {
        return new CodeInputForm(CodeInputForm.Language.PYTHON);
    }

    protected CheckResult check(AttemptId id, TextInputSchema schema, List<TestCase> tests) {
        Map<String, FeederDto> feeders = new LinkedHashMap<>();
        List<TestCaseDto> cases = new ArrayList<>(tests.size());
        for (int i = 0; i < tests.size(); i++) {
            TestCase test = tests.get(i);
            feeders.put(feederId(i), CompositeFeederDto.builder().feeders(List.of(
                    TextFeederDto.builder().stdin(test.expected()).target(Target.EXP).build(),
                    TextFeederDto.builder().stdin(test.in()).target(Target.STDIN).build()
            )).build());
            cases.add(TestCaseDto.builder()
                    .id(caseId(i))
                    .payload(PayloadDto.builder()
                            .feeder(feederId(i))
                            .lim("def")
                            .checker("tok")
                            .build())
                    .build());
        }

        SubmissionRequestDto request = SubmissionRequestDto.builder()
                .id(id.toDto())
                .callbackUrl(callbacksBaseUrl + "/" + RunnerCallbackApi.BASE_PATH + "/callbacks")
                .lang(Language.PYTHON)
                .src(schema.text())
                .executionPolicy(ExecutionPolicy.STOP_ON_FIRST_FAILURE)
                .feeders(feeders)
                .checkers(Map.of("tok", TokenCheckerDto.builder().build()))
                .limits(Map.of("def", LimitsDto.builder()
                        .cpuTimeLimitSec(1.0)
                        .realTimeLimitSec(5.0)
                        .memoryLimitMb(128)
                        .fileSizeMb(16)
                        .processCnt(1)
                        .build()))
                .groups(List.of(TestGroupDto.builder()
                        .id("g-1")
                        .tests(cases)
                        .build()))
                .build();

        codeRunnerClient.submit(request);
        return new PendingCheck(DeliveryMode.BATCHED_MS_1000);
    }

    protected SuccessCheck checkCode(AttemptId id, RunnerCallback callback, List<TestCase> tests) {
        List<CodeSubmissionFeedback.Row> bare = CodeFeedbackTable.rowsOf(callback);

        Map<String, String> expectedByCase = new LinkedHashMap<>();
        for (int i = 0; i < tests.size(); i++) {
            expectedByCase.put(caseId(i), tests.get(i).expected());
        }

        List<CodeSubmissionFeedback.Row> rows = new ArrayList<>(bare.size());
        for (CodeSubmissionFeedback.Row row : bare) {
            rows.add(CodeSubmissionFeedback.Row.builder()
                    .caseId(row.caseId())
                    .groupId(row.groupId())
                    .status(row.status())
                    .input(row.input())
                    .stdout(row.stdout())
                    .expected(expectedByCase.get(row.caseId()))
                    .checkerMsg(row.checkerMsg())
                    .cpuTimeSec(row.cpuTimeSec())
                    .realTimeSec(row.realTimeSec())
                    .memoryKb(row.memoryKb())
                    .exitCode(row.exitCode())
                    .build());
        }

        long failed = rows.stream().filter(row -> row.status() != Status.OK).count();
        boolean broken = CodeFeedbackTable.errorOf(callback) != null
                || CodeFeedbackTable.compilationFailedOf(callback);
        // В fail-fast режиме падение даже одного теста или рантайм-ошибка означает 0 баллов
        int earned = broken || failed > 0 ? 0 : 2;

        CodeSubmissionFeedback feedback = CodeSubmissionFeedback.builder()
                .rows(rows)
                .passed((int) rows.stream().filter(row -> row.status() == Status.OK).count())
                .total(rows.size())
                .compilerLog(CodeFeedbackTable.compilerLogOf(callback))
                .truncated(CodeFeedbackTable.truncatedOf(callback))
                .error(CodeFeedbackTable.errorOf(callback))
                .build();

        return new SuccessCheck(
                new ScoresDto(2, earned),
                List.of(new Criterion("main", 2, earned, "Main functional criterion")),
                feedback,
                new TextInputSchema(""),
                new ReferenceDto(null, ReferenceKind.NONE));
    }

    public static String caseId(int index) {
        return "case-" + index;
    }

    public static String feederId(int index) {
        return "f-" + index;
    }
}
