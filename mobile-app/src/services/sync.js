import { uploadGrowthRecords } from "./api";
import { listOfflineRecords, updateRecordStatus } from "./offline-db";

export async function syncPendingRecords() {
  const rows = await listOfflineRecords();
  const candidates = rows.filter(row => ["pending", "rejected", "processing"].includes(row.syncStatus));
  if (!candidates.length) {
    return { accepted: 0, duplicates: 0, rejected: 0, processing: 0, items: [] };
  }

  const records = candidates.slice(0, 200).map(row => row.payload);
  const response = await uploadGrowthRecords(records);
  const items = response.items || [];

  for (const item of items) {
    await updateRecordStatus(item.clientRecordId, item.status, item.detail);
  }

  return response;
}

export function newClientRecordId(deviceCode = "APP") {
  const random = Math.random().toString(16).slice(2);
  const timestamp = Date.now().toString(36);
  return `${deviceCode || "APP"}-${timestamp}-${random}`;
}
