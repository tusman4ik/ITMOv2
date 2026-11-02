package ru.tusman4ik.coderunner.services.sandbox;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.coderunner.exceptions.SandboxException;
import ru.tusman4ik.coderunner.models.domain.results.SandboxInvocationResult;
import ru.tusman4ik.coderunner.utils.ProcessInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.function.Function;

import static ru.tusman4ik.coderunner.services.sandbox.IsolateQueryBuilder.cleanup;
import static ru.tusman4ik.coderunner.services.sandbox.IsolateQueryBuilder.init;
import static ru.tusman4ik.coderunner.utils.FilesUtils.*;
import static ru.tusman4ik.coderunner.utils.ProcessUtils.runProcess;

@Slf4j
public class Sandbox implements AutoCloseable {
    public static final int DEFAULT_TIMEOUT = 2;
    public static final int RETRY_CNT = 3;
    public static final int USERS_CODE_FAILED = 1;

    private final int id;
    private final IdManager idManager;
    @Getter
    private final Path rootPath;
    @Getter
    private final Path homePath;
    private final Path tmpDir;
    private final Path meta;

    public static Sandbox of(IdManager idManager) {
        StringBuilder errorLog = new StringBuilder();
        Exception lastException = null;

        for (int i = 0; i < RETRY_CNT; i++) {
            try {
                return new Sandbox(idManager);
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} to initialize sandbox failed: {}", i + USERS_CODE_FAILED, RETRY_CNT, e.getMessage());
                errorLog.append(String.format("[%d/%d] %s; ", i + USERS_CODE_FAILED, RETRY_CNT, e.getMessage()));
            }
        }
        throw new SandboxException("Failed to allocate sandbox after " + RETRY_CNT + " attempts: " + errorLog, lastException);
    }

    private Sandbox(IdManager idManager) throws IOException {
        this.idManager = idManager;
        this.id = idManager.acquire();
        this.rootPath = Path.of("/var/local/lib/isolate/" + id);
        this.homePath = rootPath.resolve("box");

        if (Files.exists(rootPath)) {
            log.warn("Sandbox directory for id {} already exists on disk. Attempting cleanup before init", id);
            ProcessInfo info = runProcess(cleanup(id), DEFAULT_TIMEOUT);
            if (info.exitCode() != 0 || Files.exists(rootPath)) {
                log.error("Failed to clean up existing sandbox directory for id {}. Keeping ID acquired to prevent reuse.", id);
                throw new SandboxException("Sandbox directory already exists and cleanup failed for id " + id);
            }
            log.trace("Attempt succeed");
        }

        ProcessInfo processInfo = runProcess(init(id), DEFAULT_TIMEOUT);
        if (processInfo.exitCode() != 0) {
            log.error("Failed to initialize isolate sandbox id {}. Exit code: {}", id, processInfo.exitCode());
            throw new SandboxException("Isolate init failed for id " + id + " with exit code " + processInfo.exitCode());
        }

        try{
            tmpDir = Files.createTempDirectory(Path.of("/dev/shm"), "sandbox-");
            meta = tmpDir.resolve("meta");
            Files.createFile(meta);
            Files.setPosixFilePermissions(meta, PosixFilePermissions.fromString("rwxrwxrwx"));
        }catch (Throwable e){
            log.error("Error with meta file creation", e);
            throw new SandboxException("Error with meta file creation", e);
        }

        log.trace("Sandbox {} created successfully", id);
    }

    public SandboxInvocationResult execute(Function<IsolateQueryBuilder, ProcessBuilder> cfg) throws IOException {
        IsolateQueryBuilder queryBuilder = IsolateQueryBuilder.run(id).withMeta(meta.toString());
        ProcessBuilder process = cfg.apply(queryBuilder);
        ProcessInfo processInfo = runProcess(process, List.of(USERS_CODE_FAILED));
        return SandboxInvocationAnalyzer.analyze(meta, processInfo);
    }

    public Path get(String path) {
        return rootPath.resolve(path);
    }

    public Path get(Path path) {
        return rootPath.resolve(path);
    }

    @Override
    public void close() {
        try {
            runProcess(cleanup(id), DEFAULT_TIMEOUT);
            deleteDirRecursively(tmpDir);
            idManager.release(id);
            log.trace("Sandbox {} cleaned and released successfully", id);
        } catch (IOException e) {
            log.error("Exception during sandbox {} cleanup destructor! ID remains held for safety.", id, e);
        }
    }
}

