import { describe, expect, it } from "vitest";
import { appDialog } from "@/services/dialog";

describe("appDialog prompt", () => {
  it("accepts values emitted as numbers by an input[type=number]", async () => {
    const result = appDialog.prompt({ inputType: "number" });
    appDialog.state.value = 80;

    expect(() => appDialog.accept()).not.toThrow();
    await expect(result).resolves.toBe("80");
  });
});
