package ru.tusman4ik.coderunner.services.stages.build;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.coderunner.services.Context;
import ru.tusman4ik.coderunner.services.stages.StageRunner;

@Slf4j
public final class BuilderFactory {

    private BuilderFactory() {}

    public static BuildStageResult get(Context ctx)  {
        return StageRunner.call(
                Stage.BUILD,
                () -> ( switch (ctx.getRequest().lang()) {
                        case PYTHON -> new PythonBuildStage(ctx);
                        case CPP -> new CppBuildStage(ctx);
                        case CSHARP, JAVA, PASCAL -> throw new UnsupportedOperationException("");
                    }).call()
        );
    }
}
