<script setup>
import { useRouter, useRoute } from "vue-router";
import {
  Activity, Award, BarChart3, BookOpen, Boxes, ClipboardCheck, FileArchive,
  FlaskConical, GitBranch, GraduationCap, LayoutDashboard, MapPinned, ShieldCheck, UsersRound
} from "lucide-vue-next";

defineProps({
  open: { type: Boolean, default: false },
  items: { type: Array, required: true },
  roleLabel: { type: String, default: "管理员" }
});

const emit = defineEmits(["close"]);
const router = useRouter();
const route = useRoute();

const icons = {
  dashboard: LayoutDashboard,
  herbs: MapPinned,
  "growth-records": Activity,
  "trace-events": GitBranch,
  "teaching-resources": ShieldCheck,
  "spectrum-comparisons": FlaskConical,
  "growth-analysis": BarChart3,
  courses: BookOpen,
  projects: FlaskConical,
  trainings: GraduationCap,
  evaluations: ClipboardCheck,
  achievements: Award,
  standards: Boxes,
  users: UsersRound,
  files: FileArchive
};

function isNavActive(key) {
  if (key === "dashboard") return route.name === "dashboard";
  if (key === "files") return route.name === "files";
  return route.name === "module" && route.params.moduleKey === key;
}

function navigate(key) {
  if (key === "dashboard") router.push("/dashboard");
  else if (key === "files") router.push("/files");
  else router.push(`/module/${key}`);
  emit("close");
}
</script>

<template>
  <aside class="sidebar" :class="{ open }">
    <div class="brand">
      <span class="brand-mark">药</span>
      <div>
        <strong>生物医药数字信息系统</strong>
        <small>中药材数据采集与科研教学平台</small>
      </div>
    </div>
    <nav>
      <button
        v-for="[key, label] in items"
        :key="key"
        class="nav-item"
        :class="{ active: isNavActive(key) }"
        type="button"
        @click="navigate(key)"
      >
        <component :is="icons[key]" :size="18" />
        {{ label }}
      </button>
    </nav>
    <div class="sidebar-footer">
      <span class="user-avatar">{{ roleLabel.slice(0, 1) }}</span>
      <div><strong>{{ roleLabel }}</strong><small><i></i>系统服务正常</small></div>
    </div>
  </aside>
</template>
