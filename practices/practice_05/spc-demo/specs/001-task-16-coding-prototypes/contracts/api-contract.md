# API Contracts: Task 16 Prototypes (16-001 .. 16-004)

**Feature**: `001-task-16-coding-prototypes`  
**Date**: 2026-11-01  
**Status**: Ready

## 1. Task Generation Endpoint

### `POST /api/v1/tasks/generate`

Generates task statements for given prototypes.

#### Request Body
```json
{
  "mode": "EXAM",
  "settings": {},
  "items": [
    {
      "count": 1,
      "prototype": {
        "exam": "OGE",
        "subject": "CS",
        "number": 16,
        "prototypeIdx": 1
      }
    }
  ]
}
```

#### Response (200 OK)
```json
{
  "tasks": [
    {
      "id": {
        "exam": "OGE",
        "subject": "CS",
        "number": 16,
        "prototypeIdx": 1,
        "seed": 1234567890123456,
        "fingerprint": 987654321
      },
      "statement": "<p>Напишите программу подсчёта суммы элементов последовательности натуральных чисел, запись которых в 5-ричной системе счисления оканчивается на цифру 3...</p>",
      "form": {
        "type": "code-form",
        "defaultLanguage": "PYTHON",
        "allowedLanguages": ["PYTHON"]
      },
      "parameters": {
        "base": 5,
        "digit": 3
      }
    }
  ]
}
```

---

## 2. Task Submission Check Endpoint

### `POST /api/v1/tasks/check`

Initiates verification of student code.

#### Request Body
```json
{
  "items": [
    {
      "attempt": {
        "exam": "OGE",
        "subject": "CS",
        "number": 16,
        "prototypeIdx": 1,
        "seed": 1234567890123456,
        "fingerprint": 987654321,
        "attempt": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"
      },
      "answer": {
        "type": "text-schema",
        "text": "import sys\n..."
      }
    }
  ]
}
```

#### Immediate Response (200 OK)
Returns pending status while asynchronous evaluation executes in `code-runner`:
```json
{
  "results": [
    {
      "type": "pending-check",
      "mode": "BATCHED_MS_1000"
    }
  ]
}
```

---

## 3. Callback Endpoint (Inter-Service)

### `POST /api/v1/callbacks`

Invoked by `code-runner` to deliver execution feedback.

#### Payload
```json
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "status": "COMPLETED",
  "results": {
    "case-0": {
      "status": "OK",
      "exitCode": 0,
      "cpuTimeSec": 0.03,
      "realTimeSec": 0.05,
      "memoryKb": 12400,
      "stdout": "31\n",
      "checkerMsg": "OK"
    }
  }
}
```
