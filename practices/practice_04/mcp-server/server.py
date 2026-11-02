#!/usr/bin/env python3
"""MCP-сервер практики 4: spc-probe. Только стандартная библиотека.

Tool `spc-probe`: серия POST /v1/tasks/gen на живой spc-task-service.
  attemptCnt   — запуски с РАЗНЫМИ seed (breadth: сервис отвечает на новое).
  attemptDepth — повторы с ТЕМ ЖЕ seed (идемпотентность: один seed = один ответ).

Успех: {"ok": True, "depthMatch": ..., ...}.
Ошибка (сервис недоступен / 404 / 400) — тоже структурированный ответ
с {"ok": False, "error": ...}, чтобы агент мог показать оба сценария.

Протокол: MCP over stdio, JSON-RPC 2.0, сообщения разделены переводами строк.
Логи — только в stderr, stdout чистый под протокол.
"""

import hashlib
import json
import sys
import urllib.error
import urllib.request

TOOL_NAME = "spc-probe"
TIMEOUT = 10


def log(*args):
    print(*args, file=sys.stderr, flush=True)


def post_gen(url, proto, seed):
    body = json.dumps({"id": proto, "seed": seed}).encode()
    req = urllib.request.Request(
        url.rstrip("/") + "/v1/tasks/gen",
        data=body,
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            payload = json.load(resp)
            return {"ok": True, "statement": payload.get("statement", "")}
    except urllib.error.HTTPError as e:
        detail = e.read().decode(errors="replace")[:300]
        return {"ok": False, "error": f"HTTP {e.code}: {detail}"}
    except Exception as e:  # URLError, TimeoutError, ...
        return {"ok": False, "error": f"service unreachable ({url}): {e}"}


def short_hash(text):
    return hashlib.sha256(text.encode()).hexdigest()[:12]


def run_probe(params):
    url = params.get("url", "http://localhost:8080")
    proto = params.get("prototypeId")
    if not isinstance(proto, dict):
        return {"ok": False, "error": "prototypeId must be an object {exam,subject,number,prototype}"}
    seed_base = int(params.get("seedBase", 2000))
    attempt_cnt = max(1, int(params.get("attemptCnt", 3)))
    attempt_depth = max(1, int(params.get("attemptDepth", 2)))

    breadth = []
    for i in range(attempt_cnt):
        seed = seed_base + i
        r = post_gen(url, proto, seed)
        if not r["ok"]:
            return {"ok": False, "error": r["error"], "phase": f"breadth seed={seed}"}
        breadth.append({"seed": seed, "hash": short_hash(r["statement"]),
                        "chars": len(r["statement"])})

    repeats = []
    for _ in range(attempt_depth):
        r = post_gen(url, proto, seed_base)
        if not r["ok"]:
            return {"ok": False, "error": r["error"], "phase": f"depth seed={seed_base}"}
        repeats.append(short_hash(r["statement"]))
    depth_match = len(set(repeats)) == 1

    return {"ok": True, "depthMatch": depth_match,
            "depthRepeats": attempt_depth, "breadth": breadth}


def tool_schema():
    return {"name": TOOL_NAME,
            "description": "Probe spc-task-service generation: breadth over seeds + idempotency repeats",
            "inputSchema": {"type": "object",
                            "properties": {
                                "url": {"type": "string", "default": "http://localhost:8080"},
                                "prototypeId": {"type": "object"},
                                "seedBase": {"type": "integer", "default": 2000},
                                "attemptCnt": {"type": "integer", "default": 3},
                                "attemptDepth": {"type": "integer", "default": 2}},
                            "required": ["prototypeId"]}}


def handle(msg):
    method = msg.get("method")
    mid = msg.get("id")
    if method == "initialize":
        return {"jsonrpc": "2.0", "id": mid,
                "result": {"protocolVersion": "2024-11-05",
                           "capabilities": {"tools": {}},
                           "serverInfo": {"name": "spc-probe", "version": "1.0.0"}}}
    if method == "tools/list":
        return {"jsonrpc": "2.0", "id": mid, "result": {"tools": [tool_schema()]}}
    if method == "tools/call":
        p = msg.get("params", {})
        if p.get("name") != TOOL_NAME:
            return {"jsonrpc": "2.0", "id": mid,
                    "error": {"code": -32602, "message": f"unknown tool {p.get('name')}"}}
        result = run_probe(p.get("arguments", {}))
        return {"jsonrpc": "2.0", "id": mid,
                "result": {"content": [{"type": "text", "text": json.dumps(result, ensure_ascii=False)}]}}
    if method in ("notifications/initialized",):
        return None
    if mid is None:
        return None
    return {"jsonrpc": "2.0", "id": mid,
            "error": {"code": -32601, "message": f"unknown method {method}"}}


def main():
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            resp = handle(json.loads(line))
        except Exception as e:  # never break framing
            log("handle error:", e)
            continue
        if resp is not None:
            sys.stdout.write(json.dumps(resp, ensure_ascii=False) + "\n")
            sys.stdout.flush()


if __name__ == "__main__":
    main()
