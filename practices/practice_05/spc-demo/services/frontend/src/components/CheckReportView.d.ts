import React from "react";
import { VerificationReport } from "../api/types";
interface CheckReportViewProps {
    report?: VerificationReport;
    status: "idle" | "submitting" | "pending" | "completed" | "error";
    errorMessage?: string;
}
export declare const CheckReportView: React.FC<CheckReportViewProps>;
export {};
