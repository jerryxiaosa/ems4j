<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    text?: string
    minHeight?: number | string
    inline?: boolean
  }>(),
  {
    text: '暂无数据',
    minHeight: 56,
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
    class="ui-empty-state"
    :class="{ 'ui-empty-state-inline': inline }"
    :style="containerStyle"
  >
    {{ text }}
  </component>
</template>

<style scoped>
.ui-empty-state {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  color: var(--es-color-text-placeholder);
  font-size: var(--es-font-size-sm);
  line-height: 1.5;
  text-align: center;
}

.ui-empty-state-inline {
  display: inline-flex;
  width: auto;
  min-height: 0;
  vertical-align: middle;
}
</style>
