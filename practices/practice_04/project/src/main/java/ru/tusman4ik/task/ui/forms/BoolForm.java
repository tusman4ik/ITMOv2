package ru.tusman4ik.task.ui.forms;

/**
 * Логическое значение. Туда: {@code {"type": "bool-form-0"}}, оттуда: {@code {"answer": true}}.
 */
public final class BoolForm implements AnswerForm<Boolean> {

    public static final String TYPE = "bool-form-0";
    public static final FormDescriptor DESCRIPTOR = new FormDescriptor(TYPE);
    public static final BoolForm INSTANCE = new BoolForm();

    private BoolForm() {
    }

    record Answer(Boolean answer) {
    }

    @Override
    public FormDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Boolean parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("answer json must be not null");
        }
        final Answer parsed;
        try {
            parsed = FormJson.MAPPER.readValue(json, Answer.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("answer must be {\"answer\": <boolean>}, got: " + json, e);
        }
        if (parsed == null || parsed.answer() == null) {
            throw new IllegalArgumentException("answer must be {\"answer\": <boolean>}, got: " + json);
        }
        return parsed.answer();
    }
}
