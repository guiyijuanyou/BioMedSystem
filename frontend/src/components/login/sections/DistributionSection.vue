<script setup>
import { ref } from "vue";
import { MapPin, Sprout } from "lucide-vue-next";
const nodes = [
  { id: 1, name: "黄连", district: "石柱县", scale: "12,600 亩", x: 68, y: 34 },
  { id: 2, name: "金银花", district: "秀山县", scale: "8,920 亩", x: 76, y: 67 },
  { id: 3, name: "玄参", district: "巫溪县", scale: "6,480 亩", x: 58, y: 18 },
  { id: 4, name: "青蒿", district: "酉阳县", scale: "9,350 亩", x: 64, y: 75 },
  { id: 5, name: "白术", district: "武隆区", scale: "5,720 亩", x: 48, y: 57 }
];
const selected = ref(nodes[0]);
</script>

<template>
  <section id="distribution" class="login-section distribution-section" data-section="distribution">
    <div class="distribution-map">
      <div class="map-grid" aria-hidden="true"></div>
      <svg class="map-outline" viewBox="0 0 600 430" aria-label="重庆药材分布网络">
        <path d="M67 192 C98 121 157 132 194 84 C235 31 304 74 335 54 C382 25 419 81 462 93 C524 111 550 163 523 210 C499 252 549 294 498 332 C451 366 398 342 357 383 C321 418 270 374 224 389 C170 407 138 356 103 329 C63 298 31 246 67 192Z" />
        <path class="network-line" d="M408 146 L456 288 L350 80 L314 322 L246 245 L408 146 M246 245 L456 288 M350 80 L314 322" />
      </svg>
      <button v-for="node in nodes" :key="node.id" class="map-node" :class="{ 'is-active': selected.id === node.id }" :style="{ left: `${node.x}%`, top: `${node.y}%` }" type="button" @click="selected = node">
        <span></span><small>{{ node.name }}</small>
      </button>
      <div class="map-coordinates">29.56° N / 106.55° E</div>
    </div>
    <div class="section-copy distribution-copy">
      <span class="section-index">03 / GEO DISTRIBUTION</span>
      <h2>重庆药材分布网络，<br>让资源<em>被看见</em></h2>
      <p>从品种、区县、种植规模到生态环境，区域节点与采集记录在同一张数据网络中建立联系。</p>
      <article class="login-map-detail">
        <MapPin :size="20" /><div><span>{{ selected.district }} · 核心品种</span><strong>{{ selected.name }}</strong></div>
        <dl><div><dt>种植规模</dt><dd>{{ selected.scale }}</dd></div><div><dt>生态状态</dt><dd>适生区 · 良好</dd></div></dl>
        <p><Sprout :size="16" /> 环境、生长与溯源记录已关联</p>
      </article>
    </div>
  </section>
</template>
