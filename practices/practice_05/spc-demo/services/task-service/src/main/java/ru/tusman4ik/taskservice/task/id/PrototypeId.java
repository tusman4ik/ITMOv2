package ru.tusman4ik.taskservice.task.id;

import ru.tusman4ik.apicontracts.taskservice.id.PrototypeIdDto;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;

import java.util.Objects;

public record PrototypeId(
        Exam exam,
        Subject subject,
        int number,
        int prototypeIdx
) {

    public PrototypeId {
        Objects.requireNonNull(exam, "Exam must be not null");
        Objects.requireNonNull(subject, "Subject must be not null");

        if (number < 1 || number > 99) {
            throw new IllegalArgumentException(
                    "Number must be between 1 and 99, got: " + number
            );
        }
        if (prototypeIdx < 0 || prototypeIdx > 999) {
            throw new IllegalArgumentException(
                    "PrototypeIdx must be between 0 and 999, got: " + prototypeIdx
            );
        }
    }

    public InstanceId getInstance(long seed, long fingerprint) {
        return InstanceId.builder()
                .prototypeId(this)
                .seed(seed)
                .fingerprint(fingerprint)
                .build();
    }

    public String path() {
        return "%02d-%03d".formatted(number, prototypeIdx);
    }

    public PrototypeIdDto toDto() {
        return new PrototypeIdDto(exam, subject, number, prototypeIdx);
    }

    public static PrototypeId from(PrototypeIdDto dto) {
        return new PrototypeId(dto.exam(), dto.subject(), dto.number(), dto.prototypeIdx());
    }


}
