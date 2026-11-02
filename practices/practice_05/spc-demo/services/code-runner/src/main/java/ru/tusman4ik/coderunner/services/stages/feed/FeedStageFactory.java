package ru.tusman4ik.coderunner.services.stages.feed;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.CompositeFeeder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.Feeder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.FileFeeder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.TextFeeder;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;

@Slf4j
public final class FeedStageFactory {

    private FeedStageFactory() {}

    public static void get(Context ctx, Feeder feeder) {
        StageRunner.run(
                Stage.FEED,
                () -> {

                    (switch (feeder) {
                        case FileFeeder fileFeeder -> new FileFeedStage(fileFeeder, ctx);
                        case TextFeeder textFeeder -> new TextFeedStage(textFeeder, ctx);
                        case CompositeFeeder compositeFeeder -> new CompositeFeederStage(compositeFeeder, ctx);
                        default -> throw new IllegalStateException("Unexpected value: " + feeder);
                    }).run();

                }
        );
    }
}
