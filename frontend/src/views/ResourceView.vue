<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import MapPicker from "@/components/MapPicker.vue";
import { api } from "@/services/api";

const props = defineProps({
  moduleKey: { type: String, required: true },
  config: { type: Object, required: true },
  editId: String
});

const emit = defineEmits(["notify", "edit-consumed"]);
const items = ref([]);
const search = ref("");
const editingId = ref(null);
const form = reactive({});
const saving = ref(false);

const textareaFields = ["environment", "indicator", "applicationMaterial", "tracking", "transformation", "levelRule"];
const statuses = ["待审核", "已通过", "已发布", "数据采集中", "已归档"];

const filtered = computed(() => {
  const keyword = search.value.trim().toLowerCase();
  return items.value.filter(item => Object.values(item).join(" ").toLowerCase().includes(keyword));
});

function defaultValue(name) {
  const values = {
    recordedAt: new Date().toISOString().slice(0, 19),
    collector: "电脑终端录入",
    temperature: "20.0",
    humidity: "80",
    soilPh: "6.5",
    status: "待审核",
    effectiveDate: new Date().toISOString().slice(0, 10)
  };
  return values[name] || "";
}

function resetForm(item = null) {
  editingId.value = item?.id || null;
  Object.keys(form).forEach(key => delete form[key]);
  props.config.fields.forEach(([name]) => {
    form[name] = item?.[name] ?? defaultValue(name);
  });
}

async function load() {
  const result = await api(`/api/${props.moduleKey}`);
  items.value = result.items || [];
  if (props.editId) {
    const row = items.value.find(item => item.id === props.editId);
    resetForm(row || null);
    emit("edit-consumed");
  } else resetForm();
}

async function save() {
  saving.value = true;
  try {
    const payload = { ...form };
    if (editingId.value) payload.id = editingId.value;
    await api(`/api/${props.moduleKey}`, {
      method: editingId.value ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    emit("notify", editingId.value ? "修改已保存" : "新增记录已保存");
    await load();
  } catch (error) {
    emit("notify", error.message);
  } finally {
    saving.value = false;
  }
}

function edit(item) {
  resetForm(item);
  document.querySelector(".editor-panel")?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function display(value) {
  return value === undefined || value === null || value === "" ? "-" : value;
}

onMounted(load);
watch(() => props.moduleKey, load);
watch(() => props.editId, id => {
  if (!id) return;
  const row = items.value.find(item => item.id === id);
  if (row) {
    resetForm(row);
    emit("edit-consumed");
  }
});
</script>

<template>
  <div class="module-layout">
    <section class="panel">
      <div class="panel-head">
        <div><h2>{{ config.title }}</h2><span>{{ config.hint }}</span></div>
      </div>
      <div class="table-tools">
        <input v-model="search" type="search" placeholder="搜索名称、地区、负责人、状态">
        <button type="button" @click="resetForm()">新增记录</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr><th v-for="[, label] in config.fields" :key="label">{{ label }}</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="item in filtered" :key="item.id">
              <td v-for="[name] in config.fields" :key="name">
                <span v-if="['status', 'result', 'level'].includes(name)" class="status">{{ display(item[name]) }}</span>
                <template v-else>{{ display(item[name]) }}</template>
              </td>
              <td><button class="secondary compact-button" type="button" @click="edit(item)">编辑</button></td>
            </tr>
            <tr v-if="!filtered.length"><td :colspan="config.fields.length + 1" class="empty-cell">暂无符合条件的数据</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel editor-panel">
      <div class="panel-head">
        <div><h2>{{ editingId ? "编辑记录" : "新增记录" }}</h2><span>提交后立即保存</span></div>
      </div>
      <form class="form-grid" @submit.prevent="save">
        <label v-for="[name, label] in config.fields" :key="name">
          {{ label }}
          <textarea v-if="textareaFields.includes(name)" v-model="form[name]"></textarea>
          <select v-else-if="name === 'status'" v-model="form[name]"><option v-for="status in statuses" :key="status">{{ status }}</option></select>
          <select v-else-if="name === 'collector'" v-model="form[name]"><option>电脑终端录入</option><option>手机APP采集</option><option>传感器网关</option></select>
          <input v-else v-model="form[name]">
        </label>
        <MapPicker
          v-if="moduleKey === 'herbs'"
          v-model:latitude="form.latitude"
          v-model:longitude="form.longitude"
        />
        <button type="submit" :disabled="saving">{{ saving ? "保存中..." : editingId ? "保存修改" : "提交新增" }}</button>
      </form>
    </section>
  </div>
</template>
