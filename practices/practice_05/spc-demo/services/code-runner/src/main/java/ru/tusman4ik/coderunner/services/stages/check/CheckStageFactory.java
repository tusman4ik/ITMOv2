package ru.tusman4ik.coderunner.services.stages.check;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.checker.TokenChecker;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.coderunner.models.domain.group.TestCase;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;

@Slf4j
public final class CheckStageFactory {
    private CheckStageFactory() {}

    public static CheckStageResult get(Context ctx, TestCase testCase) {
        return StageRunner.call(
                Stage.CHECK,
                () -> (
                        switch (testCase.checker()) {
                            case TokenChecker tokenChecker -> new TokenCheckStage(tokenChecker, ctx);
                            default ->
                                    throw new IllegalStateException("Unexpected value: " + testCase.checker().getClass());
                        }
                ).call()
        );
    }
}
