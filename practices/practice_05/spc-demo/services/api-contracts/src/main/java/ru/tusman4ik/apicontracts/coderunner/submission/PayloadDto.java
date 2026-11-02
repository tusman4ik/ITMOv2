package ru.tusman4ik.apicontracts.coderunner.submission;

import lombok.Builder;

import java.util.Arrays;
import java.util.Objects;

@Builder(toBuilder = true)
public record PayloadDto(
        String feeder,
        String lim,
        String checker
) {
    public static final PayloadDto EMPTY = PayloadDto.builder().build();

    public static PayloadDto propagate(PayloadDto... e) {
        if (e == null || e.length == 0 || Arrays.stream(e).allMatch(Objects::isNull)) {
            return EMPTY;
        }

        PayloadDto result = null;
        for (int i = e.length - 1; i >= 0; i--) {
            result = propagate(e[i], result);
        }
        return result;
    }

    public static PayloadDto propagate(PayloadDto cur, PayloadDto par) {
        if (cur == null && par == null) return EMPTY;
        if (cur == null) return par;
        if (par == null) return cur;

        return builder()
                .feeder(cur.feeder != null ? cur.feeder : par.feeder)
                .lim(cur.lim != null ? cur.lim : par.lim)
                .checker(cur.checker != null ? cur.checker : par.checker)
                .build();
    }

}