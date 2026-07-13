<script setup>
import { onBeforeUnmount, watch } from "vue";
import { useLoop } from "@tresjs/core";
import {
  createMedicinalLeafModel,
  disposeMedicinalLeafModel,
  updateMedicinalLeafModel,
} from "@/components/background/medicinalLeafGeometry";

const props = defineProps({
  active: { type: Boolean, default: true },
  particleCount: { type: Number, default: 32 },
});

const model = createMedicinalLeafModel({ particleCount: props.particleCount });
const { onBeforeRender, start, stop } = useLoop();
let elapsed = 0;

onBeforeRender(({ delta }) => {
  if (!props.active) return;
  elapsed += Math.min(delta, 0.05);
  updateMedicinalLeafModel(model, elapsed);
});

watch(
  () => props.active,
  enabled => {
    if (enabled) start();
    else stop();
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  disposeMedicinalLeafModel(model);
});
</script>

<template>
  <primitive :object="model.group" />
</template>
