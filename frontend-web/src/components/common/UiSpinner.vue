<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    size?: number
    thickness?: number
    color?: string
  }>(),
  {
    size: 14,
    thickness: 2,
    color: 'currentColor'
  }
)

const spinnerStyle = computed(() => {
  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    borderWidth: `${props.thickness}px`,
    color: props.color
  }
})
</script>

<template>
  <span class="ui-spinner" :style="spinnerStyle" aria-hidden="true"></span>
</template>

<style scoped>
.ui-spinner {
  display: inline-block;
  border-color: currentcolor;
  border-right-color: transparent;
  border-style: solid;
  border-radius: 999px;
  animation: ui-spinner-rotate 0.65s linear infinite;
  flex-shrink: 0;
}

@keyframes ui-spinner-rotate {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .ui-spinner {
    animation-duration: 1.2s;
  }
}
</style>
