<script setup lang="ts">
import { computed } from 'vue'
import UiSpinner from '@/components/common/UiSpinner.vue'

const props = withDefaults(
  defineProps<{
    size?: number
    thickness?: number
    color?: string
    minHeight?: number | string
    inline?: boolean
  }>(),
  {
    size: 18,
    thickness: 2,
    color: 'currentColor',
    minHeight: 72,
    inline: false
  }
)

const containerStyle = computed(() => {
  if (props.inline) {
    return undefined
  }

  const minHeight =
    typeof props.minHeight === 'number' ? `${props.minHeight}px` : props.minHeight || undefined

  return minHeight
    ? {
        minHeight
      }
    : undefined
})
</script>

<template>
  <component
    :is="inline ? 'span' : 'div'"
    class="ui-loading-state"
    :class="{ 'ui-loading-state-inline': inline }"
    :style="containerStyle"
  >
    <UiSpinner :size="size" :thickness="thickness" :color="color" />
  </component>
</template>

<style scoped>
.ui-loading-state {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  color: var(--es-color-text-placeholder);
  line-height: 1;
  text-align: center;
}

.ui-loading-state-inline {
  display: inline-flex;
  width: auto;
  min-height: 0;
  vertical-align: middle;
}
</style>
