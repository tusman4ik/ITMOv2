package ru.tusman4ik.taskservice.common.error;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static ru.tusman4ik.taskservice.common.error.Severity.VALIDATION;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {

    private final Set<Severity> allowedForDisclosure = Set.of(VALIDATION);

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        ExceptionDef def = ex.getExceptionDef();
        ProblemDetail pd = create(def.httpStatus());
        pd.setTitle(def.title());
        pd.setType(URI.create("about:blank"));
        pd.setDetail(ex.getDetail());
        pd.setProperty("code", def.code());
        pd.setProperty("retryable", def.retryable());

        if (allowedForDisclosure.contains(def.severity()) && ex.getParams() != null) {
            pd.setProperty("params", ex.getParams());
        }
        logBySeverity(def, ex);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("code=INTERNAL.UNEXPECTED traceId={}", MDC.get("traceId"), ex);
        ProblemDetail pd = create(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal error");
        pd.setDetail("Please don't kill tusman4ik. He made a mistake, but who doesn't? Better write to him on telegram and ask: What the fuck ...");
        pd.setProperty("code", "INTERNAL.UNEXPECTED");
        return pd;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException ex) {
        ProblemDetail pd = create(HttpStatus.NOT_FOUND);
        pd.setTitle("Not found");
        pd.setProperty("code", "NOT.FOUND");
        pd.setDetail("No resource at " + ex.getResourcePath());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setProperty("code", "VALIDATION.FAILED");
        pd.setDetail(ex.getMessage());
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "code", fe.getDefaultMessage() == null ? "(empty)" : fe.getDefaultMessage(),
                        "field", fe.getField()
                ))
                .toList());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("code=VALIDATION.UNREADABLE traceId={} cause={}", MDC.get("traceId"), ex.getMostSpecificCause().getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setProperty("code", "VALIDATION.FAILED");
        pd.setProperty("traceId", MDC.get("traceId"));
        pd.setDetail("Request body is not readable: unknown type or malformed JSON");
        return pd;
    }

    private void logBySeverity(ExceptionDef def, DomainException ex) {
        final String pattern = "code={} traceId={} params={}";
        String traceId = MDC.get("traceId");
        switch (def.severity()) {
            case BUSINESS, VALIDATION -> log.warn(pattern, def.code(), traceId, ex.getParams());
            case INFRASTRUCTURE, UNEXPECTED -> log.error(pattern, def.code(), traceId, ex.getParams(), ex);
        }
    }


    private ProblemDetail create(HttpStatus status) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        String traceId = MDC.get("traceId");
        pd.setProperty("traceId", traceId);
        return pd;
    }

}
