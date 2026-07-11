<script setup>
import { computed, ref } from "vue";
import { useLoop } from "@tresjs/core";

const props = defineProps({
  rotationY: { type: Number, default: 0 },
  rotationZ: { type: Number, default: 0 },
  spread: { type: Number, default: 0 },
  opacity: { type: Number, default: 1 },
  accentMix: { type: Number, default: 0 },
  pointerX: { type: Number, default: 0 },
  pointerY: { type: Number, default: 0 }
});

const group = ref(null);
const nodes = computed(() => Array.from({ length: 36 }, (_, index) => {
  const strand = index % 2;
  const step = Math.floor(index / 2);
  const angle = step * 0.68 + strand * Math.PI;
  const radius = 1.45 + props.spread * 0.65;
  return {
    id: index,
    strand,
    position: [Math.cos(angle) * radius, (step - 8.5) * 0.34, Math.sin(angle) * radius]
  };
}));

const bars = computed(() => Array.from({ length: 18 }, (_, step) => ({
  id: step,
  position: [0, (step - 8.5) * 0.34, 0],
  rotation: [0, -step * 0.68, 0],
  scale: [2.9 + props.spread * 1.3, 0.028, 0.028]
})));

const particles = Array.from({ length: 42 }, (_, index) => ({
  id: index,
  position: [Math.sin(index * 2.17) * 4.7, Math.cos(index * 1.43) * 4.2, Math.sin(index * 0.77) * 2.6],
  scale: 0.015 + (index % 4) * 0.008
}));

const { onBeforeRender } = useLoop();
onBeforeRender(({ delta }) => {
  if (!group.value) return;
  group.value.rotation.y += delta * 0.12;
  group.value.rotation.x += (props.pointerY * 0.16 - group.value.rotation.x) * 0.035;
});
</script>

<template>
  <TresGroup ref="group" :rotation="[0, rotationY + pointerX * 0.25, rotationZ]">
    <TresMesh v-for="node in nodes" :key="node.id" :position="node.position">
      <TresSphereGeometry :args="[node.strand ? 0.115 : 0.14, 18, 18]" />
      <TresMeshStandardMaterial
        :color="node.strand ? '#635bff' : '#ff7a59'"
        :emissive="node.strand ? '#8b5cf6' : '#ffb38a'"
        :emissive-intensity="1.3 + accentMix"
        :transparent="true"
        :opacity="opacity"
        :roughness="0.25"
        :metalness="0.35"
      />
    </TresMesh>
    <TresMesh v-for="bar in bars" :key="`bar-${bar.id}`" :position="bar.position" :rotation="bar.rotation" :scale="bar.scale">
      <TresBoxGeometry />
      <TresMeshStandardMaterial color="#b8c8ff" emissive="#7a73ff" :emissive-intensity="0.5" :transparent="true" :opacity="opacity * Math.max(0.08, 0.34 - spread * 0.2)" />
    </TresMesh>
    <TresMesh v-for="particle in particles" :key="`particle-${particle.id}`" :position="particle.position" :scale="particle.scale">
      <TresSphereGeometry :args="[1, 8, 8]" />
      <TresMeshBasicMaterial color="#9db8ff" :transparent="true" :opacity="0.5 + accentMix * 0.3" />
    </TresMesh>
  </TresGroup>
</template>
