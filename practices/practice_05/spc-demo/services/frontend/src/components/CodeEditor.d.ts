import React from "react";
interface CodeEditorProps {
    code: string;
    onChangeCode: (code: string) => void;
    onSubmit: () => void;
    onResetToTemplate: () => void;
    isSubmitting: boolean;
    validationError?: string;
}
export declare const CodeEditor: React.FC<CodeEditorProps>;
export {};
