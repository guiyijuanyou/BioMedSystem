<script setup>
import { TresCanvas } from "@tresjs/core";
import MedicinalLeafModel from "@/components/background/MedicinalLeafModel.vue";

defineProps({
  active: { type: Boolean, default: true },
  dpr: { type: Array, default: () => [1, 1.25] },
  particleCount: { type: Number, default: 32 },
});
</script>

<template>
  <TresCanvas
    class="authenticated-leaf-background__canvas"
    clear-color="#000000"
    :clear-alpha="0"
    :dpr="dpr"
    :alpha="true"
    :antialias="true"
  >
    <TresPerspectiveCamera :position="[0, 0, 9]" :fov="42" />
    <TresAmbientLight :intensity="1.25" color="#edf4ff" />
    <TresDirectionalLight :position="[4, 5, 6]" :intensity="2.8" color="#68cfff" />
    <TresPointLight :position="[-3, -1, 4]" :intensity="18" color="#ff8a4c" />
    <MedicinalLeafModel :active="active" :particle-count="particleCount" />
  </TresCanvas>
</template>
