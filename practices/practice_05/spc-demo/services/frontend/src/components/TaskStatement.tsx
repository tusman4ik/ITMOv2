import React from "react";
import { RotateCcw, BookOpen, Hash, Cpu } from "lucide-react";
import { TaskItem } from "../api/types";

interface TaskStatementProps {
  task: TaskItem;
  onRecreate: () => void;
  isRecreating: boolean;
}

export const TaskStatement: React.FC<TaskStatementProps> = ({
  task,
  onRecreate,
  isRecreating,
}) => {
  const protoDisplay =
    task.instance.prototypeIdx === 999
      ? "16-999"
      : `16-${String(task.instance.prototypeIdx).padStart(3, "0")}`;

  return (
    <div className="statement-pane">
      <div className="statement-header">
        <div className="statement-title-wrap">
          <h2>
            <BookOpen size={20} color="var(--primary)" />
            Задание №16 ({protoDisplay})
          </h2>
          <div className="statement-meta-tags">
            <span className="meta-chip">
              <Hash size={12} style={{ display: "inline", marginRight: 3 }} />
              Экзамен: {task.instance.exam} {task.instance.subject}
            </span>
            <span className="meta-chip" title="Случайное зерно генерации">
              Seed: {task.instance.seed}
            </span>
            <span className="meta-chip" title="Fingerprint прототипа">
              <Cpu size={12} style={{ display: "inline", marginRight: 3 }} />
              FP: {task.instance.fingerprint}
            </span>
          </div>
        </div>

        <button
          className="btn btn-secondary btn-sm"
          onClick={onRecreate}
          disabled={isRecreating}
          title="Сгенерировать заново этот вариант с фиксированным seed"
        >
          <RotateCcw size={14} className={isRecreating ? "spin" : ""} />
          <span>Сбросить / Recreate</span>
        </button>
      </div>

      <div
        className="statement-content"
        dangerouslySetInnerHTML={{ __html: task.statementHtml }}
      />
    </div>
  );
};
