package ru.tusman4ik.coderunner.services.sandbox;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Status;
import ru.tusman4ik.coderunner.exceptions.SandboxException;
import ru.tusman4ik.coderunner.models.domain.results.SandboxInvocationResult;
import ru.tusman4ik.coderunner.utils.ProcessInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.tusman4ik.coderunner.utils.CommonUtils.or;


@Slf4j
public final class SandboxInvocationAnalyzer {
    private SandboxInvocationAnalyzer() {}

    public static SandboxInvocationResult analyze(Path meta, ProcessInfo processInfo) throws IOException {
        Map<String, String> result = readMeta(meta);


        if (processInfo.exitCode() == 2) {


            return SandboxInvocationResult.builder()
                    .status(Status.IE)
                    .stderr(processInfo.stderr())
                    .build();
        }

        Double cpuTimeSec = Double.valueOf(result.get("time"));
        Double realTimeSec = Double.valueOf(result.get("time-wall"));
        Integer memoryKb = Integer.valueOf(result.get("cg-mem"));

        Status status = getStatus(result);

        Integer exitCode = Integer.valueOf(or(result.get("exitcode"), "-1"));

        return SandboxInvocationResult.builder()
                .status(status)
                .cpuTimeSec(cpuTimeSec)
                .realTimeSec(realTimeSec)
                .memoryKb(memoryKb)
                .exitCode(exitCode)
                .stderr(processInfo.stderr().stripTrailing())
                .build();
    }

    private static Map<String, String> readMeta(Path meta) throws IOException {
        if (!Files.exists(meta)) {
            log.error("Meta analyzer error: meta path not found: {}", meta);
            throw new SandboxException("Meta path not found");
        }
        log.trace("Meta file:");
        return Files.readAllLines(meta).stream()
                .filter(s -> !s.isBlank())
                .peek(log::trace)
                .map(str -> str.split(":", 2))
                .map(arr -> new String[]{arr[0].strip(), arr[1].strip()})
                .collect(Collectors.toMap(
                                arr -> arr[0],
                                arr -> arr[1]
                        )
                );
    }

    private static Status getStatus(Map<String, String> meta) {
        if ("1".equals(meta.get("cg-oom-killed"))) {
            return Status.ML;
        }

        String status = meta.get("status");

        if (status != null) {
            switch (status) {
                case "TO":
                    return Status.TL;
                case "SG":
                    if ("9".equals(meta.get("exitsig"))) {
                        return Status.ML;
                    }
                    if ("25".equals(meta.get("exitsig"))) {
                        return Status.OL;
                    }
                    return Status.RE;
                case "RE":
                    if ("127".equals(meta.get("exitcode"))) {
                        return Status.IE;
                    }
                    return Status.RE;
                case "FO":
                case "XX":
                    return Status.IE;
            }
        }

        String exitcode = meta.get("exitcode");
        if ("0".equals(exitcode)) {
            return Status.OK;
        }

        return Status.RE;
    }

}
