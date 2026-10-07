package ru.tusman4ik.task.nodes;

public class RetryableNodeException extends RuntimeException {

    public RetryableNodeException(String message) {
        super(message);
    }

    public RetryableNodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
