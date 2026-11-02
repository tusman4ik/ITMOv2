package ru.tusman4ik.taskservice.task.node;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Node<T> {
    T get();
}
