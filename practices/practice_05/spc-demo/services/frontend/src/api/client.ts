import {
  GenerateRequestDto,
  RecreateRequestDto,
  TasksResponseDto,
  CheckRequestDto,
  CheckResponseDto,
  InstanceIdDto,
  CheckResult,
} from "./types";

const BASE_URL = "";

export function generateAttemptUuid(): string {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export async function generateTasks(
  prototypeIndices: number[],
  countPerProto: number = 1
): Promise<TasksResponseDto> {
  const req: GenerateRequestDto = {
    mode: "TRAINING",
    items: prototypeIndices.map((protoIdx) => ({
      prototype: {
        exam: "OGE",
        subject: "CS",
        number: 16,
        prototypeIdx: protoIdx,
      },
      count: countPerProto,
    })),
    settings: {},
  };

  const response = await fetch(`${BASE_URL}/api/v1/tasks/generate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Ошибка генерации заданий (${response.status}): ${errorText}`);
  }

  return response.json();
}

export async function recreateTask(instance: InstanceIdDto): Promise<TasksResponseDto> {
  const req: RecreateRequestDto = {
    mode: "TRAINING",
    items: [instance],
    settings: {},
  };

  const response = await fetch(`${BASE_URL}/api/v1/tasks/recreate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Ошибка пересоздания задания (${response.status}): ${errorText}`);
  }

  return response.json();
}

export async function checkTask(
  instance: InstanceIdDto,
  code: string,
  attemptUuid: string
): Promise<CheckResponseDto> {
  console.log(`[API] checkTask called -> prototypeIdx=${instance.prototypeIdx}, seed=${instance.seed}, attempt=${attemptUuid}, codeLength=${code.length}`);
  const req: CheckRequestDto = {
    items: [
      {
        attempt: {
          ...instance,
          attempt: attemptUuid,
        },
        answer: {
          type: "text-schema",
          text: code,
        },
      },
    ],
  };

  const response = await fetch(`${BASE_URL}/api/v1/tasks/check`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error(`[API] checkTask error (${response.status}):`, errorText);
    throw new Error(`Ошибка отправки на проверку (${response.status}): ${errorText}`);
  }

  const data = await response.json();
  console.log(`[API] checkTask response:`, data);
  return data;
}

export async function pollCheckResult(
  instance: InstanceIdDto,
  attemptUuid: string,
  userCode: string,
  onPendingUpdate?: () => void,
  maxAttempts: number = 30,
  intervalMs: number = 1000
): Promise<CheckResult> {
  console.log(`[POLL] Starting polling for attempt=${attemptUuid}, maxAttempts=${maxAttempts}`);
  for (let i = 0; i < maxAttempts; i++) {
    console.log(`[POLL] Attempt ${i + 1}/${maxAttempts}...`);
    const res = await checkTask(instance, userCode, attemptUuid);
    const item = res.items?.[0];
    if (!item) {
      console.error("[POLL] Empty response item");
      throw new Error("Не получен результат проверки от сервера");
    }

    console.log(`[POLL] Verdict type:`, item.result.type, item.result);

    if (item.result.type === "pending") {
      if (onPendingUpdate) onPendingUpdate();
      await new Promise((resolve) => setTimeout(resolve, intervalMs));
      continue;
    }

    console.log(`[POLL] Finished! Received non-pending verdict:`, item.result);
    return item.result;
  }

  console.error("[POLL] Timed out waiting for verdict");
  throw new Error("Превышено время ожидания проверки решения");
}
