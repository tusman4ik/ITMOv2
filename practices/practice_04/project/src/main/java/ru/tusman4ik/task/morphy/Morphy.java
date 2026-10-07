package ru.tusman4ik.task.morphy;

import com.github.demidko.aot.morphology.MorphologyTag;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface Morphy {
    MorphologyTag getTag();

    @Getter
    @AllArgsConstructor
    enum Number implements Morphy {

        SINGULAR(MorphologyTag.Singular),
        PLURAL(MorphologyTag.Plural);

        private final MorphologyTag tag;
    }

    @Getter
    @AllArgsConstructor
    enum Case implements Morphy {

        NOMINATIVE(MorphologyTag.Nominative),           // именительный
        GENITIVE(MorphologyTag.Genitive),               // родительный
        DATIVE(MorphologyTag.Dative),                   // дательный
        ACCUSATIVE(MorphologyTag.Accusative),           // винительный
        INSTRUMENTAL(MorphologyTag.Ablative),           // творительный
        PREPOSITIONAL(MorphologyTag.Prepositional);     // предложный
//        VOCATIVE(MorphologyTag.Vocative);             // звательный ?   (please, use NOMINATIVE)
//       GENITIVE_2(MorphologyTag.SecondGenetive);      // партитив

        private final MorphologyTag tag;
    }

    @Getter
    @AllArgsConstructor
    enum Tense implements Morphy {

        PRESENT(MorphologyTag.PresentTense),
        PAST(MorphologyTag.PastTime),
        FUTURE(MorphologyTag.FutureTime);

        private final MorphologyTag tag;
    }

    @Getter
    @AllArgsConstructor
    enum Person implements Morphy {

        FIRST(MorphologyTag.FirstPerson),
        SECOND(MorphologyTag.SecondPerson),
        THIRD(MorphologyTag.ThirdPerson);

        private final MorphologyTag tag;
    }

    @Getter
    @AllArgsConstructor
    enum Degree implements Morphy {

        COMPARATIVE(MorphologyTag.Comparative),
        SUPERLATIVE(MorphologyTag.SuperlativeAdjective);

        private final MorphologyTag tag;
    }

    @Getter
    @AllArgsConstructor
    enum Gender implements Morphy {

        MASCULINE(MorphologyTag.Male),
        FEMININE(MorphologyTag.Female),
        NEUTER(MorphologyTag.NeuterGender);

        private final MorphologyTag tag;
    }

    @Getter
    @AllArgsConstructor
    enum Animacy implements Morphy {

        ANIMATE(MorphologyTag.Animated),
        INANIMATE(MorphologyTag.Inanimate);

        private final MorphologyTag tag;
    }

    @Getter
    @AllArgsConstructor
    enum PartOfSpeech implements Morphy {

        NOUN(MorphologyTag.Noun),
        VERB(MorphologyTag.Verb),
        ADJECTIVE(MorphologyTag.Adjective),
        ADVERB(MorphologyTag.Adverb),
        PRONOUN(MorphologyTag.Pronoun),
        NUMERAL(MorphologyTag.Numeral),
        PARTICIPLE(MorphologyTag.Participle),
        VERBAL_ADVERB(MorphologyTag.VerbalParticiple),
        PARTICLE(MorphologyTag.Particle),
        PREPOSITION(MorphologyTag.Pretext),
        CONJUNCTION(MorphologyTag.Union),
        INTERJECTION(MorphologyTag.Interjection);

        private final MorphologyTag tag;
    }

}
