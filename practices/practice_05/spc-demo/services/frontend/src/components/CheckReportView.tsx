import React from "react";
import { CheckCircle, XCircle, Terminal, HelpCircle } from "lucide-react";
import { VerificationReport } from "../api/types";
import { TestRowView } from "./TestRowView";

interface CheckReportViewProps {
  report?: VerificationReport;
  status: "idle" | "submitting" | "pending" | "completed" | "error";
  errorMessage?: string;
}

export const CheckReportView: React.FC<CheckReportViewProps> = ({
  report,
  status,
  errorMessage,
}) => {
  if (status === "submitting" || status === "pending") {
    return (
      <div className="report-drawer">
        <div className="empty-state pulse">
          <Terminal size={32} className="empty-state-icon" color="var(--primary)" />
          <h4 style={{ fontWeight: 600, marginBottom: "0.25rem" }}>Исполнение в песочнице Coderunner...</h4>
          <p style={{ fontSize: "0.82rem", color: "var(--text-dim)" }}>
            Код запущен на наборе тестовых данных. Ожидание ответа...
          </p>
        </div>
      </div>
    );
  }

  if (status === "error" || errorMessage) {
    return (
      <div className="report-drawer">
        <div className="empty-state">
          <XCircle size={32} className="empty-state-icon" color="var(--accent-rose)" />
          <h4 style={{ color: "var(--accent-rose)", fontWeight: 600, marginBottom: "0.25rem" }}>
            Ошибка при тестировании
          </h4>
          <p style={{ fontSize: "0.82rem", color: "var(--text-dim)" }}>
            {errorMessage || "Произошла ошибка при обращении к сервису проверки."}
          </p>
        </div>
      </div>
    );
  }

  if (!report || status === "idle") {
    return (
      <div className="report-drawer">
        <div className="empty-state">
          <HelpCircle size={32} className="empty-state-icon" />
          <h4 style={{ fontWeight: 600, marginBottom: "0.25rem" }}>Проверка ещё не запускалась</h4>
          <p style={{ fontSize: "0.82rem", color: "var(--text-dim)" }}>
            Нажмите кнопку «Отправить решение», чтобы протестировать программу на наборе тестов.
          </p>
        </div>
      </div>
    );
  }

  const isFullScore = report.score === report.maxScore && report.allPassed;

  return (
    <div className="report-drawer">
      <div className="report-header">
        <div className="report-score-pill">
          <span className={`score-badge ${isFullScore ? "full" : "zero"}`}>
            {report.score} / {report.maxScore}
          </span>
          <div style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
            {isFullScore ? (
              <>
                <CheckCircle size={16} color="var(--accent-emerald)" />
                <span style={{ fontWeight: 600, fontSize: "0.875rem", color: "var(--accent-emerald)" }}>
                  Все тесты пройдены (Полный балл)
                </span>
              </>
            ) : (
              <>
                <XCircle size={16} color="var(--accent-rose)" />
                <span style={{ fontWeight: 600, fontSize: "0.875rem", color: "var(--accent-rose)" }}>
                  Решение содержит ошибки
                </span>
              </>
            )}
          </div>
        </div>

        {report.criteria && report.criteria.length > 0 && (
          <div style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>
            {report.criteria.map((c, i) => (
              <span key={i} style={{ marginLeft: "0.75rem" }}>
                {c.name || "Критерий"}: {c.userScore ?? 0}/{c.maxScore ?? 0}
              </span>
            ))}
          </div>
        )}
      </div>

      {report.compilerLog && (
        <div className="compiler-log-card">
          <div style={{ fontWeight: 700, marginBottom: "4px" }}>Лог компилятора / ошибки:</div>
          {report.compilerLog}
        </div>
      )}

      {report.error && (
        <div className="compiler-log-card">
          <div style={{ fontWeight: 700, marginBottom: "4px" }}>Системная ошибка:</div>
          {report.error}
        </div>
      )}

      {!isFullScore && (
        <div style={{ padding: "0.5rem 1rem", background: "rgba(244, 63, 94, 0.1)", borderLeft: "3px solid var(--accent-rose)", marginBottom: "0.75rem", fontSize: "0.82rem", color: "var(--accent-rose)", borderRadius: 4 }}>
          <strong>Режим fail-fast:</strong> выполнение тестов остановлено на первом непрошедшем тесте. Последующие тесты не запускались. Все ошибки и детали упавшего теста показаны в таблице ниже.
        </div>
      )}

      <div className="report-table-scroll">
        <table className="test-matrix-table">
          <thead>
            <tr>
              <th style={{ width: "36px" }}></th>
              <th>Тест</th>
              <th>Вердикт</th>
              <th>Входные данные</th>
              <th>Вывод программы</th>
              <th>Эталон</th>
              <th>Время</th>
              <th>Память</th>
            </tr>
          </thead>
          <tbody>
            {report.rows.map((row, idx) => (
              <TestRowView key={row.caseId || idx} row={row} index={idx} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
