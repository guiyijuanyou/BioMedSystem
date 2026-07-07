<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import {
  ArrowLeft, BarChart3, ChevronLeft, ChevronRight, Clock3, Copy, Download, Eye, FileText,
  Pencil, PlayCircle, Plus, Search, Trash2, X
} from "lucide-vue-next";
import MapPicker from "@/components/MapPicker.vue";
import { api } from "@/services/api";

const props = defineProps({
  moduleKey: { type: String, required: true },
  config: { type: Object, required: true },
  permissions: { type: Object, default: () => ({}) },
  role: { type: String, default: "admin" },
  editId: String
});

const emit = defineEmits(["notify", "edit-consumed"]);
const items = ref([]);
const teachingResources = ref([]);
const search = ref("");
const editingId = ref(null);
const selectedIds = ref([]);
const selectedGrowthKey = ref("");
const selectedTraceKey = ref("");
const activeGrowthMetric = ref("all");
const learningCourse = ref(null);
const activeLearningResourceId = ref("");
const panelOpen = ref(false);
const panelMode = ref("edit");
const saving = ref(false);
const deleting = ref(false);
const page = ref(1);
const pageSize = ref(8);
const confirmState = reactive({ open: false, ids: [] });
const form = reactive({});

const textareaFields = ["environment", "indicator", "applicationMaterial", "tracking", "transformation", "levelRule", "remark", "conclusion", "eventContent", "reviewComment", "requirements", "applicantRequests", "approvedMembers", "rejectedApplicants"];
const statuses = ["待审核", "已通过", "已发布", "已驳回", "数据采集中", "已归档"];

const filtered = computed(() => {
  const keyword = search.value.trim().toLowerCase();
  return items.value.filter(item => Object.values(item).join(" ").toLowerCase().includes(keyword));
});
const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize.value)));
const pagedItems = computed(() => filtered.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value));
const selectedItems = computed(() => items.value.filter(item => selectedIds.value.includes(item.id)));
const pageSelected = computed(() => pagedItems.value.length > 0 && pagedItems.value.every(item => selectedIds.value.includes(item.id)));
const canCreate = computed(() => props.permissions.create !== false);
const canEdit = computed(() => props.permissions.edit !== false);
const canDuplicate = computed(() => props.permissions.duplicate !== false);
const canDelete = computed(() => props.permissions.delete !== false);
const canExport = computed(() => props.permissions.export !== false);
const canBatchDelete = computed(() => props.permissions.batchDelete !== false && canDelete.value);
const hasRowActions = computed(() => true);
const panelTitle = computed(() => {
  if (panelMode.value === "view") return "记录详情";
  if (panelMode.value === "duplicate") return "创建记录副本";
  return editingId.value ? "编辑记录" : "新增记录";
});
const primaryField = computed(() => props.config.fields[0]?.[0]);
const primaryLabel = computed(() => props.config.fields[0]?.[1] || "记录");
const primaryValue = computed(() => display(form[primaryField.value]));
const isGrowthModule = computed(() => props.moduleKey === "growth-records");
const isTraceModule = computed(() => props.moduleKey === "trace-events");
const isCourseModule = computed(() => props.moduleKey === "courses");
const isStudentCourseModule = computed(() => isCourseModule.value && props.role === "student");
const isProjectModule = computed(() => props.moduleKey === "projects");
const isStudentProjectModule = computed(() => isProjectModule.value && props.role === "student");
const isProjectOwnerModule = computed(() => isProjectModule.value && ["teacher", "researcher"].includes(props.role));
const showProjectWorkflow = computed(() => isProjectModule.value && ["student", "teacher", "researcher"].includes(props.role));
const isSpectrumModule = computed(() => props.moduleKey === "spectrum-comparisons");
const isAnalysisModule = computed(() => props.moduleKey === "growth-analysis");
const insightVisible = computed(() => isGrowthModule.value || isTraceModule.value || isSpectrumModule.value || isAnalysisModule.value);
const insightTitle = computed(() => {
  if (isGrowthModule.value) return "生长数据对比概览";
  if (isTraceModule.value) return "药材溯源链路概览";
  if (isSpectrumModule.value) return "图谱比对概览";
  return "分析结论概览";
});
const insightHint = computed(() => {
  if (isGrowthModule.value) return "根据当前采集记录自动计算温湿度、PH 和采集来源分布";
  if (isTraceModule.value) return "按溯源码串联种植、采集、检测、加工、入库等关键事件";
  if (isSpectrumModule.value) return "汇总图谱相似度、通过情况和待复核样本";
  return "汇总趋势判断和指标分布，便于横向对比";
});
const growthStats = computed(() => {
  const rows = items.value;
  const temps = numericValues(rows, "temperature");
  const humidity = numericValues(rows, "humidity");
  const ph = numericValues(rows, "soilPh");
  const sources = countBy(rows, "collector");
  const districts = countBy(rows, "district");
  const warnings = rows.filter(item =>
    toNumber(item.temperature) > 28 || toNumber(item.humidity) < 55 || toNumber(item.soilPh) < 5.8 || toNumber(item.soilPh) > 7.5
  );
  return {
    count: rows.length,
    avgTemp: average(temps),
    avgHumidity: average(humidity),
    avgPh: average(ph),
    maxTemp: maxValue(temps),
    minTemp: minValue(temps),
    sources,
    districts,
    warnings
  };
});
const spectrumStats = computed(() => {
  const rows = items.value;
  const similarities = numericValues(rows, "similarity");
  const excellent = rows.filter(item => String(item.result || "").includes("通过") || toNumber(item.similarity) >= 90).length;
  return {
    count: rows.length,
    avgSimilarity: average(similarities),
    excellent,
    risk: rows.filter(item => String(item.result || "").includes("复核") || toNumber(item.similarity) < 85).length,
    types: countBy(rows, "spectrumType")
  };
});
const analysisStats = computed(() => ({
  count: items.value.length,
  up: items.value.filter(item => String(item.trend || "").includes("上升")).length,
  down: items.value.filter(item => String(item.trend || "").includes("下降")).length,
  stable: items.value.filter(item => String(item.trend || "").includes("稳定")).length,
  indicators: countBy(items.value, "indicator")
}));
const traceGroups = computed(() => {
  const groups = {};
  items.value.forEach(item => {
    const traceCode = item.traceCode || "未填写溯源码";
    if (!groups[traceCode]) groups[traceCode] = { key: traceCode, traceCode, herbName: item.herbName || "未填写药材", rows: [] };
    groups[traceCode].rows.push(item);
    if (item.herbName) groups[traceCode].herbName = item.herbName;
  });
  return Object.values(groups)
    .map(group => ({
      ...group,
      rows: group.rows.slice().sort((a, b) => new Date(a.eventTime || a.createdAt || 0) - new Date(b.eventTime || b.createdAt || 0))
    }))
    .sort((a, b) => b.rows.length - a.rows.length || a.traceCode.localeCompare(b.traceCode, "zh-Hans-CN"));
});
const selectedTraceGroup = computed(() =>
  traceGroups.value.find(group => group.key === selectedTraceKey.value) || traceGroups.value[0]
);
const traceStats = computed(() => {
  const eventTypes = countBy(items.value, "eventType");
  const latest = items.value.slice().sort((a, b) => new Date(b.eventTime || b.createdAt || 0) - new Date(a.eventTime || a.createdAt || 0))[0];
  return {
    count: items.value.length,
    traceCodeCount: traceGroups.value.length,
    eventTypes,
    latest
  };
});
const growthSeriesGroups = computed(() => {
  const groups = {};
  items.value.forEach(item => {
    const herbName = item.herbName || "未填写药材";
    const district = item.district || "未填写地区";
    const key = `${district}__${herbName}`;
    if (!groups[key]) groups[key] = { key, herbName, district, rows: [] };
    groups[key].rows.push(item);
  });
  return Object.values(groups)
    .map(group => ({
      ...group,
      rows: group.rows.slice().sort((a, b) => new Date(a.recordedAt || a.createdAt || 0) - new Date(b.recordedAt || b.createdAt || 0))
    }))
    .sort((a, b) => b.rows.length - a.rows.length || a.district.localeCompare(b.district, "zh-Hans-CN"));
});
const selectedGrowthGroup = computed(() =>
  growthSeriesGroups.value.find(group => group.key === selectedGrowthKey.value) || growthSeriesGroups.value[0]
);
const selectedGrowthRows = computed(() => selectedGrowthGroup.value?.rows || []);
const latestGrowthComparison = computed(() => {
  const rows = selectedGrowthRows.value;
  const latest = rows[rows.length - 1];
  const previous = rows[rows.length - 2];
  return {
    latest,
    previous,
    temperature: delta(latest?.temperature, previous?.temperature),
    humidity: delta(latest?.humidity, previous?.humidity),
    soilPh: delta(latest?.soilPh, previous?.soilPh)
  };
});
const growthChartSeries = computed(() => {
  const rows = selectedGrowthRows.value;
  return {
    temperature: chartLine(rows, "temperature"),
    humidity: chartLine(rows, "humidity"),
    soilPh: chartLine(rows, "soilPh")
  };
});
const growthChartMetrics = [
  { key: "temperature", label: "温度", lineClass: "temperature-line", dotClass: "temperature-dot", pointClass: "temperature-point" },
  { key: "humidity", label: "湿度", lineClass: "humidity-line", dotClass: "humidity-dot", pointClass: "humidity-point" },
  { key: "soilPh", label: "土壤 PH", lineClass: "ph-line", dotClass: "ph-dot", pointClass: "ph-point" }
];
const visibleGrowthMetrics = computed(() =>
  activeGrowthMetric.value === "all"
    ? growthChartMetrics
    : growthChartMetrics.filter(metric => metric.key === activeGrowthMetric.value)
);
const selectedGrowthMetric = computed(() =>
  growthChartMetrics.find(metric => metric.key === activeGrowthMetric.value)
);
const selectedGrowthSeries = computed(() =>
  activeGrowthMetric.value === "all" ? null : growthChartSeries.value[activeGrowthMetric.value]
);
const chartLabels = computed(() => selectedGrowthRows.value.map((item, index) => ({
  text: formatShortDate(item.recordedAt || item.createdAt),
  x: chartX(index, selectedGrowthRows.value.length)
})));
const publishedTeachingResources = computed(() =>
  teachingResources.value.filter(resource => isPublished(resource.status))
);
const visibleCourseRows = computed(() =>
  isStudentCourseModule.value ? items.value.filter(item => isPublished(item.status)) : items.value
);
const courseCards = computed(() => visibleCourseRows.value.map((item, index) => {
  const resources = courseResources(item.title);
  const videos = resources.filter(isVideoResource);
  return {
    ...item,
    resources,
    videos,
    coverClass: `course-cover-${index % 6}`,
    term: item.term || "2025-2026学年秋季",
    platform: item.platform || "线上课程学习",
    views: item.views || formatCourseViews(index)
  };
}));
const drawerCourseResources = computed(() => isCourseModule.value ? courseResources(form.title) : []);
const drawerCourseVideos = computed(() => drawerCourseResources.value.filter(isVideoResource));
const learningResources = computed(() => learningCourse.value ? courseResources(learningCourse.value.title) : []);
const learningVideos = computed(() => learningResources.value.filter(isVideoResource));
const activeLearningResource = computed(() =>
  learningResources.value.find(resource => resource.id === activeLearningResourceId.value)
  || learningVideos.value[0]
  || learningResources.value[0]
);
const publishedProjects = computed(() => items.value.filter(item => isPublished(item.status)));
const projectCards = computed(() => (props.role === "student" ? publishedProjects.value : items.value));

function defaultValue(name) {
  const values = {
    recordedAt: new Date().toISOString().slice(0, 19),
    eventTime: new Date().toISOString().slice(0, 19),
    comparedAt: new Date().toISOString().slice(0, 19),
    analyzedAt: new Date().toISOString().slice(0, 19),
    collector: "电脑终端录入",
    temperature: "20.0",
    humidity: "80",
    soilPh: "6.5",
    growthStage: "生长期",
    eventType: "采集",
    resourceType: "教学视频",
    uploaderRole: "教师",
    status: "待审核",
    requirements: "面向对中药材研究感兴趣的学生，需具备基础实验记录能力，能够按要求参与数据采集和阶段汇报。",
    applicantRequests: "",
    approvedMembers: "",
    rejectedApplicants: "",
    operator: "系统管理员",
    spectrumType: "HPLC 指纹图谱",
    result: "待复核",
    analyst: "系统管理员",
    effectiveDate: new Date().toISOString().slice(0, 10)
  };
  return values[name] || "";
}

function toNumber(value) {
  const number = Number.parseFloat(value);
  return Number.isFinite(number) ? number : null;
}

function numericValues(rows, key) {
  return rows.map(item => toNumber(item[key])).filter(value => value !== null);
}

function average(values) {
  if (!values.length) return "-";
  return (values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(1);
}

function maxValue(values) {
  return values.length ? Math.max(...values).toFixed(1) : "-";
}

function minValue(values) {
  return values.length ? Math.min(...values).toFixed(1) : "-";
}

function countBy(rows, key) {
  return rows.reduce((result, item) => {
    const name = item[key] || "未填写";
    result[name] = (result[name] || 0) + 1;
    return result;
  }, {});
}

function delta(current, previous) {
  const currentNumber = toNumber(current);
  const previousNumber = toNumber(previous);
  if (currentNumber === null || previousNumber === null) return { text: "暂无对比", tone: "muted" };
  const value = currentNumber - previousNumber;
  if (Math.abs(value) < 0.05) return { text: "基本持平", tone: "stable" };
  return {
    text: `${value > 0 ? "+" : ""}${value.toFixed(1)}`,
    tone: value > 0 ? "up" : "down"
  };
}

function formatShortDate(value) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value).slice(5, 10);
  return `${date.getMonth() + 1}/${date.getDate()}`;
}

function chartX(index, total) {
  return total === 1 ? 50 : 8 + (index / (total - 1)) * 84;
}

function chartLine(rows, key) {
  const values = rows.map(item => toNumber(item[key]));
  const valid = values.filter(value => value !== null);
  if (!valid.length) return { points: "", nodes: [] };
  const min = Math.min(...valid);
  const max = Math.max(...valid);
  const span = max - min || 1;
  const nodes = values.map((value, index) => {
    const x = chartX(index, rows.length);
    const y = value === null ? 50 : 90 - ((value - min) / span) * 72;
    return {
      x,
      y,
      value,
      text: value === null ? "-" : String(value)
    };
  });
  return {
    points: nodes.map(point => `${point.x.toFixed(2)},${point.y.toFixed(2)}`).join(" "),
    nodes
  };
}

function selectGrowthGroup(key) {
  selectedGrowthKey.value = key;
}

function selectGrowthMetric(key) {
  activeGrowthMetric.value = activeGrowthMetric.value === key ? "all" : key;
}

function formatCourseViews(index) {
  const views = [4167, 3482, "1.8w+", 489, 4898, 5314, 8847, 7854, 6248, "1.6w+"];
  return views[index % views.length];
}

function isPublished(status) {
  return String(status || "").includes("已发布") || String(status || "").includes("已通过");
}

function isVideoResource(resource) {
  return String(resource.resourceType || "").includes("视频");
}

function courseResources(courseTitle) {
  if (!courseTitle) return [];
  return publishedTeachingResources.value.filter(resource => resource.courseTitle === courseTitle);
}

function resourcePreviewUrl(resource) {
  return resource.previewUrl || resource.videoUrl || resource.fileUrl || "";
}

function resourceDownloadUrl(resource) {
  return resource.downloadUrl || resource.fileUrl || resource.videoUrl || "";
}

function splitNames(value) {
  return String(value || "")
    .split(/[、,，;\n]/)
    .map(item => item.trim())
    .filter(Boolean);
}

function joinNames(values) {
  return [...new Set(values.map(item => item.trim()).filter(Boolean))].join("、");
}

function projectApplicants(project) {
  return splitNames(project.applicantRequests);
}

function projectMembers(project) {
  return splitNames(project.approvedMembers);
}

function projectRejectedApplicants(project) {
  return splitNames(project.rejectedApplicants);
}

function hasApplied(project) {
  return projectApplicants(project).includes("当前学生") || projectMembers(project).includes("当前学生");
}

function isProjectMember(project) {
  return projectMembers(project).includes("当前学生");
}

function isProjectRejected(project) {
  return projectRejectedApplicants(project).includes("当前学生");
}

async function updateProject(project, changes, message) {
  try {
    const payload = {};
    props.config.fields.forEach(([name]) => payload[name] = project[name] ?? "");
    Object.assign(payload, changes, { id: project.id });
    await api(`/api/${props.moduleKey}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
    emit("notify", message);
    await load();
  } catch (error) {
    emit("notify", error.message);
  }
}

async function applyProject(project) {
  if (!isPublished(project.status)) {
    emit("notify", "课题发布后才可申请加入");
    return;
  }
  if (hasApplied(project)) {
    emit("notify", "你已提交过申请或已加入该课题");
    return;
  }
  if (isProjectRejected(project)) {
    emit("notify", "你的申请已被拒绝，请联系课题负责人后再处理");
    return;
  }
  await updateProject(
    project,
    { applicantRequests: joinNames([...projectApplicants(project), "当前学生"]) },
    "加入课题申请已提交"
  );
}

async function approveProjectApplicant(project, studentName) {
  await updateProject(
    project,
    {
      applicantRequests: joinNames(projectApplicants(project).filter(name => name !== studentName)),
      approvedMembers: joinNames([...projectMembers(project), studentName])
    },
    `已同意 ${studentName} 加入课题`
  );
}

async function rejectProjectApplicant(project, studentName) {
  await updateProject(
    project,
    {
      applicantRequests: joinNames(projectApplicants(project).filter(name => name !== studentName)),
      rejectedApplicants: joinNames([...projectRejectedApplicants(project), studentName])
    },
    `已拒绝 ${studentName} 的加入申请`
  );
}

function openCourseLearning(course) {
  learningCourse.value = course;
  const firstVideo = course.videos?.[0] || course.resources?.[0] || null;
  activeLearningResourceId.value = firstVideo?.id || "";
}

function closeCourseLearning() {
  learningCourse.value = null;
  activeLearningResourceId.value = "";
}

function openCourse(course) {
  if (isStudentCourseModule.value) openCourseLearning(course);
  else openView(course);
}

function fillForm(item = null) {
  editingId.value = item?.id || null;
  Object.keys(form).forEach(key => delete form[key]);
  props.config.fields.forEach(([name]) => {
    form[name] = item?.[name] ?? defaultValue(name);
  });
  if (item) {
    form.createdAt = item.createdAt || "";
    form.updatedAt = item.updatedAt || "";
  }
}

function openCreate() {
  fillForm();
  panelMode.value = "edit";
  panelOpen.value = true;
}

function openView(item) {
  fillForm(item);
  panelMode.value = "view";
  panelOpen.value = true;
}

function openEdit(item) {
  fillForm(item);
  panelMode.value = "edit";
  panelOpen.value = true;
}

function duplicate(item) {
  fillForm(item);
  editingId.value = null;
  delete form.createdAt;
  delete form.updatedAt;
  const nameField = props.config.fields[0]?.[0];
  if (nameField && form[nameField]) form[nameField] = `${form[nameField]}（副本）`;
  panelMode.value = "duplicate";
  panelOpen.value = true;
}

function closePanel() {
  if (saving.value) return;
  panelOpen.value = false;
}

async function load() {
  try {
    const [result, resourceResult] = await Promise.all([
      api(`/api/${props.moduleKey}`),
      props.moduleKey === "courses" ? api("/api/teaching-resources") : Promise.resolve({ items: [] })
    ]);
    items.value = result.items || [];
    teachingResources.value = resourceResult.items || [];
    selectedIds.value = selectedIds.value.filter(id => items.value.some(item => item.id === id));
    if (page.value > totalPages.value) page.value = totalPages.value;
    if (props.editId) {
      const row = items.value.find(item => item.id === props.editId);
      if (row) openEdit(row);
      emit("edit-consumed");
    }
  } catch (error) {
    emit("notify", `数据加载失败：${error.message}`);
  }
}

async function save() {
  saving.value = true;
  try {
    const payload = {};
    props.config.fields.forEach(([name]) => payload[name] = form[name] ?? "");
    if (editingId.value) payload.id = editingId.value;
    await api(`/api/${props.moduleKey}`, {
      method: editingId.value ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    emit("notify", editingId.value ? "修改已保存" : "新增记录已保存");
    panelOpen.value = false;
    await load();
  } catch (error) {
    emit("notify", error.message);
  } finally {
    saving.value = false;
  }
}

function requestDelete(ids) {
  confirmState.ids = [...ids];
  confirmState.open = true;
}

async function executeDelete() {
  deleting.value = true;
  try {
    await Promise.all(confirmState.ids.map(id => api(`/api/${props.moduleKey}/${id}`, { method: "DELETE" })));
    const count = confirmState.ids.length;
    selectedIds.value = selectedIds.value.filter(id => !confirmState.ids.includes(id));
    confirmState.open = false;
    panelOpen.value = false;
    await load();
    emit("notify", count > 1 ? `已删除 ${count} 条记录` : "记录已删除");
  } catch (error) {
    emit("notify", error.message);
  } finally {
    deleting.value = false;
  }
}

function togglePageSelection() {
  const ids = pagedItems.value.map(item => item.id);
  if (pageSelected.value) {
    selectedIds.value = selectedIds.value.filter(id => !ids.includes(id));
  } else {
    selectedIds.value = [...new Set([...selectedIds.value, ...ids])];
  }
}

function exportCsv(rows = filtered.value) {
  if (!rows.length) {
    emit("notify", "当前没有可导出的记录");
    return;
  }
  const headers = props.config.fields.map(([, label]) => label);
  const fields = props.config.fields.map(([name]) => name);
  const escape = value => `"${String(value ?? "").replaceAll('"', '""')}"`;
  const csv = [headers.map(escape).join(","), ...rows.map(item => fields.map(name => escape(item[name])).join(","))].join("\r\n");
  const url = URL.createObjectURL(new Blob(["\ufeff", csv], { type: "text/csv;charset=utf-8" }));
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = `${props.config.title}-${new Date().toISOString().slice(0, 10)}.csv`;
  anchor.click();
  URL.revokeObjectURL(url);
  emit("notify", `已导出 ${rows.length} 条记录`);
}

function display(value) {
  return value === undefined || value === null || value === "" ? "-" : value;
}

onMounted(load);
watch(() => props.moduleKey, () => {
  search.value = "";
  selectedIds.value = [];
  closeCourseLearning();
  page.value = 1;
  panelOpen.value = false;
  load();
});
watch(search, () => page.value = 1);
watch(growthSeriesGroups, groups => {
  if (!groups.length) {
    selectedGrowthKey.value = "";
    return;
  }
  if (!groups.some(group => group.key === selectedGrowthKey.value)) {
    selectedGrowthKey.value = groups[0].key;
  }
}, { immediate: true });
watch(traceGroups, groups => {
  if (!groups.length) {
    selectedTraceKey.value = "";
    return;
  }
  if (!groups.some(group => group.key === selectedTraceKey.value)) {
    selectedTraceKey.value = groups[0].key;
  }
}, { immediate: true });
watch(() => props.editId, id => {
  if (!id) return;
  const row = items.value.find(item => item.id === id);
  if (row) openEdit(row);
  emit("edit-consumed");
});
</script>

<template>
  <section class="panel resource-workbench">
    <div class="panel-head resource-head">
      <div><h2>{{ config.title }}</h2><span>{{ config.hint }}</span></div>
      <button v-if="canCreate" type="button" @click="openCreate"><Plus :size="16" />新增记录</button>
    </div>

    <section v-if="insightVisible" class="insight-panel">
      <div class="insight-heading">
        <span><BarChart3 :size="18" /></span>
        <div>
          <strong>{{ insightTitle }}</strong>
          <small>{{ insightHint }}</small>
        </div>
      </div>

      <div v-if="isGrowthModule" class="insight-grid">
        <article><span>记录数量</span><strong>{{ growthStats.count }}</strong><small>条生长数据</small></article>
        <article><span>平均温度</span><strong>{{ growthStats.avgTemp }}</strong><small>最高 {{ growthStats.maxTemp }} / 最低 {{ growthStats.minTemp }}</small></article>
        <article><span>平均湿度</span><strong>{{ growthStats.avgHumidity }}</strong><small>相对湿度 %</small></article>
        <article><span>平均 PH</span><strong>{{ growthStats.avgPh }}</strong><small>土壤酸碱度</small></article>
      </div>
      <div v-else-if="isTraceModule" class="insight-grid">
        <article><span>溯源事件</span><strong>{{ traceStats.count }}</strong><small>条链路记录</small></article>
        <article><span>溯源码数量</span><strong>{{ traceStats.traceCodeCount }}</strong><small>个药材批次</small></article>
        <article><span>最近事件</span><strong>{{ display(traceStats.latest?.eventType) }}</strong><small>{{ display(traceStats.latest?.eventTime) }}</small></article>
        <article><span>当前链路</span><strong>{{ selectedTraceGroup?.rows.length || 0 }}</strong><small>{{ selectedTraceGroup?.traceCode || "暂无" }}</small></article>
      </div>
      <div v-else-if="isSpectrumModule" class="insight-grid">
        <article><span>比对样本</span><strong>{{ spectrumStats.count }}</strong><small>条图谱记录</small></article>
        <article><span>平均相似度</span><strong>{{ spectrumStats.avgSimilarity }}</strong><small>百分制结果</small></article>
        <article><span>通过样本</span><strong>{{ spectrumStats.excellent }}</strong><small>相似度较高或已通过</small></article>
        <article><span>待复核</span><strong>{{ spectrumStats.risk }}</strong><small>建议人工确认</small></article>
      </div>
      <div v-else class="insight-grid">
        <article><span>分析记录</span><strong>{{ analysisStats.count }}</strong><small>条对比结论</small></article>
        <article><span>上升趋势</span><strong>{{ analysisStats.up }}</strong><small>指标改善或增长</small></article>
        <article><span>下降趋势</span><strong>{{ analysisStats.down }}</strong><small>需要关注变化</small></article>
        <article><span>稳定趋势</span><strong>{{ analysisStats.stable }}</strong><small>变化较小</small></article>
      </div>

      <section v-if="isGrowthModule" class="growth-trend-card">
        <div class="trend-toolbar">
          <div>
            <strong>地区药材生长档案</strong>
            <small>同一地区、同一药材可连续记录多次，并与上一条记录自动对比</small>
          </div>
          <select :value="selectedGrowthGroup?.key || ''" @change="selectGrowthGroup($event.target.value)">
            <option v-for="group in growthSeriesGroups" :key="group.key" :value="group.key">
              {{ group.district }} / {{ group.herbName }}（{{ group.rows.length }} 次）
            </option>
          </select>
        </div>

        <div v-if="selectedGrowthGroup" class="trend-body">
          <div class="trend-summary">
            <article>
              <span>当前档案</span>
              <strong>{{ selectedGrowthGroup.district }} / {{ selectedGrowthGroup.herbName }}</strong>
              <small>共 {{ selectedGrowthRows.length }} 次记录</small>
            </article>
            <article>
              <span>最新温度</span>
              <strong>{{ display(latestGrowthComparison.latest?.temperature) }}</strong>
              <small :class="latestGrowthComparison.temperature.tone">较上次 {{ latestGrowthComparison.temperature.text }}</small>
            </article>
            <article>
              <span>最新湿度</span>
              <strong>{{ display(latestGrowthComparison.latest?.humidity) }}</strong>
              <small :class="latestGrowthComparison.humidity.tone">较上次 {{ latestGrowthComparison.humidity.text }}</small>
            </article>
            <article>
              <span>最新 PH</span>
              <strong>{{ display(latestGrowthComparison.latest?.soilPh) }}</strong>
              <small :class="latestGrowthComparison.soilPh.tone">较上次 {{ latestGrowthComparison.soilPh.text }}</small>
            </article>
          </div>

          <div class="line-chart" aria-label="生长数据趋势折线图">
            <div class="chart-plot">
              <svg viewBox="0 0 100 100" preserveAspectRatio="none">
                <line x1="8" y1="90" x2="96" y2="90" />
                <line x1="8" y1="14" x2="8" y2="90" />
                <template v-if="selectedGrowthSeries">
                  <line
                    v-for="(point, index) in selectedGrowthSeries.nodes"
                    :key="`guide-${index}`"
                    class="chart-guide"
                    :x1="point.x"
                    :y1="point.y"
                    :x2="point.x"
                    y2="90"
                  />
                </template>
                <template v-for="metric in visibleGrowthMetrics" :key="metric.key">
                  <polyline
                    v-if="growthChartSeries[metric.key]?.points"
                    :class="metric.lineClass"
                    :points="growthChartSeries[metric.key].points"
                  />
                </template>
              </svg>
              <div v-if="selectedGrowthSeries" class="chart-values">
                <i
                  v-for="(point, index) in selectedGrowthSeries.nodes"
                  :key="`marker-${index}`"
                  class="chart-marker"
                  :class="selectedGrowthMetric?.pointClass"
                  :style="{ left: `${point.x}%`, top: `${point.y}%` }"
                ></i>
                <span
                  v-for="(point, index) in selectedGrowthSeries.nodes"
                  :key="`value-${index}`"
                  :style="{ left: `${point.x}%`, top: `${point.y}%` }"
                >{{ point.text }}</span>
              </div>
            </div>
            <div class="chart-legend">
              <button
                v-for="metric in growthChartMetrics"
                :key="metric.key"
                type="button"
                :class="{ active: activeGrowthMetric === metric.key }"
                @click="selectGrowthMetric(metric.key)"
              >
                <i :class="metric.dotClass"></i>{{ metric.label }}
              </button>
            </div>
            <div class="chart-labels">
              <span
                v-for="(label, index) in chartLabels"
                :key="`${label.text}-${index}`"
                :style="{ left: `${label.x}%` }"
              >{{ label.text }}</span>
            </div>
          </div>
        </div>
        <p v-else class="empty-state">暂无可统计的生长记录</p>
      </section>

      <section v-if="isTraceModule" class="traceability-card">
        <div class="trend-toolbar">
          <div>
            <strong>溯源码流转链路</strong>
            <small>同一溯源码下的事件会按时间顺序形成完整追溯记录</small>
          </div>
          <select :value="selectedTraceGroup?.key || ''" @change="selectedTraceKey = $event.target.value">
            <option v-for="group in traceGroups" :key="group.key" :value="group.key">
              {{ group.traceCode }} / {{ group.herbName }}（{{ group.rows.length }} 环节）
            </option>
          </select>
        </div>

        <div v-if="selectedTraceGroup" class="trace-timeline">
          <article v-for="event in selectedTraceGroup.rows" :key="event.id">
            <span>{{ display(event.eventType) }}</span>
            <div>
              <strong>{{ display(event.herbName) }} · {{ display(event.traceCode) }}</strong>
              <p>{{ display(event.eventContent) }}</p>
              <small>{{ display(event.eventTime) }} / {{ display(event.location) }} / {{ display(event.operator) }}</small>
            </div>
          </article>
        </div>
        <p v-else class="empty-state">暂无可展示的溯源链路</p>
      </section>

      <div v-if="!isGrowthModule" class="insight-lists">
        <div v-if="isTraceModule">
          <strong>事件类型</strong>
          <span v-for="(count, name) in traceStats.eventTypes" :key="name">{{ name }}：{{ count }}</span>
        </div>
        <div v-if="isSpectrumModule">
          <strong>图谱类型</strong>
          <span v-for="(count, name) in spectrumStats.types" :key="name">{{ name }}：{{ count }}</span>
        </div>
        <div v-if="isAnalysisModule">
          <strong>分析指标</strong>
          <span v-for="(count, name) in analysisStats.indicators" :key="name">{{ name }}：{{ count }}</span>
        </div>
      </div>
    </section>

    <section v-if="isStudentCourseModule && learningCourse" class="course-player-page">
      <button class="button-secondary course-back-button" type="button" @click="closeCourseLearning"><ArrowLeft :size="16" />返回课程列表</button>
      <div class="course-player-layout">
        <main class="course-player-main">
          <div class="course-player-screen">
            <video
              v-if="activeLearningResource && isVideoResource(activeLearningResource) && resourcePreviewUrl(activeLearningResource)"
              controls
              autoplay
              preload="metadata"
              :src="resourcePreviewUrl(activeLearningResource)"
            ></video>
            <div v-else class="course-video-placeholder">
              <PlayCircle :size="54" />
              <span>{{ activeLearningResource ? "该资源暂无可播放视频地址" : "该课程暂无已发布视频" }}</span>
            </div>
          </div>
          <div class="course-player-info">
            <span>{{ learningCourse.teacher }} · {{ learningCourse.hours }} 学时</span>
            <h3>{{ learningCourse.title }}</h3>
            <p>{{ activeLearningResource?.reviewComment || "请选择右侧已发布教学资源进行学习。" }}</p>
          </div>
        </main>
        <aside class="course-player-sidebar">
          <section>
            <strong>课程视频</strong>
            <button
              v-for="resource in learningVideos"
              :key="resource.id"
              type="button"
              :class="{ active: activeLearningResourceId === resource.id }"
              @click="activeLearningResourceId = resource.id"
            >
              <PlayCircle :size="16" />
              <span>{{ resource.title }}</span>
            </button>
            <p v-if="!learningVideos.length" class="empty-state">暂无已发布视频</p>
          </section>
          <section>
            <strong>课程资料</strong>
            <article v-for="resource in learningResources" :key="resource.id" class="course-player-material">
              <div><span>{{ resource.title }}</span><small>{{ resource.resourceType }} · {{ resource.uploader }}</small></div>
              <div>
                <a v-if="resourcePreviewUrl(resource)" :href="resourcePreviewUrl(resource)" target="_blank" rel="noopener"><button class="icon-button" type="button" title="查看"><Eye :size="15" /></button></a>
                <a v-if="resourceDownloadUrl(resource)" :href="resourceDownloadUrl(resource)"><button class="icon-button" type="button" title="下载"><Download :size="15" /></button></a>
              </div>
            </article>
            <p v-if="!learningResources.length" class="empty-state">暂无已发布资料</p>
          </section>
        </aside>
      </div>
    </section>

    <section v-else-if="isCourseModule" class="course-catalog">
      <article
        v-for="course in courseCards"
        :key="course.id"
        class="course-card"
        @click="openCourse(course)"
      >
        <div class="course-cover" :class="course.coverClass">
          <span>{{ course.term }}</span>
          <strong v-if="isStudentCourseModule" class="course-play-badge"><PlayCircle :size="18" />{{ course.videos.length }} 个视频</strong>
        </div>
        <div class="course-card-body">
          <strong>{{ display(course.title) }}</strong>
          <span>{{ display(course.teacher) }}</span>
          <div v-if="isStudentCourseModule" class="course-resource-chips">
            <small v-for="resource in course.resources.slice(0, 3)" :key="resource.id">{{ resource.resourceType }}</small>
            <small v-if="!course.resources.length">暂无已发布资源</small>
          </div>
          <footer>
            <small>{{ display(course.platform) }}</small>
            <small><Eye :size="16" />{{ isStudentCourseModule ? `${course.resources.length} 个资源` : course.views }}</small>
          </footer>
        </div>
      </article>
    </section>

    <p v-if="isStudentCourseModule && !courseCards.length" class="empty-state">暂无已发布课程</p>

    <section v-if="showProjectWorkflow" class="project-workflow">
      <article v-for="project in projectCards" :key="project.id" class="project-card">
        <header>
          <div>
            <span class="status">{{ display(project.status) }}</span>
            <h3>{{ display(project.title) }}</h3>
          </div>
          <small>{{ display(project.leader) }}</small>
        </header>
        <p>{{ display(project.requirements) }}</p>
        <dl>
          <div><dt>研究阶段</dt><dd>{{ display(project.stage) }}</dd></div>
          <div><dt>成果转化</dt><dd>{{ display(project.transformation) }}</dd></div>
          <div><dt>已加入</dt><dd>{{ projectMembers(project).length || 0 }} 人</dd></div>
          <div><dt>待审批</dt><dd>{{ projectApplicants(project).length || 0 }} 人</dd></div>
          <div><dt>已拒绝</dt><dd>{{ projectRejectedApplicants(project).length || 0 }} 人</dd></div>
        </dl>
        <footer v-if="isStudentProjectModule">
          <button v-if="isProjectMember(project)" class="button-secondary" type="button" disabled>已加入课题</button>
          <button v-else-if="isProjectRejected(project)" class="button-secondary" type="button" disabled>申请已拒绝</button>
          <button v-else-if="hasApplied(project)" class="button-secondary" type="button" disabled>申请待审批</button>
          <button v-else type="button" @click="applyProject(project)">申请加入</button>
        </footer>
        <footer v-else-if="isProjectOwnerModule" class="project-applicant-actions">
          <template v-if="projectApplicants(project).length">
            <div
              v-for="studentName in projectApplicants(project)"
              :key="studentName"
              class="project-applicant-row"
            >
              <span>{{ studentName }}</span>
              <button type="button" @click="approveProjectApplicant(project, studentName)">同意</button>
              <button class="danger-ghost" type="button" @click="rejectProjectApplicant(project, studentName)">拒绝</button>
            </div>
          </template>
          <button v-else class="button-secondary" type="button" disabled>暂无学生申请</button>
          <button class="button-secondary" type="button" @click="openView(project)">查看详情</button>
        </footer>
      </article>
      <p v-if="!projectCards.length" class="empty-state">{{ isStudentProjectModule ? "暂无已发布课题" : "暂无课题记录" }}</p>
    </section>

    <div v-if="!isStudentCourseModule && !showProjectWorkflow" class="table-tools">
      <div class="input-with-icon table-search">
        <Search :size="16" />
        <input v-model="search" type="search" placeholder="搜索名称、地区、负责人、状态">
      </div>
      <span class="record-count">共 {{ filtered.length }} 条记录</span>
      <button v-if="canExport" class="button-secondary" type="button" @click="exportCsv(selectedItems.length ? selectedItems : filtered)">
        <Download :size="16" />{{ selectedItems.length ? `导出已选 (${selectedItems.length})` : "导出数据" }}
      </button>
    </div>

    <div v-if="!isStudentCourseModule && !showProjectWorkflow" class="selection-slot">
      <div v-if="selectedIds.length && canBatchDelete" class="selection-bar">
        <span>已选择 <strong>{{ selectedIds.length }}</strong> 条记录</span>
        <button class="danger-button" type="button" @click="requestDelete(selectedIds)"><Trash2 :size="15" />批量删除</button>
        <button class="button-secondary" type="button" @click="selectedIds = []">取消选择</button>
      </div>
    </div>

    <div v-if="!isStudentCourseModule && !showProjectWorkflow" class="table-wrap resource-table">
      <table>
        <thead>
          <tr>
            <th v-if="canBatchDelete" class="checkbox-cell"><input type="checkbox" :checked="pageSelected" aria-label="选择当前页" @change="togglePageSelection"></th>
            <th v-for="[, label] in config.fields" :key="label">{{ label }}</th>
            <th v-if="hasRowActions" class="sticky-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedItems" :key="item.id" :class="{ selected: selectedIds.includes(item.id) }">
            <td v-if="canBatchDelete" class="checkbox-cell"><input v-model="selectedIds" type="checkbox" :value="item.id" :aria-label="`选择${item.id}`"></td>
            <td v-for="[name] in config.fields" :key="name">
              <span v-if="['status', 'result', 'level'].includes(name)" class="status">{{ display(item[name]) }}</span>
              <template v-else>{{ display(item[name]) }}</template>
            </td>
            <td v-if="hasRowActions" class="sticky-action">
              <div class="row-actions">
                <button class="icon-button" type="button" title="查看详情" @click="openView(item)"><Eye :size="16" /></button>
                <button v-if="canEdit" class="icon-button" type="button" title="编辑记录" @click="openEdit(item)"><Pencil :size="16" /></button>
                <button v-if="canDuplicate" class="icon-button" type="button" title="复制记录" @click="duplicate(item)"><Copy :size="16" /></button>
                <button v-if="canDelete" class="icon-button danger" type="button" title="删除记录" @click="requestDelete([item.id])"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
          <tr v-if="!pagedItems.length">
            <td :colspan="config.fields.length + (canBatchDelete ? 1 : 0) + (hasRowActions ? 1 : 0)" class="empty-cell">暂无符合条件的数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <footer v-if="!isStudentCourseModule && !showProjectWorkflow" class="pagination">
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <label>每页<select v-model.number="pageSize" @change="page = 1"><option :value="8">8</option><option :value="15">15</option><option :value="30">30</option></select>条</label>
      <div>
        <button class="icon-button" type="button" title="上一页" :disabled="page <= 1" @click="page--"><ChevronLeft :size="17" /></button>
        <button class="icon-button" type="button" title="下一页" :disabled="page >= totalPages" @click="page++"><ChevronRight :size="17" /></button>
      </div>
    </footer>
  </section>

  <Teleport to="body">
    <div v-if="panelOpen" class="record-drawer-layer" @click.self="closePanel">
      <aside class="record-drawer" role="dialog" aria-modal="true" :aria-label="panelTitle">
        <header class="drawer-header">
          <div class="drawer-title-group">
            <span class="drawer-title-icon"><FileText :size="19" /></span>
            <div><small>{{ config.title }}</small><h2>{{ panelTitle }}</h2></div>
          </div>
          <button class="icon-button" type="button" title="关闭" @click="closePanel"><X :size="20" /></button>
        </header>

        <div v-if="panelMode === 'view'" class="record-detail-body">
          <section class="detail-summary">
            <span>{{ primaryLabel }}</span>
            <h3>{{ primaryValue }}</h3>
            <p>{{ config.hint }}</p>
            <div>
              <span><Clock3 :size="13" />创建于 {{ display(form.createdAt) }}</span>
              <span v-if="form.updatedAt"><Clock3 :size="13" />更新于 {{ form.updatedAt }}</span>
            </div>
          </section>

          <section v-if="isCourseModule" class="course-learning-detail">
            <div class="form-section-heading">
              <div><strong>已发布教学资源</strong><span>管理员审核通过后，学生可在这里学习视频和资料</span></div>
              <span>{{ drawerCourseResources.length }} 个资源</span>
            </div>
            <div v-if="drawerCourseVideos.length" class="course-video-list">
              <article v-for="resource in drawerCourseVideos" :key="resource.id" class="course-video-item">
                <video v-if="resourcePreviewUrl(resource)" controls preload="metadata" :src="resourcePreviewUrl(resource)"></video>
                <div v-else class="course-video-placeholder"><PlayCircle :size="42" /><span>视频地址待绑定</span></div>
                <div>
                  <strong>{{ resource.title }}</strong>
                  <span>{{ resource.uploader }} · {{ resource.publishedAt || "已发布" }}</span>
                  <p>{{ resource.reviewComment || "该教学视频已发布至课程学习。" }}</p>
                </div>
              </article>
            </div>
            <div v-if="drawerCourseResources.length" class="course-material-list">
              <article v-for="resource in drawerCourseResources" :key="resource.id">
                <div><strong>{{ resource.title }}</strong><span>{{ resource.resourceType }} · {{ resource.uploader }}</span></div>
                <div>
                  <a v-if="resourcePreviewUrl(resource)" :href="resourcePreviewUrl(resource)" target="_blank" rel="noopener"><button class="button-secondary" type="button"><Eye :size="15" />查看</button></a>
                  <a v-if="resourceDownloadUrl(resource)" :href="resourceDownloadUrl(resource)"><button type="button"><Download :size="15" />下载</button></a>
                </div>
              </article>
            </div>
            <p v-else class="empty-state">该课程暂未发布教学视频或资料</p>
          </section>

          <div class="form-section-heading">
            <div><strong>记录信息</strong><span>该记录当前保存的完整业务字段</span></div>
          </div>
          <dl class="detail-grid">
            <div v-for="[name, label] in config.fields" :key="name">
              <dt>{{ label }}</dt>
              <dd><span v-if="['status', 'result', 'level'].includes(name)" class="status">{{ display(form[name]) }}</span><template v-else>{{ display(form[name]) }}</template></dd>
            </div>
          </dl>
        </div>

        <form v-else class="drawer-form" @submit.prevent="save">
          <div v-if="panelMode === 'duplicate'" class="duplicate-notice">
            <Copy :size="17" />
            <div><strong>正在创建一条新记录</strong><span>内容已从原记录复制，保存后会生成新的记录编号。</span></div>
          </div>
          <div class="form-section-heading">
            <div><strong>基础信息</strong><span>请填写并核对该记录的业务信息</span></div>
            <span>{{ config.fields.length }} 个字段</span>
          </div>
          <div class="form-grid">
            <label v-for="[name, label] in config.fields" :key="name" :class="{ 'field-wide': textareaFields.includes(name) }">
              <span class="field-label">{{ label }}</span>
              <textarea v-if="textareaFields.includes(name)" v-model="form[name]"></textarea>
              <select v-else-if="name === 'status'" v-model="form[name]"><option v-for="status in statuses" :key="status">{{ status }}</option></select>
              <select v-else-if="name === 'collector'" v-model="form[name]"><option>电脑终端录入</option><option>手机APP采集</option><option>传感器网关</option></select>
              <input v-else v-model="form[name]">
            </label>
            <MapPicker
              v-if="moduleKey === 'herbs'"
              v-model:latitude="form.latitude"
              v-model:longitude="form.longitude"
            />
          </div>
        </form>

        <footer class="drawer-footer">
          <template v-if="panelMode === 'view'">
            <button v-if="canDelete" class="danger-ghost" type="button" @click="requestDelete([editingId])"><Trash2 :size="16" />删除</button>
            <button v-if="canDuplicate" class="button-secondary" type="button" @click="duplicate(form)"><Copy :size="16" />复制</button>
            <button v-if="canEdit" type="button" @click="panelMode = 'edit'"><Pencil :size="16" />编辑记录</button>
            <button v-if="!canEdit && !canDuplicate && !canDelete" class="button-secondary" type="button" @click="closePanel">关闭</button>
          </template>
          <template v-else>
            <button class="button-secondary" type="button" @click="closePanel">取消</button>
            <button type="button" :disabled="saving" @click="save">{{ saving ? "保存中..." : panelMode === "duplicate" ? "创建副本" : editingId ? "保存修改" : "创建记录" }}</button>
          </template>
        </footer>
      </aside>
    </div>

    <div v-if="confirmState.open" class="confirm-layer" role="alertdialog" aria-modal="true">
      <section class="confirm-dialog">
        <div class="confirm-icon"><Trash2 :size="21" /></div>
        <h3>确认删除记录？</h3>
        <p>将删除 {{ confirmState.ids.length }} 条记录，此操作无法撤销。</p>
        <div>
          <button class="button-secondary" type="button" :disabled="deleting" @click="confirmState.open = false">取消</button>
          <button class="danger-button" type="button" :disabled="deleting" @click="executeDelete">{{ deleting ? "删除中..." : "确认删除" }}</button>
        </div>
      </section>
    </div>
  </Teleport>
</template>
