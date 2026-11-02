# mcp-server: spc-probe

Собственный MCP-сервер практики 4. Один полезный tool, только стандартная
библиотека Python, транспорт — stdio (запуск: `python3 server.py`).

Подключение — `project/opencode.json`, секция `mcp.spc-probe`
(`type: local`, абсолютный путь к `server.py`).

## Tool `spc-probe`

Прозвон живого spc-task-service (`POST /v1/tasks/gen`):

| Параметр | Смысл |
|---|---|
| `url` | база сервиса, по умолчанию `http://localhost:8080` |
| `prototypeId` | `{exam, subject, number, prototype}` |
| `seedBase` | начальный seed, по умолчанию `2000` |
| `attemptCnt` | запуски с РАЗНЫМИ seed (`seedBase..`) — breadth |
| `attemptDepth` | повторы с ТЕМ ЖЕ seed — идемпотентность |

Успех: `{"ok": true, "depthMatch": true/false, "breadth": [...]}`.
Ошибка — тот же envelope с `ok: false`: сервис недоступен
(`connection refused`), `HTTP 404` (неизвестный прототип), `HTTP 400` (валидация).

## Проверено

- `../proofs/mcp-direct.log` — прямой диалог по протоколу: успех
  (`depthMatch: true`, breadth 3 seed) + ошибка (`connection refused :9999`).
- `../proofs/agent-mcp.jsonl` — вызов агентом через OpenCode
  (`spc-probe_spc-probe`, `EGE/CS/1/1`, `depthMatch: true`).
- `../proofs/gen-01-01.json`, `check-01-01-right.json` (`total: 1`),
  `check-01-01-wrong.json` (`total: 0`) — живой цикл нового прототипа.
