# Research: Task 16 Dynamic Frontend

**Feature**: `002-task-16-frontend`  
**Date**: 2026-11-01  
**Status**: Completed

## 1. Technical Choices

- **Decision**: SPA на React 19 + TypeScript + Vite.
  - **Rationale**: Минимальный размер бандла, мгновенный HMR при разработке, строгая типизация DTO из `services/api-contracts/ts/taskservice.d.ts`.
  - **Alternatives considered**: 
    - Одиночный HTML/JS: сложно поддерживать при разрастании логики редактора и тестов.
    - Next.js: избыточен (серверный рендеринг не требуется, бэкенд уже работает на Spring Boot).

- **Decision**: Режим проверки кода — `fail-fast` (`ExecutionPolicy.STOP_ON_FIRST_FAILURE`).
  - **Rationale**: В соответствии с требованием пользователя, при первом упавшем тесте выполнение сразу завершается, экономя время и ресурсы песочницы.
  - **Таблица результатов**: Пользователь видит все выполненные тесты и подробные ошибки упавшего теста (входные данные, вывод программы, эталон, сообщение чекера).

- **Decision**: Редактор кода на базе легковесного `@monaco-editor/react` или CodeMirror/Textarea.
  - **Rationale**: Чистый интерфейс с моноширинным шрифтом, поддержкой табуляции (4 пробела) и подсветкой синтаксиса Python.

- **Decision**: Взаимодействие с API через Vite Dev Proxy.
  - **Rationale**: Локальный фронтенд (`http://localhost:5173`) проксирует `/api/*` напрямую на `http://localhost:8081`, устраняя проблемы с CORS.

- **Decision**: Асинхронный опрос статуса проверки (Polling).
  - **Rationale**: Проверка кода через `isolate` занимает от 0.5 до 3 секунд. Клиент отправляет сабмишен на `/api/v1/tasks/check`, получает `pending`, затем опрашивает результат с интервалом 1.5 секунды до финального `success`/`failed` (таймаут 10 секунд).
