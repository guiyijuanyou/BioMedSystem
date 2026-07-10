<script setup>
import { inject, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { api } from "@/services/api";

const router = useRouter();
const route = useRoute();
const appLogin = inject("appLogin");

const loginForm = ref({ username: "admin", password: "123456" });
const loginLoading = ref(false);
const errorText = ref("");

async function login() {
  loginLoading.value = true;
  errorText.value = "";
  try {
    const user = await api("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(loginForm.value)
    });
    appLogin(user);
    const redirect = route.query.redirect || "/dashboard";
    router.push(redirect);
  } catch (error) {
    errorText.value = error.message;
  } finally {
    loginLoading.value = false;
  }
}
</script>

<template>
  <section class="login-page">
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
      <p v-if="errorText" style="color: var(--danger); font-size: 13px; margin: 0 0 8px;">{{ errorText }}</p>
      <button type="submit" :disabled="loginLoading">{{ loginLoading ? "登录中..." : "登录系统" }}</button>
      <p>演示账号：admin、teacher、researcher、student，密码均为 123456。</p>
    </form>
  </section>
</template>
