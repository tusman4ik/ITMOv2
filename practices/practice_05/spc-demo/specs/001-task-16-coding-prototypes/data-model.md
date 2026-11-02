# Data Model: Task 16 Coding Prototypes

**Feature**: `001-task-16-coding-prototypes`  
**Date**: 2026-11-01  
**Status**: Ready

## 1. Domain Entities & Parameters

### 1.1 Prototype 16-001 (Sequence Sum, Last Digit)
- **Identity**: `(exam: OGE, subject: CS, number: 16, prototypeIdx: 1)`
- **Generator Nodes**:
  - `base: Node<Integer>` — Number system base $B \in [3, 9]$.
  - `digit: Node<Integer>` — Target last digit $D \in [1, B - 1]$.
  - `tests: Node<List<TestCase>>` — List of `TestCase(in, expected)`.
  - `firstExampleIn: Node<String>`, `firstExampleExp: Node<String>` — First example row.
  - `secondExampleIn: Node<String>`, `secondExampleExp: Node<String>` — Second example row.

### 1.2 Prototype 16-002 (Sequence Sum, Penultimate Digit)
- **Identity**: `(exam: OGE, subject: CS, number: 16, prototypeIdx: 2)`
- **Invariants**: Все элементы последовательности строго двухзначные или длиннее в системе счисления с основанием $B$ ($x \ge B$).
- **Generator Nodes**:
  - `base: Node<Integer>` — Number system base $B \in [4, 9]$.
  - `digit: Node<Integer>` — Target penultimate digit $D \in [0, B - 1]$.
  - `tests: Node<List<TestCase>>` — List of `TestCase(in, expected)`.
  - `firstExampleIn: Node<String>`, `firstExampleExp: Node<String>`
  - `secondExampleIn: Node<String>`, `secondExampleExp: Node<String>`

### 1.3 Prototype 16-003 (Single Number, Digit Count)
- **Identity**: `(exam: OGE, subject: CS, number: 16, prototypeIdx: 3)`
- **Generator Nodes**:
  - `k: Node<Integer>` — Divisibility factor $K \in \{2, 3, 4\}$.
  - `tests: Node<List<TestCase>>` — List of test cases where `in` is a single number and `expected` is the count of digits divisible by $K$.
  - `firstExampleIn: Node<String>`, `firstExampleExp: Node<String>`
  - `secondExampleIn: Node<String>`, `secondExampleExp: Node<String>`

### 1.4 Prototype 16-004 (Single Number, Digit Sum)
- **Identity**: `(exam: OGE, subject: CS, number: 16, prototypeIdx: 4)`
- **Generator Nodes**:
  - `k: Node<Integer>` — Divisibility factor $K \in \{3, 4, 5\}$.
  - `tests: Node<List<TestCase>>` — List of test cases where `in` is a single number and `expected` is the sum of digits not divisible by $K$.
  - `firstExampleIn: Node<String>`, `firstExampleExp: Node<String>`
  - `secondExampleIn: Node<String>`, `secondExampleExp: Node<String>`

---

## 2. Shared Data Structures

### 2.1 `TestCase`
Represents an individual input/output verification pair:
```java
public record TestCase(String in, String expected) {}
```
- `in`: Standard input supplied to the student program (formatted with newlines).
- `expected`: Canonical standard output expected from a valid solution.

### 2.2 `SubmissionRequestDto`
Payload sent to `code-runner` via `CodeRunnerClient`:
- `id`: Unique attempt ID (`AttemptId`).
- `callbackUrl`: Callback URL on `task-service` (`/api/v1/callbacks`).
- `lang`: Target language (`Language.PYTHON`).
- `src`: Student program source code.
- `feeders`: Map of `feederId` $\to$ `CompositeFeederDto(Target.STDIN, Target.EXP)`.
- `checkers`: Map of checker configurations (`TokenCheckerDto`).
- `limits`: Sandboxing quotas (CPU: 1s, Real: 5s, RAM: 128MB).
- `groups`: List of test groups executing all generated test cases.

### 2.3 `RunnerCallback` & `CheckResult`
Asynchronous response received by `task-service`:
- `id`: Attempt ID.
- `results`: Map of test case results with execution metrics (CPU time, real time, memory, exit code, checker verdict).
- `error` / `compilationLog`: Optional diagnostics when run fails.
- Transformed into `SuccessCheck`:
  - `ScoresDto(max: 2, earned: 0..2)`
  - `Criterion("main", 2, earned, "Main functional criterion")`
  - `CodeSubmissionFeedback` (detailed table of test results).
