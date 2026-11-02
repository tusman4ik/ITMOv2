import { InstanceIdDto } from "../api/types";
export declare function getDraftKey(instance: InstanceIdDto): string;
export declare function loadDraft(instance: InstanceIdDto, fallbackCode?: string): string;
export declare function saveDraft(instance: InstanceIdDto, code: string): void;
export declare function removeDraft(instance: InstanceIdDto): void;
