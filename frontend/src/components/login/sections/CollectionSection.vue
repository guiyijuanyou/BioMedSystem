<script setup>
import { Cpu, MonitorSmartphone, Radio, Sprout } from "lucide-vue-next";
const metrics = [
  { label: "环境温度", value: "24.8", unit: "°C", trend: "+0.6", icon: Radio },
  { label: "空气湿度", value: "68.4", unit: "%", trend: "稳定", icon: Cpu },
  { label: "土壤 pH", value: "6.42", unit: "", trend: "适宜", icon: Sprout },
  { label: "生长阶段", value: "旺长期", unit: "", trend: "第 86 天", icon: MonitorSmartphone }
];
</script>

<template>
  <section id="collection" class="login-section collection-section" data-section="collection">
    <div class="section-copy">
      <span class="section-index">02 / REAL-TIME COLLECTION</span>
      <h2>多源采集，在一条<br><em>数据脉络</em>中实时汇聚</h2>
      <p>传感器、移动端与电脑端持续接入环境和生长记录，每次变化都成为可分析、可回溯的数据节点。</p>
      <div class="source-tags"><span>IoT 传感器</span><span>移动 APP</span><span>电脑录入</span></div>
    </div>
    <div class="collection-console">
      <header><span><i></i> LIVE MONITOR</span><small>最后同步 12 秒前</small></header>
      <div class="metric-grid">
        <article v-for="metric in metrics" :key="metric.label">
          <component :is="metric.icon" :size="20" />
          <span>{{ metric.label }}</span><strong>{{ metric.value }}<small>{{ metric.unit }}</small></strong><em>{{ metric.trend }}</em>
        </article>
      </div>
      <div class="data-pipeline"><span v-for="index in 7" :key="index"></span><strong>DATA CORE</strong></div>
    </div>
  </section>
</template>
