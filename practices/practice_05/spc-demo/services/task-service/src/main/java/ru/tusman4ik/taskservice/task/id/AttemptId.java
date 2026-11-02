package ru.tusman4ik.taskservice.task.id;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import ru.tusman4ik.apicontracts.taskservice.id.AttemptIdDto;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;

import java.util.Objects;
import java.util.UUID;

@Builder
public record AttemptId(
        InstanceId instanceId,
        UUID attempt
) {

    @Override
    public @NonNull String toString() {
        return "%s_%s".formatted(instanceId, attempt);
    }

    public AttemptId {
        Objects.requireNonNull(instanceId, "InstanceId must be not null");
        Objects.requireNonNull(attempt, "Attempt must be not null");
    }

    public PrototypeId prototypeId() {
        return instanceId.prototypeId();
    }

    public AttemptIdDto toDto() {
        return new AttemptIdDto(
                exam(), subject(), number(), prototypeIdx(), seed(), fingerprint(), attempt);
    }

    public static AttemptId from(AttemptIdDto dto) {
        return AttemptId.builder()
                .instanceId(new InstanceId(
                        new PrototypeId(dto.exam(), dto.subject(), dto.number(), dto.prototypeIdx()),
                        dto.seed(),
                        dto.fingerprint()))
                .attempt(dto.attempt())
                .build();
    }

    public Exam exam() {
        return instanceId.exam();
    }

    public ru.tusman4ik.apicontracts.taskservice.task.enums.Subject subject() {
        return instanceId.subject();
    }

    public int number() {
        return instanceId.number();
    }

    public int prototypeIdx() {
        return instanceId.prototypeIdx();
    }

    public long seed() {
        return instanceId.seed();
    }

    public long fingerprint() {
        return instanceId.fingerprint();
    }
}
