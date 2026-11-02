package ru.tusman4ik.coderunner.services.stages.run;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.MountMode;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.coderunner.models.domain.group.TestCase;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.sandbox.Sandbox;
import ru.tusman4ik.coderunner.models.domain.results.SandboxInvocationResult;
import ru.tusman4ik.coderunner.services.stages.StageRunner;
import ru.tusman4ik.coderunner.services.workspace.Workspace;
import ru.tusman4ik.coderunner.services.workspace.artifacts.CommandArtifact;
import ru.tusman4ik.coderunner.services.workspace.artifacts.FileArtifact;
import ru.tusman4ik.coderunner.services.workspace.artifacts.Meta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark.*;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.CHECK;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.RUN;

@Slf4j
public final class RunStage {
    private RunStage() {

    }

    public static SandboxInvocationResult get(Context ctx, TestCase testCase) {
        return StageRunner.call(
                Stage.RUN,
                () -> call(ctx, testCase)
        );
    }

    private static SandboxInvocationResult call(Context ctx, TestCase testCase) throws IOException {
        Workspace workspace = ctx.getWorkspace();

        List<String> cmd = workspace
                .findOne(CMD, CommandArtifact.class)
                .cmd();


        String stdout = workspace.createFileArtifact("out", Meta.builder()
                        .mark(STDOUT)
                        .stage(RUN)
                        .stage(CHECK)
                        .build())
                .path()
                .getFileName()
                .toString();

        String stdin = workspace.findOne(STDIN, FileArtifact.class)
                .path().getFileName().toString();

        Path mountPath = workspace.getStageDir(RUN);

        SandboxInvocationResult res;
        try (Sandbox sandbox = Sandbox.of(ctx.getIdManager())) {

            res = sandbox.execute(builder -> builder
                    .withMount(Path.of("/box"), mountPath, MountMode.RW)
                    .withStdout(stdout)
                    .withStdin(stdin)
                    .withLimits(testCase.lim())
                    .withCommand(cmd)
            );

        } catch (IOException e) {
            log.error("Error in sandbox!", e);
            throw e;
        }
        return res;
    }

}
