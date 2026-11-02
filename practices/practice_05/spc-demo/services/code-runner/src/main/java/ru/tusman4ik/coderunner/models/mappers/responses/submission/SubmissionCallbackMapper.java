package ru.tusman4ik.coderunner.models.mappers.responses.submission;

import lombok.AllArgsConstructor;
import ru.tusman4ik.apicontracts.coderunner.submission.OutputLimitsDto;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.ResponseField;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.coderunner.exceptions.StageException;
import ru.tusman4ik.coderunner.models.domain.results.CaseResult;
import ru.tusman4ik.apicontracts.coderunner.callback.CaseResultDto;
import ru.tusman4ik.apicontracts.coderunner.callback.CompilerStatusCallbackDto;
import ru.tusman4ik.apicontracts.coderunner.callback.ErrorCallbackDto;
import ru.tusman4ik.apicontracts.coderunner.callback.PayloadCallbackDto;
import ru.tusman4ik.coderunner.services.stages.build.BuildStageResult;

import java.util.EnumSet;
import java.util.List;

import static ru.tusman4ik.apicontracts.coderunner.submission.enums.ResponseField.*;

public class SubmissionCallbackMapper {

    public static CompilerStatusCallbackDto toDto(int seq, BuildStageResult result, OutputLimitsDto limits) {
        Truncated compilerLog = truncate(result.compilerLog(), limits.maxCompilerLogChars());
        return CompilerStatusCallbackDto.builder()
                .seq(seq)
                .isLast(result.status() != Status.OK)
                .status(result.status())
                .compilerLog(compilerLog.value())
                .truncated(compilerLog.truncated())
                .build();
    }

    public static ErrorCallbackDto toDto(int seq, StageException ex) {
        return ErrorCallbackDto.builder()
                .seq(seq)
                .isLast(true)
                .stage(ex.getStage())
                .caseId(ex.getTestId())
                .groupId(ex.getGroupId())
                .build();
    }

    public static PayloadCallbackDto toDto(int seq, boolean isLast, List<CaseResult> caseResults,
                                           EnumSet<ResponseField> responseFields, OutputLimitsDto limits) {
        return PayloadCallbackDto.builder()
                .seq(seq)
                .isLast(isLast)
                .testResults(caseResults.stream()
                        .map(t -> toDto(t, responseFields, limits))
                        .toList())
                .build();

    }

    public static CaseResultDto toDto(CaseResult t, EnumSet<ResponseField> responseFields, OutputLimitsDto limits) {
        @AllArgsConstructor
        class ResponseFieldFilter {
            private EnumSet<ResponseField> responseFields;
            <T> T get(ResponseField field, T t) {
                if (responseFields.contains(field)) {
                    return t;
                }
                return null;
            }
        }
        ResponseFieldFilter f = new ResponseFieldFilter(responseFields);

        Truncated checkerMsg = truncate(f.get(CHECKER_MSG, t.checkerMsg()), limits.maxCheckerMsgChars());
        Truncated stdout = truncate(f.get(STDOUT, t.stdout()), limits.maxStdoutChars());
        Truncated stderr = truncate(f.get(STDERR, t.stderr()), limits.maxStderrChars());
        Truncated input = truncate(f.get(INPUT, t.input()), limits.maxInputChars());

        return CaseResultDto.builder()
                .caseId(t.caseId())
                .groupId(t.groupId())
                .status(t.status())
                .cpuTimeSec(t.cpuTimeSec())
                .realTimeSec(t.realTimeSec())
                .memoryKb(t.memoryKb())
                .checkerMsg(checkerMsg.value())
                .stdout(stdout.value())
                .stderr(stderr.value())
                .input(input.value())
                .exitCode(t.exitCode())
                .truncated(checkerMsg.truncated() || stdout.truncated()
                        || stderr.truncated() || input.truncated())
                .build();
    }

    private record Truncated(String value, boolean truncated) {
    }

    private static Truncated truncate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return new Truncated(value, false);
        }
        return new Truncated(value.substring(0, maxChars), true);
    }


}
