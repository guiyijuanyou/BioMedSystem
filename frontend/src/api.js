export async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  const text = await response.text();
  let data = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { error: text || `请求失败（${response.status}）` };
  }
  if (!response.ok) throw new Error(data.error || `请求失败（${response.status}）`);
  return data;
}
