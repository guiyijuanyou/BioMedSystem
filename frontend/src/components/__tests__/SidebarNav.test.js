import { flushPromises, mount } from "@vue/test-utils";
import { createMemoryHistory, createRouter } from "vue-router";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import SidebarNav from "@/components/SidebarNav.vue";

vi.mock("gsap", () => ({
  default: {
    fromTo: (_element, _from, options) => options.onComplete?.(),
    to: (_element, options) => options.onComplete?.()
  }
}));

const routes = [
  { path: "/dashboard", name: "dashboard", component: { template: "<div />" } },
  { path: "/files", name: "files", component: { template: "<div />" } },
  { path: "/improvement", name: "improvement", component: { template: "<div />" } },
  { path: "/module/:moduleKey", name: "module", component: { template: "<div />" } }
];

async function mountNavigation(items, path = "/dashboard") {
  const router = createRouter({ history: createMemoryHistory(), routes });
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
    expect(wrapper.find(".sidebar").exists()).toBe(true);
  });

  it("keeps the floating submenu open until the close delay elapses", async () => {
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
    await wrapper.vm.$nextTick();
    expect(wrapper.find(".desktop-nav-dropdown").exists()).toBe(true);

    vi.advanceTimersByTime(1);
    await wrapper.vm.$nextTick();
    expect(wrapper.find(".desktop-nav-dropdown").exists()).toBe(false);
  });

  it.each([
    ["dashboard", "/dashboard", ""],
    ["files", "/files", "成果管理"],
    ["improvement", "/improvement", "评价改进"],
    ["herbs", "/module/herbs", "药材资源"]
  ])("keeps the existing %s route destination", async (key, expectedPath, groupLabel) => {
    const { router, wrapper } = await mountNavigation([
      ["dashboard", "工作台"],
      ["herbs", "中药材分布"],
      ["improvement", "专业改进闭环"],
      ["files", "资料文件"]
    ]);

    if (groupLabel) {
      const group = wrapper.findAll(".desktop-nav-group").find(item => item.text().includes(groupLabel));
      await group.trigger("mouseenter");
    }

    await wrapper.get(`[data-nav-key="${key}"]`).trigger("click");
    await flushPromises();
    expect(router.currentRoute.value.fullPath).toBe(expectedPath);
  });
});
