package ru.tusman4ik.taskservice.taskimpl.t16;

import org.springframework.beans.factory.annotation.Value;
import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.apicontracts.coderunner.submission.*;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.TokenCheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.DeliveryMode;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Language;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Target;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.CompositeFeederDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.FeederDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.TextFeederDto;
import ru.tusman4ik.apicontracts.taskservice.api.RunnerCallbackApi;
import ru.tusman4ik.apicontracts.taskservice.check.CheckResult;
import ru.tusman4ik.apicontracts.taskservice.check.CodeSubmissionFeedback;
import ru.tusman4ik.apicontracts.taskservice.check.Criterion;
import ru.tusman4ik.apicontracts.taskservice.check.ReferenceDto;
import ru.tusman4ik.apicontracts.taskservice.check.ScoresDto;
import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.apicontracts.taskservice.check.enums.ReferenceKind;
import ru.tusman4ik.apicontracts.taskservice.check.PendingCheck;
import ru.tusman4ik.apicontracts.taskservice.form.CodeInputForm;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;
import ru.tusman4ik.apicontracts.taskservice.schema.TextInputSchema;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;
import ru.tusman4ik.taskservice.external.coderunner.CodeFeedbackTable;
import ru.tusman4ik.taskservice.external.coderunner.CodeRunnerClient;
import ru.tusman4ik.taskservice.task.generator.CodePendingChecker;
import ru.tusman4ik.taskservice.task.generator.PrototypeGenerator;
import ru.tusman4ik.taskservice.task.generator.Register;
import ru.tusman4ik.taskservice.task.id.AttemptId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.taskservice.task.node.implementations.NodeDef;
import ru.tusman4ik.taskservice.taskimpl.t16.common.TestCase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Register(
        prototypeIdx = 999,
        exam = Exam.OGE,
        subject = Subject.CS,
        number = 16
)
public class Generator_16_999 extends PrototypeGenerator<TextInputSchema>
        implements CodePendingChecker {

    private final CodeRunnerClient codeRunnerClient;
    private final String callbacksBaseUrl;

    public Generator_16_999(CodeRunnerClient codeRunnerClient,
                            @Value("${task-service.public-base-url:http://localhost:8080}") String callbacksBaseUrl) {
        this.codeRunnerClient = codeRunnerClient;
        this.callbacksBaseUrl = callbacksBaseUrl;
    }

    @Override
    public InputForm<? extends TextInputSchema> form() {
        // todo(tus): change to the settings value
        return new CodeInputForm(CodeInputForm.Language.PYTHON);
    }

    @Override
    public CheckResult check(AttemptId id, TextInputSchema schema) {
        List<TestCase> tests = this.tests.get();

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

    @Override
    public SuccessCheck checkCode(AttemptId id, RunnerCallback callback) {
        List<CodeSubmissionFeedback.Row> bare = CodeFeedbackTable.rowsOf(callback);

        Map<String, String> expectedByCase = new LinkedHashMap<>();
        List<TestCase> tests = this.tests.get();
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
        int earned = broken || failed > 1 ? 0 : failed == 1 ? 1 : 2;

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

    private static String caseId(int index) {
        return "case-" + index;
    }

    private static String feederId(int index) {
        return "f-" + index;
    }

    /** Maximal value divisible by k, or null when none of the numbers qualifies. */
    private static Long maxMultiple(long[] numbers, int k) {
        Long best = null;
        for (long n : numbers) {
            if (n % k == 0 && (best == null || n > best)) {
                best = n;
            }
        }
        return best;
    }

    private static String in(long... numbers) {
        StringBuilder sb = new StringBuilder().append(numbers.length).append('\n');
        for (int i = 0; i < numbers.length; i++) {
            sb.append(numbers[i]);
            if (i != numbers.length - 1) {
                sb.append(' ');
            }
        }
        return sb.append('\n').toString();
    }

    private static String exp(long answer) {
        return "YES\n" + answer + "\n";
    }

    private static final String NO = "NO\n";

    @NodeDef
    private Node<Integer> k = () -> 2 + ctx().random().nextInt(49); // 2..50

    @NodeDef
    private Node<List<TestCase>> tests = () -> {
        int k = this.k.get();
        List<TestCase> cases = new ArrayList<>();
        cases.add(new TestCase(in(2 * k, k + 1, 3 * k, k + 7), exp(3 * k)));
        cases.add(new TestCase(in(k + 1), NO));

        // manual corner cases
        cases.add(new TestCase(in(k, k, k), exp(k)));                                        // equal maximal multiples
        cases.add(new TestCase(in(k * 100, k * 50), exp(k * 100L)));                          // one, another
        cases.add(new TestCase(in(k + 1, k + 2, 2 * k + 1, k + 4, k + 9), NO));               // none divisible
        long nearTop = 999_999_999 - (999_999_999 % k);                                      // largest < 1e9 divisible by k
        cases.add(new TestCase(in(nearTop), exp(nearTop)));
        cases.add(new TestCase(in(nearTop, nearTop / k), exp(nearTop)));                      // max vs smaller multiple

        // random cases: alternating with/without an answer, a couple of wide ones
        for (int i = 0; i < 23; i++) {
            boolean hasAnswer = i % 2 == 0;
            boolean wide = i % 7 == 3;
            int n = wide ? 1000 : 3 + ctx().random().nextInt(38);
            long[] numbers = new long[n];
            for (int j = 0; j < n; j++) {
                long value = 1 + ctx().random().nextLong(1_000_000_000L);
                if (!hasAnswer && value % k == 0) {
                    value += 1;
                }
                numbers[j] = value;
            }
            if (hasAnswer) {
                numbers[ctx().random().nextInt(n)] = (1 + ctx().random().nextLong(1_000_000_000L / k)) * k;
            }
            Long answer = maxMultiple(numbers, k);
            cases.add(new TestCase(in(numbers), answer == null ? NO : exp(answer)));
        }
        return cases;
    };

    @NodeDef
    private Node<String>
            firstExampleIn = () -> tests.get().getFirst().in(),
            firstExampleExp = () -> tests.get().getFirst().expected(),
            secondExampleIn = () -> tests.get().get(1).in(),
            secondExampleExp = () -> tests.get().get(1).expected();
}
