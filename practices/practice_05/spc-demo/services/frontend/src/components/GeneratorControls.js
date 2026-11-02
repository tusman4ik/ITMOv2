import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import { Play, Sparkles } from "lucide-react";
const AVAILABLE_PROTOTYPES = [
    { idx: 1, label: "16-001 (Сумма, оканчивающиеся на цифру D в СС B)" },
    { idx: 2, label: "16-002 (Сумма, предпоследняя цифра D в СС B)" },
    { idx: 3, label: "16-003 (Количество цифр, кратных K)" },
    { idx: 4, label: "16-004 (Сумма цифр, не кратных K)" },
    { idx: 5, label: "16-005 (Минимальная цифра числа)" },
    { idx: 999, label: "16-999 (Максимальное число, кратное K)" },
];
export const GeneratorControls = ({ filter, onChangeFilter, onGenerate, isLoading, }) => {
    const togglePrototype = (idx) => {
        const isSelected = filter.selectedPrototypes.includes(idx);
        let updated;
        if (isSelected) {
            if (filter.selectedPrototypes.length === 1)
                return; // Keep at least one
            updated = filter.selectedPrototypes.filter((p) => p !== idx);
        }
        else {
            updated = [...filter.selectedPrototypes, idx].sort((a, b) => a - b);
        }
        onChangeFilter({ ...filter, selectedPrototypes: updated });
    };
    const selectAll = () => {
        onChangeFilter({
            ...filter,
            selectedPrototypes: AVAILABLE_PROTOTYPES.map((p) => p.idx),
        });
    };
    const isAllSelected = filter.selectedPrototypes.length === AVAILABLE_PROTOTYPES.length;
    return (_jsxs("div", { className: "generator-bar", children: [_jsxs("div", { className: "generator-controls-left", children: [_jsxs("div", { className: "filter-group", children: [_jsx("span", { className: "filter-label", children: "\u041F\u0440\u043E\u0442\u043E\u0442\u0438\u043F\u044B \u211616:" }), _jsxs("div", { className: "proto-chips", children: [_jsx("button", { type: "button", className: `proto-chip ${isAllSelected ? "active" : ""}`, onClick: selectAll, title: "\u0412\u044B\u0431\u0440\u0430\u0442\u044C \u0432\u0441\u0435 \u043F\u0440\u043E\u0442\u043E\u0442\u0438\u043F\u044B", children: "\u0412\u0441\u0435 (1-5, 999)" }), AVAILABLE_PROTOTYPES.map((proto) => {
                                        const active = filter.selectedPrototypes.includes(proto.idx);
                                        return (_jsx("button", { type: "button", className: `proto-chip ${active ? "active" : ""}`, onClick: () => togglePrototype(proto.idx), title: proto.label, children: proto.idx === 999 ? "16-999" : `16-${String(proto.idx).padStart(3, "0")}` }, proto.idx));
                                    })] })] }), _jsxs("div", { className: "filter-group", children: [_jsx("span", { className: "filter-label", children: "\u041A\u043E\u043B-\u0432\u043E \u0432\u0430\u0440\u0438\u0430\u043D\u0442\u043E\u0432:" }), _jsxs("select", { className: "count-select", value: filter.count, onChange: (e) => onChangeFilter({ ...filter, count: Number(e.target.value) }), disabled: isLoading, children: [_jsx("option", { value: 1, children: "\u041F\u043E 1 \u044D\u043A\u0437\u0435\u043C\u043F\u043B\u044F\u0440\u0443" }), _jsx("option", { value: 2, children: "\u041F\u043E 2 \u044D\u043A\u0437\u0435\u043C\u043F\u043B\u044F\u0440\u0430" }), _jsx("option", { value: 3, children: "\u041F\u043E 3 \u044D\u043A\u0437\u0435\u043C\u043F\u043B\u044F\u0440\u0430" })] })] })] }), _jsx("div", { className: "generator-controls-right", children: _jsx("button", { className: "btn btn-primary", onClick: onGenerate, disabled: isLoading, children: isLoading ? (_jsxs(_Fragment, { children: [_jsx(Sparkles, { size: 16, className: "spin" }), _jsx("span", { children: "\u0413\u0435\u043D\u0435\u0440\u0430\u0446\u0438\u044F..." })] })) : (_jsxs(_Fragment, { children: [_jsx(Play, { size: 16, fill: "currentColor" }), _jsx("span", { children: "\u0421\u0433\u0435\u043D\u0435\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u0437\u0430\u0434\u0430\u043D\u0438\u044F" })] })) }) })] }));
};
