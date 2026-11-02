package ru.tusman4ik.coderunner.services.stages.check;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.checker.TokenChecker;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;
import ru.tusman4ik.coderunner.services.workspace.artifacts.FileArtifact;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TokenCheckStage implements StageRunner.StageCallable<CheckStageResult>{

    private final TokenChecker checker;
    private final Context ctx;

    @Override
    public CheckStageResult call() throws Exception {
        FileArtifact stdout = ctx.getWorkspace().findOne(Mark.STDOUT, FileArtifact.class);
        FileArtifact expected = ctx.getWorkspace().findOne(Mark.EXP, FileArtifact.class);

        try {
            String stdoutContent = Files.readString(stdout.path());
            String expectedContent = Files.readString(expected.path());

            log.info("exp = {}", expectedContent);
            log.info("out = {}", stdoutContent);

            List<String> stdoutTokens = extractTokens(stdoutContent);
            List<String> expectedTokens = extractTokens(expectedContent);

            if (stdoutTokens.size() != expectedTokens.size()) {
                String errorMsg = String.format(
                        "Token count mismatch. Expected: %d, actual: %d",
                        expectedTokens.size(), stdoutTokens.size()
                );
                log.warn(errorMsg);
                return new CheckStageResult(Status.WA, errorMsg);
            }

            for (int i = 0; i < expectedTokens.size(); i++) {
                String actual = stdoutTokens.get(i);
                String exp = expectedTokens.get(i);

                if (!areTokensEqual(actual, exp)) {
                    String errorMsg = String.format(
                            "Token mismatch at index %d: expected '%s', actual '%s'",
                            i, exp, actual
                    );
                    log.warn(errorMsg);
                    return new CheckStageResult(Status.WA, errorMsg);
                }
            }

            log.info("Token check completed successfully.");
            return new CheckStageResult(Status.OK, "All tokens matched successfully.");

        } catch (IOException e) {
            log.error("Failed to read artifact files", e);
            throw e;
        }
    }

    private List<String> extractTokens(String content) {
        String regex = (checker.regexp() != null && !checker.regexp().isBlank())
                ? checker.regexp()
                : "\\S+";

        return Arrays.stream(content.trim().split(regex))
                .filter(token -> !token.isEmpty())
                .toList();
    }

    private boolean areTokensEqual(String actual, String expected) {
        if (checker.numericTolerance() != null) {
            Double actualNum = parseDouble(actual);
            Double expectedNum = parseDouble(expected);

            if (actualNum != null && expectedNum != null) {
                return Math.abs(actualNum - expectedNum) <= checker.numericTolerance();
            }
        }

        boolean isCaseSensitive = Boolean.TRUE.equals(checker.caseSensitive());
        if (isCaseSensitive) {
            return actual.equals(expected);
        } else {
            return actual.equalsIgnoreCase(expected);
        }
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}