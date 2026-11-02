package ru.tusman4ik.coderunner;

import com.fasterxml.jackson.core.JacksonException;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import ru.tusman4ik.apicontracts.coderunner.common.exceptions.ValidationException;
import ru.tusman4ik.coderunner.handler.SubmissionHandler;
import ru.tusman4ik.apicontracts.coderunner.common.AbstractResponseDto;

@Slf4j
public final class App {

    private static final int PORT = 8080;

    private App() {
    }

    public static void main(String[] args) {

        SubmissionHandler submissionHandler = new SubmissionHandler();

        Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(AppConfig.MAPPER, true));

            config.registerPlugin(AppConfig.SWAGGER_PLUGIN);
            config.registerPlugin(AppConfig.OPEN_API_PLUGIN);

            config.routes.post("/api/v1/submissions", submissionHandler);

            config.routes.exception(ValidationException.class, (e, ctx) -> {
                ctx.status(400).json(AbstractResponseDto.validationError(e));
            });

            config.routes.exception(JacksonException.class, (e, ctx) -> {
                log.error("here we are");
                ctx.status(400).json(AbstractResponseDto.jsonError(e));
            });

            config.routes.exception(RuntimeException.class, (e, ctx) -> {
                ctx.status(500).json(AbstractResponseDto.systemError(e));
            });

            config.routes.before(ctx -> {
                String traceId = ctx.header("traceparent") == null ? "no id" : ctx.header("traceparent");
                MDC.put("traceId", traceId);
                ctx.header("traceparent", traceId);
            });

            config.routes.after(cfg -> {
                MDC.remove("traceId");
            });

            config.events.serverStopping(submissionHandler::shutdown);
        }).start(PORT);

        log.info("Check swagger: http://localhost:{}/swagger", PORT);

    }
}
