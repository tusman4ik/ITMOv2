package ru.tusman4ik.coderunner.services.stages;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.coderunner.exceptions.StageException;

@Slf4j
public class StageRunner {

    private StageRunner() {}

    @FunctionalInterface
    public interface StageCallable<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    public interface StageRunnable {
        void run() throws Exception;
    }


    public static <T> T call(Stage stage, StageCallable<T> action)  {
        String stageName = stage.name().toLowerCase();
        log.debug("{} stage starting...", stageName);
        try {
            T result = action.call();
            log.debug("{} stage finished successfully", stageName);
            return result;
        }catch (StageException e){
            e.setStage(stage);
            throw e;
        }
        catch (Exception e) {
            throw new StageException(e, stage);
        }
    }

    public static void run(Stage stage, StageRunnable action)  {
        call(stage, () -> {
            action.run();
            return null;
        });
    }
}
