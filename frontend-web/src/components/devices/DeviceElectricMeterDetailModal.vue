<script setup lang="ts">
import { computed } from 'vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'

interface DetailCell {
  label: string
  value: string
  type?: 'online'
}

const props = defineProps<{
  modelValue: boolean
  meter: ElectricMeterItem | null
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const getSpaceRegion = (spacePath: string | undefined, spaceName: string | undefined) => {
  const segments = (spacePath || '')
    .split(' / ')
    .map((item) => item.trim())
    .filter(Boolean)

  if (segments.length <= 1) {
    return '--'
  }

  if (spaceName && segments[segments.length - 1] === spaceName) {
    return segments.slice(0, -1).join(' > ') || '--'
  }

  return segments.slice(0, -1).join(' > ') || '--'
}

const getGatewayDisplay = (meter: ElectricMeterItem) => {
  if (meter.gatewayName && meter.gatewayDeviceNo) {
    return `${meter.gatewayName} - ${meter.gatewayDeviceNo}`
  }
  if (meter.gatewayName) {
    return meter.gatewayName
  }
  return '--'
}

const getOnlineStatusText = (meter: ElectricMeterItem) => {
  if (meter.onlineStatus === 1) {
    return '在线'
  }
  if (meter.onlineStatus === 0) {
    return '离线'
  }
  return '--'
}

const getOnlineStatusClass = (meter: ElectricMeterItem) => {
  if (meter.onlineStatus === 1) {
    return 'detail-online-status detail-online-status-online'
  }
  if (meter.onlineStatus === 0) {
    return 'detail-online-status detail-online-status-offline'
  }
  return 'detail-online-status detail-online-status-unknown'
}

const buildTableRows = (cells: DetailCell[]) => {
  const rows: Array<[DetailCell, DetailCell | null]> = []

  for (let index = 0; index < cells.length; index += 2) {
    rows.push([cells[index], cells[index + 1] ?? null])
  }

  return rows
}

const baseRows = computed(() => {
  const meter = props.meter
  if (!meter) {
    return []
  }

  return buildTableRows([
    { label: '电表名称', value: meter.meterName || '--' },
    { label: '电表编号', value: meter.deviceNo || '--' },
    { label: '所在位置', value: meter.spaceName || '--' },
    { label: '所属区域', value: getSpaceRegion(meter.spacePath, meter.spaceName) },
    { label: '电表型号', value: meter.modelName || '--' },
    { label: '通讯模式', value: meter.communicateModel || '--' },
    { label: '电表地址码', value: meter.meterAddress || '--' },
    { label: '串口号', value: meter.portNo || '--' },
    { label: '接入网关', value: getGatewayDisplay(meter) },
    { label: 'IMEI', value: meter.imei || '--' },
    { label: '是否预付费', value: meter.payType === 1 ? '是' : '否' },
    { label: '是否计量', value: meter.isCalculate ? '是' : '否' },
    { label: '在线状态', value: getOnlineStatusText(meter), type: 'online' as const },
    { label: '离线时长', value: meter.onlineStatus === 0 ? meter.offlineDuration || '--' : '--' },
    { label: '表计状态', value: meter.statusName || '--' },
    { label: '是否保电', value: meter.protectedModel ? '是' : '否' },
    { label: 'CT变比', value: meter.ct || '--' }
  ])
})

const billingRows = computed(() => {
  const meter = props.meter
  if (!meter) {
    return []
  }

  return buildTableRows([
    { label: '计费方案名称', value: meter.pricePlanName || '--' },
    { label: '预警方案名称', value: meter.warnPlanName || '--' },
    { label: '电费预警级别', value: meter.electricWarnTypeName || '--' }
  ])
})

const latestRows = computed(() => {
  const meter = props.meter
  if (!meter) {
    return []
  }

  return buildTableRows([
    { label: '总电量', value: meter.latestReportPower || '--' },
    { label: '上报时间', value: meter.latestReportTime || '--' },
    { label: '尖电量', value: meter.latestReportHigherPower || '--' },
    { label: '峰电量', value: meter.latestReportHighPower || '--' },
    { label: '平电量', value: meter.latestReportLowPower || '--' },
    { label: '谷电量', value: meter.latestReportLowerPower || '--' },
    { label: '深谷电量', value: meter.latestReportDeepLowPower || '--' }
  ])
})

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="meter-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel meter-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">电表详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div v-if="loading" class="modal-body modal-body-loading">
          <UiLoadingState :size="20" :thickness="2" :min-height="180" />
        </div>
        <div v-else class="modal-body">
          <h4 class="section-title es-detail-section-title">基本信息</h4>
          <div class="summary-grid base-grid">
            <div
              v-for="[leftCell, rightCell] in baseRows"
              :key="leftCell.label"
              class="summary-row"
            >
              <div class="summary-item">
                <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                <span class="summary-value es-detail-value-box">
                  <span
                    v-if="leftCell.type === 'online'"
                    :class="getOnlineStatusClass(props.meter as ElectricMeterItem)"
                  >
                    {{ leftCell.value }}
                  </span>
                  <span v-else class="summary-text">{{ leftCell.value }}</span>
                </span>
              </div>
              <div v-if="rightCell" class="summary-item">
                <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                <span class="summary-value es-detail-value-box">
                  <span
                    v-if="rightCell.type === 'online'"
                    :class="getOnlineStatusClass(props.meter as ElectricMeterItem)"
                  >
                    {{ rightCell.value }}
                  </span>
                  <span v-else class="summary-text">{{ rightCell.value }}</span>
                </span>
              </div>
            </div>
          </div>

          <h4 class="section-title es-detail-section-title">最近上报</h4>
          <div class="summary-grid section-grid">
            <div
              v-for="[leftCell, rightCell] in latestRows"
              :key="leftCell.label"
              class="summary-row"
            >
              <div class="summary-item">
                <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                <span class="summary-value es-detail-value-box">
                  <span class="summary-text">{{ leftCell.value }}</span>
                </span>
              </div>
              <div v-if="rightCell" class="summary-item">
                <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                <span class="summary-value es-detail-value-box">
                  <span class="summary-text">{{ rightCell.value }}</span>
                </span>
              </div>
            </div>
          </div>

          <h4 class="section-title es-detail-section-title">计费信息</h4>
          <div class="summary-grid latest-grid">
            <div
              v-for="[leftCell, rightCell] in billingRows"
              :key="leftCell.label"
              class="summary-row"
            >
              <div class="summary-item">
                <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                <span class="summary-value es-detail-value-box">
                  <span class="summary-text">{{ leftCell.value }}</span>
                </span>
              </div>
              <div v-if="rightCell" class="summary-item">
                <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                <span class="summary-value es-detail-value-box">
                  <span class="summary-text">{{ rightCell.value }}</span>
                </span>
              </div>
            </div>
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

.meter-detail-modal {
  display: flex;
  width: min(840px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  flex-direction: column;
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
  min-height: 0;
  padding: 20px 20px 32px;
  overflow: auto;
  flex: 1;
}

.modal-body-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
}

.summary-label {
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
  white-space: nowrap;
}

.summary-text {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  word-break: break-all;
}

.section-title {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.summary-grid {
  display: grid;
  gap: 10px;
}

.section-grid {
  margin-bottom: 28px;
}

.base-grid {
  margin-bottom: 28px;
}

.latest-grid {
  margin-bottom: 8px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 56px;
}

.summary-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.summary-value {
  width: 100%;
}

.detail-online-status {
  font-size: var(--es-font-size-sm);
  font-weight: 600;
}

.detail-online-status-online {
  color: var(--es-color-success-text);
}

.detail-online-status-offline {
  color: var(--es-color-error-text);
}

.detail-online-status-unknown {
  color: var(--es-color-text-placeholder);
}

.meter-detail-fade-enter-active,
.meter-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.meter-detail-fade-enter-from,
.meter-detail-fade-leave-to {
  opacity: 0;
}

.meter-detail-fade-enter-from .meter-detail-modal,
.meter-detail-fade-leave-to .meter-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
