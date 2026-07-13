<script setup>
import { defineAsyncComponent } from "vue";
import { useAuthenticatedBackground } from "@/composables/useAuthenticatedBackground";

const EmptyBackground = { render: () => null };
const MedicinalLeafScene = defineAsyncComponent(() => (
  import("@/components/background/MedicinalLeafScene.vue").catch(() => EmptyBackground)
));

const {
  shouldRenderWebgl,
  active,
  dpr,
  particleCount,
} = useAuthenticatedBackground();
</script>

<template>
  <div class="authenticated-leaf-background" aria-hidden="true">
    <div class="authenticated-leaf-background__static">
      <svg
        class="authenticated-leaf-background__static-leaf"
        viewBox="0 0 520 760"
        fill="none"
        focusable="false"
      >
        <path
          class="authenticated-leaf-background__leaf-surface"
          d="M266 714C160 616 91 470 112 318C130 186 216 82 289 34C370 142 430 277 407 427C386 564 326 657 266 714Z"
        />
        <path class="authenticated-leaf-background__leaf-vein" d="M266 714C273 552 285 356 289 34" />
        <path class="authenticated-leaf-background__leaf-vein" d="M273 570L157 474M278 478L134 368M282 382L159 267M285 290L207 174" />
        <path class="authenticated-leaf-background__leaf-vein" d="M273 570L362 485M278 478L398 381M282 382L397 282M285 290L357 177" />
      </svg>
    </div>
    <MedicinalLeafScene
      v-if="shouldRenderWebgl"
      :active="active"
      :dpr="dpr"
      :particle-count="particleCount"
    />
  </div>
</template>
