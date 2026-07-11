<script setup>
import { ref } from "vue";
import { ArrowRight, BookOpen, GraduationCap, Microscope, Shield } from "lucide-vue-next";
defineEmits(["open-login"]);
const roles = [
  { id: "admin", label: "管理员", icon: Shield, description: "组织全局数据、用户权限、认定标准与安全备份。" },
  { id: "teacher", label: "教师", icon: BookOpen, description: "连接课程资源、项目指导、培训过程与成果评价。" },
  { id: "researcher", label: "科研人员", icon: Microscope, description: "管理采集数据、图谱比对、课题研究与成果转化。" },
  { id: "student", label: "学生", icon: GraduationCap, description: "参与课程学习、课题申请、资料检索与过程记录。" }
];
const selected = ref(roles[2]);
</script>

<template>
  <section id="roles" class="login-section roles-section" data-section="roles">
    <div class="roles-heading"><span class="section-index">05 / COLLABORATIVE ROLES</span><h2>一个数据空间，连接<br><em>四类协同角色</em></h2></div>
    <div class="role-layout">
      <div class="role-orbit">
        <button v-for="role in roles" :key="role.id" type="button" :class="{ 'is-active': selected.id === role.id }" @click="selected = role">
          <component :is="role.icon" :size="23" /><span>{{ role.label }}</span>
        </button>
        <div class="role-core"><span>ROLE</span><strong>04</strong><small>协同权限域</small></div>
      </div>
      <div class="role-summary"><span>{{ selected.label }}工作空间</span><h3>{{ selected.description }}</h3><p>身份验证后，系统将根据角色自动加载对应模块、数据范围和操作权限。</p>
        <button type="button" @click="$emit('open-login')">进入生物医药数据空间 <ArrowRight :size="19" /></button>
      </div>
    </div>
    <footer>BioMed Cloud · 重庆市中药材资源数字化管理</footer>
  </section>
</template>
