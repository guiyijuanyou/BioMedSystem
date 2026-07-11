<script setup>
import { Menu, X } from "lucide-vue-next";
import { ref } from "vue";

defineProps({
  sections: { type: Array, required: true },
  activeSection: { type: String, required: true }
});

const emit = defineEmits(["navigate", "open-login"]);
const menuOpen = ref(false);

function navigate(id) {
  menuOpen.value = false;
  emit("navigate", id);
}
</script>

<template>
  <header class="login-nav-shell">
    <nav class="login-nav" aria-label="登录页章节导航">
      <button class="login-nav__brand" type="button" @click="navigate('cloud')">
        <span class="login-nav__mark" aria-hidden="true"><i></i><i></i><i></i></span>
        <span><strong>BioMed Cloud</strong><small>生物医药数字信息系统</small></span>
      </button>

      <button class="login-nav__menu" type="button" :aria-expanded="menuOpen" aria-label="切换导航菜单" @click="menuOpen = !menuOpen">
        <X v-if="menuOpen" :size="20" />
        <Menu v-else :size="20" />
      </button>

      <div class="login-nav__links" :class="{ 'is-open': menuOpen }">
        <button
          v-for="section in sections"
          :key="section.id"
          :data-test="`nav-${section.id}`"
          type="button"
          :class="{ 'is-active': activeSection === section.id }"
          @click="navigate(section.id)"
        >{{ section.label }}</button>
      </div>

      <button data-test="nav-login" class="login-nav__login" type="button" @click="$emit('open-login')">
        登录系统 <span aria-hidden="true">↗</span>
      </button>
    </nav>
  </header>
</template>
