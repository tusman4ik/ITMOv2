package ru.tusman4ik.task.ui.forms;

/**
 * Форма ответа: умеет отдать свой дескриптор для фронта
 * и распарсить ответ ученика из JSON-конверта {@code {"answer": ...}}.
 *
 * @param <I> внутренний тип ответа.
 */
public interface AnswerForm<I> {

    FormDescriptor descriptor();

    I parse(String json);
}
