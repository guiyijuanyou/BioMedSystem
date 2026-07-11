<script setup>
import { inject, onMounted, ref } from "vue";
import { Download, Eye, Trash2, Upload } from "lucide-vue-next";
import { api, authHeader } from "@/services/api";

const permissions = inject("modulePermissions");
const currentRole = inject("currentRole");
const notify = inject("notify");

const items = ref([]);
const category = ref("教学视频");
const file = ref(null);
const uploading = ref(false);

const categories = ["教学视频", "课程课件", "图片资料", "图谱文件", "培训材料", "评价佐证", "溯源附件"];

async function load() {
  const result = await api("/api/files");
  items.value = result.items || [];
}

/** fetch + Blob 方式下载/预览，自动携带 Authorization header */
async function fetchFile(url, openPreview) {
  try {
    const headers = authHeader();
    const response = await fetch(url, { headers });
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || "请求失败");
    }
    const blob = await response.blob();
    const objectUrl = URL.createObjectURL(blob);
    if (openPreview) {
      window.open(objectUrl, "_blank", "noopener");
    } else {
      const disposition = response.headers.get("Content-Disposition") || "";
      const match = disposition.match(/filename\*?=(?:UTF-8'')?(.+)/i);
      const fileName = match ? decodeURIComponent(match[1]) : "download";
      const a = document.createElement("a");
      a.href = objectUrl;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    }
    setTimeout(() => URL.revokeObjectURL(objectUrl), 30000);
  } catch (e) {
    notify(e.message || "操作失败");
  }
}

/** 删除文件 */
async function deleteFile(fid) {
  if (!confirm("确定要删除该文件吗？已发布到课程中的资源可能受影响。")) return;
  try {
    await api(`/api/files/${fid}`, { method: "DELETE" });
    notify("文件已删除");
    await load();
  } catch (e) {
    notify(e.message);
  }
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
    notify("文件上传完成，可在教学视频与资料中发布到课程");
    file.value = null;
    const input = document.querySelector("#vue-file-input");
    if (input) input.value = "";
    await load();
  } catch (error) {
    notify(error.message);
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
          <span>这些文件可以在"教学视频与资料"中选择并发布到课程</span>
        </div>
      </div>
      <div class="file-list">
        <article v-for="item in items" :key="item.id" class="file-row">
          <div>
            <strong :title="item.fileName">{{ item.fileName }}</strong>
            <small>{{ item.category }} · {{ sizeText(item.sizeBytes) }}</small>
          </div>
          <div class="file-actions">
            <button class="secondary" type="button" @click="fetchFile(`/api/files/${item.id}/preview`, true)"><Eye :size="15" />查看</button>
            <button type="button" @click="fetchFile(`/api/files/${item.id}/download`, false)"><Download :size="15" />下载</button>
            <button v-if="['admin','teacher','researcher'].includes(currentRole)" class="danger" type="button" @click="deleteFile(item.id)"><Trash2 :size="15" />删除</button>
          </div>
        </article>
        <p v-if="!items.length" class="empty-state">暂无上传文件</p>
      </div>
    </section>
  </div>
</template>
