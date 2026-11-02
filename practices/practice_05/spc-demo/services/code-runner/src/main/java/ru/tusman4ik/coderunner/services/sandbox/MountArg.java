package ru.tusman4ik.coderunner.services.sandbox;

import ru.tusman4ik.apicontracts.coderunner.submission.enums.MountMode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MountArg {

    private MountArg() {}

    public static String of(Path sandboxPath, Path hostPath, MountMode... modes) {
        ArrayList<MountMode> modeList = new ArrayList<>(List.of(modes));
        return "--dir=" + sandboxPath + "=" + hostPath + (modeList.isEmpty() ? "" : ":" +
                String.join(
                        ",",
                        modeList.stream()
                                .map(MountMode::getValue)
                                .toList()
                ));
    }


}
