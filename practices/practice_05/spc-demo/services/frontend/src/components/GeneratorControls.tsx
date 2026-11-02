import React from "react";
import { Play, Sparkles } from "lucide-react";
import { GeneratorFilter } from "../api/types";

interface GeneratorControlsProps {
  filter: GeneratorFilter;
  onChangeFilter: (newFilter: GeneratorFilter) => void;
  onGenerate: () => void;
  isLoading: boolean;
}

const AVAILABLE_PROTOTYPES = [
  { idx: 1, label: "16-001 (Сумма, оканчивающиеся на цифру D в СС B)" },
  { idx: 2, label: "16-002 (Сумма, предпоследняя цифра D в СС B)" },
  { idx: 3, label: "16-003 (Количество цифр, кратных K)" },
  { idx: 4, label: "16-004 (Сумма цифр, не кратных K)" },
  { idx: 5, label: "16-005 (Минимальная цифра числа)" },
  { idx: 999, label: "16-999 (Максимальное число, кратное K)" },
];

export const GeneratorControls: React.FC<GeneratorControlsProps> = ({
  filter,
  onChangeFilter,
  onGenerate,
  isLoading,
}) => {
  const togglePrototype = (idx: number) => {
    const isSelected = filter.selectedPrototypes.includes(idx);
    let updated: number[];
    if (isSelected) {
      if (filter.selectedPrototypes.length === 1) return; // Keep at least one
      updated = filter.selectedPrototypes.filter((p) => p !== idx);
    } else {
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

  return (
    <div className="generator-bar">
      <div className="generator-controls-left">
        <div className="filter-group">
          <span className="filter-label">Прототипы №16:</span>
          <div className="proto-chips">
            <button
              type="button"
              className={`proto-chip ${isAllSelected ? "active" : ""}`}
              onClick={selectAll}
              title="Выбрать все прототипы"
            >
              Все (1-5, 999)
            </button>
            {AVAILABLE_PROTOTYPES.map((proto) => {
              const active = filter.selectedPrototypes.includes(proto.idx);
              return (
                <button
                  key={proto.idx}
                  type="button"
                  className={`proto-chip ${active ? "active" : ""}`}
                  onClick={() => togglePrototype(proto.idx)}
                  title={proto.label}
                >
                  {proto.idx === 999 ? "16-999" : `16-${String(proto.idx).padStart(3, "0")}`}
                </button>
              );
            })}
          </div>
        </div>

        <div className="filter-group">
          <span className="filter-label">Кол-во вариантов:</span>
          <select
            className="count-select"
            value={filter.count}
            onChange={(e) => onChangeFilter({ ...filter, count: Number(e.target.value) })}
            disabled={isLoading}
          >
            <option value={1}>По 1 экземпляру</option>
            <option value={2}>По 2 экземпляра</option>
            <option value={3}>По 3 экземпляра</option>
          </select>
        </div>
      </div>

      <div className="generator-controls-right">
        <button
          className="btn btn-primary"
          onClick={onGenerate}
          disabled={isLoading}
        >
          {isLoading ? (
            <>
              <Sparkles size={16} className="spin" />
              <span>Генерация...</span>
            </>
          ) : (
            <>
              <Play size={16} fill="currentColor" />
              <span>Сгенерировать задания</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};
