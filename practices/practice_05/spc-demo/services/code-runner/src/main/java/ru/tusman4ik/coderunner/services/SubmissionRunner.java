package ru.tusman4ik.coderunner.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.ExecutionPolicy;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.coderunner.exceptions.StageException;
import ru.tusman4ik.coderunner.models.domain.SubmissionRequest;
import ru.tusman4ik.coderunner.models.domain.group.TestCase;
import ru.tusman4ik.coderunner.models.domain.group.TestGroup;
import ru.tusman4ik.coderunner.models.domain.results.CaseResult;
import ru.tusman4ik.coderunner.models.domain.results.SandboxInvocationResult;
import ru.tusman4ik.coderunner.services.sandbox.IdManager;
import ru.tusman4ik.coderunner.services.stages.build.BuildStageResult;
import ru.tusman4ik.coderunner.services.stages.build.BuilderFactory;
import ru.tusman4ik.coderunner.services.stages.check.CheckStageFactory;
import ru.tusman4ik.coderunner.services.stages.check.CheckStageResult;
import ru.tusman4ik.coderunner.services.stages.feed.FeedStageFactory;
import ru.tusman4ik.coderunner.services.stages.run.RunStage;
import ru.tusman4ik.coderunner.services.workspace.artifacts.FileArtifact;

import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
public final class SubmissionRunner implements Runnable {

    private final SubmissionRequest submissionRequest;
    private final CallbackManager callbackManager;
    private final IdManager idManager;

    public void run() {
        try (Context buildStageCtx = new Context(submissionRequest, idManager)) {

            BuildStageResult result = BuilderFactory.get(buildStageCtx);

            callbackManager.sendCompilationLog(result);
            if (result.status() != Status.OK) {
                return;
            }

            runTests(buildStageCtx);

        } catch (StageException e) {
            log.error("Submission finished with error!", e);
            callbackManager.sendError(e);
        } catch (Exception e) {
            log.error("EXPECT ONLY STAGE EXCEPTION!!!!", e);
            throw new AssertionError("Unexpected exception in SubmissionRunner", e);
        }
    }

    private void runTests(Context buildStageCtx) {
        boolean stopAfterGroup = false;

        for (int i = 0; i < submissionRequest.groups().size(); i++) {
            TestGroup group = submissionRequest.groups().get(i);

            log.debug("Test group {} starting...", group.id());
            for (int j = 0; j < group.tests().size(); j++) {
                TestCase testCase = group.tests().get(j);

                CaseResult caseResult = runTest(buildStageCtx, testCase, group.id());
                boolean failed = caseResult.status() != Status.OK;

                boolean finishing = isFinishing(failed, i, j, stopAfterGroup);
                callbackManager.sendCaseResult(finishing, caseResult);

                if (finishing) {
                    return;
                }
                if (failed && submissionRequest.executionPolicy() == ExecutionPolicy.STOP_GROUP_ON_FIRST_FAILED) {
                    break;
                }
                if (failed && submissionRequest.executionPolicy() == ExecutionPolicy.COMPLETE_GROUP_ON_FAILURE) {
                    stopAfterGroup = true;
                }
            }

            if (stopAfterGroup) {
                return;
            }
            log.debug("Test group {} finished successfully", group.id());
        }
    }

    private boolean isFinishing(boolean failed, int groupIdx, int testIdx, boolean stopAfterGroup) {
        TestGroup group = submissionRequest.groups().get(groupIdx);
        boolean lastInGroup = (testIdx == group.tests().size() - 1);
        boolean lastGroup = (groupIdx == submissionRequest.groups().size() - 1);

        return switch (submissionRequest.executionPolicy()) {
            case STOP_ON_FIRST_FAILURE -> failed || (lastInGroup && lastGroup);
            case STOP_GROUP_ON_FIRST_FAILED, RUN_ALL -> lastInGroup && lastGroup;
            case COMPLETE_GROUP_ON_FAILURE -> lastInGroup && (failed || stopAfterGroup || lastGroup);
        };
    }

    private static CaseResult runTest(Context buildStageCtx, TestCase testCase, String groupId) {

        try (Context ctx = initContext(buildStageCtx)) {
            log.debug("Test case {} starting...", testCase.id());

            CaseResult.CaseResultBuilder result = CaseResult.builder()
                    .caseId(testCase.id())
                    .groupId(groupId);

            FeedStageFactory.get(ctx, testCase.feeder());

            SandboxInvocationResult sandboxResult = RunStage.get(ctx, testCase);

            result.status(sandboxResult.status())
                    .cpuTimeSec(sandboxResult.cpuTimeSec())
                    .realTimeSec(sandboxResult.realTimeSec())
                    .memoryKb(sandboxResult.memoryKb())
                    .exitCode(sandboxResult.exitCode())
                    .stderr(sandboxResult.stderr())
                    .stdout(readWorkspaceFile(ctx, Mark.STDOUT))
                    .input(readWorkspaceFile(ctx, Mark.STDIN));

            if (sandboxResult.status() == Status.OK) {
                CheckStageResult res = CheckStageFactory.get(ctx, testCase);
                result.status(res.status())
                        .checkerMsg(res.checkerMsg());

            }

            log.debug("Test case {} finished successfully", testCase.id());
            return result.build();

        } catch (StageException e) {
            e.setTestId(testCase.id());
            e.setGroupId(groupId);
            throw e;

        } catch (Exception e) {
            var targetException = new StageException(e);
            targetException.setTestId(testCase.id());
            targetException.setGroupId(groupId);
            throw targetException;
        }
    }

    private static @NotNull Context initContext(Context buildStageCtx) {
        try {
            return new Context(buildStageCtx);
        } catch (Exception e) {
            throw new StageException(e, Stage.SETUP);
        }
    }

    /**
     * Best-effort read of a workspace file artifact (program stdout / case stdin).
     * Never fails the run: executable/file feeders may produce zero or several
     * artifacts with the mark, in which case we report {@code null} and let the
     * response-field filter/truncation in the mapper decide what leaves the box.
     */
    private static String readWorkspaceFile(Context ctx, Mark mark) {
        try {
            FileArtifact artifact = ctx.getWorkspace().findOne(mark, FileArtifact.class);
            return Files.readString(artifact.path());
        } catch (Exception e) {
            log.debug("Workspace file with mark {} not readable, reporting null", mark);
            return null;
        }
    }
}
