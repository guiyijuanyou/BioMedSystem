<script setup>
import { computed, inject, onMounted, reactive, ref } from "vue";
import { Calculator, Plus, RefreshCw, Target } from "lucide-vue-next";
import { api } from "@/services/api";
const role=inject("currentRole"),notify=inject("notify");
const loading=ref(true),batchId=ref(""),data=ref({definitions:[],batches:[],results:[]}),showForm=ref(false);
const form=reactive({metricCode:"",metricName:"",sourceType:"growth",sourceField:"temperature_avg",unitName:"",minimumValue:"",maximumValue:"",targetValue:"",weightValue:10});
const batchResults=computed(()=>data.value.results.filter(x=>x.batchId===batchId.value));
async function load(){loading.value=true;try{data.value=await api("/api/quality-metrics");if(!batchId.value)batchId.value=data.value.batches[0]?.id||"";}catch(e){notify(e.message)}finally{loading.value=false}}
async function calculate(){try{await api(`/api/quality-metrics/calculate/${batchId.value}`,{method:"POST"});notify("批次指标已重新计算");await load()}catch(e){notify(e.message)}}
async function create(){try{await api("/api/quality-metrics",{method:"POST",body:JSON.stringify(form)});notify("指标标准新版本已创建");showForm.value=false;await load()}catch(e){notify(e.message)}}
function judgement(v){return ({qualified:"达标",below_standard:"低于标准",above_standard:"高于标准",no_data:"暂无数据"})[v]||v}
onMounted(load);
</script>
<template><section class="metric-workbench">
  <header class="metric-head"><div><Target :size="22"/><span><h2>统一质量指标</h2><p>按标准版本计算批次实测值，结果可作为评价依据。</p></span></div><button v-if="role==='admin'" class="button-secondary" @click="showForm=!showForm"><Plus :size="16"/>新增指标版本</button></header>
  <form v-if="showForm" class="metric-form" @submit.prevent="create"><input v-model="form.metricCode" required placeholder="指标编码"/><input v-model="form.metricName" required placeholder="指标名称"/><select v-model="form.sourceField"><option value="temperature_avg">平均温度</option><option value="humidity_avg">平均湿度</option><option value="soil_ph_avg">平均土壤pH</option><option value="similarity_max">图谱最高相似度</option></select><input v-model="form.unitName" placeholder="单位"/><input v-model="form.minimumValue" type="number" step=".01" placeholder="最小合格值"/><input v-model="form.maximumValue" type="number" step=".01" placeholder="最大合格值"/><input v-model="form.targetValue" type="number" step=".01" placeholder="目标值"/><input v-model="form.weightValue" type="number" required placeholder="权重"/><button>保存版本</button></form>
  <div class="metric-controls"><select v-model="batchId"><option v-for="b in data.batches" :key="b.id" :value="b.id">{{b.batchCode}} · {{b.batchName}} · {{b.herbName}}</option></select><button :disabled="!batchId" @click="calculate"><Calculator :size="16"/>自动计算</button><button class="button-secondary" @click="load"><RefreshCw :size="16"/>刷新</button></div>
  <div v-if="loading" class="empty-state">正在加载指标...</div><div v-else class="metric-grid"><article v-for="r in batchResults" :key="r.id" :data-status="r.judgement"><span>{{r.metricName}}</span><strong>{{r.measuredValue??'-'}} <small>{{r.unitName}}</small></strong><b>{{judgement(r.judgement)}} · {{r.score??'-'}}分</b><p>{{r.calculationNote}}，来源 {{r.sourceCount}} 条</p></article><div v-if="!batchResults.length" class="empty-state">该批次尚未计算，点击“自动计算”生成结果。</div></div>
  <div class="metric-table"><h3>当前指标标准</h3><table><thead><tr><th>指标</th><th>来源字段</th><th>合格范围</th><th>目标</th><th>权重</th><th>版本</th></tr></thead><tbody><tr v-for="d in data.definitions" :key="d.id"><td>{{d.metricName}}<small>{{d.metricCode}}</small></td><td>{{d.sourceField}}</td><td>{{d.minimumValue??'-'}} ~ {{d.maximumValue??'-'}} {{d.unitName}}</td><td>{{d.targetValue??'-'}}</td><td>{{d.weightValue}}</td><td>V{{d.versionNo}}</td></tr></tbody></table></div>
</section></template>
