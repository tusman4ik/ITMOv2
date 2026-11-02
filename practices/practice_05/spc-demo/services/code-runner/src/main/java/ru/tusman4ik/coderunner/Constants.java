package ru.tusman4ik.coderunner;

public final class Constants {
    private Constants() {}

    // Internal constants - not part of API contract
    public static final int PARALLEL_SUBMISSIONS_CNT = 2;
    public static final int MAX_CODE_SIZE_KB = 1024 * 256; // 256 kb
    public static final int CALLBACK_RETRY_CNT = 3;
}