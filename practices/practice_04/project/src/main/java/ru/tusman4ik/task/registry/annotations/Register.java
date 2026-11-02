package ru.tusman4ik.task.registry.annotations;

import org.springframework.stereotype.Component;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.registry.enums.Subj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Register {

    Exam exam();

    Subj subject();

    int number();

    int prototype();
}
