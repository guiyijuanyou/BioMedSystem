<script setup>
defineProps({
  sections: { type: Array, required: true },
  activeSection: { type: String, required: true },
  progress: { type: Number, default: 0 }
});

defineEmits(["navigate"]);
</script>

<template>
  <aside class="scroll-progress" aria-label="页面阅读进度" :style="{ '--scroll-progress': `${Math.max(0, Math.min(1, progress)) * 100}%` }">
    <span class="scroll-progress__line" aria-hidden="true"></span>
    <button
      v-for="(section, index) in sections"
      :key="section.id"
      type="button"
      :class="{ 'is-active': activeSection === section.id }"
      :aria-label="`前往${section.label}`"
      @click="$emit('navigate', section.id)"
    >
      <span>{{ String(index + 1).padStart(2, '0') }}</span>
      <small>{{ section.label }}</small>
    </button>
  </aside>
</template>
