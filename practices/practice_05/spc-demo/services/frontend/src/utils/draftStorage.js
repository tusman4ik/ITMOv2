export function getDraftKey(instance) {
    return `draft_16_${instance.prototypeIdx}_${instance.seed}_${instance.fingerprint}`;
}
export function loadDraft(instance, fallbackCode = "") {
    try {
        const key = getDraftKey(instance);
        const saved = localStorage.getItem(key);
        if (saved !== null) {
            return saved;
        }
    }
    catch (e) {
        console.warn("Could not read local draft storage", e);
    }
    return fallbackCode;
}
export function saveDraft(instance, code) {
    try {
        const key = getDraftKey(instance);
        localStorage.setItem(key, code);
    }
    catch (e) {
        console.warn("Could not save to local draft storage", e);
    }
}
export function removeDraft(instance) {
    try {
        const key = getDraftKey(instance);
        localStorage.removeItem(key);
    }
    catch (e) {
        console.warn("Could not delete local draft storage", e);
    }
}
