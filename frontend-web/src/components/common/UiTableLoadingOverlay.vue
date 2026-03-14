<script setup lang="ts">
import { computed } from 'vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'

const props = withDefaults(
  defineProps<{
    top?: number | string
    size?: number
    thickness?: number
  }>(),
  {
    top: 42,
    size: 18,
    thickness: 2
  }
)

const overlayStyle = computed(() => {
  const top = typeof props.top === 'number' ? `${props.top}px` : props.top

  return {
    top
  }
})
</script>

<template>
  <div class="ui-table-loading-overlay" :style="overlayStyle">
    <UiLoadingState :size="size" :thickness="thickness" :min-height="0" />
  </div>
</template>

<style scoped>
.ui-table-loading-overlay {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 6;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(255 255 255 / 82%);
  border-radius: 0 0 5px 5px;
}
</style>
