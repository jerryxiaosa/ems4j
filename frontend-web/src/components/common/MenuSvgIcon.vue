<script setup lang="ts">
import { computed } from 'vue'
import { menuIconMap } from '@/components/common/menu-icon-registry'

const props = withDefaults(
  defineProps<{
    iconKey?: string
    size?: number
  }>(),
  {
    iconKey: '',
    size: 16
  }
)

const iconPaths = computed(() => {
  return menuIconMap[props.iconKey || '']?.paths || []
})
</script>

<template>
  <span v-if="iconPaths.length" class="menu-svg-icon" :style="{ width: `${size}px`, height: `${size}px` }" aria-hidden="true">
    <svg viewBox="0 0 16 16" focusable="false">
      <path v-for="(pathD, index) in iconPaths" :key="`${iconKey}-${index}`" :d="pathD" />
    </svg>
  </span>
  <span v-else class="menu-svg-icon menu-svg-icon-empty" :style="{ width: `${size}px`, height: `${size}px` }" aria-hidden="true"></span>
</template>

<style scoped>
.menu-svg-icon {
  display: inline-flex;
  color: currentcolor;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.menu-svg-icon svg {
  width: 100%;
  height: 100%;
  overflow: visible;
  fill: none;
  stroke: currentcolor;
  stroke-width: 1.2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.menu-svg-icon-empty {
  border: 1px dashed var(--es-color-border);
  border-radius: 4px;
}
</style>
