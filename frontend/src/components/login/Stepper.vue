<script setup>
import { AnimatePresence, Motion } from "motion-v";
import { Check, ChevronLeft, ChevronRight } from "lucide-vue-next";
import { Comment, Fragment, computed, ref, useSlots } from "vue";

const props = defineProps({
  initialStep: { type: Number, default: 1 },
  beforeNext: { type: Function, default: () => true },
  backButtonText: { type: String, default: "上一步" },
  nextButtonText: { type: String, default: "下一步" },
  completeButtonText: { type: String, default: "登录系统" },
  disabled: { type: Boolean, default: false },
  disableStepIndicators: { type: Boolean, default: true }
});

const emit = defineEmits(["step-change", "final-step-completed"]);
const slots = useSlots();
const currentStep = ref(props.initialStep);
const direction = ref(0);
const navigating = ref(false);

function flattenSteps(nodes) {
  return nodes.flatMap(node => {
    if (node.type === Comment) return [];
    if (node.type === Fragment && Array.isArray(node.children)) return flattenSteps(node.children);
    return [node];
  });
}

const stepsArray = computed(() => flattenSteps(slots.default?.() || []));
const totalSteps = computed(() => stepsArray.value.length);
const isLastStep = computed(() => currentStep.value === totalSteps.value);

function getStepStatus(step) {
  if (step < currentStep.value) return "complete";
  if (step === currentStep.value) return "active";
  return "inactive";
}

function updateStep(step) {
  const nextStep = Math.min(Math.max(step, 1), totalSteps.value || 1);
  if (nextStep === currentStep.value) return;
  direction.value = nextStep > currentStep.value ? 1 : -1;
  currentStep.value = nextStep;
  emit("step-change", nextStep);
}

async function next() {
  if (props.disabled || navigating.value) return;
  navigating.value = true;
  try {
    const canContinue = await props.beforeNext(currentStep.value);
    if (!canContinue) return;
    if (isLastStep.value) {
      emit("final-step-completed");
      return;
    }
    updateStep(currentStep.value + 1);
  } finally {
    navigating.value = false;
  }
}

function back() {
  if (props.disabled || navigating.value || currentStep.value <= 1) return;
  updateStep(currentStep.value - 1);
}

function reset() {
  direction.value = 0;
  currentStep.value = Math.min(Math.max(props.initialStep, 1), totalSteps.value || 1);
}

function handleIndicatorClick(step) {
  if (props.disabled || props.disableStepIndicators) return;
  updateStep(step);
}

defineExpose({ back, next, reset });

const stepVariants = {
  enter: directionValue => ({
    x: directionValue >= 0 ? "20%" : "-20%",
    opacity: 0
  }),
  center: { x: "0%", opacity: 1 },
  exit: directionValue => ({
    x: directionValue >= 0 ? "-14%" : "14%",
    opacity: 0
  })
};
</script>

<template>
  <div class="login-stepper">
    <div class="login-stepper__track" aria-label="登录步骤">
      <template v-for="(_, index) in stepsArray" :key="index">
        <button
          class="login-stepper__indicator"
          :data-status="getStepStatus(index + 1)"
          :data-test="`step-indicator-${index + 1}`"
          :aria-current="currentStep === index + 1 ? 'step' : undefined"
          :aria-label="`第 ${index + 1} 步`"
          :disabled="disabled || disableStepIndicators"
          type="button"
          @click="handleIndicatorClick(index + 1)"
        >
          <Motion
            as="span"
            class="login-stepper__indicator-content"
            :initial="false"
            :animate="{ scale: getStepStatus(index + 1) === 'active' ? 1.08 : 1 }"
            :transition="{ duration: 0.25 }"
          >
            <Check v-if="getStepStatus(index + 1) === 'complete'" :size="17" :stroke-width="2.5" />
            <span v-else-if="getStepStatus(index + 1) === 'active'" class="login-stepper__active-dot"></span>
            <span v-else>{{ index + 1 }}</span>
          </Motion>
        </button>

        <div v-if="index < totalSteps - 1" class="login-stepper__connector" aria-hidden="true">
          <Motion
            as="span"
            class="login-stepper__connector-fill"
            :initial="false"
            :animate="{ scaleX: currentStep > index + 1 ? 1 : 0 }"
            :transition="{ duration: 0.35, ease: 'easeOut' }"
          />
        </div>
      </template>
    </div>

    <Motion as="div" class="login-stepper__content" layout>
      <AnimatePresence :initial="false" mode="wait" :custom="direction">
        <Motion
          :key="currentStep"
          as="div"
          :custom="direction"
          :variants="stepVariants"
          initial="enter"
          animate="center"
          exit="exit"
          :transition="{ duration: 0.28, ease: 'easeOut' }"
        >
          <component :is="stepsArray[currentStep - 1]" />
        </Motion>
      </AnimatePresence>
    </Motion>

    <div class="login-stepper__footer">
      <button
        v-if="currentStep > 1"
        class="login-stepper__back"
        data-test="stepper-back"
        type="button"
        :disabled="disabled || navigating"
        @click="back"
      >
        <ChevronLeft :size="16" />
        <span>{{ backButtonText }}</span>
      </button>

      <button
        class="login-stepper__next"
        data-test="stepper-next"
        type="button"
        :disabled="disabled || navigating"
        @click="next"
      >
        <span>{{ isLastStep ? completeButtonText : nextButtonText }}</span>
        <ChevronRight v-if="!isLastStep" :size="16" />
        <i v-else aria-hidden="true"></i>
      </button>
    </div>
  </div>
</template>
