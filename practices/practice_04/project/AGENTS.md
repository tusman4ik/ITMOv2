# AGENTS.md — spc-task-service, копия master в practices/practice_04/project

Рабочая копия старой работающей версии (`master`, `2eeb40c`) для практики 4.
Оригинал живёт в `~/projects/spc-task-service` (ветка `refactor/full-rewrite`) и здесь не трогается.

## Где требования

- Задача и скоуп — у студента в чате (поручение), плюс `TECH_DEBT.md` для контекста.
- Контракт API: `POST /v1/tasks/gen`, `POST /v1/tasks/check` (`src/main/java/ru/tusman4ik/controllers/TaskController.java`).
- Формат новых задач — skill `spc-task-authoring` (`.opencode/skills/spc-task-authoring/SKILL.md`).
  Перед реализацией генератора прочитать skill и примеры (`taskimpl/t3`, `taskimpl/t4`).

## Как проверять результат

Единственная доверенная команда:

```bash
sh scripts/check.sh
```

Она гоняет быстрые scoped-тесты (`./gradlew test --tests ...`), а не весь build.
Результат считается принятым, только если `check.sh` зелёный.
Проверка через живьё (опционально): поднять `./gradlew bootRun` и дёрнуть `POST /v1/tasks/gen`.

## Что нельзя менять без поручения

- Контракт API (`controllers/*`, DTO) и `scripts/check.sh` / `build.gradle.kts`.
- Чужие прототипы (`taskimpl/t3`, `taskimpl/t4`, `spc-res/03-00`, `spc-res/04-00`).
- Новые прототипы — только в свободных слотах (`spc-res/NN-MM/` + `@Register`), слот согласовать со студентом.

## Ограничения

- Java 26, Spring Boot 4.1.1. Тесты — JUnit 5 (`./gradlew test`).
- Mustache-шаблон `statement.mustache` обязателен для каждого `@TemplateDef` (биндится по пути `spc-res/NN-MM/templates/TT/`).
- Commit только после приёмки студентом (`git diff` + зелёный `check.sh` показать до коммита).
