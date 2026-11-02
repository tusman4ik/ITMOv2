import React, { useState } from "react";
import { ChevronDown, ChevronRight } from "lucide-react";
import { Row } from "../api/types";

interface TestRowViewProps {
  row: Row;
  index: number;
}

export const TestRowView: React.FC<TestRowViewProps> = ({ row, index }) => {
  const status = row.status || "OK";
  const isFailed = status !== "OK";
  // По умолчанию раскрываем строку с ошибкой, чтобы пользователь сразу видел причину
  const [isExpanded, setIsExpanded] = useState(isFailed);
  const hasDiff = isFailed || row.input || row.stdout || row.expected;

  return (
    <>
      <tr
        style={{ cursor: hasDiff ? "pointer" : "default" }}
        onClick={() => hasDiff && setIsExpanded(!isExpanded)}
      >
        <td style={{ width: "36px" }}>
          {hasDiff ? (
            isExpanded ? (
              <ChevronDown size={14} color="var(--text-dim)" />
            ) : (
              <ChevronRight size={14} color="var(--text-dim)" />
            )
          ) : (
            <span style={{ display: "inline-block", width: 14 }} />
          )}
        </td>
        <td>#{index + 1}</td>
        <td>
          <span className={`verdict-tag ${status}`}>{status}</span>
        </td>
        <td>
          <span title={row.input} style={{ maxWidth: "140px", display: "inline-block", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
            {row.input ? row.input.replace(/\n/g, " ") : "—"}
          </span>
        </td>
        <td>
          <span style={{ color: status === "OK" ? "var(--status-ok)" : "var(--status-wa)" }}>
            {row.stdout !== undefined ? row.stdout.trim() : "—"}
          </span>
        </td>
        <td>
          <span style={{ color: "var(--text-muted)" }}>
            {row.expected !== undefined ? row.expected.trim() : "—"}
          </span>
        </td>
        <td>
          {row.realTimeSec !== undefined
            ? `${(row.realTimeSec * 1000).toFixed(0)} ms`
            : row.cpuTimeSec !== undefined
            ? `${(row.cpuTimeSec * 1000).toFixed(0)} ms`
            : "—"}
        </td>
        <td>
          {row.memoryKb !== undefined ? `${(row.memoryKb / 1024).toFixed(1)} MB` : "—"}
        </td>
      </tr>

      {isExpanded && (
        <tr>
          <td colSpan={8} style={{ background: "rgba(0, 0, 0, 0.25)", padding: "0.75rem 1rem" }}>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "1rem", fontSize: "0.78rem" }}>
              <div>
                <strong style={{ color: "var(--text-dim)", display: "block", marginBottom: "4px" }}>Входные данные:</strong>
                <pre style={{ background: "var(--bg-app)", padding: "6px 8px", borderRadius: 4, overflowX: "auto", margin: 0 }}>
                  {row.input || "—"}
                </pre>
              </div>
              <div>
                <strong style={{ color: "var(--text-dim)", display: "block", marginBottom: "4px" }}>Вывод программы:</strong>
                <pre style={{ background: "var(--bg-app)", padding: "6px 8px", borderRadius: 4, overflowX: "auto", margin: 0, color: status === "OK" ? "var(--accent-emerald)" : "var(--accent-rose)" }}>
                  {row.stdout !== undefined && row.stdout !== "" ? row.stdout : "<пусто>"}
                </pre>
              </div>
              <div>
                <strong style={{ color: "var(--text-dim)", display: "block", marginBottom: "4px" }}>Ожидаемый ответ:</strong>
                <pre style={{ background: "var(--bg-app)", padding: "6px 8px", borderRadius: 4, overflowX: "auto", margin: 0 }}>
                  {row.expected || "—"}
                </pre>
              </div>
            </div>
            {row.checkerMsg && (
              <div style={{ marginTop: "0.5rem", fontSize: "0.8rem", color: isFailed ? "var(--accent-rose)" : "var(--accent-amber)" }}>
                <strong>{status === "RE" ? "Ошибка исполнения (stderr):" : "Сообщение чекера:"}</strong>
                <pre style={{ background: "rgba(244, 63, 94, 0.15)", padding: "6px 8px", borderRadius: 4, overflowX: "auto", marginTop: "4px", color: "var(--accent-rose)", whiteSpace: "pre-wrap" }}>
                  {row.checkerMsg}
                </pre>
              </div>
            )}
            <div style={{ marginTop: "0.4rem", fontSize: "0.75rem", color: "var(--text-muted)", display: "flex", gap: "1.5rem" }}>
              <span>Время: {row.realTimeSec !== undefined ? `${(row.realTimeSec * 1000).toFixed(1)} ms` : "—"}</span>
              <span>Память: {row.memoryKb !== undefined ? `${(row.memoryKb / 1024).toFixed(2)} MB (${row.memoryKb} KB)` : "—"}</span>
              <span>Код возврата: {row.exitCode !== undefined ? row.exitCode : "—"}</span>
            </div>
          </td>
        </tr>
      )}
    </>
  );
};
