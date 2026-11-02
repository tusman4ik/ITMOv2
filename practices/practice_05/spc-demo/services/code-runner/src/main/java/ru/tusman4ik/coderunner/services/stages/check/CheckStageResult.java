package ru.tusman4ik.coderunner.services.stages.check;

import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;

public record CheckStageResult(
        Status status,
        String checkerMsg
) {
}
