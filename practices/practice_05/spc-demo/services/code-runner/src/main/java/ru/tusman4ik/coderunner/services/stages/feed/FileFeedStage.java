package ru.tusman4ik.coderunner.services.stages.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.FileFeeder;
import ru.tusman4ik.coderunner.exceptions.SandboxException;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;
import ru.tusman4ik.coderunner.services.workspace.artifacts.FileArtifact;
import ru.tusman4ik.coderunner.services.workspace.artifacts.Meta;

import java.nio.file.Files;
import java.nio.file.Path;

import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.CHECK;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.RUN;

@Slf4j
@RequiredArgsConstructor
public class FileFeedStage implements StageRunner.StageRunnable {

    private final FileFeeder feeder;
    private final Context ctx;

    @Override
    public void run() {
        Path path = Path.of(feeder.path());

        if (!Files.exists(path)) {
            throw new SandboxException("File " +
                    "not found: " + feeder.path());
        }

        ctx.getWorkspace()
                .register(
                        new FileArtifact(
                                Meta.builder()
                                        .stage(RUN)
                                        .stage(CHECK)
                                        .mark(feeder.target().getMark())
                                        .build(),
                                path
                        )
                );
        log.debug("Feed stage finished successfully");
    }
}
