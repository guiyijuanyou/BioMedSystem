import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import LoginNavigation from "@/components/login/LoginNavigation.vue";
import LoginModal from "@/components/login/LoginModal.vue";
import { sceneStages, resolveSceneState } from "@/composables/useLoginScene";
import HeroSection from "@/components/login/sections/HeroSection.vue";
import CollectionSection from "@/components/login/sections/CollectionSection.vue";
import DistributionSection from "@/components/login/sections/DistributionSection.vue";
import TraceSection from "@/components/login/sections/TraceSection.vue";
import RolesSection from "@/components/login/sections/RolesSection.vue";
import { modules } from "@/config";
import {
  createSectionSnapPoints,
  directionalSnapPoint,
  nearestSnapPoint,
  projectedSnapPoint,
} from "@/composables/loginScrollSnap";

const loginStyles = readFileSync(resolve(process.cwd(), "src/styles/login-experience.css"), "utf8");

const sections = [
  { id: "cloud", label: "数据云" },
  { id: "collection", label: "实时采集" },
  { id: "distribution", label: "地理分布" },
  { id: "trace", label: "图谱溯源" },
  { id: "roles", label: "协同角色" }
];

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

describe("immersive login components", () => {
  it("keeps the password input free of nested hover and focus rings", () => {
    const focusRule = loginStyles.match(
      /\.login-step \.login-field__password input:hover,\s*\.login-step \.login-field__password input:focus,\s*\.login-step \.login-field__password input:focus-visible\s*\{([^}]*)\}/
    )?.[1] || "";

    expect(focusRule).toMatch(/outline:\s*none/);
    expect(focusRule).toMatch(/box-shadow:\s*none/);
  });

  it("resolves finite scene states across five narrative stages", () => {
    expect(sceneStages.map(stage => stage.id)).toEqual(["cloud", "collection", "distribution", "trace", "roles"]);
    for (const progress of [0, 0.2, 0.5, 0.8, 1]) {
      const state = resolveSceneState(progress);
      expect(Object.values(state).every(Number.isFinite)).toBe(true);
    }
  });

  it("renders the accepted five-section biomedical narrative", () => {
    const components = [HeroSection, CollectionSection, DistributionSection, TraceSection, RolesSection];
    const text = components.map(component => mount(component).text()).join(" ");
    expect(text).toContain("让每一份生物医药数据");
    expect(text).toContain("土壤 pH");
    expect(text).toContain("重庆药材分布网络");
    expect(text).toContain("种植");
    expect(text).toContain("加工");
    expect(text).toContain("科研人员");
    expect(text).toContain("进入生物医药数据空间");
  });

  it("keeps login detail classes isolated from dashboard record details", () => {
    const wrapper = mount(DistributionSection);
    expect(wrapper.find(".login-map-detail").exists()).toBe(true);
    expect(wrapper.find(".map-detail").exists()).toBe(false);
  });

  it("provides the batch-linked growth schema required by ResourceView", () => {
    expect(modules["growth-records"].fields[0]).toEqual(["batchId", "药材批次"]);
    expect(modules["growth-records"].fields).toContainEqual(["herbName", "药材名称"]);
  });

  it("calculates snap points from real section offsets instead of equal pages", () => {
    const points = createSectionSnapPoints([0, 920, 2110, 3350, 4720], 4800, 24);
    expect(points).toEqual([0, 0.1867, 0.4346, 0.6929, 0.9783]);
    expect(nearestSnapPoint(0.59, points)).toBe(0.6929);
    expect(nearestSnapPoint(0.5, points)).toBe(0.4346);
    expect(projectedSnapPoint(1500, 18, 4800, points)).toBe(0.4346);
    expect(projectedSnapPoint(1500, -18, 4800, points)).toBe(0.1867);
    expect(projectedSnapPoint(200, 500, 4800, points)).toBe(0);
    expect(projectedSnapPoint(600, 500, 4800, points)).toBe(0.1867);
  });

  it("moves exactly one section in the wheel direction", () => {
    const points = [0, 0.2, 0.45, 0.7, 1];

    expect(directionalSnapPoint(0, 1, 1000, points)).toBe(0.2);
    expect(directionalSnapPoint(200, 1, 1000, points)).toBe(0.45);
    expect(directionalSnapPoint(450, -1, 1000, points)).toBe(0.2);
    expect(directionalSnapPoint(0, -1, 1000, points)).toBe(0);
    expect(directionalSnapPoint(1000, 1, 1000, points)).toBe(1);
  });

  it("renders five sections and emits navigation and login events", async () => {
    const wrapper = mount(LoginNavigation, {
      props: { sections, activeSection: "cloud" }
    });

    for (const section of sections) {
      expect(wrapper.text()).toContain(section.label);
    }

    await wrapper.get('[data-test="nav-collection"]').trigger("click");
    await wrapper.get('[data-test="nav-login"]').trigger("click");

    expect(wrapper.emitted("navigate")?.[0]).toEqual(["collection"]);
    expect(wrapper.emitted("open-login")).toHaveLength(1);
  });

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

  it("renders the compact stepper without the legacy portal header", () => {
    const wrapper = mountLogin({ username: "admin", password: "123456" });

    expect(wrapper.text()).not.toContain("SECURE DATA PORTAL");
    expect(wrapper.find(".login-dialog__orb").exists()).toBe(false);
    expect(wrapper.find(".login-dialog__close").exists()).toBe(false);
  });

  it("omits demo accounts and resets to the first step when reopened", async () => {
    const wrapper = mountLogin({ username: "admin", password: "123456" });

    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    expect(wrapper.find('[data-test="demo-toggle"]').exists()).toBe(false);
    expect(wrapper.text()).not.toContain("统一密码");

    await wrapper.setProps({ modelValue: false });
    await wrapper.setProps({ modelValue: true });

    expect(wrapper.get('[data-test="step-indicator-1"]').attributes("data-status")).toBe("active");
  });

  it("toggles password visibility on the password step", async () => {
    const wrapper = mountLogin({ username: "admin", password: "123456" });

    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    expect(wrapper.get('[data-test="password-input"]').attributes("type")).toBe("password");

    await wrapper.get('[data-test="password-visibility"]').trigger("click");

    expect(wrapper.get('[data-test="password-input"]').attributes("type")).toBe("text");
  });

  it("shows the backend error on the confirmation step", async () => {
    const wrapper = mountLogin(
      { username: "admin", password: "123456" },
      { errorText: "账号或密码错误" }
    );

    await wrapper.get('[data-test="stepper-next"]').trigger("click");
    await wrapper.get('[data-test="stepper-next"]').trigger("click");

    expect(wrapper.text()).toContain("账号或密码错误");
  });
});
