package ru.tusman4ik.taskservice.task.generator;

import ru.tusman4ik.apicontracts.coderunner.callback.RunnerCallback;
import ru.tusman4ik.apicontracts.taskservice.check.SuccessCheck;
import ru.tusman4ik.taskservice.task.id.AttemptId;

/**
 * Capability of generators whose answers are checked by code execution.
 *
 * <p>Implemented by the generator itself: only it knows how runner snapshots
 * map back onto its domain (reference answers, criteria, scoring).
 * Invoked with warmed nodes, so node access is safe inside the call.
 * Returns the full verdict; the caller streams {@link SuccessCheck#feedback()}
 * to the client as partial feedback.
 */
public interface CodePendingChecker {

    SuccessCheck checkCode(AttemptId attempt, RunnerCallback callback);
}
