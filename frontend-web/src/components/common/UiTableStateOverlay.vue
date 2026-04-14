<script setup lang="ts">
import { computed } from 'vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'

const props = withDefaults(
  defineProps<{
    loading?: boolean
    empty?: boolean
    top?: number | string
    emptyText?: string
    size?: number
    thickness?: number
  }>(),
  {
    loading: false,
    empty: false,
    top: 42,
    emptyText: '暂无数据',
    size: 18,
    thickness: 2
  }
)

const visible = computed(() => props.loading || props.empty)

const overlayStyle = computed(() => {
  const top = typeof props.top === 'number' ? `${props.top}px` : props.top

  return { top }
})
</script>

<template>
  <div
    v-if="visible"
    class="ui-table-state-overlay"
    :class="{ 'is-loading': loading, 'is-empty': !loading && empty }"
    :style="overlayStyle"
  >
    <UiLoadingState
      v-if="loading"
      :size="size"
      :thickness="thickness"
      :min-height="0"
    />
    <UiEmptyState
      v-else
      :text="emptyText"
      :min-height="0"
    />
  </div>
</template>

<style scoped>
.ui-table-state-overlay {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 6;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.ui-table-state-overlay.is-loading {
  background: rgb(255 255 255 / 82%);
  border-radius: 0 0 5px 5px;
}
</style>
