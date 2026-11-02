package ru.tusman4ik.coderunner.services.stages.build;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;
import ru.tusman4ik.coderunner.services.workspace.artifacts.CommandArtifact;
import ru.tusman4ik.coderunner.services.workspace.artifacts.Meta;

import java.util.List;

import static ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark.CMD;
import static ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark.EXEC;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.RUN;


@Slf4j
@RequiredArgsConstructor
public class PythonBuildStage implements StageRunner.StageCallable<BuildStageResult> {

    private final Context ctx;

    @Override
    public BuildStageResult call() throws Exception {
        String src = ctx.getRequest().src();
        ctx.getWorkspace().registerFileArtifact(
                "exec.py",
                src,
                Meta.builder()
                        .stage(RUN)
                        .mark(EXEC)
                        .build()
        );

        ctx.getWorkspace().register(
                CommandArtifact.builder()
                        .meta(Meta.builder()
                                .stage(RUN)
                                .mark(CMD)
                                .build())
                        .cmd(List.of("/bin/python3", "exec.py"))
                        .build()
        );

        return BuildStageResult.ok();
    }
}
