<script setup lang="ts">
import { computed } from 'vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiErrorState from '@/components/common/UiErrorState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { OrderDetail } from '@/api/adapters/trade'

interface DetailCell {
  label: string
  value: string
}

const props = defineProps<{
  modelValue: boolean
  detail: OrderDetail | null
  loading?: boolean
  errorText?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

type OrderStatusClass = 'success' | 'failed' | 'pending' | 'closed'

const ORDER_STATUS_TEXT_MAP: Record<string, string> = {
  NOT_PAY: '待支付',
  SUCCESS: '支付成功',
  CLOSED: '已关闭',
  PAY_ERROR: '支付异常',
  REFUND_PROCESSING: '退款处理中',
  FULL_REFUND: '已全额退款',
  REFUND_CLOSED: '退款关闭',
  REFUND_ERROR: '退款异常'
}

const ORDER_STATUS_CLASS_MAP: Record<string, OrderStatusClass> = {
  NOT_PAY: 'pending',
  SUCCESS: 'success',
  CLOSED: 'closed',
  PAY_ERROR: 'failed',
  REFUND_PROCESSING: 'pending',
  FULL_REFUND: 'closed',
  REFUND_CLOSED: 'closed',
  REFUND_ERROR: 'failed'
}

const buildRows = (cells: DetailCell[]) => {
  const rows: Array<[DetailCell, DetailCell | null]> = []
  for (let index = 0; index < cells.length; index += 2) {
    rows.push([cells[index], cells[index + 1] ?? null])
  }
  return rows
}

const statusText = computed(() => {
  const detail = props.detail
  if (!detail) {
    return '--'
  }
  return ORDER_STATUS_TEXT_MAP[detail.orderStatus] || detail.orderStatus || '--'
})

const statusClass = computed(() => {
  const detail = props.detail
  if (!detail) {
    return 'status-closed'
  }
  return `status-${ORDER_STATUS_CLASS_MAP[detail.orderStatus] || 'closed'}`
})

const basicRows = computed(() => {
  const detail = props.detail
  if (!detail) {
    return []
  }
  return buildRows([
    { label: '订单号', value: detail.orderSn },
    { label: '流水号', value: detail.thirdPartySn },
    {
      label: '订单类型',
      value: detail.orderTypeName !== '--' ? detail.orderTypeName : detail.orderType
    },
    { label: '支付状态', value: statusText.value },
    { label: '下单时间', value: detail.orderCreateTime },
    { label: '支付完成时间', value: detail.orderSuccessTime }
  ])
})

const amountRows = computed(() => {
  const detail = props.detail
  if (!detail) {
    return []
  }
  const isOrderType2 = detail.orderType === '2'
  const amountCells: DetailCell[] = [
    { label: '订单金额（元）', value: detail.orderAmount },
    { label: '实付金额（元）', value: detail.userPayAmount },
    { label: '币种', value: detail.currency },
    { label: '支付方式', value: detail.paymentChannelName }
  ]

  if (!isOrderType2) {
    amountCells.splice(
      2,
      0,
      { label: '服务费金额（元）', value: detail.serviceAmount },
      { label: '服务费比例（%）', value: detail.serviceRate }
    )

    amountCells.splice(
      6,
      0,
      { label: '订单前金额（元）', value: detail.beginBalance },
      { label: '订单后金额（元）', value: detail.endBalance }
    )
  }

  return buildRows(amountCells)
})

const objectRows = computed(() => {
  const detail = props.detail
  if (!detail) {
    return []
  }
  const isOrderType2 = detail.orderType === '2'
  const cells: DetailCell[] = [
    { label: '机构名称', value: detail.ownerName },
    { label: '账户ID', value: detail.accountId },
    { label: '缴费人', value: detail.userRealName },
    { label: '联系方式', value: detail.userPhone }
  ]

  if (!isOrderType2) {
    cells.splice(
      2,
      0,
      { label: '电表名称', value: detail.meterName },
      { label: '电表编号', value: detail.deviceNo }
    )
  }

  return buildRows(cells)
})

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="order-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel order-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">订单详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div v-if="loading" class="modal-body modal-body-loading">
          <UiLoadingState :size="20" :thickness="2" :min-height="180" />
        </div>
        <div v-else class="modal-body">
          <UiErrorState v-if="errorText" :text="errorText" :min-height="72" />
          <UiEmptyState v-else-if="!detail" text="暂无详情数据" :min-height="72" />
          <template v-else>
            <h4 class="section-title es-detail-section-title">订单基础</h4>
            <div class="summary-grid section-grid">
              <div
                v-for="[leftCell, rightCell] in basicRows"
                :key="leftCell.label"
                class="summary-row"
              >
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span
                    class="summary-value es-detail-value-box"
                    :class="{ [statusClass]: leftCell.label === '支付状态' }"
                  >
                    {{ leftCell.value }}
                  </span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span
                    class="summary-value es-detail-value-box"
                    :class="{ [statusClass]: rightCell.label === '支付状态' }"
                  >
                    {{ rightCell.value }}
                  </span>
                </div>
              </div>
            </div>

            <h4 class="section-title es-detail-section-title">金额信息</h4>
            <div class="summary-grid section-grid">
              <div
                v-for="[leftCell, rightCell] in amountRows"
                :key="leftCell.label"
                class="summary-row"
              >
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
                </div>
              </div>
            </div>

            <h4 class="section-title es-detail-section-title">对象信息</h4>
            <div class="summary-grid section-grid">
              <div
                v-for="[leftCell, rightCell] in objectRows"
                :key="leftCell.label"
                class="summary-row"
              >
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
                </div>
              </div>
              <div class="summary-row summary-row-full">
                <div class="summary-item summary-item-full">
                  <span class="summary-label es-detail-label">备注</span>
                  <span class="summary-value es-detail-value-box summary-remark">{{
                    detail.remark
                  }}</span>
                </div>
              </div>
            </div>
          </template>
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

.order-detail-modal {
  display: flex;
  width: min(900px, calc(100vw - 32px));
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
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
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
  flex: 1;
  min-height: 0;
  padding: 20px 20px 32px;
  overflow: auto;
}

.modal-body-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-grid {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 56px;
}

.summary-row-full {
  grid-template-columns: minmax(0, 1fr);
}

.summary-item {
  display: grid;
  grid-template-columns: 106px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
  min-width: 0;
}

.summary-item-full {
  grid-template-columns: 106px minmax(0, 1fr);
}

.summary-label {
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.summary-value {
  color: var(--es-color-text-primary);
  word-break: break-all;
}

.summary-remark {
  min-height: 68px;
  align-items: flex-start;
  line-height: 1.6;
}

.section-title {
  margin: 0 0 10px;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.status-success {
  color: var(--es-color-success-text);
  font-weight: 600;
}

.status-failed {
  color: var(--es-color-error-text);
  font-weight: 600;
}

.status-pending {
  color: var(--es-color-info-text);
  font-weight: 600;
}

.status-closed {
  color: var(--es-color-text-placeholder);
  font-weight: 600;
}

.order-detail-fade-enter-active,
.order-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.order-detail-fade-enter-from,
.order-detail-fade-leave-to {
  opacity: 0;
}

.order-detail-fade-enter-from .order-detail-modal,
.order-detail-fade-leave-to .order-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
