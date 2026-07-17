<script setup>
import { computed, onMounted, ref } from "vue";
import { getResource } from "../../services/api";
import { resourceTabs, summarize, displayValue } from "../../services/resource-meta";
import { setCache, getCache } from "../../services/offline-db";
import { loadSettings } from "../../services/settings";

const activeIndex = ref(0);
const items = ref([]);
const keyword = ref("");
const loading = ref(false);
const settings = loadSettings();

const activeTab = computed(() => resourceTabs[activeIndex.value]);
const filteredItems = computed(() => {
  const word = keyword.value.trim().toLowerCase();
  if (!word) return items.value;
  return items.value.filter(item => JSON.stringify(item).toLowerCase().includes(word));
});

onMounted(load);

async function load() {
  if (!settings.accountToken) {
    uni.showToast({ title: "请先登录账号", icon: "none" });
    return;
  }
  loading.value = true;
  const cacheKey = "resources:" + activeTab.value.key;
  try {
    const result = await getResource(activeTab.value.key);
    items.value = result.items || [];
    setCache(cacheKey, items.value);
  } catch (error) {
    const cached = getCache(cacheKey);
    if (cached) {
      items.value = cached;
    } else {
      uni.showToast({ title: error.message, icon: "none" });
    }
  } finally {
    loading.value = false;
  }
}

function changeTab(event) {
  activeIndex.value = Number(event.detail.value);
  keyword.value = "";
  load();
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">业务数据</text>
      <text class="title">{{ activeTab.label }}</text>
      <text class="subtitle">手机端快速查看批次、生长记录、溯源、课程资料和课题信息。</text>
    </view>

    <view class="card">
      <picker mode="selector" :range="resourceTabs.map(item => item.label)" :value="activeIndex" @change="changeTab">
        <view class="select">{{ activeTab.label }}</view>
      </picker>
      <view class="field">
        <input v-model="keyword" class="input" placeholder="搜索当前列表" />
      </view>
      <view class="button-row">
        <button class="primary" :loading="loading" @tap="load">刷新</button>
        <button class="secondary" @tap="uni.switchTab({ url: '/pages/collect/index' })">去采集</button>
      </view>
    </view>

    <view class="card">
      <text class="section-title">共 {{ filteredItems.length }} 条</text>
      <view v-if="!filteredItems.length" class="empty">暂无数据</view>
      <view v-for="item in filteredItems" :key="item.id || JSON.stringify(item)" class="list-item">
        <text class="list-title">{{ displayValue(item, activeTab.titleField) || "未命名记录" }}</text>
        <text class="list-meta">{{ summarize(item, activeTab.meta) }}</text>
      </view>
    </view>
  </view>
</template>
