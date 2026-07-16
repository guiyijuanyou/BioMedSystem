<script setup>
import { computed, inject, onMounted, reactive, ref } from "vue";
import { Save, Target } from "lucide-vue-next";
import { api } from "@/services/api";

const role = inject("currentRole"), notify = inject("notify");
const loading = ref(true), data = ref({ herbs: [], configs: {}, results: [] });
const selectedHerbId = ref("");
const showForm = ref(false);

const form = reactive({
  herbId: "",
  metrics: {
    GROWTH_TEMP_AVG: { metricName: "平均生长温度", sourceType: "growth", sourceField: "temperature_avg", unitName: "°C", minimumValue: "", maximumValue: "", targetValue: "", weightValue: 25 },
    GROWTH_HUMIDITY_AVG: { metricName: "平均环境湿度", sourceType: "growth", sourceField: "humidity_avg", unitName: "%", minimumValue: "", maximumValue: "", targetValue: "", weightValue: 20 },
    GROWTH_SOIL_PH_AVG: { metricName: "平均土壤pH", sourceType: "growth", sourceField: "soil_ph_avg", unitName: "pH", minimumValue: "", maximumValue: "", targetValue: "", weightValue: 20 },
    SPECTRUM_SIMILARITY_MAX: { metricName: "图谱最高相似度", sourceType: "spectrum", sourceField: "similarity_max", unitName: "%", minimumValue: null, maximumValue: null, targetValue: null, weightValue: 35 }
  }
});

async function load() { loading.value = true; try { data.value = await api("/api/quality-metrics"); } catch (e) { notify?.(e.message) } finally { loading.value = false } }

function selectHerb(herbId) {
  selectedHerbId.value = herbId;
  form.herbId = herbId;
  const existing = data.value.configs[herbId] || [];
  const defaults = {
    GROWTH_TEMP_AVG: { metricName: "平均生长温度", sourceType: "growth", sourceField: "temperature_avg", unitName: "°C", minimumValue: "", maximumValue: "", targetValue: "", weightValue: 25 },
    GROWTH_HUMIDITY_AVG: { metricName: "平均环境湿度", sourceType: "growth", sourceField: "humidity_avg", unitName: "%", minimumValue: "", maximumValue: "", targetValue: "", weightValue: 20 },
    GROWTH_SOIL_PH_AVG: { metricName: "平均土壤pH", sourceType: "growth", sourceField: "soil_ph_avg", unitName: "pH", minimumValue: "", maximumValue: "", targetValue: "", weightValue: 20 },
    SPECTRUM_SIMILARITY_MAX: { metricName: "图谱最高相似度", sourceType: "spectrum", sourceField: "similarity_max", unitName: "%", minimumValue: null, maximumValue: null, targetValue: null, weightValue: 35 }
  };
  form.metrics = JSON.parse(JSON.stringify(defaults));
  for (const m of existing) {
    const code = m.metricCode;
    if (form.metrics[code]) {
      form.metrics[code].metricName = m.metricName || form.metrics[code].metricName;
      form.metrics[code].minimumValue = m.minimumValue != null ? m.minimumValue : "";
      form.metrics[code].maximumValue = m.maximumValue != null ? m.maximumValue : "";
      form.metrics[code].targetValue = m.targetValue != null ? m.targetValue : "";
      form.metrics[code].weightValue = m.weightValue != null ? m.weightValue : form.metrics[code].weightValue;
    }
  }
  showForm.value = true;
}

async function saveConfig() {
  try {
    const payload = { herbId: form.herbId, metrics: {} };
    for (const [code, cfg] of Object.entries(form.metrics)) {
      payload.metrics[code] = {
        metricName: cfg.metricName, sourceType: cfg.sourceType, sourceField: cfg.sourceField, unitName: cfg.unitName,
        minimumValue: code === "SPECTRUM_SIMILARITY_MAX" ? null : (cfg.minimumValue === "" ? null : cfg.minimumValue),
        maximumValue: code === "SPECTRUM_SIMILARITY_MAX" ? null : (cfg.maximumValue === "" ? null : cfg.maximumValue),
        targetValue: code === "SPECTRUM_SIMILARITY_MAX" ? null : (cfg.targetValue === "" ? null : cfg.targetValue),
        weightValue: cfg.weightValue
      };
    }
    await api("/api/quality-metrics/herb-config", { method: "PUT", body: JSON.stringify(payload) });
    notify?.("指标配置已保存");
    await load();
    const fresh = data.value.configs[form.herbId] || [];
    if (fresh.length) selectHerb(form.herbId);
  } catch (e) { notify?.(e.message) }
}

const selectedHerbName = computed(() => {
  const h = data.value.herbs.find(x => x.id === selectedHerbId.value);
  return h ? h.herbName : "";
});

onMounted(load);
</script>

<template>
  <section class="metric-workbench">
    <header class="metric-head metric-section">
      <div class="metric-head__title">
        <span class="metric-head__icon"><Target :size="21" /></span>
        <span><h2>质量指标标准</h2><p>按药材分别配置生长指标区间与图谱权重，自动评价时按药材匹配。</p></span>
      </div>
    </header>

    <section class="metric-section" style="display:flex;gap:20px;padding:16px 22px">
      <div style="width:200px;flex-shrink:0;border-right:1px solid var(--border);padding-right:16px">
        <h3 style="font-size:13px;margin:0 0 8px 0;color:var(--ink)">药材列表</h3>
        <div v-if="!data.herbs.length" style="color:var(--muted);font-size:12px">暂无药材数据</div>
        <div v-for="h in data.herbs" :key="h.id" @click="selectHerb(h.id)"
             :class="{ 'herb-item-active': selectedHerbId === h.id }"
             class="herb-item">
          {{ h.herbName }}
          <span class="herb-status">
            {{ (data.configs[h.id] || []).length ? (data.configs[h.id] || []).length + '项' : '未配置' }}
          </span>
        </div>
      </div>

      <div style="flex:1;min-width:0">
        <div v-if="!showForm" class="metric-empty-state">
          <span class="metric-empty-state__icon"><Target :size="24" /></span>
          <div><h3>选择药材配置指标</h3><p>从左侧列表选择一种药材，为其配置生长环境区间与权重。</p></div>
        </div>
        <div v-else>
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:14px">
            <h3 style="font-size:15px;color:var(--ink);margin:0">{{ selectedHerbName }} — 指标配置</h3>
            <span style="flex:1"></span>
            <button v-if="role==='admin'" @click="saveConfig" style="min-height:32px;padding:0 14px;font-size:12px"><Save :size="15" /> 保存配置</button>
          </div>
          <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:12px">
            <div v-for="(cfg, code) in form.metrics" :key="code" class="metric-card" :class="{ 'metric-card-spectrum': code === 'SPECTRUM_SIMILARITY_MAX' }">
              <div class="metric-card-title">{{ cfg.metricName }} <span class="metric-card-code">({{ code }})</span></div>
              <div v-if="code!=='SPECTRUM_SIMILARITY_MAX'" style="display:grid;grid-template-columns:1fr 1fr;gap:6px">
                <label class="metric-label">最小值<input v-model.number="cfg.minimumValue" type="number" step="0.1" /></label>
                <label class="metric-label">最大值<input v-model.number="cfg.maximumValue" type="number" step="0.1" /></label>
                <label class="metric-label">目标值<input v-model.number="cfg.targetValue" type="number" step="0.1" /></label>
                <label class="metric-label">权重 ({{ cfg.unitName }})<input v-model.number="cfg.weightValue" type="number" /></label>
              </div>
              <div v-else>
                <div class="spectrum-range-note">区间：全局统一 90–100%，目标 98%</div>
                <label class="metric-label">权重<input v-model.number="cfg.weightValue" type="number" style="width:100px" /></label>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.herb-item {
  padding: 8px 10px; cursor: pointer; border-radius: 6px; margin-bottom: 2px; font-size: 13px;
  color: var(--ink); transition: background 0.15s;
}
.herb-item:hover { background: rgba(0,0,0,0.04); }
.herb-item-active { background: rgba(0,0,0,0.06); font-weight: 600; }
.herb-status { font-size: 10px; color: var(--muted); float: right; }
.metric-card {
  padding: 14px; border-radius: 8px; border: 1px solid var(--border); background: #f7f9f8;
}
.metric-card-spectrum { background: #fafbfa; }
.metric-card-title { font-weight: 700; font-size: 13px; color: var(--ink); margin-bottom: 8px; }
.metric-card-code { font-weight: 400; font-size: 10px; color: var(--muted); }
.metric-label { font-size: 11px; color: var(--muted); }
.metric-label input { min-height: 28px; font-size: 12px; width: 100%; }
.spectrum-range-note { font-size: 10px; color: var(--muted); padding: 4px 8px; background: #f0f4f1; border-radius: 4px; margin-bottom: 8px; }
</style>
