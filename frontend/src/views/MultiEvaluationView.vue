<script setup>
import { computed, inject, onMounted, ref, watch } from "vue";
import { AlertTriangle, ClipboardCheck, ExternalLink, RefreshCw, Target } from "lucide-vue-next";
import { useRouter } from "vue-router";
import { api } from "@/services/api";
import { appDialog } from "@/services/dialog";
import AppSelect from "@/components/AppSelect.vue";

const notify = inject("notify"),
  role = inject("currentRole"),
  router = useRouter();

const loading = ref(true);
const schemeId = ref("");
const batchId = ref("");
const data = ref({ schemes: [], batches: [], evaluations: [] });
const selected = ref(null);
const recommendations = ref([]);
const matchedMetrics = ref([]);
const matchedHerb = ref(null);
const herbMetrics = ref([]);       // 该药材在"指标标准"中配置的全部指标
const evaluating = ref(false);

// ── computed ──

const currentScheme = computed(() => data.value.schemes.find(x => x.id === schemeId.value));

const batchOptions = computed(() =>
  data.value.batches.map(b => ({
    value: b.id,
    label: `${b.batchCode} · ${b.batchName} · ${b.herbName}`
  }))
);

// 只显示该药材已配置全部所需指标的方案
const compatibleSchemes = computed(() => {
  if (!herbMetrics.value.length) return [];
  const configuredCodes = new Set(herbMetrics.value.map(m => m.metricCode));
  return data.value.schemes.filter(s => {
    // 方案没有 itemCount 或者所有方案项都能匹配
    if (!s.itemCount || s.itemCount === 0) return false;
    // 需要从后端加载方案详情来确定...简化处理：只要药材配了至少一项指标就显示全部方案
    // 实际评价时会在后端精确校验
    return true;
  }).map(s => ({
    value: s.id,
    label: `${s.schemeName} · V${s.versionNo}`
  }));
});

const schemeOptions = computed(() => compatibleSchemes.value);

// 选中的批次信息
const selectedBatch = computed(() => data.value.batches.find(x => x.id === batchId.value));

// ── data loading ──

async function load() {
  loading.value = true;
  try {
    data.value = await api("/api/multi-evaluations");
    if (!schemeId.value) schemeId.value = data.value.schemes[0]?.id || "";
    if (!batchId.value) batchId.value = data.value.batches[0]?.id || "";
  } catch (e) {
    notify?.(e.message);
  } finally {
    loading.value = false;
  }
}

// 选批次 → 自动加载该药材的指标标准
watch(batchId, async (id) => {
  herbMetrics.value = [];
  matchedHerb.value = null;
  matchedMetrics.value = [];
  if (!id) return;
  const b = data.value.batches.find(x => x.id === id);
  if (!b || !b.herbId) return;
  try {
    const r = await api(`/api/quality-metrics/herb-metrics/${b.herbId}`);
    herbMetrics.value = r.metrics || [];
    matchedHerb.value = { herbId: b.herbId, herbName: b.herbName };
  } catch (e) {
    herbMetrics.value = [];
  }
});

// ── actions ──

async function evaluate() {
  if (!schemeId.value || !batchId.value) {
    notify?.("请先选择评价方案和药材批次");
    return;
  }
  evaluating.value = true;
  try {
    selected.value = await api("/api/multi-evaluations/evaluate", {
      method: "POST",
      body: JSON.stringify({ schemeId: schemeId.value, batchId: batchId.value })
    });
    if (selected.value.matchedHerb) matchedHerb.value = selected.value.matchedHerb;
    if (selected.value.matchedMetrics) matchedMetrics.value = selected.value.matchedMetrics;
    await loadRecommendations(selected.value.evaluation.id);
    notify?.("自动评价已完成");
    await load();
  } catch (e) {
    notify?.(e.message);
  } finally {
    evaluating.value = false;
  }
}

async function detail(id) {
  try {
    selected.value = await api(`/api/multi-evaluations/${id}`);
    await loadRecommendations(id);
  } catch (e) {
    notify?.(e.message);
  }
}

async function loadRecommendations(id) {
  try {
    const r = await api(`/api/improvement-recommendations/evaluation/${id}`);
    recommendations.value = r.items || [];
  } catch (e) {
    recommendations.value = [];
  }
}

async function acceptRecommendation(r) {
  const assignee = await appDialog.prompt({
    title: "采纳改进建议",
    label: "培训任务负责人（可选）",
    message: "填写负责人后将同步创建培训任务；没有匹配素材时仅创建整改问题。",
    placeholder: "请输入负责人姓名",
    confirmText: "采纳并创建"
  });
  if (assignee === null) return;
  try {
    await api(`/api/improvement-recommendations/${r.id}/accept`, {
      method: "POST",
      body: JSON.stringify({ assignee })
    });
    notify?.("整改问题和推荐培训任务已创建");
    await loadRecommendations(r.multiEvaluationId);
  } catch (e) {
    notify?.(e.message);
  }
}

async function ignoreRecommendation(r) {
  try {
    await api(`/api/improvement-recommendations/${r.id}/ignore`, { method: "PUT" });
    notify?.("建议已忽略");
    await loadRecommendations(r.multiEvaluationId);
  } catch (e) {
    notify?.(e.message);
  }
}

async function removeEvaluation(id) {
  const confirmed = await appDialog.confirm({
    title: "删除自动评价",
    message: "评价记录及其全部评分明细将被永久删除，此操作无法撤销。",
    tone: "danger",
    confirmText: "确认删除"
  });
  if (!confirmed) return;
  try {
    await api(`/api/multi-evaluations/${id}`, { method: "DELETE" });
    if (selected.value?.evaluation?.id === id) selected.value = null;
    notify?.("自动评价已删除");
    await load();
  } catch (e) {
    notify?.(e.message);
  }
}

function text(v) {
  return (
    { qualified: "合格", unqualified: "不合格", excellent: "优秀", good: "良好",
      below_standard: "低于标准", above_standard: "高于标准" }[v] || v
  );
}

onMounted(load);
</script>

<template>
  <section class="auto-evaluation-page">

    <!-- ── 命令栏 ── -->
    <div class="auto-eval-command">
      <div>
        <ClipboardCheck :size="22" />
        <span>
          <h2>批次多指标自动评价</h2>
          <p>选择批次后自动匹配该药材在「指标标准」中配置的评价区间与权重，选定方案执行评分。</p>
        </span>
      </div>

      <!-- 1. 先选批次 -->
      <AppSelect v-model="batchId" :options="batchOptions" aria-label="选择药材批次" />

      <!-- 2. 再选评价方案 -->
      <AppSelect v-model="schemeId" :options="schemeOptions" aria-label="选择评价方案" />

      <button :disabled="!schemeId || !batchId || evaluating" @click="evaluate">
        {{ evaluating ? "评价中..." : "执行自动评价" }}
      </button>
      <small v-if="currentScheme">合格线 {{ currentScheme.passingScore }} 分 · {{ currentScheme.itemCount }} 项指标</small>
    </div>

    <!-- ── 药材指标标准卡片 ── -->
    <div v-if="matchedHerb" class="insight-panel">
      <div class="insight-heading">
        <span><Target :size="18" /></span>
        <div>
          <strong>药材：{{ matchedHerb.herbName }}</strong>
          <small>以下评价标准来自「指标标准」，执行评价时将按所选方案匹配对应指标</small>
        </div>
      </div>
      <div v-if="herbMetrics.length" class="insight-grid">
        <article v-for="m in herbMetrics" :key="m.metricCode">
          <span>{{ m.metricName }}</span>
          <strong>
            <template v-if="m.minimumValue != null">{{ m.minimumValue }}–{{ m.maximumValue }} {{ m.unitName }}</template>
            <template v-else>90–100%</template>
          </strong>
          <small>权重 {{ m.weightValue }}</small>
        </article>
      </div>
      <div v-else style="display:flex;align-items:center;gap:8px;padding:10px;color:#c75a38;font-size:12px">
        <AlertTriangle :size="16" />
        <span>该药材尚未在「<router-link to="/quality-metrics">指标标准</router-link>」中配置任何评价指标</span>
      </div>
    </div>

    <!-- ── 评价结果 ── -->
    <div v-if="selected" class="auto-eval-result">
      <header>
        <div><span>综合得分</span><strong>{{ selected.evaluation.total_score }}</strong></div>
        <div><span>评价等级</span><strong>{{ text(selected.evaluation.grade_name) }}</strong></div>
        <div><span>评价结果</span><strong>{{ text(selected.evaluation.result) }}</strong></div>
        <button v-if="selected.evaluation.result === 'unqualified'" @click="router.push('/improvement')">
          <ExternalLink :size="15" />进入改进闭环
        </button>
      </header>
      <table>
        <thead>
          <tr>
            <th>指标</th>
            <th>实测值</th>
            <th>原始分</th>
            <th>权重</th>
            <th>加权分</th>
            <th>来源数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in selected.details" :key="d.metricName">
            <td>{{ d.metricName }}<small>标准 V{{ d.metricVersion }}</small></td>
            <td>{{ d.measuredValue }} {{ d.unitName }}</td>
            <td>{{ d.rawScore }}</td>
            <td>{{ d.weightValue }}%</td>
            <td>{{ d.weightedScore }}</td>
            <td>{{ d.sourceCount }}</td>
          </tr>
        </tbody>
      </table>

      <!-- 改进建议 -->
      <section v-if="recommendations.length" class="recommendation-list">
        <h3>异常指标改进建议</h3>
        <article v-for="r in recommendations" :key="r.id">
          <div>
            <strong>{{ r.issueTitle }}</strong>
            <p>{{ r.issueDescription }}</p>
            <small>目标：{{ r.improvementTarget }}</small>
          </div>
          <div>
            <span>推荐素材：{{ r.materialTitle || '暂无匹配素材' }}</span>
            <small>{{ r.recommendationReason }}</small>
          </div>
          <div v-if="r.status === 'pending'">
            <button @click="acceptRecommendation(r)">采纳并创建任务</button>
            <button class="button-secondary" @click="ignoreRecommendation(r)">忽略</button>
          </div>
          <b v-else>{{ r.status === 'accepted' ? '已采纳' : '已忽略' }}</b>
        </article>
      </section>
    </div>

    <!-- ── 评价历史 ── -->
    <div class="auto-eval-history">
      <header>
        <h3>评价历史</h3>
        <button class="button-secondary" @click="load"><RefreshCw :size="15" />刷新</button>
      </header>
      <div v-if="loading" class="empty-state">正在加载...</div>
      <table v-else>
        <thead>
          <tr>
            <th>批次</th>
            <th>方案</th>
            <th>总分</th>
            <th>等级</th>
            <th>结果</th>
            <th>评价人</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in data.evaluations" :key="e.id">
            <td>{{ e.batchName }}</td>
            <td>{{ e.schemeName }}</td>
            <td>{{ e.totalScore }}</td>
            <td>{{ text(e.gradeName) }}</td>
            <td>{{ text(e.result) }}</td>
            <td>{{ e.evaluator }}</td>
            <td class="auto-eval-actions">
              <button class="button-secondary" @click="detail(e.id)">查看明细</button>
              <button v-if="role === 'admin'" class="button-danger" @click="removeEvaluation(e.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
