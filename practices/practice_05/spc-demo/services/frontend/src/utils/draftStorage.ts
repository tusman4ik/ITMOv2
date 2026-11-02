import { InstanceIdDto } from "../api/types";

export function getDraftKey(instance: InstanceIdDto): string {
  return `draft_16_${instance.prototypeIdx}_${instance.seed}_${instance.fingerprint}`;
}

export function loadDraft(instance: InstanceIdDto, fallbackCode: string = ""): string {
  try {
    const key = getDraftKey(instance);
    const saved = localStorage.getItem(key);
    if (saved !== null) {
      return saved;
    }
  } catch (e) {
    console.warn("Could not read local draft storage", e);
  }
  return fallbackCode;
}

export function saveDraft(instance: InstanceIdDto, code: string): void {
  try {
    const key = getDraftKey(instance);
    localStorage.setItem(key, code);
  } catch (e) {
    console.warn("Could not save to local draft storage", e);
  }
}

export function removeDraft(instance: InstanceIdDto): void {
  try {
    const key = getDraftKey(instance);
    localStorage.removeItem(key);
  } catch (e) {
    console.warn("Could not delete local draft storage", e);
  }
}
