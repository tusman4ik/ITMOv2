package ru.tusman4ik.apicontracts.taskservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tusman4ik.apicontracts.taskservice.check.CheckRequestDto;
import ru.tusman4ik.apicontracts.taskservice.check.CheckResponseDto;
import ru.tusman4ik.apicontracts.taskservice.task.GenerateRequestDto;
import ru.tusman4ik.apicontracts.taskservice.task.RecreateRequestDto;
import ru.tusman4ik.apicontracts.taskservice.task.TasksResponseDto;

@Tag(name = "tasks", description = "Generation, recreation and checking of tasks")
@RestController
@RequestMapping("api/v1/tasks")
public interface TaskApi {

    @Operation(operationId = "generateTasks",
            summary = "Generate tasks",
            description = "Generates new task instances for the ordered prototypes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Generated tasks",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TasksResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation or domain error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/generate")
    TasksResponseDto generate(@RequestBody GenerateRequestDto req);

    @Operation(operationId = "recreateTasks",
            summary = "Recreate tasks",
            description = "Regenerates task instances keeping their identities")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recreated tasks",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TasksResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation or domain error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/recreate")
    TasksResponseDto recreate(@RequestBody RecreateRequestDto req);

    @Operation(operationId = "checkTasks",
            summary = "Check answers",
            description = "Checks user answers against task criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check results",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation or domain error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/check")
    CheckResponseDto check(@RequestBody CheckRequestDto req);
}
