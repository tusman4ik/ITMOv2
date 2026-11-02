package ru.tusman4ik.coderunner.services;

import lombok.Getter;
import ru.tusman4ik.coderunner.models.domain.SubmissionRequest;
import ru.tusman4ik.coderunner.services.sandbox.IdManager;
import ru.tusman4ik.coderunner.services.workspace.Workspace;

import java.io.IOException;


@Getter
public class Context implements AutoCloseable {
    private final IdManager idManager;
    private final SubmissionRequest request;
    private final Workspace workspace;

    public Context(SubmissionRequest request, IdManager idManager) throws IOException {
        this.request = request;
        this.idManager = idManager;
        try {
            workspace = new Workspace();
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public Context(Context ctx) throws IOException {
        request = ctx.request;
        idManager = ctx.idManager;
        workspace = new Workspace(ctx.workspace);
    }

    @Override
    public void close() {
        workspace.close();
    }
}
