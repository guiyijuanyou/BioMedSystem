<script setup>
import {
  Activity, Award, BarChart3, BookOpen, Boxes, ClipboardCheck, FileArchive,
  FlaskConical, GitBranch, GraduationCap, LayoutDashboard, MapPinned, ShieldCheck, UsersRound
} from "lucide-vue-next";

defineProps({
  active: { type: String, required: true },
  open: { type: Boolean, default: false },
  items: { type: Array, required: true },
  roleLabel: { type: String, default: "管理员" }
});

const emit = defineEmits(["select", "close"]);

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
        :class="{ active: active === key }"
        type="button"
        @click="emit('select', key); emit('close')"
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
