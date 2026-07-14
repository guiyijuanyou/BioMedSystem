<script setup>
import { ref } from "vue";
import { Upload } from "lucide-vue-next";
import { api } from "@/services/api";

const emit = defineEmits(["close", "changed"]);

const refName = ref("");
const refHerbName = ref("");
const refFile = ref(null);
const uploading = ref(false);
const references = ref([]);
const myReferences = ref([]);

async function load() {
  try {
    const data = await api("/api/spectrum/references");
    references.value = (data.items || []).filter(r => r.herbName || r.referenceName);
  } catch (e) {}
  try {
    myReferences.value = (await api("/api/spectrum/my-references")).items || [];
  } catch (e) {}
}

async function uploadRef() {
  if (!refFile.value || !refName.value) return;
  uploading.value = true;
  try {
    const fd = new FormData(); fd.append("file", refFile.value);
    fd.append("referenceName", refName.value);
    fd.append("herbName", refHerbName.value || refName.value);
    await api("/api/spectrum/reference", { method: "POST", body: fd });
    refFile.value = null; refName.value = ""; refHerbName.value = "";
    await load();
    emit("changed");
  } catch (e) {}
  finally { uploading.value = false; }
}

async function deleteRef(id) {
  try { await api("/api/spectrum/reference/" + id, { method: "DELETE" }); await load(); emit("changed"); }
  catch (e) {}
}

load();
</script>

<template>
  <div style="position:fixed;inset:0;z-index:100;display:flex;align-items:center;justify-content:center;background:rgba(0,0,0,.3)" @click.self="emit('close')">
    <div style="background:#fff;border-radius:8px;padding:20px 24px;width:500px;max-height:80vh;overflow-y:auto;box-shadow:0 12px 28px rgba(20,42,31,.18)">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px">
        <h3 style="margin:0">管理标准品库</h3>
        <button class="button-secondary" style="min-height:28px;padding:0 8px;font-size:12px" @click="emit('close')">关闭</button>
      </div>
      <div style="display:flex;gap:8px;align-items:flex-end;flex-wrap:wrap;margin-bottom:14px;padding-bottom:14px;border-bottom:1px solid var(--line)">
        <label style="font-size:11px;display:grid;gap:3px">名称 <input v-model="refName" placeholder="如：黄连药典2025版" style="width:160px;min-height:30px" /></label>
        <label style="font-size:11px;display:grid;gap:3px">药材 <input v-model="refHerbName" placeholder="如：黄连" style="width:100px;min-height:30px" /></label>
        <label style="font-size:12px;font-weight:650;white-space:nowrap;cursor:pointer"><input type="file" accept=".csv,.txt" @change="e => refFile = e.target.files[0]" style="width:180px" /></label>
        <button :disabled="uploading || !refFile || !refName" @click="uploadRef" style="min-height:30px;padding:0 12px;font-size:12px">{{ uploading ? '上传中...' : '上传' }}</button>
      </div>
      <div style="font-size:12px;color:var(--muted);margin-bottom:6px">已有标准品（点击删除）</div>
      <div v-for="r in [...references.filter(r => r.status === 'REFERENCE'), ...myReferences]" :key="r.id" style="display:flex;justify-content:space-between;align-items:center;padding:6px 8px;border-bottom:1px solid var(--line)">
        <span>{{ r.referenceName || r.herbName || '未命名' }} <small style="color:var(--muted)">({{ r.status === 'REFERENCE' ? '公共' : '私有' }})</small></span>
        <button class="button-secondary" style="min-height:24px;padding:0 6px;font-size:10px" @click="deleteRef(r.id)">删除</button>
      </div>
    </div>
  </div>
</template>
