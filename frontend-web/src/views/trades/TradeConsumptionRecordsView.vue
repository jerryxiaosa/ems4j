<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  fetchAccountConsumePage,
  fetchMeterConsumeDetail,
  fetchMeterConsumePage,
  type MeterConsumeDetail,
  type AccountConsumePageItem,
  type MeterConsumePageItem
} from '@/api/adapters/trade'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import MeterConsumeDetailModal from '@/components/trades/MeterConsumeDetailModal.vue'

type ConsumeTabKey = 'meter' | 'monthly'

interface MeterConsumeRow {
  id: number
  detailId: number | null
  meterName: string
  deviceNo: string
  meterType: string
  devicePosition: string
  balanceBefore: string
  consumeAmount: string
  balanceAfter: string
  billingType: string
  consumeTime: string
}

interface MonthlyConsumeRow {
  id: number
  accountName: string
  accountType: string
  contactName: string
  contactPhone: string
  balanceBefore: string
  consumeAmount: string
  balanceAfter: string
  deductReason: string
  consumeTime: string
}

const DEFAULT_PAGE_SIZE = 10
const EMPTY_TEXT = '--'
const consumptionRecordPermissionKeys = {
  detail: 'trade_management_consumption_record_detail'
} as const

const activeTab = ref<ConsumeTabKey>('meter')

const todayDate = (() => {
  const date = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
})()

const meterQueryForm = reactive({
  spaceNameLike: '',
  searchKey: '',
  consumeTimeStart: '',
  consumeTimeEnd: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
})

const meterAppliedFilters = reactive({
  spaceNameLike: '',
  searchKey: '',
  consumeTimeStart: '',
  consumeTimeEnd: ''
})

const monthlyQueryForm = reactive({
  ownerName: '',
  consumeTimeStart: '',
  consumeTimeEnd: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
})

const monthlyAppliedFilters = reactive({
  ownerName: '',
  consumeTimeStart: '',
  consumeTimeEnd: ''
})

const meterRows = ref<MeterConsumeRow[]>([])
const meterLoading = ref(false)
const meterTotal = ref(0)

const monthlyRows = ref<MonthlyConsumeRow[]>([])
const monthlyLoading = ref(false)
const monthlyTotal = ref(0)
const monthlyLoaded = ref(false)

const meterDetailVisible = ref(false)
const meterDetailLoading = ref(false)
const meterDetailError = ref('')
const meterDetail = ref<MeterConsumeDetail | null>(null)

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return EMPTY_TEXT
  }
  const text = String(value).trim()
  return text ? text : EMPTY_TEXT
}

const normalizeNumberText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return EMPTY_TEXT
  }
  if (typeof value === 'number') {
    return Number.isFinite(value) ? String(value) : EMPTY_TEXT
  }
  const text = String(value).trim()
  return text ? text : EMPTY_TEXT
}

const formatOptionalQueryDateTime = (value: string, isEnd: boolean): string | undefined => {
  const date = value.trim()
  if (!date) {
    return undefined
  }
  return `${date} ${isEnd ? '23:59:59' : '00:00:00'}`
}

const normalizeMeterRow = (item: MeterConsumePageItem, index: number): MeterConsumeRow => ({
  id: item.id || (meterQueryForm.pageNum - 1) * meterQueryForm.pageSize + index + 1,
  detailId: typeof item.id === 'number' && item.id > 0 ? item.id : null,
  meterName: normalizeText(item.meterName),
  deviceNo: normalizeText(item.deviceNo),
  meterType: normalizeText(item.meterTypeName),
  devicePosition: normalizeText(item.spaceName),
  balanceBefore: normalizeNumberText(item.beginBalance),
  consumeAmount: normalizeNumberText(item.consumeAmount),
  balanceAfter: normalizeNumberText(item.endBalance),
  billingType: normalizeText(item.electricAccountTypeText),
  consumeTime: normalizeText(item.consumeTime)
})

const normalizeMonthlyRow = (item: AccountConsumePageItem, index: number): MonthlyConsumeRow => ({
  id: item.id || (monthlyQueryForm.pageNum - 1) * monthlyQueryForm.pageSize + index + 1,
  accountName: normalizeText(item.ownerName),
  accountType: normalizeText(item.ownerTypeName),
  contactName: normalizeText(item.contactName),
  contactPhone: normalizeText(item.contactPhone),
  balanceBefore: normalizeNumberText(item.beginBalance),
  consumeAmount: normalizeNumberText(item.payAmount),
  balanceAfter: normalizeNumberText(item.endBalance),
  deductReason: normalizeText(item.consumeTypeName),
  consumeTime: normalizeText(item.consumeTime)
})

const loadMeterRecords = async () => {
  meterLoading.value = true
  meterRows.value = []

  try {
    const result = await fetchMeterConsumePage({
      searchKey: meterAppliedFilters.searchKey.trim() || undefined,
      spaceNameLike: meterAppliedFilters.spaceNameLike.trim() || undefined,
      beginTime: formatOptionalQueryDateTime(meterAppliedFilters.consumeTimeStart, false),
      endTime: formatOptionalQueryDateTime(meterAppliedFilters.consumeTimeEnd, true),
      pageNum: meterQueryForm.pageNum,
      pageSize: meterQueryForm.pageSize
    })

    meterRows.value = result.list.map(normalizeMeterRow)
    meterTotal.value = result.total
    meterQueryForm.pageNum = result.pageNum || meterQueryForm.pageNum
    meterQueryForm.pageSize = result.pageSize || meterQueryForm.pageSize
  } catch {
    meterRows.value = []
    meterTotal.value = 0
  } finally {
    meterLoading.value = false
  }
}

const loadMonthlyRecords = async () => {
  monthlyLoading.value = true
  monthlyRows.value = []

  try {
    const result = await fetchAccountConsumePage({
      accountNameLike: monthlyAppliedFilters.ownerName.trim() || undefined,
      consumeTimeStart: formatOptionalQueryDateTime(monthlyAppliedFilters.consumeTimeStart, false),
      consumeTimeEnd: formatOptionalQueryDateTime(monthlyAppliedFilters.consumeTimeEnd, true),
      pageNum: monthlyQueryForm.pageNum,
      pageSize: monthlyQueryForm.pageSize
    })

    monthlyRows.value = result.list.map(normalizeMonthlyRow)
    monthlyTotal.value = result.total
    monthlyQueryForm.pageNum = result.pageNum || monthlyQueryForm.pageNum
    monthlyQueryForm.pageSize = result.pageSize || monthlyQueryForm.pageSize
  } catch {
    monthlyRows.value = []
    monthlyTotal.value = 0
  } finally {
    monthlyLoading.value = false
  }
}

onMounted(() => {
  void loadMeterRecords()
})

watch(
  () => activeTab.value,
  async (tab) => {
    if (tab === 'monthly' && !monthlyLoaded.value) {
      monthlyLoaded.value = true
      await loadMonthlyRecords()
    }
  }
)

const meterPagedRows = computed(() => meterRows.value)
const monthlyPagedRows = computed(() => monthlyRows.value)

const searchMeterRecords = async () => {
  meterAppliedFilters.spaceNameLike = meterQueryForm.spaceNameLike
  meterAppliedFilters.searchKey = meterQueryForm.searchKey
  meterAppliedFilters.consumeTimeStart = meterQueryForm.consumeTimeStart
  meterAppliedFilters.consumeTimeEnd = meterQueryForm.consumeTimeEnd
  meterQueryForm.pageNum = 1
  await loadMeterRecords()
}

const resetMeterQuery = async () => {
  meterQueryForm.spaceNameLike = ''
  meterQueryForm.searchKey = ''
  meterQueryForm.consumeTimeStart = ''
  meterQueryForm.consumeTimeEnd = ''
  meterQueryForm.pageNum = 1

  meterAppliedFilters.spaceNameLike = ''
  meterAppliedFilters.searchKey = ''
  meterAppliedFilters.consumeTimeStart = ''
  meterAppliedFilters.consumeTimeEnd = ''
  await loadMeterRecords()
}

const searchMonthlyRecords = async () => {
  monthlyAppliedFilters.ownerName = monthlyQueryForm.ownerName
  monthlyAppliedFilters.consumeTimeStart = monthlyQueryForm.consumeTimeStart
  monthlyAppliedFilters.consumeTimeEnd = monthlyQueryForm.consumeTimeEnd
  monthlyQueryForm.pageNum = 1
  await loadMonthlyRecords()
}

const resetMonthlyQuery = async () => {
  monthlyQueryForm.ownerName = ''
  monthlyQueryForm.consumeTimeStart = ''
  monthlyQueryForm.consumeTimeEnd = ''
  monthlyQueryForm.pageNum = 1
  monthlyAppliedFilters.ownerName = ''
  monthlyAppliedFilters.consumeTimeStart = ''
  monthlyAppliedFilters.consumeTimeEnd = ''
  await loadMonthlyRecords()
}

const handleMeterPageChange = async (payload: { pageNum: number; pageSize: number }) => {
  if (meterLoading.value) {
    return
  }
  meterQueryForm.pageNum = payload.pageNum
  meterQueryForm.pageSize = payload.pageSize
  await loadMeterRecords()
}

const handleMonthlyPageChange = async (payload: { pageNum: number; pageSize: number }) => {
  if (monthlyLoading.value) {
    return
  }
  monthlyQueryForm.pageNum = payload.pageNum
  monthlyQueryForm.pageSize = payload.pageSize
  await loadMonthlyRecords()
}

const getMeterSerialNumber = (index: number) => {
  return (meterQueryForm.pageNum - 1) * meterQueryForm.pageSize + index + 1
}

const getMonthlySerialNumber = (index: number) => {
  return (monthlyQueryForm.pageNum - 1) * monthlyQueryForm.pageSize + index + 1
}

const openMeterDetail = async (row: MeterConsumeRow) => {
  if (!row.detailId) {
    meterDetailError.value = '当前记录缺少详情ID，无法查看详情'
    meterDetail.value = null
    meterDetailVisible.value = true
    return
  }

  meterDetailVisible.value = true
  meterDetailLoading.value = true
  meterDetailError.value = ''
  meterDetail.value = null

  try {
    const result = await fetchMeterConsumeDetail(row.detailId)
    meterDetail.value = result
  } catch {
    meterDetailError.value = '消费记录详情加载失败，请稍后重试'
    meterDetail.value = null
  } finally {
    meterDetailLoading.value = false
  }
}
</script>

<template>
  <div class="page">
    <section class="search-card">
      <div class="tab-nav">
        <button
          class="tab-btn"
          :class="{ 'is-active': activeTab === 'meter' }"
          type="button"
          @click="activeTab = 'meter'"
        >
          表具消费记录
        </button>
        <button
          class="tab-btn"
          :class="{ 'is-active': activeTab === 'monthly' }"
          type="button"
          @click="activeTab = 'monthly'"
        >
          包月消费记录
        </button>
      </div>

      <div v-if="activeTab === 'meter'" class="search-row">
        <div class="search-fields">
          <label class="search-item">
            <span class="search-label-inline">设备位置</span>
            <input
              v-model="meterQueryForm.spaceNameLike"
              class="search-input search-radius-micro"
              placeholder="请输入设备位置"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">表具名称/编号</span>
            <input
              v-model="meterQueryForm.searchKey"
              class="search-input search-radius-micro"
              placeholder="请输入表具名称/编号"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">开始时间</span>
            <span
              class="search-date-wrap"
              :class="{ 'search-date-wrap-empty': !meterQueryForm.consumeTimeStart }"
              data-format="YYYY-MM-DD"
            >
              <input
                v-model="meterQueryForm.consumeTimeStart"
                class="search-input search-input-date search-radius-micro"
                :class="{ 'search-input-date-empty': !meterQueryForm.consumeTimeStart }"
                type="date"
                lang="en-CA"
              />
            </span>
          </label>
          <label class="search-item">
            <span class="search-label-inline">结束时间</span>
            <span
              class="search-date-wrap"
              :class="{ 'search-date-wrap-empty': !meterQueryForm.consumeTimeEnd }"
              data-format="YYYY-MM-DD"
            >
              <input
                v-model="meterQueryForm.consumeTimeEnd"
                class="search-input search-input-date search-radius-micro"
                :class="{ 'search-input-date-empty': !meterQueryForm.consumeTimeEnd }"
                type="date"
                lang="en-CA"
              />
            </span>
          </label>
        </div>

        <div class="search-actions">
          <button
            class="btn btn-primary search-radius-micro"
            :disabled="meterLoading"
            @click="searchMeterRecords"
            >查询</button
          >
          <button
            class="btn btn-secondary search-radius-micro"
            :disabled="meterLoading"
            @click="resetMeterQuery"
            >重置</button
          >
        </div>
      </div>

      <div v-else class="search-row">
        <div class="search-fields">
          <label class="search-item">
            <span class="search-label-inline">机构名称</span>
            <input
              v-model="monthlyQueryForm.ownerName"
              class="search-input search-radius-micro"
              placeholder="请输入机构名称"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">开始时间</span>
            <span
              class="search-date-wrap"
              :class="{ 'search-date-wrap-empty': !monthlyQueryForm.consumeTimeStart }"
              data-format="YYYY-MM-DD"
            >
              <input
                v-model="monthlyQueryForm.consumeTimeStart"
                class="search-input search-input-date search-radius-micro"
                :class="{ 'search-input-date-empty': !monthlyQueryForm.consumeTimeStart }"
                type="date"
                lang="en-CA"
              />
            </span>
          </label>
          <label class="search-item">
            <span class="search-label-inline">结束时间</span>
            <span
              class="search-date-wrap"
              :class="{ 'search-date-wrap-empty': !monthlyQueryForm.consumeTimeEnd }"
              data-format="YYYY-MM-DD"
            >
              <input
                v-model="monthlyQueryForm.consumeTimeEnd"
                class="search-input search-input-date search-radius-micro"
                :class="{ 'search-input-date-empty': !monthlyQueryForm.consumeTimeEnd }"
                type="date"
                lang="en-CA"
              />
            </span>
          </label>
        </div>

        <div class="search-actions">
          <button
            class="btn btn-primary search-radius-micro"
            :disabled="monthlyLoading"
            @click="searchMonthlyRecords"
            >查询</button
          >
          <button
            class="btn btn-secondary search-radius-micro"
            :disabled="monthlyLoading"
            @click="resetMonthlyQuery"
            >重置</button
          >
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-wrap">
        <div
          v-if="activeTab === 'meter' && !meterLoading && !meterPagedRows.length"
          class="table-empty-overlay"
        >
          <UiEmptyState :min-height="0" />
        </div>
        <div
          v-if="activeTab === 'monthly' && !monthlyLoading && !monthlyPagedRows.length"
          class="table-empty-overlay"
        >
          <UiEmptyState :min-height="0" />
        </div>

        <table v-if="activeTab === 'meter'" class="table meter-table">
          <thead>
            <tr>
              <th class="table-col-index sticky-col sticky-col-left sticky-index">序号</th>
              <th class="meter-col-name sticky-col sticky-col-left sticky-meter-name">表具名称</th>
              <th class="meter-col-no sticky-col sticky-col-left sticky-meter-no">表具编号</th>
              <th>表具类型</th>
              <th>所在位置</th>
              <th>消费前余额（元）</th>
              <th>消费金额（元）</th>
              <th>消费后余额（元）</th>
              <th>计费类型</th>
              <th class="meter-col-time sticky-col sticky-col-right sticky-consume-time"
                >消费时间</th
              >
              <th class="table-col-action sticky-col sticky-col-right sticky-operation">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="meterLoading">
              <td colspan="11" class="empty">
                <UiLoadingState :size="18" :thickness="2" :min-height="56" />
              </td>
            </tr>
            <template v-else>
              <tr v-for="(row, index) in meterPagedRows" :key="row.id">
                <td class="sticky-col sticky-col-left sticky-index">{{
                  getMeterSerialNumber(index)
                }}</td>
                <td class="sticky-col sticky-col-left sticky-meter-name">{{ row.meterName }}</td>
                <td class="sticky-col sticky-col-left sticky-meter-no">{{ row.deviceNo }}</td>
                <td>{{ row.meterType }}</td>
                <td>{{ row.devicePosition }}</td>
                <td>{{ row.balanceBefore }}</td>
                <td>{{ row.consumeAmount }}</td>
                <td>{{ row.balanceAfter }}</td>
                <td>{{ row.billingType }}</td>
                <td class="sticky-col sticky-col-right sticky-consume-time">{{
                  row.consumeTime
                }}</td>
                <td class="table-col-action sticky-col sticky-col-right sticky-operation">
                  <button
                    v-menu-permission="consumptionRecordPermissionKeys.detail"
                    class="table-link"
                    type="button"
                    @click="openMeterDetail(row)"
                  >
                    详情
                  </button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>

        <table v-else class="table monthly-table">
          <thead>
            <tr>
              <th class="table-col-index sticky-col sticky-col-left sticky-monthly-index">序号</th>
              <th
                class="monthly-col-account-name sticky-col sticky-col-left sticky-monthly-account-name"
                >账户名称</th
              >
              <th
                class="monthly-col-account-type sticky-col sticky-col-left sticky-monthly-account-type"
                >账户类型</th
              >
              <th>联系人</th>
              <th>联系方式</th>
              <th>消费前余额（元）</th>
              <th>消费金额（元）</th>
              <th>消费后余额（元）</th>
              <th class="monthly-col-category sticky-col sticky-col-right sticky-monthly-category"
                >消费类别</th
              >
              <th class="monthly-col-time sticky-col sticky-col-right sticky-monthly-time"
                >消费时间</th
              >
            </tr>
          </thead>
          <tbody>
            <tr v-if="monthlyLoading">
              <td colspan="10" class="empty">
                <UiLoadingState :size="18" :thickness="2" :min-height="56" />
              </td>
            </tr>
            <template v-else>
              <tr v-for="(row, index) in monthlyPagedRows" :key="row.id">
                <td class="sticky-col sticky-col-left sticky-monthly-index">{{
                  getMonthlySerialNumber(index)
                }}</td>
                <td class="sticky-col sticky-col-left sticky-monthly-account-name">{{
                  row.accountName
                }}</td>
                <td class="sticky-col sticky-col-left sticky-monthly-account-type">{{
                  row.accountType
                }}</td>
                <td>{{ row.contactName }}</td>
                <td>{{ row.contactPhone }}</td>
                <td>{{ row.balanceBefore }}</td>
                <td>{{ row.consumeAmount }}</td>
                <td>{{ row.balanceAfter }}</td>
                <td class="sticky-col sticky-col-right sticky-monthly-category">{{
                  row.deductReason
                }}</td>
                <td class="sticky-col sticky-col-right sticky-monthly-time">{{
                  row.consumeTime
                }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <CommonPagination
        v-if="activeTab === 'meter'"
        class="pager"
        :total="meterTotal"
        :page-num="meterQueryForm.pageNum"
        :page-size="meterQueryForm.pageSize"
        :loading="meterLoading"
        @change="handleMeterPageChange"
      />

      <CommonPagination
        v-else
        class="pager"
        :total="monthlyTotal"
        :page-num="monthlyQueryForm.pageNum"
        :page-size="monthlyQueryForm.pageSize"
        :loading="monthlyLoading"
        @change="handleMonthlyPageChange"
      />
    </section>

    <MeterConsumeDetailModal
      v-model="meterDetailVisible"
      :detail="meterDetail"
      :loading="meterDetailLoading"
      :error-text="meterDetailError"
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
  padding: 0 16px 16px;
}

.tab-nav {
  display: flex;
  min-height: 50px;
  margin-bottom: 14px;
  border-bottom: 1px solid var(--es-color-border);
  align-items: center;
  gap: 20px;
}

.tab-btn {
  position: relative;
  padding: 0;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  line-height: 48px;
  color: var(--es-color-text-primary);
  cursor: pointer;
  background: transparent;
  border: none;
}

.tab-btn.is-active {
  color: var(--es-color-primary);
}

.tab-btn.is-active::after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 2px;
  background: var(--es-color-primary);
  content: '';
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

.search-input-placeholder {
  color: var(--es-color-text-placeholder);
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

.search-actions {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  gap: 8px;
  align-self: start;
}

.table-card {
  padding: 16px;
}

.table-wrap {
  position: relative;
  min-height: 120px;
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

.table {
  width: max-content;
  min-width: 100%;
  border-collapse: collapse;
}

.meter-table {
  min-width: 1700px;
}

.monthly-table {
  min-width: 1460px;
}

.table th,
.table td {
  padding: 10px;
  font-size: var(--es-font-size-sm);
  text-align: left;
  white-space: nowrap;
  border-bottom: 1px solid var(--es-color-border);
}

.table th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.table th.sticky-col {
  z-index: 4;
  background: var(--es-color-table-header-bg);
}

.table td {
  color: var(--es-color-text-secondary);
}

.table-col-index {
  width: 84px;
}

.meter-col-name {
  width: 140px;
}

.meter-col-no {
  width: 160px;
}

.meter-col-time {
  width: 170px;
}

.monthly-col-account-name {
  width: 160px;
}

.monthly-col-account-type {
  width: 120px;
}

.monthly-col-category {
  width: 136px;
}

.monthly-col-time {
  width: 170px;
}

.table-col-action {
  width: 120px;
}

.sticky-col {
  position: sticky;
  z-index: 2;
  background: #fff;
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

.sticky-meter-name {
  left: 84px;
}

.sticky-meter-no {
  left: 224px;
}

.sticky-operation {
  right: 0;
}

.sticky-consume-time {
  right: 120px;
}

.sticky-monthly-index {
  left: 0;
}

.sticky-monthly-account-name {
  left: 84px;
}

.sticky-monthly-account-type {
  left: 244px;
}

.sticky-monthly-time {
  right: 0;
}

.sticky-monthly-category {
  right: 170px;
}

.table-link {
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
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

.table-empty-overlay {
  position: absolute;
  inset: 42px 0 0;
  display: flex;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-placeholder);
  pointer-events: none;
  align-items: center;
  justify-content: center;
}

.meter-table tbody tr:hover td {
  background: #fafcff;
}

.meter-table tbody tr:hover .sticky-col {
  background: #fafcff;
}

.monthly-table tbody tr:hover td {
  background: #fafcff;
}

.monthly-table tbody tr:hover .sticky-col {
  background: #fafcff;
}

.pager {
  padding: 0 10px;
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
</style>
