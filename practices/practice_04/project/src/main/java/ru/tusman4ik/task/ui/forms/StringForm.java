package ru.tusman4ik.task.ui.forms;

/**
 * Строка как есть, пустая валидна.
 * Туда: {@code {"type": "string-form-0"}}, оттуда: {@code {"answer": "text"}}.
 */
public final class StringForm implements AnswerForm<String> {

    public static final String TYPE = "string-form-0";
    public static final FormDescriptor DESCRIPTOR = new FormDescriptor(TYPE);
    public static final StringForm INSTANCE = new StringForm();

    private StringForm() {
    }

    record Answer(String answer) {
    }

    @Override
    public FormDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("answer json must be not null");
        }
        final Answer parsed;
        try {
            parsed = FormJson.MAPPER.readValue(json, Answer.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("answer must be {\"answer\": \"<string>\"}, got: " + json, e);
        }
        if (parsed == null || parsed.answer() == null) {
            throw new IllegalArgumentException("answer must be {\"answer\": \"<string>\"}, got: " + json);
        }
        return parsed.answer();
    }
}
