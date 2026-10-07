package ru.tusman4ik.task.morphy;

import com.github.demidko.aot.morphology.MorphologyTag;

import java.util.Arrays;
import java.util.List;

public record WordForm(String normalForm, List<MorphologyTag> tags) {

    public WordForm {
        if (normalForm == null || normalForm.isBlank()) {
            throw new IllegalArgumentException("normalForm must be not blank");
        }
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("tags must be not empty for " + normalForm);
        }
        tags = List.copyOf(tags);
    }

    public static WordForm of(String normalForm, Morphy... features) {
        return new WordForm(normalForm, Arrays.stream(features).map(Morphy::getTag).toList());
    }

    public static WordForm noun(String nf, Morphy.Number number, Morphy.Case wordCase, Morphy.Animacy animacy) {
        return of(nf, Morphy.PartOfSpeech.NOUN, number, wordCase, animacy);
    }
}
