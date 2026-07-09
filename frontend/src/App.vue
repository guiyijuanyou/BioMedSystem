<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { LogOut, Menu, RefreshCw, Save, ShieldCheck } from "lucide-vue-next";
import SidebarNav from "@/components/SidebarNav.vue";
import DashboardView from "@/views/DashboardView.vue";
import ResourceView from "@/views/ResourceView.vue";
import FilesView from "@/views/FilesView.vue";
import AiAssistant from "@/components/AiAssistant.vue";
import { modules, roleMenus, roleModulePermissions, roles } from "@/config";
import { api } from "@/services/api";

const savedSession = localStorage.getItem("biomed-session");
const sessionUser = ref(savedSession ? JSON.parse(savedSession) : null);
const loginForm = ref({ username: "admin", password: "123456" });
const loginLoading = ref(false);
const currentRole = ref(sessionUser.value?.role || "admin");
const active = ref("dashboard");
const drawerOpen = ref(false);
const summary = ref({});
const herbs = ref([]);
const editId = ref("");
const toastText = ref("");
const refreshing = ref(false);
const backingUp = ref(false);
let toastTimer;
let touchStart = null;

const demoProfiles = {
  admin: { name: "系统管理员", roleLabel: "管理员" },
  teacher: { name: "李老师", roleLabel: "教师" },
  researcher: { name: "王老师", roleLabel: "科研人员" },
  student: { name: "当前学生", roleLabel: "学生" }
};

const isAuthenticated = computed(() => !!sessionUser.value);
const visibleNavItems = computed(() => roleMenus[currentRole.value] || roleMenus.admin);
const currentNavLabel = computed(() => visibleNavItems.value.find(([key]) => key === active.value)?.[1] || "");
const currentRoleLabel = computed(() => sessionUser.value?.roleLabel || roles[currentRole.value]?.label || "管理员");
const currentUser = computed(() => ({
  role: currentRole.value,
  roleLabel: currentRoleLabel.value,
  name: sessionUser.value?.name || "当前用户"
}));
const pageTitle = computed(() => {
  if (active.value === "dashboard") return roles[currentRole.value]?.title || "工作台";
  if (active.value === "files") return currentNavLabel.value || "资料上传下载";
  return currentNavLabel.value || modules[active.value]?.title || "生物医药数字信息系统";
});
const activeModuleConfig = computed(() => {
  const config = modules[active.value];
  if (!config) return null;
  return { ...config, title: currentNavLabel.value || config.title };
});
const modulePermissions = computed(() => {
  const rolePermissions = roleModulePermissions[currentRole.value] || roleModulePermissions.admin;
  return rolePermissions[active.value] || rolePermissions.default || {};
});

function notify(message) {
  toastText.value = message;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastText.value = "", 2800);
}

async function login() {
  loginLoading.value = true;
  try {
    const user = await api("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(loginForm.value)
    });
    sessionUser.value = user;
    currentRole.value = user.role;
    localStorage.setItem("biomed-session", JSON.stringify(user));
    active.value = "dashboard";
    await loadDashboard();
    notify("登录成功");
  } catch (error) {
    notify(error.message);
  } finally {
    loginLoading.value = false;
  }
}

function logout() {
  sessionUser.value = null;
  localStorage.removeItem("biomed-session");
  active.value = "dashboard";
  drawerOpen.value = false;
}

async function loadDashboard() {
  if (!isAuthenticated.value) return;
  try {
    const [summaryResult, herbsResult] = await Promise.all([
      api("/api/summary"),
      api("/api/herbs")
    ]);
    summary.value = summaryResult;
    herbs.value = herbsResult.items || [];
  } catch (error) {
    notify(`数据加载失败：${error.message}`);
  }
}

async function refresh() {
  refreshing.value = true;
  try {
    if (active.value === "dashboard") await loadDashboard();
    else {
      const key = active.value;
      active.value = "";
      requestAnimationFrame(() => active.value = key);
    }
    notify("数据已刷新");
  } finally {
    refreshing.value = false;
  }
}

async function backup() {
  backingUp.value = true;
  try {
    const result = await api("/api/backup", { method: "POST" });
    notify(`${result.message}：${result.file}`);
  } catch (error) {
    notify(error.message);
  } finally {
    backingUp.value = false;
  }
}

function selectPage(key) {
  active.value = key;
  drawerOpen.value = false;
  if (key === "dashboard") loadDashboard();
}

function selectRole() {
  notify("请退出后使用对应账号重新登录");
}

function editMapRecord(item) {
  if (currentRole.value === "student") return;
  editId.value = item.id;
  selectPage("herbs");
}

async function submitGrowth(payload) {
  try {
    await api("/api/growth-records", {
      method: "POST",
      body: JSON.stringify({
        ...payload,
        recorder: currentUser.value.name,
        recorderRole: currentUser.value.roleLabel
      })
    });
    notify("生长采集数据已入库");
    await loadDashboard();
  } catch (error) {
    notify(error.message);
  }
}

function updateAppHeight() {
  document.documentElement.style.setProperty("--app-height", `${window.innerHeight}px`);
}

function onTouchStart(event) {
  const touch = event.touches[0];
  if (!drawerOpen.value && touch.clientX > 24) return;
  if (drawerOpen.value && event.target.closest(".sidebar")) return;
  touchStart = { x: touch.clientX, y: touch.clientY, open: drawerOpen.value };
}

function onTouchEnd(event) {
  if (!touchStart || !event.changedTouches[0]) return;
  const touch = event.changedTouches[0];
  const dx = touch.clientX - touchStart.x;
  const dy = touch.clientY - touchStart.y;
  if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 45) {
    if (!touchStart.open && dx > 0) drawerOpen.value = true;
    if (touchStart.open && dx < 0) drawerOpen.value = false;
  }
  touchStart = null;
}

onMounted(() => {
  updateAppHeight();
  loadDashboard();
  window.addEventListener("resize", updateAppHeight);
  window.addEventListener("touchstart", onTouchStart, { passive: true });
  window.addEventListener("touchend", onTouchEnd, { passive: true });
});

watch(currentRole, () => {
  if (!isAuthenticated.value) return;
  const menu = visibleNavItems.value;
  if (!menu.some(([key]) => key === active.value)) {
    active.value = menu[0]?.[0] || "dashboard";
  }
  if (active.value === "dashboard") loadDashboard();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", updateAppHeight);
  window.removeEventListener("touchstart", onTouchStart);
  window.removeEventListener("touchend", onTouchEnd);
  clearTimeout(toastTimer);
});
</script>

<template>
  <section v-if="!isAuthenticated" class="login-page">
    <form class="login-panel" @submit.prevent="login">
      <div class="login-brand">
        <span>药</span>
        <div>
          <strong>生物医药数字信息系统</strong>
          <small>重庆市中药材资源管理</small>
        </div>
      </div>
      <label>
        <span>账号</span>
        <input v-model="loginForm.username" autocomplete="username" placeholder="admin / teacher / researcher / student">
      </label>
      <label>
        <span>密码</span>
        <input v-model="loginForm.password" type="password" autocomplete="current-password" placeholder="默认 123456">
      </label>
      <button type="submit" :disabled="loginLoading">{{ loginLoading ? "登录中..." : "登录系统" }}</button>
      <p>演示账号：admin、teacher、researcher、student，密码均为 123456。</p>
    </form>
  </section>

  <template v-else>
    <button class="mobile-menu-button" type="button" aria-label="打开导航菜单" @click="drawerOpen = true">
      <Menu :size="21" />
    </button>
    <div class="shell" :class="{ 'drawer-open': drawerOpen }">
      <SidebarNav
        :active="active"
        :open="drawerOpen"
        :items="visibleNavItems"
        :role-label="currentRoleLabel"
        @select="selectPage"
        @close="drawerOpen = false"
      />
      <div class="drawer-mask" @click="drawerOpen = false"></div>

      <main class="workspace">
        <header class="topbar">
          <div class="page-heading">
            <p class="eyebrow">重庆市中药材资源管理</p>
            <h1>{{ pageTitle }}</h1>
          </div>
          <div class="actions">
            <label class="role-switch">
              <span>当前角色</span>
              <strong>{{ currentRoleLabel }}</strong>
            </label>
            <span class="system-health"><ShieldCheck :size="15" />{{ currentUser.name }}</span>
            <button v-if="currentRole === 'admin'" class="button-secondary" type="button" :disabled="backingUp" @click="backup"><Save :size="16" />{{ backingUp ? "备份中..." : "自动备份" }}</button>
            <button type="button" :disabled="refreshing" @click="refresh"><RefreshCw :size="16" />{{ refreshing ? "刷新中..." : "刷新数据" }}</button>
            <button class="button-secondary" type="button" @click="logout"><LogOut :size="16" />退出</button>
          </div>
        </header>

        <DashboardView
          v-if="active === 'dashboard'"
          :summary="summary"
          :herbs="herbs"
          :role="currentRole"
          :current-user="currentUser"
          :can-edit-map="currentRole !== 'student'"
          @edit-record="editMapRecord"
          @submit-growth="submitGrowth"
          @notify="notify"
        />
        <ResourceView
          v-else-if="modules[active]"
          :key="active"
          :module-key="active"
          :config="activeModuleConfig"
          :permissions="modulePermissions"
          :role="currentRole"
          :current-user="currentUser"
          :edit-id="editId"
          @edit-consumed="editId = ''"
          @notify="notify"
        />
        <FilesView v-else-if="active === 'files'" :permissions="modulePermissions" :role="currentRole" @notify="notify" />
      </main>
    </div>

    <AiAssistant />
  </template>
  <div id="toast" :class="{ show: toastText }">{{ toastText }}</div>
</template>
