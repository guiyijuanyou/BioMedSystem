<script setup>
import { computed, inject, onMounted, reactive, ref } from "vue";
import { Calculator, Plus, RefreshCw, Target } from "lucide-vue-next";
import { api } from "@/services/api";
import AppSelect from "@/components/AppSelect.vue";
const role=inject("currentRole"),notify=inject("notify");
const loading=ref(true),batchId=ref(""),data=ref({definitions:[],batches:[],results:[]}),showForm=ref(false);
const form=reactive({metricCode:"",metricName:"",sourceType:"growth",sourceField:"temperature_avg",unitName:"",minimumValue:"",maximumValue:"",targetValue:"",weightValue:10});
const batchResults=computed(()=>data.value.results.filter(x=>x.batchId===batchId.value));
const sourceFieldOptions=[{value:"temperature_avg",label:"平均温度"},{value:"humidity_avg",label:"平均湿度"},{value:"soil_ph_avg",label:"平均土壤pH"},{value:"similarity_max",label:"图谱最高相似度"}];
const batchOptions=computed(()=>data.value.batches.map(b=>({value:b.id,label:`${b.batchCode} · ${b.batchName} · ${b.herbName}`})));
async function load(){loading.value=true;try{data.value=await api("/api/quality-metrics");if(!batchId.value)batchId.value=data.value.batches[0]?.id||"";}catch(e){notify(e.message)}finally{loading.value=false}}
async function calculate(){try{await api(`/api/quality-metrics/calculate/${batchId.value}`,{method:"POST"});notify("批次指标已重新计算");await load()}catch(e){notify(e.message)}}
async function create(){try{await api("/api/quality-metrics",{method:"POST",body:JSON.stringify(form)});notify("指标标准新版本已创建");showForm.value=false;await load()}catch(e){notify(e.message)}}
function judgement(v){return ({qualified:"达标",below_standard:"低于标准",above_standard:"高于标准",no_data:"暂无数据"})[v]||v}
onMounted(load);
</script>
<template>
  <section class="metric-workbench">
    <header class="metric-head metric-section">
      <div class="metric-head__title">
        <span class="metric-head__icon"><Target :size="21" /></span>
        <span>
          <h2>统一质量指标</h2>
          <p>按标准版本计算批次实测值，结果可作为评价依据。</p>
        </span>
      </div>
      <button v-if="role === 'admin'" class="button-secondary metric-version-button" @click="showForm = !showForm">
        <Plus :size="16" />新增指标版本
      </button>
    </header>

    <form v-if="showForm" class="metric-form metric-section" @submit.prevent="create">
      <input v-model="form.metricCode" required placeholder="指标编码">
      <input v-model="form.metricName" required placeholder="指标名称">
      <AppSelect v-model="form.sourceField" :options="sourceFieldOptions" aria-label="选择指标来源字段" />
      <input v-model="form.unitName" placeholder="单位">
      <input v-model="form.minimumValue" type="number" step=".01" placeholder="最小合格值">
      <input v-model="form.maximumValue" type="number" step=".01" placeholder="最大合格值">
      <input v-model="form.targetValue" type="number" step=".01" placeholder="目标值">
      <input v-model="form.weightValue" type="number" required placeholder="权重">
      <button>保存版本</button>
    </form>

    <section class="metric-section metric-calculation">
      <div class="metric-controls">
        <AppSelect v-model="batchId" :options="batchOptions" aria-label="选择指标计算批次" />
        <div class="metric-control-actions">
          <button class="metric-action-primary" :disabled="!batchId" @click="calculate">
            <Calculator :size="16" />自动计算
          </button>
          <button class="button-secondary metric-action-secondary" @click="load">
            <RefreshCw :size="16" />刷新
          </button>
        </div>
      </div>

      <div v-if="loading" class="metric-empty-state metric-loading-state">
        <span class="metric-empty-state__icon"><RefreshCw :size="22" /></span>
        <div><h3>正在加载指标</h3><p>正在读取批次和指标标准，请稍候。</p></div>
      </div>
      <div v-else-if="!batchResults.length" class="metric-empty-state">
        <span class="metric-empty-state__icon"><Calculator :size="24" /></span>
        <div>
          <h3>尚未生成批次指标结果</h3>
          <p>当前批次还没有计算记录，运行自动计算后将在这里展示评分与达标情况。</p>
        </div>
        <button class="metric-empty-state__action" :disabled="!batchId" @click="calculate">
          <Calculator :size="16" />立即自动计算
        </button>
      </div>
      <div v-else class="metric-grid">
        <article v-for="r in batchResults" :key="r.id" :data-status="r.judgement">
          <span>{{ r.metricName }}</span>
          <strong>{{ r.measuredValue ?? '-' }} <small>{{ r.unitName }}</small></strong>
          <b>{{ judgement(r.judgement) }} · {{ r.score ?? '-' }}分</b>
          <p>{{ r.calculationNote }}，来源 {{ r.sourceCount }} 条</p>
        </article>
      </div>
    </section>

    <section class="metric-table metric-section">
      <header class="metric-table__head">
        <div><h3>当前指标标准</h3><p>统一查看指标来源、合格范围、目标值与当前版本。</p></div>
      </header>
      <div class="metric-table__scroll">
        <table>
          <thead><tr><th>指标</th><th>来源字段</th><th>合格范围</th><th>目标</th><th>权重</th><th>版本</th></tr></thead>
          <tbody><tr v-for="d in data.definitions" :key="d.id"><td>{{ d.metricName }}<small>{{ d.metricCode }}</small></td><td>{{ d.sourceField }}</td><td>{{ d.minimumValue ?? '-' }} ~ {{ d.maximumValue ?? '-' }} {{ d.unitName }}</td><td>{{ d.targetValue ?? '-' }}</td><td>{{ d.weightValue }}</td><td>V{{ d.versionNo }}</td></tr></tbody>
        </table>
      </div>
    </section>
  </section>
</template>
