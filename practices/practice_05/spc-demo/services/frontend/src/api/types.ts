export type ExamType = "OGE" | "EGE";
export type SubjectType = "CS";

export interface InstanceIdDto {
  exam: ExamType;
  subject: SubjectType;
  number: number;
  prototypeIdx: number;
  seed: number;
  fingerprint: number;
}

export interface PrototypeIdDto {
  exam: ExamType;
  subject: SubjectType;
  number: number;
  prototypeIdx: number;
}

export interface CreatePrototypeOrderDto {
  prototype: PrototypeIdDto;
  count: number;
}

export interface GenerateRequestDto {
  mode: "EXAM" | "TRAINING";
  items: CreatePrototypeOrderDto[];
  settings: Record<string, unknown>;
}

export interface RecreateRequestDto {
  mode: "EXAM" | "TRAINING";
  items: InstanceIdDto[];
  settings: Record<string, unknown>;
}

export interface TextInputSchema {
  type: "text-schema";
  text?: string;
}

export interface IntegerSchema {
  type: "integer-schema";
  integer?: number;
}

export type InputSchema = TextInputSchema | IntegerSchema;

export interface CodeInputForm {
  type: "CodeInputForm";
  language?: "PYTHON";
  schema?: TextInputSchema;
}

export interface TaskDto {
  instance: InstanceIdDto;
  statement: string;
  parameters: Array<{
    type: "attachment";
    meta?: {
      type?: "TEXT" | "XLSX" | "PPTX";
      approximateSizeBytes?: number;
      name?: string;
    };
    token?: {
      id?: InstanceIdDto;
      nodeName?: string;
    };
  }>;
  inputForm: CodeInputForm;
}

export interface TasksResponseDto {
  tasks: TaskDto[];
}

export interface AttemptIdDto extends InstanceIdDto {
  attempt: string;
}

export interface CheckItemDto {
  attempt: AttemptIdDto;
  answer: TextInputSchema | IntegerSchema;
}

export interface CheckRequestDto {
  items: CheckItemDto[];
}

export type RowStatus = "OK" | "TL" | "ML" | "CE" | "SE" | "RE" | "WA" | "OL" | "IE";

export interface Row {
  caseId?: string;
  groupId?: string;
  status?: RowStatus;
  input?: string;
  stdout?: string;
  expected?: string;
  checkerMsg?: string;
  cpuTimeSec?: number;
  realTimeSec?: number;
  memoryKb?: number;
  exitCode?: number;
}

export interface CodeSubmissionFeedback {
  type: "code-submission-feedback";
  rows?: Row[];
  passed?: number;
  total?: number;
  compilerLog?: string;
  truncated?: boolean;
  error?: string;
}

export interface FictitiousFeedback {
  type: "fictitious-feedback";
  userAnswer?: string;
  correctAnswer?: string;
}

export type Feedback = CodeSubmissionFeedback | FictitiousFeedback;

export interface ScoresDto {
  max?: number;
  earned?: number;
}

export interface Criterion {
  name?: string;
  maxScore?: number;
  userScore?: number;
  description?: string;
}

export interface ReferenceDto {
  answer?: TextInputSchema | IntegerSchema;
  kind?: "EXACT" | "SAMPLE" | "NONE";
}

export interface SuccessCheck {
  type: "success";
  scores?: ScoresDto;
  criteria?: Criterion[];
  feedback?: Feedback;
  answer?: TextInputSchema | IntegerSchema;
  reference?: ReferenceDto;
}

export interface FailedCheck {
  type: "failed";
  error?: Record<string, unknown>;
}

export interface PendingCheck {
  type: "pending";
  deliveryMode?: "BATCHED_MS_100" | "BATCHED_MS_500" | "BATCHED_MS_1000" | "BATCHED_MS_2000";
}

export type CheckResult = SuccessCheck | FailedCheck | PendingCheck;

export interface CheckedItemDto {
  attempt: AttemptIdDto;
  result: CheckResult;
}

export interface CheckResponseDto {
  items: CheckedItemDto[];
}

// Frontend Model
export interface VerificationReport {
  score: number;
  maxScore: number;
  allPassed: boolean;
  rows: Row[];
  compilerLog?: string;
  error?: string;
  criteria?: Criterion[];
}

export interface TaskItem {
  instance: InstanceIdDto;
  statementHtml: string;
  defaultCode: string;
  userCode: string;
  status: "idle" | "submitting" | "pending" | "completed" | "error";
  checkReport?: VerificationReport;
  lastAttemptId?: string;
  errorMessage?: string;
}

export interface GeneratorFilter {
  count: number;
  selectedPrototypes: number[]; // 1, 2, 3, 4, 5, 999
}
