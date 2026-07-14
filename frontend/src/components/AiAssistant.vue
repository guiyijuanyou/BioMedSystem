<script setup>
import { nextTick, ref } from "vue";
import { Bot, Send, X } from "lucide-vue-next";
import { api } from "@/services/api";

const open = ref(false);
const question = ref("");
const busy = ref(false);
const messages = ref([
  { type: "ai", text: "你好，我是系统 AI 助手。可以帮你查询业务数据、了解模块用途和操作流程。" }
]);
const messageBox = ref();

async function ask(text = question.value) {
  const value = text.trim();
  if (!value || busy.value) return;

  const history = messages.value
    .filter((item, index) => !(index === 0 && item.type === "ai"))
    .slice(-10)
    .map((item) => ({ role: item.type === "ai" ? "assistant" : "user", content: item.text }));

  messages.value.push({ type: "user", text: value });
  question.value = "";
  busy.value = true;
  await scrollToBottom();

  try {
    const result = await api("/api/assistant/chat", {
      method: "POST",
      body: JSON.stringify({ question: value, history })
    });
    messages.value.push({ type: "ai", text: result.answer || "暂时没有生成有效回答。" });
  } catch (error) {
    messages.value.push({ type: "ai", text: error.message || "AI 助手暂时不可用，请稍后重试。" });
  } finally {
    busy.value = false;
    await scrollToBottom();
  }
}

async function scrollToBottom() {
  await nextTick();
  if (messageBox.value) {
    messageBox.value.scrollTop = messageBox.value.scrollHeight;
  }
}
</script>

<template>
  <section class="assistant-widget" :class="{ open }">
    <button v-if="!open" class="assistant-fab" type="button" aria-label="打开 AI 助手" @click="open = true">
      <Bot :size="23" />
    </button>
    <div v-else class="assistant-popover">
      <div class="assistant-header">
        <div><strong>AI 助手</strong><small>系统使用与数据查询</small></div>
        <button class="assistant-close" type="button" aria-label="关闭 AI 助手" @click="open = false">
          <X :size="18" />
        </button>
      </div>
      <div class="assistant-suggestions">
        <button type="button" @click="ask('当前系统有多少样本数据？')">统计</button>
        <button type="button" @click="ask('系统里有哪些中药材分布？')">药材</button>
        <button type="button" @click="ask('资料上传后怎么查看？')">资料</button>
      </div>
      <div ref="messageBox" class="assistant-messages">
        <div v-for="(item, index) in messages" :key="index" class="assistant-message" :class="item.type">
          {{ item.text }}
        </div>
        <div v-if="busy" class="assistant-message ai">正在思考…</div>
      </div>
      <form class="assistant-form" @submit.prevent="ask()">
        <input v-model="question" :disabled="busy" maxlength="2000" autocomplete="off" placeholder="问我系统怎么用…">
        <button type="submit" :disabled="busy || !question.trim()" aria-label="发送">
          <Send :size="16" />
        </button>
      </form>
    </div>
  </section>
</template>
