import React, { useRef, useMemo } from "react";
import { Send, Code2, RotateCcw, AlertTriangle } from "lucide-react";

interface CodeEditorProps {
  code: string;
  onChangeCode: (code: string) => void;
  onSubmit: () => void;
  onResetToTemplate: () => void;
  isSubmitting: boolean;
  validationError?: string;
}

export const CodeEditor: React.FC<CodeEditorProps> = ({
  code,
  onChangeCode,
  onSubmit,
  onResetToTemplate,
  isSubmitting,
  validationError,
}) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Split code into lines for the line gutter
  const lineCount = useMemo(() => {
    return Math.max(1, code.split("\n").length);
  }, [code]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Tab") {
      e.preventDefault();
      const target = e.currentTarget;
      const start = target.selectionStart;
      const end = target.selectionEnd;

      // 4 spaces for Python tab
      const tabSpaces = "    ";
      const updated = code.substring(0, start) + tabSpaces + code.substring(end);
      onChangeCode(updated);

      setTimeout(() => {
        if (textareaRef.current) {
          textareaRef.current.selectionStart = textareaRef.current.selectionEnd = start + 4;
        }
      }, 0);
    }
  };

  return (
    <div className="editor-wrapper">
      <div className="editor-header">
        <span className="editor-title">
          <Code2 size={16} color="var(--primary)" />
          Редактор решения (Python 3)
        </span>

        <div className="editor-actions">
          <button
            className="btn btn-ghost btn-sm"
            onClick={onResetToTemplate}
            title="Восстановить шаблон кода"
            disabled={isSubmitting}
          >
            <RotateCcw size={13} />
            <span>Сброс</span>
          </button>

          <button
            className="btn btn-success btn-sm"
            onClick={onSubmit}
            disabled={isSubmitting}
            title="Отправить решение на тестирование"
          >
            {isSubmitting ? (
              <>
                <span className="spin">⭮</span>
                <span>Проверка...</span>
              </>
            ) : (
              <>
                <Send size={13} />
                <span>Отправить решение</span>
              </>
            )}
          </button>
        </div>
      </div>

      {validationError && (
        <div style={{
          background: "rgba(244, 63, 94, 0.1)",
          color: "var(--accent-rose)",
          padding: "0.4rem 1rem",
          fontSize: "0.8rem",
          display: "flex",
          alignItems: "center",
          gap: "0.5rem",
          borderBottom: "1px solid rgba(244, 63, 94, 0.2)"
        }}>
          <AlertTriangle size={14} />
          {validationError}
        </div>
      )}

      <div className="code-container">
        <div className="code-line-numbers">
          {Array.from({ length: lineCount }, (_, i) => (
            <div key={i + 1}>{i + 1}</div>
          ))}
        </div>

        <textarea
          ref={textareaRef}
          className="code-textarea"
          value={code}
          onChange={(e) => onChangeCode(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="# Напишите здесь решение задачи на Python 3..."
          spellCheck={false}
          autoComplete="off"
          autoCorrect="off"
          autoCapitalize="off"
        />
      </div>
    </div>
  );
};
