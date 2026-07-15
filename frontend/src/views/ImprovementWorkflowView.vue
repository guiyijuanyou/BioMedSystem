<script setup>
import { computed, inject, onMounted, reactive, ref } from "vue";
import { CheckCircle2, ClipboardPlus, GraduationCap, Link2, RefreshCcw } from "lucide-vue-next";
import { api } from "@/services/api";
import { appDialog } from "@/services/dialog";
import AppSelect from "@/components/AppSelect.vue";

const currentRole = inject("currentRole");
const notify = inject("notify");
const loading = ref(true);
const loadError = ref("");
const showEvaluationForm = ref(false);
const data = ref({ issues: [], tasks: [], evaluations: [], analyses: [], materials: [], achievements: [], evidence: [] });
const evaluationForm = reactive({ sourceAnalysisId: "", batchId: "", herbName: "", indicator: "", score: "", result: "", subjectOwner: "" });
const issueForm = reactive({ evaluationId: "", sourceAnalysisId: "", title: "", description: "", severity: "medium", improvementTarget: "" });
const taskDrafts = reactive({});
const recheckDrafts = reactive({});
const evidenceDrafts = reactive({});
const openIssues = computed(() => data.value.issues.filter(item => item.status !== "closed"));
const closedIssues = computed(() => data.value.issues.filter(item => item.status === "closed"));
const completedTasks = computed(() => data.value.tasks.filter(item => item.status === "completed").length);
const evaluationOptions = computed(() => [
  { value: "", label: data.value.evaluations.length ? "选择评价记录" : "暂无评价，请先新建初评" },
  ...data.value.evaluations.map(item => ({ value: item.id, label: `${item.herbName} · ${item.indicator} · ${item.score ?? "未评分"}` }))
]);
const analysisOptions = computed(() => [
  { value: "", label: "不关联分析" },
  ...data.value.analyses.map(item => ({ value: item.id, label: item.analysisName }))
]);
const evaluationAnalysisOptions = computed(() => [
  { value: "", label: "选择分析依据（可选）" },
  ...data.value.analyses.map(item => ({ value: item.id, label: item.analysisName }))
]);
const severityOptions = [{ value: "low", label: "一般" }, { value: "medium", label: "重要" }, { value: "high", label: "严重" }];
const materialOptions = computed(() => [
  { value: "", label: "选择培训素材（可选）" },
  ...data.value.materials.map(item => ({ value: item.id, label: item.title }))
]);
const achievementOptions = computed(() => [
  { value: "", label: "选择工作业绩" },
  ...data.value.achievements.map(item => ({ value: item.id, label: item.title }))
]);

async function load() {
  loading.value = true;
  loadError.value = "";
  try { data.value = await api("/api/improvement/overview"); }
  catch (error) { loadError.value = error.message; notify(error.message); }
  finally { loading.value = false; }
}

async function createEvaluation() {
  try {
    const created = await api("/api/improvement/evaluations", { method: "POST", body: JSON.stringify(evaluationForm) });
    await load();
    issueForm.evaluationId = created.id;
    issueForm.sourceAnalysisId = evaluationForm.sourceAnalysisId;
    showEvaluationForm.value = false;
    Object.assign(evaluationForm, { sourceAnalysisId: "", batchId: "", herbName: "", indicator: "", score: "", result: "", subjectOwner: "" });
    notify("初评已创建并自动选中");
  } catch (error) { notify(error.message); }
}

async function createIssue() {
  try {
    await api(`/api/improvement/evaluations/${issueForm.evaluationId}/issues`, { method: "POST", body: JSON.stringify(issueForm) });
    Object.assign(issueForm, { evaluationId: "", sourceAnalysisId: "", title: "", description: "", severity: "medium", improvementTarget: "" });
    notify("整改问题已建立"); await load();
  } catch (error) { notify(error.message); }
}

function taskDraft(id) {
  return taskDrafts[id] ||= { title: "", assignee: "", trainingMaterialId: "", dueDate: "", preScore: "" };
}
async function addTask(issue) {
  try { await api(`/api/improvement/issues/${issue.id}/tasks`, { method: "POST", body: JSON.stringify(taskDraft(issue.id)) }); notify("培训任务已下达"); await load(); }
  catch (error) { notify(error.message); }
}
async function completeTask(task) {
  const completionNote = await appDialog.prompt({
    title: "完成培训任务",
    label: "完成说明",
    message: `请记录“${task.title || "当前任务"}”的完成情况。`,
    placeholder: "例如：已完成培训并通过现场考核",
    required: true,
    multiline: true,
    confirmText: "下一步"
  });
  if (completionNote === null) return;
  const postScore = await appDialog.prompt({
    title: "填写培训后评分",
    label: "培训后评分（可选）",
    placeholder: "请输入数字评分",
    inputType: "number",
    confirmText: "完成任务"
  });
  if (postScore === null) return;
  try { await api(`/api/improvement/tasks/${task.id}/complete`, { method: "PUT", body: JSON.stringify({ completionNote, postScore }) }); notify("培训任务已完成"); await load(); }
  catch (error) { notify(error.message); }
}
function recheckDraft(id) { return recheckDrafts[id] ||= { score: "", result: "" }; }
async function recheck(issue) {
  try { await api(`/api/improvement/issues/${issue.id}/recheck`, { method: "POST", body: JSON.stringify(recheckDraft(issue.id)) }); notify("复评记录已生成，请到评价体系提交审核"); await load(); }
  catch (error) { notify(error.message); }
}
async function autoRecheck(issue) { try { const r=await api(`/api/improvement/issues/${issue.id}/auto-recheck`,{method:"POST"}); notify(`自动复评完成：${r.initialScore} → ${r.recheckScore}`); await load(); } catch(error){ notify(error.message); } }
async function closeIssue(issue) {
  try { await api(`/api/improvement/issues/${issue.id}/close`, { method: "PUT" }); notify("问题已关闭"); await load(); }
  catch (error) { notify(error.message); }
}
function evidenceDraft(id) { return evidenceDrafts[id] ||= { achievementId: "", evidenceTitle: "", evidenceSnapshot: "" }; }
async function addEvidence(issue) {
  const draft = evidenceDraft(issue.id);
  try {
    const result = await api(`/api/improvement/achievements/${draft.achievementId}/evidence`, { method: "POST", body: JSON.stringify({ ...draft, issueId: issue.id }) });
    notify(result.alreadyLinked ? "该闭环已挂接到这条工作业绩，无需重复添加" : "已挂接为业绩证据，可在工作业绩的佐证材料中查看");
    await load();
  }
  catch (error) { notify(error.message); }
}
async function confirmPoints(evidence){const points=await appDialog.prompt({title:"确认业绩分值",label:"认定分值",message:"请核对系统建议分值，确认后将计入关联业绩。",value:String(evidence.suggestedPoints??""),inputType:"number",required:true,confirmText:"确认分值"});if(points===null)return;try{await api(`/api/improvement/evidence/${evidence.id}/confirm-points`,{method:"PUT",body:JSON.stringify({points})});notify("业绩分值已确认");await load()}catch(error){notify(error.message)}}
function issueTasks(id) { return data.value.tasks.filter(task => task.issueId === id); }
function statusText(status) { return ({ open: "待整改", training: "培训中", rechecking: "待复评审核", closed: "已闭环", pending: "待完成", completed: "已完成" })[status] || status; }

onMounted(load);
</script>

<template>
  <section class="improvement-page">
    <div class="improvement-metrics">
      <div><strong>{{ openIssues.length }}</strong><span>进行中问题</span></div>
      <div><strong>{{ completedTasks }}/{{ data.tasks.length }}</strong><span>培训完成</span></div>
      <div><strong>{{ closedIssues.length }}</strong><span>已闭环</span></div>
      <div><strong>{{ data.evidence.length }}</strong><span>业绩证据</span></div>
    </div>

    <form class="workflow-create" @submit.prevent="createIssue">
      <div class="section-title"><ClipboardPlus :size="20" /><div><h2>从评价建立整改问题</h2><p>可关联数据分析结论，形成评价依据。</p></div><button class="button-secondary create-evaluation-button" type="button" @click="showEvaluationForm = !showEvaluationForm">{{ showEvaluationForm ? "收起" : "新建初评" }}</button></div>
      <AppSelect v-model="issueForm.evaluationId" :options="evaluationOptions" required aria-label="选择评价记录" />
      <AppSelect v-model="issueForm.sourceAnalysisId" :options="analysisOptions" aria-label="选择关联分析" />
      <input v-model="issueForm.title" required placeholder="问题标题" />
      <AppSelect v-model="issueForm.severity" :options="severityOptions" aria-label="选择问题严重程度" />
      <input v-model="issueForm.improvementTarget" placeholder="改进目标" />
      <input v-model="issueForm.description" placeholder="问题说明" />
      <button type="submit"><ClipboardPlus :size="16" />建立问题</button>
    </form>

    <form v-if="showEvaluationForm" class="quick-evaluation-form" @submit.prevent="createEvaluation">
      <div><strong>新建初次评价</strong><span>关联分析后可自动识别药材和批次，也可手动填写药材。</span></div>
      <AppSelect v-model="evaluationForm.sourceAnalysisId" :options="evaluationAnalysisOptions" aria-label="选择初评分析依据" />
      <input v-model="evaluationForm.herbName" placeholder="药材名称（未选分析时必填）" />
      <input v-model="evaluationForm.indicator" required placeholder="评价指标" />
      <input v-model="evaluationForm.score" type="number" step="0.01" placeholder="初评分数" />
      <input v-model="evaluationForm.result" required placeholder="评价结论" />
      <input v-model="evaluationForm.subjectOwner" placeholder="被评价负责人" />
      <button type="submit">保存初评</button>
    </form>

    <div v-if="loading" class="empty-state">正在加载闭环数据...</div>
    <div v-else-if="loadError" class="empty-state workflow-error"><strong>闭环数据加载失败</strong><span>{{ loadError }}</span><button type="button" @click="load">重新加载</button></div>
    <div v-else class="issue-list">
      <article v-for="issue in openIssues" :key="issue.id" class="issue-panel">
        <header><div><span class="severity" :data-level="issue.severity">{{ issue.severity }}</span><h3>{{ issue.title }}</h3><p>{{ issue.herbName }} · {{ issue.indicator }} · 初评 {{ issue.initialScore ?? "-" }} 分</p></div><span class="status-chip">{{ statusText(issue.status) }}</span></header>
        <div class="evidence-line"><Link2 :size="15" /><span>分析依据：{{ issue.analysisName || "未关联" }}</span><span>目标：{{ issue.improvementTarget || "未填写" }}</span></div>
        <div class="task-list"><div v-for="task in issueTasks(issue.id)" :key="task.id" class="task-row"><GraduationCap :size="16" /><span><strong>{{ task.title }}</strong><small>{{ task.assignee }} · {{ task.materialTitle || "自定义培训" }} · {{ statusText(task.status) }}</small></span><button v-if="task.status !== 'completed'" class="icon-action" type="button" title="完成培训" @click="completeTask(task)"><CheckCircle2 :size="18" /></button></div></div>
        <form v-if="!issue.recheckEvaluationId" class="compact-form" @submit.prevent="addTask(issue)"><input v-model="taskDraft(issue.id).title" required placeholder="培训任务" /><input v-model="taskDraft(issue.id).assignee" required placeholder="负责人" /><AppSelect v-model="taskDraft(issue.id).trainingMaterialId" :options="materialOptions" aria-label="选择培训素材" /><input v-model="taskDraft(issue.id).dueDate" type="date" /><button type="submit">下达任务</button></form>
        <div v-if="issue.taskCount > 0 && issue.completedTaskCount === issue.taskCount && !issue.recheckEvaluationId" class="recheck-actions"><button type="button" @click="autoRecheck(issue)"><RefreshCcw :size="15" />同方案自动复评</button><form class="compact-form recheck" @submit.prevent="recheck(issue)"><input v-model="recheckDraft(issue.id).score" required type="number" step="0.01" placeholder="手工复评分数" /><input v-model="recheckDraft(issue.id).result" required placeholder="手工复评结论" /><button class="button-secondary" type="submit">手工复评</button></form></div>
        <div v-if="issue.recheckEvaluationId" class="recheck-result"><span>复评：{{ issue.recheckScore ?? "待评分" }} 分 · {{ issue.recheckResult || "待审核" }}</span><button v-if="currentRole === 'admin' && issue.status !== 'closed'" type="button" @click="closeIssue(issue)">确认闭环</button></div>
      </article>
    </div>

    <section v-if="closedIssues.length" class="closed-section"><div class="section-title"><CheckCircle2 :size="20" /><div><h2>闭环成果转工作业绩</h2><p>保留问题、培训与复评快照作为业绩证据。</p></div></div><article v-for="issue in closedIssues" :key="issue.id" class="closed-row"><div><strong>{{ issue.title }}</strong><span>初评 {{ issue.initialScore ?? "-" }} → 复评 {{ issue.recheckScore ?? "-" }}</span></div><form @submit.prevent="addEvidence(issue)"><AppSelect v-model="evidenceDraft(issue.id).achievementId" :options="achievementOptions" required aria-label="选择工作业绩" /><input v-model="evidenceDraft(issue.id).evidenceTitle" required placeholder="证据标题" /><input v-model="evidenceDraft(issue.id).evidenceSnapshot" placeholder="成果摘要" /><button type="submit">挂接证据</button></form></article><div class="quantification-list"><article v-for="e in data.evidence.filter(x=>x.suggestedPoints!=null)" :key="e.id"><span>{{e.evidenceTitle}}</span><strong>建议 {{e.suggestedPoints}} 分</strong><b>{{e.confirmedPoints!=null?`已确认 ${e.confirmedPoints} 分`:'待确认'}}</b><button v-if="currentRole==='admin'&&e.confirmedPoints==null" @click="confirmPoints(e)">确认分值</button></article></div></section>
  </section>
</template>
