<script setup>
import { reactive, ref } from "vue";
import { createTraceEvent } from "../../services/api";
import { loadSettings } from "../../services/settings";

const settings = loadSettings();
const submitting = ref(false);

const form = reactive({
  batchId: "",
  eventType: "harvest",
  description: "",
  operator: "",
  location: "",
  attachments: ""
});

const eventTypes = [
  { value: "planting", label: "种植" },
  { value: "growing", label: "生长" },
  { value: "harvest", label: "采收" },
  { value: "processing", label: "加工" },
  { value: "storage", label: "仓储" },
  { value: "transport", label: "运输" },
  { value: "quality_check", label: "质检" },
  { value: "other", label: "其他" }
];

async function submitTrace() {
  if (!settings.accountToken) {
    uni.showToast({ title: "请先登录账号", icon: "none" });
    return;
  }
  if (!form.batchId.trim()) {
    uni.showToast({ title: "请填写批次编号", icon: "none" });
    return;
  }
  if (!form.description.trim()) {
    uni.showToast({ title: "请填写事件描述", icon: "none" });
    return;
  }

  submitting.value = true;
  try {
    await createTraceEvent({
      batchId: form.batchId.trim(),
      eventType: form.eventType,
      description: form.description.trim(),
      operator: form.operator.trim() || undefined,
      location: form.location.trim() || undefined,
      attachments: form.attachments.trim() || undefined
    });
    uni.showToast({ title: "溯源事件已记录", icon: "success" });
    form.batchId = "";
    form.eventType = "harvest";
    form.description = "";
    form.operator = "";
    form.location = "";
    form.attachments = "";
  } catch (error) {
    uni.showToast({ title: error.message, icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">全程可追溯</text>
      <text class="title">溯源事件</text>
      <text class="subtitle">记录中药材从种植到流通各环节的关键事件，构建完整的溯源链条。</text>
    </view>

    <view class="card">
      <text class="section-title">记录新事件</text>

      <view class="field">
        <text class="label">批次编号</text>
        <input v-model="form.batchId" class="input" placeholder="输入批次 ID 或编号" />
      </view>

      <view class="field">
        <text class="label">事件类型</text>
        <picker mode="selector" :range="eventTypes.map(e => e.label)" :value="eventTypes.findIndex(e => e.value === form.eventType)" @change="e => { const idx = Number(e.detail.value); form.eventType = eventTypes[idx]?.value || 'harvest'; }">
          <view class="select">{{ eventTypes.find(e => e.value === form.eventType)?.label || "请选择" }}</view>
        </picker>
      </view>

      <view class="field">
        <text class="label">事件描述</text>
        <textarea v-model="form.description" class="textarea" placeholder="详细描述该环节发生的关键操作或异常情况" />
      </view>

      <view class="field">
        <text class="label">操作人</text>
        <input v-model="form.operator" class="input" placeholder="填写操作人姓名（可选）" />
      </view>

      <view class="field">
        <text class="label">发生地点</text>
        <input v-model="form.location" class="input" placeholder="填写地点（可选）" />
      </view>

      <view class="field">
        <text class="label">附件说明</text>
        <input v-model="form.attachments" class="input" placeholder="照片编号或文件名称（可选）" />
      </view>

      <view class="button-row">
        <button class="primary" :loading="submitting" @tap="submitTrace">提交事件</button>
      </view>
    </view>
  </view>
</template>
