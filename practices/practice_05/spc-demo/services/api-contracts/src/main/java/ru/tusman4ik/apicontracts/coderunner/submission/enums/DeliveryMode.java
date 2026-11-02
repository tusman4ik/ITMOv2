package ru.tusman4ik.apicontracts.coderunner.submission.enums;

import lombok.Getter;

@Getter
public enum DeliveryMode {
//    IMMEDIATE(0),
    BATCHED_MS_100(100),
    BATCHED_MS_500(500),
    BATCHED_MS_1000(1000),
    BATCHED_MS_2000(2000);

    private final int interval;

    DeliveryMode(int intervalMs) {this.interval = intervalMs;}
}
