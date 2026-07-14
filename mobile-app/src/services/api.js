import { loadSettings, resolveApiBase } from "./settings";

function request(path, options = {}) {
  const settings = loadSettings();
  const base = resolveApiBase();
  const url = `${base}${path}`;
  const headers = {
    "Content-Type": "application/json",
    ...(settings.deviceToken ? { "X-Device-Token": settings.deviceToken } : {}),
    ...(options.headers || {})
  };

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: options.method || "GET",
      header: headers,
      data: options.body,
      timeout: options.timeout || 12000,
      success(response) {
        const status = response.statusCode || 0;
        const data = response.data || {};
        if (status >= 200 && status < 300) {
          resolve(data);
          return;
        }
        const message = data.message || data.error || `请求失败（${status}）`;
        const error = new Error(message);
        error.status = status;
        reject(error);
      },
      fail(error) {
      reject(new Error(error.errMsg || "网络连接失败"));
      }
    });
  });
}

function accountRequest(path, options = {}) {
  const settings = loadSettings();
  const base = resolveApiBase();
  const url = `${base}${path}`;
  const headers = {
    "Content-Type": "application/json",
    ...(settings.accountToken ? { Authorization: `Bearer ${settings.accountToken}` } : {}),
    ...(options.headers || {})
  };

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: options.method || "GET",
      header: headers,
      data: options.body,
      timeout: options.timeout || 12000,
      success(response) {
        const status = response.statusCode || 0;
        const data = response.data || {};
        if (status >= 200 && status < 300) {
          resolve(data);
          return;
        }
        const message = data.message || data.error || `请求失败（${status}）`;
        const error = new Error(message);
        error.status = status;
        reject(error);
      },
      fail(error) {
        reject(new Error(error.errMsg || "网络连接失败"));
      }
    });
  });
}

export function login(username, password) {
  return accountRequest("/api/auth/login", {
    method: "POST",
    body: { username, password }
  });
}

export function getSummary() {
  return accountRequest("/api/summary");
}

export function getResource(resourceType) {
  return accountRequest(`/api/${resourceType}`);
}

export function createTraceEvent(payload) {
  return accountRequest("/api/trace-events", {
    method: "POST",
    body: payload
  });
}

export function getBatches() {
  return request("/api/mobile/batches");
}

export function getSyncStatus() {
  return request("/api/mobile/sync/status");
}

export function uploadGrowthRecords(records) {
  return request("/api/mobile/growth-records/batch", {
    method: "POST",
    headers: {
      "X-Request-Id": `app-${Date.now()}-${Math.random().toString(16).slice(2)}`
    },
    body: { records }
  });
}

export function assertConfigured() {
  const settings = loadSettings();
  if (!settings.deviceToken) {
    throw new Error("请先填写设备令牌");
  }
}
