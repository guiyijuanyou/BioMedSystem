import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

const view = readFileSync(resolve(process.cwd(), "src/views/QualityMetricView.vue"), "utf8");
const styles = readFileSync(resolve(process.cwd(), "src/styles.css"), "utf8");

describe("quality metric workbench presentation", () => {
  it("uses dedicated rounded sections and a structured empty state", () => {
    expect(view).toContain('class="metric-section"');
    expect(view).toContain('class="metric-empty-state"');
    expect(view).toContain('class="metric-head metric-section"');
    expect(styles).toMatch(/\.metric-section\s*\{[^}]*border-radius:\s*18px/s);
  });

  it("keeps metric toolbar actions on one line", () => {
    expect(view).toContain('@click="saveConfig"');
    expect(view).toMatch(/display:\s*flex;\s*align-items:\s*center/);
    expect(styles).toMatch(/\.metric-controls\s+button\s*\{[^}]*white-space:\s*nowrap/s);
  });
});
