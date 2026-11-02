package ru.tusman4ik.coderunner.models.domain;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.OutputLimitsDto;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.*;
import ru.tusman4ik.apicontracts.taskservice.id.AttemptIdDto;
import ru.tusman4ik.coderunner.models.domain.group.TestGroup;

import java.util.EnumSet;
import java.util.List;

@Builder
public record SubmissionRequest(
        AttemptIdDto id,
        String callbackUrl,

        CallbackTrigger callbackTrigger,
        DeliveryMode deliveryMode,
        SnapshotPolicy snapshotPolicy,

        Integer priority,
        EnumSet<ResponseField> responseFields,
        OutputLimitsDto outputLimits,

        ExecutionPolicy executionPolicy,
        Language lang,
        String src,

        List<TestGroup> groups

) {

}
