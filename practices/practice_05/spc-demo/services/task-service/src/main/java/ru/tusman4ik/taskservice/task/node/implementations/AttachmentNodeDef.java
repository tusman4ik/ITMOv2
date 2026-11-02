package ru.tusman4ik.taskservice.task.node.implementations;

import ru.tusman4ik.taskservice.task.node.NodeDefMarker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NodeDefMarker
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AttachmentNodeDef {
}
