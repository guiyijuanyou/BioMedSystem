import { reactive } from "vue";

const state = reactive({
  open: false,
  type: "confirm",
  tone: "default",
  title: "",
  message: "",
  label: "",
  placeholder: "",
  value: "",
  inputType: "text",
  multiline: false,
  required: false,
  confirmText: "确定",
  cancelText: "取消"
});

let resolver = null;

function show(options) {
  if (resolver) resolver(state.type === "prompt" ? null : false);
  Object.assign(state, {
    open: true,
    type: "confirm",
    tone: "default",
    title: "请确认操作",
    message: "",
    label: "",
    placeholder: "",
    value: "",
    inputType: "text",
    multiline: false,
    required: false,
    confirmText: "确定",
    cancelText: "取消",
    ...options
  });
  return new Promise((resolve) => {
    resolver = resolve;
  });
}

function finish(value) {
  state.open = false;
  const resolve = resolver;
  resolver = null;
  resolve?.(value);
}

export const appDialog = {
  state,
  confirm(options = {}) {
    const normalized = typeof options === "string" ? { message: options } : options;
    return show({ type: "confirm", ...normalized });
  },
  prompt(options = {}) {
    const normalized = typeof options === "string" ? { label: options } : options;
    return show({ type: "prompt", title: "填写信息", ...normalized });
  },
  alert(options = {}) {
    const normalized = typeof options === "string" ? { message: options } : options;
    return show({ type: "alert", title: "提示", cancelText: "", ...normalized });
  },
  accept() {
    finish(state.type === "prompt" ? String(state.value ?? "").trim() : true);
  },
  cancel() {
    if (state.type === "alert") finish(true);
    else finish(state.type === "prompt" ? null : false);
  }
};
