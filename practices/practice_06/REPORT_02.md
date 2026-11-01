# Отчёт 2. Повторные атаки после исправлений

После выполнения аудита (см. [REPORT_01.md](./REPORT_01.md)) в микросервис `lab/service.py` были внесены архитектурные исправления, реализующие принцип эшелонированной защиты (**Defense-in-Depth**) и строгий контроль доступа на стороне сервера.

Ниже приведены результаты повторного тестирования всех выявленных векторов атак, подтверждающие их полную нейтрализацию.

---

## 1. Сравнительная матрица результатов атак

| ID | Вектор атаки | Эндпоинт | Исходное состояние (REPORT_01) | Состояние после исправлений (REPORT_02) | Статус защиты |
|---|---|---|---|---|:---:|
| **V1** | SQLi & IDOR | `GET /profile` | **200 OK** (слив всех профилей через `1 OR 1=1`) | **403 Forbidden** (или 400 при невалидном int). Просмотр только своего ID. | **Устранено** |
| **V2** | SQLi | `GET /search` | **200 OK** (извлечение данных через UNION SELECT) | **200 OK** (параметризованный LIKE-поиск `?`, спецсимволы SQL не исполняются). | **Устранено** |
| **V3** | Role Spoofing | `GET /admin/export` | **200 OK** (обход проверки заголовком `X-Role: staff`) | **403 Forbidden** (заголовок игнорируется, роль берется строго из сессии токена). | **Устранено** |
| **V4** | Price Tampering | `POST /orders` | **201 Created** (заказ кресла за 0.01 RUB) | **201 Created** (цена берется строго из `self.products`, клиентский `price` игнорируется). | **Устранено** |
| **V5** | Path Traversal & IDOR | `GET /files` | **200 OK** (чтение `private/operator-note.txt` и чужих чеков) | **400 / 403 Forbidden** (жесткая привязка к сессии `files/{username}/{filename}`, имя файла санитизировано). | **Устранено** |
| **V6** | Stored XSS | `POST /reviews` & `/reviews/view` | **200 OK** (исполняемый `<script>` в HTML) | **200 OK** (HTML экранирован через `html.escape`, теги выводятся как безопасный текст). | **Устранено** |
| **V7** | Sensitive Data Exposure | `GET /debug/logs` | **200 OK** (публичный слив токенов и паролей) | **403 Forbidden** для клиентов (доступен только роли `staff`, пароли маскируются как `***`). | **Устранено** |
| **V8** | Direct Dangerous Tool Execution | `POST /assistant/execute` | **200 OK** (удаление `delete_tree`, экспорт `export_profiles`) | **403 Forbidden** (инструменты `delete_tree` и `export_profiles` удалены; чтение файлов ограничено сессией). | **Устранено** |
| **V9** | Prompt Injection Impact | `GET /assistant/context` | **Эксплуатируемо** (инструкции из отзывов могли вызвать деструктивное действие на сервере) | **Нейтрализовано на уровне бэкенда** (даже если LLM вернет вредоносный JSON, сервер отклонит вызов). | **Устранено** |

---

## 2. Верификация исправлений (Proof of Fix)

Все тесты повторно выполнены против локального сервера (`http://127.0.0.1:8094`). Сырые результаты сохранены в `evidence/after/vulnerabilities_fixed_summary.json`.

### 2.1. V1: Проверка SQLi и IDOR в `/profile`
- **Запрос без авторизации:** `GET /profile?id=1` -> **401 Unauthorized**.
- **Запрос Alice с попыткой SQL-инъекции (`1 OR 1=1`):**
  ```http
  GET /profile?id=1%20OR%201=1 HTTP/1.1
  Host: 127.0.0.1:8094
  X-Token: <token_alice>
  ```
- **Ответ сервера:**
  ```json
  {"error": "invalid profile id"}
  ```
  *(Status: 400 Bad Request, параметр валидируется как целое число и биндится через `?`)*.
- **Запрос Alice с попыткой прочитать профиль Bob (`id=2`):**
  ```json
  {"error": "forbidden"}
  ```
  *(Status: 403 Forbidden, клиент может смотреть только собственный профиль `id=1`)*.

### 2.2. V2: Проверка SQLi в `/search`
- **Запрос:**
  ```http
  GET /search?q=%27%20UNION%20SELECT%20id%2Cname%2Cemail%2Caddress%20FROM%20profiles%20WHERE%201%3D1-- HTTP/1.1
  Host: 127.0.0.1:8094
  X-Token: <token_alice>
  ```
- **Ответ сервера (Status: 200 OK):** `[]` (Пустой список, так как поиск выполняется через параметризованный запрос `SELECT * FROM profiles WHERE name LIKE ?`, где весь пейлоад воспринимается как строковый литерал подстроки имени).

### 2.3. V3: Проверка Role Spoofing в `/admin/export`
- **Запрос клиента с поддельным заголовком:**
  ```http
  GET /admin/export HTTP/1.1
  Host: 127.0.0.1:8094
  X-Token: <token_alice>
  X-Role: staff
  ```
- **Ответ сервера (Status: 403 Forbidden):**
  ```json
  {"error": "staff only"}
  ```
  *(Сервер игнорирует заголовок клиента `X-Role` и берет роль исключительно из проверенной серверной сессии `session["role"]`)*.

### 2.4. V4: Проверка подделки цены в `/orders`
- **Запрос с попыткой передать `price: 0.01`:**
  ```http
  POST /orders HTTP/1.1
  Host: 127.0.0.1:8094
  X-Token: <token_alice>
  Content-Type: application/json

  {"product": "chair", "price": 0.01}
  ```
- **Ответ сервера (Status: 201 Created):**
  ```json
  {"id": 3, "owner": 1, "product": "chair", "price": 5000}
  ```
  *(Цена принудительно установлена из доверенного серверного каталога `self.products["chair"] == 5000`)*.

### 2.5. V5: Проверка Path Traversal и IDOR в `/files`
- **Попытка обхода каталога:** `GET /files?path=..%2Fprivate%2Foperator-note.txt` с токеном Alice.
- **Ответ сервера:** **400 Bad Request** (`{"error": "invalid path"}`). Сервер изолирует путь шаблоном `files/{session['username']}/{Path(path).name}` и блокирует любые символы разделителей директорий (`/`, `\`).
- **Попытка чтения чека Bob:** `GET /files?user=bob&path=receipt.txt` с токеном Alice.
- **Ответ сервера:** Возвращается чек Alice, так как параметр `user` из запроса игнорируется — путь строится исключительно на основе логина текущей сессии.

### 2.6. V6: Проверка экранирования XSS в `/reviews/view`
- **Сохраненный отзыв:** `<script>alert("xss")</script>`
- **Ответ сервера (Status: 200 OK):**
  ```html
  <html><body><p>&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;</p></body></html>
  ```
  *(Все опасные HTML-символы экранированы с помощью `html.escape()`, исполнение скрипта в браузере предотвращено)*.

### 2.7. V7: Проверка доступа к `/debug/logs`
- **Запрос клиента:** `GET /debug/logs` с токеном Alice -> **403 Forbidden** (`{"error": "staff only"}`).
- **Запрос сотрудника:** Доступ разрешен, пароли в логах маскируются (`body['password'] = '***'`).

### 2.8. V8: Проверка вызова опасных инструментов в `/assistant/execute`
- **Попытка вызвать `delete_tree`:**
  ```http
  POST /assistant/execute HTTP/1.1
  Host: 127.0.0.1:8094
  X-Token: <token_alice>
  Content-Type: application/json

  {"decision": {"tool": "delete_tree", "arguments": {"path": "files"}}}
  ```
  - **Ответ:** **403 Forbidden** (`{"error": "forbidden tool: delete_tree"}`).
- **Попытка вызвать `export_profiles` от клиента:**
  - **Ответ:** **403 Forbidden** (`{"error": "staff only tool"}`).
- **Попытка прочитать чужой файл через `read_file`:**
  - **Ответ:** **403 Forbidden** (`{"error": "access denied to file"}`).

---

## 3. Автоматизированное тестирование

Для непрерывного контроля безопасности в `lab/test_service.py` был добавлен тестовый набор `SecurityTest`, включающий регрессионные проверки по каждому вектору.

Команда запуска проверки:
```bash
make step6
```

**Результат прогона:**
```text
Ran 25 tests in 0.543s

OK
```
Все 25 тестов (включая 16 базовых функциональных и 9 новых проверок безопасности) успешно пройдены.
