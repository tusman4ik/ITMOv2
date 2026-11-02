# Quickstart: Task 16 Coding Prototypes

**Feature**: `001-task-16-coding-prototypes`  
**Date**: 2026-11-01  
**Status**: Ready

This guide outlines how to build, test, and verify the four new Task 16 prototypes (16-001, 16-002, 16-003, 16-004).

## 1. Prerequisites

- JDK 26 installed and available in PATH.
- Docker & Docker Compose running (for end-to-end sandbox testing).

## 2. Build & Unit Verification

Run standard Gradle compilation and unit tests:
```bash
./gradlew test
```

To run only prototype-related tests:
```bash
./gradlew :services:task-service:test
```

## 3. End-to-End Local Stand Verification

1. Start the services using Docker Compose:
```bash
docker compose up -d --build
```

2. Test task generation via HTTP POST:
```bash
curl -X POST http://localhost:8081/api/v1/tasks/generate \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

3. Submit a Python code solution for checking:
```bash
curl -X POST http://localhost:8081/api/v1/tasks/check \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "attempt": {
          "exam": "OGE",
          "subject": "CS",
          "number": 16,
          "prototypeIdx": 1,
          "seed": 123456789,
          "fingerprint": 0,
          "attempt": "test-attempt-uuid"
        },
        "answer": {
          "type": "text-schema",
          "text": "print(0)"
        }
      }
    ]
  }'
```

4. Verify callback delivery and check verdict logs:
```bash
docker compose logs -f task-service code-runner
```
