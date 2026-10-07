package ru.tusman4ik.task.templates;

public record Criterion(int maxScore, int userScore) {

    public Criterion {
        if (userScore < 0 || userScore > maxScore) {
            throw new IllegalArgumentException(
                    "illegal criterion: maxScore=" + maxScore + ", userScore=" + userScore);
        }
    }
}
