# Quickstart: Task 16 Frontend

**Feature**: `002-task-16-frontend`  
**Date**: 2026-11-01  
**Status**: Ready

## 1. Prerequisites
- Node.js >= 20
- Docker стенд `spc-demo-stand` запущен:
```bash
docker compose up -d
```

## 2. Launch Frontend
```bash
cd services/frontend
npm install
npm run dev
```

Откройте браузер по адресу `http://localhost:5173`.

## 3. Scenarios
1. Нажмите «Сгенерировать задания» — появится список вариантов №16.
2. Выберите задачу (например, 16-001 или 16-005).
3. Введите Python-код решения и нажмите «Проверить».
4. Дождитесь получения статуса и потестовой таблицы результатов.
