package ru.tusman4ik.coderunner.services.stages.build;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.limits.Limits;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.MountMode;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.coderunner.exceptions.SandboxException;
import ru.tusman4ik.coderunner.models.domain.results.SandboxInvocationResult;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.sandbox.Sandbox;
import ru.tusman4ik.coderunner.services.stages.StageRunner;
import ru.tusman4ik.coderunner.services.workspace.Workspace;
import ru.tusman4ik.coderunner.services.workspace.artifacts.CommandArtifact;
import ru.tusman4ik.coderunner.services.workspace.artifacts.Meta;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import static ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark.CMD;
import static ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark.EXEC;
import static ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark.SRC;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.BUILD;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.RUN;

@Slf4j
@RequiredArgsConstructor
public class CppBuildStage implements StageRunner.StageCallable<BuildStageResult> {

    private final Context ctx;

    @Override
    public BuildStageResult call() throws Exception {

        Workspace workspace = ctx.getWorkspace();
        workspace.registerFileArtifact(
                "exec.cpp",
                ctx.getRequest().src(),
                Meta.builder()
                        .stage(BUILD)
                        .mark(SRC)
                        .build()
        );

        workspace.createFileArtifact(
                "exec",
                Meta.builder()
                        .stage(BUILD)
                        .stage(RUN)
                        .mark(EXEC)
                        .build(),
                PosixFilePermissions.fromString("rwxrwxrwx")
        );

        workspace.register(
                CommandArtifact.builder()
                        .meta(Meta.builder().stage(RUN).mark(CMD).build())
                        .cmd(List.of("./exec"))
                        .build()
        );

        Path mountPath = workspace.getStageDir(BUILD);

        try (Sandbox sandbox = Sandbox.of(ctx.getIdManager())) {
            SandboxInvocationResult res = sandbox.execute(cfg ->
                    cfg
                            .withLimits(
                                    Limits.builder()
                                            .cpuTimeLimitSec(12.)
                                            .realTimeLimitSec(12.)
                                            .memoryLimitKb(1024 * 10)
                                            .fileSizeKb(12211)
                                            .processCnt(10).build()
                            )
                            .withMount(Path.of("/box"), mountPath, MountMode.RW)
                            .withCommand(List.of("/bin/clang++", "-o", "exec",
                                    "-I/usr/include/c++/16",
                                    "--gcc-install-dir=/usr/lib/gcc/x86_64-pc-linux-gnu/16",
                                    "exec.cpp"
                            )));
            if (res.status() == Status.IE) {
                throw new SandboxException(res.stderr());
            }

            if (res.status() != Status.OK) {
                return BuildStageResult.error(res.stderr());
            }

        }
        return BuildStageResult.ok();
    }
}// 969 706 21 77 лейсан
