<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { getBatches } from "../../services/api";
import { getCurrentLocation } from "../../services/location";
import { insertOfflineRecord, queueSummary } from "../../services/offline-db";
import { loadSettings } from "../../services/settings";
import { newClientRecordId, syncPendingRecords } from "../../services/sync";

const settings = loadSettings();
const batches = ref([]);
const batchIndex = ref(0);
const queue = ref({ pending: 0, total: 0 });
const syncing = ref(false);

const form = reactive({
  temperature: "",
  humidity: "",
  soilPh: "",
  growthStage: "",
  longitude: "",
  latitude: "",
  locationAccuracy: "",
  remark: ""
});

const selectedBatch = computed(() => batches.value[batchIndex.value] || null);

onMounted(async () => {
  await refreshQueue();
  await loadBatches();
});

async function loadBatches() {
  if (!settings.deviceToken) {
    uni.showToast({ title: "请先配置设备令牌", icon: "none" });
    return;
  }
  try {
    const result = await getBatches();
    batches.value = result.items || [];
  } catch (error) {
    uni.showToast({ title: error.message, icon: "none" });
  }
}

function changeBatch(event) {
  batchIndex.value = Number(event.detail.value);
}

async function locate() {
  try {
    const location = await getCurrentLocation();
    Object.assign(form, location);
    uni.showToast({ title: "定位成功", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message, icon: "none" });
  }
}

async function saveRecord() {
  if (!selectedBatch.value) {
    uni.showToast({ title: "请选择批次", icon: "none" });
    return;
  }
  const record = {
    clientRecordId: newClientRecordId(settings.deviceCode),
    batchId: selectedBatch.value.id,
    recordedAt: localDateTime(),
    temperature: numberOrNull(form.temperature),
    humidity: numberOrNull(form.humidity),
    soilPh: numberOrNull(form.soilPh),
    growthStage: form.growthStage.trim(),
    longitude: numberOrNull(form.longitude),
    latitude: numberOrNull(form.latitude),
    locationAccuracy: numberOrNull(form.locationAccuracy),
    remark: form.remark.trim()
  };
  await insertOfflineRecord(record);
  resetForm();
  await refreshQueue();
  uni.showToast({ title: "已保存到队列", icon: "success" });
}

async function syncNow() {
  if (!settings.deviceToken) {
    uni.showToast({ title: "请先配置设备令牌", icon: "none" });
    return;
  }
  syncing.value = true;
  try {
    const result = await syncPendingRecords();
    await refreshQueue();
    uni.showToast({ title: `成功 ${result.accepted || 0} 条`, icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message, icon: "none" });
  } finally {
    syncing.value = false;
  }
}

async function refreshQueue() {
  queue.value = await queueSummary();
}

function resetForm() {
  Object.assign(form, {
    temperature: "",
    humidity: "",
    soilPh: "",
    growthStage: "",
    longitude: "",
    latitude: "",
    locationAccuracy: "",
    remark: ""
  });
}

function localDateTime() {
  const date = new Date();
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 19);
}

function numberOrNull(value) {
  if (value === "" || value === null || value === undefined) return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">离线优先</text>
      <text class="title">生长数据采集</text>
      <text class="subtitle">先保存到本机队列，有网时再自动或手动补传到后端。</text>
    </view>

    <view class="card">
      <text class="section-title">采集批次</text>
      <picker mode="selector" :range="batches.map(item => `${item.batchName}｜${item.herbName}`)" :value="batchIndex" @change="changeBatch">
        <view class="select">{{ selectedBatch ? `${selectedBatch.batchName}｜${selectedBatch.herbName}` : "请选择批次" }}</view>
      </picker>
      <text v-if="selectedBatch" class="list-meta">
        {{ selectedBatch.batchCode }} · {{ selectedBatch.district || "未填写地区" }} · {{ selectedBatch.currentStage || "未填写阶段" }}
      </text>
      <view class="button-row">
        <button class="secondary" @tap="loadBatches">刷新批次</button>
        <button class="ghost" @tap="uni.switchTab({ url: '/pages/activation/index' })">设备配置</button>
      </view>
    </view>

    <view class="card">
      <text class="section-title">环境指标</text>
      <view class="row">
        <view class="field">
          <text class="label">温度</text>
          <input v-model="form.temperature" class="input" type="digit" placeholder="℃" />
        </view>
        <view class="field">
          <text class="label">湿度</text>
          <input v-model="form.humidity" class="input" type="digit" placeholder="%" />
        </view>
      </view>
      <view class="row">
        <view class="field">
          <text class="label">土壤 pH</text>
          <input v-model="form.soilPh" class="input" type="digit" placeholder="6.5" />
        </view>
        <view class="field">
          <text class="label">生长阶段</text>
          <input v-model="form.growthStage" class="input" placeholder="展叶期" />
        </view>
      </view>
      <view class="field">
        <text class="label">备注</text>
        <textarea v-model="form.remark" class="textarea" placeholder="可填写异常情况、照片编号或现场说明" />
      </view>
    </view>

    <view class="card">
      <text class="section-title">GPS 定位</text>
      <view class="row">
        <view class="field">
          <text class="label">经度</text>
          <input v-model="form.longitude" class="input" type="digit" />
        </view>
        <view class="field">
          <text class="label">纬度</text>
          <input v-model="form.latitude" class="input" type="digit" />
        </view>
      </view>
      <view class="field">
        <text class="label">定位精度（米）</text>
        <input v-model="form.locationAccuracy" class="input" type="digit" />
      </view>
      <button class="secondary" @tap="locate">获取当前位置</button>
    </view>

    <view class="card">
      <text class="section-title">队列</text>
      <view class="metric-grid">
        <view class="metric">
          <text class="metric-value">{{ queue.pending || 0 }}</text>
          <text class="metric-label">待上传</text>
        </view>
        <view class="metric">
          <text class="metric-value">{{ queue.accepted || 0 }}</text>
          <text class="metric-label">已成功</text>
        </view>
        <view class="metric">
          <text class="metric-value">{{ queue.rejected || 0 }}</text>
          <text class="metric-label">需处理</text>
        </view>
      </view>
      <view class="button-row">
        <button class="primary" @tap="saveRecord">保存记录</button>
        <button class="secondary" :loading="syncing" @tap="syncNow">立即同步</button>
      </view>
    </view>
  </view>
</template>
