package ru.tusman4ik.task.ui.forms;

public final class DoubleForm implements AnswerForm<Double> {

    public static final String TYPE = "double-form-0";
    public static final FormDescriptor DESCRIPTOR = new FormDescriptor(TYPE);
    public static final DoubleForm INSTANCE = new DoubleForm();

    private DoubleForm() {
    }

    record Answer(Double answer) {
    }

    @Override
    public FormDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Double parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("answer json must be not null");
        }
        final Answer parsed;
        try {
            parsed = FormJson.MAPPER.readValue(json, Answer.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("answer must be {\"answer\": <double>}, got: " + json, e);
        }
        if (parsed == null || parsed.answer() == null) {
            throw new IllegalArgumentException("answer must be {\"answer\": <double>}, got: " + json);
        }
        return parsed.answer();
    }
}
