<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import {
  ChevronLeft, ChevronRight, Clock3, Copy, Download, Eye, FileText,
  Pencil, Plus, Search, Trash2, X
} from "lucide-vue-next";
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
const selectedIds = ref([]);
const panelOpen = ref(false);
const panelMode = ref("edit");
const saving = ref(false);
const deleting = ref(false);
const page = ref(1);
const pageSize = ref(8);
const confirmState = reactive({ open: false, ids: [] });
const form = reactive({});

const textareaFields = ["environment", "indicator", "applicationMaterial", "tracking", "transformation", "levelRule"];
const statuses = ["待审核", "已通过", "已发布", "数据采集中", "已归档"];

const filtered = computed(() => {
  const keyword = search.value.trim().toLowerCase();
  return items.value.filter(item => Object.values(item).join(" ").toLowerCase().includes(keyword));
});
const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize.value)));
const pagedItems = computed(() => filtered.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value));
const selectedItems = computed(() => items.value.filter(item => selectedIds.value.includes(item.id)));
const pageSelected = computed(() => pagedItems.value.length > 0 && pagedItems.value.every(item => selectedIds.value.includes(item.id)));
const panelTitle = computed(() => {
  if (panelMode.value === "view") return "记录详情";
  if (panelMode.value === "duplicate") return "创建记录副本";
  return editingId.value ? "编辑记录" : "新增记录";
});
const primaryField = computed(() => props.config.fields[0]?.[0]);
const primaryLabel = computed(() => props.config.fields[0]?.[1] || "记录");
const primaryValue = computed(() => display(form[primaryField.value]));

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

function fillForm(item = null) {
  editingId.value = item?.id || null;
  Object.keys(form).forEach(key => delete form[key]);
  props.config.fields.forEach(([name]) => {
    form[name] = item?.[name] ?? defaultValue(name);
  });
  if (item) {
    form.createdAt = item.createdAt || "";
    form.updatedAt = item.updatedAt || "";
  }
}

function openCreate() {
  fillForm();
  panelMode.value = "edit";
  panelOpen.value = true;
}

function openView(item) {
  fillForm(item);
  panelMode.value = "view";
  panelOpen.value = true;
}

function openEdit(item) {
  fillForm(item);
  panelMode.value = "edit";
  panelOpen.value = true;
}

function duplicate(item) {
  fillForm(item);
  editingId.value = null;
  delete form.createdAt;
  delete form.updatedAt;
  const nameField = props.config.fields[0]?.[0];
  if (nameField && form[nameField]) form[nameField] = `${form[nameField]}（副本）`;
  panelMode.value = "duplicate";
  panelOpen.value = true;
}

function closePanel() {
  if (saving.value) return;
  panelOpen.value = false;
}

async function load() {
  try {
    const result = await api(`/api/${props.moduleKey}`);
    items.value = result.items || [];
    selectedIds.value = selectedIds.value.filter(id => items.value.some(item => item.id === id));
    if (page.value > totalPages.value) page.value = totalPages.value;
    if (props.editId) {
      const row = items.value.find(item => item.id === props.editId);
      if (row) openEdit(row);
      emit("edit-consumed");
    }
  } catch (error) {
    emit("notify", `数据加载失败：${error.message}`);
  }
}

async function save() {
  saving.value = true;
  try {
    const payload = {};
    props.config.fields.forEach(([name]) => payload[name] = form[name] ?? "");
    if (editingId.value) payload.id = editingId.value;
    await api(`/api/${props.moduleKey}`, {
      method: editingId.value ? "PUT" : "POST",
      body: JSON.stringify(payload)
    });
    emit("notify", editingId.value ? "修改已保存" : "新增记录已保存");
    panelOpen.value = false;
    await load();
  } catch (error) {
    emit("notify", error.message);
  } finally {
    saving.value = false;
  }
}

function requestDelete(ids) {
  confirmState.ids = [...ids];
  confirmState.open = true;
}

async function executeDelete() {
  deleting.value = true;
  try {
    await Promise.all(confirmState.ids.map(id => api(`/api/${props.moduleKey}/${id}`, { method: "DELETE" })));
    const count = confirmState.ids.length;
    selectedIds.value = selectedIds.value.filter(id => !confirmState.ids.includes(id));
    confirmState.open = false;
    panelOpen.value = false;
    await load();
    emit("notify", count > 1 ? `已删除 ${count} 条记录` : "记录已删除");
  } catch (error) {
    emit("notify", error.message);
  } finally {
    deleting.value = false;
  }
}

function togglePageSelection() {
  const ids = pagedItems.value.map(item => item.id);
  if (pageSelected.value) {
    selectedIds.value = selectedIds.value.filter(id => !ids.includes(id));
  } else {
    selectedIds.value = [...new Set([...selectedIds.value, ...ids])];
  }
}

function exportCsv(rows = filtered.value) {
  if (!rows.length) {
    emit("notify", "当前没有可导出的记录");
    return;
  }
  const headers = props.config.fields.map(([, label]) => label);
  const fields = props.config.fields.map(([name]) => name);
  const escape = value => `"${String(value ?? "").replaceAll('"', '""')}"`;
  const csv = [headers.map(escape).join(","), ...rows.map(item => fields.map(name => escape(item[name])).join(","))].join("\r\n");
  const url = URL.createObjectURL(new Blob(["\ufeff", csv], { type: "text/csv;charset=utf-8" }));
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = `${props.config.title}-${new Date().toISOString().slice(0, 10)}.csv`;
  anchor.click();
  URL.revokeObjectURL(url);
  emit("notify", `已导出 ${rows.length} 条记录`);
}

function display(value) {
  return value === undefined || value === null || value === "" ? "-" : value;
}

onMounted(load);
watch(() => props.moduleKey, () => {
  search.value = "";
  selectedIds.value = [];
  page.value = 1;
  panelOpen.value = false;
  load();
});
watch(search, () => page.value = 1);
watch(() => props.editId, id => {
  if (!id) return;
  const row = items.value.find(item => item.id === id);
  if (row) openEdit(row);
  emit("edit-consumed");
});
</script>

<template>
  <section class="panel resource-workbench">
    <div class="panel-head resource-head">
      <div><h2>{{ config.title }}</h2><span>{{ config.hint }}</span></div>
      <button type="button" @click="openCreate"><Plus :size="16" />新增记录</button>
    </div>

    <div class="table-tools">
      <div class="input-with-icon table-search">
        <Search :size="16" />
        <input v-model="search" type="search" placeholder="搜索名称、地区、负责人、状态">
      </div>
      <span class="record-count">共 {{ filtered.length }} 条记录</span>
      <button class="button-secondary" type="button" @click="exportCsv(selectedItems.length ? selectedItems : filtered)">
        <Download :size="16" />{{ selectedItems.length ? `导出已选 (${selectedItems.length})` : "导出数据" }}
      </button>
    </div>

    <div class="selection-slot">
      <div v-if="selectedIds.length" class="selection-bar">
        <span>已选择 <strong>{{ selectedIds.length }}</strong> 条记录</span>
        <button class="danger-button" type="button" @click="requestDelete(selectedIds)"><Trash2 :size="15" />批量删除</button>
        <button class="button-secondary" type="button" @click="selectedIds = []">取消选择</button>
      </div>
    </div>

    <div class="table-wrap resource-table">
      <table>
        <thead>
          <tr>
            <th class="checkbox-cell"><input type="checkbox" :checked="pageSelected" aria-label="选择当前页" @change="togglePageSelection"></th>
            <th v-for="[, label] in config.fields" :key="label">{{ label }}</th>
            <th class="sticky-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedItems" :key="item.id" :class="{ selected: selectedIds.includes(item.id) }">
            <td class="checkbox-cell"><input v-model="selectedIds" type="checkbox" :value="item.id" :aria-label="`选择${item.id}`"></td>
            <td v-for="[name] in config.fields" :key="name">
              <span v-if="['status', 'result', 'level'].includes(name)" class="status">{{ display(item[name]) }}</span>
              <template v-else>{{ display(item[name]) }}</template>
            </td>
            <td class="sticky-action">
              <div class="row-actions">
                <button class="icon-button" type="button" title="查看详情" @click="openView(item)"><Eye :size="16" /></button>
                <button class="icon-button" type="button" title="编辑记录" @click="openEdit(item)"><Pencil :size="16" /></button>
                <button class="icon-button" type="button" title="复制记录" @click="duplicate(item)"><Copy :size="16" /></button>
                <button class="icon-button danger" type="button" title="删除记录" @click="requestDelete([item.id])"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
          <tr v-if="!pagedItems.length">
            <td :colspan="config.fields.length + 2" class="empty-cell">暂无符合条件的数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <footer class="pagination">
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <label>每页<select v-model.number="pageSize" @change="page = 1"><option :value="8">8</option><option :value="15">15</option><option :value="30">30</option></select>条</label>
      <div>
        <button class="icon-button" type="button" title="上一页" :disabled="page <= 1" @click="page--"><ChevronLeft :size="17" /></button>
        <button class="icon-button" type="button" title="下一页" :disabled="page >= totalPages" @click="page++"><ChevronRight :size="17" /></button>
      </div>
    </footer>
  </section>

  <Teleport to="body">
    <div v-if="panelOpen" class="record-drawer-layer" @click.self="closePanel">
      <aside class="record-drawer" role="dialog" aria-modal="true" :aria-label="panelTitle">
        <header class="drawer-header">
          <div class="drawer-title-group">
            <span class="drawer-title-icon"><FileText :size="19" /></span>
            <div><small>{{ config.title }}</small><h2>{{ panelTitle }}</h2></div>
          </div>
          <button class="icon-button" type="button" title="关闭" @click="closePanel"><X :size="20" /></button>
        </header>

        <div v-if="panelMode === 'view'" class="record-detail-body">
          <section class="detail-summary">
            <span>{{ primaryLabel }}</span>
            <h3>{{ primaryValue }}</h3>
            <p>{{ config.hint }}</p>
            <div>
              <span><Clock3 :size="13" />创建于 {{ display(form.createdAt) }}</span>
              <span v-if="form.updatedAt"><Clock3 :size="13" />更新于 {{ form.updatedAt }}</span>
            </div>
          </section>
          <div class="form-section-heading">
            <div><strong>记录信息</strong><span>该记录当前保存的完整业务字段</span></div>
          </div>
          <dl class="detail-grid">
            <div v-for="[name, label] in config.fields" :key="name">
              <dt>{{ label }}</dt>
              <dd><span v-if="['status', 'result', 'level'].includes(name)" class="status">{{ display(form[name]) }}</span><template v-else>{{ display(form[name]) }}</template></dd>
            </div>
          </dl>
        </div>

        <form v-else class="drawer-form" @submit.prevent="save">
          <div v-if="panelMode === 'duplicate'" class="duplicate-notice">
            <Copy :size="17" />
            <div><strong>正在创建一条新记录</strong><span>内容已从原记录复制，保存后会生成新的记录编号。</span></div>
          </div>
          <div class="form-section-heading">
            <div><strong>基础信息</strong><span>请填写并核对该记录的业务信息</span></div>
            <span>{{ config.fields.length }} 个字段</span>
          </div>
          <div class="form-grid">
            <label v-for="[name, label] in config.fields" :key="name" :class="{ 'field-wide': textareaFields.includes(name) }">
              <span class="field-label">{{ label }}</span>
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
          </div>
        </form>

        <footer class="drawer-footer">
          <template v-if="panelMode === 'view'">
            <button class="danger-ghost" type="button" @click="requestDelete([editingId])"><Trash2 :size="16" />删除</button>
            <button class="button-secondary" type="button" @click="duplicate(form)"><Copy :size="16" />复制</button>
            <button type="button" @click="panelMode = 'edit'"><Pencil :size="16" />编辑记录</button>
          </template>
          <template v-else>
            <button class="button-secondary" type="button" @click="closePanel">取消</button>
            <button type="button" :disabled="saving" @click="save">{{ saving ? "保存中..." : panelMode === "duplicate" ? "创建副本" : editingId ? "保存修改" : "创建记录" }}</button>
          </template>
        </footer>
      </aside>
    </div>

    <div v-if="confirmState.open" class="confirm-layer" role="alertdialog" aria-modal="true">
      <section class="confirm-dialog">
        <div class="confirm-icon"><Trash2 :size="21" /></div>
        <h3>确认删除记录？</h3>
        <p>将删除 {{ confirmState.ids.length }} 条记录，此操作无法撤销。</p>
        <div>
          <button class="button-secondary" type="button" :disabled="deleting" @click="confirmState.open = false">取消</button>
          <button class="danger-button" type="button" :disabled="deleting" @click="executeDelete">{{ deleting ? "删除中..." : "确认删除" }}</button>
        </div>
      </section>
    </div>
  </Teleport>
</template>
