import { TasksResponseDto, CheckResponseDto, InstanceIdDto, CheckResult } from "./types";
export declare function generateAttemptUuid(): string;
export declare function generateTasks(prototypeIndices: number[], countPerProto?: number): Promise<TasksResponseDto>;
export declare function recreateTask(instance: InstanceIdDto): Promise<TasksResponseDto>;
export declare function checkTask(instance: InstanceIdDto, code: string, attemptUuid: string): Promise<CheckResponseDto>;
export declare function pollCheckResult(instance: InstanceIdDto, attemptUuid: string, userCode: string, onPendingUpdate?: () => void, maxAttempts?: number, intervalMs?: number): Promise<CheckResult>;
