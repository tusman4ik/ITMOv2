package ru.tusman4ik.coderunner.handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.openapi.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.tusman4ik.coderunner.models.domain.SubmissionRequest;
import ru.tusman4ik.coderunner.models.mappers.requests.submission.SubmissionRequestMapper;
import ru.tusman4ik.apicontracts.coderunner.submission.SubmissionRequestDto;
import ru.tusman4ik.coderunner.services.SubmissionService;


@Slf4j
public class SubmissionHandler implements Handler {
    private final SubmissionService service = new SubmissionService();

    @OpenApi(
            summary = "Submit a code execution request",
            description = "Passes source code along with tests, structures, and resource limits to the evaluation queue.",
            path = "/api/v1/submissions",
            methods = {HttpMethod.POST},

            tags = {"Submissions"},

            requestBody = @OpenApiRequestBody(
                    content = @OpenApiContent(from = SubmissionRequestDto.class),
                    description = "Serialized submission request object containing source code and test groups",
                    required = true
            ),

            responses = {
                    @OpenApiResponse(
                            status = "202",
                            description = "Submission accepted and successfully queued for compilation and testing"
                    )
            }
    )
    @Override
    public void handle(@NotNull Context ctx) {
        SubmissionRequestDto dto = ctx.bodyAsClass(SubmissionRequestDto.class);
        SubmissionRequest submissionRequest = SubmissionRequestMapper.fromDto(dto);
        log.info(submissionRequest.toString().replace("\n", " "));
        service.submit(submissionRequest);
        // TODO check 429
        ctx.status(202);
    }

    public void shutdown(){
        service.shutdown();
    }


}
