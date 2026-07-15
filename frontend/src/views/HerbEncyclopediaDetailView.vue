<script setup>
import { inject, onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { api } from "@/services/api";
import { ArrowLeft, BookOpen, ImageOff, ExternalLink } from "lucide-vue-next";

const router = useRouter();
const route = useRoute();
const notify = inject("notify");

const herb = ref(null);
const loading = ref(true);
const imageError = ref(false);
let abortController;

async function load() {
  const name = route.params.name;
  if (!name) {
    notify("缺少药材名称");
    router.replace("/herb-encyclopedia");
    return;
  }

  loading.value = true;
  abortController = new AbortController();
  try {
    const res = await api(`/api/herb-encyclopedia/by-name/${encodeURIComponent(name)}`, {
      signal: abortController.signal,
    });
    if (res && res.name) {
      herb.value = res;
    } else {
      herb.value = null;
      notify("未找到该药材");
    }
  } catch (e) {
    if (e.name !== "AbortError") {
      notify(e.message || "加载失败");
      herb.value = null;
    }
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push("/herb-encyclopedia");
}

onMounted(load);
onBeforeUnmount(() => {
  abortController?.abort();
});
</script>

<template>
  <div class="herb-detail-page">
    <!-- Loading -->
    <div v-if="loading" class="herb-detail-loading">
      <div class="herb-detail-loading__spinner"></div>
      <p>正在加载药材详情...</p>
    </div>

    <!-- Not found -->
    <div v-else-if="!herb" class="herb-detail-empty">
      <BookOpen :size="48" />
      <p>未找到该药材信息</p>
      <button class="herb-detail-back-btn" type="button" @click="goBack">返回药材百科</button>
    </div>

    <!-- Detail -->
    <template v-else>
      <!-- Back nav -->
      <nav class="herb-detail-nav">
        <button class="herb-nav-back" type="button" @click="goBack">
          <ArrowLeft :size="18" /> 返回药材百科
        </button>
      </nav>

      <!-- Hero section -->
      <section class="herb-detail-hero">
        <div class="herb-detail-hero__image">
          <img
            v-if="herb.imageFileId && !imageError"
            :src="`/api/files/${herb.imageFileId}/preview`"
            :alt="herb.name"
            @error="imageError = true"
          />
          <div v-else class="herb-detail-hero__image-placeholder">
            <ImageOff :size="48" />
            <span>暂无图片</span>
          </div>
        </div>

        <div class="herb-detail-hero__info">
          <h1 class="herb-detail-title">{{ herb.name }}</h1>
          <div class="herb-detail-tags">
            <span v-if="herb.pinyin" class="herb-tag">{{ herb.pinyin }}</span>
            <span v-if="herb.category" class="herb-tag herb-tag--category">{{ herb.category }}</span>
          </div>

          <div class="herb-detail-meta">
            <div v-if="herb.englishName" class="herb-meta-item">
              <span class="herb-meta-label">英文名</span>
              <span class="herb-meta-value">{{ herb.englishName }}</span>
            </div>
            <div v-if="herb.latinName" class="herb-meta-item">
              <span class="herb-meta-label">拉丁学名</span>
              <span class="herb-meta-value"><i>{{ herb.latinName }}</i></span>
            </div>
            <div v-if="herb.natureFlavor" class="herb-meta-item">
              <span class="herb-meta-label">性味</span>
              <span class="herb-meta-value">{{ herb.natureFlavor }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Info sections -->
      <div class="herb-detail-sections">
        <article v-if="herb.sourceDesc" class="herb-info-card">
          <h2 class="herb-info-card__title">来源</h2>
          <p class="herb-info-card__text">{{ herb.sourceDesc }}</p>
        </article>

        <article v-if="herb.originDesc" class="herb-info-card">
          <h2 class="herb-info-card__title">产地</h2>
          <p class="herb-info-card__text">{{ herb.originDesc }}</p>
        </article>

        <article v-if="herb.macroscopic" class="herb-info-card">
          <h2 class="herb-info-card__title">性状</h2>
          <p class="herb-info-card__text">{{ herb.macroscopic }}</p>
        </article>

        <article v-if="herb.qualityDesc" class="herb-info-card">
          <h2 class="herb-info-card__title">品质</h2>
          <p class="herb-info-card__text">{{ herb.qualityDesc }}</p>
        </article>

        <article v-if="herb.efficacy" class="herb-info-card herb-info-card--accent">
          <h2 class="herb-info-card__title">功效</h2>
          <p class="herb-info-card__text">{{ herb.efficacy }}</p>
        </article>

        <article v-if="herb.sourceUrl" class="herb-info-card herb-info-card--source">
          <h2 class="herb-info-card__title">数据来源</h2>
          <a :href="herb.sourceUrl" target="_blank" rel="noopener" class="herb-source-link">
            <ExternalLink :size="14" /> HKBU 中药材图像数据库
          </a>
        </article>
      </div>
    </template>
  </div>
</template>

<style scoped>
.herb-detail-page {
  max-width: 820px;
  margin: 0 auto;
  padding: 0 4px 40px;
}

/* Loading */
.herb-detail-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 80px 0;
  color: var(--muted);
}

.herb-detail-loading__spinner {
  width: 28px;
  height: 28px;
  border: 3px solid var(--line);
  border-top-color: var(--brand);
  border-radius: 50%;
  animation: herb-spin .7s linear infinite;
}

@keyframes herb-spin {
  to { transform: rotate(360deg); }
}

/* Empty */
.herb-detail-empty {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 80px 0;
  color: var(--muted);
}

.herb-detail-back-btn {
  padding: 8px 20px;
  border: 1px solid var(--line);
  background: var(--surface);
  border-radius: 8px;
  font-size: 14px;
  color: var(--ink);
  cursor: pointer;
}

.herb-detail-back-btn:hover {
  border-color: var(--brand);
}

/* Back nav */
.herb-detail-nav {
  margin-bottom: 16px;
}

.herb-nav-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  background: var(--surface);
  color: var(--muted);
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: color .15s, background .15s;
}

.herb-nav-back:hover {
  color: var(--brand);
  background: var(--brand-soft);
}

/* Hero section */
.herb-detail-hero {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 28px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.herb-detail-hero__image {
  border-radius: 8px;
  overflow: hidden;
  background: var(--surface-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  max-height: 420px;
}

.herb-detail-hero__image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.herb-detail-hero__image-placeholder {
  display: grid;
  place-items: center;
  gap: 10px;
  padding: 50px;
  color: var(--muted);
  font-size: 14px;
}

.herb-detail-hero__info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}

.herb-detail-title {
  margin: 0;
  font-size: 28px;
  color: var(--ink);
  line-height: 1.3;
}

.herb-detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.herb-tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 4px;
  background: var(--surface-subtle);
  color: var(--muted);
  border: 1px solid var(--line);
}

.herb-tag--category {
  background: var(--brand-soft);
  color: var(--brand);
  border-color: transparent;
}

.herb-detail-meta {
  display: grid;
  gap: 6px;
  margin-top: 4px;
}

.herb-meta-item {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.herb-meta-label {
  font-weight: 600;
  color: var(--muted);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: .04em;
}

.herb-meta-value {
  color: var(--ink);
}

/* Info sections */
.herb-detail-sections {
  display: grid;
  gap: 12px;
}

.herb-info-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 20px 24px;
}

.herb-info-card__title {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: .04em;
}

.herb-info-card__text {
  margin: 0;
  font-size: 15px;
  color: var(--ink);
  line-height: 1.7;
}

.herb-info-card--accent {
  border-left: 3px solid var(--accent, #b85c2a);
}

.herb-info-card--accent .herb-info-card__title {
  color: var(--accent, #b85c2a);
}

.herb-info-card--accent .herb-info-card__text {
  color: var(--accent, #b85c2a);
  font-weight: 500;
}

.herb-info-card--source {
  background: var(--surface-subtle);
}

.herb-source-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--brand);
  text-decoration: none;
}

.herb-source-link:hover {
  text-decoration: underline;
}

@media (max-width: 700px) {
  .herb-detail-hero {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 16px;
  }

  .herb-detail-hero__image {
    min-height: 200px;
    max-height: 280px;
  }

  .herb-detail-title {
    font-size: 22px;
  }

  .herb-meta-item {
    grid-template-columns: 70px 1fr;
  }

  .herb-info-card {
    padding: 16px;
  }
}
</style>
