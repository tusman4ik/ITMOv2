package ru.tusman4ik.task.registry;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import ru.tusman4ik.task.registry.annotations.Register;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.registry.enums.Subj;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Builder
public record PrototypeId(
        @NotNull Exam exam,
        @NotNull Subj subject,
        @NotNull @Min(0) @Max(99) Integer number,
        @NotNull @Min(0) @Max(99) Integer prototype
) {

    public static PrototypeId from(Register register) {
        if (register.exam() == null) {
            throw new IllegalArgumentException("exam must be not null");
        }
        if (register.subject() == null) {
            throw new IllegalArgumentException("subject must be not null");
        }
        if (register.number() < 0 || register.number() > 99) {
            throw new IllegalArgumentException("number out of range [0, 99]: " + register.number());
        }
        if (register.prototype() < 0 || register.prototype() > 99) {
            throw new IllegalArgumentException("prototype out of range [0, 99]: " + register.prototype());
        }
        return builder()
                .exam(register.exam())
                .subject(register.subject())
                .number(register.number())
                .prototype(register.prototype())
                .build();
    }

    public String path() {
        return String.format("%02d-%02d", number, prototype);
    }


    @Override
    public @NonNull String toString() {
        return String.format("[%s-%s-%d-%d]", exam, subject, number, prototype);
    }
}
