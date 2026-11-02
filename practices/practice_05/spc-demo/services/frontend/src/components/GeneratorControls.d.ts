import React from "react";
import { GeneratorFilter } from "../api/types";
interface GeneratorControlsProps {
    filter: GeneratorFilter;
    onChangeFilter: (newFilter: GeneratorFilter) => void;
    onGenerate: () => void;
    isLoading: boolean;
}
export declare const GeneratorControls: React.FC<GeneratorControlsProps>;
export {};
