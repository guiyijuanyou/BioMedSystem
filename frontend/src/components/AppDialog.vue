<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { AlertTriangle, CircleHelp, Info, ShieldAlert, X } from "lucide-vue-next";
import { appDialog } from "@/services/dialog";

const state = appDialog.state;
const input = ref(null);
const validationError = ref("");
let previouslyFocused = null;

const icon = computed(() => {
  if (state.tone === "danger") return ShieldAlert;
  if (state.tone === "warning") return AlertTriangle;
  if (state.type === "alert") return Info;
  return CircleHelp;
});

function accept() {
  if (state.type === "prompt" && state.required && !String(state.value ?? "").trim()) {
    validationError.value = "请填写此项后再继续";
    input.value?.focus();
    return;
  }
  validationError.value = "";
  appDialog.accept();
}

function onKeydown(event) {
  if (!state.open) return;
  if (event.key === "Escape") {
    event.preventDefault();
    appDialog.cancel();
  }
  if (event.key === "Enter" && !event.isComposing && (!state.multiline || event.ctrlKey || event.metaKey)) {
    event.preventDefault();
    accept();
  }
}

watch(() => state.open, async (open) => {
  if (open) {
    validationError.value = "";
    previouslyFocused = document.activeElement;
    await nextTick();
    input.value?.focus();
    input.value?.select?.();
  } else {
    previouslyFocused?.focus?.();
    previouslyFocused = null;
  }
});

onMounted(() => document.addEventListener("keydown", onKeydown));
onBeforeUnmount(() => document.removeEventListener("keydown", onKeydown));
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog-fade">
      <div v-if="state.open" class="dialog-layer" @mousedown.self="appDialog.cancel">
        <section
          class="app-dialog"
          :class="[`tone-${state.tone}`, `type-${state.type}`]"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="'app-dialog-title'"
        >
          <header class="dialog-head">
            <span class="dialog-icon"><component :is="icon" :size="21" /></span>
            <div>
              <span class="dialog-kicker">BioMed Cloud</span>
              <h2 id="app-dialog-title">{{ state.title }}</h2>
            </div>
            <button class="dialog-close" type="button" aria-label="关闭" @click="appDialog.cancel">
              <X :size="18" />
            </button>
          </header>

          <div class="dialog-body">
            <p v-if="state.message" class="dialog-message">{{ state.message }}</p>
            <label v-if="state.type === 'prompt'" class="dialog-field">
              <span>{{ state.label || "请输入内容" }}<b v-if="state.required">必填</b></span>
              <textarea
                v-if="state.multiline"
                ref="input"
                v-model="state.value"
                rows="4"
                :placeholder="state.placeholder"
                @input="validationError = ''"
              ></textarea>
              <input
                v-else
                ref="input"
                v-model="state.value"
                :type="state.inputType"
                :placeholder="state.placeholder"
                @input="validationError = ''"
              >
              <small v-if="validationError" class="dialog-error">{{ validationError }}</small>
            </label>
          </div>

          <footer class="dialog-actions">
            <button
              v-if="state.type !== 'alert' && state.cancelText"
              class="dialog-button secondary"
              type="button"
              @click="appDialog.cancel"
            >{{ state.cancelText }}</button>
            <button class="dialog-button primary" type="button" @click="accept">{{ state.confirmText }}</button>
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dialog-layer {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(16, 35, 27, .32);
  backdrop-filter: blur(8px) saturate(.9);
}

.app-dialog {
  width: min(480px, 100%);
  overflow: hidden;
  border: 1px solid rgba(197, 214, 204, .9);
  border-radius: 22px;
  background: rgba(255, 255, 255, .98);
  box-shadow: 0 28px 80px rgba(18, 49, 37, .24), 0 4px 16px rgba(18, 49, 37, .1);
}

.dialog-head {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 13px;
  padding: 22px 22px 16px;
}

.dialog-icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 13px;
  color: var(--brand, #167052);
  background: var(--brand-soft, #e7f3ed);
}

.tone-warning .dialog-icon { color: #98631c; background: #fff4d9; }
.tone-danger .dialog-icon { color: #b23b3b; background: #fae9e9; }

.dialog-kicker {
  display: block;
  margin-bottom: 2px;
  color: #728078;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: .13em;
  text-transform: uppercase;
}

.dialog-head h2 {
  margin: 0;
  color: var(--ink, #17231c);
  font-size: 20px;
  line-height: 1.25;
}

.dialog-close {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  padding: 0;
  border: 0;
  border-radius: 10px;
  color: #738078;
  background: transparent;
}

.dialog-close:hover { color: #24372e; background: #f0f4f2; transform: none; }
.dialog-body { padding: 2px 22px 22px; }

.dialog-message {
  margin: 0;
  color: #56665e;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-line;
}

.dialog-field { display: grid; gap: 9px; }
.dialog-message + .dialog-field { margin-top: 16px; }
.dialog-field > span { color: #33483e; font-size: 13px; font-weight: 700; }
.dialog-field > span b {
  margin-left: 7px;
  padding: 2px 6px;
  border-radius: 999px;
  color: #8b5541;
  background: #faeee9;
  font-size: 9px;
  letter-spacing: .08em;
}

.dialog-field input,
.dialog-field textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #cedbd4;
  border-radius: 12px;
  outline: none;
  color: #1c3027;
  background: #f9fbfa;
  font: inherit;
  font-size: 14px;
  transition: border-color .18s ease, box-shadow .18s ease, background .18s ease;
}

.dialog-field input { height: 46px; padding: 0 14px; }
.dialog-field textarea { min-height: 108px; padding: 12px 14px; resize: vertical; line-height: 1.6; }
.dialog-field input:focus,
.dialog-field textarea:focus {
  border-color: #4b957c;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(22, 112, 82, .1);
}

.dialog-error { color: #b23b3b; font-size: 12px; }
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 22px 20px;
  border-top: 1px solid #edf1ef;
  background: #fafcfb;
}

.dialog-button {
  min-width: 92px;
  height: 42px;
  padding: 0 18px;
  border-radius: 11px;
  font-weight: 700;
  box-shadow: none;
}

.dialog-button.secondary { border: 1px solid #d5dfda; color: #53645b; background: #fff; }
.dialog-button.primary { border: 1px solid #167052; color: #fff; background: #167052; }
.tone-danger .dialog-button.primary { border-color: #b23b3b; background: #b23b3b; }
.dialog-button:hover { transform: translateY(-1px); }

.dialog-fade-enter-active,
.dialog-fade-leave-active { transition: opacity .2s ease; }
.dialog-fade-enter-active .app-dialog,
.dialog-fade-leave-active .app-dialog { transition: transform .22s ease, opacity .2s ease; }
.dialog-fade-enter-from,
.dialog-fade-leave-to { opacity: 0; }
.dialog-fade-enter-from .app-dialog { opacity: 0; transform: translateY(12px) scale(.98); }
.dialog-fade-leave-to .app-dialog { opacity: 0; transform: translateY(6px) scale(.99); }

@media (max-width: 560px) {
  .dialog-layer { align-items: end; padding: 12px; }
  .app-dialog { border-radius: 20px; }
  .dialog-head { padding: 19px 18px 14px; }
  .dialog-body { padding: 2px 18px 18px; }
  .dialog-actions { padding: 13px 18px 18px; }
  .dialog-button { flex: 1; }
}
</style>
