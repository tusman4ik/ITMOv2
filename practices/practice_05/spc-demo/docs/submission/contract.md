# Общий контракт: Библиотека `services:api-contracts` как Single Source of Truth

В рамках ДЗ 5 роль общего контракта между Командой 1 (Frontend UI) и Командой 2 (Backend Logic) выполняет не просто текстовое описание, а выделенный модуль **`services:api-contracts`**.

## 1. Архитектура контракта

Модуль `services:api-contracts` является единым источником правды для всей распределенной системы:

```text
               ┌────────────────────────────────────────────────────────┐
               │              services:api-contracts                   │
               │  - Java Sealed Records & Enums                         │
               │  - Jackson Polymorphic Mappings (@JsonSubTypes)        │
               │  - PolymorphicInvariantGuardTest (drift protection)    │
               └───────────┬────────────────────────────────┬───────────┘
                           │ Gradle task                    │ Gradle task
                           │ :bootJar / project dependency   │ :generateTsApi
                           ▼                                ▼
       ┌───────────────────────────────┐        ┌───────────────────────────────┐
       │     Команда 2: Бэкенд         │        │     Команда 1: Фронтенд       │
       │     (task-service / runner)   │        │     (React / TypeScript)      │
       │  Реализация генераторов       │        │  Импорт сгенерированных DTO   │
       │  и проверки по контракту      │        │  taskservice.d.ts             │
       └───────────────────────────────┘        └───────────────────────────────┘
```

## 2. Ключевые структуры контракта

### 2.1 Идентификатор задачи (`InstanceIdDto` / `AttemptIdDto`)
- Java: `ru.tusman4ik.apicontracts.taskservice.id.InstanceIdDto`
- TS: `components["schemas"]["InstanceIdDto"]`
- Гарантия: `seed` ограничен диапазоном безопасных целых чисел JS (`Number.MAX_SAFE_INTEGER`), исключая искажения при JSON-сериализации.

### 2.2 Генерация и воспроизведение
- Контракт эндпоинтов: `TaskApi.java` (`POST /api/v1/tasks/generate`, `POST /api/v1/tasks/recreate`).
- Запрос/Ответ: `GenerateRequestDto` $\to$ `TasksResponseDto(tasks: List<TaskDto>)`.

### 2.3 Проверка решений и Fail-Fast режим
- Контракт эндпоинта: `TaskApi.java` (`POST /api/v1/tasks/check`).
- Запрос: `CheckRequestDto(items: List<CheckItemDto>)`.
- Ответ: полиморфный `CheckResult`:
  - `PendingCheck` (`type: "pending"`) — немедленный асинхронный ответ.
  - `SuccessCheck` (`type: "success"`) — вердикт с набранными баллами (`scores`), критериями и детализированной таблицей тестов (`CodeSubmissionFeedback`).
- Политика исполнения: `ExecutionPolicy.STOP_ON_FIRST_FAILURE`.

## 3. Защита от дрейфа контракта (Schema Drift Protection)
Тест `PolymorphicInvariantGuardTest` на CI/локальном билде верифицирует:
1. Все базовые типы (`CheckResult`, `InputForm`, `InputSchema`, `Parameter`, `Feedback`) являются `sealed`.
2. Наличие корректных `@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)` и непустых `@JsonSubTypes`.
3. Соответствие сгенерированных OpenAPI-схем и TypeScript-деклараций.
