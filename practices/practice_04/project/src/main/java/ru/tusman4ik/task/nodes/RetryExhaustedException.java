package ru.tusman4ik.task.nodes;

public class RetryExhaustedException extends RetryableNodeException {

    private final String nodeName;
    private final int maxAttempts;

    public RetryExhaustedException(String nodeName, int maxAttempts, RetryableNodeException cause) {
        super("node '" + nodeName + "' exhausted after " + maxAttempts + " attempts", cause);
        this.nodeName = nodeName;
        this.maxAttempts = maxAttempts;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
