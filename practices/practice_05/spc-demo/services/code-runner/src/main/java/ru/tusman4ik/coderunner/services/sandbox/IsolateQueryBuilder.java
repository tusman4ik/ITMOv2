package ru.tusman4ik.coderunner.services.sandbox;

import lombok.extern.slf4j.Slf4j;
import ru.tusman4ik.apicontracts.coderunner.submission.domain.limits.Limits;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.MountMode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public final class IsolateQueryBuilder {

    private final List<String> cmd = new ArrayList<>();

    private IsolateQueryBuilder() {
    }

    public static ProcessBuilder init(int id) {
        return new ProcessBuilder(
                "isolate", "--cg", "--box-id", String.valueOf(id), "--init");
    }

    public static IsolateQueryBuilder run(int id) {
        IsolateQueryBuilder instance = new IsolateQueryBuilder();

        instance.cmd.add("isolate");
        instance.cmd.add("--box-id");
        instance.cmd.add(String.valueOf(id));
        instance.cmd.add("--run");
        instance.cmd.add("--cg");

        return instance;
    }

    public IsolateQueryBuilder withLimits(Limits limits) {
        if (limits.cpuTimeLimitSec() != null)
            cmd.add(String.format(Locale.ROOT, "--time=%.3f", limits.cpuTimeLimitSec()));
        if (limits.realTimeLimitSec() != null)
            cmd.add(String.format(Locale.ROOT, "--wall-time=%.3f", limits.realTimeLimitSec()));
        if (limits.memoryLimitKb() != null)
            cmd.add("--cg-mem=" + limits.memoryLimitKb());
        if (limits.fileSizeKb() != null)
            cmd.add("--fsize=" + limits.fileSizeKb());
        if (limits.processCnt() != null)
            cmd.add("--processes=" + limits.processCnt());

        return this;
    }

    public IsolateQueryBuilder withStdin(String stdin) {
        if (stdin != null) cmd.add("--stdin=" + stdin);
        return this;
    }

    public IsolateQueryBuilder withStdout(String stdout) {
        if (stdout != null) cmd.add("--stdout=" + stdout);
        return this;
    }
    public IsolateQueryBuilder withStderr(String stderr) {
        if (stderr != null) cmd.add("--stderr=" + stderr);
        return this;
    }

    public IsolateQueryBuilder withMeta(String meta) {
        if (meta != null) cmd.add("--meta=" + meta);
        return this;
    }

    public ProcessBuilder withCommand(String... args){
        return withCommand(List.of(args));
    }

    public ProcessBuilder withCommand(List<String> command) {
        cmd.add("--");
        cmd.addAll(command);

        log.trace("Run sandbox with command: '{}'", String.join(" ", cmd));
        return new ProcessBuilder(cmd);
    }


    public IsolateQueryBuilder withMount(Path sandboxPath, Path hostPath, MountMode... modes) {
        cmd.add(MountArg.of(sandboxPath, hostPath, modes));
        return this;
    }

    public static ProcessBuilder cleanup(int id) {
        return new ProcessBuilder("isolate",
                "--box-id", String.valueOf(id),
                "--cg", "--cleanup");
    }

}
