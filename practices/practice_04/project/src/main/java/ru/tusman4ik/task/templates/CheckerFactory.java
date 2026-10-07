package ru.tusman4ik.task.templates;

public interface CheckerFactory<I> {

    Checker<I> create(CallCtx ctx);
}
