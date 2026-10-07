package ru.tusman4ik.task.templates;

import ru.tusman4ik.task.ui.forms.AnswerForm;

public interface FormFactory<I> {

    AnswerForm<I> create(CallCtx ctx);
}
