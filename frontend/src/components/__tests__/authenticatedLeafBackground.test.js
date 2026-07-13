import { describe, expect, it } from "vitest";
import {
  AUTHENTICATED_BACKGROUND_LIMITS,
  resolveAuthenticatedBackgroundConfig,
} from "@/composables/useAuthenticatedBackground";

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
