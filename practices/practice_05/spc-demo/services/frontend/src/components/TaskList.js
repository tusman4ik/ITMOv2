import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { CheckCircle2, Clock, AlertCircle, CircleDashed, FileCode } from "lucide-react";
export const TaskList = ({ tasks, selectedIndex, onSelectTask, }) => {
    const getStatusBadge = (status, score) => {
        switch (status) {
            case "completed":
                return (_jsxs("span", { className: "status-badge completed", children: [_jsx(CheckCircle2, { size: 12 }), score !== undefined ? `${score}/2` : "Сдано"] }));
            case "pending":
            case "submitting":
                return (_jsxs("span", { className: "status-badge pending", children: [_jsx(Clock, { size: 12, className: "spin" }), "\u041F\u0440\u043E\u0432\u0435\u0440\u043A\u0430"] }));
            case "error":
                return (_jsxs("span", { className: "status-badge error", children: [_jsx(AlertCircle, { size: 12 }), "\u041E\u0448\u0438\u0431\u043A\u0430"] }));
            case "idle":
            default:
                return (_jsxs("span", { className: "status-badge idle", children: [_jsx(CircleDashed, { size: 12 }), "\u041D\u0435 \u043D\u0430\u0447\u0430\u0442\u043E"] }));
        }
    };
    return (_jsxs("aside", { className: "task-sidebar", children: [_jsx("div", { className: "sidebar-header", children: _jsxs("span", { className: "sidebar-title", children: [_jsx(FileCode, { size: 16 }), "\u0412\u0430\u0440\u0438\u0430\u043D\u0442\u044B \u0432 \u043F\u0443\u043B\u0435 (", tasks.length, ")"] }) }), _jsxs("div", { className: "task-list", children: [tasks.map((task, idx) => {
                        const isSelected = idx === selectedIndex;
                        const protoFormatted = task.instance.prototypeIdx === 999
                            ? "16-999"
                            : `16-${String(task.instance.prototypeIdx).padStart(3, "0")}`;
                        return (_jsxs("div", { className: `task-card-item ${isSelected ? "active" : ""}`, onClick: () => onSelectTask(idx), children: [_jsxs("div", { className: "task-card-header", children: [_jsx("span", { className: "proto-badge", children: protoFormatted }), getStatusBadge(task.status, task.checkReport?.score)] }), _jsxs("div", { className: "task-card-meta", children: [_jsxs("span", { children: ["\u2116 ", idx + 1] }), _jsxs("span", { title: `Seed: ${task.instance.seed}`, children: ["seed: ", task.instance.seed] })] })] }, `${task.instance.prototypeIdx}_${task.instance.seed}_${idx}`));
                    }), tasks.length === 0 && (_jsx("div", { className: "empty-state", style: { padding: "1.5rem" }, children: _jsx("p", { style: { fontSize: "0.85rem" }, children: "\u041D\u0435\u0442 \u0441\u0433\u0435\u043D\u0435\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u044B\u0445 \u0437\u0430\u0434\u0430\u043D\u0438\u0439. \u041D\u0430\u0436\u043C\u0438\u0442\u0435 \u00AB\u0421\u0433\u0435\u043D\u0435\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u0437\u0430\u0434\u0430\u043D\u0438\u044F\u00BB \u0441\u0432\u0435\u0440\u0445\u0443." }) }))] })] }));
};
