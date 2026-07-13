<script setup>
import { computed, inject, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ArrowLeft, FlaskConical, RotateCcw, Upload } from "lucide-vue-next";
import { api } from "@/services/api";
import AppSelect from "@/components/AppSelect.vue";

const router = useRouter();
const notify = inject("notify");
const currentRole = inject("currentRole");

const sampleFile = ref(null);
const sampleInfo = ref(null);
const referenceFile = ref(null);
const referenceInfo = ref(null);
const referenceId = ref("");
const references = ref([]);
const comparing = ref(false);
const result = ref(null);
const errorMsg = ref("");
const loadingRefs = ref(false);
const chartDom = ref(null);
const referenceOptions = computed(() => [
  { value: "", label: "从已有记录选择..." },
  ...references.value.map(item => ({
    value: item.id,
    label: `${item.herbName || item.referenceName || "标准品"}（${item.spectrumType || "HPLC"}）`
  }))
]);

let chartInstance = null;
let echartsLib = null;

// ── Load ECharts from CDN ──
function loadECharts() {
  return new Promise((resolve) => {
    if (window.echarts) { echartsLib = window.echarts; resolve(); return; }
    const script = document.createElement("script");
    script.src = "https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js";
    script.onload = () => { echartsLib = window.echarts; resolve(); };
    document.head.appendChild(script);
  });
}

// ── Load references ──
async function loadReferences() {
  loadingRefs.value = true;
  try {
    const data = await api("/api/spectrum/references");
    references.value = (data.items || []).filter(r => r.herbName || r.referenceName);
  } catch (e) { /* ignore */ }
  finally { loadingRefs.value = false; }
}

function handleSampleFile(e) {
  const f = e.target.files[0];
  if (!f) return;
  sampleFile.value = f;
  sampleInfo.value = { name: f.name, size: (f.size / 1024).toFixed(1) + " KB" };
}

function handleReferenceFile(e) {
  const f = e.target.files[0];
  if (!f) return;
  referenceFile.value = f;
  referenceInfo.value = { name: f.name, size: (f.size / 1024).toFixed(1) + " KB" };
  referenceId.value = "";
}

function selectReference() {
  referenceFile.value = null;
  referenceInfo.value = null;
}

async function runComparison() {
  if (!sampleFile.value) { errorMsg.value = "请先上传样本图谱文件"; return; }
  if (!referenceFile.value && !referenceId.value) { errorMsg.value = "请上传标准品文件或从已有记录中选择"; return; }
  errorMsg.value = "";
  comparing.value = true;
  result.value = null;

  try {
    const fd = new FormData();
    fd.append("sampleFile", sampleFile.value);
    if (referenceFile.value) fd.append("referenceFile", referenceFile.value);
    if (referenceId.value) fd.append("referenceId", referenceId.value);
    fd.append("algorithm", "COSINE");
    fd.append("resolution", "500");

    const resp = await api("/api/spectrum/compare", { method: "POST", body: fd });
    result.value = resp;
    notify?.("比对完成 — 相似度 " + resp.similarity + "%");
    setTimeout(() => renderChart(resp), 100);
  } catch (e) {
    errorMsg.value = e.message;
  } finally {
    comparing.value = false;
  }
}

function renderChart(data) {
  if (!echartsLib || !chartDom.value) return;
  if (!chartInstance) {
    chartInstance = echartsLib.init(chartDom.value);
    window.addEventListener("resize", () => chartInstance?.resize());
  }

  const sData = JSON.parse(data.alignedSample || "[]");
  const rData = JSON.parse(data.alignedReference || "[]");
  const diffs = (data.diffRegions || []).map(d => [
    { xAxis: d.xStart, itemStyle: { color: "rgba(199,90,56,0.12)", borderColor: "#c75a38", borderWidth: 1, borderType: "dashed" } },
    { xAxis: d.xEnd }
  ]);

  chartInstance.setOption({
    tooltip: {
      trigger: "axis",
      formatter: function(ps) {
        let h = "<b>保留时间: " + parseFloat(ps[0].axisValue).toFixed(2) + " min</b><br/>";
        ps.forEach(p => { h += p.marker + " " + p.seriesName + ": " + p.data[1].toFixed(4) + "<br/>"; });
        if (ps.length >= 2) {
          const d = Math.abs(ps[0].data[1] - ps[1].data[1]);
          const a = (Math.abs(ps[0].data[1]) + Math.abs(ps[1].data[1])) / 2 || 1;
          h += '<span style="color:#66736c">偏差: ' + (d / a * 100).toFixed(1) + "%</span>";
        }
        return h;
      }
    },
    legend: { data: ["样本图谱", "标准品图谱"], top: 6, textStyle: { fontSize: 11, color: "#66736c" } },
    grid: { left: 60, right: 20, top: 45, bottom: 55 },
    xAxis: { type: "value", name: "保留时间 (min)", nameLocation: "center", nameGap: 35, axisLabel: { fontSize: 10, color: "#66736c" }, nameTextStyle: { color: "#66736c" } },
    yAxis: { type: "value", name: "归一化信号值", nameLocation: "center", nameGap: 50, axisLabel: { fontSize: 10, color: "#66736c" }, nameTextStyle: { color: "#66736c" } },
    dataZoom: [
      { type: "inside", xAxisIndex: 0, zoomOnMouseWheel: true },
      { type: "slider", xAxisIndex: 0, bottom: 8, height: 18, borderColor: "#e1e7e3", backgroundColor: "#f7f9f8", fillerColor: "rgba(21,116,87,0.12)" }
    ],
    toolbox: { feature: { restore: { title: "重置" }, saveAsImage: { title: "保存图片", pixelRatio: 2 } }, right: 8, top: 4 },
    series: [
      { name: "样本图谱", type: "line", data: sData.map(p => [p.x, p.y]), lineStyle: { color: "#157457", width: 1.8 }, symbol: "none", z: 2 },
      { name: "标准品图谱", type: "line", data: rData.map(p => [p.x, p.y]), lineStyle: { color: "#c75a38", width: 2, type: "dashed" }, symbol: "none", z: 1 },
      { name: "差异区域", type: "line", data: [], markArea: { silent: true, label: { show: true, fontSize: 9, color: "#c75a38", formatter: "差异" }, data: diffs }, z: 0 }
    ]
  }, true);
}

function resetChart() { chartInstance?.dispatchAction({ type: "restore" }); }

function scoreClass(s) { return s >= 90 ? "pass" : s >= 80 ? "warn" : "fail"; }
function scoreColor(s) { return s >= 90 ? "#157457" : s >= 80 ? "#c75a38" : "#b93d3d"; }
function verdictText(s) {
  if (s >= 90) return " 通过 — 高度一致，达到标准品水平";
  if (s >= 80) return "基本一致 — 存在可接受差异，建议复核";
  return "不一致 — 差异显著，建议重新检测";
}
function verdictClass(s) { return s >= 90 ? "pass" : s >= 80 ? "warn" : "fail"; }

onMounted(async () => {
  await loadECharts();
  loadReferences();
});

onUnmounted(() => {
  chartInstance?.dispose();
  chartInstance = null;
});
</script>

<template>
  <section class="panel resource-workbench">
    <div class="panel-head resource-head">
      <div><h2>图谱比对</h2><span>上传 HPLC 数据，自动计算相似度并生成叠加图谱</span></div>
      <button type="button" @click="router.push('/module/spectrum-comparisons')"><ArrowLeft :size="16" />返回图谱比对列表</button>
    </div>

    <!-- CSV 选择栏 -->
    <div style="margin-bottom:14px">
      <div style="display:flex;align-items:center;gap:18px;flex-wrap:wrap">
        <!-- 样本 -->
        <div style="display:flex;align-items:center;gap:8px;min-width:0">
          <span style="font-size:12px;font-weight:700;white-space:nowrap;color:var(--ink)">样本</span>
          <label class="file-upload-label" style="margin:0">
            <input type="file" accept=".csv,.txt" @change="handleSampleFile" />
            <Upload :size="14" /> {{ sampleInfo ? '更换' : '选择 CSV' }}
          </label>
          <span v-if="sampleInfo" style="font-size:11px;color:#157457;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">{{ sampleInfo.name }}</span>
        </div>
        <!-- 标准品 -->
        <div style="display:flex;align-items:center;gap:8px;min-width:0">
          <span style="font-size:12px;font-weight:700;white-space:nowrap;color:var(--ink)">标准品</span>
          <label class="file-upload-label" style="margin:0">
            <input type="file" accept=".csv,.txt" @change="handleReferenceFile" />
            <Upload :size="14" /> {{ referenceInfo ? '更换' : '上传 CSV' }}
          </label>
          <span style="color:var(--muted);font-size:11px">或</span>
          <AppSelect v-model="referenceId" :options="referenceOptions" aria-label="选择已有标准品记录" style="width:auto;min-width:220px" @change="selectReference" />
          <span v-if="referenceInfo" style="font-size:11px;color:#157457;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">{{ referenceInfo.name }}</span>
          <span v-else-if="referenceId" style="font-size:11px;color:#157457;white-space:nowrap">已选择</span>
        </div>
        <!-- 比对按钮 -->
        <button :disabled="comparing" @click="runComparison" style="flex-shrink:0">
          <RotateCcw v-if="comparing" :size="16" style="animation:spin 1s linear infinite" />
          <FlaskConical v-else :size="16" />
          {{ comparing ? "比对中..." : "开始比对" }}
        </button>
      </div>
      <div v-if="errorMsg" style="margin-top:8px;padding:8px 12px;background:#fef2f2;color:#b93d3d;border:1px solid #fecaca;border-radius:6px;font-size:12px">{{ errorMsg }}</div>
    </div>

    <!-- 中间：图谱区域（全宽） -->
    <div class="panel" style="margin-bottom:14px;min-height:420px;display:flex;flex-direction:column">
      <div class="panel-head">
        <h2>HPLC 图谱叠加比对</h2>
        <span style="font-size:10px;color:var(--muted)">绿 = 样本 · 红 = 标准品</span>
        <span style="flex:1"></span>
        <button class="button-secondary" style="min-height:30px;padding:0 10px;font-size:11px" @click="resetChart"><RotateCcw :size="13" /> 重置</button>
      </div>
      <div style="flex:1;position:relative;min-height:380px">
        <div v-if="!result && !comparing" style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;color:var(--muted);font-size:13px">
          请上传数据并点击"开始比对"
        </div>
        <div v-else-if="comparing" style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;color:var(--muted);font-size:13px">
          ⏳ 正在比对中...
        </div>
        <div ref="chartDom" v-show="result" style="position:absolute;inset:0"></div>
      </div>
    </div>

    <!-- 下方：比对结果（横向展开） -->
    <div v-if="result" class="panel">
      <div class="panel-head"><h2>比对结果</h2></div>
      <div style="display:flex;gap:18px;align-items:flex-start;flex-wrap:wrap">
        <!-- 分数环 -->
        <div :style="{ width:'82px', height:'82px', borderRadius:'50%', display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', fontWeight:700, flexShrink:0,
          background: scoreClass(result.similarity) === 'pass' ? '#e8f4ee' : scoreClass(result.similarity) === 'warn' ? '#fdf3eb' : '#fef2f2',
          color: scoreColor(result.similarity),
          border: '3px solid ' + (scoreClass(result.similarity) === 'pass' ? '#b6ddcc' : scoreClass(result.similarity) === 'warn' ? '#f5ceb3' : '#f5c2c2') }">
          <span style="font-size:21px;line-height:1">{{ result.similarity }}%</span>
          <span style="font-size:9px;opacity:.7;margin-top:2px">相似度</span>
        </div>
        <!-- 判定 + 元数据 -->
        <div style="min-width:200px;flex:1">
          <div :style="{ fontSize:'15px', fontWeight:700, color: scoreColor(result.similarity), marginBottom:'4px' }">{{ verdictText(result.similarity) }}</div>
          <div style="font-size:11px;color:var(--muted);line-height:1.7">
            算法：{{ result.algorithm }} · 比对点数：{{ result.comparedPoints }} · X 范围：{{ result.xMin?.toFixed(1) || '?' }} ~ {{ result.xMax?.toFixed(1) || '?' }} min
          </div>
        </div>
        <!-- 差异区域 -->
        <div v-if="result.diffRegions?.length" style="min-width:180px;flex:1">
          <div style="font-size:11px;font-weight:700;color:var(--muted);margin-bottom:4px">差异区域（{{ result.diffRegions.length }} 处）</div>
          <div style="display:flex;flex-wrap:wrap;gap:4px">
            <span v-for="(d, i) in result.diffRegions.slice(0, 6)" :key="i"
              style="font-size:10px;padding:3px 8px;border-radius:14px;background:#fdf3eb;color:#c75a38;border:1px solid #f5ceb3">
              RT {{ d.xStart }}~{{ d.xEnd }} · {{ d.maxDiffPercent }}%
            </span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
/* ── back-link (shared) ── */
.back-link {
  display: inline-flex; align-items: center; gap: 6px;
  background: none; border: none; color: #66736c; cursor: pointer;
  font-size: 13px; font-weight: 600; min-height: auto; padding: 4px 0;
  box-shadow: none;
}
.back-link:hover { color: #157457; background: none; box-shadow: none; }

/* ── file upload ── */
.file-upload-label {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 14px; border: 1px dashed #cdd8d1; border-radius: 7px;
  cursor: pointer; font-size: 13px; color: #157457; background: #f7f9f8;
  transition: all .2s; font-weight: 650;
}
.file-upload-label:hover { border-color: #157457; background: #e8f4ee; }
.file-upload-label input[type="file"] { display: none; }

.upload-ok {
  padding: 8px 12px; border-radius: 7px; font-size: 12px;
  background: #e8f4ee; color: #157457; border: 1px solid #b6ddcc;
}

/* ── spin animation ── */
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
