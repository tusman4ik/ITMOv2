package ru.tusman4ik.task.ui.forms;

public final class IntForm implements AnswerForm<Integer> {

    public static final String TYPE = "int-form-0";
    public static final FormDescriptor DESCRIPTOR = new FormDescriptor(TYPE);
    public static final IntForm INSTANCE = new IntForm();

    private IntForm() {
    }

    record Answer(Integer answer) {
    }

    @Override
    public FormDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Integer parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("answer json must be not null");
        }
        final Answer parsed;
        try {
            parsed = FormJson.MAPPER.readValue(json, Answer.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("answer must be {\"answer\": <int>}, got: " + json, e);
        }
        if (parsed == null || parsed.answer() == null) {
            throw new IllegalArgumentException("answer must be {\"answer\": <int>}, got: " + json);
        }
        return parsed.answer();
    }
}
