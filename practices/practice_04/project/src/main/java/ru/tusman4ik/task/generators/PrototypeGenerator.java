package ru.tusman4ik.task.generators;

import ru.tusman4ik.task.context.Context;

public abstract class PrototypeGenerator {
    protected Context ctx() {
        return Context.current();
    }

    protected boolean bool(){
        return ctx().random().nextBoolean();
    }
}
