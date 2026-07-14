<script setup>
import { inject, onMounted, ref, watch, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, BarChart3, RefreshCw, Save, TrendingUp, Target, Activity } from "lucide-vue-next";
import { api } from "@/services/api";

const route = useRoute();
const router = useRouter();
const notify = inject("notify");

const loading = ref(true);
const saving = ref(false);
const report = ref(null);
const batchInfo = ref(null);
const conclusion = ref("");
const verdict = ref("");
const suggestions = ref("");
const analysisName = ref("");
const notFound = ref(false);

async function load() {
  loading.value = true;
  try {
    const detail = await api(`/api/growth-analysis/${route.params.id}/detail`);
    if (!detail || !detail.id) { notFound.value = true; return; }

    batchInfo.value = {
      herbName: detail.herbName || "",
      district: detail.district || "",
      batchName: detail.batchName || "",
      dateStart: detail.analysisConfig?.dateRange?.start || "",
      dateEnd: detail.analysisConfig?.dateRange?.end || "",
      recordCount: detail.recordCount || 0
    };

    report.value = {
      id: detail.id,
      analysisName: detail.analysisName || "",
      analystName: detail.analystName || detail.analyst || "",
      analyzedAt: (detail.analyzedAt || "").replace("T", " ").substring(0, 16),
      indicators: detail.analysisConfig?.indicators || [],
      trendData: detail.trendData || [],
      suitability: detail.suitability || [],
      hasData: !!(detail.trendData && detail.trendData.length)
    };

    conclusion.value = detail.conclusion || "";
    parseParts(detail.conclusion || "");
    analysisName.value = detail.analysisName || "";
  } catch (e) {
    notify(e.message);
    notFound.value = true;
  } finally { loading.value = false; }
}

async function saveConclusion() {
  saving.value = true;
  try {
    await api("/api/growth-analysis", {
      method: "PUT",
      body: JSON.stringify({
        id: report.value.id,
        analysisName: analysisName.value,
        conclusion: buildParts(),
        version: 0
      })
    });
    notify("已保存");
  } catch (e) { notify(e.message); }
  finally { saving.value = false; }
}

// Helpers
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

function parseParts(text) {
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
function buildParts() {
  let p = [];
  if (verdict.value.trim()) p.push("综合判断：" + verdict.value.trim());
  if (suggestions.value.trim()) p.push("改进建议：" + suggestions.value.trim());
  return p.join("\n\n") || conclusion.value;
}
function colorFor(indicator) {
  return { temperature: "#e67e22", humidity: "#3498db", soil_ph: "#2c7a4a" }[indicator] || "#635bff";
}

// ── Chart drawing ──
function drawTrendChart(canvasId, values, dates, optMin, optMax, color) {
  nextTick(() => {
    setTimeout(() => {
      const canvas = document.getElementById(canvasId);
      if (!canvas) return;
      const dpr = window.devicePixelRatio || 1;
      const container = canvas.parentElement;
      const W = container.clientWidth - 16;
      const H = 220;
      canvas.width = W * dpr; canvas.height = H * dpr;
      canvas.style.width = W + "px"; canvas.style.height = H + "px";
      const ctx = canvas.getContext("2d");
      ctx.scale(dpr, dpr);
      ctx.clearRect(0, 0, W, H);

      const pad = { top: 16, right: 16, bottom: 32, left: 48 };
      const pw = W - pad.left - pad.right, ph = H - pad.top - pad.bottom;
      const n = values.length;
      let yMin = Math.min(optMin, ...values) - 1;
      let yMax = Math.max(optMax, ...values) + 1;

      // Optimal band
      const yLo = pad.top + ph * (1 - (optMin - yMin) / (yMax - yMin));
      const yHi = pad.top + ph * (1 - (optMax - yMin) / (yMax - yMin));
      ctx.fillStyle = "rgba(46,204,113,0.13)";
      ctx.fillRect(pad.left, Math.min(yLo, yHi), pw, Math.abs(yHi - yLo));
      ctx.fillStyle = "rgba(46,204,113,0.55)";
      ctx.font = "10px sans-serif"; ctx.textAlign = "left";
      ctx.fillText("最优区间", pad.left + 6, Math.min(yLo, yHi) - 5);

      // Grid
      ctx.strokeStyle = "#e8e8e8"; ctx.lineWidth = 0.5;
      for (let i = 0; i <= 4; i++) {
        const y = pad.top + ph * (i / 4);
        ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(W - pad.right, y); ctx.stroke();
        ctx.fillStyle = "#999"; ctx.font = "10px sans-serif"; ctx.textAlign = "right";
        ctx.fillText((yMin + (yMax - yMin) * (1 - i / 4)).toFixed(1), pad.left - 6, y + 3);
      }

      // Line
      ctx.strokeStyle = color; ctx.lineWidth = 2; ctx.beginPath();
      values.forEach((v, i) => {
        const x = pad.left + pw * (i / Math.max(n - 1, 1));
        const y = pad.top + ph * (1 - (v - yMin) / (yMax - yMin));
        if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
      });
      ctx.stroke();

      // Dots
      ctx.fillStyle = color;
      values.forEach((v, i) => {
        const x = pad.left + pw * (i / Math.max(n - 1, 1));
        const y = pad.top + ph * (1 - (v - yMin) / (yMax - yMin));
        ctx.beginPath(); ctx.arc(x, y, 2.5, 0, Math.PI * 2); ctx.fill();
      });

      // X-axis date labels
      ctx.fillStyle = "#999"; ctx.font = "9px sans-serif"; ctx.textAlign = "center";
      const step = Math.max(1, Math.floor(n / 6));
      for (let i = 0; i < n; i += step) {
        const label = dates && dates[i] ? dates[i].slice(5) : (i + 1);
        ctx.fillText(label, pad.left + pw * (i / Math.max(n - 1, 1)), pad.top + ph + 18);
      }
    }, 150);
  });
}

function drawGauge(canvasId, okRate, loRate, hiRate) {
  nextTick(() => {
    setTimeout(() => {
      const canvas = document.getElementById(canvasId);
      if (!canvas) return;
      const dpr = window.devicePixelRatio || 1;
      const W = canvas.parentElement.clientWidth - 16;
      canvas.width = W * dpr; canvas.height = 18 * dpr;
      canvas.style.width = W + "px"; canvas.style.height = "18px";
      const ctx = canvas.getContext("2d");
      ctx.scale(dpr, dpr);
      let x = 0;
      if (loRate > 1) { ctx.fillStyle = "#3498db"; const w = W * loRate / 100; ctx.fillRect(x, 2, w, 12); x += w; }
      ctx.fillStyle = "#2ecc71"; ctx.fillRect(x, 2, W * okRate / 100, 12); x += W * okRate / 100;
      if (hiRate > 1) { ctx.fillStyle = "#e74c3c"; ctx.fillRect(x, 2, W * hiRate / 100, 12); }
    }, 150);
  });
}

// Trigger charts
watch(() => report.value, (r) => {
  if (!r || !r.hasData) return;
  nextTick(() => {
    setTimeout(() => {
      (r.trendData || []).forEach(t => {
        const opt = (r.suitability || []).find(s => s.indicator === t.indicator);
        const vals = t.values || [];
        const dates = t.dates || [];
        if (opt && vals.length > 1) {
          drawTrendChart("rchart-" + t.indicator, vals, dates, opt.optMin, opt.optMax, colorFor(t.indicator));
        }
      });
      (r.suitability || []).forEach(s => {
        drawGauge("rgauge-" + s.indicator, s.okRate, s.loRate, s.hiRate);
      });
    }, 200);
  });
}, { deep: true });

onMounted(load);
</script>

<template>
  <section class="panel resource-workbench">
    <div class="panel-head resource-head">
      <div><h2>分析报告</h2><span>查看和编辑生长数据分析结果</span></div>
      <div style="display:flex;gap:8px">
        <button class="button-secondary" @click="router.push('/module/growth-analysis')">
          <ArrowLeft :size="16" />返回列表
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" style="text-align:center;padding:60px">
      <RefreshCw :size="24" style="animation:spin 1s linear infinite;color:var(--muted)" />
      <p style="color:var(--muted);margin-top:8px">加载分析报告...</p>
    </div>

    <!-- Not found -->
    <div v-else-if="notFound" class="panel" style="text-align:center;padding:60px 20px">
      <BarChart3 :size="40" style="color:var(--muted);margin-bottom:12px" />
      <p style="color:var(--muted);font-size:15px">未找到该分析记录</p>
      <button class="button-secondary" style="margin-top:12px" @click="router.push('/module/growth-analysis')">返回列表</button>
    </div>

    <template v-else-if="report">
      <!-- ====== Banner: batch identity + indicator overview ====== -->
      <div class="panel" style="margin-bottom:20px">
        <div class="panel-head">
          <div>
            <h2>{{ analysisName }}</h2>
            <span v-if="report.analystName">{{ report.analystName }}{{ report.analyzedAt ? ' · ' + report.analyzedAt : '' }}</span>
          </div>
        </div>

        <!-- Batch identity row -->
        <div style="display:flex;align-items:center;gap:16px;flex-wrap:wrap;margin-bottom:14px">
          <span style="font-size:14px;color:var(--ink)">
            <strong>{{ batchInfo.herbName }}</strong> · {{ batchInfo.district }} · {{ batchInfo.batchName }}
          </span>
          <span style="font-size:12px;color:var(--muted)">
            {{ batchInfo.dateStart }} ~ {{ batchInfo.dateEnd }} · {{ batchInfo.recordCount }} 条记录
          </span>
        </div>

        <!-- Indicator summary strip -->
        <div v-if="report.hasData" style="display:flex;gap:20px;flex-wrap:wrap;padding-top:14px;border-top:1px solid var(--line, #e0e0e0)">
          <div v-for="t in report.trendData" :key="t.indicator"
            style="display:flex;align-items:center;gap:8px;padding:8px 14px;background:var(--surface-subtle,#f8f9fa);border-radius:8px">
            <span style="font-size:12px;color:var(--muted)">{{ t.label }}</span>
            <strong style="font-size:14px;color:var(--ink)">{{ t.mean }}{{ t.unit }}</strong>
            <span class="metric-badge" :class="trendBadge(t.direction).cls" style="font-size:11px">
              {{ trendBadge(t.direction).icon }} {{ t.direction }}
            </span>
            <span style="font-size:11px;color:var(--muted)">CV {{ t.cv }}%</span>
          </div>
        </div>
      </div>

      <!-- Old record fallback -->
      <div v-if="!report.hasData" class="panel" style="text-align:center;padding:40px 20px;margin-bottom:20px">
        <BarChart3 :size="40" style="color:var(--muted);margin-bottom:12px" />
        <p style="color:var(--muted);font-size:15px">此记录为旧版手动分析，无详细趋势图表</p>
        <p style="color:var(--muted);font-size:13px;margin-top:4px">可使用"开始分析"重新生成自动化报告</p>
      </div>

      <!-- ====== Per-indicator cards (trend + suitability combined) ====== -->
      <div v-for="t in report.trendData" :key="t.indicator" class="panel" style="margin-bottom:16px">
        <!-- Card header: indicator name + trend + compliance -->
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;flex-wrap:wrap;gap:8px">
          <div style="display:flex;align-items:center;gap:10px">
            <span :style="{width:'10px',height:'10px',borderRadius:'50%',background:colorFor(t.indicator),display:'inline-block'}"></span>
            <strong style="font-size:15px">{{ t.label }}</strong>
            <span class="metric-badge" :class="trendBadge(t.direction).cls">{{ trendBadge(t.direction).icon }} {{ t.direction }}</span>
            <template v-if="report.suitability.length">
              <span v-for="s in report.suitability.filter(x => x.indicator === t.indicator)" :key="s.indicator"
                class="metric-badge" :class="okBadge(s.okRate)" style="font-size:11px">
                达标 {{ s.okRate }}%
              </span>
            </template>
          </div>
          <!-- Mini stats -->
          <div style="display:flex;gap:18px;font-size:12px;color:var(--muted)">
            <span>均值 <strong style="color:var(--ink)">{{ t.mean }}{{ t.unit }}</strong></span>
            <span>CV <strong style="color:var(--ink)">{{ t.cv }}%</strong></span>
            <span>范围 <strong style="color:var(--ink)">{{ t.rangeStart }} ~ {{ t.rangeEnd }}{{ t.unit }}</strong></span>
          </div>
        </div>

        <!-- Chart + stats row -->
        <div style="display:flex;gap:16px;flex-wrap:wrap">
          <!-- Trend chart -->
          <div style="flex:1;min-width:300px;max-width:520px;background:var(--surface-subtle,#f8f9fa);border-radius:8px;padding:10px">
            <canvas :id="'rchart-' + t.indicator"></canvas>
          </div>

          <!-- Suitability panel -->
          <div v-for="s in report.suitability.filter(x => x.indicator === t.indicator)" :key="'s'+s.indicator"
            style="flex:1;min-width:220px;display:flex;flex-direction:column;gap:8px">
            <div style="font-size:11px;color:var(--muted);font-weight:600">适宜性分析</div>

            <!-- Stacked bar -->
            <div style="background:var(--surface-subtle,#f6f9fc);border-radius:8px;padding:12px 14px">
              <div style="display:flex;justify-content:space-between;font-size:11px;color:var(--muted);margin-bottom:5px">
                <span style="color:#3498db">▼ 偏低 {{ s.loRate }}%</span>
                <span style="color:#2ecc71">达标 {{ s.okRate }}%</span>
                <span style="color:#e74c3c">偏高 {{ s.hiRate }}% ▲</span>
              </div>
              <div style="height:22px;border-radius:11px;overflow:hidden;display:flex;background:#eee">
                <div v-if="s.loRate > 1" :style="{width:s.loRate+'%',background:'#3498db'}"></div>
                <div :style="{width:s.okRate+'%',background:'#2ecc71'}"></div>
                <div v-if="s.hiRate > 1" :style="{width:s.hiRate+'%',background:'#e74c3c'}"></div>
              </div>
              <div style="display:flex;justify-content:space-between;margin-top:5px;font-size:10px;color:var(--muted)">
                <span>{{ s.lo }}条</span><span>{{ s.okRate > 0 ? s.okRate - s.loRate - s.hiRate + s.lo + s.hi : 20 - s.lo - s.hi }}条</span><span>{{ s.hi }}条</span>
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
                  <div :style="{
                    width: (100 - sd.badRate) + '%',
                    background: sd.badRate >= 50 ? '#e74c3c' : sd.badRate > 0 ? '#f0ad4e' : '#2ecc71',
                    borderRadius: '7px',
                    transition: 'width .3s'
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

      <!-- ====== Conclusion ====== -->
      <div class="panel" style="margin-bottom:20px">
        <div class="panel-head">
          <div><h2>分析结论</h2><span>{{ report.analyzedAt ? '分析时间 ' + report.analyzedAt : '' }}</span></div>
        </div>

        <div style="margin-bottom:12px">
          <label style="font-size:13px;color:var(--muted);display:block;margin-bottom:4px">分析名称</label>
          <input v-model="analysisName" style="max-width:420px" />
        </div>
        <div style="display:flex;gap:16px;flex-wrap:wrap;margin-bottom:12px">
          <div style="flex:1;min-width:280px">
            <label style="font-size:13px;color:var(--muted);display:block;margin-bottom:4px">综合判断</label>
            <textarea v-model="verdict" rows="4" style="width:100%;font-size:13px;line-height:1.7"
              placeholder="综合判断..."></textarea>
          </div>
          <div style="flex:1;min-width:280px">
            <label style="font-size:13px;color:var(--muted);display:block;margin-bottom:4px">改进建议</label>
            <textarea v-model="suggestions" rows="4" style="width:100%;font-size:13px;line-height:1.7"
              placeholder="改进建议..."></textarea>
          </div>
        </div>

        <div style="margin-top:12px;display:flex;gap:12px">
          <button @click="saveConclusion" :disabled="saving">
            <Save :size="16" />{{ saving ? "保存中..." : "保存" }}
          </button>
          <button class="button-secondary" @click="router.push('/module/growth-analysis')">返回列表</button>
        </div>
      </div>
    </template>
  </section>
</template>
