<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { searchOrganizationOptions, type OrganizationOption } from '@/api/adapters/organization'
import UiLoadingState from '@/components/common/UiLoadingState.vue'

const props = withDefaults(
  defineProps<{
    modelValue: string
    placeholder?: string
    disabled?: boolean
    autoBlurOnSelect?: boolean
    clearOnOpen?: boolean
    searchFn?: (keyword: string) => Promise<OrganizationOption[]>
  }>(),
  {
    placeholder: '请选择机构',
    disabled: false,
    autoBlurOnSelect: false,
    clearOnOpen: false,
    searchFn: searchOrganizationOptions
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  select: [option: OrganizationOption]
}>()

const pickerRef = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)
const dropdownVisible = ref(false)
const loading = ref(false)
const options = ref<OrganizationOption[]>([])

let searchRequestId = 0
let searchTimer: number | null = null

const runSearch = async (keyword: string) => {
  const requestId = ++searchRequestId
  loading.value = true

  try {
    const result = await props.searchFn(keyword)
    if (requestId !== searchRequestId) {
      return
    }
    options.value = result
  } catch {
    if (requestId !== searchRequestId) {
      return
    }
    options.value = []
  } finally {
    if (requestId === searchRequestId) {
      loading.value = false
    }
  }
}

const scheduleSearch = (keyword: string) => {
  if (searchTimer !== null) {
    window.clearTimeout(searchTimer)
  }
  searchTimer = window.setTimeout(() => {
    searchTimer = null
    void runSearch(keyword.trim())
  }, 200)
}

const openDropdown = () => {
  if (props.disabled) {
    return
  }
  if (props.clearOnOpen && props.modelValue) {
    emit('update:modelValue', '')
  }
  dropdownVisible.value = true
  if (!options.value.length && !loading.value) {
    scheduleSearch(props.clearOnOpen ? '' : props.modelValue)
  }
}

const handleInput = (event: Event) => {
  const nextValue = (event.target as HTMLInputElement).value
  emit('update:modelValue', nextValue)
  dropdownVisible.value = true
  scheduleSearch(nextValue)
}

const selectOption = (option: OrganizationOption) => {
  emit('update:modelValue', option.name)
  emit('select', option)
  dropdownVisible.value = false
  if (props.autoBlurOnSelect) {
    inputRef.value?.blur()
  }
}

const handleDocumentMouseDown = (event: MouseEvent) => {
  if (!dropdownVisible.value) {
    return
  }
  const target = event.target as Node | null
  if (pickerRef.value && target && !pickerRef.value.contains(target)) {
    dropdownVisible.value = false
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (!dropdownVisible.value || loading.value) {
      return
    }
    scheduleSearch(value)
  }
)

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentMouseDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentMouseDown)
  if (searchTimer !== null) {
    window.clearTimeout(searchTimer)
  }
})
</script>

<template>
  <div ref="pickerRef" class="organization-picker">
    <div class="picker-control-wrap" :class="{ 'picker-control-wrap-active': dropdownVisible }">
      <input
        ref="inputRef"
        class="form-control"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        autocomplete="off"
        @focus="openDropdown"
        @input="handleInput"
      />
      <button
        type="button"
        class="picker-toggle-btn"
        :disabled="disabled"
        aria-label="切换机构下拉框"
        @click="dropdownVisible ? (dropdownVisible = false) : openDropdown()"
      >
        ▾
      </button>
    </div>

    <div v-if="dropdownVisible" class="picker-dropdown">
      <UiLoadingState
        v-if="loading"
        class="picker-state picker-state-loading"
        :size="14"
        :thickness="2"
        :min-height="44"
      />
      <template v-else>
        <button
          v-for="option in options"
          :key="option.id"
          type="button"
          class="picker-option"
          @mousedown.prevent="selectOption(option)"
        >
          <span>{{ option.name }}</span>
          <small v-if="option.managerName || option.managerPhone">
            {{ option.managerName || '--' }} / {{ option.managerPhone || '--' }}
          </small>
        </button>
        <div v-if="!options.length" class="picker-state">暂无匹配机构</div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.organization-picker {
  position: relative;
  width: 100%;
}

.form-control {
  width: 100%;
  height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-control:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.form-control::placeholder {
  color: var(--es-color-text-placeholder);
}

.picker-control-wrap {
  position: relative;
}

.picker-control-wrap-active .form-control {
  border-color: var(--es-color-primary);
}

.picker-control-wrap .form-control {
  padding-right: 34px;
}

.picker-toggle-btn {
  position: absolute;
  top: 50%;
  right: 6px;
  width: 24px;
  height: 24px;
  color: var(--es-color-text-placeholder);
  background: transparent;
  border: none;
  transform: translateY(-50%);
}

.picker-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 12;
  width: 100%;
  max-height: 240px;
  overflow: auto;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: 0 10px 24px rgb(15 23 42 / 12%);
}

.picker-option {
  display: flex;
  width: 100%;
  padding: 10px 12px;
  text-align: left;
  background: #fff;
  border: none;
  flex-direction: column;
}

.picker-option:hover {
  background: #f5f9ff;
}

.picker-option small,
.picker-state {
  font-size: 12px;
  color: var(--es-color-text-placeholder);
}

.picker-state {
  padding: 12px;
}

.picker-state-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
