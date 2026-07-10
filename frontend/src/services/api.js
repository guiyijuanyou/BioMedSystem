export async function api(path, options = {}) {
  const session = JSON.parse(sessionStorage.getItem("biomed-session") || "null");
  const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;
  const headers = {
    ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
    ...(isFormData ? {} : { "Content-Type": "application/json" }),
    ...(options.headers || {})
  };
  const response = await fetch(path, {
    ...options,
    headers
  });
  const text = await response.text();
  let data = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { error: text || `Request failed (${response.status})` };
  }
  if (!response.ok) {
    if (data.error === "login required") {
      sessionStorage.removeItem("biomed-session");
      window.dispatchEvent(new CustomEvent("biomed-login-required"));
    }
    throw new Error(data.error || `Request failed (${response.status})`);
  }
  return data;
}
