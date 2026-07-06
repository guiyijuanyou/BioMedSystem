<script setup>
import { nextTick, ref } from "vue";
import { Bot, Send, X } from "lucide-vue-next";
import { api } from "@/api";

const open = ref(false);
const question = ref("");
const busy = ref(false);
const messages = ref([
  { type: "ai", text: "你好，我是系统 AI 助手。可以帮你查询样本数据、说明模块用途和操作流程。" }
]);
const messageBox = ref();

async function ask(text = question.value) {
  const value = text.trim();
  if (!value || busy.value) return;
  messages.value.push({ type: "user", text: value });
  question.value = "";
  busy.value = true;
  try {
    const result = await api("/api/assistant/chat", { method: "POST", body: JSON.stringify({ question: value }) });
    messages.value.push({ type: "ai", text: result.answer || "暂时没有找到合适的回答。" });
  } catch {
    messages.value.push({ type: "ai", text: "助手接口暂时不可用，请确认后端服务已经启动。" });
  } finally {
    busy.value = false;
    await nextTick();
    messageBox.value.scrollTop = messageBox.value.scrollHeight;
  }
}
</script>

<template>
  <section class="assistant-widget" :class="{ open }">
    <button v-if="!open" class="assistant-fab" type="button" aria-label="打开 AI 助手" @click="open = true"><Bot :size="23" /></button>
    <div v-else class="assistant-popover">
      <div class="assistant-header">
        <div><strong>AI 助手</strong><small>系统使用与数据查询</small></div>
        <button class="assistant-close" type="button" @click="open = false"><X :size="18" /></button>
      </div>
      <div class="assistant-suggestions">
        <button type="button" @click="ask('当前系统有多少样本数据？')">统计</button>
        <button type="button" @click="ask('系统里有哪些中药材分布？')">药材</button>
        <button type="button" @click="ask('资料上传后怎么查看？')">资料</button>
      </div>
      <div ref="messageBox" class="assistant-messages">
        <div v-for="(item, index) in messages" :key="index" class="assistant-message" :class="item.type">{{ item.text }}</div>
      </div>
      <form class="assistant-form" @submit.prevent="ask()">
        <input v-model="question" autocomplete="off" placeholder="问我系统怎么用">
        <button type="submit" :disabled="busy"><Send :size="16" /></button>
      </form>
    </div>
  </section>
</template>
