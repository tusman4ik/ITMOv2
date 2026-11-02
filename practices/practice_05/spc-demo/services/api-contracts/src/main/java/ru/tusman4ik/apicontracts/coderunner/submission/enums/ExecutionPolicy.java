package ru.tusman4ik.apicontracts.coderunner.submission.enums;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Defines how the test runner behaves when tests fail")
public enum ExecutionPolicy {

    RUN_ALL,
    STOP_ON_FIRST_FAILURE,

    STOP_GROUP_ON_FIRST_FAILED,
    COMPLETE_GROUP_ON_FAILURE

}