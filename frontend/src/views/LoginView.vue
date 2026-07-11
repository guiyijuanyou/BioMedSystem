<script setup>
import { inject, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "@/services/api";
import LoginNavigation from "@/components/login/LoginNavigation.vue";
import ScrollProgress from "@/components/login/ScrollProgress.vue";
import LoginModal from "@/components/login/LoginModal.vue";
import BiomedScene from "@/components/login/BiomedScene.vue";
import HeroSection from "@/components/login/sections/HeroSection.vue";
import CollectionSection from "@/components/login/sections/CollectionSection.vue";
import DistributionSection from "@/components/login/sections/DistributionSection.vue";
import TraceSection from "@/components/login/sections/TraceSection.vue";
import RolesSection from "@/components/login/sections/RolesSection.vue";
import { useImmersiveScroll } from "@/composables/useImmersiveScroll";

const router = useRouter();
const route = useRoute();
const appLogin = inject("appLogin");
const sections = [
  { id: "cloud", label: "数据云" },
  { id: "collection", label: "实时采集" },
  { id: "distribution", label: "地理分布" },
  { id: "trace", label: "图谱溯源" },
  { id: "roles", label: "协同角色" }
];

const loginForm = ref({ username: "admin", password: "123456" });
const loginLoading = ref(false);
const errorText = ref("");
const loginOpen = ref(false);
const { progress, activeSection, scrollToSection, pause, resume } = useImmersiveScroll(sections.map(section => section.id));

watch(loginOpen, open => open ? pause() : resume());

function openLogin() {
  errorText.value = "";
  loginOpen.value = true;
}

async function login(credentials) {
  loginForm.value = { ...credentials };
  loginLoading.value = true;
  errorText.value = "";
  try {
    const user = await api("/api/auth/login", { method: "POST", body: JSON.stringify(loginForm.value) });
    appLogin(user);
    loginOpen.value = false;
    await router.push(route.query.redirect || "/dashboard");
  } catch (error) {
    errorText.value = error.message;
  } finally {
    loginLoading.value = false;
  }
}
</script>

<template>
  <main class="login-experience">
    <div class="login-experience__noise" aria-hidden="true"></div>
    <LoginNavigation :sections="sections" :active-section="activeSection" @navigate="scrollToSection" @open-login="openLogin" />
    <ScrollProgress :sections="sections" :active-section="activeSection" :progress="progress" @navigate="scrollToSection" />
    <div class="login-scene-layer"><BiomedScene :progress="progress" /></div>
    <div class="login-narrative">
      <HeroSection @explore="scrollToSection('collection')" />
      <CollectionSection />
      <DistributionSection />
      <TraceSection />
      <RolesSection @open-login="openLogin" />
    </div>
    <LoginModal v-model="loginOpen" :credentials="loginForm" :loading="loginLoading" :error-text="errorText" @submit="login" />
  </main>
</template>
