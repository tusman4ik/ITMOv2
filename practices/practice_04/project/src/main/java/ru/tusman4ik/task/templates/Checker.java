package ru.tusman4ik.task.templates;

public interface Checker<I> {

    CheckResult score(I answer);
}
