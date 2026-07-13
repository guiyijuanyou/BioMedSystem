import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import AuthenticatedLeafBackground from "@/components/background/AuthenticatedLeafBackground.vue";
import {
  AUTHENTICATED_BACKGROUND_LIMITS,
  resolveAuthenticatedBackgroundConfig,
} from "@/composables/useAuthenticatedBackground";

const appSource = readFileSync(resolve(process.cwd(), "src/App.vue"), "utf8");
const loginSource = readFileSync(resolve(process.cwd(), "src/views/LoginView.vue"), "utf8");
const loginSceneSource = readFileSync(
  resolve(process.cwd(), "src/components/login/BiomedScene.vue"),
  "utf8",
);
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
    dispatchEvent: vi.fn(),
  };
}

beforeEach(() => {
  Object.defineProperty(window, "innerWidth", { configurable: true, value: 1440 });
  vi.stubGlobal("matchMedia", vi.fn(query => mediaQuery(query.includes("prefers-reduced-motion"))));
  vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(null);
});

afterEach(() => {
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

describe("authenticated background runtime policy", () => {
  it("enables the bounded desktop WebGL configuration", () => {
    expect(resolveAuthenticatedBackgroundConfig({
      viewportWidth: 1440,
      coarsePointer: false,
      reducedMotion: false,
      webglSupported: true,
      hidden: false,
    })).toEqual({
      shouldRenderWebgl: true,
      active: true,
      dpr: [1, 1.25],
      particleCount: 32,
    });

    expect(AUTHENTICATED_BACKGROUND_LIMITS).toEqual({
      mobileBreakpoint: 768,
      maxDpr: 1.25,
      desktopParticleCount: 32,
    });
  });

  it("falls back on mobile and coarse-pointer devices", () => {
    const config = resolveAuthenticatedBackgroundConfig({
      viewportWidth: 390,
      coarsePointer: true,
      reducedMotion: false,
      webglSupported: true,
      hidden: false,
    });

    expect(config.shouldRenderWebgl).toBe(false);
    expect(config.particleCount).toBe(0);
  });

  it("falls back when reduced motion is requested", () => {
    expect(resolveAuthenticatedBackgroundConfig({
      viewportWidth: 1440,
      coarsePointer: false,
      reducedMotion: true,
      webglSupported: true,
      hidden: false,
    }).shouldRenderWebgl).toBe(false);
  });

  it("pauses active updates while the page is hidden", () => {
    const config = resolveAuthenticatedBackgroundConfig({
      viewportWidth: 1440,
      coarsePointer: false,
      reducedMotion: false,
      webglSupported: true,
      hidden: true,
    });

    expect(config.shouldRenderWebgl).toBe(true);
    expect(config.active).toBe(false);
  });
});

describe("authenticated leaf background components", () => {
  it("keeps the static decorative fallback under reduced motion", () => {
    const wrapper = mount(AuthenticatedLeafBackground);

    expect(wrapper.attributes("aria-hidden")).toBe("true");
    expect(wrapper.classes()).toContain("authenticated-leaf-background");
    expect(wrapper.find(".authenticated-leaf-background__static").exists()).toBe(true);
    expect(wrapper.find("canvas").exists()).toBe(false);

    wrapper.unmount();
  });

  it("uses the accepted TresJS primitive and transparent canvas configuration", () => {
    const modelSource = readFileSync(
      resolve(process.cwd(), "src/components/background/MedicinalLeafModel.vue"),
      "utf8",
    );
    const sceneSource = readFileSync(
      resolve(process.cwd(), "src/components/background/MedicinalLeafScene.vue"),
      "utf8",
    );

    expect(modelSource).toContain('<primitive :object="model.group" />');
    expect(modelSource).not.toContain("TresPrimitive");
    expect(sceneSource).toContain('clear-color="#000000"');
    expect(sceneSource).toContain(':clear-alpha="0"');
    expect(sceneSource).toContain(':dpr="dpr"');
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
