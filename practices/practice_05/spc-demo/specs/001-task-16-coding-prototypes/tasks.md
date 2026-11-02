---
description: "Task list for Task 16 coding prototypes implementation"
---

# Tasks: Task 16 Coding Prototypes (16-001 .. 16-004)

**Input**: Design documents from `specs/001-task-16-coding-prototypes/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/api-contract.md`, `quickstart.md`
**Tests**: Unit tests included per project quality gates (`./gradlew test`).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths included in all tasks

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Resource directories and template scaffolding

- [X] T001 [P] Create template directories for new prototypes in `services/task-service/src/main/resources/spc-res/16-001`, `16-002`, `16-003`, `16-004`
- [X] T002 [P] Configure Bruno request directories for new prototypes in `services/task-service/bruno/generate` and `services/task-service/bruno/check`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared utilities for test case generation, sequence formatting, and digit manipulation

**⚠️ CRITICAL**: Must complete before user story generator implementations

- [X] T003 [P] Implement sequence and numeral system helper functions (`toBaseString`, `extractPenultimateDigit`, `in(long... numbers)`) in `services/task-service/src/main/java/ru/tusman4ik/taskservice/taskimpl/t16/common/NumberSequenceUtils.java`
- [X] T004 [P] Implement unit tests for sequence and numeral system helpers in `services/task-service/src/test/java/ru/tusman4ik/taskservice/taskimpl/t16/common/NumberSequenceUtilsTest.java`

**Checkpoint**: Shared utilities verified; user story generators can now be implemented.

---

## Phase 3: User Story 1 - Генерация и решение задач на последовательности чисел (Priority: P1) 🎯 MVP

**Goal**: Implement and verify prototypes 16-001 (sequence sum, last digit) and 16-002 (sequence sum, penultimate digit).

**Independent Test**: Generate tasks with seeds, execute reference Python solvers through unit tests, and verify 100% test pass with 2/2 score.

### Prototype 16-001: Sequence Sum (Last Digit in Base B)

- [X] T005 [P] [US1] Create Mustache statement template with examples table in `services/task-service/src/main/resources/spc-res/16-001/statement.mustache`
- [X] T006 [P] [US1] Implement unit test suite verifying generation and reference solution in `services/task-service/src/test/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_001_Test.java`
- [X] T007 [US1] Implement generator `Generator_16_001` with constraints (`base: Node<Integer>` $B \in [3, 9]$, `digit: Node<Integer>` $D \in [1, B-1]$, $1 \le N \le 1000$) in `services/task-service/src/main/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_001.java`

### Prototype 16-002: Sequence Sum (Penultimate Digit in Base B)

- [X] T008 [P] [US1] Create Mustache statement template guaranteeing $x \ge B$ in `services/task-service/src/main/resources/spc-res/16-002/statement.mustache`
- [X] T009 [P] [US1] Implement unit test suite verifying generation and penultimate digit logic in `services/task-service/src/test/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_002_Test.java`
- [X] T010 [US1] Implement generator `Generator_16_002` with constraints (`base: Node<Integer>` $B \in [4, 9]$, `digit: Node<Integer>` $D \in [0, B-1]$, invariant: all numbers $x \ge B$) in `services/task-service/src/main/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_002.java`

**Checkpoint**: User Story 1 complete and independently testable via `Generator_16_001_Test` and `Generator_16_002_Test`.

---

## Phase 4: User Story 2 - Анализ и подсчет свойств цифр одного числа (Priority: P2)

**Goal**: Implement and verify prototypes 16-003 (digit count with divisibility) and 16-004 (digit sum with divisibility).

**Independent Test**: Generate tasks with seeds, execute reference Python solvers, and verify correct digit parsing and scoring.

### Prototype 16-003: Single Number Digit Count

- [X] T011 [P] [US2] Create Mustache statement template in `services/task-service/src/main/resources/spc-res/16-003/statement.mustache`
- [X] T012 [P] [US2] Implement unit test suite for digit count logic in `services/task-service/src/test/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_003_Test.java`
- [X] T013 [US2] Implement generator `Generator_16_003` with constraints (`k: Node<Integer>` $K \in \{2, 3, 4\}$, input number $X \in [1, 10^9]$, count digits where $d \pmod K == 0$) in `services/task-service/src/main/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_003.java`

### Prototype 16-004: Single Number Digit Sum

- [X] T014 [P] [US2] Create Mustache statement template in `services/task-service/src/main/resources/spc-res/16-004/statement.mustache`
- [X] T015 [P] [US2] Implement unit test suite for digit sum logic in `services/task-service/src/test/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_004_Test.java`
- [X] T016 [US2] Implement generator `Generator_16_004` with constraints (`k: Node<Integer>` $K \in \{3, 4, 5\}$, input number $X \in [1, 10^9]$, sum digits where $d \pmod K \ne 0$) in `services/task-service/src/main/java/ru/tusman4ik/taskservice/taskimpl/t16/Generator_16_004.java`

**Checkpoint**: User Stories 1 and 2 both fully functional and tested.

---

## Phase 5: User Story 3 - Безопасная асинхронная проверка и интеграция API (Priority: P3)

**Goal**: Provide API Bruno collections and integration tests for end-to-end task generation and callback submission.

**Independent Test**: Run `./gradlew test` and verify Bruno collections against local test stand.

- [X] T017 [P] [US3] Add Bruno HTTP request collections for generating prototypes 16-001..16-004 in `services/task-service/bruno/generate/16-001.yml`, `16-002.yml`, `16-003.yml`, `16-004.yml`
- [X] T018 [P] [US3] Add Bruno HTTP request collections for checking prototypes 16-001..16-004 in `services/task-service/bruno/check/16-001.yml`, `16-002.yml`, `16-003.yml`, `16-004.yml`
- [X] T019 [US3] Implement end-to-end prototype registry integration test in `services/task-service/src/test/java/ru/tusman4ik/taskservice/task/wire/Task16PrototypesIntegrationTest.java`

**Checkpoint**: All prototypes registered, discoverable, and validated through API collection fixtures.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Quality gate verification, code cleanliness, and documentation validation

- [X] T020 Run full `./gradlew test` across all subprojects to ensure zero regressions
- [X] T021 Validate `quickstart.md` scenarios and build artifact consistency
- [X] T022 [P] Clean up any unused imports or formatting issues across `ru.tusman4ik.taskservice.taskimpl.t16`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - BLOCKS User Stories.
- **User Story 1 (Phase 3)**: Depends on Foundational (Phase 2) completion.
- **User Story 2 (Phase 4)**: Depends on Foundational (Phase 2) completion. Can run in parallel with US1.
- **User Story 3 (Phase 5)**: Depends on completion of US1 and US2 (needs all generator beans registered).
- **Polish (Phase 6)**: Depends on all user stories completed.

### Parallel Opportunities

- Within Phase 1: T001 and T002 can run in parallel.
- Within Phase 2: T003 and T004 can run in parallel.
- Within US1: T005, T006, T008, T009 can run in parallel.
- Within US2: T011, T012, T014, T015 can run in parallel.
- US1 and US2 can be developed concurrently once Phase 2 is finished.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational utilities).
2. Complete Phase 3 (Prototypes 16-001 and 16-002).
3. Run `./gradlew test` to validate User Story 1.
4. Stop and demo/review MVP.

### Incremental Delivery

1. Foundation ready (Phase 1 + 2).
2. Sequence processing ready (US1: 16-001, 16-002).
3. Digit analysis ready (US2: 16-003, 16-004).
4. End-to-end API collections & integration tests (US3).
5. Final quality gate pass (Phase 6).
