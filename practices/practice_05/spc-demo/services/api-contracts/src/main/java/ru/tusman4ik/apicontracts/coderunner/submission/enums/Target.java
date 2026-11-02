package ru.tusman4ik.apicontracts.coderunner.submission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.tusman4ik.apicontracts.coderunner.callback.enums.Mark;

@Getter
@AllArgsConstructor
public enum Target {
    STDIN(Mark.STDIN),
    EXP(Mark.EXP),
    ;

    private final Mark mark;
}
