<script setup>
import { ref, onMounted, inject } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, User, Building, Briefcase, Mail, Phone, Clock } from "lucide-vue-next";
import { api } from "@/services/api";

const route = useRoute();
const router = useRouter();
const notify = inject("notify");

const profile = ref(null);
const loading = ref(true);

function getRoleLabel(role) {
  const labels = { admin: "管理员", teacher: "教师", researcher: "科研人员", student: "学生" };
  return labels[role] || role || "未知";
}
function getInitial(name) { return (name || "?").charAt(0); }

async function loadProfile() {
  try {
    const userId = route.params.userId;
    profile.value = await api(`/api/users/${userId}/profile`);
  } catch (e) {
    notify("无法加载用户资料：" + (e.message || "用户不存在"));
  } finally {
    loading.value = false;
  }
}

onMounted(loadProfile);
</script>

<template>
  <div v-if="loading" class="empty-state" style="padding:60px 0;">加载中...</div>

  <div v-else-if="!profile" class="empty-state" style="padding:60px 0;">用户不存在或无权查看</div>

  <template v-else>
    <button class="button-secondary" style="margin-bottom:14px;" @click="router.back()">
      <ArrowLeft :size="16" />返回
    </button>

    <div class="module-layout" style="grid-template-columns: minmax(0, 2fr) minmax(260px, 0.9fr); gap: 16px;">
      <section class="panel">
        <div class="panel-head">
          <div><h2 style="display:flex;align-items:center;gap:8px;"><User :size="18" />公开资料</h2><span>该用户的公开信息</span></div>
        </div>
        <dl class="profile-grid">
          <div><dt>姓名</dt><dd>{{ profile.name || "-" }}</dd></div>
          <div><dt><Building :size="13" /> 部门</dt><dd>{{ profile.department || "未设置" }}</dd></div>
          <div><dt><Briefcase :size="13" /> 职称</dt><dd>{{ profile.title || "未设置" }}</dd></div>
          <div class="profile-grid-wide"><dt>研究方向</dt><dd>{{ profile.researchArea || "未设置" }}</dd></div>
          <div class="profile-grid-wide"><dt>个人简介</dt><dd>{{ profile.bio || "未设置" }}</dd></div>
        </dl>
      </section>

      <aside>
        <section class="panel" style="text-align:center;margin-bottom:14px;">
          <div style="display:grid;place-items:center;gap:12px;padding:8px 0;">
            <div style="width:64px;height:64px;border-radius:50%;background:#e8f4ee;color:#166534;font-size:24px;font-weight:800;display:grid;place-items:center;border:1px solid #d2e8dc;">{{ getInitial(profile.name) }}</div>
            <div>
              <strong style="display:block;font-size:17px;color:var(--ink);">{{ profile.name || "加载中..." }}</strong>
              <span class="status" style="margin-top:4px;">{{ getRoleLabel(profile.role) }}</span>
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div><h2 style="display:flex;align-items:center;gap:8px;"><Clock :size="18" />基本信息</h2></div>
          </div>
          <div style="display:flex;align-items:center;justify-content:space-between;padding:6px 0;border-bottom:1px solid #edf1ee;">
            <span style="color:var(--muted);font-size:12px;">角色</span>
            <span class="status">{{ getRoleLabel(profile.role) }}</span>
          </div>
          <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 0;">
            <span style="color:var(--muted);font-size:12px;">部门</span>
            <span style="font-size:13px;">{{ profile.department || "未设置" }}</span>
          </div>
        </section>
      </aside>
    </div>
  </template>
</template>
