<script setup lang="ts">
import { booleanSelectOptions, meterSwitchStatusOptions, onlineStatusOptions } from '@/components/devices/electric-meter.mock'
import type { ElectricMeterQueryFormState } from '@/modules/devices/electric-meters/composables/useElectricMeterQuery'

interface Props {
  queryForm: ElectricMeterQueryFormState
}

defineProps<Props>()

const emit = defineEmits<{
  search: []
  reset: []
}>()
</script>

<template>
  <header class="workspace-search">
    <div class="search-row">
      <label class="search-item">
        <span class="search-label-inline">电表名称/编号</span>
        <input
          v-model="queryForm.searchKey"
          class="search-input"
          type="text"
          placeholder="请输入电表名称/编号"
        />
      </label>

      <label class="search-item search-item-secondary">
        <span class="search-label-inline">在线状态</span>
        <select
          v-model="queryForm.onlineStatus"
          class="search-input search-input-select"
          :class="{ 'search-input-placeholder': !queryForm.onlineStatus }"
        >
          <option value="">请选择</option>
          <option v-for="item in onlineStatusOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </label>

      <label class="search-item search-item-secondary">
        <span class="search-label-inline">表计状态</span>
        <select
          v-model="queryForm.status"
          class="search-input search-input-select"
          :class="{ 'search-input-placeholder': !queryForm.status }"
        >
          <option value="">请选择</option>
          <option v-for="item in meterSwitchStatusOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </label>

      <label class="search-item search-item-secondary">
        <span class="search-label-inline">是否预付费</span>
        <select
          v-model="queryForm.payType"
          class="search-input search-input-select"
          :class="{ 'search-input-placeholder': !queryForm.payType }"
        >
          <option value="">请选择</option>
          <option v-for="item in booleanSelectOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </label>

      <div class="search-actions">
        <button class="btn btn-primary" type="button" @click="emit('search')">查询</button>
        <button class="btn btn-secondary" type="button" @click="emit('reset')">重置</button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.workspace-search {
  padding: 16px;
  border-bottom: 1px solid var(--es-color-border);
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.search-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.search-item-secondary {
  margin-left: 10px;
}

.search-label-inline {
  flex-shrink: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.search-input {
  width: 180px;
  height: 36px;
  max-width: 180px;
  min-width: 180px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.search-input-select {
  width: 120px;
  max-width: 120px;
  min-width: 120px;
}

.search-input::placeholder {
  color: var(--es-color-text-placeholder);
}

.search-input-placeholder {
  color: var(--es-color-text-placeholder);
}

.search-input:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-shrink: 0;
}

.btn {
  display: inline-flex;
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-primary:hover {
  background: var(--es-color-primary-hover);
  border-color: var(--es-color-primary-hover);
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #f8fbff;
  border-color: var(--es-color-border-strong);
}

.btn-secondary:hover {
  color: var(--es-color-primary);
  background: #eff6ff;
  border-color: #93c5fd;
}

@media (width <= 1200px) {
  .search-item,
  .search-input {
    width: 100%;
    max-width: none;
  }

  .search-item {
    justify-content: space-between;
  }

  .search-item-secondary {
    margin-left: 0;
  }

  .search-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }
}
</style>
