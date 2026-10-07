// Hook практики 4: после каждой правки Java-файла гоняет доверенный runner.
// OpenCode 1.x plugin API: событие "tool.execute.after".
// Успех — молча (лог в консоль сервера), провал — throw, чтобы агент увидел FAIL и чинил.

export const CheckAfterEdit = async ({ $, directory }) => {
  return {
    "tool.execute.after": async (input, output) => {
      const tool = input?.tool ?? output?.tool;
      if (tool !== "edit" && tool !== "write") return;

      const filePath =
        output?.args?.filePath ?? input?.args?.filePath ?? output?.filePath ?? "";
      if (!filePath.endsWith(".java") || !filePath.includes("/src/")) return;

      try {
        const result = await $`sh scripts/check.sh`.cwd(directory).nothrow().quiet();
        const tail = (result.text() || "").split("\n").slice(-6).join("\n");
        if (result.exitCode !== 0) {
          throw new Error(
            `check.sh FAILED after edit of ${filePath}:\n${tail}\nИсправь и добейся зелёного прогона.`
          );
        }
        console.log(`check.sh PASS after edit of ${filePath}`);
      } catch (e) {
        if (e instanceof Error && e.message.startsWith("check.sh FAILED")) throw e;
        throw new Error(`check.sh could not run after edit of ${filePath}: ${e}`);
      }
    },
  };
};
