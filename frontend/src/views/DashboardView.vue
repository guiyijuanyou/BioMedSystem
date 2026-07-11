<script setup>
import { computed, inject, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import L from "leaflet";
import "leaflet.markercluster";
import {
  Activity, Award, BookOpen, Boxes, ClipboardCheck, FlaskConical, Leaf,
  GitBranch, Plus, RotateCcw, Search, X, ZoomIn, ZoomOut
} from "lucide-vue-next";

const router = useRouter();
const summary = inject("summary");
const herbs = inject("herbs");
const batches = inject("batches");
const currentRole = inject("currentRole");
const currentUser = inject("currentUser");
const canEditMap = inject("canEditMap");
const notify = inject("notify");
const submitGrowthHandler = inject("submitGrowth");
const mapElement = ref();
const search = ref("");
const herbFilter = ref("");
const districtFilter = ref("");
const selected = ref(null);
const captureOpen = ref(false);
const mapStatus = ref("正在加载真实地图...");
const sourceIndex = ref(0);
let map;
let cluster;
let selectedMarker;
let tileLayer;
let sourceAttempts = 0;

const tileSources = [
  {
    name: "直连 OpenStreetMap",
    url: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
    options: { maxZoom: 19, attribution: "&copy; OpenStreetMap" }
  },
  {
    name: "系统代理 Esri",
    url: "/api/map/tiles/esri/{z}/{x}/{y}",
    options: { maxZoom: 18, attribution: "Tiles &copy; Esri" }
  },
  {
    name: "直连 Esri 街道图",
    url: "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}",
    options: { maxZoom: 18, attribution: "Tiles &copy; Esri" }
  },
  {
    name: "直连 OSM HOT",
    url: "https://a.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png",
    options: { maxZoom: 19, attribution: "&copy; OpenStreetMap contributors" }
  }
];

const metrics = computed(() => [
  { label: "资源点数量", value: summary.value.herbCount || 0, icon: Leaf, tone: "green" },
  { label: "药材批次", value: summary.value.herbBatchCount || 0, icon: Boxes, tone: "blue" },
  { label: "检测样本", value: summary.value.labSampleCount || 0, icon: FlaskConical, tone: "violet" },
  { label: "采集记录", value: summary.value.growthRecordCount || 0, icon: Activity, tone: "blue" },
  { label: "溯源事件", value: summary.value.traceEventCount || 0, icon: GitBranch, tone: "green" },
  { label: "教学资源", value: summary.value.teachingResourceCount || 0, icon: BookOpen, tone: "amber" },
  { label: "图谱比对", value: summary.value.spectrumComparisonCount || 0, icon: FlaskConical, tone: "violet" },
  { label: "数据分析", value: summary.value.growthAnalysisCount || 0, icon: ClipboardCheck, tone: "cyan" },
  { label: "试验课程", value: summary.value.courseCount || 0, icon: BookOpen, tone: "amber" },
  { label: "研究课题", value: summary.value.projectCount || 0, icon: FlaskConical, tone: "violet" },
  { label: "评价记录", value: summary.value.evaluationCount || 0, icon: ClipboardCheck, tone: "cyan" },
  { label: "业绩记录", value: summary.value.achievementCount || 0, icon: Award, tone: "red" }
]);

const herbOptions = computed(() => [...new Set(herbs.value.map(item => item.name).filter(Boolean))].sort());
const districtOptions = computed(() => [...new Set(herbs.value.map(item => item.district).filter(Boolean))].sort());
const filtered = computed(() => herbs.value.filter(item => {
  const text = `${item.name || ""} ${item.district || ""}`.toLowerCase();
  return (!search.value || text.includes(search.value.trim().toLowerCase()))
    && (!herbFilter.value || item.name === herbFilter.value)
    && (!districtFilter.value || item.district === districtFilter.value);
}));

const located = computed(() => filtered.value.filter(item => {
  const lat = Number(item.latitude);
  const lng = Number(item.longitude);
  return Number.isFinite(lat) && Number.isFinite(lng) && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
}));

const growth = reactive({
  herbName: "",
  batchId: "",
  district: "",
  temperature: "20.0",
  humidity: "80",
  soilPh: "6.5",
  collectSource: "电脑终端录入",
  recordedAt: new Date().toISOString().slice(0, 19)
});

function markerIcon(active = false) {
  return L.divIcon({
    className: "",
    html: `<span class="herb-map-marker${active ? " selected" : ""}"></span>`,
    iconSize: active ? [31, 31] : [25, 25],
    iconAnchor: active ? [15, 15] : [12, 12]
  });
}

function useTileSource(index, automatic = false) {
  if (!map) return;
  if (tileLayer) map.removeLayer(tileLayer);
  sourceIndex.value = index;
  const source = tileSources[index];
  mapStatus.value = `正在加载${source.name}...`;
  let failures = 0;
  let loaded = false;
  tileLayer = L.tileLayer(source.url, source.options);
  tileLayer.on("load", () => {
    loaded = true;
    sourceAttempts = 0;
    mapStatus.value = `底图：${source.name}`;
  });
  tileLayer.on("tileerror", () => {
    failures += 1;
    if (!loaded && failures >= 3) fallbackTileSource();
  });
  tileLayer.addTo(map);
  setTimeout(() => {
    if (!loaded && document.querySelectorAll(".leaflet-tile-loaded").length === 0) fallbackTileSource();
  }, automatic ? 2500 : 4500);
}

function fallbackTileSource() {
  if (sourceAttempts >= tileSources.length - 1) {
    mapStatus.value = "底图加载失败，请检查网络后切换底图";
    return;
  }
  sourceAttempts += 1;
  useTileSource((sourceIndex.value + 1) % tileSources.length, true);
}

function changeSource(event) {
  sourceAttempts = 0;
  useTileSource(Number(event.target.value));
}

function renderMarkers() {
  if (!map || !cluster) return;
  cluster.clearLayers();
  selected.value = null;
  selectedMarker = null;
  located.value.forEach(item => {
    const marker = L.marker([Number(item.latitude), Number(item.longitude)], {
      icon: markerIcon(),
      title: `${item.name || "未命名药材"} · ${item.district || "未知地区"}`
    });
    marker.bindTooltip(`${item.name || "未命名药材"} · ${item.district || "未知地区"}`, { direction: "top", offset: [0, -12] });
    marker.on("click", () => {
      if (selectedMarker) selectedMarker.setIcon(markerIcon());
      selected.value = item;
      selectedMarker = marker;
      marker.setIcon(markerIcon(true));
    });
    cluster.addLayer(marker);
  });
  if (located.value.length && (search.value || herbFilter.value || districtFilter.value)) {
    map.fitBounds(L.latLngBounds(located.value.map(item => [Number(item.latitude), Number(item.longitude)])), {
      padding: [35, 35],
      maxZoom: 12
    });
  } else if (!search.value && !herbFilter.value && !districtFilter.value) resetMap();
}

function clearFilters() {
  search.value = "";
  herbFilter.value = "";
  districtFilter.value = "";
}

function resetMap() {
  map?.setView([29.563, 106.5516], 7);
}

async function submitGrowth() {
  if (!growth.batchId) {
    notify("请选择药材批次后再提交采集数据");
    return;
  }
  await submitGrowthHandler({ ...growth });
  growth.recordedAt = new Date().toISOString().slice(0, 19);
}

function applyGrowthBatch() {
  const batch = batches.value.find(item => item.id === growth.batchId);
  growth.herbName = batch?.herbName || "";
  growth.district = batch?.district || "";
}

onMounted(async () => {
  await nextTick();
  map = L.map(mapElement.value, { center: [29.563, 106.5516], zoom: 7, minZoom: 6, maxZoom: 17, zoomControl: false });
  cluster = L.markerClusterGroup({ showCoverageOnHover: false, spiderfyOnMaxZoom: true, disableClusteringAtZoom: 13, maxClusterRadius: 45 });
  map.addLayer(cluster);
  map.on("click", () => {
    if (selectedMarker) selectedMarker.setIcon(markerIcon());
    selected.value = null;
    selectedMarker = null;
  });
  useTileSource(0);
  renderMarkers();
  setTimeout(() => map.invalidateSize(), 0);
});

watch([filtered, located], renderMarkers, { deep: true });
onBeforeUnmount(() => map?.remove());
</script>

<template>
  <section>
    <div class="metrics">
      <article v-for="metric in metrics" :key="metric.label" class="metric" :data-tone="metric.tone">
        <div class="metric-icon"><component :is="metric.icon" :size="19" /></div>
        <div><span>{{ metric.label }}</span><strong>{{ metric.value }}</strong><small>实时统计</small></div>
      </article>
    </div>

    <div class="dashboard-layout">
      <section class="panel map-panel">
        <div class="panel-head">
          <div>
            <h2>种植基地与资源点地图</h2>
            <span>维护资源点位置，并作为药材批次的数据来源</span>
          </div>
          <div class="map-tools">
            <button v-if="canEditMap" class="map-add-point" type="button" @click="router.push({ path: '/module/herbs', query: { create: '1' } })"><Plus :size="16" />新增资源点</button>
            <button type="button" title="缩小地图" @click="map?.zoomOut()"><ZoomOut :size="17" /></button>
            <button type="button" title="复位地图" @click="resetMap"><RotateCcw :size="17" /></button>
            <button type="button" title="放大地图" @click="map?.zoomIn()"><ZoomIn :size="17" /></button>
          </div>
        </div>

        <div class="map-filter-bar">
          <label class="map-search">
            <span>搜索</span>
            <div class="input-with-icon"><Search :size="15" /><input v-model="search" type="search" placeholder="药材名称或区县"></div>
          </label>
          <label><span>药材品种</span><select v-model="herbFilter"><option value="">全部药材</option><option v-for="name in herbOptions" :key="name">{{ name }}</option></select></label>
          <label><span>所属区县</span><select v-model="districtFilter"><option value="">全部区县</option><option v-for="name in districtOptions" :key="name">{{ name }}</option></select></label>
          <button class="button-secondary" type="button" @click="clearFilters"><X :size="15" />清空</button>
          <output class="map-result-count">{{ filtered.length }} 条结果<span v-if="filtered.length - located.length">，{{ filtered.length - located.length }} 条缺少坐标</span></output>
        </div>

        <div class="map-source-row">
          <span :class="{ error: mapStatus.includes('失败') }">{{ mapStatus }}</span>
          <label>底图<select :value="sourceIndex" @change="changeSource"><option v-for="(source, index) in tileSources" :key="source.name" :value="index">{{ source.name }}</option></select></label>
        </div>

        <div class="map-content">
          <div class="map-wrap">
            <div ref="mapElement" class="real-map"></div>
            <div v-if="!located.length" class="map-empty">没有符合条件且包含经纬度的记录</div>
          </div>
          <aside class="map-detail">
            <div v-if="!selected" class="map-detail-empty"><strong>资源点详情</strong><p>点击地图标记查看种植基地信息</p></div>
            <template v-else>
              <div class="map-detail-head"><strong>{{ selected.name || "未命名药材" }}</strong><span>{{ selected.district || "未知地区" }}</span></div>
              <dl class="map-detail-list">
                <div><dt>具体地点</dt><dd>{{ selected.location || selected.town || selected.district || "-" }}</dd></div>
                <div><dt>经纬度</dt><dd>{{ selected.longitude || "-" }}, {{ selected.latitude || "-" }}</dd></div>
                <div><dt>种植规模</dt><dd>{{ selected.scale || "-" }}</dd></div>
                <div><dt>生态环境</dt><dd>{{ selected.environment || "-" }}</dd></div>
                <div><dt>溯源码</dt><dd>{{ selected.traceCode || "-" }}</dd></div>
                <div><dt>记录时间</dt><dd>{{ selected.createdAt || selected.recordedAt || "-" }}</dd></div>
              </dl>
              <button v-if="canEditMap" type="button" @click="router.push({ path: '/module/herbs', query: { editId: selected.id } })">查看并编辑资源点</button>
            </template>
          </aside>
        </div>
      </section>

      <section class="panel capture-panel" :class="{ collapsed: !captureOpen }">
        <div class="panel-head">
          <div><h2>生长数据快捷录入</h2><span>支持 APP、传感器和电脑终端采集</span></div>
          <button class="button-secondary" type="button" @click="captureOpen = !captureOpen">{{ captureOpen ? "收起录入" : "展开录入" }}</button>
        </div>
        <form v-if="captureOpen" class="form-grid quick-growth-form" @submit.prevent="submitGrowth">
          <label>药材批次<select v-model="growth.batchId" @change="applyGrowthBatch"><option value="">请选择批次</option><option v-for="batch in batches" :key="batch.id" :value="batch.id">{{ batch.batchName }} / {{ batch.batchCode }}</option></select></label>
          <label>药材名称<input v-model="growth.herbName" readonly></label>
          <label>采集地点<input v-model="growth.district" readonly></label>
          <label>温度<input v-model="growth.temperature"></label>
          <label>湿度<input v-model="growth.humidity"></label>
          <label>土壤 PH<input v-model="growth.soilPh"></label>
          <label>采集来源<select v-model="growth.collectSource"><option>电脑终端录入</option><option>手机APP采集</option><option>传感器网关</option></select></label>
          <label>采集时间<input v-model="growth.recordedAt"></label>
          <button type="submit">提交采集数据</button>
        </form>
      </section>
    </div>
  </section>
</template>
