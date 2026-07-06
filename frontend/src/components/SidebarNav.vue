<script setup>
import { navItems } from "@/config";
import {
  Activity, Award, BookOpen, Boxes, ClipboardCheck, FileArchive,
  FlaskConical, GraduationCap, LayoutDashboard, MapPinned, UsersRound
} from "lucide-vue-next";

defineProps({
  active: { type: String, required: true },
  open: { type: Boolean, default: false }
});

const emit = defineEmits(["select", "close"]);

const icons = {
  dashboard: LayoutDashboard,
  herbs: MapPinned,
  "growth-records": Activity,
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
        v-for="[key, label] in navItems"
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
      <span class="user-avatar">管</span>
      <div><strong>系统管理员</strong><small><i></i>系统服务正常</small></div>
    </div>
  </aside>
</template>
