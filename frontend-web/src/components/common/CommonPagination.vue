<script setup lang="ts">
import { computed, ref, watch } from 'vue'

type PageToken = number | 'ellipsis-left' | 'ellipsis-right'

interface PageChangePayload {
  pageNum: number
  pageSize: number
}

const props = withDefaults(
  defineProps<{
    total: number
    pageNum: number
    pageSize: number
    loading?: boolean
    pageSizeOptions?: number[]
    showPageSize?: boolean
    showJumper?: boolean
  }>(),
  {
    loading: false,
    pageSizeOptions: () => [10, 20, 50, 100],
    showPageSize: true,
    showJumper: true
  }
)

const emit = defineEmits<{
  (e: 'change', payload: PageChangePayload): void
}>()

const totalPages = computed(() => {
  const size = props.pageSize > 0 ? props.pageSize : 10
  const pages = Math.ceil((props.total || 0) / size)
  return pages > 0 ? pages : 1
})

const toValidPageNum = (value: number) => {
  if (!Number.isFinite(value)) {
    return 1
  }
  return Math.min(Math.max(Math.trunc(value), 1), totalPages.value)
}

const emitChange = (pageNum: number, pageSize: number) => {
  const nextPageNum = toValidPageNum(pageNum)
  const nextPageSize = pageSize > 0 ? pageSize : props.pageSize
  if (nextPageNum === props.pageNum && nextPageSize === props.pageSize) {
    return
  }
  emit('change', {
    pageNum: nextPageNum,
    pageSize: nextPageSize
  })
}

const pageTokens = computed<PageToken[]>(() => {
  const total = totalPages.value
  const current = toValidPageNum(props.pageNum)

  if (total <= 9) {
    return Array.from({ length: total }, (_item, index) => index + 1)
  }

  const tokens: PageToken[] = [1]

  if (current <= 4) {
    for (let i = 2; i <= 6; i += 1) {
      tokens.push(i)
    }
    tokens.push('ellipsis-right', total)
    return tokens
  }

  if (current >= total - 3) {
    tokens.push('ellipsis-left')
    for (let i = total - 5; i <= total - 1; i += 1) {
      tokens.push(i)
    }
    tokens.push(total)
    return tokens
  }

  tokens.push('ellipsis-left')
  for (let i = current - 2; i <= current + 2; i += 1) {
    tokens.push(i)
  }
  tokens.push('ellipsis-right', total)
  return tokens
})

const jumpPageInput = ref(String(props.pageNum || 1))

watch(
  () => props.pageNum,
  (value) => {
    jumpPageInput.value = String(toValidPageNum(value || 1))
  },
  { immediate: true }
)

const onSelectPageSize = (event: Event) => {
  const target = event.target as HTMLSelectElement
  const nextPageSize = Number(target.value)
  if (!Number.isFinite(nextPageSize) || nextPageSize <= 0) {
    return
  }
  emitChange(1, nextPageSize)
}

const goPrev = () => {
  if (props.loading) {
    return
  }
  emitChange(props.pageNum - 1, props.pageSize)
}

const goNext = () => {
  if (props.loading) {
    return
  }
  emitChange(props.pageNum + 1, props.pageSize)
}

const goPage = (pageNum: number) => {
  if (props.loading) {
    return
  }
  emitChange(pageNum, props.pageSize)
}

const onJump = () => {
  if (props.loading) {
    return
  }
  const target = Number(jumpPageInput.value.trim())
  if (!Number.isFinite(target)) {
    jumpPageInput.value = String(toValidPageNum(props.pageNum || 1))
    return
  }
  emitChange(target, props.pageSize)
}
</script>

<template>
  <div class="es-pagination">
    <span class="es-pagination-total">共 {{ total }} 条</span>
    <div class="es-pagination-main">
      <label v-if="showPageSize" class="es-pagination-size">
        <select
          class="es-pagination-size-select"
          :disabled="loading"
          :value="pageSize"
          @change="onSelectPageSize"
        >
          <option v-for="item in pageSizeOptions" :key="item" :value="item">{{ item }}条/页</option>
        </select>
      </label>

      <button
        type="button"
        class="es-pagination-btn es-pagination-nav"
        :disabled="loading || pageNum <= 1"
        @click="goPrev"
      >
        ‹
      </button>

      <template v-for="(token, tokenIndex) in pageTokens" :key="`${token}-${tokenIndex}`">
        <span v-if="typeof token !== 'number'" class="es-pagination-ellipsis"> ... </span>
        <button
          v-else
          type="button"
          class="es-pagination-btn"
          :class="{ 'is-active': token === pageNum }"
          :disabled="loading || token === pageNum"
          @click="goPage(token)"
        >
          {{ token }}
        </button>
      </template>

      <button
        type="button"
        class="es-pagination-btn es-pagination-nav"
        :disabled="loading || pageNum >= totalPages"
        @click="goNext"
      >
        ›
      </button>

      <label v-if="showJumper" class="es-pagination-jumper">
        <span>前往</span>
        <input
          v-model="jumpPageInput"
          class="es-pagination-jump-input"
          :disabled="loading"
          inputmode="numeric"
          @keydown.enter.prevent="onJump"
          @blur="onJump"
        />
        <span>页</span>
      </label>
    </div>
  </div>
</template>

<style scoped>
.es-pagination {
  display: flex;
  width: 100%;
  min-width: 0;
  margin-top: 12px;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.es-pagination-total {
  font-size: var(--es-font-size-sm);
  line-height: 1;
  color: var(--es-color-text-secondary);
}

.es-pagination-main {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.es-pagination-size-select,
.es-pagination-jump-input {
  height: 30px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.es-pagination-size-select {
  width: 96px;
  padding: 0 8px;
}

.es-pagination-btn {
  height: 30px;
  min-width: 30px;
  font-size: var(--es-font-size-sm);
  line-height: 1;
  color: var(--es-color-text-secondary);
  cursor: pointer;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.es-pagination-btn:hover:not(:disabled) {
  color: var(--es-color-primary);
  border-color: #93c5fd;
}

.es-pagination-btn.is-active {
  color: #fff;
  background: var(--es-color-primary);
  border-color: var(--es-color-primary);
}

.es-pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.es-pagination-nav {
  font-size: 16px;
}

.es-pagination-ellipsis {
  display: inline-flex;
  min-width: 18px;
  font-size: var(--es-font-size-sm);
  line-height: 1;
  color: var(--es-color-text-secondary);
  align-items: center;
  justify-content: center;
}

.es-pagination-jumper {
  display: inline-flex;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  align-items: center;
  gap: 6px;
}

.es-pagination-jump-input {
  width: 52px;
  padding: 0 8px;
  text-align: center;
}

@media (width <= 1200px) {
  .es-pagination {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}
</style>
