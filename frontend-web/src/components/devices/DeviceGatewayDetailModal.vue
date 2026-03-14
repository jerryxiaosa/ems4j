<script setup lang="ts">
import { computed } from 'vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { GatewayDetailDeviceItem, GatewayItem } from '@/components/devices/gateway.mock'

const props = defineProps<{
  modelValue: boolean
  gateway: GatewayItem | null
  devices: GatewayDetailDeviceItem[]
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const getSerialNumber = (index: number) => index + 1
const detailSectionTitle = computed(() =>
  props.devices.length > 0 ? `接入的设备（共${props.devices.length}个）` : '接入的设备'
)
const getDeviceOnlineStatus = (value: boolean | null | undefined) => {
  if (value === true) {
    return '在线'
  }
  if (value === false) {
    return '离线'
  }
  return '--'
}

const getDeviceOnlineStatusClass = (value: boolean | null | undefined) => {
  if (value === true) {
    return 'detail-online-status detail-online-status-online'
  }
  if (value === false) {
    return 'detail-online-status detail-online-status-offline'
  }
  return 'detail-online-status detail-online-status-unknown'
}

const summaryRows = computed(() => {
  const gateway = props.gateway
  if (!gateway) {
    return []
  }

  const rows = [
    { label: '网关名称', value: gateway.gatewayName || '—' },
    { label: '网关编号', value: gateway.deviceNo || '—' },
    { label: '设备位置', value: gateway.spacePath || gateway.spaceName || '—' },
    { label: '网关型号', value: gateway.modelName || '—' },
    { label: '网关SN', value: gateway.sn || '—' },
    { label: '网关模式', value: gateway.communicateModel || '—' }
  ]

  if (gateway.imei) {
    rows.push({ label: 'IMEI', value: gateway.imei })
  }

  return rows
})

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="gateway-modal-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel gateway-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">网关详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="summary-grid">
            <div v-for="row in summaryRows" :key="row.label" class="summary-item">
              <span class="summary-label es-detail-label">{{ row.label }}</span>
              <span class="summary-value es-detail-value-box">{{ row.value }}</span>
            </div>
          </div>

          <h4 class="section-title es-detail-section-title">{{ detailSectionTitle }}</h4>
          <div class="detail-table-wrap es-detail-table-wrap">
            <table class="table es-detail-table">
              <thead>
                <tr>
                  <th class="table-col-index">序号</th>
                  <th>设备名称</th>
                  <th>设备类型</th>
                  <th>设备编号</th>
                  <th>串口号</th>
                  <th>表地址码</th>
                  <th>是否在线</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading">
                  <td colspan="7" class="empty-row es-detail-empty-row">
                    <UiLoadingState :size="18" :thickness="2" :min-height="56" />
                  </td>
                </tr>
                <tr v-else-if="!devices.length">
                  <td colspan="7" class="empty-row es-detail-empty-row">
                    <UiEmptyState :min-height="56" />
                  </td>
                </tr>
                <tr v-for="(item, index) in devices" :key="item.id">
                  <td>{{ getSerialNumber(index) }}</td>
                  <td>{{ item.deviceName }}</td>
                  <td>{{ item.deviceType }}</td>
                  <td>{{ item.deviceNo }}</td>
                  <td>{{ item.portNo || '--' }}</td>
                  <td>{{ item.meterAddress || '--' }}</td>
                  <td>
                    <span :class="getDeviceOnlineStatusClass(item.isOnline)">
                      {{ getDeviceOnlineStatus(item.isOnline) }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
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

.gateway-detail-modal {
  width: min(960px, calc(100vw - 32px));
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

.icon-btn,
.btn {
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

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 56px;
  margin-bottom: 14px;
}

.summary-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.summary-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.summary-value {
  width: 100%;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  word-break: break-all;
}

.detail-table-wrap {
  margin-bottom: 20px;
  overflow: hidden;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.section-title {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.table th,
.table td {
  padding: 12px 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  border-bottom: 1px solid var(--es-color-border);
}

.table th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.table-col-index {
  width: 84px;
}

.empty-row {
  height: 88px;
  padding: 0;
  color: var(--es-color-text-placeholder);
  text-align: center !important;
  vertical-align: middle;
}

.table-loading-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.detail-online-status {
  font-weight: 600;
}

.detail-online-status-online {
  color: var(--es-color-success-text);
}

.detail-online-status-offline {
  color: var(--es-color-error-text);
}

.detail-online-status-unknown {
  font-weight: 500;
  color: var(--es-color-text-placeholder);
}

.pager-total,
.pager-current {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.pager-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.gateway-modal-fade-enter-active,
.gateway-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.gateway-modal-fade-enter-from,
.gateway-modal-fade-leave-to {
  opacity: 0;
}

.gateway-modal-fade-enter-from .gateway-detail-modal,
.gateway-modal-fade-leave-to .gateway-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
