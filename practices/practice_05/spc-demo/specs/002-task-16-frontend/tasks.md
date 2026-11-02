---
description: "Task list for Task 16 Dynamic Frontend implementation"
---

# Tasks: Task 16 Dynamic Frontend

**Input**: Design documents from `specs/002-task-16-frontend/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/api-contract.md`, `quickstart.md`
**Tests**: Client component and helper tests included.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths included in all tasks

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Vite project initialization and proxy configuration

- [X] T001 Initialize React + TypeScript + Vite project scaffolding in `services/frontend/package.json`, `services/frontend/tsconfig.json`, `services/frontend/index.html`
- [X] T002 Configure Vite server proxy for `/api` routing to `http://localhost:8081` in `services/frontend/vite.config.ts`
- [X] T003 [P] Configure global CSS design tokens, reset, and layout styles in `services/frontend/src/styles/app.css`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core TypeScript types, API client, and state abstractions

**⚠️ CRITICAL**: Must complete before implementing UI components

- [X] T004 [P] Define API request/response and entity TypeScript types in `services/frontend/src/api/types.ts`
- [X] T005 [P] Implement API client functions (`generateTasks`, `recreateTask`, `checkTask`, `pollCheckResult`) in `services/frontend/src/api/client.ts`
- [X] T006 Implement unit tests for API client mapping and polling logic in `services/frontend/src/api/client.test.ts`

**Checkpoint**: Foundation ready; user story UI components can proceed.

---

## Phase 3: User Story 1 - Просмотр динамического списка задач №16 (Priority: P1) 🎯 MVP

**Goal**: Load and view dynamically generated Task 16 variants (16-001..16-005, 16-999) with HTML statements and examples.

**Independent Test**: Open frontend, generate a pool of tasks, select different items from the sidebar, and confirm rendered statement and example tables.

- [X] T007 [P] [US1] Implement generator control panel component (prototype selector, count, generate button) in `services/frontend/src/components/GeneratorControls.tsx`
- [X] T008 [P] [US1] Implement task list sidebar component with prototype badges and active item selection in `services/frontend/src/components/TaskList.tsx`
- [X] T009 [US1] Implement HTML task statement renderer (supporting sanitized HTML, math, and tables) in `services/frontend/src/components/TaskStatement.tsx`
- [X] T010 [US1] Integrate User Story 1 components into main view layout in `services/frontend/src/App.tsx`

**Checkpoint**: User Story 1 functional (MVP). User can generate and browse dynamic Task 16 instances.

---

## Phase 4: User Story 2 - Ввод и отправка программного кода решения (Priority: P2)

**Goal**: Provide code editing experience with Python formatting, draft preservation, and non-blocking submission.

**Independent Test**: Enter Python code for a selected task, switch to another task and back (confirm draft persists), and click Submit to observe pending status.

- [X] T011 [P] [US2] Implement code editor component with tab indentation, line numbering, and Python template boilerplate in `services/frontend/src/components/CodeEditor.tsx`
- [X] T012 [US2] Implement local storage draft manager for preserving user code per `attemptId` / `instanceId` in `services/frontend/src/utils/draftStorage.ts`
- [X] T013 [US2] Wire submission trigger and pending check state transitions into `services/frontend/src/components/CodeEditor.tsx` and `services/frontend/src/App.tsx`

**Checkpoint**: User Stories 1 and 2 functional. Users can write code and trigger asynchronous checks.

---

## Phase 5: User Story 3 - Визуализация результатов и потестового протокола проверки (Priority: P3)

**Goal**: Display verdict badge (points 0..2, status), compiler errors, and detailed test-by-test breakdown.

**Independent Test**: Complete a check flow; verify table displays rows with status (OK/WA/TLE), inputs, outputs, and execution metrics.

- [X] T014 [P] [US3] Implement test case row component with expandable diff (stdout vs expected) in `services/frontend/src/components/TestRowView.tsx`
- [X] T015 [US3] Implement comprehensive verification report component (scores, criteria, test matrix, compiler log) in `services/frontend/src/components/CheckReportView.tsx`
- [X] T016 [US3] Wire report display and polling updates into `services/frontend/src/App.tsx`

**Checkpoint**: Complete end-to-end user workflow: generate task -> solve -> submit -> inspect detailed test feedback.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Build verification, containerization, and styling refinements

- [X] T017 [P] Add Dockerfile and compose entry for frontend service in `services/frontend/Dockerfile` and `docker-compose.yml`
- [X] T018 Run production build (`npm run build`) and verify bundle integrity
- [X] T019 Validate `quickstart.md` execution flow against the running stand

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Phase 1; blocks UI development.
- **User Story 1 (Phase 3)**: Depends on Phase 2.
- **User Story 2 (Phase 4)**: Depends on Phase 2 & US1.
- **User Story 3 (Phase 5)**: Depends on US2 (needs submission triggers).
- **Polish (Phase 6)**: Depends on all user stories.

### Parallel Opportunities

- T001, T002, T003 can run in parallel.
- T004 and T005 can run in parallel.
- T007, T008, T009 can run in parallel.
- T011, T014 can run in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Setup Vite project (`services/frontend`).
2. Implement API types and client.
3. Build GeneratorControls, TaskList, and TaskStatement.
4. Validate dynamic browsing of generated Task 16 instances.
