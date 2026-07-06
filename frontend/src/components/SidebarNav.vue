<script setup>
import { navItems } from "@/config";

defineProps({
  active: { type: String, required: true },
  open: { type: Boolean, default: false }
});

const emit = defineEmits(["select", "close"]);
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
        {{ label }}
      </button>
    </nav>
  </aside>
</template>
