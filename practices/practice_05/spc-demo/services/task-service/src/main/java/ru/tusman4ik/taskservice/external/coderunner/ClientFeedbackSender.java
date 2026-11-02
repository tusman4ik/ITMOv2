package ru.tusman4ik.taskservice.external.coderunner;

import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.taskservice.task.id.AttemptId;

/**
 * Outbound delivery of projected verdicts to the client backend.
 */
public interface ClientFeedbackSender {

    void send(AttemptId attempt, SuccessCheck verdict);
}
