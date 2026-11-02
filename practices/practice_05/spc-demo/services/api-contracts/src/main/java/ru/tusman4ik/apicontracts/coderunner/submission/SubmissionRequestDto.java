package ru.tusman4ik.apicontracts.coderunner.submission;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.*;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.CheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.FeederDto;
import ru.tusman4ik.apicontracts.taskservice.id.AttemptIdDto;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static ru.tusman4ik.apicontracts.coderunner.submission.enums.CallbackTrigger.ON_COMPLETE_ONLY;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.SnapshotPolicy.FULL;

@Builder(toBuilder = true)
public record SubmissionRequestDto(

        AttemptIdDto id,
        String callbackUrl,

        CallbackTrigger callbackTrigger,
        DeliveryMode deliveryMode,
        SnapshotPolicy snapshotPolicy,

        Integer priority,
        EnumSet<ResponseField> responseFields,

        Map<String, FeederDto> feeders,
        Map<String, CheckerDto> checkers,
        Map<String, LimitsDto> limits,

        ExecutionPolicy executionPolicy,
        PayloadDto payload,

        OutputLimitsDto outputLimits,

        Language lang,
        String src,

        List<TestGroupDto> groups
) {
    private final static int DEFAULT_PRIORITY = 50;
    private final static ExecutionPolicy DEFAULT_EXECUTION_POLICY = ExecutionPolicy.STOP_ON_FIRST_FAILURE;

    // todo: callbackTrigger
    public SubmissionRequestDto {
        callbackTrigger = callbackTrigger == null ? ON_COMPLETE_ONLY : callbackTrigger;
        responseFields = responseFields == null ? EnumSet.allOf(ResponseField.class) : responseFields;
        executionPolicy = executionPolicy == null ? DEFAULT_EXECUTION_POLICY : executionPolicy;
        priority = priority == null ? DEFAULT_PRIORITY : priority;

        deliveryMode = deliveryMode == null ? DeliveryMode.BATCHED_MS_1000 : deliveryMode;
        snapshotPolicy = snapshotPolicy == null ? FULL : snapshotPolicy;
        outputLimits = outputLimits == null ? OutputLimitsDto.DEFAULT : outputLimits;
    }

}