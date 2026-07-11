<script setup>
import { computed, onBeforeUnmount, onMounted, provide, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ChevronDown, ClipboardList, Grid2X2, Home, LogOut, Menu, RefreshCw, Save, ShieldCheck, Sprout, User, KeyRound } from "lucide-vue-next";
import SidebarNav from "@/components/SidebarNav.vue";
import AiAssistant from "@/components/AiAssistant.vue";
import { modules, roleMenus, roleModulePermissions, roles } from "@/config";
import { api } from "@/services/api";

const router = useRouter();
const route = useRoute();

const sessionUser = ref(JSON.parse(sessionStorage.getItem("biomed-session") || "null"));
const currentRole = computed(() => sessionUser.value?.role || "admin");
const currentRoleLabel = computed(() => sessionUser.value?.roleLabel || roles[currentRole.value]?.label || "管理员");
const currentUser = computed(() => ({
  role: currentRole.value,
  roleLabel: currentRoleLabel.value,
  name: sessionUser.value?.name || "当前用户"
}));
const canEditMap = computed(() => currentRole.value !== "student");
const isAuthenticated = computed(() => !!sessionUser.value);

const summary = ref({});
const herbs = ref([]);
const batches = ref([]);
const drawerOpen = ref(false);
const navCollapsed = ref(localStorage.getItem("biomed-nav-collapsed") === "1");
const toastText = ref("");
const refreshing = ref(false);
const backingUp = ref(false);
const refreshKey = ref(0);
const userMenuOpen = ref(false);
let toastTimer;
let touchStart = null;

const visibleNavItems = computed(() => roleMenus[currentRole.value] || roleMenus.admin);
const modulePermissions = computed(() => {
  const perms = roleModulePermissions[currentRole.value] || roleModulePermissions.admin;
  const key = route.name === "files" ? "files" : route.params.moduleKey;
  if (!key) return {};
  return perms[key] || perms.default || {};
});
const activeModuleConfig = computed(() => {
  const key = route.params.moduleKey;
  if (!key || !modules[key]) return null;
  return { ...modules[key], title: modules[key].title };
});
const requestedEditId = computed(() => String(route.query.editId || ""));
const pageTitle = computed(() => {
  if (route.name === "dashboard") return roles[currentRole.value]?.title || "工作台";
  if (route.name === "files") return "资料文件";
  if (route.name === "batch-detail") return "药材批次档案";
  if (route.name === "improvement") return "专业改进闭环";
  if (route.name === "profile") return "个人中心";
  if (route.name === "module") {
    const key = route.params.moduleKey;
    return modules[key]?.title || "业务模块";
  }
  return "生物医药数字信息系统";
});

function notify(message) {
  toastText.value = message;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toastText.value = "", 2800);
}

function appLogin(user) {
  sessionStorage.setItem("biomed-session", JSON.stringify(user));
  sessionUser.value = user;
  loadDashboard();
}

function appLogout() {
  sessionUser.value = null;
  sessionStorage.removeItem("biomed-session");
  drawerOpen.value = false;
  userMenuOpen.value = false;
  router.push("/login");
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value;
}

function closeUserMenu() {
  userMenuOpen.value = false;
}

function goProfile() {
  userMenuOpen.value = false;
  router.push("/profile");
}

function goChangePassword() {
  userMenuOpen.value = false;
  router.push({ path: "/profile", query: { action: "changePassword" } });
}

function openModuleRecord({ moduleKey, id }) {
  router.push({ path: `/module/${moduleKey}`, query: { editId: id } });
}

async function loadDashboard() {
  if (!isAuthenticated.value) return;
  try {
    const [summaryResult, herbsResult, batchResult] = await Promise.all([
      api("/api/summary"),
      api("/api/herbs"),
      api("/api/herb-batches")
    ]);
    summary.value = summaryResult;
    herbs.value = herbsResult.items || [];
    batches.value = batchResult.items || [];
  } catch (error) {
    notify(`数据加载失败：${error.message}`);
  }
}

async function refresh() {
  refreshing.value = true;
  try {
    if (route.name === "dashboard") await loadDashboard();
    refreshKey.value++;
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

function handleLoginRequired() {
  sessionUser.value = null;
  sessionStorage.removeItem("biomed-session");
  router.push("/login");
}

function mobileNavigate(key) {
  if (key === "dashboard") router.push("/dashboard");
  else if (key === "herb-batches") router.push("/module/herb-batches");
  else if (key === "improvement" && currentRole.value !== "student") router.push("/improvement");
  else drawerOpen.value = true;
}

function consumeResourceQuery(name) {
  if (!route.query[name]) return;
  const query = { ...route.query };
  delete query[name];
  router.replace({ path: route.path, query });
}

function consumeCreateQuery() {
  const query = { ...route.query };
  delete query.create;
  delete query.batchId;
  router.replace({ path: route.path, query });
}

async function handleResourceSaved(event) {
  if (event?.moduleKey === "herbs") await loadDashboard();
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

provide("currentRole", currentRole);
provide("currentUser", currentUser);
provide("canEditMap", canEditMap);
provide("summary", summary);
provide("herbs", herbs);
provide("batches", batches);
provide("notify", notify);
provide("refreshKey", refreshKey);
provide("appLogin", appLogin);
provide("appLogout", appLogout);
provide("loadDashboard", loadDashboard);
provide("submitGrowth", submitGrowth);
provide("modulePermissions", modulePermissions);
provide("activeModuleConfig", activeModuleConfig);

onMounted(() => {
  updateAppHeight();
  if (isAuthenticated.value) loadDashboard();
  window.addEventListener("resize", updateAppHeight);
  window.addEventListener("touchstart", onTouchStart, { passive: true });
  window.addEventListener("touchend", onTouchEnd, { passive: true });
  window.addEventListener("biomed-login-required", handleLoginRequired);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", updateAppHeight);
  window.removeEventListener("touchstart", onTouchStart);
  window.removeEventListener("touchend", onTouchEnd);
  window.removeEventListener("biomed-login-required", handleLoginRequired);
  clearTimeout(toastTimer);
});
</script>

<template>
  <router-view v-if="!isAuthenticated" />

  <template v-else>
    <button class="mobile-menu-button" type="button" aria-label="打开导航菜单" @click="drawerOpen = true">
      <Menu :size="21" />
    </button>
    <div class="shell" :class="{ 'drawer-open': drawerOpen, 'nav-collapsed': navCollapsed }">
      <SidebarNav
        :open="drawerOpen"
        :items="visibleNavItems"
        :role-label="currentRoleLabel"
        @collapsed="navCollapsed = $event"
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
            <button v-if="currentRole === 'admin'" class="button-secondary" type="button" :disabled="backingUp" @click="backup"><Save :size="16" />{{ backingUp ? "备份中..." : "自动备份" }}</button>
            <button type="button" :disabled="refreshing" @click="refresh"><RefreshCw :size="16" />{{ refreshing ? "刷新中..." : "刷新数据" }}</button>
            <div class="user-menu-wrapper">
              <button class="user-trigger" type="button" @click="toggleUserMenu">
                <span class="user-dot">{{ (currentUser.name || "?").charAt(0) }}</span>
                <span class="user-label">{{ currentUser.name }}</span>
                <ChevronDown :size="14" :class="{ 'chevron-up': userMenuOpen }" />
              </button>
              <Transition name="menu-fade">
                <div class="user-dropdown" v-if="userMenuOpen" @mouseleave="closeUserMenu">
                  <div class="dropdown-user">
                    <span class="drop-avatar">{{ (currentUser.name || "?").charAt(0) }}</span>
                    <span class="drop-name">{{ currentUser.name }}</span>
                  </div>
                  <div class="dropdown-divider"></div>
                  <button class="dropdown-item" @click="goProfile"><User :size="15" />个人中心</button>
                  <button class="dropdown-item" @click="goChangePassword"><KeyRound :size="15" />修改密码</button>
                  <div class="dropdown-divider"></div>
                  <button class="dropdown-item danger" @click="appLogout"><LogOut :size="15" />退出登录</button>
                </div>
              </Transition>
            </div>
          </div>
        </header>

        <router-view v-slot="{ Component, route: viewRoute }">
          <component
            :is="Component"
            :key="viewRoute.name === 'module' ? viewRoute.params.moduleKey : viewRoute.name"
            :module-key="viewRoute.params.moduleKey"
            :config="activeModuleConfig"
            :permissions="modulePermissions"
            :role="currentRole"
            :current-user="currentUser"
            :edit-id="requestedEditId"
            :open-create-on-load="route.query.create === '1'"
            :prefill-batch-id="String(route.query.batchId || '')"
            @edit-consumed="consumeResourceQuery('editId')"
            @create-consumed="consumeCreateQuery"
            @resource-saved="handleResourceSaved"
            @open-module-record="openModuleRecord"
            @notify="notify"
          />
        </router-view>
      </main>
    </div>

    <nav class="mobile-bottom-nav" aria-label="手机快捷导航">
      <button type="button" :class="{ active: route.name === 'dashboard' }" @click="mobileNavigate('dashboard')"><Home :size="20" /><span>工作台</span></button>
      <button type="button" :class="{ active: route.params.moduleKey === 'herb-batches' || route.name === 'batch-detail' }" @click="mobileNavigate('herb-batches')"><Sprout :size="20" /><span>药材</span></button>
      <button v-if="currentRole !== 'student'" type="button" :class="{ active: route.name === 'improvement' }" @click="mobileNavigate('improvement')"><ClipboardList :size="20" /><span>改进</span></button>
      <button type="button" :class="{ active: drawerOpen }" @click="mobileNavigate('all')"><Grid2X2 :size="20" /><span>全部</span></button>
    </nav>

    <AiAssistant />
  </template>
  <div id="toast" :class="{ show: toastText }">{{ toastText }}</div>
</template>
