package ru.tusman4ik.apicontracts.coderunner.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tusman4ik.apicontracts.coderunner.submission.SubmissionRequestDto;

@Tag(name = "Submissions", description = "Code execution requests")
@RestController
@RequestMapping("api/v1/submissions")
public interface SubmissionsApi {

    @Operation(operationId = "submit",
            summary = "Submit a code execution request",
            description = "Passes source code along with tests, structures, and resource limits to the evaluation queue.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Submission accepted and successfully queued for compilation and testing")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    void submit(@RequestBody SubmissionRequestDto req);
}
