package ru.tusman4ik.apicontracts.taskservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;

@Tag(name = "runner-callbacks", description = "Internal ingestion of code-runner callbacks")
@RestController
@RequestMapping(RunnerCallbackApi.BASE_PATH)
public interface RunnerCallbackApi {

    String BASE_PATH = "api/v1/internal/coderunner";

    @Operation(operationId = "runnerCallback",
            summary = "Ingest code-runner callback",
            description = "Accepts a cumulative runner snapshot for one attempt and projects it into client feedback")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Callback accepted"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Attempt prototype does not handle code callbacks",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(value = "/callbacks", consumes = MediaType.APPLICATION_JSON_VALUE)
    void onCallback(@Valid @RequestBody RunnerCallback callback);
}
