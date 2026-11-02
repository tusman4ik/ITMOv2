package ru.tusman4ik.apicontracts.coderunner.submission.enums;

import lombok.Getter;

@Getter
public enum MountMode {
    RW("rw"),
    NO_EXEC("noexec");

    private final String value;

    MountMode(String value) {
        this.value = value;
    }

}
