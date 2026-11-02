package ru.tusman4ik.taskservice.external.coderunner;

import ru.tusman4ik.apicontracts.coderunner.callback.CaseResultDto;
import ru.tusman4ik.apicontracts.coderunner.callback.CompilerStatusCallbackDto;
import ru.tusman4ik.apicontracts.coderunner.callback.ErrorCallbackDto;
import ru.tusman4ik.apicontracts.coderunner.callback.PayloadCallbackDto;
import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;
import ru.tusman4ik.apicontracts.coderunner.callback.SubmissionCallbackDto;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.apicontracts.taskservice.check.CodeSubmissionFeedback;

import java.util.List;

public final class CodeFeedbackTable {

    private CodeFeedbackTable() {
    }

    public static List<CodeSubmissionFeedback.Row> rowsOf(RunnerCallback callback) {
        if (callback.payload() instanceof PayloadCallbackDto payload) {
            return payload.testResults().stream()
                    .map(CodeFeedbackTable::rowOf)
                    .toList();
        }
        return List.of();
    }

    public static String compilerLogOf(RunnerCallback callback) {
        if (callback.payload() instanceof CompilerStatusCallbackDto compilerStatus) {
            return compilerStatus.compilerLog();
        }
        return null;
    }

    public static String errorOf(RunnerCallback callback) {
        if (callback.payload() instanceof ErrorCallbackDto error) {
            return "stage=%s, case=%s, group=%s".formatted(error.stage(), error.caseId(), error.groupId());
        }
        return null;
    }

    public static boolean truncatedOf(RunnerCallback callback) {
        SubmissionCallbackDto payload = callback.payload();
        if (payload instanceof PayloadCallbackDto p) {
            return p.testResults().stream().anyMatch(CaseResultDto::truncated);
        }
        if (payload instanceof CompilerStatusCallbackDto c) {
            return c.truncated();
        }
        return false;
    }

    public static boolean compilationFailedOf(RunnerCallback callback) {
        return callback.payload() instanceof CompilerStatusCallbackDto compilerStatus
                && compilerStatus.status() != Status.OK;
    }

    private static CodeSubmissionFeedback.Row rowOf(CaseResultDto t) {
        return new CodeSubmissionFeedback.Row(
                t.caseId(),
                t.groupId(),
                t.status(),
                t.input(),
                t.stdout(),
                null,
                t.stderr() != null && !t.stderr().isBlank() ? t.stderr() : t.checkerMsg(),
                t.cpuTimeSec(),
                t.realTimeSec(),
                t.memoryKb(),
                t.exitCode()
        );
    }
}
