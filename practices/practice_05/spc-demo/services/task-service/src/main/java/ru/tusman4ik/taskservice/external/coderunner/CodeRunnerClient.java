package ru.tusman4ik.taskservice.external.coderunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.tusman4ik.apicontracts.coderunner.submission.SubmissionRequestDto;
import ru.tusman4ik.taskservice.common.error.implementations.CodeRunnerClientException;

/**
 * Submits code to code-runner over HTTP.
 *
 * <p>Serializes with Jackson 2 on purpose: api-contracts annotations are
 * {@code com.fasterxml}-based, while Boot 4 defaults to {@code tools.jackson}.
 */
@Component
public class CodeRunnerClient {

    private static final JsonMapper MAPPER = JsonMapper.builder().findAndAddModules().build();

    private final RestClient restClient;

    public CodeRunnerClient(@Value("${code-runner.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Retryable(
            retryFor = {CodeRunnerClientException.class, RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void submit(SubmissionRequestDto request) {
        final String body;
        try {
            body = MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new CodeRunnerClientException("Code-runner request serialization failed", e);
        }
        restClient.post()
                .uri("/api/v1/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                    try { handleError(response); } catch (Exception e) { throw new CodeRunnerClientException("Code-runner request failed", e); }
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                    try { handleError(response); } catch (Exception e) { throw new CodeRunnerClientException("Code-runner request failed", e); }
                })
                .toBodilessEntity();
    }

    private void handleError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
        String body = new String(response.getBody().readAllBytes());
        throw new CodeRunnerClientException(
                "Code-runner error: " + response.getStatusCode() + " - " + body);
    }
}
