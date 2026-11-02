package ru.tusman4ik.coderunner;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.*;
import ru.tusman4ik.apicontracts.coderunner.submission.checker.TokenCheckerDto;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Language;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.CompositeFeederDto;
import ru.tusman4ik.apicontracts.coderunner.submission.feeder.TextFeederDto;
import ru.tusman4ik.coderunner.models.mappers.requests.submission.SubmissionRequestMapper;
import ru.tusman4ik.coderunner.services.SubmissionService;

import java.util.List;
import java.util.Map;

import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Target.EXP;
import static ru.tusman4ik.apicontracts.coderunner.submission.enums.Target.STDIN;

@Slf4j
public class Showroom {

    static void main(String[] args) throws Exception {

        logic();
    }

    private static void logic() throws InterruptedException {
        var service = new SubmissionService();

        SubmissionRequestDto dto = SubmissionRequestDto.builder()
                .id(null) // todo
                .lang(Language.PYTHON)
                .callbackUrl("https://google.com")
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

        service.submit(SubmissionRequestMapper.fromDto(dto));


        Thread.sleep(1000000);
        service.shutdown();
    }

}
