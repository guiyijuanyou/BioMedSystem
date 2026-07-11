<script setup>
import { computed, ref } from "vue";
import { TresCanvas } from "@tresjs/core";
import DnaHelix from "@/components/login/DnaHelix.vue";
import { resolveSceneState } from "@/composables/useLoginScene";

const props = defineProps({ progress: { type: Number, default: 0 } });
const pointer = ref({ x: 0, y: 0 });
const sceneState = computed(() => resolveSceneState(props.progress));

function movePointer(event) {
  const rect = event.currentTarget.getBoundingClientRect();
  pointer.value = {
    x: ((event.clientX - rect.left) / rect.width - 0.5) * 2,
    y: ((event.clientY - rect.top) / rect.height - 0.5) * 2
  };
}
</script>

<template>
  <div class="biomed-scene" aria-label="交互式 DNA 双螺旋数据模型" @pointermove="movePointer" @pointerleave="pointer = { x: 0, y: 0 }">
    <TresCanvas clear-color="#f6f9fc" :dpr="[1, 1.6]" :alpha="true" :antialias="true">
      <TresPerspectiveCamera :position="[0, 0, sceneState.cameraZ]" :fov="44" />
      <TresAmbientLight :intensity="1.1" color="#d8e9ff" />
      <TresDirectionalLight :position="[4, 5, 6]" :intensity="3.2" color="#8ec5ff" />
      <TresPointLight :position="[-4, -2, 3]" :intensity="28" color="#ff8a5c" />
      <DnaHelix v-bind="sceneState" :pointer-x="pointer.x" :pointer-y="pointer.y" />
    </TresCanvas>
    <div class="biomed-scene__fallback" aria-hidden="true">
      <span v-for="index in 12" :key="index" :style="{ '--i': index }"></span>
    </div>
  </div>
</template>
