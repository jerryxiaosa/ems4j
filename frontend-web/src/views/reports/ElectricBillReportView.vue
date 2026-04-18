<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  fetchElectricBillReportDetail,
  fetchElectricBillReportPage
} from '@/api/adapters/report'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiTableStateOverlay from '@/components/common/UiTableStateOverlay.vue'
import ElectricBillReportDetailModal from '@/components/reports/ElectricBillReportDetailModal.vue'
import type {
  ElectricBillReportDetailResult,
  ElectricBillReportPageResult,
  ElectricBillReportQuery
} from '@/types/report'

const NOTICE_VISIBLE_MS = 3500
const DAY_IN_MS = 24 * 60 * 60 * 1000
const MAX_RANGE_DAYS = 65

const notice = reactive({
  type: 'info' as 'info' | 'error',
  text: ''
})

let noticeTimer: number | null = null

const pageLoading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const detailData = ref<ElectricBillReportDetailResult | null>(null)

const listPage = reactive<ElectricBillReportPageResult>({
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
})

const getYesterdayText = () => {
  const date = new Date()
  date.setDate(date.getDate() - 1)
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const defaultDate = getYesterdayText()

const queryForm = reactive<ElectricBillReportQuery>({
  accountNameLike: '',
  startDate: defaultDate,
  endDate: defaultDate,
  pageNum: 1,
  pageSize: 10
})

const yesterdayText = computed(() => getYesterdayText())

const clearNoticeTimer = () => {
  if (noticeTimer !== null) {
    window.clearTimeout(noticeTimer)
    noticeTimer = null
  }
}

const setNotice = (type: 'info' | 'error', text: string) => {
  clearNoticeTimer()
  notice.type = type
  notice.text = text
  noticeTimer = window.setTimeout(() => {
    notice.text = ''
    noticeTimer = null
  }, NOTICE_VISIBLE_MS)
}

const toDateValue = (value: string) => {
  const parsed = new Date(`${value}T00:00:00`)
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

const validateQuery = () => {
  const startDate = toDateValue(queryForm.startDate)
  const endDate = toDateValue(queryForm.endDate)
  const yesterdayDate = toDateValue(yesterdayText.value)

  if (!startDate || !endDate || !yesterdayDate) {
    setNotice('error', '统计日期不能为空')
    return false
  }

  if (startDate.getTime() > endDate.getTime()) {
    setNotice('error', '开始日期不能晚于结束日期')
    return false
  }

  if (endDate.getTime() > yesterdayDate.getTime()) {
    setNotice('error', '统计结束日期不能选择今天')
    return false
  }

  const rangeDays = Math.floor((endDate.getTime() - startDate.getTime()) / DAY_IN_MS) + 1
  if (rangeDays > MAX_RANGE_DAYS) {
    setNotice('error', '统计日期跨度不能超过 65 天')
    return false
  }

  return true
}

const loadList = async () => {
  if (!validateQuery()) {
    return
  }

  pageLoading.value = true
  try {
    const result = await fetchElectricBillReportPage(queryForm)
    listPage.list = result.list
    listPage.total = result.total
    listPage.pageNum = result.pageNum
    listPage.pageSize = result.pageSize
  } catch (error) {
    setNotice('error', (error as Error)?.message || '电费报表加载失败')
  } finally {
    pageLoading.value = false
  }
}

const getSerialNumber = (index: number) => {
  return (queryForm.pageNum - 1) * queryForm.pageSize + index + 1
}

const search = async () => {
  queryForm.pageNum = 1
  await loadList()
}

const resetQuery = async () => {
  queryForm.accountNameLike = ''
  queryForm.startDate = defaultDate
  queryForm.endDate = defaultDate
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  await loadList()
}

const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  await loadList()
}

const openDetail = async (accountId: number) => {
  if (!validateQuery()) {
    return
  }

  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    detailData.value = await fetchElectricBillReportDetail(
      accountId,
      {
        startDate: queryForm.startDate,
        endDate: queryForm.endDate
      }
    )
  } catch (error) {
    setNotice('error', (error as Error)?.message || '电费报表详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(async () => {
  await loadList()
})
</script>

<template>
  <div class="page">
    <div v-if="notice.text" class="notice" :class="`notice-${notice.type}`">
      {{ notice.text }}
    </div>

    <section class="search-card">
      <div class="search-row">
        <label class="search-item">
          <span class="search-label-inline">账户名称</span>
          <input
            v-model="queryForm.accountNameLike"
            class="search-input"
            type="text"
            maxlength="40"
            placeholder="请输入账户名称"
            @keydown.enter.prevent="search"
          />
        </label>

        <label class="search-item">
          <span class="search-label-inline">统计日期</span>
          <input
            v-model="queryForm.startDate"
            class="search-input search-date-input"
            :max="yesterdayText"
            type="date"
          />
        </label>

        <span class="date-separator">至</span>

        <label class="search-item">
          <input
            v-model="queryForm.endDate"
            class="search-input search-date-input"
            :max="yesterdayText"
            type="date"
          />
        </label>

        <div class="search-actions">
          <button class="btn btn-primary search-radius-micro" :disabled="pageLoading" @click="search">
            查询
          </button>
          <button class="btn btn-secondary search-radius-micro" :disabled="pageLoading" @click="resetQuery">
            重置
          </button>
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-toolbar">
        <h2 class="table-title">电费报表</h2>
      </div>

      <div class="table-wrap">
        <UiTableStateOverlay
          :loading="pageLoading"
          :empty="!pageLoading && listPage.list.length === 0"
        />
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th class="col-account-name">账户名称</th>
              <th>电价计费类型</th>
              <th>电表数量</th>
              <th>本期电量(kWh)</th>
              <th>本期电费</th>
              <th>充值金额</th>
              <th>补正金额</th>
              <th>合计费用</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in listPage.list" :key="row.accountId">
              <td>{{ getSerialNumber(index) }}</td>
              <td class="account-name-cell" :title="row.accountName">{{ row.accountName }}</td>
              <td>{{ row.electricAccountTypeName }}</td>
              <td>{{ row.meterCount }}</td>
              <td>{{ row.periodConsumePowerText }}</td>
              <td>{{ row.periodElectricChargeAmountText }}</td>
              <td>{{ row.periodRechargeAmountText }}</td>
              <td>{{ row.periodCorrectionAmountText }}</td>
              <td>{{ row.totalDebitAmountText }}</td>
              <td class="actions">
                <button class="btn-link" @click="openDetail(row.accountId)">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <CommonPagination
        class="pager"
        :total="listPage.total"
        :page-num="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :loading="pageLoading"
        @change="handlePageChange"
      />
    </section>

    <ElectricBillReportDetailModal
      v-model="detailVisible"
      :detail="detailData"
      :loading="detailLoading"
    />
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.notice {
  padding: 10px 12px;
  font-size: var(--es-font-size-md);
  border: 1px solid transparent;
  border-radius: 5px;
  box-shadow: 0 8px 24px rgb(15 23 42 / 10%);
}

.notice-info {
  color: var(--es-color-info-text);
  background: var(--es-color-info-bg);
  border-color: var(--es-color-info-border);
}

.notice-error {
  color: var(--es-color-error-text);
  background: var(--es-color-error-bg);
  border-color: var(--es-color-error-border);
}

.search-card,
.table-card {
  padding: 16px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
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
  margin: 0;
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
  min-width: 180px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.search-date-input {
  width: 160px;
  min-width: 160px;
}

.search-input::placeholder {
  color: var(--es-color-text-placeholder);
}

.search-input:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.date-separator {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-shrink: 0;
}

.search-radius-micro {
  border-radius: 5px !important;
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

.btn {
  display: inline-flex;
  height: 36px;
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
  border-color: var(--es-color-primary);
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

.btn-link {
  height: auto;
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  line-height: 1.2;
  color: var(--es-color-primary);
  background: transparent;
  border: none;
}

.btn-link:hover {
  color: var(--es-color-primary-hover);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.table-wrap {
  position: relative;
  min-height: 120px;
  overflow: auto;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

table {
  width: 100%;
  min-width: 1040px;
  border-collapse: collapse;
}

th,
td {
  padding: 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  border-bottom: 1px solid var(--es-color-border);
  border-right: 1px solid var(--es-color-border);
  white-space: nowrap;
}

th:last-child,
td:last-child {
  border-right: none;
}

thead th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

tbody tr:hover {
  background: #fafcff;
}

.col-account-name {
  width: 180px;
  min-width: 180px;
}

.account-name-cell {
  min-width: 180px;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.actions {
  min-width: 80px;
}

.empty {
  padding: 0;
}

@media (width <= 960px) {
  .search-actions {
    width: 100%;
    margin-left: 0;
  }
}
</style>
