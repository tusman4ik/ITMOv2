package ru.tusman4ik.coderunner.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.coderunner.AppConfig;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.SnapshotPolicy;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;
import ru.tusman4ik.coderunner.exceptions.StageException;
import ru.tusman4ik.coderunner.models.domain.SubmissionRequest;
import ru.tusman4ik.coderunner.models.domain.results.CaseResult;
import ru.tusman4ik.coderunner.models.mappers.responses.submission.SubmissionCallbackMapper;
import ru.tusman4ik.apicontracts.coderunner.callback.ErrorCallbackDto;
import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;
import ru.tusman4ik.apicontracts.coderunner.callback.SubmissionCallbackDto;
import ru.tusman4ik.coderunner.services.stages.build.BuildStageResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.tusman4ik.coderunner.Constants.CALLBACK_RETRY_CNT;

/**
 * Sends runner → task-service callbacks for a single submission.
 *
 * <p>Ordering guarantee: all sends go through one single-threaded sender, so bytes
 * on the wire follow {@code seq} order. The submission thread only serializes and
 * enqueues — running tests is never blocked by the network. Different submissions
 * own different managers/executors and never interfere with each other.
 */
@Slf4j
public final class CallbackManager implements AutoCloseable {

    private final HttpClient httpClient;
    private final SubmissionRequest submissionRequest;

    private final ExecutorService sender = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    private final List<CaseResult> results = new ArrayList<>();
    private int seq = 0;
    private long lastSendMs = 0;

    public CallbackManager(SubmissionRequest submissionRequest, HttpClient httpClient) {
        this.submissionRequest = submissionRequest;
        this.httpClient = httpClient;
    }

    public void sendError(StageException e) {
        flushResults(false);
        send(SubmissionCallbackMapper.toDto(seq++, e));
    }

    public void sendCaseResult(boolean isLast, CaseResult caseResult) {
        results.add(caseResult);
        flushResults(isLast);
    }

    public void sendCompilationLog(BuildStageResult buildStageResult){
        send(SubmissionCallbackMapper.toDto(seq++, buildStageResult, submissionRequest.outputLimits()));
    }

    private void flushResults(boolean isLast) {
        if(results.isEmpty()){
            return;
        }
        boolean isEnoughOfTime = System.currentTimeMillis() - lastSendMs > submissionRequest.deliveryMode().getInterval();
        if (isLast || isEnoughOfTime) {
            lastSendMs = System.currentTimeMillis();
            send(SubmissionCallbackMapper.toDto(
                    seq++, isLast, results,
                    submissionRequest.responseFields(), submissionRequest.outputLimits()));

            if(submissionRequest.snapshotPolicy() == SnapshotPolicy.DELTA){
                results.clear();
            }
        }
    }

    private void send(SubmissionCallbackDto dto) {
        int msgSeq = dto.seq();
        boolean terminal = dto.isLast();
        try {
            String json = AppConfig.MAPPER.writeValueAsString(
                    new RunnerCallback(submissionRequest.id(), dto));
            sender.execute(() -> {
                sendWithRetry(CALLBACK_RETRY_CNT, json, msgSeq);
                if (terminal) {
                    sender.shutdown();
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize callback seq={}, type={}, runId={}",
                    msgSeq, dto.getClass().getSimpleName(), submissionRequest.id(), e);
            sendErrorFallback(msgSeq);
        }
    }

    private void sendWithRetry(int leftAttempts, String body, long seq) {
        int attempt = CALLBACK_RETRY_CNT - leftAttempts + 1;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(submissionRequest.callbackUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();

            if (code >= 200 && code < 300) {
                log.debug("Callback seq={} sent successfully on attempt {}/{}", seq, attempt, CALLBACK_RETRY_CNT);
                return;
            }

            if (code >= 400 && code < 500) {
                log.error("Callback seq={} rejected by client: HTTP {} on attempt {}/{}, body={}",
                        seq, code, attempt, CALLBACK_RETRY_CNT, response.body());
                return;
            }

            // 5xx
            log.warn("Callback seq={} got HTTP {} on attempt {}/{}, will retry",
                    seq, code, attempt, CALLBACK_RETRY_CNT);
            retryOrFail(leftAttempts - 1, body, seq);

        } catch (Exception e) {
            log.warn("Callback seq={} attempt {}/{} failed: {} ({})",
                    seq, attempt, CALLBACK_RETRY_CNT, e.getClass().getSimpleName(), e.getMessage());
            retryOrFail(leftAttempts - 1, body, seq);
        }
    }

    private void retryOrFail(int leftAttempts, String body, long seq) {
        if (leftAttempts <= 0) {
            log.error("Callback seq={} exhausted all {} attempts, URL={}, body={}",
                    seq, CALLBACK_RETRY_CNT, submissionRequest.callbackUrl(), body);
            return;
        }
        backoff(CALLBACK_RETRY_CNT - leftAttempts);
        sendWithRetry(leftAttempts, body, seq);
    }

    private void backoff(int attemptNumber) {
        long delayMs = 100L * (1L << attemptNumber);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendErrorFallback(int seq) {
        try {
            ErrorCallbackDto error = ErrorCallbackDto.builder()
                    .seq(seq)
                    .isLast(true)
                    .stage(Stage.CALLBACK)
                    .build();
            String json = AppConfig.MAPPER.writeValueAsString(
                    new RunnerCallback(submissionRequest.id(), error));
            sender.execute(() -> {
                sendWithRetry(CALLBACK_RETRY_CNT, json, seq);
                sender.shutdown();
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error fallback for seq={}", seq, e);
        }
    }

    @Override
    public void close() {
        sender.shutdown();
    }
}
