package ru.tusman4ik.coderunner.models.mappers.requests.submission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tusman4ik.apicontracts.coderunner.submission.TestCaseDto;
import ru.tusman4ik.apicontracts.coderunner.submission.TestGroupDto;
import ru.tusman4ik.coderunner.models.mappers.IContext;
import ru.tusman4ik.apicontracts.coderunner.submission.SubmissionRequestDto;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Context implements IContext {
    private SubmissionRequestDto submissionRequestDto;
    private TestGroupDto testGroupDto;
    private TestCaseDto testCaseDto;

    private ArrayList<String> errors = new ArrayList<>();

    private String currentPos;

    public Context goTo(String nextPartOfPath) {
        String newPos = (currentPos == null || currentPos.isEmpty())
                ? nextPartOfPath
                : currentPos + "." + nextPartOfPath;

        return new Context(submissionRequestDto, testGroupDto,
                testCaseDto, errors, newPos);
    }


}
