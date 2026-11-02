import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState, useEffect, useCallback } from "react";
import { Moon, Sun, Layers } from "lucide-react";
import { generateTasks, recreateTask, checkTask, pollCheckResult, generateAttemptUuid, } from "./api/client";
import { loadDraft, saveDraft } from "./utils/draftStorage";
import { GeneratorControls } from "./components/GeneratorControls";
import { TaskList } from "./components/TaskList";
import { TaskStatement } from "./components/TaskStatement";
import { CodeEditor } from "./components/CodeEditor";
import { CheckReportView } from "./components/CheckReportView";
function getDefaultTemplateForPrototype(prototypeIdx) {
    if (prototypeIdx === 3 || prototypeIdx === 4 || prototypeIdx === 5) {
        return `# Программа получает на вход одно натуральное число.
x = int(input())

# Ваша логика обработки числа x:
ans = 0

print(ans)
`;
    }
    return `# Программа получает на вход последовательность целых чисел.
# Конец последовательности — число 0.

count = 0
s = 0

while True:
    x = int(input())
    if x == 0:
        break
    # Ваша логика обработки числа x:
    # ...

print(s)
`;
}
export const App = () => {
    const [theme, setTheme] = useState("dark");
    const [tasks, setTasks] = useState([]);
    const [selectedIndex, setSelectedIndex] = useState(0);
    const [isGenerating, setIsGenerating] = useState(false);
    const [isRecreating, setIsRecreating] = useState(false);
    const [validationError, setValidationError] = useState();
    const [filter, setFilter] = useState({
        count: 1,
        selectedPrototypes: [1, 2, 3, 4, 5, 999],
    });
    const toggleTheme = () => {
        const next = theme === "dark" ? "light" : "dark";
        setTheme(next);
        document.documentElement.setAttribute("data-theme", next);
    };
    // Generate initial or updated set of tasks
    const handleGenerate = useCallback(async () => {
        setIsGenerating(true);
        setValidationError(undefined);
        try {
            const resp = await generateTasks(filter.selectedPrototypes, filter.count);
            const items = resp.tasks.map((taskDto) => {
                const protoTemplate = getDefaultTemplateForPrototype(taskDto.instance.prototypeIdx);
                const defaultCode = taskDto.inputForm?.schema?.text || protoTemplate;
                const savedCode = loadDraft(taskDto.instance, defaultCode);
                return {
                    instance: taskDto.instance,
                    statementHtml: taskDto.statement,
                    defaultCode,
                    userCode: savedCode,
                    status: "idle",
                };
            });
            setTasks(items);
            setSelectedIndex(0);
        }
        catch (err) {
            console.error(err);
            alert(err instanceof Error ? err.message : "Не удалось сгенерировать задания");
        }
        finally {
            setIsGenerating(false);
        }
    }, [filter]);
    // Initial load: trigger generation once on mount
    useEffect(() => {
        handleGenerate();
    }, [handleGenerate]);
    const currentTask = tasks[selectedIndex];
    // Recreate the current task with same seed
    const handleRecreate = async () => {
        if (!currentTask)
            return;
        setIsRecreating(true);
        try {
            const resp = await recreateTask(currentTask.instance);
            if (resp.tasks.length > 0) {
                const updatedDto = resp.tasks[0];
                const protoTemplate = getDefaultTemplateForPrototype(updatedDto.instance.prototypeIdx);
                const defaultCode = updatedDto.inputForm?.schema?.text || protoTemplate;
                const savedCode = loadDraft(updatedDto.instance, defaultCode);
                setTasks((prev) => prev.map((t, idx) => idx === selectedIndex
                    ? {
                        ...t,
                        instance: updatedDto.instance,
                        statementHtml: updatedDto.statement,
                        defaultCode,
                        userCode: savedCode,
                        status: "idle",
                        checkReport: undefined,
                        errorMessage: undefined,
                    }
                    : t));
            }
        }
        catch (err) {
            console.error(err);
            alert(err instanceof Error ? err.message : "Не удалось пересоздать задание");
        }
        finally {
            setIsRecreating(false);
        }
    };
    // Code change in editor
    const handleCodeChange = (newCode) => {
        if (!currentTask)
            return;
        saveDraft(currentTask.instance, newCode);
        setTasks((prev) => prev.map((t, idx) => (idx === selectedIndex ? { ...t, userCode: newCode } : t)));
        if (validationError) {
            setValidationError(undefined);
        }
    };
    // Reset code to default template
    const handleResetToTemplate = () => {
        if (!currentTask)
            return;
        if (window.confirm("Сбросить введённый код к исходному шаблону?")) {
            handleCodeChange(currentTask.defaultCode);
        }
    };
    // Submit code for grading
    const handleSubmitCode = async () => {
        if (!currentTask)
            return;
        if (!currentTask.userCode.trim()) {
            setValidationError("Введите программный код решения перед отправкой на проверку.");
            return;
        }
        const attemptUuid = generateAttemptUuid();
        // Set status to submitting
        setTasks((prev) => prev.map((t, idx) => idx === selectedIndex
            ? {
                ...t,
                status: "submitting",
                lastAttemptId: attemptUuid,
                errorMessage: undefined,
            }
            : t));
        try {
            const initialResp = await checkTask(currentTask.instance, currentTask.userCode, attemptUuid);
            const initialResult = initialResp.items?.[0]?.result;
            let finalResult = initialResult;
            // If pending, poll for completion
            if (initialResult?.type === "pending") {
                setTasks((prev) => prev.map((t, idx) => (idx === selectedIndex ? { ...t, status: "pending" } : t)));
                finalResult = await pollCheckResult(currentTask.instance, attemptUuid, currentTask.userCode, () => {
                    // Progress heartbeat
                });
            }
            console.log(`[APP] Check completed. finalResult:`, finalResult);
            if (finalResult?.type === "success") {
                const feedback = finalResult.feedback;
                const rows = feedback?.rows || [];
                const maxScore = finalResult.scores?.max ?? 2;
                const score = finalResult.scores?.earned ?? 0;
                console.log(`[APP] Rendering report: score=${score}/${maxScore}, rows=`, rows);
                const report = {
                    score,
                    maxScore,
                    allPassed: score === maxScore && rows.every((r) => r.status === "OK"),
                    rows,
                    compilerLog: feedback?.compilerLog,
                    error: feedback?.error,
                    criteria: finalResult.criteria,
                };
                setTasks((prev) => prev.map((t, idx) => idx === selectedIndex
                    ? {
                        ...t,
                        status: "completed",
                        checkReport: report,
                    }
                    : t));
            }
            else if (finalResult?.type === "failed") {
                setTasks((prev) => prev.map((t, idx) => idx === selectedIndex
                    ? {
                        ...t,
                        status: "error",
                        errorMessage: JSON.stringify(finalResult.error) || "Ошибка проверки задания",
                    }
                    : t));
            }
        }
        catch (err) {
            console.error(err);
            const msg = err instanceof Error ? err.message : "Сетевой сбой при проверке";
            setTasks((prev) => prev.map((t, idx) => idx === selectedIndex
                ? {
                    ...t,
                    status: "error",
                    errorMessage: msg,
                }
                : t));
        }
    };
    return (_jsxs("div", { className: "app-container", children: [_jsxs("header", { className: "app-header", children: [_jsxs("div", { className: "brand-wrapper", children: [_jsx("div", { className: "brand-icon", children: "16" }), _jsxs("div", { className: "brand-title", children: [_jsx("span", { children: "\u041E\u0413\u042D \u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0442\u0438\u043A\u0430" }), _jsx("span", { className: "brand-tag", children: "\u0417\u0430\u0434\u0430\u043D\u0438\u0435 \u211616" })] })] }), _jsx("div", { className: "header-actions", children: _jsx("button", { className: "btn btn-ghost btn-sm", onClick: toggleTheme, title: theme === "dark" ? "Переключить на светлую тему" : "Переключить на тёмную тему", children: theme === "dark" ? _jsx(Sun, { size: 16 }) : _jsx(Moon, { size: 16 }) }) })] }), _jsx(GeneratorControls, { filter: filter, onChangeFilter: setFilter, onGenerate: handleGenerate, isLoading: isGenerating }), _jsxs("div", { className: "app-content", children: [_jsx(TaskList, { tasks: tasks, selectedIndex: selectedIndex, onSelectTask: (idx) => {
                            setSelectedIndex(idx);
                            setValidationError(undefined);
                        } }), _jsx("main", { className: "workspace-container", children: currentTask ? (_jsxs("div", { className: "workspace-panes", children: [_jsx(TaskStatement, { task: currentTask, onRecreate: handleRecreate, isRecreating: isRecreating }), _jsxs("div", { className: "editor-and-report-pane", children: [_jsx(CodeEditor, { code: currentTask.userCode, onChangeCode: handleCodeChange, onSubmit: handleSubmitCode, onResetToTemplate: handleResetToTemplate, isSubmitting: currentTask.status === "submitting" || currentTask.status === "pending", validationError: validationError }), _jsx(CheckReportView, { report: currentTask.checkReport, status: currentTask.status, errorMessage: currentTask.errorMessage })] })] })) : (_jsxs("div", { className: "empty-state", children: [_jsx(Layers, { size: 48, className: "empty-state-icon" }), _jsx("h3", { children: "\u0417\u0430\u0434\u0430\u043D\u0438\u044F \u043D\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043D\u044B" }), _jsx("p", { children: "\u041D\u0430\u0436\u043C\u0438\u0442\u0435 \u00AB\u0421\u0433\u0435\u043D\u0435\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u0437\u0430\u0434\u0430\u043D\u0438\u044F\u00BB \u0432 \u0432\u0435\u0440\u0445\u043D\u0435\u0439 \u043F\u0430\u043D\u0435\u043B\u0438 \u0443\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u0438\u044F." })] })) })] })] }));
};
export default App;
