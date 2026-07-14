<script setup>
import { Eye, EyeOff, X } from "lucide-vue-next";
import { nextTick, onBeforeUnmount, ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  credentials: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  errorText: { type: String, default: "" }
});

const emit = defineEmits(["update:modelValue", "submit", "closed"]);
const usernameInput = ref(null);
const showPassword = ref(false);
const localCredentials = ref({ ...props.credentials });

watch(() => props.credentials, value => {
  localCredentials.value = { ...value };
}, { deep: true });

watch(() => props.modelValue, async open => {
  document.body.classList.toggle("login-modal-open", open);
  if (open) {
    await nextTick();
    usernameInput.value?.focus({ preventScroll: true });
  }
});

function close() {
  if (props.loading) return;
  emit("update:modelValue", false);
  emit("closed");
}

function handleKeydown(event) {
  if (event.key === "Escape") close();
}

function submit() {
  emit("submit", { ...localCredentials.value });
}

onBeforeUnmount(() => document.body.classList.remove("login-modal-open"));
</script>

<template>
  <Teleport to="body">
    <Transition name="login-dialog">
      <div v-if="modelValue" class="login-dialog" role="presentation" @keydown="handleKeydown">
        <button class="login-dialog__backdrop" type="button" aria-label="关闭登录面板" @click="close"></button>
        <section class="login-dialog__panel" role="dialog" aria-modal="true" aria-labelledby="login-dialog-title">
          <div class="login-dialog__orb" aria-hidden="true"><span></span><span></span><span></span></div>
          <button class="login-dialog__close" type="button" aria-label="关闭" @click="close"><X :size="19" /></button>
          <header>
            <span class="login-dialog__eyebrow">SECURE DATA PORTAL</span>
            <h2 id="login-dialog-title">进入生物医药数据空间</h2>
            <p>连接采集、研究、教学与质量溯源的统一工作台。</p>
          </header>

          <form @submit.prevent="submit">
            <label class="login-field">
              <span>账号</span>
              <input ref="usernameInput" v-model="localCredentials.username" placeholder="请输入系统账号">
            </label>
            <label class="login-field">
              <span>密码</span>
              <span class="login-field__password">
                <input v-model="localCredentials.password" :type="showPassword ? 'text' : 'password'" placeholder="请输入登录密码">
                <button type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" :size="18" /><Eye v-else :size="18" />
                </button>
              </span>
            </label>

            <p v-if="errorText" class="login-dialog__error" aria-live="polite">{{ errorText }}</p>

            <button class="login-dialog__submit" type="submit" :disabled="loading">
              <span>{{ loading ? "正在验证数据权限..." : "登录系统" }}</span><i aria-hidden="true"></i>
            </button>

          </form>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>
