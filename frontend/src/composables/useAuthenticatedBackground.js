import { computed, onBeforeUnmount, onMounted, ref } from "vue";

export const AUTHENTICATED_BACKGROUND_LIMITS = Object.freeze({
  mobileBreakpoint: 768,
  maxDpr: 1.25,
  desktopParticleCount: 32,
});

export function resolveAuthenticatedBackgroundConfig({
  viewportWidth = 0,
  coarsePointer = false,
  reducedMotion = false,
  webglSupported = false,
  hidden = false,
} = {}) {
  const desktop = viewportWidth >= AUTHENTICATED_BACKGROUND_LIMITS.mobileBreakpoint;
  const shouldRenderWebgl = desktop && !coarsePointer && !reducedMotion && webglSupported;

  return {
    shouldRenderWebgl,
    active: shouldRenderWebgl && !hidden,
    dpr: viewportWidth >= 1280 ? [1, AUTHENTICATED_BACKGROUND_LIMITS.maxDpr] : [1, 1.1],
    particleCount: shouldRenderWebgl ? AUTHENTICATED_BACKGROUND_LIMITS.desktopParticleCount : 0,
  };
}

export function supportsWebGL() {
  if (typeof document === "undefined") return false;

  try {
    const canvas = document.createElement("canvas");
    return Boolean(canvas.getContext("webgl2") || canvas.getContext("webgl"));
  } catch {
    return false;
  }
}

function addMediaListener(query, listener) {
  if (query.addEventListener) query.addEventListener("change", listener);
  else query.addListener(listener);
}

function removeMediaListener(query, listener) {
  if (query.removeEventListener) query.removeEventListener("change", listener);
  else query.removeListener(listener);
}

export function useAuthenticatedBackground() {
  const viewportWidth = ref(typeof window === "undefined" ? 0 : window.innerWidth);
  const coarsePointer = ref(false);
  const reducedMotion = ref(false);
  const webglSupported = ref(false);
  const hidden = ref(typeof document === "undefined" || document.visibilityState === "hidden");

  let reducedMotionQuery;
  let coarsePointerQuery;

  const updateViewport = () => {
    viewportWidth.value = window.innerWidth;
  };
  const updateVisibility = () => {
    hidden.value = document.visibilityState === "hidden";
  };
  const updateReducedMotion = event => {
    reducedMotion.value = event.matches;
  };
  const updateCoarsePointer = event => {
    coarsePointer.value = event.matches;
  };

  onMounted(() => {
    reducedMotionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
    coarsePointerQuery = window.matchMedia("(pointer: coarse)");

    reducedMotion.value = reducedMotionQuery.matches;
    coarsePointer.value = coarsePointerQuery.matches;
    webglSupported.value = supportsWebGL();
    updateViewport();
    updateVisibility();

    addMediaListener(reducedMotionQuery, updateReducedMotion);
    addMediaListener(coarsePointerQuery, updateCoarsePointer);
    window.addEventListener("resize", updateViewport);
    document.addEventListener("visibilitychange", updateVisibility);
  });

  onBeforeUnmount(() => {
    if (reducedMotionQuery) removeMediaListener(reducedMotionQuery, updateReducedMotion);
    if (coarsePointerQuery) removeMediaListener(coarsePointerQuery, updateCoarsePointer);
    window.removeEventListener("resize", updateViewport);
    document.removeEventListener("visibilitychange", updateVisibility);
  });

  const config = computed(() => resolveAuthenticatedBackgroundConfig({
    viewportWidth: viewportWidth.value,
    coarsePointer: coarsePointer.value,
    reducedMotion: reducedMotion.value,
    webglSupported: webglSupported.value,
    hidden: hidden.value,
  }));

  return {
    shouldRenderWebgl: computed(() => config.value.shouldRenderWebgl),
    active: computed(() => config.value.active),
    dpr: computed(() => config.value.dpr),
    particleCount: computed(() => config.value.particleCount),
  };
}
