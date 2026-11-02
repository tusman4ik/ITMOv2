package ru.tusman4ik.apicontracts.coderunner.submission.checker;

import lombok.Builder;

import static ru.tusman4ik.apicontracts.coderunner.submission.Constants.or;

@Builder
public record TokenCheckerDto(
        Boolean caseSensitive,
        Double numericTolerance,
        String regexp
) implements CheckerDto {
    public static final String DISC = "token";

    private final static boolean DEFAULT_CASE_SENSITIVE = false;
    private final static double DEFAULT_NUMERIC_TOLERANCE = 0.;
    private final static String DEFAULT_REGEXP = "[ \n\t]+";

    public TokenCheckerDto {
        caseSensitive = or(caseSensitive, DEFAULT_CASE_SENSITIVE);
        numericTolerance = or(numericTolerance, DEFAULT_NUMERIC_TOLERANCE);
        regexp = or(regexp, DEFAULT_REGEXP);
    }
}

