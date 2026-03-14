<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  fetchOrderDetail,
  fetchOrderPage,
  type OrderDetail,
  type OrderPageItem
} from '@/api/adapters/trade'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import OrderDetailModal from '@/components/trades/OrderDetailModal.vue'

type PayStatusClass = 'success' | 'failed' | 'pending' | 'closed'

interface OrderFlowRow {
  id: string
  orderNo: string
  flowNo: string
  ownerName: string
  orderType: string
  orderTypeCode: string
  meterName: string
  meterNo: string
  rechargeAmount: string
  beforeBalance: string
  afterBalance: string
  serviceFee: string
  serviceRate: string
  orderTime: string
  paymentMethod: string
  paymentMethodCode: string
  payerName: string
  payStatus: string
  payStatusCode: string
  payStatusClass: PayStatusClass
}

const EMPTY_TEXT = '--'
const DEFAULT_PAGE_SIZE = 10
const orderFlowPermissionKeys = {
  detail: 'trade_management_order_flow_detail'
} as const

const payStatusOptions = [
  { label: '请选择', value: '' },
  { label: '待支付', value: 'NOT_PAY' },
  { label: '支付成功', value: 'SUCCESS' },
  { label: '已关闭', value: 'CLOSED' },
  { label: '支付异常', value: 'PAY_ERROR' }
]

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

const ORDER_STATUS_CLASS_MAP: Record<string, PayStatusClass> = {
  NOT_PAY: 'pending',
  SUCCESS: 'success',
  CLOSED: 'closed',
  PAY_ERROR: 'failed',
  REFUND_PROCESSING: 'pending',
  FULL_REFUND: 'closed',
  REFUND_CLOSED: 'closed',
  REFUND_ERROR: 'failed'
}

const queryForm = reactive({
  orderNo: '',
  flowNo: '',
  ownerName: '',
  orderTypeCode: '',
  paymentMethodCode: '',
  payStatusCode: '',
  orderTimeStart: '',
  orderTimeEnd: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
})

const appliedFilters = reactive({
  orderNo: '',
  flowNo: '',
  ownerName: '',
  orderTypeCode: '',
  paymentMethodCode: '',
  payStatusCode: '',
  orderTimeStart: '',
  orderTimeEnd: ''
})

const orderTypeOptions = ref<EnumOption[]>([{ label: '请选择', value: '' }])
const paymentMethodOptions = ref<EnumOption[]>([{ label: '请选择', value: '' }])
const rows = ref<OrderFlowRow[]>([])
const total = ref(0)
const loading = ref(false)
const orderDetailVisible = ref(false)
const orderDetailLoading = ref(false)
const orderDetailError = ref('')
const orderDetail = ref<OrderDetail | null>(null)

const normalizeText = (value: unknown) => {
  if (value === undefined || value === null) {
    return EMPTY_TEXT
  }
  const text = String(value).trim()
  return text ? text : EMPTY_TEXT
}

const normalizeNumberText = (value: unknown) => {
  if (value === undefined || value === null) {
    return EMPTY_TEXT
  }
  if (typeof value === 'number') {
    return Number.isFinite(value) ? String(value) : EMPTY_TEXT
  }
  if (typeof value === 'string') {
    const text = value.trim()
    return text ? text : EMPTY_TEXT
  }
  return EMPTY_TEXT
}

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
}

const toNumber = (value: string) => {
  const text = value.trim()
  if (!text) {
    return undefined
  }
  const parsed = Number(text)
  return Number.isFinite(parsed) ? parsed : undefined
}

const padNumber = (value: number) => String(value).padStart(2, '0')

const formatDateTime = (value: unknown) => {
  if (typeof value !== 'string' || !value.trim()) {
    return EMPTY_TEXT
  }

  const text = value.trim()
  if (!text.includes('T')) {
    return text.length >= 19 ? text.slice(0, 19) : text
  }

  const date = new Date(text)
  if (Number.isNaN(date.getTime())) {
    const fallback = text.replace('T', ' ')
    return fallback.length >= 19 ? fallback.slice(0, 19) : fallback
  }

  return `${date.getFullYear()}-${padNumber(date.getMonth() + 1)}-${padNumber(
    date.getDate()
  )} ${padNumber(date.getHours())}:${padNumber(date.getMinutes())}:${padNumber(date.getSeconds())}`
}

const formatQueryDateTime = (value: string, isEnd: boolean) => {
  const date = value.trim()
  if (!date) {
    return undefined
  }
  return `${date} ${isEnd ? '23:59:59' : '00:00:00'}`
}

const getPayStatusLabel = (status: string) => {
  return ORDER_STATUS_TEXT_MAP[status] || normalizeText(status)
}

const getPayStatusClass = (status: string): PayStatusClass => {
  return ORDER_STATUS_CLASS_MAP[status] || 'closed'
}

const normalizeOrderRow = (item: OrderPageItem, index: number): OrderFlowRow => {
  const statusCode = item.orderStatus === EMPTY_TEXT ? '' : item.orderStatus
  const paymentCode = item.paymentChannel || ''
  const orderTypeCode = item.orderType !== undefined ? String(item.orderType) : ''
  const orderTypeLabel =
    item.orderTypeName !== EMPTY_TEXT
      ? normalizeText(item.orderTypeName)
      : normalizeNumberText(item.orderType)
  const paymentMethodLabel =
    item.paymentChannelName !== EMPTY_TEXT
      ? normalizeText(item.paymentChannelName)
      : normalizeText(item.paymentChannel)

  return {
    id: item.orderSn !== EMPTY_TEXT ? `${item.orderSn}-${index}` : `${queryForm.pageNum}-${index}`,
    orderNo: normalizeText(item.orderSn),
    flowNo: normalizeText(item.thirdPartySn),
    ownerName: normalizeText(item.ownerName),
    orderType: orderTypeLabel,
    orderTypeCode,
    meterName: normalizeText(item.meterName),
    meterNo: normalizeText(item.deviceNo),
    rechargeAmount: normalizeNumberText(item.orderAmount),
    beforeBalance: normalizeNumberText(item.beginBalance),
    afterBalance: normalizeNumberText(item.endBalance),
    serviceFee: normalizeNumberText(item.serviceAmount),
    serviceRate: normalizeNumberText(item.serviceRate),
    orderTime: formatDateTime(item.orderCreateTime),
    paymentMethod: paymentMethodLabel,
    paymentMethodCode: paymentCode,
    payerName: normalizeText(item.userRealName),
    payStatus: getPayStatusLabel(statusCode),
    payStatusCode: statusCode,
    payStatusClass: getPayStatusClass(statusCode)
  }
}

const loadFilterOptions = async () => {
  try {
    const [orderTypes, paymentChannels] = await Promise.all([
      fetchEnumOptionsByKey('orderType'),
      fetchEnumOptionsByKey('paymentChannel')
    ])
    orderTypeOptions.value = [{ label: '请选择', value: '' }, ...orderTypes]
    paymentMethodOptions.value = [{ label: '请选择', value: '' }, ...paymentChannels]
  } catch {
    orderTypeOptions.value = [{ label: '请选择', value: '' }]
    paymentMethodOptions.value = [{ label: '请选择', value: '' }]
  }
}

const loadOrders = async () => {
  loading.value = true
  rows.value = []

  try {
    const result = await fetchOrderPage({
      orderType: toNumber(appliedFilters.orderTypeCode),
      orderStatus: appliedFilters.payStatusCode.trim() || undefined,
      orderSnLike: appliedFilters.orderNo.trim() || undefined,
      thirdPartySnLike: appliedFilters.flowNo.trim() || undefined,
      enterpriseNameLike: appliedFilters.ownerName.trim() || undefined,
      createStartTime: formatQueryDateTime(appliedFilters.orderTimeStart, false),
      createEndTime: formatQueryDateTime(appliedFilters.orderTimeEnd, true),
      paymentChannel: appliedFilters.paymentMethodCode.trim() || undefined,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })

    rows.value = result.list.map(normalizeOrderRow)
    total.value = result.total
    queryForm.pageNum = result.pageNum || queryForm.pageNum
    queryForm.pageSize = result.pageSize || queryForm.pageSize
  } catch {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const pagedRows = computed(() => rows.value)

const getSerialNumber = (index: number) => {
  const pageNum = queryForm.pageNum > 0 ? queryForm.pageNum : 1
  const pageSize = queryForm.pageSize > 0 ? queryForm.pageSize : DEFAULT_PAGE_SIZE
  return (pageNum - 1) * pageSize + index + 1
}

const search = async () => {
  appliedFilters.orderNo = queryForm.orderNo
  appliedFilters.flowNo = queryForm.flowNo
  appliedFilters.ownerName = queryForm.ownerName
  appliedFilters.orderTypeCode = queryForm.orderTypeCode
  appliedFilters.paymentMethodCode = queryForm.paymentMethodCode
  appliedFilters.payStatusCode = queryForm.payStatusCode
  appliedFilters.orderTimeStart = queryForm.orderTimeStart
  appliedFilters.orderTimeEnd = queryForm.orderTimeEnd
  queryForm.pageNum = 1
  await loadOrders()
}

const resetQuery = async () => {
  queryForm.orderNo = ''
  queryForm.flowNo = ''
  queryForm.ownerName = ''
  queryForm.orderTypeCode = ''
  queryForm.paymentMethodCode = ''
  queryForm.payStatusCode = ''
  queryForm.orderTimeStart = ''
  queryForm.orderTimeEnd = ''
  queryForm.pageNum = 1
  queryForm.pageSize = DEFAULT_PAGE_SIZE
  appliedFilters.orderNo = ''
  appliedFilters.flowNo = ''
  appliedFilters.ownerName = ''
  appliedFilters.orderTypeCode = ''
  appliedFilters.paymentMethodCode = ''
  appliedFilters.payStatusCode = ''
  appliedFilters.orderTimeStart = ''
  appliedFilters.orderTimeEnd = ''
  await loadOrders()
}

const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
  if (loading.value) {
    return
  }
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  await loadOrders()
}

const openOrderDetail = async (row: OrderFlowRow) => {
  const orderSn = row.orderNo.trim()
  if (!orderSn || orderSn === EMPTY_TEXT) {
    orderDetailError.value = '当前记录缺少订单号，无法查看详情'
    orderDetail.value = null
    orderDetailVisible.value = true
    return
  }

  orderDetailVisible.value = true
  orderDetailLoading.value = true
  orderDetailError.value = ''
  orderDetail.value = null

  try {
    orderDetail.value = await fetchOrderDetail(orderSn)
  } catch (error) {
    orderDetailError.value = `订单详情加载失败：${getErrorMessage(error)}`
  } finally {
    orderDetailLoading.value = false
  }
}

onMounted(async () => {
  await loadFilterOptions()
  await loadOrders()
})
</script>

<template>
  <div class="page">
    <section class="search-card">
      <div class="search-row">
        <div class="search-fields">
          <label class="search-item">
            <span class="search-label-inline">订单号</span>
            <input
              v-model="queryForm.orderNo"
              class="search-input search-radius-micro"
              placeholder="请输入订单号"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">流水号</span>
            <input
              v-model="queryForm.flowNo"
              class="search-input search-radius-micro"
              placeholder="请输入流水号"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">机构名称</span>
            <input
              v-model="queryForm.ownerName"
              class="search-input search-radius-micro"
              placeholder="请输入机构名称"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">订单类型</span>
            <select
              v-model="queryForm.orderTypeCode"
              class="search-input search-radius-micro"
              :class="{ 'search-input-placeholder': !queryForm.orderTypeCode }"
            >
              <option v-for="option in orderTypeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="search-item">
            <span class="search-label-inline">付款方式</span>
            <select
              v-model="queryForm.paymentMethodCode"
              class="search-input search-radius-micro"
              :class="{ 'search-input-placeholder': !queryForm.paymentMethodCode }"
            >
              <option
                v-for="option in paymentMethodOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="search-item">
            <span class="search-label-inline">支付状态</span>
            <select
              v-model="queryForm.payStatusCode"
              class="search-input search-radius-micro"
              :class="{ 'search-input-placeholder': !queryForm.payStatusCode }"
            >
              <option v-for="option in payStatusOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="search-item">
            <span class="search-label-inline">订单开始时间</span>
            <span
              class="search-date-wrap"
              :class="{ 'search-date-wrap-empty': !queryForm.orderTimeStart }"
              data-format="YYYY-MM-DD"
            >
              <input
                v-model="queryForm.orderTimeStart"
                class="search-input search-input-date search-radius-micro"
                :class="{ 'search-input-date-empty': !queryForm.orderTimeStart }"
                type="date"
                lang="en-CA"
              />
            </span>
          </label>
          <label class="search-item">
            <span class="search-label-inline">订单结束时间</span>
            <span
              class="search-date-wrap"
              :class="{ 'search-date-wrap-empty': !queryForm.orderTimeEnd }"
              data-format="YYYY-MM-DD"
            >
              <input
                v-model="queryForm.orderTimeEnd"
                class="search-input search-input-date search-radius-micro"
                :class="{ 'search-input-date-empty': !queryForm.orderTimeEnd }"
                type="date"
                lang="en-CA"
              />
            </span>
          </label>
        </div>

        <div class="search-actions">
          <button class="btn btn-primary search-radius-micro" :disabled="loading" @click="search">
            查询
          </button>
          <button
            class="btn btn-secondary search-radius-micro"
            :disabled="loading"
            @click="resetQuery"
          >
            重置
          </button>
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-toolbar">
        <h2 class="table-title">订单流水</h2>
      </div>

      <div class="table-wrap">
        <table class="table order-table">
          <thead>
            <tr>
              <th class="table-col-index sticky-col sticky-col-left sticky-index">序号</th>
              <th class="order-col-no sticky-col sticky-col-left sticky-order-no">订单号</th>
              <th class="order-col-type sticky-col sticky-col-left sticky-order-type">订单类型</th>
              <th>订单金额（元）</th>
              <th>支付方式</th>
              <th>流水号</th>
              <th>缴费人</th>
              <th>机构名称</th>
              <th>电表名称</th>
              <th>电表号</th>
              <th>订单前金额（元）</th>
              <th>订单后金额（元）</th>
              <th>服务费（元）</th>
              <th>服务费比例（%）</th>
              <th class="order-col-time sticky-col sticky-col-right sticky-order-time">订单时间</th>
              <th class="order-col-status sticky-col sticky-col-right sticky-pay-status"
                >支付状态</th
              >
              <th
                class="table-col-action order-col-action sticky-col sticky-col-right sticky-operation"
                >操作</th
              >
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="17" class="empty">
                <UiLoadingState :size="18" :thickness="2" :min-height="56" />
              </td>
            </tr>
            <tr v-else-if="pagedRows.length === 0">
              <td colspan="17" class="empty">
                <UiEmptyState :min-height="56" />
              </td>
            </tr>
            <template v-else>
              <tr v-for="(row, index) in pagedRows" :key="row.id">
                <td class="sticky-col sticky-col-left sticky-index">{{
                  getSerialNumber(index)
                }}</td>
                <td class="sticky-col sticky-col-left sticky-order-no">{{ row.orderNo }}</td>
                <td class="sticky-col sticky-col-left sticky-order-type">{{ row.orderType }}</td>
                <td>{{ row.rechargeAmount }}</td>
                <td>{{ row.paymentMethod }}</td>
                <td>{{ row.flowNo }}</td>
                <td>{{ row.payerName }}</td>
                <td class="enterprise-col" :title="row.ownerName">{{ row.ownerName }}</td>
                <td>{{ row.meterName }}</td>
                <td>{{ row.meterNo }}</td>
                <td>{{ row.beforeBalance }}</td>
                <td>{{ row.afterBalance }}</td>
                <td>{{ row.serviceFee }}</td>
                <td>{{ row.serviceRate }}</td>
                <td class="sticky-col sticky-col-right sticky-order-time">{{ row.orderTime }}</td>
                <td class="sticky-col sticky-col-right sticky-pay-status">
                  <span :class="['status-tag', `status-${row.payStatusClass}`]">{{
                    row.payStatus
                  }}</span>
                </td>
                <td class="table-col-action sticky-col sticky-col-right sticky-operation">
                  <button
                    v-menu-permission="orderFlowPermissionKeys.detail"
                    class="table-link"
                    type="button"
                    @click="openOrderDetail(row)"
                  >
                    详情
                  </button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <CommonPagination
        class="pager"
        :total="total"
        :page-num="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :loading="loading"
        @change="handlePageChange"
      />
    </section>

    <OrderDetailModal
      v-model="orderDetailVisible"
      :detail="orderDetail"
      :loading="orderDetailLoading"
      :error-text="orderDetailError"
    />
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 12px;
  min-width: 0;
  overflow: hidden;
}

.search-card,
.table-card {
  min-width: 0;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
}

.search-card {
  padding: 16px;
}

.table-card {
  padding: 16px;
}

.search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
  gap: 10px 16px;
}

.search-fields {
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
  margin: 0;
}

.search-label-inline {
  font-size: var(--es-font-size-md);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.search-input {
  width: 188px;
  height: 34px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.search-input-date {
  width: 164px;
  max-width: 164px;
  min-width: 164px;
  color: var(--es-color-text-primary);
}

.search-date-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.search-date-wrap::before {
  position: absolute;
  top: 50%;
  left: 10px;
  font-size: var(--es-font-size-sm);
  letter-spacing: 0.2px;
  color: var(--es-color-text-placeholder);
  pointer-events: none;
  content: attr(data-format);
  opacity: 0;
  transform: translateY(-50%);
}

.search-date-wrap.search-date-wrap-empty::before {
  opacity: 1;
}

.search-input-date.search-input-date-empty {
  color: var(--es-color-text-placeholder);
}

.search-input-date::-webkit-datetime-edit {
  color: inherit;
}

.search-input-date.search-input-date-empty::-webkit-datetime-edit {
  color: transparent;
}

.search-input-date.search-input-date-empty::-webkit-datetime-edit-text {
  color: transparent;
}

.search-input-date::-webkit-calendar-picker-indicator {
  cursor: pointer;
}

.search-input-date::-webkit-calendar-picker-indicator:hover {
  cursor: pointer;
}

.search-input-placeholder {
  color: var(--es-color-text-placeholder);
}

.search-actions {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  gap: 8px;
  align-self: start;
}

.btn {
  height: 34px;
  min-width: 72px;
  padding: 0 14px;
  font-size: var(--es-font-size-sm);
  white-space: nowrap;
  border: 1px solid transparent;
  border-radius: 5px;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.table-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.table-wrap {
  overflow: auto;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  scrollbar-gutter: stable;
}

.table-wrap::-webkit-scrollbar {
  height: 14px;
}

.table-wrap::-webkit-scrollbar-track {
  background: #f0f4fa;
  border-radius: 999px;
}

.table-wrap::-webkit-scrollbar-thumb {
  background: #c2d3ea;
  border: 3px solid #f0f4fa;
  border-radius: 999px;
}

.table-wrap:hover::-webkit-scrollbar-thumb {
  background: #9fb8de;
}

table {
  width: max-content;
  min-width: 100%;
  min-width: 1760px;
  border-collapse: collapse;
}

th,
td {
  padding: 10px;
  font-size: var(--es-font-size-sm);
  text-align: left;
  white-space: nowrap;
  border-bottom: 1px solid var(--es-color-border);
}

.table-col-index {
  width: 84px;
}

.order-col-no {
  width: 168px;
}

.order-col-type {
  width: 120px;
}

.order-col-time {
  width: 160px;
}

.order-col-status {
  width: 112px;
}

.order-col-action {
  width: 92px;
}

.sticky-col {
  position: sticky;
  z-index: 2;
  background: #fff;
}

th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.table th.sticky-col {
  z-index: 4;
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.sticky-col-left {
  box-shadow: 1px 0 0 var(--es-color-border);
}

.sticky-col-right {
  box-shadow: -1px 0 0 var(--es-color-border);
}

.sticky-index {
  left: 0;
}

.sticky-order-no {
  left: 84px;
}

.sticky-order-type {
  left: 252px;
}

.sticky-pay-status {
  right: 92px;
}

.sticky-order-time {
  right: 204px;
}

.sticky-operation {
  right: 0;
}

td {
  color: var(--es-color-text-secondary);
}

.enterprise-col {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  font-weight: 500;
}

.status-success {
  color: var(--es-color-success-text);
}

.status-failed {
  color: var(--es-color-error-text);
}

.status-pending {
  color: var(--es-color-info-text);
}

.status-closed {
  color: var(--es-color-text-placeholder);
}

.order-table tbody tr:hover td {
  background: #fafcff;
}

.order-table tbody tr:hover .sticky-col {
  background: #fafcff;
}

.table-col-action {
  text-align: left;
}

.table-link {
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-primary);
  cursor: pointer;
  background: transparent;
  border: none;
}

.empty {
  padding: 16px 0;
  color: var(--es-color-text-placeholder);
  text-align: center;
}

.pager {
  padding: 0 10px;
}

@media (width <= 1280px) {
  .search-row {
    grid-template-columns: 1fr;
  }

  .search-actions {
    justify-self: end;
  }
}
</style>
