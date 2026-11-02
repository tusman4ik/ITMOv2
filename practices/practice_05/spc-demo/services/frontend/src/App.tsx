import React, { useState, useEffect, useCallback } from "react";
import { Moon, Sun, Layers } from "lucide-react";
import {
  TaskItem,
  GeneratorFilter,
  VerificationReport,
  Row,
  CodeSubmissionFeedback,
} from "./api/types";
import {
  generateTasks,
  recreateTask,
  checkTask,
  pollCheckResult,
  generateAttemptUuid,
} from "./api/client";
import { loadDraft, saveDraft } from "./utils/draftStorage";
import { GeneratorControls } from "./components/GeneratorControls";
import { TaskList } from "./components/TaskList";
import { TaskStatement } from "./components/TaskStatement";
import { CodeEditor } from "./components/CodeEditor";
import { CheckReportView } from "./components/CheckReportView";

function getDefaultTemplateForPrototype(prototypeIdx: number): string {
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

export const App: React.FC = () => {
  const [theme, setTheme] = useState<"dark" | "light">("dark");
  const [tasks, setTasks] = useState<TaskItem[]>([]);
  const [selectedIndex, setSelectedIndex] = useState<number>(0);
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [isRecreating, setIsRecreating] = useState<boolean>(false);
  const [validationError, setValidationError] = useState<string | undefined>();

  const [filter, setFilter] = useState<GeneratorFilter>({
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
      const items: TaskItem[] = resp.tasks.map((taskDto) => {
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
    } catch (err: unknown) {
      console.error(err);
      alert(err instanceof Error ? err.message : "Не удалось сгенерировать задания");
    } finally {
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
    if (!currentTask) return;
    setIsRecreating(true);
    try {
      const resp = await recreateTask(currentTask.instance);
      if (resp.tasks.length > 0) {
        const updatedDto = resp.tasks[0];
        const protoTemplate = getDefaultTemplateForPrototype(updatedDto.instance.prototypeIdx);
        const defaultCode = updatedDto.inputForm?.schema?.text || protoTemplate;
        const savedCode = loadDraft(updatedDto.instance, defaultCode);

        setTasks((prev) =>
          prev.map((t, idx) =>
            idx === selectedIndex
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
              : t
          )
        );
      }
    } catch (err: unknown) {
      console.error(err);
      alert(err instanceof Error ? err.message : "Не удалось пересоздать задание");
    } finally {
      setIsRecreating(false);
    }
  };

  // Code change in editor
  const handleCodeChange = (newCode: string) => {
    if (!currentTask) return;
    saveDraft(currentTask.instance, newCode);
    setTasks((prev) =>
      prev.map((t, idx) => (idx === selectedIndex ? { ...t, userCode: newCode } : t))
    );
    if (validationError) {
      setValidationError(undefined);
    }
  };

  // Reset code to default template
  const handleResetToTemplate = () => {
    if (!currentTask) return;
    if (window.confirm("Сбросить введённый код к исходному шаблону?")) {
      handleCodeChange(currentTask.defaultCode);
    }
  };

  // Submit code for grading
  const handleSubmitCode = async () => {
    if (!currentTask) return;
    if (!currentTask.userCode.trim()) {
      setValidationError("Введите программный код решения перед отправкой на проверку.");
      return;
    }

    const attemptUuid = generateAttemptUuid();

    // Set status to submitting
    setTasks((prev) =>
      prev.map((t, idx) =>
        idx === selectedIndex
          ? {
              ...t,
              status: "submitting",
              lastAttemptId: attemptUuid,
              errorMessage: undefined,
            }
          : t
      )
    );

    try {
      const initialResp = await checkTask(currentTask.instance, currentTask.userCode, attemptUuid);
      const initialResult = initialResp.items?.[0]?.result;

      let finalResult = initialResult;

      // If pending, poll for completion
      if (initialResult?.type === "pending") {
        setTasks((prev) =>
          prev.map((t, idx) => (idx === selectedIndex ? { ...t, status: "pending" } : t))
        );

        finalResult = await pollCheckResult(currentTask.instance, attemptUuid, currentTask.userCode, () => {
          // Progress heartbeat
        });
      }

      console.log(`[APP] Check completed. finalResult:`, finalResult);

      if (finalResult?.type === "success") {
        const feedback = finalResult.feedback as CodeSubmissionFeedback | undefined;
        const rows: Row[] = feedback?.rows || [];
        const maxScore = finalResult.scores?.max ?? 2;
        const score = finalResult.scores?.earned ?? 0;

        console.log(`[APP] Rendering report: score=${score}/${maxScore}, rows=`, rows);

        const report: VerificationReport = {
          score,
          maxScore,
          allPassed: score === maxScore && rows.every((r) => r.status === "OK"),
          rows,
          compilerLog: feedback?.compilerLog,
          error: feedback?.error,
          criteria: finalResult.criteria,
        };

        setTasks((prev) =>
          prev.map((t, idx) =>
            idx === selectedIndex
              ? {
                  ...t,
                  status: "completed",
                  checkReport: report,
                }
              : t
          )
        );
      } else if (finalResult?.type === "failed") {
        setTasks((prev) =>
          prev.map((t, idx) =>
            idx === selectedIndex
              ? {
                  ...t,
                  status: "error",
                  errorMessage: JSON.stringify(finalResult.error) || "Ошибка проверки задания",
                }
              : t
          )
        );
      }
    } catch (err: unknown) {
      console.error(err);
      const msg = err instanceof Error ? err.message : "Сетевой сбой при проверке";
      setTasks((prev) =>
        prev.map((t, idx) =>
          idx === selectedIndex
            ? {
                ...t,
                status: "error",
                errorMessage: msg,
              }
            : t
        )
      );
    }
  };

  return (
    <div className="app-container">
      {/* Top Header */}
      <header className="app-header">
        <div className="brand-wrapper">
          <div className="brand-icon">16</div>
          <div className="brand-title">
            <span>ОГЭ Информатика</span>
            <span className="brand-tag">Задание №16</span>
          </div>
        </div>

        <div className="header-actions">
          <button
            className="btn btn-ghost btn-sm"
            onClick={toggleTheme}
            title={theme === "dark" ? "Переключить на светлую тему" : "Переключить на тёмную тему"}
          >
            {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
          </button>
        </div>
      </header>

      {/* Generator Control Bar */}
      <GeneratorControls
        filter={filter}
        onChangeFilter={setFilter}
        onGenerate={handleGenerate}
        isLoading={isGenerating}
      />

      {/* Main Workspace Layout */}
      <div className="app-content">
        <TaskList
          tasks={tasks}
          selectedIndex={selectedIndex}
          onSelectTask={(idx) => {
            setSelectedIndex(idx);
            setValidationError(undefined);
          }}
        />

        <main className="workspace-container">
          {currentTask ? (
            <div className="workspace-panes">
              {/* Left Column: Task Statement */}
              <TaskStatement
                task={currentTask}
                onRecreate={handleRecreate}
                isRecreating={isRecreating}
              />

              {/* Right Column: Code Editor & Verification Report */}
              <div className="editor-and-report-pane">
                <CodeEditor
                  code={currentTask.userCode}
                  onChangeCode={handleCodeChange}
                  onSubmit={handleSubmitCode}
                  onResetToTemplate={handleResetToTemplate}
                  isSubmitting={currentTask.status === "submitting" || currentTask.status === "pending"}
                  validationError={validationError}
                />

                <CheckReportView
                  report={currentTask.checkReport}
                  status={currentTask.status}
                  errorMessage={currentTask.errorMessage}
                />
              </div>
            </div>
          ) : (
            <div className="empty-state">
              <Layers size={48} className="empty-state-icon" />
              <h3>Задания не загружены</h3>
              <p>Нажмите «Сгенерировать задания» в верхней панели управления.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};
export default App;
