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
import ru.tusman4ik.apicontracts.taskservice.check.CheckedItemDto;
import ru.tusman4ik.apicontracts.taskservice.check.CheckResponseDto;
import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.taskservice.common.error.implementations.ClientCallbackFailedException;
import ru.tusman4ik.taskservice.task.id.AttemptId;

import java.util.List;

/**
 * Delivers verdicts to the client backend over HTTP.
 *
 * <p>Serializes with Jackson 2 on purpose: api-contracts annotations are
 * {@code com.fasterxml}-based, while Boot 4 defaults to {@code tools.jackson}.
 *
 * <p>Failures propagate as 502 so the ingress answers the runner with 5xx
 * and the runner retries the callback (bounded, at-least-once redelivery).
 */
@Component
public class HttpClientFeedbackSender implements ClientFeedbackSender {

    private static final JsonMapper MAPPER = JsonMapper.builder().findAndAddModules().build();

    private final RestClient restClient;

    public HttpClientFeedbackSender(@Value("${demo.base-url:http://localhost:8090}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Retryable(
            retryFor = {RestClientException.class, ClientCallbackFailedException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Override
    public void send(AttemptId attempt, SuccessCheck verdict) {
        final String body;
        try {
            body = MAPPER.writeValueAsString(
                    new CheckResponseDto(List.of(new CheckedItemDto(attempt.toDto(), verdict))));
        } catch (JsonProcessingException e) {
            throw new ClientCallbackFailedException("serialize: " + e.getMessage());
        }
        try {
            restClient.post()
                    .uri("/api/verdicts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        throw new ClientCallbackFailedException(
                                response.getStatusCode() + " " + new String(readBody(response)));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                        throw new ClientCallbackFailedException(
                                response.getStatusCode() + " " + new String(readBody(response)));
                    })
                    .toBodilessEntity();
        } catch (ClientCallbackFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientCallbackFailedException(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static byte[] readBody(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return response.getBody().readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
