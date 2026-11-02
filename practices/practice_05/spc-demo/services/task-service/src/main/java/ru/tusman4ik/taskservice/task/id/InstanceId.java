package ru.tusman4ik.taskservice.task.id;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import ru.tusman4ik.apicontracts.taskservice.id.InstanceIdDto;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;

import java.util.Objects;

@Builder
public record InstanceId(
        PrototypeId prototypeId,
        long seed,
        long fingerprint
) {


    @Override
    public @NonNull String toString() {
        return "%s_%s_%02d_%03d_%016X_%016X".formatted(
                prototypeId.exam(),
                prototypeId.subject(),
                prototypeId.number(),
                prototypeId.prototypeIdx(),
                seed,
                fingerprint
        );
    }

    public InstanceId {
        Objects.requireNonNull(prototypeId, "PrototypeId must be not null");
    }

    public InstanceIdDto toDto() {
        return new InstanceIdDto(exam(), subject(), number(), prototypeIdx(), seed, fingerprint);
    }

    public static InstanceId from(InstanceIdDto dto) {
        PrototypeId prototype = new PrototypeId(dto.exam(), dto.subject(), dto.number(), dto.prototypeIdx());
        return InstanceId.builder()
                .prototypeId(prototype)
                .seed(dto.seed())
                .fingerprint(dto.fingerprint())
                .build();
    }


    public Exam exam() {
        return prototypeId.exam();
    }

    public Subject subject() {
        return prototypeId.subject();
    }

    public int number() {
        return prototypeId.number();
    }

    public int prototypeIdx() {
        return prototypeId.prototypeIdx();
    }

}
