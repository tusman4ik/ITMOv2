package ru.tusman4ik.task.templates;

import ru.tusman4ik.task.ui.forms.AnswerForm;

public interface UiBox {

    AnswerForm<?> form(CallCtx ctx);

    CheckResult check(CallCtx ctx, String json);


    record Impl<I>(FormFactory<I> formFactory, CheckerFactory<I> checkerFactory) implements UiBox {

        @Override
        public AnswerForm<?> form(CallCtx ctx) {
            return formFactory.create(ctx);
        }

        @Override
        public CheckResult check(CallCtx ctx, String json) {
            AnswerForm<I> form = formFactory.create(ctx);
            I internal = form.parse(json);
            return checkerFactory.create(ctx).score(internal);
        }
    }
}
