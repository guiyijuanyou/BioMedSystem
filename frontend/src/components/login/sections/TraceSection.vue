<script setup>
import { computed, ref } from "vue";
import { Check, FlaskConical, ScanLine } from "lucide-vue-next";
const comparison = ref(67);
const similarity = computed(() => (94.2 + comparison.value * 0.066).toFixed(1));
const stages = ["种植", "采集", "检测", "加工"];
</script>

<template>
  <section id="trace" class="login-section trace-section" data-section="trace">
    <div class="section-copy">
      <span class="section-index">04 / SPECTRUM & TRACE</span>
      <h2>从一条图谱，追踪<br><em>完整质量链路</em></h2>
      <p>标准图谱与样本数据实时比对，质量结果沿着种植、采集、检测和加工节点持续传递。</p>
      <div class="similarity"><ScanLine :size="20" /><span>当前相似度</span><strong>{{ similarity }}%</strong></div>
    </div>
    <div class="spectrum-panel">
      <header><span><FlaskConical :size="17" /> HPLC 图谱智能比对</span><small>SAMPLE CQ-2026-0710</small></header>
      <div class="spectrum-chart">
        <svg viewBox="0 0 720 250" preserveAspectRatio="none"><path class="standard" d="M0 220 L54 218 L70 32 L80 216 L146 213 L161 105 L170 214 L245 211 L260 62 L269 210 L351 207 L367 16 L377 205 L445 202 L460 91 L470 201 L557 198 L572 43 L582 197 L650 194 L669 113 L678 193 L720 191" /><path class="sample" :style="{ clipPath: `inset(0 ${100 - comparison}% 0 0)` }" d="M0 223 L55 220 L72 39 L82 218 L147 216 L163 111 L173 216 L246 214 L262 68 L271 212 L352 210 L369 22 L379 208 L446 205 L462 96 L472 204 L558 201 L574 49 L584 200 L651 197 L671 118 L680 196 L720 194" /></svg>
        <label>样本覆盖范围 <input v-model="comparison" type="range" min="12" max="100"></label>
      </div>
      <ol class="trace-chain"><li v-for="stage in stages" :key="stage"><span><Check :size="15" /></span><strong>{{ stage }}</strong><small>数据已确认</small></li></ol>
    </div>
  </section>
</template>
