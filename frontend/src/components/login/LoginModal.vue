<script setup>
import { ChevronDown, Eye, EyeOff, X } from "lucide-vue-next";
import { nextTick, onBeforeUnmount, ref, watch } from "vue";
import Stepper from "./Stepper.vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  credentials: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  errorText: { type: String, default: "" }
});

const emit = defineEmits(["update:modelValue", "submit", "closed"]);
const stepper = ref(null);
const usernameInput = ref(null);
const passwordInput = ref(null);
const showPassword = ref(false);
const showDemo = ref(false);
const currentStep = ref(1);
const validationText = ref("");
const localCredentials = ref({ ...props.credentials });

watch(() => props.credentials, value => {
  localCredentials.value = { ...value };
}, { deep: true });

watch(() => props.modelValue, async open => {
  document.body.classList.toggle("login-modal-open", open);
  if (open) {
    localCredentials.value = { ...props.credentials };
    currentStep.value = 1;
    validationText.value = "";
    showDemo.value = false;
    showPassword.value = false;
    await nextTick();
    stepper.value?.reset();
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

function clearValidation() {
  validationText.value = "";
}

function validateStep(step) {
  validationText.value = "";
  if (step === 1 && !localCredentials.value.username.trim()) {
    validationText.value = "请输入系统账号";
    return false;
  }
  if (step === 2 && !localCredentials.value.password) {
    validationText.value = "请输入登录密码";
    return false;
  }
  return true;
}

async function handleStepChange(step) {
  currentStep.value = step;
  validationText.value = "";
  await nextTick();
  if (step === 1) usernameInput.value?.focus({ preventScroll: true });
  if (step === 2) passwordInput.value?.focus({ preventScroll: true });
}

function chooseDemo(username) {
  localCredentials.value = { username, password: "123456" };
  validationText.value = "";
}

function submit() {
  if (props.loading || !validateStep(1) || !validateStep(2)) return;
  emit("submit", { ...localCredentials.value });
}

function handleFormSubmit() {
  if (props.loading) return;
  if (currentStep.value < 3) {
    stepper.value?.next();
    return;
  }
  submit();
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

          <form @submit.prevent="handleFormSubmit">
            <Stepper
              ref="stepper"
              :before-next="validateStep"
              :disabled="loading"
              :complete-button-text="loading ? '正在验证数据权限...' : '登录系统'"
              back-button-text="上一步"
              next-button-text="下一步"
              @step-change="handleStepChange"
              @final-step-completed="submit"
            >
              <section class="login-step" aria-labelledby="login-step-account-title">
                <h3 id="login-step-account-title" class="login-step__title">输入系统账号</h3>
                <p class="login-step__copy">使用分配给你的数据门户账号继续。</p>
                <label class="login-field">
                  <span>账号</span>
                  <input
                    ref="usernameInput"
                    v-model="localCredentials.username"
                    data-test="username-input"
                    autocomplete="username"
                    placeholder="请输入系统账号"
                    @input="clearValidation"
                  >
                </label>
                <p v-if="validationText" class="login-step__validation" aria-live="polite">{{ validationText }}</p>
              </section>

              <section class="login-step" aria-labelledby="login-step-password-title">
                <h3 id="login-step-password-title" class="login-step__title">验证访问密码</h3>
                <p class="login-step__copy">密码仅用于本次身份验证，不会在页面中保存。</p>
                <label class="login-field">
                  <span>密码</span>
                  <span class="login-field__password">
                    <input
                      ref="passwordInput"
                      v-model="localCredentials.password"
                      data-test="password-input"
                      :type="showPassword ? 'text' : 'password'"
                      autocomplete="current-password"
                      placeholder="请输入登录密码"
                      @input="clearValidation"
                    >
                    <button
                      data-test="password-visibility"
                      type="button"
                      :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                      @click="showPassword = !showPassword"
                    >
                      <EyeOff v-if="showPassword" :size="18" /><Eye v-else :size="18" />
                    </button>
                  </span>
                </label>
                <p v-if="validationText" class="login-step__validation" aria-live="polite">{{ validationText }}</p>

                <button data-test="demo-toggle" class="login-dialog__demo-toggle" type="button" :aria-expanded="showDemo" @click="showDemo = !showDemo">
                  查看演示账号 <ChevronDown :size="16" :class="{ 'is-open': showDemo }" />
                </button>
                <div v-if="showDemo" class="login-dialog__demo">
                  <button data-test="demo-admin" type="button" @click="chooseDemo('admin')">admin</button>
                  <button data-test="demo-teacher" type="button" @click="chooseDemo('teacher')">teacher</button>
                  <button data-test="demo-researcher" type="button" @click="chooseDemo('researcher')">researcher</button>
                  <button data-test="demo-student" type="button" @click="chooseDemo('student')">student</button>
                  <span>统一密码：123456</span>
                </div>
              </section>

              <section class="login-step" aria-labelledby="login-step-confirm-title">
                <h3 id="login-step-confirm-title" class="login-step__title">确认登录信息</h3>
                <p class="login-step__copy">确认账号无误后进入生物医药数据空间。</p>
                <div class="login-confirmation">
                  <div class="login-confirmation__row">
                    <span>登录账号</span>
                    <strong>{{ localCredentials.username }}</strong>
                  </div>
                  <div class="login-confirmation__row">
                    <span>访问凭据</span>
                    <strong>密码已填写</strong>
                  </div>
                  <p class="login-confirmation__note">系统将按现有角色权限加载对应工作台与数据范围。</p>
                </div>
                <p v-if="errorText" class="login-dialog__error" aria-live="polite">{{ errorText }}</p>
              </section>
            </Stepper>
          </form>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>
