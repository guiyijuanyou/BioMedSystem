import { existsSync, readFileSync } from "node:fs";
import { resolve } from "node:path";
import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import AuthenticatedLeafBackground from "@/components/background/AuthenticatedLeafBackground.vue";

const appSource = readFileSync(resolve(process.cwd(), "src/App.vue"), "utf8");
const backgroundSource = readFileSync(
  resolve(process.cwd(), "src/components/background/AuthenticatedLeafBackground.vue"),
  "utf8",
);
const loginSource = readFileSync(resolve(process.cwd(), "src/views/LoginView.vue"), "utf8");
const loginSceneSource = readFileSync(
  resolve(process.cwd(), "src/components/login/BiomedScene.vue"),
  "utf8",
);
const sharedSceneStylesPath = resolve(process.cwd(), "src/styles/biomed-scene.css");
const sharedSceneStyles = existsSync(sharedSceneStylesPath)
  ? readFileSync(sharedSceneStylesPath, "utf8")
  : "";
const styles = readFileSync(resolve(process.cwd(), "src/styles.css"), "utf8");
const packageJson = JSON.parse(readFileSync(resolve(process.cwd(), "package.json"), "utf8"));

function mediaQuery(matches) {
  return {
    matches,
    media: "",
    onchange: null,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    addListener: vi.fn(),
    removeListener: vi.fn(),
  };
}

beforeEach(() => {
  vi.stubGlobal("matchMedia", vi.fn(query => mediaQuery(query.includes("prefers-reduced-motion"))));
});

afterEach(() => {
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

describe("authenticated leaf background components", () => {
  it("reuses the homepage scene at its first-screen state", () => {
    const wrapper = mount(AuthenticatedLeafBackground, {
      global: {
        stubs: {
          BiomedScene: { props: ["progress"], template: '<div data-test="biomed-scene-stub" />' },
        },
      },
    });

    expect(wrapper.attributes("aria-hidden")).toBe("true");
    expect(wrapper.classes()).toContain("authenticated-leaf-background");
    expect(wrapper.find('[data-test="biomed-scene-stub"]').exists()).toBe(true);
    expect(backgroundSource).toContain('import BiomedScene from "@/components/login/BiomedScene.vue"');
    expect(backgroundSource).toContain('<BiomedScene :progress="0" />');
    expect(backgroundSource).not.toContain("MedicinalLeafScene");
    expect(backgroundSource).not.toContain("useAuthenticatedBackground");
    expect(loginSceneSource).toContain("@/styles/biomed-scene.css");
    expect(sharedSceneStyles).toContain(".biomed-scene::after");

    wrapper.unmount();
  });

  it("disables interaction only for the authenticated wrapper", () => {
    expect(styles).toMatch(/\.authenticated-leaf-background\s+\.biomed-scene\s*\{[\s\S]*?pointer-events:\s*none/);
    expect(styles).not.toContain("authenticated-leaf-background__static-leaf");
    expect(loginSceneSource).toContain('@pointermove="movePointer"');
  });

  it("removes the retired leaf implementation", () => {
    for (const file of [
      "src/composables/useAuthenticatedBackground.js",
      "src/components/background/medicinalLeafGeometry.js",
      "src/components/background/MedicinalLeafModel.vue",
      "src/components/background/MedicinalLeafScene.vue",
      "src/components/__tests__/medicinalLeafGeometry.test.js",
    ]) {
      expect(existsSync(resolve(process.cwd(), file))).toBe(false);
    }
  });
});

describe("authenticated-only shell integration", () => {
  it("mounts one async background inside the authenticated shell", () => {
    expect(appSource).toContain("defineAsyncComponent");
    expect(appSource).toContain("@/components/background/AuthenticatedLeafBackground.vue");
    expect(appSource).toMatch(/<div class="shell"[\s\S]*?<AuthenticatedLeafBackground/);
  });

  it("does not modify or replace the immersive login scene", () => {
    expect(loginSource).not.toContain("AuthenticatedLeafBackground");
    expect(loginSceneSource).toContain("DnaHelix");
  });

  it("keeps authenticated content above the fixed decorative layer", () => {
    expect(styles).toMatch(/\.authenticated-leaf-background\s*\{/);
    expect(styles).toMatch(/\.workspace\s*\{[\s\S]*?z-index:\s*[1-9]/);
  });

  it("adds no second 3D runtime", () => {
    expect(packageJson.dependencies.ogl).toBeUndefined();
  });
});
