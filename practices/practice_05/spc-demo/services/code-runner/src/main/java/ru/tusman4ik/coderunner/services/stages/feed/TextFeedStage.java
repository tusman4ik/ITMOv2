package ru.tusman4ik.coderunner.services.stages.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.TextFeeder;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;
import ru.tusman4ik.coderunner.services.workspace.artifacts.Meta;

import java.io.IOException;
import java.util.Locale;

import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.CHECK;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage.RUN;

@Slf4j
@RequiredArgsConstructor
public class TextFeedStage implements StageRunner.StageRunnable {

    private final TextFeeder feeder;
    private final Context ctx;

    @Override
    public void run() throws IOException {
        ctx.getWorkspace().registerFileArtifact(
                feeder.target().getMark().name().toLowerCase(Locale.ROOT),
                feeder.stdin(),
                Meta.builder()
                        .stage(RUN)
                        .stage(CHECK)
                        .mark(feeder.target().getMark())
                        .build()
        );
    }
}
