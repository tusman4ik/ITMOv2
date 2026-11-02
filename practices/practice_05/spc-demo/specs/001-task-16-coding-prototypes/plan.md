# Implementation Plan: Task 16 Coding Prototypes (16-001 .. 16-004)

**Branch**: `001-task-16-coding-prototypes` | **Date**: 2026-11-01 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-task-16-coding-prototypes/spec.md`

## Summary

Implement the four remaining prototypes for OGE Computer Science Task 16 based on the official task bank (`docs/prototypes.pdf`):
- `16-001`: Sequence element sum filtered by last digit in base $B \in [3, 9]$.
- `16-002`: Sequence element sum filtered by penultimate digit in base $B \in [4, 9]$.
- `16-003`: Single number digit count with divisibility by $K \in \{2, 3, 4\}$.
- `16-004`: Single number digit sum with divisibility by $K \in \{3, 4, 5\}$.

Each prototype provides declarative generation nodes, Mustache statement rendering, test case generation (examples, corner cases, random suites), Python code input forms, and integration with `code-runner` via asynchronous callback checks.

## Technical Context

**Language/Version**: Java 26 (Gradle toolchain), Python 3 (student solution runtime).

**Primary Dependencies**: Spring Boot 4.1.1, Javalin 7.2.2, JMustache 1.16, Jackson 2.18, Lombok.

**Storage**: In-memory prototype registry (`GeneratorRegistry`), stateless seeded generation, static Mustache templates in `resources/spc-res/`.

**Testing**: JUnit 5, AssertJ, `./gradlew test`.

**Target Platform**: Linux containers (Docker Compose stand: `spc-code-runner-demo` on port 8080, `spc-task-service-demo` on port 8081).

**Project Type**: Multi-service distributed system (REST APIs + isolate code execution sandbox).

**Performance Goals**: Task variant generation latency < 100ms; runner check real-time limit 5.0s, CPU limit 1.0s.

**Constraints**: Strict Isolate sandbox execution, non-blocking asynchronous callbacks, deterministic seeded randomness.

**Scale/Scope**: 4 new prototype classes + 4 Mustache templates + unit test coverage for generators and solvers.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Sandbox Isolation (Principle I)**: PASS. All arbitrary code from student submissions executes exclusively within Isolate sandboxes with cgroups, memory limits (128MB), and CPU timeouts (1.0s).
- **Asynchronous Decoupling (Principle II)**: PASS. The service immediately acknowledges submissions with `PendingCheck(BATCHED_MS_1000)` and receives test verdicts asynchronously via `/api/v1/callbacks`.
- **Quality Gates (Workflow)**: PASS. `./gradlew test` passes locally; contract definitions and schema invariants remain validated.

## Project Structure

### Documentation (this feature)

```text
specs/001-task-16-coding-prototypes/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan
├── research.md          # Phase 0 research & solver designs
├── data-model.md        # Phase 1 data entities and generator nodes
├── quickstart.md        # Phase 1 run & validation instructions
├── contracts/           # Phase 1 interface definitions
│   └── api-contract.md
└── checklists/
    └── requirements.md  # Spec quality checklist
```

### Source Code (repository root)

```text
services/task-service/
├── src/main/java/ru/tusman4ik/taskservice/taskimpl/t16/
│   ├── Generator_16_001.java         # Prototype 16-001 (seq sum, last digit)
│   ├── Generator_16_002.java         # Prototype 16-002 (seq sum, penultimate digit)
│   ├── Generator_16_003.java         # Prototype 16-003 (single number, digit count)
│   ├── Generator_16_004.java         # Prototype 16-004 (single number, digit sum)
│   └── common/
│       └── TestCase.java             # Shared test case record
├── src/main/resources/spc-res/
│   ├── 16-001/statement.mustache     # Template for 16-001
│   ├── 16-002/statement.mustache     # Template for 16-002
│   ├── 16-003/statement.mustache     # Template for 16-003
│   └── 16-004/statement.mustache     # Template for 16-004
└── src/test/java/ru/tusman4ik/taskservice/taskimpl/t16/
    ├── Generator_16_001_Test.java    # Unit tests for 16-001
    ├── Generator_16_002_Test.java    # Unit tests for 16-002
    ├── Generator_16_003_Test.java    # Unit tests for 16-003
    └── Generator_16_004_Test.java    # Unit tests for 16-004
```

**Structure Decision**: Prototypes are placed into `ru.tusman4ik.taskservice.taskimpl.t16` alongside existing `Generator_16_005` and `Generator_16_999`. Templates reside in `services/task-service/src/main/resources/spc-res/16-XXX/statement.mustache` matching established conventions.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| *None*    | *N/A*      | *N/A*                               |
