# Login Stepper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the existing single-screen login form with a three-step animated account, password, and confirmation flow while preserving the current authentication request and visual identity.

**Architecture:** Add a reusable Vue `Stepper` component that owns navigation and `motion-v` transitions but knows nothing about authentication. Keep credentials, field validation, password visibility, demo accounts, and submit events inside `LoginModal`; keep the `/api/auth/login` request and routing unchanged in `LoginView`.

**Tech Stack:** Vue 3 Composition API, Vite, Vitest, Vue Test Utils, `motion-v`, `@vueuse/core`, lucide-vue-next, plain CSS.

**Current Baseline:** Remote `main` at `b94c0d4`. Commits `1388471` and `b94c0d4` add the DeepSeek assistant, resource/file workflows, and a globally mounted `AppDialog`, but do not change the login components, login tests, frontend dependencies, or `/api/auth/login` flow.

---

## File Map

- Create `frontend/src/components/login/Stepper.vue`: generic animated step navigation, indicators, connectors, and exposed reset method.
- Create `frontend/src/components/login/__tests__/Stepper.test.js`: isolated navigation, validation gate, reset, and completion tests.
- Modify `frontend/src/components/login/LoginModal.vue`: compose the three login steps and preserve the existing submit contract.
- Modify `frontend/src/components/login/__tests__/loginExperience.test.js`: cover the complete login workflow and regression behavior.
- Modify `frontend/src/styles/login-experience.css`: stepper layout, purple-orange state colors, responsive rules, and retained shine animation.
- Modify `frontend/package.json` and `frontend/package-lock.json`: add `motion-v` and its required `@vueuse/core` peer dependency directly.

### Task 1: Add the animation dependency

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/package-lock.json`

- [ ] **Step 1: Install `motion-v` and its required peer dependency in the frontend workspace**

Run:

```powershell
npm install motion-v @vueuse/core
```

Working directory: `frontend`

Expected: command exits with code 0 and adds both `motion-v` and `@vueuse/core` to `dependencies`.

- [ ] **Step 2: Verify the dependency is resolvable**

Run:

```powershell
npm ls motion-v @vueuse/core --depth=0
```

Expected: output contains both `motion-v@` and `@vueuse/core@` and exits with code 0.

- [ ] **Step 3: Commit the dependency change**

```powershell
git add frontend/package.json frontend/package-lock.json
git commit -m "build: add motion-v for login stepper"
```

### Task 2: Build the reusable Stepper with tests first

**Files:**
- Create: `frontend/src/components/login/__tests__/Stepper.test.js`
- Create: `frontend/src/components/login/Stepper.vue`

- [ ] **Step 1: Write the failing Stepper behavior tests**

Create `Stepper.test.js` with these cases and selectors:

```js
import { h } from "vue";
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import Stepper from "@/components/login/Stepper.vue";

const motionStubs = {
  Motion: { template: "<div><slot /></div>" },
  AnimatePresence: { template: "<div><slot /></div>" }
};

function mountStepper(props = {}) {
  return mount(Stepper, {
    props: { initialStep: 1, ...props },
    slots: {
      default: [
        h("section", { "data-test": "step-one" }, "账号"),
        h("section", { "data-test": "step-two" }, "密码"),
        h("section", { "data-test": "step-three" }, "确认")
      ]
    },
    global: { stubs: motionStubs }
  });
}

describe("Login Stepper", () => {
  it("advances, goes back, and emits the active step", async () => {
    const wrapper = mountStepper();
    expect(wrapper.get('[data-test="step-indicator-1"]').attributes("data-status")).toBe("active");
    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    expect(wrapper.get('[data-test="step-indicator-1"]').attributes("data-status")).toBe("complete");
    expect(wrapper.emitted("step-change")?.[0]).toEqual([2]);
    await wrapper.get('[data-test="stepper-back"]').trigger("click");
    expect(wrapper.emitted("step-change")?.[1]).toEqual([1]);
  });

  it("does not advance when beforeNext rejects the current step", async () => {
    const beforeNext = vi.fn(() => false);
    const wrapper = mountStepper({ beforeNext });
    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    expect(beforeNext).toHaveBeenCalledWith(1);
    expect(wrapper.get('[data-test="step-indicator-1"]').attributes("data-status")).toBe("active");
  });

  it("emits completion on the last step and can reset", async () => {
    const wrapper = mountStepper();
    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    expect(wrapper.emitted("final-step-completed")).toHaveLength(1);
    wrapper.vm.reset();
    await wrapper.vm.$nextTick();
    expect(wrapper.get('[data-test="step-indicator-1"]').attributes("data-status")).toBe("active");
  });
});
```

- [ ] **Step 2: Run the new test and verify it fails**

Run:

```powershell
npx vitest run src/components/login/__tests__/Stepper.test.js
```

Working directory: `frontend`

Expected: FAIL because `@/components/login/Stepper.vue` does not exist.

- [ ] **Step 3: Implement the minimal Stepper component**

Create a Vue component with this public contract:

```js
const props = defineProps({
  initialStep: { type: Number, default: 1 },
  beforeNext: { type: Function, default: () => true },
  backButtonText: { type: String, default: "上一步" },
  nextButtonText: { type: String, default: "下一步" },
  completeButtonText: { type: String, default: "登录系统" },
  disabled: { type: Boolean, default: false },
  disableStepIndicators: { type: Boolean, default: true }
});

const emit = defineEmits(["step-change", "final-step-completed"]);
```

Implementation requirements:

- Derive `stepsArray` from the default slot and ignore comment/empty nodes.
- Track `currentStep`, `direction`, and measured content height with refs.
- Render `data-test="step-indicator-N"` and `data-status="active|complete|inactive"` for every step.
- Render a `Check` icon for completed steps, an inner dot for the active step, and the step number otherwise.
- Use `Motion` for indicator state, connector fill, and sliding step content; use `AnimatePresence` for keyed content changes.
- Use `<button type="button">` for navigation so the parent form does not submit early.
- Call `await props.beforeNext(currentStep.value)` before moving forward or completing.
- Emit `final-step-completed` without advancing past the last step so a failed login remains visible.
- Expose `reset()`, `next()`, and `back()` with `defineExpose`.
- Ignore navigation while `disabled` is true.
- Keep every new selector under the `login-stepper__*` namespace. Do not use the latest global dialog names `.dialog-layer`, `.app-dialog`, `.dialog-*`, or `.dialog-button`.

The navigation buttons must use these stable selectors:

```vue
<button v-if="currentStep > 1" data-test="stepper-back" type="button" @click="back">
  {{ backButtonText }}
</button>
<button data-test="stepper-next" type="button" :disabled="disabled" @click="next">
  {{ isLastStep ? completeButtonText : nextButtonText }}
  <i v-if="isLastStep" aria-hidden="true"></i>
</button>
```

- [ ] **Step 4: Run the Stepper test and verify it passes**

Run:

```powershell
npx vitest run src/components/login/__tests__/Stepper.test.js
```

Expected: all three Stepper tests PASS.

- [ ] **Step 5: Commit the component and tests**

```powershell
git add frontend/src/components/login/Stepper.vue frontend/src/components/login/__tests__/Stepper.test.js
git commit -m "feat: add animated login stepper"
```

### Task 3: Refactor LoginModal through workflow tests

**Files:**
- Modify: `frontend/src/components/login/LoginModal.vue:1-94`
- Modify: `frontend/src/components/login/__tests__/loginExperience.test.js:96-114`

- [ ] **Step 1: Replace the single-form regression test with three-step tests**

Replace the existing login submission test with the following mount helper and assertions:

```js
const modalMotionStubs = {
  teleport: true,
  Motion: { template: "<div><slot /></div>" },
  AnimatePresence: { template: "<div><slot /></div>" }
};

function mountLogin(credentials, extraProps = {}) {
  return mount(LoginModal, {
    props: {
      modelValue: true,
      credentials,
      loading: false,
      errorText: "",
      ...extraProps
    },
    global: { stubs: modalMotionStubs }
  });
}

it("validates each step and submits the unchanged credential payload", async () => {
  const wrapper = mountLogin({ username: "", password: "" });

  await wrapper.get('[data-test="stepper-next"]').trigger("click");
  expect(wrapper.text()).toContain("请输入系统账号");

  await wrapper.get('[data-test="username-input"]').setValue("admin");
  await wrapper.get('[data-test="stepper-next"]').trigger("click");
  await wrapper.get('[data-test="stepper-next"]').trigger("click");
  expect(wrapper.text()).toContain("请输入登录密码");

  await wrapper.get('[data-test="password-input"]').setValue("123456");
  await wrapper.get('[data-test="stepper-next"]').trigger("click");
  expect(wrapper.text()).toContain("admin");

  await wrapper.get('[data-test="stepper-next"]').trigger("click");
  expect(wrapper.emitted("submit")?.[0]).toEqual([{ username: "admin", password: "123456" }]);
});

it("fills a demo account and resets to the first step when reopened", async () => {
  const wrapper = mountLogin({ username: "admin", password: "123456" });
  await wrapper.get('[data-test="stepper-next"]').trigger("click");
  await wrapper.get('[data-test="demo-toggle"]').trigger("click");
  await wrapper.get('[data-test="demo-researcher"]').trigger("click");
  expect(wrapper.get('[data-test="password-input"]').element.value).toBe("123456");

  await wrapper.setProps({ modelValue: false });
  await wrapper.setProps({ modelValue: true });
  expect(wrapper.get('[data-test="step-indicator-1"]').attributes("data-status")).toBe("active");
});
```

- [ ] **Step 2: Run the login component tests and verify they fail**

Run:

```powershell
npx vitest run src/components/login/__tests__/loginExperience.test.js
```

Expected: FAIL because the current modal has no stepper selectors or step validation.

- [ ] **Step 3: Implement the three-step LoginModal composition**

Update the script with these state and handlers while retaining the existing props and emitted events:

```js
import Stepper from "./Stepper.vue";

const stepper = ref(null);
const passwordInput = ref(null);
const currentStep = ref(1);
const validationText = ref("");

function validateStep(step) {
  validationText.value = "";
  if (step === 1 && !localCredentials.value.username.trim()) {
    validationText.value = "请输入系统账号";
    return false;
  }
  if (step === 2 && !localCredentials.value.password) {
    validationText.value = "请输入登录密码";
    return false;
  }
  return true;
}

async function handleStepChange(step) {
  currentStep.value = step;
  validationText.value = "";
  await nextTick();
  if (step === 1) usernameInput.value?.focus({ preventScroll: true });
  if (step === 2) passwordInput.value?.focus({ preventScroll: true });
}

function chooseDemo(username) {
  localCredentials.value = { username, password: "123456" };
  validationText.value = "";
}

function submit() {
  if (props.loading || !validateStep(1) || !validateStep(2)) return;
  emit("submit", { ...localCredentials.value });
}
```

On every open, call `stepper.value?.reset()`, reset `currentStep`, validation, demo expansion, and password visibility, then focus the username. Compose these three slot children inside one `<form @submit.prevent>`:

- Step 1: title “输入系统账号”, helper copy, username input with `data-test="username-input"`, and local validation text.
- Step 2: title “验证访问密码”, password input with `data-test="password-input"`, visibility toggle, demo toggle, and demo account buttons with `data-test="demo-admin|teacher|researcher|student"`.
- Step 3: title “确认登录信息”, account summary, backend `errorText`, and a security note. The Stepper final button emits the existing submit event.

Pass `:disabled="loading"`, `:before-next="validateStep"`, and localized button labels to Stepper. Keep the close button and backdrop behavior unchanged; they already prevent close while loading.

- [ ] **Step 4: Run login workflow tests and verify they pass**

Run:

```powershell
npx vitest run src/components/login/__tests__/loginExperience.test.js
```

Expected: all immersive login tests PASS.

- [ ] **Step 5: Commit the login workflow**

```powershell
git add frontend/src/components/login/LoginModal.vue frontend/src/components/login/__tests__/loginExperience.test.js
git commit -m "feat: convert login modal to three steps"
```

### Task 4: Apply the accepted visual system

**Files:**
- Modify: `frontend/src/styles/login-experience.css`

- [ ] **Step 1: Add stable Stepper layout and state styles**

Add rules for these component classes:

```css
.login-stepper__track { display: flex; align-items: center; padding: 4px 8px 26px; }
.login-stepper__indicator { display: grid; place-items: center; width: 34px; height: 34px; border: 1px solid rgba(99,91,255,.18); border-radius: 50%; color: #8793a5; background: #f2f5fa; }
.login-stepper__indicator[data-status="active"],
.login-stepper__indicator[data-status="complete"] { color: #fff; border-color: transparent; background: linear-gradient(135deg,#635bff,#ff5ca8 62%,#ff8a4c); box-shadow: 0 8px 20px rgba(99,91,255,.2); }
.login-stepper__connector { position: relative; flex: 1; height: 2px; margin: 0 10px; overflow: hidden; border-radius: 999px; background: rgba(99,91,255,.12); }
.login-stepper__connector-fill { position: absolute; inset: 0; transform-origin: left; background: linear-gradient(90deg,#635bff,#ff5ca8,#ff8a4c); }
.login-stepper__content { position: relative; overflow: hidden; }
.login-stepper__footer { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin-top: 28px; }
.login-stepper__back { border: 0; color: #697386; background: transparent; }
.login-stepper__next { position: relative; min-width: 126px; height: 48px; margin-left: auto; overflow: hidden; border: 0; border-radius: 13px; color: #fff; background: linear-gradient(110deg,#635bff,#a45cff 58%,#ff7a59); box-shadow: 0 12px 28px rgba(99,91,255,.22); }
.login-stepper__next i { position: absolute; inset-block: 0; width: 45%; transform: skewX(-25deg) translateX(-220%); background: linear-gradient(90deg,transparent,rgba(255,255,255,.58),transparent); animation: submit-shine 2.3s infinite; }
```

Add the exact content typography and confirmation layout rules:

```css
.login-step { min-height: 206px; padding: 2px 2px 4px; }
.login-step__title { margin: 0; color: #0a2540; font-size: 20px; font-weight: 760; line-height: 1.3; }
.login-step__copy { margin: 8px 0 22px; color: #697386; font-size: 13px; line-height: 1.65; }
.login-step .login-field { margin-top: 0; }
.login-step__validation { margin: 10px 0 0; color: #c53d62; font-size: 11px; }
.login-confirmation { display: grid; gap: 10px; padding: 4px 0; }
.login-confirmation__row { display: flex; align-items: center; justify-content: space-between; gap: 16px; min-height: 54px; padding: 0 16px; border: 1px solid rgba(10,37,64,.08); border-radius: 12px; background: #f6f9fc; }
.login-confirmation__row span { color: #697386; font-size: 12px; }
.login-confirmation__row strong { min-width: 0; overflow: hidden; color: #0a2540; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.login-confirmation__note { margin: 2px 0 0; color: #697386; font-size: 11px; line-height: 1.6; }
```

Do not introduce green; keep all active states in the approved purple-pink-orange palette.

- [ ] **Step 2: Adjust the dialog container without changing the page behind it**

Update only login dialog selectors:

- Set the panel width to `width: min(560px, 100%)`.
- Keep the existing white translucent panel, blur, shadow, close button, orb rings, heading, and backdrop.
- Remove the old full-width `.login-dialog__submit` layout because the Stepper final button owns submission styling.
- Keep the existing `@keyframes submit-shine` so the raster sweep remains unchanged.
- Keep `.login-field` and password visibility styles, adding consistent heights for both steps.
- Keep `.login-dialog` at its existing `z-index: 100`; do not alter the global `AppDialog` layer at `z-index: 10000` or its scoped styles.

- [ ] **Step 3: Add responsive and reduced-motion rules**

Add these exact rules:

```css
@media (max-width: 520px) {
  .login-dialog__panel { padding: 30px 22px 24px; }
  .login-stepper__track { padding-inline: 2px; }
  .login-stepper__connector { margin-inline: 6px; }
  .login-stepper__footer { gap: 10px; }
  .login-stepper__next { min-width: 116px; }
}

@media (prefers-reduced-motion: reduce) {
  .login-stepper__next i { animation-duration: .01ms!important; animation-iteration-count: 1!important; }
}
```

The existing global reduced-motion rule continues to collapse all login transition durations.

- [ ] **Step 4: Run focused tests after styling**

Run:

```powershell
npx vitest run src/components/login/__tests__/Stepper.test.js src/components/login/__tests__/loginExperience.test.js
```

Expected: all focused tests PASS.

- [ ] **Step 5: Commit the visual integration**

```powershell
git add frontend/src/styles/login-experience.css
git commit -m "style: match login stepper to portal theme"
```

### Task 5: Full frontend verification

**Files:**
- Verify only; no backend files may change.

- [ ] **Step 1: Run the complete frontend test suite**

Run:

```powershell
npm test -- --run
```

Working directory: `frontend`

Expected: all Vitest suites PASS.

- [ ] **Step 2: Build the production frontend**

Run:

```powershell
npm run build
```

Expected: Vite exits with code 0 and writes `frontend/dist`.

- [ ] **Step 3: Confirm the backend is untouched**

Run from the repository root:

```powershell
git status --short
git diff --name-only b94c0d4..HEAD -- backend
```

Expected: the backend diff command prints no files, proving the login work made no backend changes relative to the updated baseline. Any generated `frontend/dist` output must remain uncommitted if ignored by the repository.

- [ ] **Step 4: Record the user-owned visual QA boundary**

Do not run browser screenshot comparison or claim pixel-level visual approval. Report that unit tests and the production build passed, and that final visual inspection remains with the user as explicitly requested.
