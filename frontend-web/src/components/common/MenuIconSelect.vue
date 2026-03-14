<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import MenuSvgIcon from '@/components/common/MenuSvgIcon.vue'
import { menuIconOptions } from '@/components/common/menu-icon-registry'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    disabled?: boolean
    size?: number
  }>(),
  {
    modelValue: '',
    disabled: false,
    size: 18
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const dropdownOpen = ref(false)
const rootRef = ref<HTMLElement | null>(null)
const triggerRef = ref<HTMLButtonElement | null>(null)

const selectedOption = computed(() => menuIconOptions.find((item) => item.key === props.modelValue))

const closeDropdown = () => {
  dropdownOpen.value = false
}

const toggleDropdown = () => {
  if (props.disabled) {
    return
  }
  dropdownOpen.value = !dropdownOpen.value
}

const selectIcon = (iconKey: string) => {
  emit('update:modelValue', iconKey)
  closeDropdown()
  triggerRef.value?.blur()
}

const handlePointerDownOutside = (event: PointerEvent) => {
  const target = event.target as Node | null
  if (!dropdownOpen.value || !target || !rootRef.value) {
    return
  }
  if (!rootRef.value.contains(target)) {
    closeDropdown()
  }
}

onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('pointerdown', handlePointerDownOutside)
  }
})

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('pointerdown', handlePointerDownOutside)
  }
})
</script>

<template>
  <div ref="rootRef" class="menu-icon-select">
    <button
      ref="triggerRef"
      type="button"
      class="menu-icon-select__trigger"
      :class="{ 'form-control-placeholder': !modelValue, 'is-open': dropdownOpen, 'is-disabled': disabled }"
      :title="selectedOption?.label || '请选择图标'"
      @click.stop="toggleDropdown"
    >
      <span class="menu-icon-select__preview">
        <MenuSvgIcon :icon-key="modelValue" :size="size" />
      </span>
      <span class="menu-icon-select__arrow" :class="{ 'is-open': dropdownOpen }"></span>
    </button>

    <div v-if="dropdownOpen" class="menu-icon-select__panel">
      <button
        type="button"
        class="menu-icon-select__option"
        :class="{ 'is-active': !modelValue }"
        title="清空图标"
        @mousedown.prevent.stop="selectIcon('')"
      >
        <MenuSvgIcon icon-key="" :size="size" />
      </button>
      <button
        v-for="item in menuIconOptions"
        :key="item.key"
        type="button"
        class="menu-icon-select__option"
        :class="{ 'is-active': modelValue === item.key }"
        :title="item.label"
        @mousedown.prevent.stop="selectIcon(item.key)"
      >
        <MenuSvgIcon :icon-key="item.key" :size="size" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.menu-icon-select {
  position: relative;
}

.menu-icon-select__trigger {
  display: flex;
  width: 100%;
  height: 36px;
  padding: 0 12px;
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.menu-icon-select__trigger.is-open {
  border-color: var(--es-color-primary);
}

.menu-icon-select__trigger.is-disabled {
  color: var(--es-color-text-tertiary);
  background: var(--es-color-fill-muted);
  cursor: not-allowed;
}

.menu-icon-select__preview {
  display: inline-flex;
  width: 18px;
  height: 18px;
  align-items: center;
  justify-content: center;
}

.menu-icon-select__arrow {
  width: 8px;
  height: 8px;
  border-right: 1.5px solid currentcolor;
  border-bottom: 1.5px solid currentcolor;
  transform: rotate(45deg);
  transition: transform 0.2s ease;
}

.menu-icon-select__arrow.is-open {
  transform: rotate(225deg);
}

.menu-icon-select__panel {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 8;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 6px;
  width: 270px;
  max-height: 248px;
  padding: 6px;
  overflow: auto;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
}

.menu-icon-select__option {
  display: inline-flex;
  width: 38px;
  height: 38px;
  padding: 0;
  color: var(--es-color-text-secondary);
  background: transparent;
  border: 0;
  border-radius: 4px;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.menu-icon-select__option:hover,
.menu-icon-select__option.is-active {
  color: var(--es-color-primary);
  background: var(--es-color-fill-muted);
}

.form-control-placeholder {
  color: var(--es-color-text-placeholder);
}
</style>
