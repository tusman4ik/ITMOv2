package ru.tusman4ik.taskservice.taskimpl.t16.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class NumberSequenceUtils {

    private NumberSequenceUtils() {
    }

    /**
     * Formats numbers separated by newlines (standard input feeder for sequence tasks).
     */
    public static String in(long... numbers) {
        return Arrays.stream(numbers)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Extracts digits in base B from least significant to most significant (index 0 is least significant digit).
     */
    public static List<Integer> digits(long n, int base) {
        if (base < 2) {
            throw new IllegalArgumentException("Base must be >= 2, got: " + base);
        }
        long val = Math.abs(n);
        if (val == 0) {
            return List.of(0);
        }
        List<Integer> result = new ArrayList<>();
        while (val > 0) {
            result.add((int) (val % base));
            val /= base;
        }
        return result;
    }

    /**
     * Extracts digits in decimal base from least significant to most significant.
     */
    public static List<Integer> digits(long n) {
        return digits(n, 10);
    }

    /**
     * Task 16-001: Sum of elements whose representation in base B ends with digit D.
     */
    public static long sumEndingWithDigit(long[] sequence, int base, int d) {
        if (base < 2) {
            throw new IllegalArgumentException("Base must be >= 2, got: " + base);
        }
        if (d < 0 || d >= base) {
            throw new IllegalArgumentException("Digit D must be in range [0, " + (base - 1) + "], got: " + d);
        }
        long sum = 0;
        for (long x : sequence) {
            if (Math.abs(x) % base == d) {
                sum += x;
            }
        }
        return sum;
    }

    /**
     * Task 16-002: Sum of elements whose base B representation penultimate digit equals D,
     * with invariant that all elements are >= B (so penultimate digit exists).
     */
    public static long sumPenultimateDigitEquals(long[] sequence, int base, int d) {
        if (base < 2) {
            throw new IllegalArgumentException("Base must be >= 2, got: " + base);
        }
        if (d < 0 || d >= base) {
            throw new IllegalArgumentException("Digit D must be in range [0, " + (base - 1) + "], got: " + d);
        }
        long sum = 0;
        for (long x : sequence) {
            if (x < base) {
                throw new IllegalArgumentException("Element " + x + " violates invariant x >= base (" + base + ")");
            }
            long penultimate = (x / base) % base;
            if (penultimate == d) {
                sum += x;
            }
        }
        return sum;
    }

    /**
     * Task 16-003: Count of digits of a single positive number X that are divisible by K.
     * Note: In base 10, digit 0 is divisible by any K (0 % K == 0).
     */
    public static int countDigitsDivisibleBy(long x, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("Divisor K must be > 0, got: " + k);
        }
        int count = 0;
        for (int digit : digits(x, 10)) {
            if (digit % k == 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Task 16-004: Sum of digits of a single positive number X that are not divisible by K.
     */
    public static int sumDigitsNotDivisibleBy(long x, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("Divisor K must be > 0, got: " + k);
        }
        int sum = 0;
        for (int digit : digits(x, 10)) {
            if (digit % k != 0) {
                sum += digit;
            }
        }
        return sum;
    }

    // --- Generation helpers for non-trivial test suites ---

    /**
     * Generates a non-trivial test suite for Task 16-001 (sum elements ending with digit D in base B).
     * Sequence terminates with 0 (which is not part of sequence, or standard OGE 16 stop flag).
     * Includes matching elements, non-matching elements, corner cases, and zero matches.
     */
    public static List<TestCase> generateSuite16_001(int base, int d, Random random) {
        List<TestCase> suite = new ArrayList<>();

        // 1. Example 1: multiple matching elements
        long[] ex1Seq = new long[]{
                generateNumberWithLastDigit(base, d, 1, 100, random),
                generateNumberNotLastDigit(base, d, 1, 100, random),
                generateNumberWithLastDigit(base, d, 1, 100, random),
                0
        };
        suite.add(createSequenceTestCase(ex1Seq, sumEndingWithDigit(trimStop(ex1Seq), base, d)));

        // 2. Example 2: single matching element
        long[] ex2Seq = new long[]{
                generateNumberNotLastDigit(base, d, 1, 100, random),
                generateNumberWithLastDigit(base, d, 1, 100, random),
                0
        };
        suite.add(createSequenceTestCase(ex2Seq, sumEndingWithDigit(trimStop(ex2Seq), base, d)));

        // 3. Corner case: no matching elements -> sum is 0
        long[] noMatch = new long[]{
                generateNumberNotLastDigit(base, d, 1, 200, random),
                generateNumberNotLastDigit(base, d, 1, 200, random),
                generateNumberNotLastDigit(base, d, 1, 200, random),
                0
        };
        suite.add(createSequenceTestCase(noMatch, 0));

        // 4. Corner case: all matching elements
        long[] allMatch = new long[]{
                generateNumberWithLastDigit(base, d, 1, 100, random),
                generateNumberWithLastDigit(base, d, 1, 100, random),
                generateNumberWithLastDigit(base, d, 1, 100, random),
                0
        };
        suite.add(createSequenceTestCase(allMatch, sumEndingWithDigit(trimStop(allMatch), base, d)));

        // 5. Corner case: minimal sequence (1 element matching + 0)
        long minMatchVal = d == 0 ? base : d;
        long[] singleMatch = new long[]{minMatchVal, 0};
        suite.add(createSequenceTestCase(singleMatch, minMatchVal));

        // 6. Corner case: minimal sequence (1 element non-matching + 0)
        long minNonMatchVal = (d == 1) ? 2 : 1;
        long[] singleNonMatch = new long[]{minNonMatchVal, 0};
        suite.add(createSequenceTestCase(singleNonMatch, 0));

        // 7-10. Larger random stress sequences
        for (int i = 0; i < 4; i++) {
            int len = 5 + random.nextInt(10);
            long[] seq = new long[len + 1];
            for (int j = 0; j < len; j++) {
                if (random.nextBoolean()) {
                    seq[j] = generateNumberWithLastDigit(base, d, 1, 1000, random);
                } else {
                    seq[j] = generateNumberNotLastDigit(base, d, 1, 1000, random);
                }
            }
            seq[len] = 0;
            suite.add(createSequenceTestCase(seq, sumEndingWithDigit(trimStop(seq), base, d)));
        }

        return suite;
    }

    /**
     * Generates a non-trivial test suite for Task 16-002 (sum elements with penultimate digit D in base B).
     * Invariant: all elements >= base. Sequence terminates with 0.
     */
    public static List<TestCase> generateSuite16_002(int base, int d, Random random) {
        List<TestCase> suite = new ArrayList<>();

        // 1. Example 1: multiple matching elements
        long[] ex1Seq = new long[]{
                generateNumberWithPenultimateDigit(base, d, random),
                generateNumberNotPenultimateDigit(base, d, random),
                generateNumberWithPenultimateDigit(base, d, random),
                0
        };
        suite.add(createSequenceTestCase(ex1Seq, sumPenultimateDigitEquals(trimStop(ex1Seq), base, d)));

        // 2. Example 2: single matching
        long[] ex2Seq = new long[]{
                generateNumberNotPenultimateDigit(base, d, random),
                generateNumberWithPenultimateDigit(base, d, random),
                0
        };
        suite.add(createSequenceTestCase(ex2Seq, sumPenultimateDigitEquals(trimStop(ex2Seq), base, d)));

        // 3. Corner case: no matching elements -> sum is 0
        long[] noMatch = new long[]{
                generateNumberNotPenultimateDigit(base, d, random),
                generateNumberNotPenultimateDigit(base, d, random),
                0
        };
        suite.add(createSequenceTestCase(noMatch, 0));

        // 4. Corner case: boundary values (minimal valid number having penultimate digit d)
        long minVal = (long) d * base + (random.nextInt(base));
        if (minVal < base) {
            // if d == 0, base <= val < 2*base has penultimate digit 0!
            minVal = (long) base + random.nextInt(base);
        }
        long[] boundarySeq = new long[]{minVal, 0};
        suite.add(createSequenceTestCase(boundarySeq, sumPenultimateDigitEquals(trimStop(boundarySeq), base, d)));

        // 5-10. Larger sequences
        for (int i = 0; i < 6; i++) {
            int len = 4 + random.nextInt(8);
            long[] seq = new long[len + 1];
            for (int j = 0; j < len; j++) {
                if (random.nextBoolean()) {
                    seq[j] = generateNumberWithPenultimateDigit(base, d, random);
                } else {
                    seq[j] = generateNumberNotPenultimateDigit(base, d, random);
                }
            }
            seq[len] = 0;
            suite.add(createSequenceTestCase(seq, sumPenultimateDigitEquals(trimStop(seq), base, d)));
        }

        return suite;
    }

    /**
     * Generates a non-trivial test suite for Task 16-003 (count digits of X divisible by K).
     */
    public static List<TestCase> generateSuite16_003(int k, Random random) {
        List<TestCase> suite = new ArrayList<>();

        // 1. Examples
        long ex1 = generateNumberWithDivisibleDigits(k, 2, 2, random);
        suite.add(new TestCase(String.valueOf(ex1), String.valueOf(countDigitsDivisibleBy(ex1, k))));

        long ex2 = generateNumberWithDivisibleDigits(k, 1, 3, random);
        suite.add(new TestCase(String.valueOf(ex2), String.valueOf(countDigitsDivisibleBy(ex2, k))));

        // 2. Corner case: single digit divisible
        int singleDiv = (k <= 9) ? k : 0;
        suite.add(new TestCase(String.valueOf(singleDiv == 0 ? k % 10 : singleDiv),
                String.valueOf(countDigitsDivisibleBy(singleDiv == 0 ? k % 10 : singleDiv, k))));

        // 3. Corner case: single digit not divisible
        int nonDiv = 1;
        while (nonDiv % k == 0) {
            nonDiv++;
        }
        suite.add(new TestCase(String.valueOf(nonDiv), "0"));

        // 4. Corner case: number with 0 (0 is divisible by k)
        long withZero = 1000 + random.nextInt(9000);
        suite.add(new TestCase(String.valueOf(withZero), String.valueOf(countDigitsDivisibleBy(withZero, k))));

        // 5. Corner case: large number (up to 9-10 digits)
        long bigNum = 1_000_000_000L + random.nextInt(1_000_000_000);
        suite.add(new TestCase(String.valueOf(bigNum), String.valueOf(countDigitsDivisibleBy(bigNum, k))));

        // 6-10. Random tests
        for (int i = 0; i < 5; i++) {
            long val = 10 + random.nextInt(999_999);
            suite.add(new TestCase(String.valueOf(val), String.valueOf(countDigitsDivisibleBy(val, k))));
        }

        return suite;
    }

    /**
     * Generates a non-trivial test suite for Task 16-004 (sum digits of X not divisible by K).
     */
    public static List<TestCase> generateSuite16_004(int k, Random random) {
        List<TestCase> suite = new ArrayList<>();

        // 1. Examples
        long ex1 = generateNumberWithDivisibleDigits(k, 2, 2, random);
        suite.add(new TestCase(String.valueOf(ex1), String.valueOf(sumDigitsNotDivisibleBy(ex1, k))));

        long ex2 = generateNumberWithDivisibleDigits(k, 3, 1, random);
        suite.add(new TestCase(String.valueOf(ex2), String.valueOf(sumDigitsNotDivisibleBy(ex2, k))));

        // 2. Corner case: all digits divisible -> sum is 0
        long allDiv = buildNumberFromDigits(getDivisibleDigits(k, false), 4, random);
        if (allDiv > 0) {
            suite.add(new TestCase(String.valueOf(allDiv), "0"));
        }

        // 3. Corner case: no digits divisible -> sum is all digits
        List<Integer> nonDivDigits = getNonDivisibleDigits(k);
        if (!nonDivDigits.isEmpty()) {
            long allNonDiv = buildNumberFromDigits(nonDivDigits, 4, random);
            suite.add(new TestCase(String.valueOf(allNonDiv), String.valueOf(sumDigitsNotDivisibleBy(allNonDiv, k))));
        }

        // 4. Corner case: contains 0 (0 is divisible, should not contribute to sum)
        long withZero = 1002 + random.nextInt(8000);
        suite.add(new TestCase(String.valueOf(withZero), String.valueOf(sumDigitsNotDivisibleBy(withZero, k))));

        // 5. Corner case: large number
        long bigNum = 1_000_000_000L + random.nextInt(1_000_000_000);
        suite.add(new TestCase(String.valueOf(bigNum), String.valueOf(sumDigitsNotDivisibleBy(bigNum, k))));

        // 6-10. Random tests
        for (int i = 0; i < 5; i++) {
            long val = 10 + random.nextInt(999_999);
            suite.add(new TestCase(String.valueOf(val), String.valueOf(sumDigitsNotDivisibleBy(val, k))));
        }

        return suite;
    }

    // --- Private helper utilities ---

    private static TestCase createSequenceTestCase(long[] seq, long expected) {
        return new TestCase(in(seq), String.valueOf(expected));
    }

    private static long[] trimStop(long[] seq) {
        if (seq.length > 0 && seq[seq.length - 1] == 0) {
            return Arrays.copyOf(seq, seq.length - 1);
        }
        return seq;
    }

    private static long generateNumberWithLastDigit(int base, int d, int minPrefix, int maxPrefix, Random random) {
        int prefix = minPrefix + random.nextInt(Math.max(1, maxPrefix - minPrefix + 1));
        return (long) prefix * base + d;
    }

    private static long generateNumberNotLastDigit(int base, int d, int minPrefix, int maxPrefix, Random random) {
        int prefix = minPrefix + random.nextInt(Math.max(1, maxPrefix - minPrefix + 1));
        int otherDigit = random.nextInt(base);
        if (otherDigit == d) {
            otherDigit = (d + 1) % base;
        }
        return (long) prefix * base + otherDigit;
    }

    private static long generateNumberWithPenultimateDigit(int base, int d, Random random) {
        int higher = 1 + random.nextInt(50);
        int last = random.nextInt(base);
        return ((long) higher * base + d) * base + last;
    }

    private static long generateNumberNotPenultimateDigit(int base, int d, Random random) {
        int higher = 1 + random.nextInt(50);
        int pen = random.nextInt(base);
        if (pen == d) {
            pen = (d + 1) % base;
        }
        int last = random.nextInt(base);
        return ((long) higher * base + pen) * base + last;
    }

    private static List<Integer> getDivisibleDigits(int k, boolean allowZeroFirst) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            if (i % k == 0) {
                list.add(i);
            }
        }
        return list;
    }

    private static List<Integer> getNonDivisibleDigits(int k) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            if (i % k != 0) {
                list.add(i);
            }
        }
        return list;
    }

    private static long buildNumberFromDigits(List<Integer> digits, int count, Random random) {
        List<Integer> nonZeroDigits = digits.stream().filter(d -> d > 0).toList();
        if (nonZeroDigits.isEmpty()) {
            return 0;
        }
        long val = nonZeroDigits.get(random.nextInt(nonZeroDigits.size()));
        for (int i = 1; i < count; i++) {
            val = val * 10 + digits.get(random.nextInt(digits.size()));
        }
        return val;
    }

    private static long generateNumberWithDivisibleDigits(int k, int divCount, int nonDivCount, Random random) {
        List<Integer> div = getDivisibleDigits(k, false);
        List<Integer> nonDiv = getNonDivisibleDigits(k);
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < divCount; i++) {
            chosen.add(div.get(random.nextInt(div.size())));
        }
        for (int i = 0; i < nonDivCount; i++) {
            if (!nonDiv.isEmpty()) {
                chosen.add(nonDiv.get(random.nextInt(nonDiv.size())));
            }
        }
        Collections.shuffle(chosen, random);
        if (chosen.get(0) == 0) {
            for (int i = 1; i < chosen.size(); i++) {
                if (chosen.get(i) != 0) {
                    chosen.set(0, chosen.get(i));
                    chosen.set(i, 0);
                    break;
                }
            }
            if (chosen.get(0) == 0) {
                chosen.set(0, 1);
            }
        }
        long num = 0;
        for (int d : chosen) {
            num = num * 10 + d;
        }
        return num;
    }
}
