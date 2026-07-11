import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

const styles = readFileSync(resolve(process.cwd(), "src/styles.css"), "utf8");

describe("authenticated Stripe-inspired theme", () => {
  it("defines the desktop top-navigation surface and floating dropdown", () => {
    expect(styles).toContain("/* Stripe-inspired authenticated shell */");
    expect(styles).toMatch(/\.desktop-nav-shell\s*\{/);
    expect(styles).toMatch(/\.desktop-nav-dropdown\s*\{[\s\S]*?position:\s*absolute/);
  });

  it("preserves the mobile drawer below the existing breakpoint", () => {
    const theme = styles.slice(styles.indexOf("/* Stripe-inspired authenticated shell */"));
    expect(theme).toMatch(/@media \(max-width:\s*860px\)[\s\S]*?\.desktop-nav-shell\s*\{\s*display:\s*none/);
    expect(theme).toMatch(/@media \(max-width:\s*860px\)[\s\S]*?\.sidebar\s*\{[\s\S]*?display:\s*flex/);
  });
});
