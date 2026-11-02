package ru.tusman4ik.coderunner.openapi;

import ru.tusman4ik.apicontracts.coderunner.submission.*;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.TokenCheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Language;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.CompositeFeederDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.TextFeederDto;

import java.util.List;
import java.util.Map;

import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Target.EXP;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Target.STDIN;

public final class OpenApiExampleFactory {

    private OpenApiExampleFactory() {}

    public static SubmissionRequestDto submissionRequest() {
        return SubmissionRequestDto.builder()
                .id(null) //todo
                .lang(Language.PYTHON)
                .callbackUrl("https://webhook.site/a2120bcc-1242-4a8c-ba2b-01f67700899c")
                .feeders(Map.of(
                        "c1",
                        CompositeFeederDto.builder().feeders(List.of(
                                TextFeederDto.builder().stdin("0.16666666666").target(EXP).build(),
                                TextFeederDto.builder().stdin("2 12").target(STDIN).build()
                        )).build()
                ))
                .limits(Map.of(
                        "def",
                        LimitsDto.MIN_LIM
                ))
                .checkers(Map.of(
                        "def",
                        TokenCheckerDto.builder().numericTolerance(0.000001).build()
                ))
                .src("""
                        a, b = map(int, input().split())
                        print(a / b)
                        """)
                .groups(List.of(TestGroupDto.builder()
                        .id("tg-1")
                        .tests(List.of(
                                TestCaseDto.builder()
                                        .id("tc-1")
                                        .payload(PayloadDto.builder()
                                                .feeder("c1")
                                                .lim("def")
                                                .checker("def")
                                                .build()
                                        )
                                        .build()
                        ))
                        .build()
                ))
                .build();

    }

}
