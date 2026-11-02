package ru.tusman4ik.coderunner.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static ru.tusman4ik.coderunner.utils.CommonUtils.consumeEveryLine;

@Slf4j
public final class ProcessUtils {
    private static final ExecutorService IO_POOL = Executors.newCachedThreadPool();

    private ProcessUtils() {}

    public static ProcessInfo runProcess(ProcessBuilder builder) throws IOException {
        return runProcess(builder, Integer.MAX_VALUE, List.of());
    }

    public static ProcessInfo runProcess(ProcessBuilder builder, long timeoutSeconds)
            throws IOException {
        return runProcess(builder, timeoutSeconds, List.of());
    }

    public static ProcessInfo runProcess(ProcessBuilder builder, Collection<Integer> allowedExitCodes)
            throws IOException {
        return runProcess(builder, Integer.MAX_VALUE, allowedExitCodes);
    }

    public static ProcessInfo runProcess(ProcessBuilder builder, long timeoutSeconds, Collection<Integer> allowedExitCodes)
            throws IOException {
        Process process = builder.start();

        Future<String> stdoutFuture = IO_POOL.submit(() -> readStream(process.getInputStream()));
        Future<String> stderrFuture = IO_POOL.submit(() -> readStream(process.getErrorStream()));

        try {
            CompletableFuture<Process> onExitFuture = process.onExit();
            onExitFuture.get(timeoutSeconds, TimeUnit.SECONDS);

            String stdout = stdoutFuture.get();
            String stderr = stderrFuture.get();

            int exitCode = process.exitValue();
            boolean isAllowed = exitCode == 0 || (allowedExitCodes != null && allowedExitCodes.contains(exitCode));

            if (!isAllowed) {
                log.error("Process execution failed with exit code: {}", exitCode);
                log.error("Command: {}", String.join(" ", builder.command()));
                logStream("stdout", stdout, log::error);
                logStream("stderr", stderr, log::error);
            } else {
                log.trace("Process executed successfully with exit code: {}", exitCode);
                log.trace("Command: {}", String.join(" ", builder.command()));
                logStream("stdout", stdout, log::trace);
                logStream("stderr", stderr, log::trace);
            }

            return new ProcessInfo(exitCode, stdout, stderr);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cleanup(process, stdoutFuture, stderrFuture);
            throw new RuntimeException("Process execution interrupted", e);

        } catch (ExecutionException e) {
            cleanup(process, stdoutFuture, stderrFuture);
            throw new RuntimeException("Error reading process output streams", e.getCause());

        } catch (TimeoutException e) {
            cleanup(process, stdoutFuture, stderrFuture);
            throw new RuntimeException("Process timed out after " + timeoutSeconds + " seconds", e);
        }
    }

    private static void logStream(String name, String content, Consumer<String> logger) {
        if (content != null && !content.isBlank()) {
            consumeEveryLine(content, line -> logger.accept(String.format("[%s] %s", name, line)));
        }
    }

    private static String readStream(InputStream is) throws IOException {
        try (is) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void cleanup(Process process, Future<?>... futures) {
        for (Future<?> future : futures) {
            if (future != null) {
                future.cancel(true);
            }
        }
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }
}