package ru.tusman4ik.taskservice.taskimpl.t16.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumberSequenceUtilsTest {

    @Test
    void testInFormatting() {
        assertThat(NumberSequenceUtils.in(1, 2, 3)).isEqualTo("1\n2\n3");
        assertThat(NumberSequenceUtils.in(42)).isEqualTo("42");
        assertThat(NumberSequenceUtils.in()).isEqualTo("");
    }

    @Test
    void testDigitsExtraction() {
        // Base 10
        assertThat(NumberSequenceUtils.digits(0)).containsExactly(0);
        assertThat(NumberSequenceUtils.digits(7)).containsExactly(7);
        assertThat(NumberSequenceUtils.digits(1234)).containsExactly(4, 3, 2, 1);
        assertThat(NumberSequenceUtils.digits(-1234)).containsExactly(4, 3, 2, 1);

        // Base 2
        assertThat(NumberSequenceUtils.digits(5, 2)).containsExactly(1, 0, 1);
        assertThat(NumberSequenceUtils.digits(0, 2)).containsExactly(0);

        // Base 8
        assertThat(NumberSequenceUtils.digits(65, 8)).containsExactly(1, 0, 1); // 65 = 1 * 64 + 0 * 8 + 1

        assertThatThrownBy(() -> NumberSequenceUtils.digits(10, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSumEndingWithDigit_16_001() {
        long[] seq = {12, 25, 42, 102, 33};
        // Base 10, digit 2 -> 12 + 42 + 102 = 156
        assertThat(NumberSequenceUtils.sumEndingWithDigit(seq, 10, 2)).isEqualTo(156);

        // Base 8: 10 in octal is 12 (ends with 2)
        // 18 in octal is 22 (ends with 2)
        long[] octSeq = {10, 18, 11};
        assertThat(NumberSequenceUtils.sumEndingWithDigit(octSeq, 8, 2)).isEqualTo(28);

        // No matches
        assertThat(NumberSequenceUtils.sumEndingWithDigit(seq, 10, 9)).isEqualTo(0);

        // Negative/invalid checks
        assertThatThrownBy(() -> NumberSequenceUtils.sumEndingWithDigit(seq, 1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberSequenceUtils.sumEndingWithDigit(seq, 10, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSumPenultimateDigitEquals_16_002() {
        // Base 10:
        // 123 -> penultimate is 2
        // 425 -> penultimate is 2
        // 530 -> penultimate is 3
        long[] seq = {123, 425, 530};
        assertThat(NumberSequenceUtils.sumPenultimateDigitEquals(seq, 10, 2)).isEqualTo(548);
        assertThat(NumberSequenceUtils.sumPenultimateDigitEquals(seq, 10, 3)).isEqualTo(530);
        assertThat(NumberSequenceUtils.sumPenultimateDigitEquals(seq, 10, 9)).isEqualTo(0);

        // Invariant check: element < base throws exception
        long[] invalidSeq = {9, 120};
        assertThatThrownBy(() -> NumberSequenceUtils.sumPenultimateDigitEquals(invalidSeq, 10, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("violates invariant x >= base");
    }

    @Test
    void testCountDigitsDivisibleBy_16_003() {
        // 1024, k = 2:
        // digits: 4 (div), 2 (div), 0 (div by 2), 1 (not div) -> 3
        assertThat(NumberSequenceUtils.countDigitsDivisibleBy(1024, 2)).isEqualTo(3);

        // 1357, k = 2 -> 0
        assertThat(NumberSequenceUtils.countDigitsDivisibleBy(1357, 2)).isEqualTo(0);

        // 369, k = 3: 3, 6, 9 -> 3
        assertThat(NumberSequenceUtils.countDigitsDivisibleBy(369, 3)).isEqualTo(3);

        assertThatThrownBy(() -> NumberSequenceUtils.countDigitsDivisibleBy(123, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSumDigitsNotDivisibleBy_16_004() {
        // 1234, k = 2:
        // digits: 1 (not div), 2 (div), 3 (not div), 4 (div)
        // sum = 1 + 3 = 4
        assertThat(NumberSequenceUtils.sumDigitsNotDivisibleBy(1234, 2)).isEqualTo(4);

        // 2468, k = 2 -> 0
        assertThat(NumberSequenceUtils.sumDigitsNotDivisibleBy(2468, 2)).isEqualTo(0);

        // 102, k = 2:
        // digits: 1 (not div), 0 (div by 2), 2 (div by 2) -> 1
        assertThat(NumberSequenceUtils.sumDigitsNotDivisibleBy(102, 2)).isEqualTo(1);

        assertThatThrownBy(() -> NumberSequenceUtils.sumDigitsNotDivisibleBy(123, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGenerateSuite16_001() {
        Random rnd = new Random(42);
        List<TestCase> suite = NumberSequenceUtils.generateSuite16_001(8, 3, rnd);
        assertThat(suite).isNotEmpty();
        assertThat(suite.size()).isGreaterThanOrEqualTo(6);

        for (TestCase tc : suite) {
            String in = tc.in();
            String[] lines = in.split("\n");
            assertThat(lines[lines.length - 1]).isEqualTo("0");

            long expected = Long.parseLong(tc.expected());
            long[] numbers = new long[lines.length - 1];
            for (int i = 0; i < lines.length - 1; i++) {
                numbers[i] = Long.parseLong(lines[i]);
            }
            assertThat(NumberSequenceUtils.sumEndingWithDigit(numbers, 8, 3)).isEqualTo(expected);
        }
    }

    @Test
    void testGenerateSuite16_002() {
        Random rnd = new Random(42);
        List<TestCase> suite = NumberSequenceUtils.generateSuite16_002(10, 4, rnd);
        assertThat(suite).isNotEmpty();
        assertThat(suite.size()).isGreaterThanOrEqualTo(5);

        for (TestCase tc : suite) {
            String in = tc.in();
            String[] lines = in.split("\n");
            assertThat(lines[lines.length - 1]).isEqualTo("0");

            long expected = Long.parseLong(tc.expected());
            long[] numbers = new long[lines.length - 1];
            for (int i = 0; i < lines.length - 1; i++) {
                numbers[i] = Long.parseLong(lines[i]);
            }
            assertThat(NumberSequenceUtils.sumPenultimateDigitEquals(numbers, 10, 4)).isEqualTo(expected);
        }
    }

    @Test
    void testGenerateSuite16_003() {
        Random rnd = new Random(42);
        List<TestCase> suite = NumberSequenceUtils.generateSuite16_003(3, rnd);
        assertThat(suite).isNotEmpty();

        for (TestCase tc : suite) {
            long x = Long.parseLong(tc.in());
            int exp = Integer.parseInt(tc.expected());
            assertThat(NumberSequenceUtils.countDigitsDivisibleBy(x, 3)).isEqualTo(exp);
        }
    }

    @Test
    void testGenerateSuite16_004() {
        Random rnd = new Random(42);
        List<TestCase> suite = NumberSequenceUtils.generateSuite16_004(2, rnd);
        assertThat(suite).isNotEmpty();

        for (TestCase tc : suite) {
            long x = Long.parseLong(tc.in());
            int exp = Integer.parseInt(tc.expected());
            assertThat(NumberSequenceUtils.sumDigitsNotDivisibleBy(x, 2)).isEqualTo(exp);
        }
    }
}
