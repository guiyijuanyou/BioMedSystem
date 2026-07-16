<script setup>
import { onMounted, reactive, ref } from "vue";
import { getSummary, getSyncStatus } from "../../services/api";
import { queueSummary, setCache, getCache } from "../../services/offline-db";
import { loadSettings } from "../../services/settings";

const settings = reactive(loadSettings());
const summary = ref({});
const syncStatus = ref({});
const queue = ref({ total: 0, pending: 0, accepted: 0, rejected: 0 });
const loading = ref(false);

const cards = [
  ["药材批次", "herbBatchCount"],
  ["生长记录", "growthRecordCount"],
  ["溯源事件", "traceEventCount"],
  ["课程资料", "teachingResourceCount"],
  ["检测样本", "labSampleCount"],
  ["课题研究", "projectCount"]
];

onMounted(refresh);
uni.$on("app-visible", refresh);

async function refresh() {
  loading.value = true;
  try {
    queue.value = await queueSummary();
    if (settings.accountToken) {
      try {
        summary.value = await getSummary();
        setCache("summary", summary.value);
      } catch (_) {
        summary.value = getCache("summary") || {};
      }
    }
    if (settings.deviceToken) {
      try {
        syncStatus.value = await getSyncStatus();
        setCache("syncStatus", syncStatus.value);
      } catch (_) {
        syncStatus.value = getCache("syncStatus") || {};
      }
    }
  } catch (error) {
    uni.showToast({ title: error.message, icon: "none" });
  } finally {
    loading.value = false;
  }
}

function openActivation() {
  uni.switchTab({ url: "/pages/activation/index" });
}

function openMap() {
  uni.navigateTo({ url: "/pages/map/index" });
}

function openTrace() {
  uni.navigateTo({ url: "/pages/trace/index" });
}

function openResources() {
  uni.switchTab({ url: "/pages/resources/index" });
}

function openQueue() {
  uni.switchTab({ url: "/pages/queue/index" });
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">生物医药数字信息系统</text>
      <text class="title">移动工作台</text>
      <text class="subtitle">采集、离线补传、批次查看、课程资料和溯源信息集中在手机端。</text>
    </view>

    <view v-if="!settings.accountToken || !settings.deviceToken" class="card">
      <text class="section-title">待完成配置</text>
      <text class="muted">
        {{ !settings.accountToken ? "请登录账号以查看业务数据。" : "" }}
        {{ !settings.deviceToken ? "请填写设备令牌以使用采集同步。" : "" }}
      </text>
      <button class="secondary" @tap="openActivation">去配置</button>
    </view>

    <view class="card">
      <text class="section-title">今日状态</text>
      <view class="metric-grid">
        <view class="metric">
          <text class="metric-value">{{ queue.pending || 0 }}</text>
          <text class="metric-label">待同步</text>
        </view>
        <view class="metric">
          <text class="metric-value">{{ syncStatus.accepted || 0 }}</text>
          <text class="metric-label">已接收</text>
        </view>
        <view class="metric">
          <text class="metric-value">{{ syncStatus.rejected || 0 }}</text>
          <text class="metric-label">被拒绝</text>
        </view>
      </view>
    </view>

    <view class="card">
      <text class="section-title">业务总览</text>
      <view class="metric-grid">
        <view v-for="card in cards" :key="card[1]" class="metric">
          <text class="metric-value">{{ summary[card[1]] ?? "-" }}</text>
          <text class="metric-label">{{ card[0] }}</text>
        </view>
      </view>
      <view class="button-row">
        <button class="primary" :loading="loading" @tap="refresh">刷新</button>
        <button class="secondary" @tap="openResources">查看数据</button>
      </view>
      <view class="button-row">
        <button class="ghost" @tap="openMap">种植地图</button>
        <button class="ghost" @tap="openTrace">溯源事件</button>
        <button class="ghost" @tap="openQueue">离线队列</button>
      </view>
    </view>
  </view>
</template>
