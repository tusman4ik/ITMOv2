package ru.tusman4ik.task.nodes.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PickNodeDef {

    String path() default "";

    boolean commonDir() default false;

    int n() default 1;

    boolean distinct() default false;
}
