package ru.tusman4ik.coderunner.services;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.coderunner.models.domain.SubmissionRequest;
import ru.tusman4ik.coderunner.services.sandbox.IdManager;

import java.net.http.HttpClient;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ru.tusman4ik.coderunner.Constants.PARALLEL_SUBMISSIONS_CNT;

@Slf4j
public final class SubmissionService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final IdManager idManager = new IdManager();
    private final ExecutorService threadPool = new ThreadPoolExecutor(
            PARALLEL_SUBMISSIONS_CNT,
            PARALLEL_SUBMISSIONS_CNT,
            0L, TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<>()
    );


    private record Task(SubmissionRequest request, long createdNano, SubmissionService service)
            implements Runnable, Comparable<Task> {

        private static final Comparator<Task> COMPARATOR = Comparator
                .comparingInt((Task t) -> t.request().priority()).reversed()
                .thenComparingLong(Task::createdNano);

        private Task(SubmissionRequest request, SubmissionService service) {
            this(request, System.nanoTime(), service);
        }

        @Override
        public void run() {
            try (CallbackManager callbackManager = new CallbackManager(request, service.httpClient)) {
                new SubmissionRunner(
                        request,
                        callbackManager,
                        service.idManager
                ).run();
            }

        }


        @Override
        public int compareTo(Task other) {
            return COMPARATOR.compare(this, other);
        }
    }

    public void submit(SubmissionRequest submissionRequest) {
        threadPool.execute(new Task(submissionRequest, this));
    }

    public void shutdown() {
        log.info("Shutting down SubmissionService thread pool...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
//
//    private void run(SubmissionRequest submissionRequest) {
//
//        HttpRequest httpRequest = null;
//        try {
//
//            SubmissionResponse response = new SubmissionRunner(submissionRequest).run();
//            String json = AppConfig.MAPPER.writeValueAsString(response);
//
//            httpRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(submissionRequest.callbackUrl()))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(json))
//                    .build();
//
//        } catch (Exception e) {
//            // TODO
//        }
//
//        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
//                .thenAccept(resp -> {
//                    if (200 <= resp.statusCode() && resp.statusCode() <= 299)
//                        logSuccess(submissionRequest);
//                    else
//                        logFailed(submissionRequest, resp);
//                })
//                .exceptionally(throwable -> logError(submissionRequest, throwable));
//    }
//
//    private static @Nullable Void logError(SubmissionRequest submissionRequest, Throwable throwable) {
//        log.error("Response for request '{}' failed with exception", submissionRequest.id(), throwable);
//        return null;
//    }
//
//    private static void logFailed(SubmissionRequest submissionRequest, HttpResponse<String> resp) {
//        log.error("Response for request '{}' probably was failed", submissionRequest.id());
//        log.error("Status code = {}", resp.statusCode());
//
//        resp.headers().map().forEach((s, vals) ->
//                log.error("Header: {}: {}", s, vals));
//
//        log.error("");
//
//        or(resp.body(), "").lines().forEach(line ->
//                log.error("Body: {}", line));
//    }
//
//    private static void logSuccess(SubmissionRequest submissionRequest) {
//        log.info("Response for request '{}' was sent correctly", submissionRequest.id());
//    }

}
