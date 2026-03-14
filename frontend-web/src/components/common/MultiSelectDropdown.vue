<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import type { SystemOption } from '@/types/system'

const props = withDefaults(
  defineProps<{
    modelValue: string[]
    options: SystemOption[]
    placeholder?: string
    disabled?: boolean
    wrapText?: boolean
    maxTextLines?: number
  }>(),
  {
    placeholder: '请选择',
    disabled: false,
    wrapText: false,
    maxTextLines: 2
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

const rootRef = ref<HTMLElement | null>(null)
const dropdownVisible = ref(false)

const selectedLabelText = computed(() => {
  const labels = props.options
    .filter((item) => props.modelValue.includes(item.value))
    .map((item) => item.label)

  return labels.length > 0 ? labels.join('、') : ''
})

const toggleDropdown = () => {
  if (props.disabled) {
    return
  }
  dropdownVisible.value = !dropdownVisible.value
}

const handleOptionToggle = (value: string, checked: boolean) => {
  const current = new Set(props.modelValue)
  if (checked) {
    current.add(value)
  } else {
    current.delete(value)
  }
  emit('update:modelValue', Array.from(current))
}

const handleDocumentMouseDown = (event: MouseEvent) => {
  if (!dropdownVisible.value) {
    return
  }
  const target = event.target as Node | null
  if (rootRef.value && target && !rootRef.value.contains(target)) {
    dropdownVisible.value = false
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentMouseDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentMouseDown)
})
</script>

<template>
  <div ref="rootRef" class="multi-select-dropdown">
    <button
      type="button"
      class="multi-select-control"
      :class="{ 'is-placeholder': !selectedLabelText, 'is-open': dropdownVisible, 'is-wrap': wrapText }"
      :disabled="disabled"
      @click="toggleDropdown"
    >
      <span
        class="multi-select-text"
        :class="{ 'is-wrap': wrapText }"
        :style="wrapText ? { WebkitLineClamp: String(maxTextLines) } : undefined"
      >
        {{ selectedLabelText || placeholder }}
      </span>
      <span class="multi-select-arrow">▾</span>
    </button>

    <div v-if="dropdownVisible" class="multi-select-panel">
      <label v-for="item in options" :key="item.value" class="multi-select-option">
        <input
          type="checkbox"
          :checked="modelValue.includes(item.value)"
          @change="handleOptionToggle(item.value, ($event.target as HTMLInputElement).checked)"
        />
        <span>{{ item.label }}</span>
      </label>
      <div v-if="options.length === 0" class="multi-select-empty">暂无可选项</div>
    </div>
  </div>
</template>

<style scoped>
.multi-select-dropdown {
  position: relative;
  width: 100%;
}

.multi-select-control {
  display: flex;
  width: 100%;
  min-height: 34px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.multi-select-control.is-wrap {
  padding: 7px 10px;
  align-items: flex-start;
}

.multi-select-control.is-open {
  border-color: var(--es-color-primary);
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.multi-select-control.is-placeholder {
  color: var(--es-color-text-placeholder);
}

.multi-select-text {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.multi-select-text.is-wrap {
  display: -webkit-box;
  overflow: hidden;
  line-height: 18px;
  text-overflow: initial;
  white-space: normal;
  word-break: break-all;
  -webkit-box-orient: vertical;
}

.multi-select-arrow {
  flex: 0 0 auto;
  color: var(--es-color-text-placeholder);
}

.multi-select-panel {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 12;
  display: grid;
  width: 100%;
  max-height: 240px;
  overflow: auto;
  padding: 6px 0;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: 0 10px 24px rgb(15 23 42 / 12%);
}

.multi-select-option {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  padding: 8px 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
}

.multi-select-option:hover {
  background: #f5f9ff;
}

.multi-select-option input {
  width: 14px;
  height: 14px;
}

.multi-select-empty {
  padding: 10px 12px;
  font-size: 12px;
  color: var(--es-color-text-placeholder);
}
</style>
