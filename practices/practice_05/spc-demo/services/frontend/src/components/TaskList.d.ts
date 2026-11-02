import React from "react";
import { TaskItem } from "../api/types";
interface TaskListProps {
    tasks: TaskItem[];
    selectedIndex: number;
    onSelectTask: (index: number) => void;
}
export declare const TaskList: React.FC<TaskListProps>;
export {};
