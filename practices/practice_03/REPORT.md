# Отчёт: локальный помощник (практика 3)

Ветка: `practice_03`. Коммитов нет, все изменения незакоммичены. Модель одна на все этапы по заданию: `qwen3.5:9b`.

## Окружение

* ОС: Arch Linux, kernel 7.1.8-arch1-3. CPU: Intel Core Ultra 7 255H, 16 потоков. RAM: 30 GB. Дискретной GPU нет (только Intel Arc iGPU) — инференс полностью на CPU.
* Ollama 0.31.2 (сервер `localhost:11434`, проверено `curl /api/tags`). OpenCode 1.18.34. Python 3.14.7 (требование 3.10+ выполнено).
* Базовые веса: `qwen3.5:9b`, ID `56671c2ab938`, 6.6 GB, GGUF `Q4_K_M`, 9.0B параметров, нативное окно 262144.
* `itmo-local` (ID `3b0a84a4e5d1`): `FROM qwen3.5:9b`, `num_ctx 4096`, `temperature 0.2`, SYSTEM из `lab/Modelfile`. Назначение — прямые API-запросы.
* `itmo-agent` (ID `021e981aaf71`): `FROM qwen3.5:9b`, `num_ctx 8192` (см. ограничения), `temperature 0.2`, без SYSTEM. Назначение — агент `local-guide` в OpenCode.
* Почему эта конфигурация: модель задана (`qwen3.5:9b`); контекст ограничен фактически нужным (`demo/` — несколько маленьких файлов, 8k с запасом); thinking отключён только на агентском пути (см. ниже), API-этап идёт с родным поведением модели.

## Воспроизведение

```bash
cd practices/practice_03/lab
make test                                   # 3/3 OK (unittest)
ollama create itmo-local -f Modelfile
ollama create itmo-agent -f Modelfile.agent
ollama show itmo-local / itmo-agent         # сверка num_ctx, temperature, Q4_K_M
curl --fail http://localhost:11434/api/tags
python3 experiment.py --mode baseline --model itmo-local --output results/baseline.json
python3 experiment.py --mode system   --model itmo-local --output results/system.json
opencode run --dir demo --agent local-guide --model ollama/itmo-agent --format json "<промпт>" > results/qN.jsonl
```

`make test` выполняется: `Standard library only: ready`, `Ran 3 tests ... OK`.
`lab/demo/opencode.json` настроен: провайдер `ollama` на `http://localhost:11434/v1`, модель `ollama/itmo-agent`, агент `local-guide` (только `read/glob/grep`, остальное `deny`, `steps: 8`). Изменение относительно исходника: у модели `itmo-agent` добавлено `"options": {"reasoningEffort": "none"}` и лимит контекста приведён к реальному `8192` (было `65536`). Входные файлы `demo/` (код, `repo-system.txt`) не менялись.

## API-эксперимент (experiment.py, think=false, seed 42)

Вопрос один: «Какая CI-система запускает тесты проекта?», контекст — `demo/README.md`.

| Режим | Ответ | Метрики |
|---|---|---|
| baseline (`results/baseline.json`) | «В предоставленных материалах нет ответа.» | wall 17.3с, 3.8 tok/s, eval 9 токенов |
| system (`results/system.json`) | «В предоставленных материалах нет ответа» + основание: в тексте только `make test`, CI не описана | wall 32.9с, 3.6 tok/s, eval 65 токенов |

Оба ответа корректны (CI в репозитории действительно нет), выдумывания нет. System-вариант даёт grounding, baseline — короткий отказ.

## Агентский этап: 5 вопросов из QUESTIONS.md

Каждый вопрос — отдельная сессия `opencode run --dir demo --agent local-guide --model ollama/itmo-agent`. Эталоны подготовлены по коду заранее и модели не передавались.

| # | Вопрос | Эталон (file:line) | Ответ модели | Вызовы инструментов | Вердикт |
|---|---|---|---|---|---|
| 1 | Как запустить тесты? | `make test`; `demo/Makefile:2-3` (`python3 -m unittest -v`), упомянуто в `demo/README.md:6` | `make test`, `README.md:6`, `Makefile:1-3` | `read README.md`, `read Makefile` (подтверждены в `q1.jsonl`), 6:37 | Верно, мелочь: «запускает pytest через unittest» — неточно, там чистый `unittest`, без pytest |
| 2 | Пустое имя подписчика? | `ValueError("empty name")`, `service.py:5-6`; тест `test_service.py:13-15` | `ValueError("empty name")`, `service.py:4-6` с цитатой кода | `read service.py`, `read test_service.py`, 8:01 | Верно, строки точные |
| 3 | Где unsubscribe? (ложная предпосылка) | Не существует: в `service.py` только `subscribe` + `subscribers` | «Ничего с unsubscribe не найдено», предложено проверить написание | `grep unsubscribe` (пусто), 6:43 | Верно, предпосылка отвергнута |
| 4 | Какая CI запускает тесты? (без ответа) | Отсутствует; только локальный `make test` (`README.md:6`) | «Информация о CI отсутствует», ссылка на `README.md:1-9`, упомянут `make test:6` | `read README.md`, 7:16 | Верно, без выдумок |
| 5 | Сохраняются ли подписки после рестарта? | Нет: `subscribers = set()` в памяти процесса, `service.py:1` | «НЕ сохраняются», `service.py:1`, объяснён механизм | `read service.py`, 7:12 | Верно |

Файлы: `lab/results/q1.jsonl … q5.jsonl` (ответы + события инструментов, без внутренних рассуждений).

## Скорость

* API (`think=false`): 11–33с на ответ, ~3.6–3.8 tok/s на CPU.
* Агент (`reasoningEffort:none`): ~6.5–8 мин на вопрос (2 чтения + ответ ≈ 3 шага по ~3 мин). Замер по пяти сессиям: 6:37 / 8:01 / 6:43 / 7:16 / 7:12.
* Холодный старт отдельно не замерялся (модель держалась загруженной между прогонами).

## Ограничения и обнаруженная граница (проверено замерами)

1. Thinking на CPU — непроходим для агента: `opencode run` с thinking-моделью дважды давал 0 байт за 10 минут.
2. `PARAMETER think false` в Modelfile невозможен: `ollama create` отвечает `Error: unknown parameter 'think'` (совпадает с апстрим-issue ollama#14809).
3. `think:false` через `/v1` игнорируется для `qwen3.5`: замер `max_tokens=512` → 165.3с, все 512 токенов съедены, ответ пустой. Маркер `/no_think` в промпте тоже не работает: 32.2с, thinking 247 символов, ответ пустой.
4. Рабочий рычаг: `reasoning_effort: "none"` через `/v1` → `READY` за 11.2с, 2 токена, без reasoning. Доставлено через `options.reasoningEffort: "none"` в `demo/opencode.json` (есть в документации OpenCode «Configure models»): было 0 байт за 10+ минут, стало ответ за ~5 минут.
5. Без thinking модель плохо держит tool-дисциплину: свободный промпт («прочитай файлы…») дал текст без единого вызова инструментов. Лечится директивным промптом («сначала вызови read…, текст только после») — все 5 вопросов после этого с подтверждёнными вызовами.
6. `num_ctx 65536` для `9b` на CPU не влезает в разумное время (8.8 GB резидентно, 0 байт за 10 минут) → снижен до `8192` (6.4 GB), для `demo/` достаточно с запасом.
7. Не проверено: большие репозитории, контекст 64k на машине с GPU, стабильность tool-вызовов на длинных сессиях (>8 шагов).

## Вывод

Оставляю связку `itmo-local` (API, 4k) + `itmo-agent` (OpenCode `local-guide`, 8k, `reasoningEffort:none`) на весах `qwen3.5:9b Q4_K_M`. На пяти контрольных вопросах (включая ложную предпосылку про `unsubscribe` и вопрос без ответа про CI) модель не выдумала ничего, все ответы сошлись с эталонами, каждый ответ подтверждён вызовом `read`/`grep`. Цена: ~7 минут на вопрос на CPU и обязательные директивные промпты для вызова инструментов. Для быстрой интерактивной работы этого железа не хватает — для неё нужен либо GPU, либо модель меньше.
