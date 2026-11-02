import React from "react";
import { CheckCircle2, Clock, AlertCircle, CircleDashed, FileCode } from "lucide-react";
import { TaskItem } from "../api/types";

interface TaskListProps {
  tasks: TaskItem[];
  selectedIndex: number;
  onSelectTask: (index: number) => void;
}

export const TaskList: React.FC<TaskListProps> = ({
  tasks,
  selectedIndex,
  onSelectTask,
}) => {
  const getStatusBadge = (status: TaskItem["status"], score?: number) => {
    switch (status) {
      case "completed":
        return (
          <span className="status-badge completed">
            <CheckCircle2 size={12} />
            {score !== undefined ? `${score}/2` : "Сдано"}
          </span>
        );
      case "pending":
      case "submitting":
        return (
          <span className="status-badge pending">
            <Clock size={12} className="spin" />
            Проверка
          </span>
        );
      case "error":
        return (
          <span className="status-badge error">
            <AlertCircle size={12} />
            Ошибка
          </span>
        );
      case "idle":
      default:
        return (
          <span className="status-badge idle">
            <CircleDashed size={12} />
            Не начато
          </span>
        );
    }
  };

  return (
    <aside className="task-sidebar">
      <div className="sidebar-header">
        <span className="sidebar-title">
          <FileCode size={16} />
          Варианты в пуле ({tasks.length})
        </span>
      </div>

      <div className="task-list">
        {tasks.map((task, idx) => {
          const isSelected = idx === selectedIndex;
          const protoFormatted =
            task.instance.prototypeIdx === 999
              ? "16-999"
              : `16-${String(task.instance.prototypeIdx).padStart(3, "0")}`;

          return (
            <div
              key={`${task.instance.prototypeIdx}_${task.instance.seed}_${idx}`}
              className={`task-card-item ${isSelected ? "active" : ""}`}
              onClick={() => onSelectTask(idx)}
            >
              <div className="task-card-header">
                <span className="proto-badge">{protoFormatted}</span>
                {getStatusBadge(task.status, task.checkReport?.score)}
              </div>
              <div className="task-card-meta">
                <span>№ {idx + 1}</span>
                <span title={`Seed: ${task.instance.seed}`}>
                  seed: {task.instance.seed}
                </span>
              </div>
            </div>
          );
        })}

        {tasks.length === 0 && (
          <div className="empty-state" style={{ padding: "1.5rem" }}>
            <p style={{ fontSize: "0.85rem" }}>Нет сгенерированных заданий. Нажмите «Сгенерировать задания» сверху.</p>
          </div>
        )}
      </div>
    </aside>
  );
};
