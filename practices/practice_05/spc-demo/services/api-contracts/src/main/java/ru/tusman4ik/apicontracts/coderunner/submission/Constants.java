package ru.tusman4ik.apicontracts.coderunner.submission;

public final class Constants {
    private Constants() {}

    public static final double MIN_CPU_TIME_LIMIT_SEC = 0.5;       // 250 ms
    public static final double MAX_CPU_TIME_LIMIT_SEC = 5. * 60;    // 5 min

    public static final double MIN_REAL_TIME_LIMIT_SEC = 0.25;       // 250 ms
    public static final double MAX_REAL_TIME_LIMIT_SEC = 5. * 60;    // 5 min

    public static final int MIN_PROCESS_CNT = 1;
    public static final int MAX_PROCESS_CNT = 4;

    public static final int MIN_FILE_SIZE_MB = 1;
    public static final int MAX_FILE_SIZE_MB = 16;

    public static final int MIN_MEMORY_LIMIT_MB = 0;
    public static final int MAX_MEMORY_LIMIT_MB = 512;

    public static final int MIN_PRIORITY = 0;
    public static final int MAX_PRIORITY = 100;

    public static final int MIN_OUTPUT_LIMIT_CHARS = 0;
    public static final int MAX_OUTPUT_LIMIT_CHARS = 64 * 1024;

    public static final int DEFAULT_MAX_STDOUT_CHARS = 4 * 1024;
    public static final int DEFAULT_MAX_STDERR_CHARS = 4 * 1024;
    public static final int DEFAULT_MAX_INPUT_CHARS = 4 * 1024;
    public static final int DEFAULT_MAX_CHECKER_MSG_CHARS = 1024;
    public static final int DEFAULT_MAX_COMPILER_LOG_CHARS = 8 * 1024;

    public static final int PARALLEL_SUBMISSIONS_CNT = 2;

    public static final int MAX_CODE_SIZE_KB = 1024 * 256; //256 kb

    public static final int CALLBACK_RETRY_CNT = 3;

    @SuppressWarnings("unchecked")
    public static <T> T or(T cur, T def) {
        return cur == null ? def : cur;
    }
}