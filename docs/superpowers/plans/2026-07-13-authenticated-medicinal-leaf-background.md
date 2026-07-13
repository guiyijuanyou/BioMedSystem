# Authenticated Medicinal Leaf Background Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a low-cost medicinal-leaf 3D background only to the authenticated application shell while leaving the immersive login experience unchanged.

**Architecture:** `App.vue` asynchronously mounts one authenticated-only background entry. A pure composable resolves desktop/WebGL/reduced-motion/visibility state, while a TresJS scene renders a programmatic Three.js leaf surface, batched veins, instanced flow nodes, and one particle cloud. CSS provides the always-available white/blue/purple/orange fallback layer.

**Tech Stack:** Vue 3, Vite, Three.js, TresJS, GSAP ScrollTrigger, Lenis, Lucide Vue, Vitest, Vue Test Utils, CSS/SVG. No new dependencies.

---

## File Map

- Create `frontend/src/composables/useAuthenticatedBackground.js`: media, WebGL, visibility, DPR, and motion policy.
- Create `frontend/src/components/background/medicinalLeafGeometry.js`: deterministic Three.js geometry, materials, instancing, animation update, and disposal.
- Create `frontend/src/components/background/MedicinalLeafModel.vue`: connect the Three.js model to the TresJS render loop.
- Create `frontend/src/components/background/MedicinalLeafScene.vue`: one transparent TresCanvas with the established camera/light stack.
- Create `frontend/src/components/background/AuthenticatedLeafBackground.vue`: authenticated background entry and static fallback.
- Create `frontend/src/components/__tests__/authenticatedLeafBackground.test.js`: policy, authenticated-only integration, and accessibility tests.
- Create `frontend/src/components/__tests__/medicinalLeafGeometry.test.js`: geometry, color, batching, finite animation, and disposal tests.
- Modify `frontend/src/App.vue`: async import and mount inside the authenticated `.shell` only.
- Modify `frontend/src/styles.css`: fixed background layer, content stacking, palette, responsive and reduced-motion fallback.
- Do not modify any file below `frontend/src/components/login/`, `frontend/src/views/LoginView.vue`, `frontend/src/composables/useLoginScene.js`, or `frontend/src/styles/login-experience.css`.

### Task 1: Background Runtime Policy

**Files:**
- Create: `frontend/src/composables/useAuthenticatedBackground.js`
- Create: `frontend/src/components/__tests__/authenticatedLeafBackground.test.js`

- [ ] **Step 1: Write the failing policy tests**

Add tests that import `AUTHENTICATED_BACKGROUND_LIMITS` and `resolveAuthenticatedBackgroundConfig` and assert:

```js
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

expect(resolveAuthenticatedBackgroundConfig({
  viewportWidth: 390,
  coarsePointer: true,
  reducedMotion: false,
  webglSupported: true,
  hidden: false,
}).shouldRenderWebgl).toBe(false);

expect(resolveAuthenticatedBackgroundConfig({
  viewportWidth: 1440,
  coarsePointer: false,
  reducedMotion: true,
  webglSupported: true,
  hidden: false,
}).shouldRenderWebgl).toBe(false);

expect(resolveAuthenticatedBackgroundConfig({
  viewportWidth: 1440,
  coarsePointer: false,
  reducedMotion: false,
  webglSupported: true,
  hidden: true,
}).active).toBe(false);
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js`

Expected: FAIL because `useAuthenticatedBackground.js` does not exist.

- [ ] **Step 3: Implement the pure policy and lifecycle composable**

Define these stable exports:

```js
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
```

Implement `supportsWebGL()` with a temporary canvas and `webgl2`/`webgl` fallback. Implement `useAuthenticatedBackground()` with Vue refs, `matchMedia('(prefers-reduced-motion: reduce)')`, `matchMedia('(pointer: coarse)')`, `resize`, and `visibilitychange`; clean up every listener in `onBeforeUnmount`.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js`

Expected: policy tests PASS.

- [ ] **Step 5: Commit the runtime policy**

```bash
git add frontend/src/composables/useAuthenticatedBackground.js frontend/src/components/__tests__/authenticatedLeafBackground.test.js
git commit -m "feat: add authenticated background runtime policy"
```

### Task 2: Batched Medicinal Leaf Geometry

**Files:**
- Create: `frontend/src/components/background/medicinalLeafGeometry.js`
- Create: `frontend/src/components/__tests__/medicinalLeafGeometry.test.js`

- [ ] **Step 1: Write failing geometry tests**

Test the public API:

```js
const model = createMedicinalLeafModel({ particleCount: 32 });
const surfaceDispose = vi.spyOn(model.surface.geometry, 'dispose');
const surfaceMaterialDispose = vi.spyOn(model.surface.material, 'dispose');
expect(model.group.children.length).toBeLessThanOrEqual(5);
expect(model.surface.isMesh).toBe(true);
expect(model.surface.geometry.getAttribute('position').count).toBeGreaterThan(100);
expect(model.surface.geometry.getAttribute('color').count)
  .toBe(model.surface.geometry.getAttribute('position').count);
expect(model.veins.isLineSegments).toBe(true);
expect(model.flowNodes.isInstancedMesh).toBe(true);
expect(model.flowNodes.count).toBe(18);
expect(model.particles.isPoints).toBe(true);

updateMedicinalLeafModel(model, 12.5);
expect(model.group.rotation.toArray().every(Number.isFinite)).toBe(true);
expect(Array.from(model.flowNodes.instanceMatrix.array).every(Number.isFinite)).toBe(true);

disposeMedicinalLeafModel(model);
expect(surfaceDispose).toHaveBeenCalledOnce();
expect(surfaceMaterialDispose).toHaveBeenCalledOnce();
```

- [ ] **Step 2: Run the geometry test and verify RED**

Run: `npm test -- --run src/components/__tests__/medicinalLeafGeometry.test.js`

Expected: FAIL because the geometry module does not exist.

- [ ] **Step 3: Implement deterministic leaf construction**

Export `LEAF_RENDER_BUDGET` with `longitudinalSegments: 28`, `widthSegments: 10`, `flowNodeCount: 18`, and `maxDrawObjects: 5`. Export `createMedicinalLeafModel({ particleCount = 32 } = {})`, `updateMedicinalLeafModel(model, elapsed)`, and `disposeMedicinalLeafModel(model)` with those exact signatures.

Build one indexed `BufferGeometry` surface from a 29 by 11 grid. Set a `color` attribute by interpolating `#0073e6` to `#635bff` along leaf length and mixing at most 18% `#ff7a59` near the edge. Use `MeshStandardMaterial` with `vertexColors`, `transparent`, `depthWrite: false`, and low opacity.

Build the center vein and paired secondary veins into one `LineSegments`. Create 18 moving nodes in one `InstancedMesh`, assigning blue, purple, and sparse orange instance colors. Create deterministic ambient positions in one `Points` object. Return direct references required by the tests.

- [ ] **Step 4: Run geometry tests and verify GREEN**

Run: `npm test -- --run src/components/__tests__/medicinalLeafGeometry.test.js`

Expected: all geometry tests PASS with no NaN matrices.

- [ ] **Step 5: Commit the geometry module**

```bash
git add frontend/src/components/background/medicinalLeafGeometry.js frontend/src/components/__tests__/medicinalLeafGeometry.test.js
git commit -m "feat: build batched medicinal leaf geometry"
```

### Task 3: TresJS Leaf Scene and Static Fallback

**Files:**
- Create: `frontend/src/components/background/MedicinalLeafModel.vue`
- Create: `frontend/src/components/background/MedicinalLeafScene.vue`
- Create: `frontend/src/components/background/AuthenticatedLeafBackground.vue`
- Modify: `frontend/src/components/__tests__/authenticatedLeafBackground.test.js`

- [ ] **Step 1: Add failing component tests**

Mount `AuthenticatedLeafBackground` under a reduced-motion `matchMedia` stub and assert:

```js
expect(wrapper.attributes('aria-hidden')).toBe('true');
expect(wrapper.classes()).toContain('authenticated-leaf-background');
expect(wrapper.find('.authenticated-leaf-background__static').exists()).toBe(true);
expect(wrapper.find('canvas').exists()).toBe(false);
```

Read the scene/model source and assert the accepted TresJS v5 primitive and transparent clear configuration:

```js
expect(modelSource).toContain('<primitive :object="model.group" />');
expect(modelSource).not.toContain('TresPrimitive');
expect(sceneSource).toContain('clear-color="#000000"');
expect(sceneSource).toContain(':clear-alpha="0"');
expect(sceneSource).toContain(':dpr="dpr"');
```

- [ ] **Step 2: Run focused tests and verify RED**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js`

Expected: FAIL because the Vue components do not exist.

- [ ] **Step 3: Implement `MedicinalLeafModel.vue`**

Create the model once in setup. Use `useLoop()` and a local elapsed counter. Watch the `active` prop and call the loop's `start()`/`stop()` methods. On each active frame call `updateMedicinalLeafModel(model, elapsed)`. Dispose the model on unmount. Render the Three.js group only through lowercase `<primitive>`.

- [ ] **Step 4: Implement `MedicinalLeafScene.vue`**

Use the same stack as the immersive login scene:

```vue
<TresCanvas
  clear-color="#000000"
  :clear-alpha="0"
  :dpr="dpr"
  :alpha="true"
  :antialias="true"
>
  <TresPerspectiveCamera :position="[0, 0, 9]" :fov="42" />
  <TresAmbientLight :intensity="1.25" color="#edf4ff" />
  <TresDirectionalLight :position="[4, 5, 6]" :intensity="2.8" color="#68cfff" />
  <TresPointLight :position="[-3, -1, 4]" :intensity="18" color="#ff8a4c" />
  <MedicinalLeafModel :active="active" :particle-count="particleCount" />
</TresCanvas>
```

- [ ] **Step 5: Implement `AuthenticatedLeafBackground.vue`**

Always render the static layer. Use `useAuthenticatedBackground()` to gate the async scene. Catch the dynamic import failure and resolve to an empty render component while leaving the static layer visible. Pass `active`, `dpr`, and `particleCount`. Mark the root `aria-hidden="true"`.

- [ ] **Step 6: Run focused tests and verify GREEN**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js src/components/__tests__/medicinalLeafGeometry.test.js`

Expected: both files PASS.

- [ ] **Step 7: Commit the scene components**

```bash
git add frontend/src/components/background frontend/src/components/__tests__/authenticatedLeafBackground.test.js
git commit -m "feat: add authenticated medicinal leaf scene"
```

### Task 4: Authenticated Shell Integration and Layering

**Files:**
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/styles.css`
- Modify: `frontend/src/components/__tests__/authenticatedLeafBackground.test.js`

- [ ] **Step 1: Add failing authenticated-only integration tests**

Read `App.vue`, `LoginView.vue`, login scene source, styles, and `package.json` as UTF-8. Assert:

```js
expect(appSource).toContain('defineAsyncComponent');
expect(appSource).toContain('@/components/background/AuthenticatedLeafBackground.vue');
expect(appSource).toMatch(/<div class="shell"[\s\S]*?<AuthenticatedLeafBackground/);
expect(loginSource).not.toContain('AuthenticatedLeafBackground');
expect(loginSceneSource).toContain('DnaHelix');
expect(styles).toMatch(/\.authenticated-leaf-background\s*\{/);
expect(styles).toMatch(/\.workspace\s*\{[\s\S]*?z-index:\s*[1-9]/);
expect(packageJson.dependencies.ogl).toBeUndefined();
```

- [ ] **Step 2: Run focused tests and verify RED**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js`

Expected: FAIL because `App.vue` and authenticated styles have not been integrated.

- [ ] **Step 3: Integrate only the authenticated branch**

Import `defineAsyncComponent` from Vue and define:

```js
const AuthenticatedLeafBackground = defineAsyncComponent(
  () => import('@/components/background/AuthenticatedLeafBackground.vue'),
);
```

Mount `<AuthenticatedLeafBackground />` as the first child inside the authenticated `.shell`. Do not edit the unauthenticated `<router-view v-if="!isAuthenticated" />` branch or any login file.

- [ ] **Step 4: Add final authenticated background CSS**

Append one scoped section beginning with `/* Authenticated medicinal leaf background */`. Establish `.shell { position: relative; isolation: isolate; }`, fixed background z-index 0, `.workspace { position: relative; z-index: 1; }`, and preserve existing nav/overlay z-indices. Build the static white/blue/purple/orange gradient with CSS radial gradients and a faint line grid. Apply `pointer-events: none`, a right-weighted mask, and low canvas opacity. Hide the canvas under `860px`, coarse pointers, forced colors, and reduced motion.

- [ ] **Step 5: Run focused and login regression tests**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js src/components/login/__tests__/loginExperience.test.js`

Expected: all tests PASS and the login experience test count remains unchanged.

- [ ] **Step 6: Commit shell integration**

```bash
git add frontend/src/App.vue frontend/src/styles.css frontend/src/components/__tests__/authenticatedLeafBackground.test.js
git commit -m "feat: integrate authenticated medicinal leaf background"
```

### Task 5: Full Verification and Runtime QA

**Files:**
- No production file changes unless verification exposes a defect.

- [ ] **Step 1: Run the complete test suite**

Run: `npm test -- --run`

Expected: all Vitest files PASS with zero failures.

- [ ] **Step 2: Run the production build**

Run: `npm run build`

Expected: Vite exits 0; no missing TresJS/Three exports and no OGL/Vue Bits dependency is added.

- [ ] **Step 3: Check patch integrity**

Run: `git diff --check`

Expected: no whitespace errors. Existing line-ending-only warnings may remain on the unrelated map design document.

- [ ] **Step 4: Verify the authenticated browser flow technically**

Run the existing frontend and backend. Exercise login to `/dashboard`. Verify the page identity, no framework overlay, no console errors, one nonblank authenticated background canvas, and clickable dashboard controls. Resize below 860px and verify the canvas is absent while the static layer remains.

Do not make subjective visual-acceptance claims; the user performs final visual acceptance.

- [ ] **Step 5: Report completion without pushing**

Report commits, tests, build, runtime evidence, remaining user-owned map-document status, and the local dev URL. Do not push unless explicitly requested.
