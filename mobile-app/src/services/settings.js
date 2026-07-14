const KEY = "biomed-mobile-settings";

const defaults = {
  apiBase: "http://120.26.46.218:8088",
  deviceToken: "",
  deviceCode: "",
  accountToken: "",
  accountName: "",
  accountRole: ""
};

export function loadSettings() {
  const data = uni.getStorageSync(KEY);
  return { ...defaults, ...(data || {}) };
}

export function saveSettings(settings) {
  const next = {
    ...loadSettings(),
    ...settings
  };
  uni.setStorageSync(KEY, next);
  return next;
}

export function hasDeviceToken() {
  return Boolean(loadSettings().deviceToken);
}

export function resolveApiBase() {
  const value = loadSettings().apiBase.trim();
  return value.replace(/\/$/, "");
}
