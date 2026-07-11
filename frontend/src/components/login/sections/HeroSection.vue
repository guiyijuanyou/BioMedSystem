<script setup>
import { ArrowDown, DatabaseZap, ShieldCheck, Sparkles } from "lucide-vue-next";
defineEmits(["explore"]);
</script>

<template>
  <section id="cloud" class="login-section hero-section" data-section="cloud">
    <div class="section-copy hero-copy">
      <span class="section-kicker"><Sparkles :size="15" /> BIOMEDICAL DATA CLOUD · CQUTCM</span>
      <h1>让每一份生物医药数据，<em>都拥有清晰的生命轨迹。</em></h1>
      <p>连接药材资源、生长环境、质量图谱与全链路溯源，让采集、研究与教学协作发生在同一个数据空间。</p>
      <div class="hero-actions">
        <button class="hero-primary" type="button" @click="$emit('explore')">探索数据链路 <ArrowDown :size="18" /></button>
        <span><i></i> 数据空间在线 · 安全连接</span>
      </div>
      <div class="hero-proof">
        <div><DatabaseZap :size="18" /><strong>15+</strong><span>业务模块联动</span></div>
        <div><ShieldCheck :size="18" /><strong>4 级</strong><span>角色权限协同</span></div>
      </div>
    </div>
    <div class="hero-data-cards" aria-hidden="true">
      <article class="data-float data-float--temperature"><span>实时温度</span><strong>24.8<small>°C</small></strong><i></i></article>
      <article class="data-float data-float--spectrum"><span>图谱相似度</span><strong>98.6<small>%</small></strong><svg viewBox="0 0 160 42"><path d="M2 36 L16 34 L24 8 L31 34 L53 32 L62 14 L70 33 L92 31 L103 4 L109 30 L131 27 L142 12 L158 26" /></svg></article>
      <article class="data-float data-float--trace"><span>溯源链路</span><strong>04 / 04</strong><small>全节点已验证</small></article>
    </div>
  </section>
</template>
