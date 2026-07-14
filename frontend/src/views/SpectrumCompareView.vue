<script setup>
import { computed, inject, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ArrowLeft, FlaskConical, RotateCcw, Upload } from "lucide-vue-next";
import { api } from "@/services/api";
import AppSelect from "@/components/AppSelect.vue";
import ReferenceManager from "@/components/ReferenceManager.vue";

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
const diffThreshold = ref(6);
const loadingRefs = ref(false);
const chartDom = ref(null);
const referenceOptions = computed(() => [
  { value: "", label: "从已有记录选择..." },
  ...references.value.map(item => ({
    value: item.id,
    label: `${item.herbName || item.referenceName || "标准品"}（${item.spectrumType || "HPLC"}）`
  }))
]);

const herbName = ref("");
const sampleCode = ref("");
const district = ref("");
const remark = ref("");

const mySamples = ref([]);
const myReferences = ref([]);

const showRefManager = ref(false);

let chartInstance = null;
let echartsLib = null;

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
  } catch (e) {}
  finally { loadingRefs.value = false; }
}

async function loadMySamples() { try { mySamples.value = (await api("/api/spectrum/my-samples")).items || []; } catch (e) {} }
async function loadMyReferences() { try { myReferences.value = (await api("/api/spectrum/my-references")).items || []; } catch (e) {} }

function handleSampleFile(e) {
  const f = e.target.files[0]; if (!f) return;
  sampleFile.value = f;
  sampleInfo.value = { name: f.name, size: (f.size / 1024).toFixed(1) + " KB" };
}

function handleReferenceFile(e) {
  const f = e.target.files[0]; if (!f) return;
  referenceFile.value = f;
  referenceInfo.value = { name: f.name, size: (f.size / 1024).toFixed(1) + " KB" };
  referenceId.value = "";
}

function selectReference() { referenceFile.value = null; referenceInfo.value = null; }

async function onRefSelect() {
  if (referenceId.value === "__clear__") {
    try { await api("/api/spectrum/my-references", { method: "DELETE" }); loadMyReferences(); loadReferences(); referenceId.value = ""; }
    catch (e) { notify?.(e.message); }
    return;
  }
  selectReference();
}

async function selectMySample(id) {
  if (id === "__clear__") {
    try { await api("/api/spectrum/my-samples", { method: "DELETE" }); loadMySamples(); }
    catch (e) { notify?.(e.message); }
    return;
  }
  if (!id) { sampleInfo.value = null; return; }
  sampleFile.value = null;
  sampleInfo.value = { name: "History #" + id.slice(0, 8), size: "-", _existingId: id };
  const s = mySamples.value.find(r => r.id === id);
  if (s) {
    if (s.herbName && !herbName.value) herbName.value = s.herbName;
    if (s.sampleCode && !sampleCode.value) sampleCode.value = s.sampleCode;
  }
}

async function runComparison() {
  if (!herbName.value.trim()) { errorMsg.value = "请填写药材名称"; return; }
  if (!sampleFile.value && !sampleInfo.value?._existingId) { errorMsg.value = "请上传样本 CSV 文件"; return; }
  if (!referenceFile.value && !referenceId.value) { errorMsg.value = "请选择标准品"; return; }
  errorMsg.value = "";
  comparing.value = true; result.value = null;

  try {
    const fd = new FormData();
    if (sampleFile.value) fd.append("sampleFile", sampleFile.value);
    if (sampleInfo.value?._existingId) fd.append("sampleId", sampleInfo.value._existingId);
    if (referenceFile.value) fd.append("referenceFile", referenceFile.value);
    if (referenceId.value) fd.append("referenceId", referenceId.value);
    fd.append("herbName", herbName.value.trim());
    fd.append("sampleCode", sampleCode.value.trim());
    fd.append("district", district.value.trim());
    fd.append("remark", remark.value.trim());
    fd.append("algorithm", "COSINE");
    fd.append("resolution", "2000");
    fd.append("diffThreshold", String(diffThreshold.value / 100));

    const resp = await api("/api/spectrum/compare", { method: "POST", body: fd });
    result.value = resp;
    notify?.("比对完成 - 相似度 " + resp.similarity + "%");
    loadMySamples();
    loadMyReferences();
    setTimeout(() => renderChart(resp), 100);
  } catch (e) { errorMsg.value = e.message; }
  finally { comparing.value = false; }
}

let lockedIdx = null; let chartData = null; let baselineFloor = 0; let currentRegions = [];

function isInDiff(xVal) {
  if (!currentRegions || !currentRegions.length) return false;
  return currentRegions.some(r => xVal >= r.xStart && xVal <= r.xEnd);
}

function renderChart(data) {
  if (!echartsLib || !chartDom.value) return;
  if (chartInstance) { chartInstance.dispose(); chartInstance = null; }
  chartInstance = echartsLib.init(chartDom.value);
  window.addEventListener("resize", () => chartInstance?.resize());
  lockedIdx = null; chartData = data;
  const sData = JSON.parse(data.alignedSample || "[]");
  const rData = JSON.parse(data.alignedReference || "[]");
  const xVals = sData.map(p => p.x);

  let maxSig = 0;
  for (let i = 0; i < sData.length; i++) { maxSig = Math.max(maxSig, Math.abs(sData[i].y), Math.abs(rData[i].y)); }
  baselineFloor = maxSig * 0.005;

  const rt = diffThreshold.value / 100;
  const isDiffArr = [];
  for (let i = 0; i < sData.length; i++) {
    const sig = Math.max(Math.abs(sData[i].y), Math.abs(rData[i].y));
    if (sig < baselineFloor) { isDiffArr.push(false); continue; }
    isDiffArr.push(Math.abs(sData[i].y - rData[i].y) / sig > rt);
  }
  currentRegions = [];
  let rStart = -1, rMax = 0;
  for (let i = 0; i < isDiffArr.length; i++) {
    if (isDiffArr[i]) {
      if (rStart < 0) rStart = i;
      rMax = Math.max(rMax, Math.abs(sData[i].y - rData[i].y) / Math.max(Math.abs(sData[i].y), Math.abs(rData[i].y)));
    } else {
      if (rStart >= 0 && i - rStart >= 3) currentRegions.push({ xStart: xVals[rStart], xEnd: xVals[i-1] });
      rStart = -1; rMax = 0;
    }
  }
  if (rStart >= 0 && isDiffArr.length - rStart >= 3) currentRegions.push({ xStart: xVals[rStart], xEnd: xVals[isDiffArr.length-1] });
  const markAreas = currentRegions.map(r => [
    { xAxis: r.xStart, itemStyle: { color: "rgba(199,90,56,0.12)", borderColor: "#c75a38", borderWidth: 1, borderType: "dashed" } },
    { xAxis: r.xEnd }
  ]);

  chartInstance.on("legendselectchanged", function(args) {
    const bothOn = args.selected["样本图谱"] !== false && args.selected["标准品图谱"] !== false;
    chartInstance.setOption({ series: [{}, {}, { markArea: { data: bothOn ? markAreas : [] } }] });
  });

  chartDom.value.onclick = function(evt) {
    const rect = chartDom.value.getBoundingClientRect();
    const px = evt.clientX - rect.left;
    const point = chartInstance.convertFromPixel({ seriesIndex: 0 }, [px, 0]);
    if (!point || isNaN(point[0])) { lockedIdx = null; applyLock(); return; }
    const cx = point[0];
    let lo = 0, hi = xVals.length - 1;
    while (lo < hi - 1) { const mid = (lo + hi) >> 1; if (xVals[mid] <= cx) lo = mid; else hi = mid; }
    const best = Math.abs(xVals[lo] - cx) <= Math.abs(xVals[hi] - cx) ? lo : hi;
    lockedIdx = (lockedIdx === best) ? null : best;
    applyLock();
  };

  chartInstance.setOption({
    tooltip: { trigger: "axis", triggerOn: "mousemove", axisPointer: { type: "none" }, alwaysShowContent: false,
      formatter: function(ps) {
        if (ps.length < 2) return "";
        const xv = parseFloat(ps[0].axisValue);
        const sig = Math.max(Math.abs(ps[0].data[1]), Math.abs(ps[1].data[1]));
        const d = Math.abs(ps[0].data[1] - ps[1].data[1]);
        const a = sig || 1; const pct = (d / a * 100).toFixed(1);
        const inDiff = isInDiff(xv);
        let reason = "";
        if (!inDiff) {
          if (sig < baselineFloor) reason = " (信号低于基线阈值)";
          else if (parseFloat(pct) < (diffThreshold.value || 6)) reason = " (偏差未达阈值)";
          else reason = " (不连续，未凑够3个差异点)";
        }
        return "<b>保留时间: " + xv.toFixed(2) + " </b><br/>"
          + ps[0].marker + " 样本图谱: " + ps[0].data[1].toFixed(4) + "<br/>"
          + ps[1].marker + " 标准品图谱: " + ps[1].data[1].toFixed(4) + "<br/>"
          + "<span style=\"color:" + (inDiff ? "#c75a38" : "#66736c") + "\">偏差: " + pct + "%" + (inDiff ? " (差异区域)" : reason) + "</span>";
      }
    },
    legend: { data: [
      { name: "样本图谱", textStyle: { color: "#157457", fontSize: 11, fontWeight: 700 } },
      { name: "标准品图谱", textStyle: { color: "#c75a38", fontSize: 11, fontWeight: 700 } }
    ], top: 6, itemWidth: 36, itemGap: 24, selectedMode: true },
    grid: { left: 60, right: 20, top: 45, bottom: 55 },
    xAxis: { type: "value", name: "保留时间 (min)", nameLocation: "center", nameGap: 35, axisLabel: { fontSize: 10, color: "#66736c" }, nameTextStyle: { color: "#66736c" } },
    yAxis: { type: "value", name: "归一化信号值", nameLocation: "center", nameGap: 50, axisLabel: { fontSize: 10, color: "#66736c" }, nameTextStyle: { color: "#66736c" } },
    dataZoom: [
      { type: "inside", zoomOnMouseWheel: true, moveOnMouseMove: true },
      { type: "slider", bottom: 8, height: 18, borderColor: "#e1e7e3", backgroundColor: "#f7f9f8", fillerColor: "rgba(21,116,87,0.12)" }
    ],
    toolbox: { feature: { restore: { title: "重置" }, saveAsImage: { title: "保存图片", pixelRatio: 2 } }, right: 8, top: 4 },
    series: [
      { name: "样本图谱", type: "line", data: sData.map(p => [p.x, p.y]), lineStyle: { color: "#157457", width: 1.8 }, symbol: "none", z: 2 },
      { name: "标准品图谱", type: "line", data: rData.map(p => [p.x, p.y]), lineStyle: { color: "#c75a38", width: 2, type: "dashed" }, symbol: "none", z: 1 },
      { name: "差异区域", type: "line", data: [], markArea: { silent: true, label: { show: false }, data: markAreas }, z: 0 }
    ]
  }, true);
}

function applyLock() {
  if (!chartInstance || !chartData) return;
  chartInstance.dispatchAction({ type: "hideTip" });
  if (lockedIdx == null) {
    chartInstance.setOption({ tooltip: { alwaysShowContent: false, triggerOn: "mousemove" }, series: [{ markLine: { data: [] } }, { markLine: { data: [] } }, {}] });
    return;
  }
  const xVals = JSON.parse(chartData.alignedSample || "[]").map(p => p.x);
  const xVal = xVals[lockedIdx];
  chartInstance.setOption({
    tooltip: { alwaysShowContent: true, triggerOn: "none" },
    series: [{ markLine: { silent: true, symbol: "none", animation: false, data: [{ xAxis: xVal, lineStyle: { color: "#94a3b8", width: 1, type: "dashed" }, label: { show: false } }] } }, { markLine: { data: [] } }, {}]
  });
  chartInstance.dispatchAction({ type: "showTip", seriesIndex: 0, dataIndex: lockedIdx });
}

function resetChart() { chartInstance?.dispatchAction({ type: "restore" }); }
function scoreClass(s) { return s >= 90 ? "pass" : s >= 80 ? "warn" : "fail"; }
function scoreColor(s) { return s >= 90 ? "#157457" : s >= 80 ? "#c75a38" : "#b93d3d"; }
function verdictText(s) {
  if (s >= 90) return "通过 — 高度一致，达到标准品水平";
  if (s >= 80) return "基本一致 — 存在可接受差异，建议复核";
  return "不一致 — 差异显著，建议重新检测";
}
function verdictClass(s) { return s >= 90 ? "pass" : s >= 80 ? "warn" : "fail"; }

onMounted(async () => {
  await loadECharts();
  await Promise.all([loadReferences(), loadMySamples(), loadMyReferences()]);
});
onUnmounted(() => { chartInstance?.dispose(); chartInstance = null; });
</script>

<template>
  <section class="panel resource-workbench">
    <div class="panel-head resource-head">
      <div><h2>图谱比对</h2><span>上传 HPLC 数据，自动计算相似度并生成叠加图谱</span></div>
      <button type="button" @click="router.push('/module/spectrum-comparisons')"><ArrowLeft :size="16" /> 返回图谱比对列表</button>
    </div>

    <!-- Metadata + CSV -->
    <div style="margin-bottom:14px">
      <div style="display:flex;align-items:flex-end;gap:10px;flex-wrap:wrap;margin-bottom:10px">
        <label style="font-size:12px;color:var(--ink);font-weight:700;display:grid;gap:3px">药材名称 *
          <input v-model="herbName" placeholder="如：黄连" style="width:110px;min-height:32px" />
        </label>
        <label style="font-size:12px;color:var(--muted);display:grid;gap:3px">样本编号
          <input v-model="sampleCode" placeholder="选填" style="width:130px;min-height:32px" />
        </label>
        <label style="font-size:12px;color:var(--muted);display:grid;gap:3px">区县
          <input v-model="district" placeholder="选填" style="width:100px;min-height:32px" />
        </label>
        <label style="font-size:12px;color:var(--muted);display:grid;gap:3px;flex:1;min-width:120px">备注
          <input v-model="remark" placeholder="选填" style="min-height:32px" />
        </label>
      </div>
      <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
        <div style="display:flex;align-items:center;gap:6px;min-width:0">
          <span style="font-size:12px;font-weight:700;white-space:nowrap;color:var(--ink)">样本</span>
          <label style="font-size:12px;font-weight:650;white-space:nowrap;cursor:pointer">
            <input type="file" accept=".csv,.txt" @change="handleSampleFile" style="width:180px" />
          </label>
          <select style="width:auto;min-width:140px;font-size:11px" @change="selectMySample($event.target.value)">
            <option value="" disabled selected>选择历史样本</option>
            <option v-if="mySamples.length" value="__clear__">清空历史样本</option>
            <option v-if="mySamples.length" disabled>──────────</option>
            <option v-for="s in mySamples" :key="s.id" :value="s.id">{{ s.herbName || '样本' }}{{ s.sampleCode ? ' - ' + s.sampleCode : '' }}</option>
          </select>
        </div>
        <div style="display:flex;align-items:center;gap:6px;min-width:0">
          <span style="font-size:12px;font-weight:700;white-space:nowrap;color:var(--ink)">标准品</span>
          <label style="font-size:12px;font-weight:650;white-space:nowrap;cursor:pointer">
            <input type="file" accept=".csv,.txt" @change="handleReferenceFile" style="width:180px" />
          </label>
          <span style="color:var(--muted);font-size:11px">或</span>
          <select v-model="referenceId" @change="onRefSelect" style="width:auto;min-width:180px">
            <option value="" disabled selected>选择标准品</option>
            <optgroup v-if="references.filter(r => r.status === 'REFERENCE').length" label="公共标准品">
              <option v-for="r in references.filter(r => r.status === 'REFERENCE')" :key="r.id" :value="r.id">{{ r.referenceName || r.herbName || '标准品' }}</option>
            </optgroup>
            <optgroup v-if="myReferences.length" label="我的标准品">
              <option value="__clear__">清空我的标准品</option>
              <option disabled>──────────</option>
              <option v-for="r in myReferences" :key="r.id" :value="r.id">{{ r.referenceName || r.herbName || '标准品' }}</option>
            </optgroup>
          </select>
          <button v-if="currentRole === 'admin'" class="button-secondary" style="min-height:28px;padding:0 8px;font-size:11px;white-space:nowrap" @click="showRefManager = true">管理标准品库</button>
        </div>
        <button :disabled="comparing || !herbName.trim()" @click="runComparison" style="flex-shrink:0">
          <RotateCcw v-if="comparing" :size="16" style="animation:spin 1s linear infinite" />
          <FlaskConical v-else :size="16" />
          {{ comparing ? "比对中..." : "开始比对" }}
        </button>
        <label style="display:flex;align-items:center;gap:6px;font-size:12px;color:var(--muted);white-space:nowrap">
          差异阈值
          <input type="number" v-model.number="diffThreshold" min="1" max="50" step="1" style="width:56px;min-height:30px;text-align:center" /> %
        </label>
      </div>
      <div v-if="errorMsg" style="margin-top:8px;padding:8px 12px;background:#fef2f2;color:#b93d3d;border:1px solid #fecaca;border-radius:6px;font-size:12px">{{ errorMsg }}</div>
    </div>

    <ReferenceManager v-if="showRefManager" @close="showRefManager = false" @changed="loadReferences(); loadMyReferences()" />

    <!-- Chart -->
    <div class="panel" style="margin-bottom:14px;min-height:420px;display:flex;flex-direction:column">
      <div class="panel-head">
        <h2>HPLC 图谱叠加比对</h2>
        <span style="font-size:10px;color:var(--muted)">绿 = 样本 &middot; 红 = 标准品 &middot; 差异 = 偏差 &gt; {{ diffThreshold }}% 的区域 &middot; 点击图例可切换</span>
        <span style="flex:1"></span>
        <button class="button-secondary" style="min-height:30px;padding:0 10px;font-size:11px" @click="resetChart"><RotateCcw :size="13" /> 重置</button>
      </div>
      <div style="flex:1;position:relative;min-height:380px">
        <div v-if="!result && !comparing" style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;color:var(--muted);font-size:13px">请上传数据并点击开始比对</div>
        <div v-else-if="comparing" style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;color:var(--muted);font-size:13px">比对中...</div>
        <div ref="chartDom" v-show="result" style="position:absolute;inset:0"></div>
      </div>
    </div>

    <!-- Result -->
    <div v-if="result" class="panel">
      <div class="panel-head"><h2>比对结果</h2></div>
      <div style="display:flex;gap:18px;align-items:flex-start;flex-wrap:wrap">
        <div :style="{ width:'82px', height:'82px', borderRadius:'50%', display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', fontWeight:700, flexShrink:0,
          background: scoreClass(result.similarity) === 'pass' ? '#e8f4ee' : scoreClass(result.similarity) === 'warn' ? '#fdf3eb' : '#fef2f2',
          color: scoreColor(result.similarity),
          border: '3px solid ' + (scoreClass(result.similarity) === 'pass' ? '#b6ddcc' : scoreClass(result.similarity) === 'warn' ? '#f5ceb3' : '#f5c2c2') }">
          <span style="font-size:21px;line-height:1">{{ result.similarity }}%</span>
          <span style="font-size:9px;opacity:.7;margin-top:2px">相似度</span>
        </div>
        <div style="min-width:200px;flex:1">
          <div :style="{ fontSize:'15px', fontWeight:700, color: scoreColor(result.similarity), marginBottom:'4px' }">{{ verdictText(result.similarity) }}</div>
          <div style="font-size:11px;color:var(--muted);line-height:1.7">
            Algorithm: {{ result.algorithm }} &middot; Points: {{ result.comparedPoints }} &middot; X: {{ result.xMin?.toFixed(1) || '?' }} ~ {{ result.xMax?.toFixed(1) || '?' }} min
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style>
.back-link { display: inline-flex; align-items: center; gap: 6px; background: none; border: none; color: #66736c; cursor: pointer; font-size: 13px; font-weight: 600; min-height: auto; padding: 4px 0; box-shadow: none; }
.back-link:hover { color: #157457; background: none; box-shadow: none; }
.upload-ok { padding: 8px 12px; border-radius: 7px; font-size: 12px; background: #e8f4ee; color: #157457; border: 1px solid #b6ddcc; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
