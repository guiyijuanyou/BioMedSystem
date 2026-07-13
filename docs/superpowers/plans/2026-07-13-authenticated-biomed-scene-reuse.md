# Authenticated BiomedScene Reuse Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the authenticated medicinal-leaf background with the exact homepage `BiomedScene` visual and animation, with pointer interaction disabled only inside the authenticated shell.

**Architecture:** Keep `App.vue`'s authenticated-only async background entry. Rewrite `AuthenticatedLeafBackground.vue` to render the existing `BiomedScene` with `progress=0`; move the scene's effective visual rules into a shared stylesheet loaded by `BiomedScene.vue`; apply only `pointer-events: none` and fixed-layer geometry at the authenticated wrapper. Delete the custom leaf policy, geometry, scene, and tests.

**Tech Stack:** Vue 3, TresJS, Three.js, CSS, Vitest, Vue Test Utils, Vite. No dependency changes.

---

## File Map

- Create: `frontend/src/styles/biomed-scene.css` — shared final visual rules for `BiomedScene`.
- Modify: `frontend/src/components/login/BiomedScene.vue` — import the shared scene stylesheet; do not change template or scene logic.
- Modify: `frontend/src/components/background/AuthenticatedLeafBackground.vue` — render `BiomedScene` at `progress=0`.
- Modify: `frontend/src/components/__tests__/authenticatedLeafBackground.test.js` — verify direct reuse, fixed progress, shared styles, and no leaf implementation references.
- Modify: `frontend/src/styles/login-experience.css` — remove the duplicated `.biomed-scene` rules that move to the shared stylesheet.
- Modify: `frontend/src/styles.css` — replace the authenticated medicinal-leaf section with a fixed DNA wrapper and pointer-event override; preserve workspace stacking.
- Delete: `frontend/src/composables/useAuthenticatedBackground.js`.
- Delete: `frontend/src/components/background/medicinalLeafGeometry.js`.
- Delete: `frontend/src/components/background/MedicinalLeafModel.vue`.
- Delete: `frontend/src/components/background/MedicinalLeafScene.vue`.
- Delete: `frontend/src/components/__tests__/medicinalLeafGeometry.test.js`.

### Task 1: Write the failing reuse tests

**Files:**
- Modify: `frontend/src/components/__tests__/authenticatedLeafBackground.test.js`

- [ ] **Step 1: Replace policy and leaf assertions with reuse assertions**

Remove imports of `useAuthenticatedBackground`, the media-query stubs, and the policy tests. Keep the existing authenticated-shell source checks and add these assertions:

```js
const backgroundSource = readFileSync(
  resolve(process.cwd(), "src/components/background/AuthenticatedLeafBackground.vue"),
  "utf8",
);
const biomedSceneSource = readFileSync(
  resolve(process.cwd(), "src/components/login/BiomedScene.vue"),
  "utf8",
);
const sharedSceneStyles = readFileSync(
  resolve(process.cwd(), "src/styles/biomed-scene.css"),
  "utf8",
);

it("reuses the homepage scene at its first-screen state", () => {
  expect(backgroundSource).toContain('import BiomedScene from "@/components/login/BiomedScene.vue"');
  expect(backgroundSource).toContain('<BiomedScene :progress="0" />');
  expect(backgroundSource).not.toContain("MedicinalLeafScene");
  expect(backgroundSource).not.toContain("useAuthenticatedBackground");
  expect(biomedSceneSource).toContain("@/styles/biomed-scene.css");
  expect(sharedSceneStyles).toContain(".biomed-scene::after");
});

it("disables interaction only for the authenticated wrapper", () => {
  expect(styles).toMatch(/\.authenticated-leaf-background\s+\.biomed-scene\s*\{[\s\S]*?pointer-events:\s*none/);
  expect(styles).not.toContain("authenticated-leaf-background__static-leaf");
  expect(loginSceneSource).toContain("@pointermove=\"movePointer\"");
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
```

Mount the wrapper with a `BiomedScene` stub so the test remains a unit test:

```js
const wrapper = mount(AuthenticatedLeafBackground, {
  global: {
    stubs: {
      BiomedScene: { props: ["progress"], template: '<div data-test="biomed-scene-stub" />' },
    },
  },
});
expect(wrapper.find('[data-test="biomed-scene-stub"]').exists()).toBe(true);
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js`

Expected: FAIL because the wrapper still imports the leaf scene, the shared stylesheet does not exist, and the retired files are still present.

### Task 2: Implement exact shared `BiomedScene` reuse

**Files:**
- Create: `frontend/src/styles/biomed-scene.css`
- Modify: `frontend/src/components/login/BiomedScene.vue`
- Modify: `frontend/src/components/background/AuthenticatedLeafBackground.vue`
- Modify: `frontend/src/styles/login-experience.css`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1: Create the shared scene stylesheet with the homepage's effective rules**

Create the following stylesheet, preserving the homepage's current desktop and `max-width: 900px` gradient overlays:

```css
.biomed-scene {
  position: absolute;
  inset: 0;
  pointer-events: auto;
}

.biomed-scene canvas {
  opacity: .98;
}

.biomed-scene::after {
  position: absolute;
  inset: 0;
  content: "";
  pointer-events: none;
  background:
    radial-gradient(ellipse at 83% 8%, rgba(255, 133, 66, .24), transparent 28%),
    radial-gradient(ellipse at 70% 17%, rgba(255, 83, 168, .18), transparent 32%),
    radial-gradient(ellipse at 58% 13%, rgba(99, 91, 255, .17), transparent 35%),
    radial-gradient(circle at 28% 40%, rgba(104, 207, 255, .13), transparent 30%),
    linear-gradient(90deg, rgba(255,255,255,.88) 0%, rgba(246,249,252,.38) 48%, rgba(255,255,255,.25) 100%);
}

.biomed-scene__fallback {
  display: none;
}

@media (max-width: 900px) {
  .biomed-scene::after {
    background:
      radial-gradient(ellipse at 76% 8%, rgba(255,122,89,.2), transparent 32%),
      radial-gradient(ellipse at 58% 16%, rgba(99,91,255,.15), transparent 38%),
      linear-gradient(180deg,rgba(255,255,255,.28),rgba(246,249,252,.62) 55%,rgba(246,249,252,.94));
  }
}
```

- [ ] **Step 2: Load shared styles from `BiomedScene.vue`**

Add this import to the existing script setup without changing any template or logic:

```js
import "@/styles/biomed-scene.css";
```

- [ ] **Step 3: Remove duplicate scene rules from login CSS**

Remove the standalone `.biomed-scene` base block, the later desktop `.biomed-scene::after` override, the `max-width: 900px` `.biomed-scene::after` block, and the `.biomed-scene__fallback` declaration from `styles/login-experience.css`. Leave all login layout and narrative rules untouched.

- [ ] **Step 4: Replace the authenticated wrapper with the homepage scene**

Replace the wrapper script/template with:

```vue
<script setup>
import BiomedScene from "@/components/login/BiomedScene.vue";
</script>

<template>
  <div class="authenticated-leaf-background" aria-hidden="true">
    <BiomedScene :progress="0" />
  </div>
</template>
```

- [ ] **Step 5: Replace leaf CSS with the single interaction difference**

Replace the authenticated medicinal-leaf section with:

```css
/* Authenticated homepage DNA background */
.shell,
.shell.nav-collapsed {
  position: relative;
  isolation: isolate;
}

.authenticated-leaf-background {
  position: fixed;
  z-index: 0;
  inset: 0;
  overflow: hidden;
  background: #f6f9fc;
  pointer-events: none;
}

.authenticated-leaf-background .biomed-scene {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.workspace {
  position: relative;
  z-index: 1;
}
```

Do not add mobile or reduced-motion rules that hide the Canvas; the approved behavior is to keep the same WebGL scene in every environment.

- [ ] **Step 6: Run the focused tests and verify GREEN**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js src/components/login/__tests__/loginExperience.test.js`

Expected: all reuse, shell, and login regression tests PASS.

- [ ] **Step 7: Commit shared scene reuse**

```bash
git add frontend/src/styles/biomed-scene.css frontend/src/components/login/BiomedScene.vue frontend/src/components/background/AuthenticatedLeafBackground.vue frontend/src/styles/login-experience.css frontend/src/styles.css frontend/src/components/__tests__/authenticatedLeafBackground.test.js
git commit -m "feat: reuse homepage DNA scene after authentication"
```

### Task 3: Remove the retired medicinal-leaf implementation

**Files:**
- Delete: `frontend/src/composables/useAuthenticatedBackground.js`
- Delete: `frontend/src/components/background/medicinalLeafGeometry.js`
- Delete: `frontend/src/components/background/MedicinalLeafModel.vue`
- Delete: `frontend/src/components/background/MedicinalLeafScene.vue`
- Delete: `frontend/src/components/__tests__/medicinalLeafGeometry.test.js`

- [ ] **Step 1: Delete only the listed leaf files**

Use `git rm` for exactly the five files above. Do not remove `AuthenticatedLeafBackground.vue`, `BiomedScene.vue`, `DnaHelix.vue`, or any login styles outside the scene rules.

- [ ] **Step 2: Search for stale leaf imports**

Run: `Get-ChildItem -LiteralPath frontend/src -Recurse -File | Where-Object { $_.Name -ne 'authenticatedLeafBackground.test.js' } | Select-String -Pattern 'MedicinalLeaf|useAuthenticatedBackground|medicinalLeafGeometry|authenticated-leaf-background__static'`

Expected: no matches.

- [ ] **Step 3: Run focused tests and verify the deletion is green**

Run: `npm test -- --run src/components/__tests__/authenticatedLeafBackground.test.js src/components/login/__tests__/loginExperience.test.js`

Expected: all focused tests PASS with the deleted-file assertions green.

- [ ] **Step 4: Commit the cleanup**

```bash
git add -u frontend/src/composables/useAuthenticatedBackground.js frontend/src/components/background frontend/src/components/__tests__/medicinalLeafGeometry.test.js
git commit -m "refactor: remove medicinal leaf background implementation"
```

### Task 4: Full verification and technical runtime QA

**Files:**
- No production changes unless a verification failure identifies a defect.

- [ ] **Step 1: Run the complete test suite**

Run: `npm test -- --run`

Expected: all remaining Vitest files PASS with zero failures.

- [ ] **Step 2: Run the production build**

Run: `npm run build`

Expected: Vite exits 0 and emits the shared `BiomedScene` CSS without missing imports.

- [ ] **Step 3: Check patch integrity**

Run: `git diff --check`

Expected: no whitespace errors.

- [ ] **Step 4: Verify desktop authenticated runtime**

Start the existing backend and Vite dev server, then use Playwright to log in with `admin / 123456` at `/login`. Verify `/dashboard` has one `.authenticated-leaf-background`, one nonblank Canvas, a fixed full-viewport background, and no framework overlay or relevant console errors. Click `刷新数据` and verify the toast appears.

- [ ] **Step 5: Verify mobile and non-interaction behavior**

Resize the authenticated page to `390x844`. Verify the same Canvas remains present, the static background color remains `#f6f9fc`, there is no horizontal overflow, and the background root has `pointer-events: none`. The user performs final visual acceptance; do not make subjective style claims.

- [ ] **Step 6: Commit any verification-only fix and report without pushing**

Run `git status --short --branch` and report the commits, tests, build, runtime evidence, local URL, and the preserved user-owned map document status. Do not push unless explicitly requested.
