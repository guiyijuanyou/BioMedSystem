export async function api(path, options = {}) {
  const session = JSON.parse(localStorage.getItem("biomed-session") || "null");
  const headers = {
    "Content-Type": "application/json",
    ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
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
      localStorage.removeItem("biomed-session");
    }
    throw new Error(data.error || `Request failed (${response.status})`);
  }
  return data;
}
