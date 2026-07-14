<script setup>
import { onMounted, ref } from "vue";
import { clearAcceptedRecords, listOfflineRecords, queueSummary } from "../../services/offline-db";
import { syncPendingRecords } from "../../services/sync";

const rows = ref([]);
const summary = ref({ total: 0, pending: 0, accepted: 0, rejected: 0, duplicate: 0, processing: 0 });
const syncing = ref(false);

onMounted(refresh);
uni.$on("app-visible", refresh);

async function refresh() {
  rows.value = await listOfflineRecords();
  summary.value = await queueSummary();
}

async function syncNow() {
  syncing.value = true;
  try {
    const result = await syncPendingRecords();
    await refresh();
    uni.showToast({ title: `同步完成 ${result.accepted || 0} 条`, icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message, icon: "none" });
  } finally {
    syncing.value = false;
  }
}

async function clearDone() {
  await clearAcceptedRecords();
  await refresh();
  uni.showToast({ title: "已清理成功记录", icon: "none" });
}

function statusLabel(status) {
  return {
    pending: "待同步",
    accepted: "已成功",
    duplicate: "已去重",
    rejected: "失败",
    processing: "处理中"
  }[status] || status;
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">同步中心</text>
      <text class="title">离线队列</text>
      <text class="subtitle">网络不稳定时先存本机，恢复连接后继续补传。</text>
    </view>

    <view class="card">
      <text class="section-title">队列统计</text>
      <view class="metric-grid">
        <view class="metric">
          <text class="metric-value">{{ summary.pending }}</text>
          <text class="metric-label">待同步</text>
        </view>
        <view class="metric">
          <text class="metric-value">{{ summary.accepted + summary.duplicate }}</text>
          <text class="metric-label">已完成</text>
        </view>
        <view class="metric">
          <text class="metric-value">{{ summary.rejected }}</text>
          <text class="metric-label">失败</text>
        </view>
      </view>
      <view class="button-row">
        <button class="primary" :loading="syncing" @tap="syncNow">同步队列</button>
        <button class="secondary" @tap="refresh">刷新</button>
      </view>
      <button class="ghost" @tap="clearDone">清理成功记录</button>
    </view>

    <view class="card">
      <text class="section-title">本机记录</text>
      <view v-if="!rows.length" class="empty">暂无离线记录</view>
      <view v-for="row in rows" :key="row.clientRecordId" class="list-item">
        <view class="pill">{{ statusLabel(row.syncStatus) }}</view>
        <text class="list-title">{{ row.payload.growthStage || "生长记录" }}</text>
        <text class="list-meta">
          {{ row.clientRecordId }} · {{ row.payload.recordedAt }}
        </text>
        <text v-if="row.detail" class="list-meta">{{ row.detail }}</text>
      </view>
    </view>
  </view>
</template>
