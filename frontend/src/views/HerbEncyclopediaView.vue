<script setup>
import { computed, inject, onBeforeUnmount, onMounted, ref, shallowRef, watch } from "vue";
import { useRouter } from "vue-router";
import { api } from "@/services/api";
import { Search, X, BookOpen, ChevronLeft, ChevronRight, ImageOff } from "lucide-vue-next";

const router = useRouter();
const notify = inject("notify");
const allHerbs = shallowRef([]);
const keyword = ref("");
const debouncedKeyword = ref("");
const page = ref(1);
const pageSize = 18;
const imageError = ref({});
let searchTimer;

watch(keyword, (val) => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    debouncedKeyword.value = val.trim();
    page.value = 1;
  }, 200);
});

function doSearch() {
  clearTimeout(searchTimer);
  debouncedKeyword.value = keyword.value.trim();
  page.value = 1;
}

function clearSearch() {
  keyword.value = "";
  clearTimeout(searchTimer);
  debouncedKeyword.value = "";
  page.value = 1;
}

function onImageError(id) {
  imageError.value[id] = true;
}

async function load() {
  try {
    const res = await api("/api/herb-encyclopedia");
    allHerbs.value = res.items || [];
  } catch (e) {
    notify(e.message);
  }
}

function matches(item, kw) {
  if (!kw) return true;
  const q = kw.toLowerCase();
  return [item.name, item.pinyin, item.englishName, item.latinName, item.category,
          item.efficacy, item.natureFlavor, item.sourceDesc, item.originDesc]
    .some(v => v && v.toLowerCase().includes(q));
}

const filtered = computed(() => {
  const kw = debouncedKeyword.value;
  return kw ? allHerbs.value.filter(h => matches(h, kw)) : allHerbs.value;
});

const totalCount = computed(() => filtered.value.length);
const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)));

const pagedHerbs = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filtered.value.slice(start, start + pageSize);
});

function goDetail(herb) {
  router.push(`/herb-encyclopedia/by-name/${encodeURIComponent(herb.name)}`);
}

function prevPage() {
  if (page.value > 1) page.value--;
  document.documentElement.scrollTop = 0;
}

function nextPage() {
  if (page.value < totalPages.value) page.value++;
  document.documentElement.scrollTop = 0;
}

onMounted(load);
onBeforeUnmount(() => clearTimeout(searchTimer));
</script>

<template>
  <div class="encyclopedia-page">
    <!-- Search -->
    <div class="encyclopedia-search">
      <div class="encyclopedia-search__inner">
        <Search :size="18" class="encyclopedia-search__icon" />
        <input
          v-model="keyword"
          placeholder="搜索药材名称、拼音、功效..."
          @keyup.enter="doSearch"
        />
        <button v-if="keyword" class="encyclopedia-search__clear" type="button" @click="clearSearch">
          <X :size="16" />
        </button>
      </div>
      <button class="encyclopedia-search__btn" type="button" @click="doSearch">搜索</button>
    </div>

    <!-- Summary -->
    <div class="encyclopedia-summary">
      <BookOpen :size="16" />
      <span>共收录 <strong>{{ totalCount }}</strong> 种中药材，点击卡片查看详细信息</span>
    </div>

    <!-- Card Grid -->
    <div v-if="pagedHerbs.length" class="encyclopedia-grid">
      <article
        v-for="herb in pagedHerbs"
        :key="herb.id"
        class="herb-card"
        @click="goDetail(herb)"
      >
        <div class="herb-card__image">
          <img
            v-if="herb.imageFileId && !imageError[herb.id]"
            :src="`/api/files/${herb.imageFileId}/preview`"
            :alt="herb.name"
            loading="lazy"
            @error="onImageError(herb.id)"
          />
          <div v-else class="herb-card__placeholder">
            <ImageOff :size="28" />
          </div>
        </div>
        <div class="herb-card__body">
          <h3 class="herb-card__name">{{ herb.name }}</h3>
          <span v-if="herb.pinyin" class="herb-card__pinyin">{{ herb.pinyin }}</span>
          <span v-if="herb.category" class="herb-card__category">{{ herb.category }}</span>
          <p v-if="herb.efficacy" class="herb-card__efficacy">{{ herb.efficacy.slice(0, 60) }}{{ herb.efficacy.length > 60 ? '...' : '' }}</p>
        </div>
      </article>
    </div>

    <!-- Empty -->
    <div v-else class="encyclopedia-empty">
      <p>未找到符合条件的药材</p>
    </div>

    <!-- Pagination -->
    <div v-if="totalCount > pageSize" class="encyclopedia-pagination">
      <button :disabled="page <= 1" type="button" @click="prevPage"><ChevronLeft :size="16" />上一页</button>
      <span>{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" type="button" @click="nextPage">下一页<ChevronRight :size="16" /></button>
    </div>

  </div>
</template>

<style scoped>
.encyclopedia-page {
  display: grid;
  gap: 16px;
  padding: 0 4px 32px;
  min-height: calc(100vh - 160px);
}

/* Search */
.encyclopedia-search {
  display: flex;
  gap: 8px;
  align-items: center;
}

.encyclopedia-search__inner {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 8px;
  transition: border-color .15s;
}

.encyclopedia-search__inner:focus-within {
  border-color: var(--brand);
}

.encyclopedia-search__icon {
  color: var(--muted);
  flex-shrink: 0;
}

.encyclopedia-search__inner input {
  flex: 1;
  border: none;
  outline: none;
  background: none;
  font-size: 14px;
  color: var(--ink);
}

.encyclopedia-search__inner input::placeholder {
  color: var(--muted);
}

.encyclopedia-search__clear {
  display: grid;
  place-items: center;
  padding: 4px;
  border: none;
  background: var(--surface-subtle);
  color: var(--muted);
  border-radius: 4px;
  cursor: pointer;
}

.encyclopedia-search__btn {
  padding: 8px 20px;
  border: none;
  background: var(--brand);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: background .15s;
}

.encyclopedia-search__btn:hover {
  background: var(--brand-dark);
}

/* Summary */
.encyclopedia-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
}

.encyclopedia-summary strong {
  color: var(--brand);
}

/* Card Grid */
.encyclopedia-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.herb-card {
  display: grid;
  grid-template-rows: 180px auto;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow .2s, transform .15s;
  contain: content;
}

.herb-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.herb-card__image {
  overflow: hidden;
  background: var(--surface-subtle);
  display: grid;
  place-items: center;
}

.herb-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  aspect-ratio: 260 / 180;
}

.herb-card__placeholder {
  display: grid;
  place-items: center;
  color: var(--line-strong);
}

.herb-card__body {
  display: grid;
  gap: 4px;
  padding: 12px 14px 14px;
}

.herb-card__name {
  margin: 0;
  font-size: 16px;
  color: var(--ink);
}

.herb-card__pinyin {
  font-size: 12px;
  color: var(--muted);
}

.herb-card__category {
  font-size: 11px;
  color: var(--brand);
  background: var(--brand-soft);
  padding: 2px 8px;
  border-radius: 4px;
  justify-self: start;
}

.herb-card__efficacy {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Empty */
.encyclopedia-empty {
  display: grid;
  place-items: center;
  padding: 60px 0;
  color: var(--muted);
}

/* Pagination */
.encyclopedia-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 8px 0;
}

.encyclopedia-pagination button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 1px solid var(--line);
  background: var(--surface);
  border-radius: 6px;
  font-size: 13px;
  color: var(--ink);
  cursor: pointer;
  transition: border-color .15s;
}

.encyclopedia-pagination button:disabled {
  opacity: .4;
  cursor: default;
}

.encyclopedia-pagination button:not(:disabled):hover {
  border-color: var(--brand);
}

.encyclopedia-pagination span {
  font-size: 13px;
  color: var(--muted);
}

@media (max-width: 700px) {
  .encyclopedia-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }

  .herb-card {
    grid-template-rows: 120px auto;
  }
}
</style>
