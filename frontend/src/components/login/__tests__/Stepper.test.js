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
