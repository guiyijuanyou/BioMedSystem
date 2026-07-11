<script setup>
import { computed, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  Activity, Award, BarChart3, BookOpen, Boxes, ChevronDown, ChevronsLeft,
  ClipboardCheck, FileArchive, FlaskConical, FolderKanban, GitBranch,
  GraduationCap, LayoutDashboard, MapPinned, RefreshCcw, Settings, ShieldCheck,
  Sprout, Target, UsersRound
} from "lucide-vue-next";

const props = defineProps({
  open: { type: Boolean, default: false },
  items: { type: Array, required: true },
  roleLabel: { type: String, default: "管理员" }
});

const emit = defineEmits(["close", "collapsed"]);
const router = useRouter();
const route = useRoute();
const collapsed = ref(localStorage.getItem("biomed-nav-collapsed") === "1");

const icons = {
  dashboard: LayoutDashboard, herbs: MapPinned, "herb-batches": Boxes,
  "lab-samples": FlaskConical, "growth-records": Activity, "trace-events": GitBranch,
  "teaching-resources": ShieldCheck, "spectrum-comparisons": FlaskConical,
  "growth-analysis": BarChart3, courses: BookOpen, projects: FolderKanban,
  trainings: GraduationCap, evaluations: ClipboardCheck, achievements: Award,
  improvement: RefreshCcw, standards: Target, users: UsersRound, files: FileArchive
};

const groupDefinitions = [
  { key: "resources", label: "药材资源", icon: Sprout, members: ["herbs", "herb-batches", "lab-samples", "trace-events"] },
  { key: "research", label: "质量科研", icon: FlaskConical, members: ["growth-records", "spectrum-comparisons", "growth-analysis", "projects"] },
  { key: "teaching", label: "教学培训", icon: GraduationCap, members: ["courses", "teaching-resources", "trainings"] },
  { key: "evaluation", label: "评价改进", icon: ClipboardCheck, members: ["evaluations", "improvement", "standards"] },
  { key: "results", label: "成果管理", icon: Award, members: ["achievements", "files"] },
  { key: "system", label: "系统管理", icon: Settings, members: ["users"] }
];

const itemMap = computed(() => new Map(props.items));
const groups = computed(() => groupDefinitions.map(group => ({
  ...group,
  items: group.members.filter(key => itemMap.value.has(key)).map(key => [key, itemMap.value.get(key)])
})).filter(group => group.items.length));
const activeKey = computed(() => {
  if (route.name === "dashboard") return "dashboard";
  if (route.name === "files") return "files";
  if (route.name === "improvement") return "improvement";
  if (route.name === "batch-detail") return "herb-batches";
  return String(route.params.moduleKey || "");
});
const activeGroup = computed(() => groups.value.find(group => group.members.includes(activeKey.value))?.key || "");
const expandedGroup = ref(localStorage.getItem("biomed-nav-group") || activeGroup.value || groups.value[0]?.key || "");

watch(activeGroup, value => {
  if (!value) return;
  expandedGroup.value = value;
  localStorage.setItem("biomed-nav-group", value);
});

function toggleGroup(key) {
  expandedGroup.value = expandedGroup.value === key ? "" : key;
  localStorage.setItem("biomed-nav-group", expandedGroup.value);
}

function toggleCollapsed() {
  collapsed.value = !collapsed.value;
  localStorage.setItem("biomed-nav-collapsed", collapsed.value ? "1" : "0");
  emit("collapsed", collapsed.value);
}

function navigate(key) {
  if (key === "dashboard") router.push("/dashboard");
  else if (key === "files") router.push("/files");
  else if (key === "improvement") router.push("/improvement");
  else router.push(`/module/${key}`);
  emit("close");
}

emit("collapsed", collapsed.value);
</script>

<template>
  <aside class="sidebar" :class="{ open, collapsed }">
    <div class="brand">
      <span class="brand-mark">药</span>
      <div class="brand-copy"><strong>生物医药数字信息系统</strong><small>中药材数据采集与科研教学平台</small></div>
    </div>

    <nav class="grouped-nav" aria-label="主导航">
      <button class="nav-item nav-home" :class="{ active: activeKey === 'dashboard' }" type="button" title="工作台" @click="navigate('dashboard')">
        <LayoutDashboard :size="18" /><span>工作台</span>
      </button>

      <section v-for="group in groups" :key="group.key" class="nav-group" :class="{ active: activeGroup === group.key, expanded: expandedGroup === group.key }">
        <button class="nav-group-trigger" type="button" :title="group.label" :aria-expanded="expandedGroup === group.key" @click="toggleGroup(group.key)">
          <component :is="group.icon" :size="18" /><span>{{ group.label }}</span><ChevronDown class="nav-chevron" :size="15" />
        </button>
        <div class="nav-children">
          <button v-for="[key, label] in group.items" :key="key" class="nav-item nav-child" :class="{ active: activeKey === key }" type="button" :title="label" @click="navigate(key)">
            <component :is="icons[key]" :size="16" /><span>{{ label }}</span>
          </button>
        </div>
      </section>
    </nav>

    <button class="sidebar-collapse" type="button" :title="collapsed ? '展开导航' : '收起导航'" @click="toggleCollapsed"><ChevronsLeft :size="17" /><span>收起导航</span></button>
    <div class="sidebar-footer"><span class="user-avatar">{{ roleLabel.slice(0, 1) }}</span><div><strong>{{ roleLabel }}</strong><small><i></i>系统服务正常</small></div></div>
  </aside>
</template>
