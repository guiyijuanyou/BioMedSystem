<script setup>
import { onMounted, ref } from "vue";
import { getBatches } from "../../services/api";
import { setCache, getCache } from "../../services/offline-db";
import { loadSettings } from "../../services/settings";

const settings = loadSettings();
const batches = ref([]);
const loading = ref(false);
const markers = ref([]);
const online = ref(true);
const centerLat = ref(29.5583);
const centerLng = ref(106.574);
const scale = ref(10);

onMounted(async () => {
  uni.getNetworkType({
    success(r) { online.value = r.networkType !== "none"; }
  });
  uni.onNetworkStatusChange((r) => { online.value = r.isConnected; });
  if (settings.deviceToken) {
    await loadBatchLocations();
  }
});

async function loadBatchLocations() {
  loading.value = true;
  try {
    const result = await getBatches();
    const items = result.items || [];
    batches.value = items;
    setCache("batches", items);

    const mk = items
      .filter(b => b.longitude && b.latitude)
      .map((b, i) => ({
        id: i,
        latitude: Number(b.latitude),
        longitude: Number(b.longitude),
        title: b.batchName || b.herbName || "",
        label: { content: b.herbName || "", color: "#0c6b4f" },
        callout: {
          content: `${b.batchName || ""}\n${b.district || ""}`,
          padding: "6,10",
          borderRadius: "4",
          bgColor: "#ffffff",
          display: "ALWAYS"
        }
      }));

    if (mk.length > 0) {
      markers.value = mk;
      centerLat.value = mk[0].latitude;
      centerLng.value = mk[0].longitude;
    }
  } catch (_) {
    const cached = getCache("batches");
    if (cached) {
      batches.value = cached;
      const mk = cached.filter(b => b.longitude && b.latitude).map((b, i) => ({
        id: i, latitude: Number(b.latitude), longitude: Number(b.longitude),
        title: b.batchName || b.herbName || "",
        label: { content: b.herbName || "", color: "#0c6b4f" },
        callout: { content: `${b.batchName || ""}\n${b.district || ""}`, padding: "6,10", borderRadius: "4", bgColor: "#ffffff", display: "ALWAYS" }
      }));
      if (mk.length > 0) { markers.value = mk; centerLat.value = mk[0].latitude; centerLng.value = mk[0].longitude; }
    }
  } finally {
    loading.value = false;
  }
}

function onMarkerTap(e) {
  const marker = markers.value.find(m => m.id === e.detail.markerId);
  if (marker) {
    uni.showActionSheet({
      itemList: ["查看详情"],
      success() { uni.switchTab({ url: "/pages/resources/index" }); }
    });
  }
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">重庆中药材分布</text>
      <text class="title">种植地图</text>
      <text class="subtitle">在线时查看地图分布，离线时查看批次位置列表。</text>
    </view>

    <view v-if="online" class="card map-card">
      <map
        :latitude="centerLat"
        :longitude="centerLng"
        :markers="markers"
        :scale="scale"
        style="width:100%;height:420px"
        :show-location="true"
        @markertap="onMarkerTap"
      />
      <view class="button-row">
        <button class="primary" :loading="loading" @tap="loadBatchLocations">刷新</button>
      </view>
    </view>

    <view v-else class="card">
      <view class="offline-banner">
        <text class="offline-icon">📡</text>
        <text class="offline-text">当前无网络，地图暂不可用</text>
        <text class="offline-hint">连接网络后自动恢复地图显示</text>
      </view>
    </view>

    <view class="card">
      <text class="section-title">批次位置（{{ batches.length }}）</text>
      <view v-for="b in batches" :key="b.id" class="list-item">
        <view class="list-title">{{ b.batchName }}｜{{ b.herbName }}</view>
        <text class="list-meta">
          {{ b.batchCode }} ·
          <text v-if="b.longitude && b.latitude">
            {{ Number(b.latitude).toFixed(4) }}, {{ Number(b.longitude).toFixed(4) }}
          </text>
          <text v-else>未定位</text>
          · {{ b.district || "未填地区" }}
        </text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.map-card { padding: 0; overflow: hidden; }
.map-card .button-row { padding: 12px 16px; margin: 0; }
.list-item { padding: 10px 0; border-bottom: 1px solid #eef2ef; }
.list-item:last-child { border-bottom: none; }
.list-title { font-size: 14px; font-weight: 500; color: #1a2e22; }
.offline-banner { padding: 32px 0; text-align: center; }
.offline-icon { font-size: 32px; display: block; margin-bottom: 8px; }
.offline-text { font-size: 14px; color: #6f8077; display: block; }
.offline-hint { font-size: 12px; color: #9aa8a0; margin-top: 4px; display: block; }
</style>
