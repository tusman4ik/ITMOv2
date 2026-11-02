package ru.tusman4ik.coderunner.exceptions;

public class SandboxException extends RuntimeException {
    public SandboxException(String message) {
        super(message);
    }

    public SandboxException(String message, Throwable cause) {
        super(message, cause);
    }
}

