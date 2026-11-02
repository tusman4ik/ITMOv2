# Data Model: Task 16 Dynamic Frontend

**Feature**: `002-task-16-frontend`  
**Date**: 2026-11-01  
**Status**: Ready

## 1. Client State Entities

### 1.1 `TaskItem`
Представление сгенерированной задачи:
```typescript
interface TaskItem {
  id: {
    exam: "OGE";
    subject: "CS";
    number: number;
    prototypeIdx: number;
    seed: number;
    fingerprint: number;
  };
  statementHtml: string;
  defaultCode: string;
  userCode: string;
  status: "idle" | "submitting" | "pending" | "completed" | "error";
  checkReport?: VerificationReport;
}
```

### 1.2 `VerificationReport`
Результат проверки программы учащегося в режиме fail-fast:
```typescript
interface VerificationReport {
  score: number;
  maxScore: number;
  allPassed: boolean;
  failedFast?: boolean; // признак остановки на первом сбое
  rows: Array<{
    caseId: string;
    status: "OK" | "WA" | "TLE" | "RE" | "CRASH";
    input?: string;
    stdout?: string;
    expected?: string;
    checkerMsg?: string;
    cpuTimeSec?: number;
    memoryKb?: number;
  }>;
  compilerLog?: string;
  error?: string;
}
```

### 1.3 `GeneratorFilter`
Состояние панели управления генерацией:
```typescript
interface GeneratorFilter {
  count: number;
  selectedPrototypes: number[]; // e.g. [1, 2, 3, 4, 5, 999]
}
```
