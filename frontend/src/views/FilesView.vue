<script setup>
import { onMounted, ref } from "vue";
import { Download, Eye, Upload } from "lucide-vue-next";
import { api } from "@/services/api";

const emit = defineEmits(["notify"]);
const items = ref([]);
const category = ref("教学视频");
const file = ref(null);
const uploading = ref(false);

async function load() {
  const result = await api("/api/files");
  items.value = result.items || [];
}

function chooseFile(event) {
  file.value = event.target.files?.[0] || null;
}

function toBase64(value) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result).split(",")[1]);
    reader.onerror = reject;
    reader.readAsDataURL(value);
  });
}

async function upload() {
  if (!file.value) return;
  uploading.value = true;
  try {
    await api("/api/files/upload", {
      method: "POST",
      body: JSON.stringify({
        category: category.value,
        fileName: file.value.name,
        contentBase64: await toBase64(file.value)
      })
    });
    emit("notify", "文件上传完成");
    file.value = null;
    document.querySelector("#vue-file-input").value = "";
    await load();
  } catch (error) {
    emit("notify", error.message);
  } finally {
    uploading.value = false;
  }
}

function sizeText(size) {
  const bytes = Number(size || 0);
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

onMounted(load);
</script>

<template>
  <div class="module-layout">
    <section class="panel">
      <div class="panel-head"><div><h2>资料上传下载</h2><span>课程视频、图谱、培训材料、评价佐证</span></div></div>
      <form class="form-grid" @submit.prevent="upload">
        <label>资料分类<select v-model="category"><option>教学视频</option><option>图谱文件</option><option>培训材料</option><option>评价佐证</option><option>溯源附件</option></select></label>
        <label>选择文件<input id="vue-file-input" type="file" required @change="chooseFile"></label>
        <button type="submit" :disabled="uploading || !file"><Upload :size="16" />{{ uploading ? "上传中..." : "上传文件" }}</button>
      </form>
    </section>
    <section class="panel">
      <div class="panel-head"><div><h2>已上传资料</h2><span>支持在线查看与下载</span></div></div>
      <div class="file-list">
        <article v-for="item in items" :key="item.id" class="file-row">
          <div><strong :title="item.fileName">{{ item.fileName }}</strong><small>{{ item.category }} · {{ sizeText(item.size) }}</small></div>
          <div class="file-actions">
            <a :href="`/api/files/${item.id}/preview`" target="_blank" rel="noopener"><button class="secondary" type="button"><Eye :size="15" />查看</button></a>
            <a :href="`/api/files/${item.id}/download`"><button type="button"><Download :size="15" />下载</button></a>
          </div>
        </article>
        <p v-if="!items.length" class="empty-state">暂无上传文件</p>
      </div>
    </section>
  </div>
</template>
