# Implementation Plan: Task 16 Dynamic Frontend

**Branch**: `002-task-16-frontend` | **Date**: 2026-11-01 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/002-task-16-frontend/spec.md`

## Summary

Разработка клиентского SPA-приложения на React + TypeScript (Vite) для интерактивной работы со сгенерированным пулом заданий №16 ОГЭ по информатике:
- Панель конфигурации и генерации вариантов (прототипы 16-001..16-005, 16-999).
- Динамический список задач с переключением и сохранением черновиков кода.
- Рендеринг условий заданий (HTML-разметка и таблицы примеров).
- Редактор кода решений на языке Python с подсветкой и табуляцией.
- Асинхронная отправка решений на проверку в `task-service` с polling-механизмом.
- Визуализация итоговых баллов и подробной таблицы тестов (статус, входные данные, фактический и эталонный вывод).

## Technical Context

**Language/Version**: TypeScript 5, React 19, HTML5, CSS3.

**Primary Dependencies**: Vite, Lucide-React (иконки), CSS Modules / Tailwind.

**Storage**: Local state (React state / localStorage) для черновиков решений.

**Testing**: Vitest / Playwright (клиентские сценарии).

**Target Platform**: Современные веб-браузеры (Chrome, Firefox, Safari, Edge), адаптивная верстка от 768px.

**Project Type**: Веб-приложение (SPA).

**Performance Goals**: Первичная отрисовка условий < 200 мс, переключение задач без лагов.

**Constraints**: Асинхронное взаимодействие с `task-service` (порт 8081) через Vite Proxy, неблокирующий интерфейс.

## Constitution Check

- **Изоляция исполнения (Principle I)**: PASS. Код проверяется на стороне изолированной песочницы, frontend передает только исходный текст.
- **Асинхронность сервиса (Principle II)**: PASS. Клиент отправляет запрос, получает статус `pending` и асинхронно опрашивает готовность вердикта.
- **Ворота качества**: PASS.

## Project Structure

### Documentation (this feature)

```text
specs/002-task-16-frontend/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan
├── research.md          # Technical choices and research
├── data-model.md        # Client entities and state models
├── quickstart.md        # Run and testing guide
├── contracts/           # API integration contracts
│   └── api-contract.md
└── checklists/
    └── requirements.md  # Quality checklist
```

### Source Code

```text
services/frontend/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── api/
│   │   ├── client.ts             # API client for task-service
│   │   └── types.ts              # DTO definitions
│   ├── components/
│   │   ├── TaskList.tsx          # Dynamic task sidebar / selector
│   │   ├── TaskStatement.tsx     # Statement HTML renderer
│   │   ├── CodeEditor.tsx        # Solution editor (Python)
│   │   ├── CheckReportView.tsx   # Detailed test verdict table
│   │   └── GeneratorControls.tsx # Count / prototype generator bar
│   └── styles/
│       └── app.css
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| *None*    | *N/A*      | *N/A*                               |
