<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const todoRoutePaths = ['/reports/electric-bill']
const showTodoOnly = computed(() => todoRoutePaths.includes(route.path))
</script>

<template>
  <div class="card">
    <h2>{{ route.meta.title }}</h2>
    <template v-if="showTodoOnly">
      <p>TODO</p>
    </template>
    <template v-else>
      <p>该模块已进入 V1 范围</p>
      <p>下一步将接入对应 API 适配层，并按统一样式迁移业务组件。</p>
    </template>
  </div>
</template>

<style scoped>
.card {
  padding: 20px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: var(--es-radius-lg);
  box-shadow: var(--es-shadow-card);
}

h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1e3a8a;
}

p {
  margin: 0 0 6px;
  font-size: var(--es-font-size-md);
  color: var(--es-color-text-secondary);
}
</style>
