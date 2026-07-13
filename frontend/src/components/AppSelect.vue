<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useId, watch } from "vue";
import { Check, ChevronDown } from "lucide-vue-next";

const props = defineProps({
  modelValue: { type: [String, Number, Boolean], default: "" },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: "请选择" },
  disabled: { type: Boolean, default: false },
  required: { type: Boolean, default: false },
  name: { type: String, default: "" },
  ariaLabel: { type: String, default: "" },
  modelModifiers: { type: Object, default: () => ({}) }
});

const emit = defineEmits(["update:modelValue", "change"]);
const root = ref(null);
const trigger = ref(null);
const menu = ref(null);
const open = ref(false);
const opensUp = ref(false);
const activeIndex = ref(-1);
const invalid = ref(false);
const menuStyle = ref({});
const listboxId = `app-select-${useId()}`;

function sameValue(left, right) {
  return Object.is(left, right) || (left != null && right != null && String(left) === String(right));
}

const selectedIndex = computed(() => props.options.findIndex(option => sameValue(option.value, props.modelValue)));
const selectedOption = computed(() => props.options[selectedIndex.value] || null);
const displayLabel = computed(() => selectedOption.value?.label || props.placeholder);

function enabledIndex(start, direction) {
  if (!props.options.length) return -1;
  let index = start;
  for (let count = 0; count < props.options.length; count += 1) {
    index = (index + direction + props.options.length) % props.options.length;
    if (!props.options[index]?.disabled) return index;
  }
  return -1;
}

function updateMenuPosition() {
  if (!open.value || !trigger.value) return;
  const rect = trigger.value.getBoundingClientRect();
  const gap = 8;
  const viewportPadding = 12;
  const width = Math.min(Math.max(rect.width, 220), window.innerWidth - viewportPadding * 2);
  const left = Math.min(Math.max(rect.left, viewportPadding), window.innerWidth - width - viewportPadding);
  const below = window.innerHeight - rect.bottom - gap - viewportPadding;
  const above = rect.top - gap - viewportPadding;
  opensUp.value = below < 220 && above > below;
  const available = Math.max(120, opensUp.value ? above : below);

  menuStyle.value = {
    left: `${left}px`,
    width: `${width}px`,
    maxHeight: `${Math.min(320, available)}px`,
    ...(opensUp.value
      ? { bottom: `${window.innerHeight - rect.top + gap}px`, top: "auto" }
      : { top: `${rect.bottom + gap}px`, bottom: "auto" })
  };
}

async function openMenu() {
  if (props.disabled || open.value) return;
  open.value = true;
  activeIndex.value = selectedIndex.value >= 0 && !props.options[selectedIndex.value]?.disabled
    ? selectedIndex.value
    : enabledIndex(-1, 1);
  await nextTick();
  updateMenuPosition();
}

function closeMenu({ focus = false } = {}) {
  if (!open.value) return;
  open.value = false;
  if (focus) nextTick(() => trigger.value?.focus());
}

function toggleMenu() {
  if (open.value) closeMenu();
  else openMenu();
}

function choose(option) {
  if (!option || option.disabled) return;
  const value = props.modelModifiers.number ? Number(option.value) : option.value;
  invalid.value = false;
  emit("update:modelValue", value);
  emit("change", { target: { value } });
  closeMenu({ focus: true });
}

function onKeydown(event) {
  if (props.disabled) return;
  if (event.key === "Escape") {
    if (open.value) {
      event.preventDefault();
      closeMenu({ focus: true });
    }
    return;
  }
  if (["ArrowDown", "ArrowUp", "Home", "End", "Enter", " "].includes(event.key)) {
    event.preventDefault();
  } else {
    return;
  }
  if (!open.value) {
    openMenu();
    return;
  }
  if (event.key === "ArrowDown") activeIndex.value = enabledIndex(activeIndex.value, 1);
  if (event.key === "ArrowUp") activeIndex.value = enabledIndex(activeIndex.value, -1);
  if (event.key === "Home") activeIndex.value = enabledIndex(-1, 1);
  if (event.key === "End") activeIndex.value = enabledIndex(0, -1);
  if (["Enter", " "].includes(event.key)) choose(props.options[activeIndex.value]);
}

function onDocumentPointerDown(event) {
  if (!open.value) return;
  if (root.value?.contains(event.target) || menu.value?.contains(event.target)) return;
  closeMenu();
}

function onInvalid(event) {
  event.preventDefault();
  invalid.value = true;
  trigger.value?.focus();
}

watch(() => props.modelValue, () => invalid.value = false);
watch(() => props.disabled, value => value && closeMenu());

onMounted(() => {
  document.addEventListener("pointerdown", onDocumentPointerDown);
  window.addEventListener("resize", updateMenuPosition);
  window.addEventListener("scroll", updateMenuPosition, true);
});

onBeforeUnmount(() => {
  document.removeEventListener("pointerdown", onDocumentPointerDown);
  window.removeEventListener("resize", updateMenuPosition);
  window.removeEventListener("scroll", updateMenuPosition, true);
});
</script>

<template>
  <div ref="root" class="app-select" :class="{ open, disabled, invalid }">
    <select
      class="app-select__native"
      tabindex="-1"
      aria-hidden="true"
      :name="name || undefined"
      :required="required"
      :disabled="disabled"
      :value="modelValue"
      @invalid="onInvalid"
    >
      <option v-if="placeholder" value="">{{ placeholder }}</option>
      <option v-for="option in options" :key="`${option.value}-${option.label}`" :value="option.value" :disabled="option.disabled">
        {{ option.label }}
      </option>
    </select>

    <button
      ref="trigger"
      class="app-select__trigger"
      type="button"
      role="combobox"
      :aria-label="ariaLabel || undefined"
      :aria-controls="listboxId"
      :aria-expanded="open"
      :aria-required="required"
      :aria-invalid="invalid"
      :disabled="disabled"
      :title="displayLabel"
      @click="toggleMenu"
      @keydown="onKeydown"
    >
      <span :class="{ placeholder: !selectedOption }">{{ displayLabel }}</span>
      <ChevronDown :size="16" aria-hidden="true" />
    </button>

    <Teleport to="body">
      <Transition name="app-select-menu">
        <div
          v-if="open"
          :id="listboxId"
          ref="menu"
          class="app-select__menu"
          :class="{ 'opens-up': opensUp }"
          :style="menuStyle"
          role="listbox"
          @keydown="onKeydown"
        >
          <button
            v-for="(option, index) in options"
            :id="`${listboxId}-option-${index}`"
            :key="`${option.value}-${option.label}`"
            class="app-select__option"
            :class="{ active: index === activeIndex, selected: sameValue(option.value, modelValue) }"
            type="button"
            role="option"
            :aria-selected="sameValue(option.value, modelValue)"
            :disabled="option.disabled"
            :title="option.label"
            @mouseenter="!option.disabled && (activeIndex = index)"
            @click="choose(option)"
          >
            <span>{{ option.label }}</span>
            <Check v-if="sameValue(option.value, modelValue)" :size="16" aria-hidden="true" />
          </button>
          <p v-if="!options.length" class="app-select__empty">暂无可选项</p>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.app-select { position: relative; width: 100%; min-width: 0; }
.app-select__native { position: absolute; left: 1px; bottom: 1px; width: 1px; height: 1px; margin: 0; padding: 0; opacity: 0; pointer-events: none; }
.app-select__trigger { display: grid; grid-template-columns: minmax(0, 1fr) 16px; gap: 10px; align-items: center; width: 100%; min-height: 40px; padding: 0 12px; color: var(--ink, #0a2540); background: rgba(255, 255, 255, .96); border: 1px solid var(--line, #d8e0ea); border-radius: 12px; box-shadow: 0 1px 2px rgba(10, 37, 64, .04); font: inherit; font-size: 13px; font-weight: 550; text-align: left; cursor: pointer; transition: border-color 160ms cubic-bezier(.16, 1, .3, 1), box-shadow 160ms cubic-bezier(.16, 1, .3, 1), background-color 160ms cubic-bezier(.16, 1, .3, 1); }
.app-select__trigger span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.app-select__trigger .placeholder { color: var(--muted, #697386); font-weight: 500; }
.app-select__trigger svg { color: var(--muted, #697386); transition: transform 180ms cubic-bezier(.16, 1, .3, 1), color 160ms cubic-bezier(.16, 1, .3, 1); }
.app-select.open .app-select__trigger, .app-select__trigger:focus-visible { border-color: rgba(99, 91, 255, .48); outline: 0; box-shadow: 0 0 0 3px rgba(99, 91, 255, .12); }
.app-select.open .app-select__trigger svg { color: var(--brand, #635bff); transform: rotate(180deg); }
.app-select.invalid .app-select__trigger { border-color: #d45b41; box-shadow: 0 0 0 3px rgba(212, 91, 65, .12); }
.app-select.disabled .app-select__trigger { color: #8b95a1; background: #f5f7fa; cursor: not-allowed; opacity: .72; }
.app-select__menu { position: fixed; z-index: 1400; display: grid; gap: 3px; overflow-y: auto; padding: 7px; color: var(--ink, #0a2540); background: rgba(255, 255, 255, .98); border: 1px solid var(--line, #d8e0ea); border-radius: 12px; box-shadow: 0 18px 48px rgba(50, 50, 93, .18), 0 4px 12px rgba(10, 37, 64, .08); backdrop-filter: blur(18px) saturate(130%); transform-origin: top center; }
.app-select__menu.opens-up { transform-origin: bottom center; }
.app-select__option { display: grid; grid-template-columns: minmax(0, 1fr) 18px; gap: 10px; align-items: center; width: 100%; min-height: 38px; padding: 8px 10px; color: var(--ink, #0a2540); background: transparent; border: 0; border-radius: 8px; box-shadow: none; font: inherit; font-size: 13px; font-weight: 520; text-align: left; cursor: pointer; }
.app-select__option span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.app-select__option.active { color: var(--brand, #635bff); background: var(--brand-soft, #f0efff); }
.app-select__option.selected { color: var(--brand, #635bff); font-weight: 650; }
.app-select__option svg { justify-self: end; }
.app-select__option:disabled { color: #9aa4af; cursor: not-allowed; opacity: .62; }
.app-select__empty { margin: 0; padding: 14px 10px; color: var(--muted, #697386); font-size: 12px; text-align: center; }
.app-select-menu-enter-active { transition: opacity 180ms cubic-bezier(.16, 1, .3, 1), transform 180ms cubic-bezier(.16, 1, .3, 1); }
.app-select-menu-leave-active { transition: opacity 120ms cubic-bezier(.16, 1, .3, 1), transform 120ms cubic-bezier(.16, 1, .3, 1); }
.app-select-menu-enter-from, .app-select-menu-leave-to { opacity: 0; transform: translateY(-6px) scale(.98); }
.app-select__menu.opens-up.app-select-menu-enter-from, .app-select__menu.opens-up.app-select-menu-leave-to { transform: translateY(6px) scale(.98); }
@media (hover: hover) and (pointer: fine) { .app-select__trigger:hover:not(:disabled) { border-color: rgba(99, 91, 255, .3); background: #fff; } .app-select__option:hover:not(:disabled) { color: var(--brand, #635bff); background: var(--brand-soft, #f0efff); } }
@media (prefers-reduced-motion: reduce) { .app-select__trigger, .app-select__trigger svg, .app-select-menu-enter-active, .app-select-menu-leave-active { transition-duration: .01ms; } .app-select-menu-enter-from, .app-select-menu-leave-to, .app-select__menu.opens-up.app-select-menu-enter-from, .app-select__menu.opens-up.app-select-menu-leave-to { transform: none; } }
</style>
