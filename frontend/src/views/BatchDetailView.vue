<script setup>
import { computed, inject, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Activity, ArrowLeft, BarChart3, Beaker, ClipboardCheck, FlaskConical,
  GitBranch, MapPin, Plus, RefreshCw
} from "lucide-vue-next";
import { api } from "@/services/api";

const route = useRoute();
const router = useRouter();
const currentRole = inject("currentRole");
const notify = inject("notify");
const loading = ref(true);
const errorText = ref("");
const activeTab = ref("overview");
const overview = ref({
  batch: {}, growthRecords: [], traceEvents: [], samples: [],
  spectrumComparisons: [], analyses: [], evaluations: []
});

const batch = computed(() => overview.value.batch || {});
const growth = computed(() => overview.value.growthRecords || []);
const traces = computed(() => overview.value.traceEvents || []);
const samples = computed(() => overview.value.samples || []);
const spectra = computed(() => overview.value.spectrumComparisons || []);
const analyses = computed(() => overview.value.analyses || []);
const evaluations = computed(() => overview.value.evaluations || []);
const canMaintain = computed(() => currentRole.value !== "student");
const tabs = computed(() => [
  { key: "overview", label: "档案概览", count: growth.value.length },
  { key: "trace", label: "溯源链路", count: traces.value.length },
  { key: "samples", label: "样本与图谱", count: samples.value.length + spectra.value.length },
  { key: "analysis", label: "分析与评价", count: analyses.value.length + evaluations.value.length }
]);
const metrics = computed(() => [
  { label: "生长记录", value: growth.value.length, icon: Activity },
  { label: "溯源事件", value: traces.value.length, icon: GitBranch },
  { label: "检测样本", value: samples.value.length, icon: Beaker },
  { label: "图谱比对", value: spectra.value.length, icon: FlaskConical },
  { label: "分析结论", value: analyses.value.length, icon: BarChart3 },
  { label: "评价记录", value: evaluations.value.length, icon: ClipboardCheck }
]);
const growthSummary = computed(() => ({
  temperature: average(growth.value, "temperature"),
  humidity: average(growth.value, "humidity"),
  soilPh: average(growth.value, "soilPh"),
  latest: growth.value[growth.value.length - 1]
}));
const chartSeries = computed(() => [
  { key: "temperature", label: "温度", color: "#287aa6", points: chartPoints("temperature") },
  { key: "humidity", label: "湿度", color: "#b36c32", points: chartPoints("humidity") },
  { key: "soilPh", label: "土壤 PH", color: "#26765a", points: chartPoints("soilPh") }
]);

async function load() {
  loading.value = true;
  errorText.value = "";
  try {
    overview.value = await api(`/api/herb-batches/${route.params.batchId}/overview`);
  } catch (error) {
    errorText.value = error.message;
  } finally {
    loading.value = false;
  }
}

function createForBatch(moduleKey) {
  router.push({ path: `/module/${moduleKey}`, query: { create: "1", batchId: batch.value.id } });
}

function openRecord(moduleKey, id) {
  router.push({ path: `/module/${moduleKey}`, query: { editId: id } });
}

function average(rows, key) {
  const values = rows.map(row => Number(row[key])).filter(Number.isFinite);
  if (!values.length) return "-";
  return (values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(1);
}

function chartPoints(key) {
  const values = growth.value.map(row => Number(row[key]));
  const valid = values.filter(Number.isFinite);
  if (!valid.length) return "";
  const min = Math.min(...valid);
  const max = Math.max(...valid);
  return values.map((value, index) => {
    const x = growth.value.length <= 1 ? 50 : 5 + index * 90 / (growth.value.length - 1);
    const y = Number.isFinite(value) ? 88 - ((value - min) / (max - min || 1)) * 70 : 88;
    return `${x},${y}`;
  }).join(" ");
}

function display(value) {
  return value === undefined || value === null || value === "" ? "-" : value;
}

function statusLabel(value) {
  return { active: "使用中", archived: "已归档", collected: "已采样", tested: "已检测" }[value] || display(value);
}

function formatTime(value) {
  return value ? String(value).replace("T", " ") : "-";
}

onMounted(load);
</script>

<template>
  <section class="batch-detail-page">
    <div v-if="loading" class="batch-detail-loading"><RefreshCw :size="20" class="spin" />正在汇总批次档案...</div>
    <div v-else-if="errorText" class="batch-detail-error"><strong>批次档案加载失败</strong><span>{{ errorText }}</span><button type="button" @click="load">重新加载</button></div>
    <template v-else>
      <header class="batch-detail-head">
        <button class="icon-button" type="button" title="返回批次列表" @click="router.push('/module/herb-batches')"><ArrowLeft :size="18" /></button>
        <div>
          <span class="batch-kicker">{{ batch.herbName }} · {{ batch.district }}</span>
          <h2>{{ batch.batchName }}</h2>
          <p>{{ batch.batchCode }} · {{ batch.traceCode || "未设置溯源码" }}</p>
        </div>
        <div class="batch-head-meta">
          <span class="status">{{ statusLabel(batch.status) }}</span>
          <small>负责人 {{ display(batch.responsiblePerson) }}</small>
        </div>
      </header>

      <section class="batch-metric-band">
        <article v-for="metric in metrics" :key="metric.label">
          <component :is="metric.icon" :size="17" />
          <span>{{ metric.label }}</span>
          <strong>{{ metric.value }}</strong>
        </article>
      </section>

      <nav class="batch-tabs" aria-label="批次档案视图">
        <button v-for="tab in tabs" :key="tab.key" type="button" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          {{ tab.label }}<span>{{ tab.count }}</span>
        </button>
      </nav>

      <section v-if="activeTab === 'overview'" class="batch-section batch-overview-grid">
        <div class="batch-profile">
          <div class="section-title"><div><span>基础档案</span><h3>批次与基地信息</h3></div><MapPin :size="20" /></div>
          <dl>
            <div><dt>来源资源点</dt><dd>{{ batch.herbName }} / {{ batch.district }}</dd></div>
            <div><dt>地块或基地</dt><dd>{{ display(batch.plotName) }}</dd></div>
            <div><dt>当前阶段</dt><dd>{{ display(batch.currentStage) }}</dd></div>
            <div><dt>种植日期</dt><dd>{{ display(batch.plantingDate) }}</dd></div>
            <div><dt>预计采收</dt><dd>{{ display(batch.expectedHarvestDate) }}</dd></div>
            <div><dt>种植规模</dt><dd>{{ display(batch.scale) }}</dd></div>
            <div><dt>经纬度</dt><dd>{{ display(batch.longitude) }}, {{ display(batch.latitude) }}</dd></div>
            <div><dt>生态环境</dt><dd>{{ display(batch.environment) }}</dd></div>
          </dl>
        </div>

        <div class="batch-growth-panel">
          <div class="section-title"><div><span>连续监测</span><h3>生长指标趋势</h3></div><button v-if="canMaintain" type="button" @click="createForBatch('growth-records')"><Plus :size="15" />新增采集</button></div>
          <div class="growth-summary-strip">
            <span>平均温度<strong>{{ growthSummary.temperature }}</strong></span>
            <span>平均湿度<strong>{{ growthSummary.humidity }}</strong></span>
            <span>平均 PH<strong>{{ growthSummary.soilPh }}</strong></span>
            <span>最新阶段<strong>{{ display(growthSummary.latest?.growthStage) }}</strong></span>
          </div>
          <div v-if="growth.length" class="batch-line-chart">
            <svg viewBox="0 0 100 100" preserveAspectRatio="none" aria-label="生长指标趋势图">
              <line v-for="y in [18, 41, 64, 87]" :key="y" x1="5" :y1="y" x2="95" :y2="y" class="chart-grid-line" />
              <polyline v-for="series in chartSeries" :key="series.key" :points="series.points" :style="{ stroke: series.color }" />
            </svg>
            <div class="chart-legend"><span v-for="series in chartSeries" :key="series.key"><i :style="{ background: series.color }"></i>{{ series.label }}</span></div>
          </div>
          <p v-else class="empty-state">该批次还没有生长采集记录</p>
        </div>
      </section>

      <section v-else-if="activeTab === 'trace'" class="batch-section">
        <div class="section-title"><div><span>全程留痕</span><h3>批次溯源时间线</h3></div><button v-if="canMaintain" type="button" @click="createForBatch('trace-events')"><Plus :size="15" />新增事件</button></div>
        <div v-if="traces.length" class="batch-timeline">
          <article v-for="event in traces" :key="event.id">
            <time>{{ formatTime(event.eventTime) }}</time>
            <div><strong>{{ event.eventType }}</strong><span>{{ event.location || batch.district }}</span><p>{{ event.eventContent || "未填写事件说明" }}</p><small>{{ event.operatorName }}</small></div>
          </article>
        </div>
        <p v-else class="empty-state">该批次还没有溯源事件</p>
      </section>

      <section v-else-if="activeTab === 'samples'" class="batch-section">
        <div class="section-title"><div><span>质量检测</span><h3>样本与图谱结果</h3></div><div class="section-actions"><button v-if="canMaintain" class="button-secondary" type="button" @click="createForBatch('lab-samples')"><Plus :size="15" />新增样本</button><button v-if="canMaintain" type="button" @click="createForBatch('spectrum-comparisons')"><Plus :size="15" />图谱比对</button></div></div>
        <div class="batch-split-tables">
          <div><h4>检测样本</h4><table><thead><tr><th>样本编号</th><th>类型</th><th>采样时间</th><th>状态</th></tr></thead><tbody><tr v-for="sample in samples" :key="sample.id" @click="openRecord('lab-samples', sample.id)"><td>{{ sample.sampleCode }}</td><td>{{ display(sample.sampleType) }}</td><td>{{ formatTime(sample.collectedAt) }}</td><td><span class="status">{{ statusLabel(sample.status) }}</span></td></tr><tr v-if="!samples.length"><td colspan="4" class="empty-cell">暂无检测样本</td></tr></tbody></table></div>
          <div><h4>图谱结果</h4><table><thead><tr><th>样本</th><th>类型</th><th>相似度</th><th>结论</th></tr></thead><tbody><tr v-for="spectrum in spectra" :key="spectrum.id" @click="openRecord('spectrum-comparisons', spectrum.id)"><td>{{ spectrum.sampleCode }}</td><td>{{ display(spectrum.spectrumType) }}</td><td>{{ display(spectrum.similarity) }}</td><td><span class="status">{{ display(spectrum.result) }}</span></td></tr><tr v-if="!spectra.length"><td colspan="4" class="empty-cell">暂无图谱结果</td></tr></tbody></table></div>
        </div>
      </section>

      <section v-else class="batch-section">
        <div class="section-title"><div><span>决策依据</span><h3>分析结论与质量评价</h3></div><div class="section-actions"><button v-if="canMaintain" class="button-secondary" type="button" @click="createForBatch('growth-analysis')"><Plus :size="15" />新增分析</button><button v-if="canMaintain" type="button" @click="createForBatch('evaluations')"><Plus :size="15" />发起评价</button></div></div>
        <div class="analysis-evaluation-grid">
          <div><h4>数据分析</h4><article v-for="analysis in analyses" :key="analysis.id" @click="openRecord('growth-analysis', analysis.id)"><div><strong>{{ analysis.analysisName }}</strong><span class="status">{{ analysis.status }}</span></div><p>{{ analysis.conclusion || "暂无分析结论" }}</p><small>{{ analysis.indicator }} · {{ analysis.trend }} · {{ formatTime(analysis.analyzedAt) }}</small></article><p v-if="!analyses.length" class="empty-state">暂无分析结论</p></div>
          <div><h4>质量评价</h4><article v-for="evaluation in evaluations" :key="evaluation.id" @click="openRecord('evaluations', evaluation.id)"><div><strong>{{ evaluation.indicator || "综合评价" }}</strong><span class="evaluation-score">{{ display(evaluation.score) }}</span></div><p>{{ evaluation.result || "暂无评价结论" }}</p><small>{{ evaluation.evaluator }} · {{ evaluation.status }}</small></article><p v-if="!evaluations.length" class="empty-state">暂无质量评价</p></div>
        </div>
      </section>
    </template>
  </section>
</template>
