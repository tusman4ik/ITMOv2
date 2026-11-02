package ru.tusman4ik.taskservice.task.generator;

import ru.tusman4ik.apicontracts.taskservice.check.CheckResult;
import ru.tusman4ik.apicontracts.taskservice.form.InputForm;
import ru.tusman4ik.taskservice.task.id.AttemptId;
import ru.tusman4ik.taskservice.task.node.Node;
import ru.tusman4ik.apicontracts.taskservice.schema.InputSchema;

import java.util.List;
import java.util.Random;

public abstract class PrototypeGenerator<I extends InputSchema> {

    public abstract InputForm<? extends I> form();

    public abstract CheckResult check(AttemptId id, I schema);

    protected int nextInt() {return ctx().random().nextInt();}

    protected Random random() {return ctx().random();}

    protected Context ctx() {return ContextHolder.current();}

}
