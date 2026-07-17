<script setup>
import { computed, inject, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ArrowLeft, BarChart3, RefreshCw, Save, TrendingUp } from "lucide-vue-next";
import gsap from "gsap";
import AppSelect from "@/components/AppSelect.vue";
import { api } from "@/services/api";
import { useAnalysisAnimation } from "@/composables/useAnalysisAnimation";

const router = useRouter();
const notify = inject("notify");
const currentUser = inject("currentUser");

// ── State ──
const computing = ref(false);
const saving = ref(false);
const batchOptions = ref([]);
const selectedBatchId = ref("");
const batchInfo = ref(null);
const result = ref(null);
const conclusion = ref("");
const verdict = ref("");
const suggestions = ref("");
const analysisName = ref("");
const analyzeButtonRef = ref(null);
const stepsContainerRef = ref(null);
const conclusionPanelRef = ref(null);
const stepIndex = ref(0);

// Default optimal indicators (editable)
const indicators = ref([
  { field: "temperature", label: "温度", unit: "°C", optMin: 15, optMax: 25 },
  { field: "humidity",    label: "湿度", unit: "%",  optMin: 55, optMax: 75 },
  { field: "soil_ph",     label: "土壤pH", unit: "",  optMin: 6.0, optMax: 7.0 }
]);

// ── Load batches ──
onMounted(async () => {
  try {
    const data = await api("/api/herb-batches");
    batchOptions.value = (data.items || []).map(b => ({
      value: b.id,
      label: `${b.batchCode || b.id} · ${b.batchName || ""}`
    }));
  } catch (e) { notify(e.message); }
});

// ── Watch batch selection → load preview ──
watch(selectedBatchId, async (bid) => {
  if (!bid) { batchInfo.value = null; return; }
  try {
    const data = await api(`/api/herb-batches/${bid}/growth-summary`);
    batchInfo.value = {
      id: data.id || bid,
      batchName: data.batchName || "",
      batchCode: data.batchCode || "",
      herbName: data.herbName || "",
      district: data.district || "",
      responsiblePerson: data.responsiblePerson || "",
      dateStart: data.dateStart || "",
      dateEnd: data.dateEnd || "",
      recordCount: data.recordCount || 0,
      stageCount: data.stageCount || 0
    };
    result.value = null;
    // 从指标标准加载该药材的生长区间
    if (data.herbId) {
      try {
        const m = await api(`/api/quality-metrics/herb-metrics/${data.herbId}`);
        const metrics = m.metrics || [];
        for (const ind of indicators.value) {
          const cfg = metrics.find(x => x.metricCode === ({temperature:'GROWTH_TEMP_AVG',humidity:'GROWTH_HUMIDITY_AVG',soil_ph:'GROWTH_SOIL_PH_AVG'})[ind.field]);
          if (cfg) {
            if (cfg.minimumValue != null) ind.optMin = cfg.minimumValue;
            if (cfg.maximumValue != null) ind.optMax = cfg.maximumValue;
          }
        }
      } catch (e) {}
    }
  } catch (e) {
    batchInfo.value = null;
    notify(e.message);
  }
});

// ── Run analysis ──
async function runAnalysis() {
  if (!selectedBatchId.value || !batchInfo.value) return;
  if (batchInfo.value.recordCount < 5) {
    notify("该批次数据不足（至少需要5条记录），无法分析"); return;
  }
  computing.value = true;
  result.value = null;
  stepIndex.value = 0;
  animateButtonStart();

  try {
    const body = {
      batchId: selectedBatchId.value,
      indicators: indicators.value.map(ind => ({
        field: ind.field, label: ind.label, unit: ind.unit,
        optMin: Number(ind.optMin), optMax: Number(ind.optMax)
      }))
    };
    const data = await api("/api/growth-analysis/compute", {
      method: "POST", body: JSON.stringify(body)
    });
    result.value = data;
    analysisName.value = `${data.herbName}-${data.batchName}-生长分析`;
    parseConclusion(data.conclusion || "");
    animateButtonComplete(true);
    notify("分析完成");
  } catch (e) {
    notify(e.message);
    animateButtonComplete(false);
  }
  finally { computing.value = false; }
}

// ── Save analysis ──
async function saveAnalysis() {
  if (!result.value) return;
  saving.value = true;
  try {
    const body = {
      batchId: result.value.batchId,
      batchName: result.value.batchName,
      analysisName: analysisName.value,
      herbName: result.value.herbName,
      district: result.value.district,
      conclusion: buildConclusionText(),
      analysisConfigJson: {
        indicators: result.value.indicators,
        dateRange: { start: result.value.dateStart, end: result.value.dateEnd }
      },
      trendDataJson: result.value.trendData,
      suitabilityJson: result.value.suitability,
      recordCount: result.value.recordCount,
      stageCount: result.value.stageCount
    };
    await api("/api/growth-analysis/save", { method: "POST", body: JSON.stringify(body) });
    notify("分析结果已保存");
  } catch (e) { notify(e.message); }
  finally { saving.value = false; }
}

// ── Helpers ──
function colorFor(ind) {
  return { temperature: "#e67e22", humidity: "#3498db", soil_ph: "#2c7a4a" }[ind] || "#635bff";
}
function trendBadge(dir) {
  if (dir === "上升") return { cls: "warn", icon: "↑" };
  if (dir === "下降") return { cls: "bad", icon: "↓" };
  return { cls: "stable", icon: "→" };
}
function okBadge(rate) {
  if (rate >= 80) return "good";
  if (rate >= 60) return "warn";
  return "bad";
}

// Split conclusion text into verdict + suggestions
function parseConclusion(text) {
  const vIdx = text.indexOf("综合判断：");
  const sIdx = text.indexOf("改进建议：");
  if (vIdx >= 0 && sIdx >= 0) {
    verdict.value = text.substring(vIdx + 5, sIdx).trim();
    suggestions.value = text.substring(sIdx + 5).trim();
  } else if (vIdx >= 0) {
    verdict.value = text.substring(vIdx + 5).trim();
    suggestions.value = "";
  } else {
    verdict.value = text;
    suggestions.value = "";
  }
}

// Combine verdict + suggestions into full conclusion text
function buildConclusionText() {
  let parts = [];
  if (verdict.value.trim()) parts.push("综合判断：" + verdict.value.trim());
  if (suggestions.value.trim()) parts.push("改进建议：" + suggestions.value.trim());
  return parts.join("\n\n") || conclusion.value;
}

// Chart drawing is now handled by useAnalysisAnimation composable (A2–A5)

// ── Animated Canvas line chart (A2) ──
function drawTrendAnimated(canvasEl, values, optMin, optMax, unit, color) {
  const dpr = window.devicePixelRatio || 1;
  const W = canvasEl.parentElement.clientWidth - 32;
  const H = 240;
  canvasEl.width = W * dpr; canvasEl.height = H * dpr;
  canvasEl.style.width = W + "px"; canvasEl.style.height = H + "px";
  const ctx = canvasEl.getContext("2d");
  ctx.scale(dpr, dpr);

  const pad = { top: 20, right: 20, bottom: 35, left: 50 };
  const pw = W - pad.left - pad.right, ph = H - pad.top - pad.bottom;
  const n = values.length;
  const yMin = Math.min(optMin, ...values) - 1;
  const yMax = Math.max(optMax, ...values) + 1;
  const yLo = pad.top + ph * (1 - (optMin - yMin) / (yMax - yMin));
  const yHi = pad.top + ph * (1 - (optMax - yMin) / (yMax - yMin));

  const progress = { value: 0 };

  function drawFrame() {
    ctx.clearRect(0, 0, W, H);
    const p = progress.value;

    // Optimal band fades in
    ctx.fillStyle = `rgba(46,204,113,${0.12 * p})`;
    ctx.fillRect(pad.left, Math.min(yLo, yHi), pw, Math.abs(yHi - yLo));
    if (p > 0.1) {
      ctx.fillStyle = `rgba(46,204,113,${0.5 * p})`;
      ctx.font = "10px sans-serif";
      ctx.fillText("最优", pad.left + 4, Math.min(yLo, yHi) - 4);
    }

    // Grid (always visible)
    ctx.strokeStyle = "#eee"; ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = pad.top + ph * (i / 4);
      ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(W - pad.right, y); ctx.stroke();
    }
    ctx.fillStyle = "#888"; ctx.font = "10px sans-serif"; ctx.textAlign = "right";
    for (let i = 0; i <= 4; i++) {
      ctx.fillText((yMin + (yMax - yMin) * (1 - i / 4)).toFixed(1), pad.left - 6, pad.top + ph * (i / 4) + 3);
    }

    // Line draws progressively
    const pointCount = Math.max(1, Math.floor(p * n));
    ctx.strokeStyle = color; ctx.lineWidth = 2; ctx.beginPath();
    for (let i = 0; i < pointCount; i++) {
      const x = pad.left + pw * (i / Math.max(n - 1, 1));
      const y = pad.top + ph * (1 - (values[i] - yMin) / (yMax - yMin));
      if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    }
    ctx.stroke();

    // Dots
    ctx.fillStyle = color;
    for (let i = 0; i < pointCount; i++) {
      const x = pad.left + pw * (i / Math.max(n - 1, 1));
      const y = pad.top + ph * (1 - (values[i] - yMin) / (yMax - yMin));
      ctx.beginPath(); ctx.arc(x, y, 3, 0, Math.PI * 2); ctx.fill();
    }

    // X labels
    ctx.fillStyle = "#888"; ctx.textAlign = "center";
    const step = Math.max(1, Math.floor(n / 6));
    for (let i = 0; i < pointCount; i += step) {
      ctx.fillText(i + 1, pad.left + pw * (i / Math.max(n - 1, 1)), pad.top + ph + 16);
    }
  }

  gsap.to(progress, {
    value: 1,
    duration: 1.2,
    ease: "power2.inOut",
    onUpdate: drawFrame,
    onComplete: drawFrame,
  });
}

// ── Chart drawing (Canvas, no dependencies) ──
function drawTrendChart(canvasId, values, optMin, optMax, unit, color) {
  setTimeout(() => {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const dpr = window.devicePixelRatio || 1;
    const W = canvas.parentElement.clientWidth - 32;
    const H = 240;
    canvas.width = W * dpr; canvas.height = H * dpr;
    canvas.style.width = W + "px"; canvas.style.height = H + "px";
    const ctx = canvas.getContext("2d");
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, W, H);

    const pad = { top: 20, right: 20, bottom: 35, left: 50 };
    const pw = W - pad.left - pad.right, ph = H - pad.top - pad.bottom;
    const n = values.length;
    let yMin = Math.min(optMin, ...values) - 1;
    let yMax = Math.max(optMax, ...values) + 1;

    // Optimal band
    const yLo = pad.top + ph * (1 - (optMin - yMin) / (yMax - yMin));
    const yHi = pad.top + ph * (1 - (optMax - yMin) / (yMax - yMin));
    ctx.fillStyle = "rgba(46,204,113,0.12)";
    ctx.fillRect(pad.left, Math.min(yLo, yHi), pw, Math.abs(yHi - yLo));
    ctx.fillStyle = "rgba(46,204,113,0.5)";
    ctx.font = "10px sans-serif";
    ctx.fillText("最优", pad.left + 4, Math.min(yLo, yHi) - 4);

    // Grid
    ctx.strokeStyle = "#eee"; ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = pad.top + ph * (i / 4);
      ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(W - pad.right, y); ctx.stroke();
    }
    ctx.fillStyle = "#888"; ctx.font = "10px sans-serif"; ctx.textAlign = "right";
    for (let i = 0; i <= 4; i++) {
      ctx.fillText((yMin + (yMax - yMin) * (1 - i / 4)).toFixed(1), pad.left - 6, pad.top + ph * (i / 4) + 3);
    }

    // Line
    ctx.strokeStyle = color; ctx.lineWidth = 2; ctx.beginPath();
    values.forEach((v, i) => {
      const x = pad.left + pw * (i / (n - 1));
      const y = pad.top + ph * (1 - (v - yMin) / (yMax - yMin));
      if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    });
    ctx.stroke();

    // Dots
    ctx.fillStyle = color;
    values.forEach((v, i) => {
      const x = pad.left + pw * (i / (n - 1));
      const y = pad.top + ph * (1 - (v - yMin) / (yMax - yMin));
      ctx.beginPath(); ctx.arc(x, y, 3, 0, Math.PI * 2); ctx.fill();
    });

    // X labels
    ctx.fillStyle = "#888"; ctx.textAlign = "center";
    const step = Math.max(1, Math.floor(n / 6));
    for (let i = 0; i < n; i += step) {
      ctx.fillText(i + 1, pad.left + pw * (i / (n - 1)), pad.top + ph + 16);
    }
  }, 100);
}
// ── Animation composable ──
const { animateButtonStart, animateButtonComplete } = useAnalysisAnimation({
  computing,
  result,
  analyzeButtonRef,
  stepsContainerRef,
  conclusionPanelRef,
  drawTrendAnimated,
  stepIndex,
});
</script>

<template>
  <section class="panel resource-workbench">
    <div class="panel-head resource-head">
      <div><h2>生长数据分析</h2><span>{{ false ? '查看已保存的分析报告' : '选择批次，配置最优指标，自动生成趋势和适宜性分析' }}</span></div>
      <div style="display:flex;gap:8px">
        <button class="button-secondary" @click="router.push('/module/growth-analysis')">
          <ArrowLeft :size="16" />返回列表
        </button>
      </div>
    </div>

    <!-- Loading -->
    <!-- Config Panel -->
    <div class="panel" style="margin-bottom:20px">
      <div class="panel-head">
        <div><h2>分析配置</h2><span>选择批次并设置各指标的最优区间</span></div>
      </div>

      <!-- Batch selector -->
      <div style="display:flex;gap:16px;align-items:flex-end;flex-wrap:wrap;margin-bottom:16px">
        <div style="min-width:300px">
          <label style="font-size:13px;color:var(--muted);display:block;margin-bottom:4px">选择药材批次</label>
          <AppSelect v-model="selectedBatchId" :options="batchOptions" placeholder="请选择批次..." />
        </div>
        <button v-if="!batchInfo && selectedBatchId" disabled style="opacity:0.5">
          <RefreshCw :size="15" /> 加载中...
        </button>
      </div>

      <!-- Batch info card (inside config panel) -->
      <div v-if="batchInfo" style="margin-bottom:16px">
        <div style="background:var(--surface-subtle, #f6f9fc);border-radius:8px;padding:16px 20px">
          <div>
            <span style="font-size:12px;color:var(--muted)">{{ batchInfo.herbName }} · {{ batchInfo.district }}</span>
            <h3 style="margin:2px 0;font-size:18px;font-weight:600">{{ batchInfo.batchName || batchInfo.batchCode }}</h3>
            <p v-if="batchInfo.responsiblePerson" style="margin:0;font-size:12px;color:var(--muted)">负责人 {{ batchInfo.responsiblePerson }}</p>
          </div>
          <div style="display:flex;gap:32px;margin-top:14px;padding-top:12px;border-top:1px solid var(--line, #e0e0e0)">
            <div style="display:flex;flex-direction:column">
              <span style="font-size:12px;color:var(--muted)">生长记录</span>
              <strong style="font-size:16px;font-weight:600">{{ batchInfo.recordCount }} 条</strong>
            </div>
            <div style="display:flex;flex-direction:column">
              <span style="font-size:12px;color:var(--muted)">生长阶段</span>
              <strong style="font-size:16px;font-weight:600">{{ batchInfo.stageCount }} 个</strong>
            </div>
            <div style="display:flex;flex-direction:column">
              <span style="font-size:12px;color:var(--muted)">时间范围</span>
              <strong style="font-size:14px;font-weight:600">{{ batchInfo.dateStart }} ~ {{ batchInfo.dateEnd }}</strong>
            </div>
          </div>
        </div>
      </div>

      <!-- Insufficient data warning -->
      <div v-if="batchInfo && batchInfo.recordCount > 0 && batchInfo.recordCount < 5" style="padding:12px;background:#fff3cd;border-radius:6px;font-size:13px;color:#856404;margin-bottom:12px">
        该批次仅有 {{ batchInfo.recordCount }} 条生长数据，至少需要 5 条记录才能进行有效分析。请通过"生长采集"模块补充更多数据。
      </div>
      <div v-if="batchInfo && batchInfo.recordCount === 0" style="padding:12px;background:#fff3cd;border-radius:6px;font-size:13px;color:#856404;margin-bottom:12px">
        该批次暂无生长数据记录，请先通过"生长采集"模块录入数据后再进行分析。
      </div>

      <!-- Indicator config table -->
      <table v-if="batchInfo" style="margin-bottom:12px">
        <thead>
          <tr><th>指标</th><th>单位</th><th>最优下限</th><th>最优上限</th></tr>
        </thead>
        <tbody>
          <tr v-for="(ind, i) in indicators" :key="ind.field">
            <td><strong>{{ ind.label }}</strong></td>
            <td>{{ ind.unit || "—" }}</td>
            <td><input v-model.number="ind.optMin" type="number" step="0.1" style="width:100px" /></td>
            <td><input v-model.number="ind.optMax" type="number" step="0.1" style="width:100px" /></td>
          </tr>
        </tbody>
      </table>

      <button
        ref="analyzeButtonRef"
        :disabled="!batchInfo || batchInfo.recordCount < 5 || computing"
        @click="runAnalysis"
        style="min-width:140px"
        :class="{ 'analysis-computing': computing }"
      >
        <TrendingUp :size="16" />{{ computing ? "分析中..." : "开始分析" }}
      </button>

      <!-- Step indicator (A1) -->
      <div ref="stepsContainerRef" v-if="computing" class="analysis-steps">
        <div class="analysis-step-item" :class="{ done: stepIndex >= 1 }">
          <span class="step-dot"></span>读取生长数据
        </div>
        <div class="analysis-step-item" :class="{ done: stepIndex >= 2 }">
          <span class="step-dot"></span>计算趋势指标
        </div>
        <div class="analysis-step-item" :class="{ done: stepIndex >= 3 }">
          <span class="step-dot"></span>生成适宜性报告
        </div>
      </div>
    </div>

    <!-- Results (same layout as report view) -->
    <template v-if="result">
      <!-- Per-indicator cards (trend + suitability combined) -->
      <div v-for="t in result.trendData" :key="t.indicator" class="panel analysis-result-card" style="margin-bottom:16px">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;flex-wrap:wrap;gap:8px">
          <div style="display:flex;align-items:center;gap:10px">
            <span :style="{width:'10px',height:'10px',borderRadius:'50%',background:colorFor(t.indicator),display:'inline-block'}"></span>
            <strong style="font-size:15px">{{ t.label }}</strong>
            <span class="metric-badge" :class="trendBadge(t.direction).cls">{{ trendBadge(t.direction).icon }} {{ t.direction }}</span>
            <template v-if="result.suitability.length">
              <span v-for="s in result.suitability.filter(x => x.indicator === t.indicator)" :key="s.indicator"
                class="metric-badge" :class="okBadge(s.okRate)" style="font-size:11px">达标 {{ s.okRate }}%</span>
            </template>
          </div>
          <div style="display:flex;gap:18px;font-size:12px;color:var(--muted)">
            <span>均值 <strong style="color:var(--ink)">{{ t.mean }}{{ t.unit }}</strong></span>
            <span>CV <strong style="color:var(--ink)">{{ t.cv }}%</strong></span>
            <span>范围 <strong style="color:var(--ink)">{{ t.rangeStart }} ~ {{ t.rangeEnd }}{{ t.unit }}</strong></span>
          </div>
        </div>
        <div style="display:flex;gap:16px;flex-wrap:wrap">
          <div style="flex:1;min-width:300px;max-width:520px;background:var(--surface-subtle,#f8f9fa);border-radius:8px;padding:10px">
            <canvas :id="'chart-' + t.indicator"></canvas>
          </div>
          <!-- Suitability panel -->
          <div v-for="s in result.suitability.filter(x => x.indicator === t.indicator)" :key="'s'+s.indicator"
            style="flex:1;min-width:220px;display:flex;flex-direction:column;gap:8px">
            <div style="font-size:11px;color:var(--muted);font-weight:600">适宜性分析</div>


            <!-- Stacked bar -->
            <div style="background:var(--surface-subtle,#f6f9fc);border-radius:8px;padding:12px 14px">
              <div style="display:flex;justify-content:space-between;font-size:11px;color:var(--muted);margin-bottom:5px">
                <span style="color:#3498db">▼ 偏低 {{ s.loRate }}%</span>
                <span style="color:#2ecc71">达标 {{ s.okRate }}%</span>
                <span style="color:#e74c3c">偏高 {{ s.hiRate }}% ▲</span>
              </div>
              <div class="gauge-stacked-bar" style="height:22px;border-radius:11px;overflow:hidden;display:flex;background:#eee">
                <div v-if="s.loRate > 1" class="gauge-segment" :style="{width:s.loRate+'%',background:'#3498db'}"></div>
                <div class="gauge-segment" :style="{width:s.okRate+'%',background:'#2ecc71'}"></div>
                <div v-if="s.hiRate > 1" class="gauge-segment" :style="{width:s.hiRate+'%',background:'#e74c3c'}"></div>
              </div>
              <div style="display:flex;justify-content:space-between;margin-top:5px;font-size:10px;color:var(--muted)">
                <span>{{ s.lo }}条</span><span>{{ s.okRate > 0 ? 20 - s.lo - s.hi : 0 }}条</span><span>{{ s.hi }}条</span>
              </div>
            </div>

            <!-- Deviation summary -->
            <div style="font-size:12px;color:var(--muted);line-height:1.6">
              平均偏离 <strong style="color:var(--ink)">{{ s.avgDeviation }}{{ t.unit }}</strong>
            </div>

            <!-- Stage bars -->
            <div v-if="s.stageDetail && s.stageDetail.length" style="margin-top:4px">
              <div style="font-size:11px;color:var(--muted);margin-bottom:6px;font-weight:600">分阶段达标率</div>
              <div v-for="sd in s.stageDetail" :key="sd.stage"
                style="display:flex;align-items:center;gap:8px;margin-bottom:5px">
                <span style="font-size:11px;width:60px;text-align:right;color:var(--muted);white-space:nowrap">{{ sd.stage }}</span>
                <div style="flex:1;height:14px;border-radius:7px;overflow:hidden;display:flex;background:#eee">
                  <div class="stage-compliance-bar-fill" :style="{
                    width: (100 - sd.badRate) + '%',
                    background: sd.badRate >= 50 ? '#e74c3c' : sd.badRate > 0 ? '#f0ad4e' : '#2ecc71',
                    borderRadius: '7px'
                  }"></div>
                </div>
                <span style="font-size:11px;width:36px;font-weight:600"
                  :style="{color: sd.badRate >= 50 ? '#c62828' : sd.badRate > 0 ? '#e67e22' : '#2e7d32'}">
                  {{ 100 - sd.badRate }}%
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Conclusion: split into verdict + suggestions -->
      <div ref="conclusionPanelRef" class="panel" style="margin-bottom:20px">
        <div class="panel-head">
          <div><h2>分析结论</h2><span>自动生成，可编辑后保存</span></div>
        </div>
        <div style="margin-bottom:12px">
          <label style="font-size:13px;color:var(--muted);display:block;margin-bottom:4px">分析名称</label>
          <input v-model="analysisName" style="max-width:420px" />
        </div>
        <div style="display:flex;flex-direction:column;gap:14px;margin-bottom:12px">
          <div>
            <label style="font-size:13px;color:var(--muted);display:block;margin-bottom:4px">综合判断</label>
            <textarea v-model="verdict" rows="6" style="width:100%;font-size:13px;line-height:1.7"
              placeholder="综合判断..."></textarea>
          </div>
          <div>
            <label style="font-size:13px;color:var(--muted);display:block;margin-bottom:4px">改进建议</label>
            <textarea v-model="suggestions" rows="8" style="width:100%;font-size:13px;line-height:1.7"
              placeholder="改进建议..."></textarea>
          </div>
        </div>
        <div style="display:flex;gap:12px">
          <button @click="saveAnalysis" :disabled="saving">
            <Save :size="16" />{{ saving ? "保存中..." : "保存分析结果" }}
          </button>
          <button class="button-secondary" @click="router.push('/module/growth-analysis')">返回列表</button>
        </div>
      </div>
    </template>

    <!-- Empty state -->
    <div v-if="!batchInfo" class="panel" style="text-align:center;padding:60px 20px">
      <BarChart3 :size="40" style="color:var(--muted);margin-bottom:12px" />
      <p style="color:var(--muted);font-size:15px">请先选择一个药材批次开始分析</p>
      <p style="color:var(--muted);font-size:13px;margin-top:4px">系统将自动计算生长趋势和适宜性偏差</p>
    </div>
  </section>
</template>

<style scoped>
/* ── A1: Step indicator ── */
.analysis-steps {
  display: flex;
  gap: 20px;
  margin-top: 12px;
  overflow: hidden;
}
.analysis-step-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--muted, #888);
  opacity: 0.4;
  transition: opacity 0.3s, color 0.3s;
}
.analysis-step-item.done {
  opacity: 1;
  color: #2ecc71;
}
.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--muted, #aaa);
  transition: background 0.3s, box-shadow 0.3s;
}
.analysis-step-item.done .step-dot {
  background: #2ecc71;
  box-shadow: 0 0 6px rgba(46, 204, 113, 0.5);
}

/* ── A1: Button states ── */
.analysis-computing {
  box-shadow: 0 0 0 3px rgba(99, 91, 255, 0.2);
  transition: box-shadow 0.3s;
}
.analysis-success-flash {
  box-shadow: 0 0 16px rgba(46, 204, 113, 0.55) !important;
}
</style>
