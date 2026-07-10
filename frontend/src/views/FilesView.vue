<script setup>
import { onMounted, ref } from "vue";
import { Download, Eye, Upload } from "lucide-vue-next";
import { api } from "@/services/api";

defineProps({
  permissions: { type: Object, default: () => ({ create: true }) },
  role: { type: String, default: "admin" }
});

const emit = defineEmits(["notify"]);
const items = ref([]);
const category = ref("教学视频");
const file = ref(null);
const uploading = ref(false);

const categories = ["教学视频", "课程课件", "图片资料", "图谱文件", "培训材料", "评价佐证", "溯源附件"];

async function load() {
  const result = await api("/api/files");
  items.value = result.items || [];
}

function chooseFile(event) {
  file.value = event.target.files?.[0] || null;
}

async function upload() {
  if (!file.value) return;
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append("category", category.value);
    formData.append("file", file.value);
    await api("/api/files/upload", {
      method: "POST",
      body: formData
    });
    emit("notify", "文件上传完成，可在教学视频与资料中发布到课程");
    file.value = null;
    const input = document.querySelector("#vue-file-input");
    if (input) input.value = "";
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
      <div class="panel-head">
        <div>
          <h2>资料文件</h2>
          <span>教师或科研人员先上传视频、课件、图片等文件，再发布到课程中</span>
        </div>
      </div>
      <form v-if="permissions.create !== false" class="form-grid" @submit.prevent="upload">
        <label>
          资料分类
          <select v-model="category">
            <option v-for="item in categories" :key="item">{{ item }}</option>
          </select>
        </label>
        <label>
          选择文件
          <input id="vue-file-input" type="file" required @change="chooseFile">
        </label>
        <button type="submit" :disabled="uploading || !file">
          <Upload :size="16" />{{ uploading ? "上传中..." : "上传文件" }}
        </button>
      </form>
      <p v-else class="empty-state">当前角色仅可查看和下载已发布资料</p>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>已上传资料</h2>
          <span>这些文件可以在“教学视频与资料”中选择并发布到课程</span>
        </div>
      </div>
      <div class="file-list">
        <article v-for="item in items" :key="item.id" class="file-row">
          <div>
            <strong :title="item.fileName">{{ item.fileName }}</strong>
            <small>{{ item.category }} · {{ sizeText(item.sizeBytes) }}</small>
          </div>
          <div class="file-actions">
            <a :href="`/api/files/${item.id}/preview`" target="_blank" rel="noopener">
              <button class="secondary" type="button"><Eye :size="15" />查看</button>
            </a>
            <a :href="`/api/files/${item.id}/download`">
              <button type="button"><Download :size="15" />下载</button>
            </a>
          </div>
        </article>
        <p v-if="!items.length" class="empty-state">暂无上传文件</p>
      </div>
    </section>
  </div>
</template>
