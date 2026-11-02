import React from "react";
import { TaskItem } from "../api/types";
interface TaskStatementProps {
    task: TaskItem;
    onRecreate: () => void;
    isRecreating: boolean;
}
export declare const TaskStatement: React.FC<TaskStatementProps>;
export {};
