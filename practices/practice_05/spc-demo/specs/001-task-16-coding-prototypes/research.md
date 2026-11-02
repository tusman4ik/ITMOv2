# Research: Task 16 Coding Prototypes (16-001, 16-002, 16-003, 16-004)

**Feature**: `001-task-16-coding-prototypes`  
**Date**: 2026-11-01  
**Status**: Completed

## 1. Context & Scope

This research defines the mathematical domains, invariants, template structures, and test-case generation strategies for the four remaining prototypes of Task 16 (OGE Computer Science) based on `docs/prototypes.pdf` and existing code in `ru.tusman4ik.taskservice.taskimpl.t16`:

- **16-001** (Cluster 1): Sequence sum filtered by last digit in base $B$.
- **16-002** (Cluster 2): Sequence sum filtered by penultimate digit in base $B$.
- **16-003** (Cluster 3): Single number digit count with divisibility condition.
- **16-004** (Cluster 4): Single number digit sum with divisibility condition.

All prototypes implement `CodePendingChecker`, use `CodeInputForm(CodeInputForm.Language.PYTHON)`, and delegate sandboxed execution to `CodeRunnerClient`.

---

## 2. Prototype 16-001: Sequence Sum (Last Digit in Base B)

### 2.1 Specification & Invariants
- **Problem**: Given a sequence of $N$ natural numbers ($1 \le N \le 1000$, elements $\le 10^9$ or $\le 30000$), calculate the sum of numbers whose representation in base $B$ ends with digit $D$.
- **Parameters**:
  - `base` ($B$): integer in range $[3, 9]$.
  - `digit` ($D$): integer in range $[1, B-1]$ (guaranteeing non-trivial condition).
- **Test Generation Strategy**:
  - Always include 2 example test cases matching the statement description.
  - Corner cases:
    - Sequence where all elements match.
    - Sequence where exactly one element matches.
    - Sequence where no elements match (sum = 0).
    - Large values near upper bound ($10^9$).
  - Random cases: 15–20 randomized sequences of length $3 \le N \le 50$ with mixed matching/non-matching elements.

### 2.2 Reference Solver
```python
import sys
data = sys.stdin.read().split()
if data:
    n = int(data[0])
    nums = [int(x) for x in data[1:n+1]]
    ans = sum(x for x in nums if x % base == digit)
    print(ans)
```

---

## 3. Prototype 16-002: Sequence Sum (Penultimate Digit in Base B)

### 3.1 Specification & Invariants
- **Problem**: Given a sequence of $N$ natural numbers, calculate the sum of numbers whose penultimate digit in base $B$ equals $D$.
- **Parameters**:
  - `base` ($B$): integer in range $[4, 9]$.
  - `digit` ($D$): integer in range $[0, B-1]$.
  - Filter condition: `(x // B) % B == D` for $x \ge B$. По условию задачи гарантируется, что все числа последовательности в целевой системе счисления состоят как минимум из двух цифр ($x \ge B$).
- **Test Generation Strategy**:
  - All generated sequence elements satisfy $x \ge B$.
  - Examples: 2 verified cases.
  - Corner cases: numbers with exactly two digits, numbers where penultimate digit is 0, maximum values.
  - Random cases: 15–20 diverse cases.

---

## 4. Prototype 16-003: Single Number Digit Count

### 4.1 Specification & Invariants
- **Problem**: Given a single natural number $X$ ($1 \le X \le 10^9$), count the number of digits whose value is divisible by $K$ (e.g. divisible by 2, 3, or 4).
- **Parameters**:
  - `k` ($K$): divisor, typically in $\{2, 3, 4\}$.
  - Zero handling: Digit `0` is mathematically divisible by any $K > 0$ ($0 \pmod K == 0$).
  - Invariants: $X$ generated such that the answer is well-defined and positive in examples.
- **Test Generation Strategy**:
  - Examples: 2 small numbers demonstrating matching and non-matching digits.
  - Corner cases: 1-digit numbers, numbers with repeated digits (e.g., `999999`), numbers containing `0`.
  - Random cases: 20 random numbers in range $[10, 10^9]$.

---

## 5. Prototype 16-004: Single Number Digit Sum

### 5.1 Specification & Invariants
- **Problem**: Given a single natural number $X$ ($1 \le X \le 10^9$), calculate the sum of digits whose numeric value is not divisible by $K$ (or divisible by $K$).
- **Parameters**:
  - `k` ($K$): divisor in $\{3, 4, 5\}$.
  - `predicate`: "не кратно K" (not divisible by $K$).
- **Test Generation Strategy**:
  - Examples: 2 cases.
  - Corner cases: all digits divisible, no digits divisible, single-digit input, number with trailing zeroes.
  - Random cases: 20 varied inputs.

---

## 6. Architecture & Framework Integration

- **Generator Registry**: Wire into `PrototypeRegistry` via `@Register(prototypeIdx = X, exam = Exam.OGE, subject = Subject.CS, number = 16)`.
- **Mustache Resource Resolution**:
  - Standard resource paths: `spc-res/16-001/statement.mustache`, `spc-res/16-002/statement.mustache`, `spc-res/16-003/statement.mustache`, `spc-res/16-004/statement.mustache`.
- **Sandbox Limits**:
  - `cpuTimeLimitSec`: 1.0s
  - `realTimeLimitSec`: 5.0s
  - `memoryLimitMb`: 128MB
  - `fileSizeMb`: 16MB
  - `processCnt`: 1
- **Scoring**: Standard OGE 2-point scale:
  - 0 failed tests: 2 points
  - 1 failed test: 1 point
  - >1 failed tests or compilation/runtime error: 0 points.
