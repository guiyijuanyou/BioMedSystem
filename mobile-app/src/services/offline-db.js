const DB_NAME = "biomed_collection";
const DB_PATH = "_doc/biomed_collection.db";
const STORAGE_KEY = "biomed-offline-records";

let sqliteReady = false;

export async function initOfflineStore() {
  if (canUseSqlite()) {
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
    sqliteReady = true;
    return;
  }
  const existing = uni.getStorageSync(STORAGE_KEY);
  if (!Array.isArray(existing)) {
    uni.setStorageSync(STORAGE_KEY, []);
  }
}

export async function insertOfflineRecord(record) {
  await initOfflineStore();
  const now = new Date().toISOString();
  const item = {
    clientRecordId: record.clientRecordId,
    payload: record,
    syncStatus: "pending",
    detail: "",
    createdAt: now,
    updatedAt: now
  };

  if (sqliteReady) {
    await executeSqlite(
      "INSERT OR REPLACE INTO offline_growth_record(client_record_id,payload,sync_status,detail,created_at,updated_at) VALUES(?,?,?,?,?,?)",
      [item.clientRecordId, JSON.stringify(item.payload), item.syncStatus, item.detail, item.createdAt, item.updatedAt]
    );
    return item;
  }

  const rows = await listOfflineRecords();
  const next = [item, ...rows.filter(row => row.clientRecordId !== item.clientRecordId)];
  uni.setStorageSync(STORAGE_KEY, next);
  return item;
}

export async function listOfflineRecords() {
  await initOfflineStore();
  if (sqliteReady) {
    const rows = await selectSqlite("SELECT * FROM offline_growth_record ORDER BY created_at DESC");
    return rows.map(row => ({
      clientRecordId: row.client_record_id,
      payload: JSON.parse(row.payload),
      syncStatus: row.sync_status,
      detail: row.detail || "",
      createdAt: row.created_at,
      updatedAt: row.updated_at
    }));
  }
  return uni.getStorageSync(STORAGE_KEY) || [];
}

export async function updateRecordStatus(clientRecordId, syncStatus, detail = "") {
  await initOfflineStore();
  const now = new Date().toISOString();
  if (sqliteReady) {
    await executeSqlite(
      "UPDATE offline_growth_record SET sync_status=?, detail=?, updated_at=? WHERE client_record_id=?",
      [syncStatus, detail, now, clientRecordId]
    );
    return;
  }
  const rows = await listOfflineRecords();
  uni.setStorageSync(STORAGE_KEY, rows.map(row => (
    row.clientRecordId === clientRecordId ? { ...row, syncStatus, detail, updatedAt: now } : row
  )));
}

export async function clearAcceptedRecords() {
  await initOfflineStore();
  if (sqliteReady) {
    await executeSqlite("DELETE FROM offline_growth_record WHERE sync_status IN ('accepted','duplicate')");
    return;
  }
  const rows = await listOfflineRecords();
  uni.setStorageSync(STORAGE_KEY, rows.filter(row => !["accepted", "duplicate"].includes(row.syncStatus)));
}

export async function queueSummary() {
  const rows = await listOfflineRecords();
  return rows.reduce((summary, row) => {
    summary.total += 1;
    summary[row.syncStatus] = (summary[row.syncStatus] || 0) + 1;
    return summary;
  }, { total: 0, pending: 0, accepted: 0, rejected: 0, duplicate: 0, processing: 0 });
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
