import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { useRef, useMemo } from "react";
import { Send, Code2, RotateCcw, AlertTriangle } from "lucide-react";
export const CodeEditor = ({ code, onChangeCode, onSubmit, onResetToTemplate, isSubmitting, validationError, }) => {
    const textareaRef = useRef(null);
    // Split code into lines for the line gutter
    const lineCount = useMemo(() => {
        return Math.max(1, code.split("\n").length);
    }, [code]);
    const handleKeyDown = (e) => {
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
    return (_jsxs("div", { className: "editor-wrapper", children: [_jsxs("div", { className: "editor-header", children: [_jsxs("span", { className: "editor-title", children: [_jsx(Code2, { size: 16, color: "var(--primary)" }), "\u0420\u0435\u0434\u0430\u043A\u0442\u043E\u0440 \u0440\u0435\u0448\u0435\u043D\u0438\u044F (Python 3)"] }), _jsxs("div", { className: "editor-actions", children: [_jsxs("button", { className: "btn btn-ghost btn-sm", onClick: onResetToTemplate, title: "\u0412\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u044C \u0448\u0430\u0431\u043B\u043E\u043D \u043A\u043E\u0434\u0430", disabled: isSubmitting, children: [_jsx(RotateCcw, { size: 13 }), _jsx("span", { children: "\u0421\u0431\u0440\u043E\u0441" })] }), _jsx("button", { className: "btn btn-success btn-sm", onClick: onSubmit, disabled: isSubmitting, title: "\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C \u0440\u0435\u0448\u0435\u043D\u0438\u0435 \u043D\u0430 \u0442\u0435\u0441\u0442\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u0435", children: isSubmitting ? (_jsxs(_Fragment, { children: [_jsx("span", { className: "spin", children: "\u2B6E" }), _jsx("span", { children: "\u041F\u0440\u043E\u0432\u0435\u0440\u043A\u0430..." })] })) : (_jsxs(_Fragment, { children: [_jsx(Send, { size: 13 }), _jsx("span", { children: "\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C \u0440\u0435\u0448\u0435\u043D\u0438\u0435" })] })) })] })] }), validationError && (_jsxs("div", { style: {
                    background: "rgba(244, 63, 94, 0.1)",
                    color: "var(--accent-rose)",
                    padding: "0.4rem 1rem",
                    fontSize: "0.8rem",
                    display: "flex",
                    alignItems: "center",
                    gap: "0.5rem",
                    borderBottom: "1px solid rgba(244, 63, 94, 0.2)"
                }, children: [_jsx(AlertTriangle, { size: 14 }), validationError] })), _jsxs("div", { className: "code-container", children: [_jsx("div", { className: "code-line-numbers", children: Array.from({ length: lineCount }, (_, i) => (_jsx("div", { children: i + 1 }, i + 1))) }), _jsx("textarea", { ref: textareaRef, className: "code-textarea", value: code, onChange: (e) => onChangeCode(e.target.value), onKeyDown: handleKeyDown, placeholder: "# \u041D\u0430\u043F\u0438\u0448\u0438\u0442\u0435 \u0437\u0434\u0435\u0441\u044C \u0440\u0435\u0448\u0435\u043D\u0438\u0435 \u0437\u0430\u0434\u0430\u0447\u0438 \u043D\u0430 Python 3...", spellCheck: false, autoComplete: "off", autoCorrect: "off", autoCapitalize: "off" })] })] }));
};
