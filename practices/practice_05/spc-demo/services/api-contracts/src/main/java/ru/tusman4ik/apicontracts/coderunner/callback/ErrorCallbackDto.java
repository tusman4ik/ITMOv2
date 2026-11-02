package ru.tusman4ik.apicontracts.coderunner.callback;

import lombok.Builder;
import ru.tusman4ik.apicontracts.coderunner.submission.enums.Stage;

@Builder
public record ErrorCallbackDto(
        int seq,
        boolean isLast,

        Stage stage,
        String caseId,
        String groupId
) implements SubmissionCallbackDto {

    public static final String DISC = "error";
}
