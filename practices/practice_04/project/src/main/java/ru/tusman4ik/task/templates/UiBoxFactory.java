package ru.tusman4ik.task.templates;

public final class UiBoxFactory {

    private UiBoxFactory() {
    }

    public static <I> UiBox of(FormFactory<I> formFactory, CheckerFactory<I> checkerFactory) {
        return new UiBox.Impl<>(formFactory, checkerFactory);
    }

}
