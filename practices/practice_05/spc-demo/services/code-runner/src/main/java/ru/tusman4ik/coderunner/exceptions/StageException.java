package ru.tusman4ik.coderunner.exceptions;

import lombok.Getter;
import lombok.Setter;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;

@Getter
@Setter
public class StageException extends RuntimeException {
    private Stage stage;

    private String testId;
    private String groupId;

    public StageException(Stage stage, String testId, String groupId) {
        this.stage = stage;
        this.testId = testId;
        this.groupId = groupId;
    }

    public StageException(Throwable cause) {
        super(cause);
    }

    public StageException(Stage stage) {
        this.stage = stage;
    }

    public StageException(String message, Stage stage) {
        super(message);
        this.stage = stage;
    }

    public StageException(String message, Throwable cause, Stage stage) {
        super(message, cause);
        this.stage = stage;
    }

    public StageException(Throwable cause, Stage stage) {
        super(cause);
        this.stage = stage;
    }

    public StageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Stage stage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.stage = stage;
    }

    public StageException(String message, Stage stage, String testId, String groupId) {
        super(message);
        this.stage = stage;
        this.testId = testId;
        this.groupId = groupId;
    }

    public StageException(String message, Throwable cause, Stage stage, String testId, String groupId) {
        super(message, cause);
        this.stage = stage;
        this.testId = testId;
        this.groupId = groupId;
    }

    public StageException(Throwable cause, Stage stage, String testId, String groupId) {
        super(cause);
        this.stage = stage;
        this.testId = testId;
        this.groupId = groupId;
    }

    public StageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Stage stage, String testId, String groupId) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.stage = stage;
        this.testId = testId;
        this.groupId = groupId;
    }
}
