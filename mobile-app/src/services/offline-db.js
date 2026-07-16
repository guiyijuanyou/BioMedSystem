const DB_NAME = "biomed_collection";
const DB_PATH = "_doc/biomed_collection.db";
const STORAGE_KEY = "biomed-offline-records";

let storeType = "init"; // "init" | "sqlite" | "storage"

export async function initOfflineStore() {
  return ensureStore();
}

async function ensureStore() {
  if (storeType !== "init") return;

  if (canUseSqlite()) {
    try {
      await openSqlite();
      await executeSqlite(`
        CREATE TABLE IF NOT EXISTS offline_growth_record (
          client_record_id TEXT PRIMARY KEY,
          payload TEXT NOT NULL,
          sync_status TEXT NOT NULL,
          detail TEXT,
          created_at TEXT NOT NULL,
          updated_at TEXT NOT NULL
        )
      `);
      storeType = "sqlite";
      return;
    } catch (e) {
      console.warn("SQLite init failed:", e.message);
    }
  }
  // Fallback: ensure Storage array exists
  storeType = "storage";
  const existing = uni.getStorageSync(STORAGE_KEY);
  if (!Array.isArray(existing)) {
    uni.setStorageSync(STORAGE_KEY, []);
  }
}

export async function insertOfflineRecord(record) {
  if (!record || !record.clientRecordId) {
    throw new Error("记录数据无效，无法保存");
  }

  const payloadStr = JSON.stringify(record);
  if (typeof payloadStr !== "string") {
    throw new Error("记录序列化失败，无法保存");
  }

  await ensureStore();

  const now = new Date().toISOString();
  const item = {
    clientRecordId: record.clientRecordId,
    payload: record,
    syncStatus: "pending",
    detail: "",
    createdAt: now,
    updatedAt: now
  };

  if (storeType === "sqlite") {
    try {
      await executeSqlite(
        "INSERT OR REPLACE INTO offline_growth_record(client_record_id,payload,sync_status,detail,created_at,updated_at) VALUES(?,?,?,?,?,?)",
        [item.clientRecordId, payloadStr, item.syncStatus, item.detail, item.createdAt, item.updatedAt]
      );
      return item;
    } catch (e) {
      console.warn("SQLite insert failed, switching to Storage:", e.message);
      storeType = "storage";
      // Migrate existing SQLite records to Storage so no data is lost
      try {
        const existing = await selectSqlite("SELECT * FROM offline_growth_record");
        const migrated = existing.map(row => ({
          clientRecordId: row.client_record_id,
          payload: JSON.parse(row.payload),
          syncStatus: row.sync_status,
          detail: row.detail || "",
          createdAt: row.created_at,
          updatedAt: row.updated_at
        }));
        uni.setStorageSync(STORAGE_KEY, migrated);
      } catch (_) {}
    }
  }

  if (storeType === "storage") {
    const rows = listFromStorage();
    const next = [item, ...rows.filter(row => row.clientRecordId !== item.clientRecordId)];
    uni.setStorageSync(STORAGE_KEY, next);
    return item;
  }

  throw new Error("存储未初始化");
}

export async function listOfflineRecords() {
  await ensureStore();
  if (storeType === "sqlite") {
    try {
      const rows = await selectSqlite("SELECT * FROM offline_growth_record ORDER BY created_at DESC");
      return rows.map(row => ({
        clientRecordId: row.client_record_id,
        payload: JSON.parse(row.payload),
        syncStatus: row.sync_status,
        detail: row.detail || "",
        createdAt: row.created_at,
        updatedAt: row.updated_at
      }));
    } catch (e) {
      console.warn("SQLite query failed, switching to Storage:", e.message);
      storeType = "storage";
    }
  }
  if (storeType === "storage") {
    return listFromStorage();
  }
  return [];
}

export async function updateRecordStatus(clientRecordId, syncStatus, detail = "") {
  await ensureStore();
  const now = new Date().toISOString();
  if (storeType === "sqlite") {
    try {
      await executeSqlite(
        "UPDATE offline_growth_record SET sync_status=?, detail=?, updated_at=? WHERE client_record_id=?",
        [syncStatus, detail, now, clientRecordId]
      );
      return;
    } catch (e) {
      console.warn("SQLite update failed, switching to Storage:", e.message);
      storeType = "storage";
    }
  }
  if (storeType === "storage") {
    const rows = listFromStorage();
    uni.setStorageSync(STORAGE_KEY, rows.map(row => (
      row.clientRecordId === clientRecordId ? { ...row, syncStatus, detail, updatedAt: now } : row
    )));
  }
}

export async function clearAcceptedRecords() {
  await ensureStore();
  if (storeType === "sqlite") {
    try {
      await executeSqlite("DELETE FROM offline_growth_record WHERE sync_status IN ('accepted','duplicate')");
      return;
    } catch (e) {
      console.warn("SQLite clear failed, switching to Storage:", e.message);
      storeType = "storage";
    }
  }
  if (storeType === "storage") {
    const rows = listFromStorage();
    uni.setStorageSync(STORAGE_KEY, rows.filter(row => !["accepted", "duplicate"].includes(row.syncStatus)));
  }
}

function listFromStorage() {
  return uni.getStorageSync(STORAGE_KEY) || [];
}

export async function queueSummary() {
  const rows = await listOfflineRecords();
  return rows.reduce((summary, row) => {
    summary.total += 1;
    summary[row.syncStatus] = (summary[row.syncStatus] || 0) + 1;
    return summary;
  }, { total: 0, pending: 0, accepted: 0, rejected: 0, duplicate: 0, processing: 0 });
}

const CACHE_PREFIX = "biomed-cache-";
const CACHE_TTL = 24 * 60 * 60 * 1000; // 24h

export function setCache(key, data) {
  try {
    uni.setStorageSync(CACHE_PREFIX + key, {
      data,
      expires: Date.now() + CACHE_TTL
    });
  } catch (_) {}
}

export function getCache(key) {
  try {
    const entry = uni.getStorageSync(CACHE_PREFIX + key);
    if (entry && entry.expires > Date.now()) return entry.data;
  } catch (_) {}
  return null;
}

function canUseSqlite() {
  return typeof plus !== "undefined" && plus.sqlite;
}

function openSqlite() {
  return new Promise((resolve, reject) => {
    if (plus.sqlite.isOpenDatabase({ name: DB_NAME, path: DB_PATH })) {
      resolve();
      return;
    }
    plus.sqlite.openDatabase({
      name: DB_NAME,
      path: DB_PATH,
      success: resolve,
      fail: error => reject(new Error(error.message || "SQLite 打开失败"))
    });
  });
}

function executeSqlite(sql, values = []) {
  return new Promise((resolve, reject) => {
    plus.sqlite.executeSql({
      name: DB_NAME,
      sql,
      values,
      success: resolve,
      fail: error => reject(new Error(error.message || "SQLite 执行失败"))
    });
  });
}

function selectSqlite(sql, values = []) {
  return new Promise((resolve, reject) => {
    plus.sqlite.selectSql({
      name: DB_NAME,
      sql,
      values,
      success: resolve,
      fail: error => reject(new Error(error.message || "SQLite 查询失败"))
    });
  });
}
