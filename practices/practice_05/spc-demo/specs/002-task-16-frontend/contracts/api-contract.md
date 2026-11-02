# API Contracts: Task 16 Frontend Integration

**Feature**: `002-task-16-frontend`  
**Date**: 2026-11-01  
**Status**: Ready

## 1. Endpoints Used by Frontend

### `POST /api/v1/tasks/generate`
Генерация нового набора задач.

### `POST /api/v1/tasks/recreate`
Воспроизведение задачи по заданному `seed` и `prototypeIdx`.

### `POST /api/v1/tasks/check`
Отправка решения учащегося на проверку.
- Запрос: `{ items: [{ attempt: {...}, answer: { type: "text-schema", text: "..." } }] }`
- Ответ: `{ items: [{ attempt: {...}, result: { type: "pending" | "success" | "failed", ... } }] }`
- При статусе `pending` клиент повторяет вызов `/tasks/check` для этого же `attemptId` до получения `success`/`failed`.
