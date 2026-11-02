package ru.tusman4ik.taskservice.task.generator;

import org.springframework.stereotype.Component;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Exam;
import ru.tusman4ik.apicontracts.taskservice.task.enums.Subject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Register {

    Exam exam();

    Subject subject();

    int number();

    int prototypeIdx();
}
