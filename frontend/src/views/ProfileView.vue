<script setup>
import { ref, onMounted, inject } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  User, Phone, Mail, Building, Briefcase, Shield,
  Clock, Save, X, Lock, FileText
} from "lucide-vue-next";
import { api } from "@/services/api";

const route = useRoute();
const router = useRouter();
const notify = inject("notify");

const profile = ref(null);
const drawerOpen = ref(false);
const drawerSaving = ref(false);
const editForm = ref({});

// 密码表单
const pwdOpen = ref(false);
const pwdSaving = ref(false);
const pwdError = ref("");
const pwdForm = ref({ oldPassword: "", newPassword: "", confirmPassword: "" });

async function loadProfile() {
  try {
    profile.value = await api("/api/users/profile");
  } catch (e) {
    notify(e.message || "加载个人资料失败");
  }
}

function openEdit() {
  editForm.value = {
    phone: profile.value?.phone || "",
    email: profile.value?.email || "",
    avatarUrl: profile.value?.avatarUrl || "",
    title: profile.value?.title || "",
    researchArea: profile.value?.researchArea || "",
    bio: profile.value?.bio || ""
  };
  drawerOpen.value = true;
}

function closeDrawer() {
  if (drawerSaving.value) return;
  drawerOpen.value = false;
}

async function saveProfile() {
  drawerSaving.value = true;
  try {
    const result = await api("/api/users/profile", {
      method: "PUT",
      body: JSON.stringify(editForm.value)
    });
    profile.value = result;
    drawerOpen.value = false;
    notify("个人资料已保存");
  } catch (e) {
    notify(e.message || "保存失败");
  } finally {
    drawerSaving.value = false;
  }
}

function openPwd() {
  pwdForm.value = { oldPassword: "", newPassword: "", confirmPassword: "" };
  pwdError.value = "";
  pwdOpen.value = true;
}

function clearPwdError() {
  pwdError.value = "";
}

async function changePassword() {
  if (!pwdForm.value.oldPassword) {
    pwdError.value = "请输入当前密码";
    return;
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    pwdError.value = "两次输入的新密码不一致";
    return;
  }
  if (pwdForm.value.newPassword.length < 6) {
    pwdError.value = "新密码长度不能少于6位";
    return;
  }
  pwdSaving.value = true;
  pwdError.value = "";
  try {
    await api("/api/auth/change-password", {
      method: "POST",
      body: JSON.stringify({
        oldPassword: pwdForm.value.oldPassword,
        newPassword: pwdForm.value.newPassword
      })
    });
    notify("密码修改成功");
    pwdOpen.value = false;
  } catch (e) {
    pwdError.value = e.message || "密码修改失败";
  } finally {
    pwdSaving.value = false;
  }
}

function getRoleLabel(role) {
  const labels = { admin: "管理员", teacher: "教师", researcher: "科研人员", student: "学生" };
  return labels[role] || role || "未知";
}
function getInitial(name) { return (name || "?").charAt(0); }

onMounted(async () => {
  await loadProfile();
  if (route.query.action === "changePassword") {
    openPwd();
    router.replace({ path: "/profile", query: {} });
  }
});
</script>

<template>
  <!-- ============ 页面主体 ============ -->
  <div class="module-layout" style="grid-template-columns: minmax(0, 2fr) minmax(280px, 0.9fr); gap: 16px;">
    <!-- 左：基本信息 -->
    <section class="panel">
      <div class="panel-head">
        <div>
          <h2 style="display:flex;align-items:center;gap:8px;"><User :size="18" />基本信息</h2>
          <span>账户信息和联系方式</span>
        </div>
        <button @click="openEdit">编辑资料</button>
      </div>
      <dl class="profile-grid">
        <div><dt>用户名</dt><dd>{{ profile?.username || "-" }}</dd></div>
        <div><dt>显示名称</dt><dd>{{ profile?.name || "-" }}</dd></div>
        <div><dt><Building :size="13" /> 部门</dt><dd>{{ profile?.department || "未设置" }}</dd></div>
        <div><dt><Briefcase :size="13" /> 职称</dt><dd>{{ profile?.title || "未设置" }}</dd></div>
        <div><dt><Phone :size="13" /> 手机号</dt><dd>{{ profile?.phone || "未设置" }}</dd></div>
        <div><dt><Mail :size="13" /> 邮箱</dt><dd>{{ profile?.email || "未设置" }}</dd></div>
        <div class="profile-grid-wide"><dt>研究方向</dt><dd>{{ profile?.researchArea || "未设置" }}</dd></div>
        <div class="profile-grid-wide"><dt>个人简介</dt><dd>{{ profile?.bio || "未设置" }}</dd></div>
        <div><dt><Clock :size="13" /> 注册时间</dt><dd>{{ profile?.createdAt?.substring(0, 10) || "-" }}</dd></div>
      </dl>
    </section>

    <!-- 右：头像 + 安全 -->
    <aside>
      <section class="panel" style="text-align:center;margin-bottom:14px;">
        <div style="display:grid;place-items:center;gap:12px;padding:8px 0;">
          <div style="width:64px;height:64px;border-radius:50%;background:#e8f4ee;color:#166534;font-size:24px;font-weight:800;display:grid;place-items:center;border:1px solid #d2e8dc;">{{ getInitial(profile?.name) }}</div>
          <div>
            <strong style="display:block;font-size:17px;color:var(--ink);">{{ profile?.name || "加载中..." }}</strong>
            <span class="status" style="margin-top:4px;">{{ getRoleLabel(profile?.role) }}</span>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <div><h2 style="display:flex;align-items:center;gap:8px;"><Shield :size="18" />账户安全</h2></div>
        </div>
        <div style="display:flex;align-items:center;justify-content:space-between;padding:6px 0;border-bottom:1px solid #edf1ee;">
          <span style="color:var(--muted);font-size:12px;">账号状态</span>
          <span class="status">{{ profile?.status === "enabled" ? "正常" : "禁用" }}</span>
        </div>
        <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 0 0;">
          <span style="color:var(--muted);font-size:12px;">登录密码</span>
          <button class="button-secondary compact-button" @click="openPwd"><Lock :size="14" />修改密码</button>
        </div>
      </section>
    </aside>
  </div>

  <!-- ============ 编辑资料抽屉 ============ -->
  <Teleport to="body">
    <div v-if="drawerOpen" class="record-drawer-layer" @click.self="closeDrawer">
      <aside class="record-drawer" role="dialog" aria-modal="true" aria-label="编辑个人资料">
        <header class="drawer-header">
          <div class="drawer-title-group">
            <span class="drawer-title-icon"><FileText :size="19" /></span>
            <div>
              <small>个人中心</small>
              <h2>编辑个人资料</h2>
            </div>
          </div>
          <button class="icon-button" type="button" title="关闭" @click="closeDrawer"><X :size="20" /></button>
        </header>
        <form class="drawer-form" @submit.prevent="saveProfile">
          <div class="form-grid">
            <label>手机号<input v-model="editForm.phone" placeholder="请输入手机号" /></label>
            <label>邮箱<input v-model="editForm.email" type="email" placeholder="请输入邮箱地址" /></label>
            <label>头像链接<input v-model="editForm.avatarUrl" placeholder="头像图片URL（可选）" /></label>
            <label>职称<input v-model="editForm.title" placeholder="如：教授、副教授、研究员" /></label>
            <label class="field-wide">研究方向<input v-model="editForm.researchArea" placeholder="如：中药材栽培与鉴定" /></label>
            <label class="field-wide">个人简介<textarea v-model="editForm.bio" rows="3" placeholder="简要介绍自己的学术背景"></textarea></label>
          </div>
        </form>
        <footer class="drawer-footer">
          <button class="button-secondary" type="button" @click="closeDrawer">取消</button>
          <button :disabled="drawerSaving" @click="saveProfile"><Save :size="16" />{{ drawerSaving ? "保存中..." : "保存" }}</button>
        </footer>
      </aside>
    </div>

    <!-- ============ 修改密码抽屉 ============ -->
    <div v-if="pwdOpen" class="record-drawer-layer" @click.self="pwdOpen = false">
      <aside class="record-drawer" role="dialog" aria-modal="true" aria-label="修改密码">
        <header class="drawer-header">
          <div class="drawer-title-group">
            <span class="drawer-title-icon"><Lock :size="19" /></span>
            <div>
              <small>账户安全</small>
              <h2>修改登录密码</h2>
            </div>
          </div>
          <button class="icon-button" type="button" title="关闭" @click="pwdOpen = false"><X :size="20" /></button>
        </header>
        <form class="drawer-form" @submit.prevent="changePassword">
          <div class="form-grid" style="grid-template-columns:1fr;">
            <label>当前密码
              <input v-model="pwdForm.oldPassword" type="password" placeholder="输入当前密码以验证身份" @input="clearPwdError" />
            </label>
            <label>新密码
              <input v-model="pwdForm.newPassword" type="password" placeholder="至少6位" @input="clearPwdError" />
            </label>
            <label>确认新密码
              <input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" @input="clearPwdError" />
            </label>
          </div>
          <p v-if="pwdError" class="form-error">{{ pwdError }}</p>
        </form>
        <footer class="drawer-footer">
          <button class="button-secondary" type="button" @click="pwdOpen = false">取消</button>
          <button :disabled="pwdSaving" @click="changePassword"><Save :size="16" />{{ pwdSaving ? "修改中..." : "确认修改" }}</button>
        </footer>
      </aside>
    </div>
  </Teleport>
</template>

