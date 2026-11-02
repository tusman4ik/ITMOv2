package ru.tusman4ik.task.registry;

import org.jspecify.annotations.NonNull;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.registry.enums.Subj;

public record TemplateId(
        Exam exam,
        Subj subject,
        int number,
        int prototype,
        int idx
) {

    public TemplateId(PrototypeId id, int idx) {
        if (id == null) {
            throw new IllegalArgumentException("prototype id must be not null");
        }
        if (idx < 0 || idx > 99) {
            throw new IllegalArgumentException("idx out of range [0, 99]: " + idx);
        }
        this(id.exam(), id.subject(), id.number(), id.prototype(), idx);
    }

    public String path() {
        return new PrototypeId(exam, subject, number, prototype).path()
                + "/templates/" + String.format("%02d", idx);
    }

    @Override
    public @NonNull String toString() {
        return String.format("[%s-%s-%d-%d-%d]", exam, subject, number, prototype, idx);
    }
}
