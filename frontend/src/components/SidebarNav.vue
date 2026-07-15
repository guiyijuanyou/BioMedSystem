<script setup>
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import gsap from "gsap";
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
  improvement: RefreshCcw, "quality-metrics": Target, "multi-evaluations": ClipboardCheck, "mobile-devices": Activity, standards: Target, users: UsersRound, files: FileArchive,
  "herb-encyclopedia": BookOpen
};

const groupDefinitions = [
  { key: "resources", label: "药材资源", icon: Sprout, members: ["herbs", "herb-batches", "lab-samples", "trace-events", "herb-encyclopedia"] },
  { key: "research", label: "质量科研", icon: FlaskConical, members: ["growth-records", "spectrum-comparisons", "growth-analysis", "quality-metrics", "projects"] },
  { key: "teaching", label: "教学培训", icon: GraduationCap, members: ["courses", "teaching-resources", "trainings"] },
  { key: "evaluation", label: "评价改进", icon: ClipboardCheck, members: ["evaluations", "multi-evaluations", "improvement", "standards"] },
  { key: "results", label: "成果管理", icon: Award, members: ["achievements", "files"] },
  { key: "system", label: "系统管理", icon: Settings, members: ["users", "mobile-devices"] }
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
  if (route.name === "quality-metrics") return "quality-metrics";
  if (route.name === "multi-evaluations") return "multi-evaluations";
  if (route.name === "mobile-devices") return "mobile-devices";
  if (route.name === "batch-detail") return "herb-batches";
  return String(route.params.moduleKey || "");
});
const activeGroup = computed(() => groups.value.find(group => group.members.includes(activeKey.value))?.key || "");
const expandedGroup = ref(localStorage.getItem("biomed-nav-group") || activeGroup.value || groups.value[0]?.key || "");
const desktopGroup = ref("");
let desktopCloseTimer;

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

function openDesktopGroup(key) {
  clearTimeout(desktopCloseTimer);
  desktopGroup.value = key;
}

function scheduleDesktopGroupClose() {
  clearTimeout(desktopCloseTimer);
  desktopCloseTimer = setTimeout(() => {
    desktopGroup.value = "";
  }, 160);
}

function closeDesktopGroup() {
  clearTimeout(desktopCloseTimer);
  desktopGroup.value = "";
}

function onDesktopMenuEnter(element, done) {
  gsap.fromTo(element, { autoAlpha: 0, y: -8 }, {
    autoAlpha: 1,
    y: 0,
    duration: 0.22,
    ease: "power2.out",
    onComplete: done
  });
}

function onDesktopMenuLeave(element, done) {
  gsap.to(element, {
    autoAlpha: 0,
    y: -6,
    duration: 0.16,
    ease: "power2.in",
    onComplete: done
  });
}

function navigate(key) {
  if (key === "dashboard") router.push("/dashboard");
  else if (key === "files") router.push("/files");
  else if (key === "improvement") router.push("/improvement");
  else if (key === "quality-metrics") router.push("/quality-metrics");
  else if (key === "multi-evaluations") router.push("/multi-evaluations");
  else if (key === "mobile-devices") router.push("/mobile-devices");
  else if (key === "herb-encyclopedia") router.push("/herb-encyclopedia");
  else router.push(`/module/${key}`);
  closeDesktopGroup();
  emit("close");
}

emit("collapsed", collapsed.value);
onBeforeUnmount(() => clearTimeout(desktopCloseTimer));
</script>

<template>
  <header class="desktop-nav-shell">
    <div class="desktop-primary-nav">
      <button class="desktop-brand" type="button" data-nav-key="dashboard" @click="navigate('dashboard')">
        <span class="desktop-brand-mark" aria-hidden="true"><i></i><i></i><i></i></span>
        <span><strong>BioMed Cloud</strong><small>生物医药数字信息系统</small></span>
      </button>

      <nav class="desktop-nav-links" aria-label="桌面主导航">
        <button class="desktop-nav-home" :class="{ active: activeKey === 'dashboard' }" type="button" data-nav-key="dashboard" @click="navigate('dashboard')">工作台</button>
        <section
          v-for="group in groups"
          :key="group.key"
          class="desktop-nav-group"
          :class="{ active: activeGroup === group.key, open: desktopGroup === group.key }"
          @mouseenter="openDesktopGroup(group.key)"
          @mouseleave="scheduleDesktopGroupClose"
          @focusin="openDesktopGroup(group.key)"
          @focusout="scheduleDesktopGroupClose"
          @keydown.esc="closeDesktopGroup"
        >
          <button class="desktop-nav-trigger" type="button" :aria-expanded="desktopGroup === group.key">
            <span>{{ group.label }}</span><ChevronDown :size="14" />
          </button>
          <Transition :css="false" @enter="onDesktopMenuEnter" @leave="onDesktopMenuLeave">
            <div v-if="desktopGroup === group.key" class="desktop-nav-dropdown">
              <div class="desktop-nav-dropdown-head"><component :is="group.icon" :size="18" /><strong>{{ group.label }}</strong></div>
              <button
                v-for="[key, label] in group.items"
                :key="key"
                class="desktop-nav-option"
                :class="{ active: activeKey === key }"
                type="button"
                :data-nav-key="key"
                @click="navigate(key)"
              >
                <span class="desktop-nav-option-icon"><component :is="icons[key]" :size="17" /></span>
                <span><strong>{{ label }}</strong><small>进入{{ label }}模块</small></span>
              </button>
            </div>
          </Transition>
        </section>
      </nav>

      <div class="desktop-nav-status"><span>{{ roleLabel }}</span><small><i></i>系统服务正常</small></div>
    </div>
  </header>

  <aside class="sidebar" :class="{ open, collapsed }">
    <div class="brand">
      <span class="brand-mark">药</span>
      <div class="brand-copy"><strong>生物医药数字信息系统</strong><small>中药材数据采集与科研教学平台</small></div>
    </div>

    <nav class="grouped-nav" aria-label="主导航">
      <button class="nav-item nav-home" :class="{ active: activeKey === 'dashboard' }" type="button" title="工作台" data-nav-key="dashboard" @click="navigate('dashboard')">
        <LayoutDashboard :size="18" /><span>工作台</span>
      </button>

      <section v-for="group in groups" :key="group.key" class="nav-group" :class="{ active: activeGroup === group.key, expanded: expandedGroup === group.key }">
        <button class="nav-group-trigger" type="button" :title="group.label" :aria-expanded="expandedGroup === group.key" @click="toggleGroup(group.key)">
          <component :is="group.icon" :size="18" /><span>{{ group.label }}</span><ChevronDown class="nav-chevron" :size="15" />
        </button>
        <div class="nav-children">
          <button v-for="[key, label] in group.items" :key="key" class="nav-item nav-child" :class="{ active: activeKey === key }" type="button" :title="label" :data-nav-key="key" @click="navigate(key)">
            <component :is="icons[key]" :size="16" /><span>{{ label }}</span>
          </button>
        </div>
      </section>
    </nav>

    <button class="sidebar-collapse" type="button" :title="collapsed ? '展开导航' : '收起导航'" @click="toggleCollapsed"><ChevronsLeft :size="17" /><span>收起导航</span></button>
    <div class="sidebar-footer"><span class="user-avatar">{{ roleLabel.slice(0, 1) }}</span><div><strong>{{ roleLabel }}</strong><small><i></i>系统服务正常</small></div></div>
  </aside>
</template>
