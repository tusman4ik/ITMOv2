# Фреймворк задач spc-task-service (master, практика 4)

## Ноды (`task/nodes/`)

`Node<T>` — функциональный интерфейс с одним методом `get()`. Нода = именованная
ленивая величина варианта. Собираются рефлексией по аннотациям на полях генератора:

- `@NodeDef` — вычисляемая нода (`() -> ...`).
- `@PickIntNodeDef(min, max)` — случайное целое, тянет `ctx().random()`.
- `@PickBoolNodeDef`, `@PickNodeDef`, `@RetryNodeDef` — флаг / выбор / повторы.
- Правило: ноды собираются хендлером `NodeCollectHandler`, поле без инициализации —
  ошибка. Производные ноды читают один `spec`-нод, а не RNG напрямую.

## `@Register` (`task/registry/annotations/`)

`@Component` + `exam` (`OGE`/`EGE`), `subject` (`CS`), `number`, `prototype`.
Пара `(number, prototype)` задаёт путь `spc-res/NN-MM/` (`PrototypeId.path()`:
`"%02d-%02d"`). Реестр (`GeneratorRegistry`) находит генераторы сканированием
класспаса — новый класс с `@Register` подхватывается автоматически.

## Шаблоны (`task/templates/`)

- `@TemplateDef(TT)` на поле `Template` + `Template.of(форма, чекер).raw(ноды...)`.
- `TemplateId.path()` = `<NN-MM>/templates/<TT>` (`"%02d"`), туда кладётся
  `statement.mustache`. Рендер — `MustacheRenderEngine`: `{{{имяНоды}}}` подставляет
  значение ноды с таким именем поля (пример: `04-00/templates/99` содержит
  `{{{statementText}}}`).
- `TemplateResourceBindHandler` инжектит `resources` (proto-base + tmpl-base)
  в каждый шаблон. Шаблон без `statement.mustache` не отрендерится.

## Формы ответа (`task/ui/forms/`)

`AnswerForms.intForm()` / `stringForm()` / `boolForm()` / `doubleForm()` — как ученик
вводит ответ (`FormFactory`). Форма едет в `GenTaskResponse.inputForm` и обязана
соответствовать чекеру (`Checkers.exactInt` ↔ `intForm`, `trimmedString` ↔ `stringForm`).

## Чекеры (`task/checkers/Checkers.java`)

`exactInt(expected)`, `trimmedString(expected)` — фабрики `CheckerFactory`,
сравнивают ответ ученика со значением ноды, возвращают `CheckResult` с критериями.
Проверка живьём: `POST /v1/tasks/check` (`TaskController`).

## Ресурсы (`src/main/resources/spc-res/`)

```
spc-res/NN-MM/
  templates/TT/statement.mustache   # обязательно, {{переменные}} = имена нод
  data/*.yaml                       # опционально (пример: 01-03/data/*.yaml)
```

`ResourceResolver`/`Loader` отдают `text()`/`yaml()` относительно proto- и tmpl-base.
Новых потребителей `data/*.yaml` в коде пока нет — списки проще держать инлайн
константой в базовом классе (как `GraphConfig` в `Generator_4_0`); выбор задокументировать.

## Разобранный пример: `taskimpl/t4/Generator_4_0`

`@Register(OGE, CS, 4, 0)` → ресурсы `04-00/`. Изменяемые параметры — `GraphConfig`
(вершины, рёбра, веса). Варьируемое тянется нодами (`spec` от `build(random, CFG)`),
производные (`answer`, `statementText`, `matrix`) читают `spec`. Шаблон
`@TemplateDef(99)` → `04-00/templates/99/statement.mustache` (`{{{statementText}}}`),
форма `intForm`, чекер `exactInt(answer)`. Тест `Generator_4Test`: инварианты на
2000 seed + детерминизм + распределение. Этот каркас копировать для новых прототипов.
