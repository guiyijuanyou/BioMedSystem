# Stripe-Style Desktop Top Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the authenticated desktop sidebar with a Stripe-inspired horizontal navigation and floating submenu while preserving every existing route, permission, API, state, and business interaction.

**Architecture:** Keep `SidebarNav.vue` as the single navigation boundary. Add a desktop-only top navigation to that component while retaining its current mobile drawer markup and shared `navigate()` route mapping; restyle the authenticated shell through an appended, isolated CSS section so `App.vue` business logic remains untouched. Use tests to lock role-filtered menu rendering, hover lifecycle, route destinations, and mobile drawer preservation.

**Tech Stack:** Vue 3, Vite, Three.js, TresJS, GSAP ScrollTrigger, Lenis, Lucide Vue, Vitest, Vue Test Utils, CSS/SVG. This change directly uses Vue 3, Vite, GSAP, Lucide Vue, Vitest, Vue Test Utils, and CSS/SVG. Three.js, TresJS, ScrollTrigger, and Lenis remain available through the existing stack but are not attached to authenticated page scrolling because that would expand the behavioral surface.

---

### Task 1: Lock Desktop Navigation Behavior With Failing Tests

**Files:**
- Create: `frontend/src/components/__tests__/SidebarNav.test.js`
- Test: `frontend/src/components/__tests__/SidebarNav.test.js`

- [ ] **Step 1: Write the failing desktop structure and permission test**

```js
import { mount } from "@vue/test-utils";
import { createMemoryHistory, createRouter } from "vue-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import SidebarNav from "@/components/SidebarNav.vue";

vi.mock("gsap", () => ({
  default: {
    fromTo: (_element, _from, options) => options.onComplete?.(),
    to: (_element, options) => options.onComplete?.()
  }
}));

async function mountNavigation(items, path = "/dashboard") {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: "/dashboard", name: "dashboard", component: { template: "<div />" } },
      { path: "/files", name: "files", component: { template: "<div />" } },
      { path: "/improvement", name: "improvement", component: { template: "<div />" } },
      { path: "/module/:moduleKey", name: "module", component: { template: "<div />" } }
    ]
  });
  await router.push(path);
  await router.isReady();
  return {
    router,
    wrapper: mount(SidebarNav, {
      props: { items, roleLabel: "管理员" },
      global: { plugins: [router] }
    })
  };
}

describe("SidebarNav desktop navigation", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("renders only role-allowed groups and items in the desktop navigation", async () => {
    const { wrapper } = await mountNavigation([
      ["dashboard", "工作台"],
      ["herbs", "中药材分布"],
      ["herb-batches", "药材批次"]
    ]);

    expect(wrapper.find(".desktop-nav-shell").exists()).toBe(true);
    expect(wrapper.findAll(".desktop-nav-group")).toHaveLength(1);
    expect(wrapper.text()).toContain("药材资源");
    expect(wrapper.text()).not.toContain("系统管理");
  });
});
```

- [ ] **Step 2: Run the focused test to verify RED**

Run: `cd frontend && npm test -- --run src/components/__tests__/SidebarNav.test.js`

Expected: FAIL because `.desktop-nav-shell` and `.desktop-nav-group` do not exist.

- [ ] **Step 3: Add failing hover-delay and route-preservation tests**

```js
it("keeps the floating submenu open while hovered and closes it after the delay", async () => {
  const { wrapper } = await mountNavigation([
    ["dashboard", "工作台"],
    ["herbs", "中药材分布"],
    ["herb-batches", "药材批次"]
  ]);
  const group = wrapper.get(".desktop-nav-group");

  await group.trigger("mouseenter");
  expect(wrapper.find(".desktop-nav-dropdown").exists()).toBe(true);
  await group.trigger("mouseleave");
  vi.advanceTimersByTime(159);
  expect(wrapper.find(".desktop-nav-dropdown").exists()).toBe(true);
  vi.advanceTimersByTime(1);
  await wrapper.vm.$nextTick();
  expect(wrapper.find(".desktop-nav-dropdown").exists()).toBe(false);
});

it.each([
  ["dashboard", "/dashboard"],
  ["files", "/files"],
  ["improvement", "/improvement"],
  ["herbs", "/module/herbs"]
])("keeps the existing %s route destination", async (key, expectedPath) => {
  const { router, wrapper } = await mountNavigation([
    ["dashboard", "工作台"],
    ["herbs", "中药材分布"],
    ["improvement", "专业改进闭环"],
    ["files", "资料文件"]
  ]);
  const target = wrapper.get(`[data-nav-key="${key}"]`);
  if (key !== "dashboard") {
    const parent = target.element.closest(".desktop-nav-group");
    await wrapper.findAll(".desktop-nav-group").find(item => item.element === parent).trigger("mouseenter");
  }
  await target.trigger("click");
  await wrapper.vm.$nextTick();
  expect(router.currentRoute.value.fullPath).toBe(expectedPath);
});
```

- [ ] **Step 4: Run the focused test again to verify the new cases fail for the expected missing selectors**

Run: `cd frontend && npm test -- --run src/components/__tests__/SidebarNav.test.js`

Expected: FAIL because the desktop trigger, dropdown, and `data-nav-key` hooks are not implemented.

### Task 2: Implement the Desktop Top Navigation Without Rewriting Route Logic

**Files:**
- Modify: `frontend/src/components/SidebarNav.vue`
- Test: `frontend/src/components/__tests__/SidebarNav.test.js`

- [ ] **Step 1: Add desktop hover state and GSAP transition hooks**

Add `gsap` to the existing imports and add the following state beside `expandedGroup`:

```js
import gsap from "gsap";

const desktopGroup = ref("");
let desktopCloseTimer;

function openDesktopGroup(key) {
  clearTimeout(desktopCloseTimer);
  desktopGroup.value = key;
}

function scheduleDesktopGroupClose() {
  clearTimeout(desktopCloseTimer);
  desktopCloseTimer = setTimeout(() => {
    desktopGroup.value = "";
  }, 160);
}

function closeDesktopGroup() {
  clearTimeout(desktopCloseTimer);
  desktopGroup.value = "";
}

function onDesktopMenuEnter(element, done) {
  gsap.fromTo(element, { autoAlpha: 0, y: -8 }, {
    autoAlpha: 1,
    y: 0,
    duration: 0.22,
    ease: "power2.out",
    onComplete: done
  });
}

function onDesktopMenuLeave(element, done) {
  gsap.to(element, {
    autoAlpha: 0,
    y: -6,
    duration: 0.16,
    ease: "power2.in",
    onComplete: done
  });
}
```

Update only the end of the existing `navigate()` function so navigation also closes the visual overlay:

```js
function navigate(key) {
  if (key === "dashboard") router.push("/dashboard");
  else if (key === "files") router.push("/files");
  else if (key === "improvement") router.push("/improvement");
  else router.push(`/module/${key}`);
  closeDesktopGroup();
  emit("close");
}
```

- [ ] **Step 2: Add the desktop-only header before the existing mobile `<aside>`**

```vue
<header class="desktop-nav-shell">
  <div class="desktop-primary-nav">
    <button class="desktop-brand" type="button" data-nav-key="dashboard" @click="navigate('dashboard')">
      <span class="login-nav__mark" aria-hidden="true"><i></i><i></i><i></i></span>
      <span><strong>BioMed Cloud</strong><small>生物医药数字信息系统</small></span>
    </button>

    <nav class="desktop-nav-links" aria-label="桌面主导航">
      <button class="desktop-nav-home" :class="{ active: activeKey === 'dashboard' }" type="button" data-nav-key="dashboard" @click="navigate('dashboard')">工作台</button>
      <section
        v-for="group in groups"
        :key="group.key"
        class="desktop-nav-group"
        :class="{ active: activeGroup === group.key, open: desktopGroup === group.key }"
        @mouseenter="openDesktopGroup(group.key)"
        @mouseleave="scheduleDesktopGroupClose"
        @focusin="openDesktopGroup(group.key)"
        @focusout="scheduleDesktopGroupClose"
      >
        <button class="desktop-nav-trigger" type="button" :aria-expanded="desktopGroup === group.key">
          <span>{{ group.label }}</span><ChevronDown :size="14" />
        </button>
        <Transition :css="false" @enter="onDesktopMenuEnter" @leave="onDesktopMenuLeave">
          <div v-if="desktopGroup === group.key" class="desktop-nav-dropdown">
            <div class="desktop-nav-dropdown-head"><component :is="group.icon" :size="18" /><strong>{{ group.label }}</strong></div>
            <button
              v-for="[key, label] in group.items"
              :key="key"
              class="desktop-nav-option"
              :class="{ active: activeKey === key }"
              type="button"
              :data-nav-key="key"
              @click="navigate(key)"
            >
              <span class="desktop-nav-option-icon"><component :is="icons[key]" :size="17" /></span>
              <span><strong>{{ label }}</strong><small>进入{{ label }}模块</small></span>
            </button>
          </div>
        </Transition>
      </section>
    </nav>

    <div class="desktop-nav-status"><span>{{ roleLabel }}</span><small><i></i>系统服务正常</small></div>
  </div>
</header>
```

Add `data-nav-key` attributes to the existing mobile dashboard and child buttons so the test and accessible behavior share the same route identifiers.

- [ ] **Step 3: Run the focused navigation tests to verify GREEN**

Run: `cd frontend && npm test -- --run src/components/__tests__/SidebarNav.test.js`

Expected: all `SidebarNav` tests PASS.

- [ ] **Step 4: Commit the navigation behavior**

```powershell
git add frontend/src/components/SidebarNav.vue frontend/src/components/__tests__/SidebarNav.test.js
git commit -m "feat: add role-aware desktop top navigation"
```

### Task 3: Apply the Stripe-Inspired Authenticated Theme

**Files:**
- Create: `frontend/src/components/__tests__/authenticatedTheme.test.js`
- Modify: `frontend/src/styles.css`
- Test: `frontend/src/components/__tests__/authenticatedTheme.test.js`

- [ ] **Step 1: Write a failing static contract test for the isolated theme section**

```js
import { readFileSync } from "node:fs";
import { fileURLToPath, URL } from "node:url";
import { describe, expect, it } from "vitest";

const styles = readFileSync(fileURLToPath(new URL("../../styles.css", import.meta.url)), "utf8");

describe("authenticated Stripe-inspired theme", () => {
  it("defines the desktop top-navigation surface and floating dropdown", () => {
    expect(styles).toContain("/* Stripe-inspired authenticated shell */");
    expect(styles).toMatch(/\.desktop-nav-shell\s*\{/);
    expect(styles).toMatch(/\.desktop-nav-dropdown\s*\{/);
    expect(styles).toMatch(/position:\s*absolute/);
  });

  it("preserves the mobile drawer below the existing breakpoint", () => {
    expect(styles).toMatch(/@media \(max-width:\s*860px\)[\s\S]*\.desktop-nav-shell\s*\{\s*display:\s*none/);
    expect(styles).toMatch(/@media \(max-width:\s*860px\)[\s\S]*\.sidebar\s*\{[\s\S]*display:\s*flex/);
  });
});
```

- [ ] **Step 2: Run the theme contract test to verify RED**

Run: `cd frontend && npm test -- --run src/components/__tests__/authenticatedTheme.test.js`

Expected: FAIL because the isolated authenticated shell section does not exist.

- [ ] **Step 3: Append the isolated authenticated shell styles**

Add a final `/* Stripe-inspired authenticated shell */` section to `styles.css` that:

```css
:root {
  --bg: #f6f9fc;
  --surface: rgba(255, 255, 255, 0.88);
  --surface-subtle: #f6f9fc;
  --ink: #0a2540;
  --muted: #697386;
  --line: rgba(10, 37, 64, 0.11);
  --line-strong: rgba(10, 37, 64, 0.18);
  --brand: #635bff;
  --brand-dark: #3f37d7;
  --brand-soft: #eef1ff;
  --accent: #ff7a59;
  --shadow-sm: 0 8px 24px rgba(50, 50, 93, 0.07);
  --shadow-md: 0 24px 64px rgba(50, 50, 93, 0.13);
}

.shell { display: block; min-height: 100vh; }
.desktop-nav-shell { position: sticky; z-index: 300; top: 0; padding: 18px 3.4vw 0; pointer-events: none; }
.desktop-primary-nav { position: relative; display: grid; grid-template-columns: minmax(210px, .8fr) minmax(0, 2fr) minmax(150px, .7fr); align-items: center; width: min(1400px, 100%); min-height: 68px; margin: 0 auto; padding: 8px 12px 8px 18px; border: 1px solid rgba(10, 37, 64, .09); border-radius: 20px; background: rgba(255,255,255,.82); box-shadow: 0 16px 44px rgba(50,50,93,.09); backdrop-filter: blur(24px) saturate(150%); pointer-events: auto; }
.desktop-nav-dropdown { position: absolute; top: calc(100% + 13px); left: 50%; z-index: 500; width: min(390px, 42vw); padding: 16px; border: 1px solid rgba(10,37,64,.1); border-radius: 20px; background: rgba(255,255,255,.96); box-shadow: 0 28px 70px rgba(50,50,93,.18); backdrop-filter: blur(24px) saturate(145%); transform: translateX(-50%); }
.sidebar { display: none; }
.workspace { width: min(1500px, 100%); max-width: none; margin: 0 auto; padding: 22px clamp(22px, 3.4vw, 52px) 48px; }
.topbar, .panel, .metric, .batch-detail-page { border-radius: 18px; background: rgba(255,255,255,.88); box-shadow: var(--shadow-sm); }

@media (max-width: 860px) {
  .desktop-nav-shell { display: none; }
  .sidebar { display: flex; }
  .workspace { padding: 68px 14px 32px; }
}
```

Complete the section with matching styles for desktop brand mark, active gradient underline, submenu options, role/status block, rounded buttons/inputs/tables, user menu, cards, and `prefers-reduced-motion`. Keep card radii at or below 20px and preserve the existing mobile rules after the breakpoint override.

- [ ] **Step 4: Run the theme contract and navigation tests to verify GREEN**

Run: `cd frontend && npm test -- --run src/components/__tests__/authenticatedTheme.test.js src/components/__tests__/SidebarNav.test.js`

Expected: all focused tests PASS.

- [ ] **Step 5: Commit the authenticated theme**

```powershell
git add frontend/src/styles.css frontend/src/components/__tests__/authenticatedTheme.test.js
git commit -m "style: align authenticated shell with Stripe visual system"
```

### Task 4: Verify Existing Logic and Production Build

**Files:**
- Verify only: `frontend/src/App.vue`
- Verify only: `frontend/src/router/index.js`
- Verify only: `frontend/src/config.js`
- Verify only: `frontend/src/services/api.js`
- Verify only: `frontend/src/composables/useImmersiveScroll.js`

- [ ] **Step 1: Confirm protected logic files were not modified by the implementation**

Run:

```powershell
git diff HEAD~2 -- frontend/src/App.vue frontend/src/router/index.js frontend/src/config.js frontend/src/services/api.js frontend/src/composables/useImmersiveScroll.js
```

Expected: no implementation diff in `App.vue`, router, config, or API service. The pre-existing user change in `useImmersiveScroll.js` remains present and unchanged relative to the implementation baseline.

- [ ] **Step 2: Run the complete frontend test suite**

Run: `cd frontend && npm test -- --run`

Expected: all Vitest and Vue Test Utils tests PASS with no unhandled errors.

- [ ] **Step 3: Run the production build**

Run: `cd frontend && npm run build`

Expected: Vite exits with code 0 and writes `frontend/dist` without compilation errors.

- [ ] **Step 4: Verify route and permission behavior against the running application**

Using the already-running local app at `http://localhost:8088`, verify with browser automation:

1. Log in as `admin / 123456` and confirm all allowed top-level groups render.
2. Open each desktop submenu and click one standard module, `/files`, and `/improvement`.
3. Confirm refresh, automatic backup, personal center, password change, and logout controls still invoke their existing handlers.
4. Log in as a restricted role and confirm disallowed groups/items are absent.

Expected: route destinations, permission filtering, API-backed actions, and session behavior match the pre-change implementation.

- [ ] **Step 5: Record final status without visual sign-off**

Report test and build commands, protected-file diff result, route/permission checks, and any residual logic risk. Per the approved requirement, do not perform pixel-level or final visual acceptance.
