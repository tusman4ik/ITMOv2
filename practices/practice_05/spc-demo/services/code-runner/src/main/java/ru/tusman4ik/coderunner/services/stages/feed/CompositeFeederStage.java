package ru.tusman4ik.coderunner.services.stages.feed;

import lombok.RequiredArgsConstructor;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.CompositeFeeder;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.feeder.Feeder;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;

@RequiredArgsConstructor
public class CompositeFeederStage implements StageRunner.StageRunnable {

    private final CompositeFeeder compositeFeeder;
    private final Context ctx;


    @Override
    public void run() throws Exception {
        for (Feeder feeder : compositeFeeder.feeders()){
            FeedStageFactory.get(ctx, feeder);
        }
    }
}
