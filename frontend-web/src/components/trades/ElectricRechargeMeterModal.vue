<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import type { RechargeMeterItem } from '@/components/trades/electric-recharge.mock'

const props = defineProps<{
  modelValue: boolean
  meters: RechargeMeterItem[]
  selectedMeterId?: number | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [meter: RechargeMeterItem]
}>()

const queryForm = reactive({
  meterName: ''
})
const currentSelectedId = reactive({
  value: 0
})

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      queryForm.meterName = ''
      currentSelectedId.value = props.selectedMeterId || 0
    }
  },
  { immediate: true }
)

const filteredMeters = computed(() => {
  const keyword = queryForm.meterName.trim()

  return props.meters.filter((item) => {
    if (!keyword) {
      return true
    }
    return item.meterName.includes(keyword) || item.deviceNo.includes(keyword)
  })
})

const getSerialNumber = (index: number) => index + 1

const close = () => emit('update:modelValue', false)

const resetQuery = () => {
  queryForm.meterName = ''
}

const confirm = () => {
  const selected = filteredMeters.value.find((item) => item.id === currentSelectedId.value)
  if (!selected) {
    return
  }
  emit('confirm', selected)
  close()
}
</script>

<template>
  <Transition name="recharge-meter-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel">
        <div class="modal-head">
          <h3 class="modal-title">更换电表</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="search-row">
            <label class="search-item">
              <span class="search-label-inline">电表名称/编号</span>
              <input
                v-model="queryForm.meterName"
                class="search-input"
                type="text"
                placeholder="请输入电表名称或编号"
              />
            </label>
            <div class="search-actions">
              <button class="btn btn-primary" type="button">查询</button>
              <button class="btn btn-secondary" type="button" @click="resetQuery">重置</button>
            </div>
          </div>

          <div class="table-wrap es-detail-table-wrap">
            <table class="table es-detail-table">
              <colgroup>
                <col class="col-radio" />
                <col class="col-index" />
                <col class="col-meter-name" />
                <col class="col-meter-no" />
                <col class="col-location" />
                <col class="col-region" />
                <col class="col-balance" />
              </colgroup>
              <thead>
                <tr>
                  <th class="radio-col"></th>
                  <th class="table-col-index col-index">序号</th>
                  <th>电表名称</th>
                  <th>电表号</th>
                  <th class="col-location">所在位置</th>
                  <th class="col-region">所属区域</th>
                  <th>电表余额（元）</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!filteredMeters.length">
                  <td colspan="7" class="empty-row es-detail-empty-row">
                    <UiEmptyState :min-height="56" />
                  </td>
                </tr>
                <tr v-for="(item, index) in filteredMeters" :key="item.id">
                  <td class="radio-col">
                    <input
                      :checked="currentSelectedId.value === item.id"
                      type="radio"
                      name="recharge-meter"
                      @change="currentSelectedId.value = item.id"
                    />
                  </td>
                  <td class="col-index">{{ getSerialNumber(index) }}</td>
                  <td>{{ item.meterName }}</td>
                  <td>{{ item.deviceNo }}</td>
                  <td class="col-location">{{ item.roomNo }}</td>
                  <td class="col-region">{{ item.regionName }}</td>
                  <td>{{ item.balanceAmountText }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="close">取消</button>
          <button
            class="btn btn-primary"
            type="button"
            :disabled="!currentSelectedId.value"
            @click="confirm"
            >确定</button
          >
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 42;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
}

.modal-panel {
  width: min(980px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
}

.modal-head {
  display: flex;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  margin: 0;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.icon-btn {
  height: 36px;
  min-width: 64px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.modal-body {
  padding: 20px;
  overflow: auto;
}

.search-row {
  display: grid;
  grid-template-columns: minmax(0, 320px) 1fr;
  gap: 16px;
  align-items: end;
  margin-bottom: 16px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-label-inline {
  flex: 0 0 auto;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.search-input {
  width: 100%;
  height: 32px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn {
  height: 32px;
  padding: 0 16px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

.table-wrap {
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 12px 10px;
  font-size: var(--es-font-size-sm);
  text-align: left;
  border-bottom: 1px solid var(--es-color-border);
}

.radio-col {
  width: 48px;
}

.col-index {
  width: 52px;
}

.col-location {
  width: 140px;
}

.col-region {
  width: 240px;
}

.col-meter-name {
  width: 130px;
}

.col-meter-no {
  width: 150px;
}

.col-balance {
  width: 140px;
}

.table th.col-index,
.table td.col-index {
  padding-right: 8px;
  padding-left: 8px;
}

.table th.col-location,
.table td.col-location {
  padding-right: 8px;
  padding-left: 6px;
}

.modal-actions {
  display: flex;
  padding: 16px 20px 20px;
  background: #fff;
  border-top: 1px solid var(--es-color-border);
  justify-content: flex-end;
  gap: 12px;
}

.recharge-meter-fade-enter-active,
.recharge-meter-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.recharge-meter-fade-enter-from,
.recharge-meter-fade-leave-to {
  opacity: 0;
}

.recharge-meter-fade-enter-from .modal-panel,
.recharge-meter-fade-leave-to .modal-panel {
  transform: translateY(10px) scale(0.98);
}
</style>
