<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { fetchCancelDetail, fetchCancelRecordPage } from '@/api/adapters/account'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { CancelDetail, CancelRecordPageResult } from '@/types/account'

const cancelRecordPermissionKeys = {
  detail: 'account_management_cancel_records_detail'
} as const

const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})
const noticeFading = ref(false)

let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null

const NOTICE_VISIBLE_MS = 5000
const NOTICE_FADE_MS = 300

const clearNoticeTimers = () => {
  if (noticeFadeTimer !== null) {
    window.clearTimeout(noticeFadeTimer)
    noticeFadeTimer = null
  }
  if (noticeClearTimer !== null) {
    window.clearTimeout(noticeClearTimer)
    noticeClearTimer = null
  }
}

const setNotice = (type: 'info' | 'success' | 'error', text: string) => {
  clearNoticeTimers()
  noticeFading.value = false
  notice.type = type
  notice.text = text
  noticeFadeTimer = window.setTimeout(() => {
    noticeFading.value = true
    noticeClearTimer = window.setTimeout(() => {
      notice.type = 'info'
      notice.text = ''
      noticeFading.value = false
      noticeClearTimer = null
    }, NOTICE_FADE_MS)
    noticeFadeTimer = null
  }, NOTICE_VISIBLE_MS)
}

const getErrorMessage = (error: unknown) => {
  return (error as Error)?.message || '请求失败'
}

const toNumber = (value: string): number | undefined => {
  const source = value.trim()
  if (!source) {
    return undefined
  }

  const parsed = Number(source)
  return Number.isFinite(parsed) ? parsed : undefined
}

const queryForm = reactive({
  ownerName: '',
  cleanBalanceType: '',
  pageNum: 1,
  pageSize: 10
})

const cleanBalanceTypeOptions = ref<EnumOption[]>([])

const cancelPage = reactive<CancelRecordPageResult>({
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
})

const loading = ref(false)

const getCleanBalanceTypeLabel = (value?: number) => {
  if (value === undefined || value === null) {
    return '--'
  }
  const target = String(value)
  return cleanBalanceTypeOptions.value.find((item) => item.value === target)?.label || target
}

const getCleanBalanceAmountText = (payload: {
  cleanBalanceAmountText?: string
  cleanBalanceReal?: number
}) => {
  if (typeof payload.cleanBalanceAmountText === 'string' && payload.cleanBalanceAmountText.trim()) {
    return payload.cleanBalanceAmountText.trim()
  }
  if (payload.cleanBalanceReal !== undefined && payload.cleanBalanceReal !== null) {
    return String(payload.cleanBalanceReal)
  }
  return '--'
}

const loadCleanBalanceTypeOptions = async () => {
  try {
    cleanBalanceTypeOptions.value = await fetchEnumOptionsByKey('cleanBalanceType')
  } catch (error) {
    setNotice('error', `结算类型枚举加载失败：${getErrorMessage(error)}`)
  }
}

const loadCancelRecords = async () => {
  loading.value = true
  cancelPage.list = []
  try {
    const result = await fetchCancelRecordPage({
      ownerName: queryForm.ownerName.trim() || undefined,
      cleanBalanceType: toNumber(queryForm.cleanBalanceType),
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })

    cancelPage.list = result.list
    cancelPage.total = result.total
    cancelPage.pageNum = result.pageNum ?? queryForm.pageNum
    cancelPage.pageSize = result.pageSize ?? queryForm.pageSize
    queryForm.pageNum = cancelPage.pageNum
    queryForm.pageSize = cancelPage.pageSize
  } catch (error) {
    setNotice('error', `销户记录加载失败：${getErrorMessage(error)}`)
  } finally {
    loading.value = false
  }
}

const currentPageSize = computed(() => {
  return cancelPage.pageSize || queryForm.pageSize || 10
})

const getSerialNumber = (index: number) => {
  return (queryForm.pageNum - 1) * currentPageSize.value + index + 1
}

const search = async () => {
  queryForm.pageNum = 1
  await loadCancelRecords()
}

const resetQuery = async () => {
  queryForm.ownerName = ''
  queryForm.cleanBalanceType = ''
  queryForm.pageNum = 1
  await loadCancelRecords()
}

const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
  if (loading.value) {
    return
  }
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  await loadCancelRecords()
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const cancelDetail = ref<CancelDetail | null>(null)

const openDetail = async (cancelNo: string | undefined) => {
  if (!cancelNo) {
    setNotice('error', '缺少销户编号')
    return
  }

  detailVisible.value = true
  detailLoading.value = true
  cancelDetail.value = null

  try {
    cancelDetail.value = await fetchCancelDetail(cancelNo)
  } catch (error) {
    setNotice('error', `销户详情加载失败：${getErrorMessage(error)}`)
  } finally {
    detailLoading.value = false
  }
}

const closeDetail = () => {
  detailVisible.value = false
}

onMounted(async () => {
  await loadCleanBalanceTypeOptions()
  await loadCancelRecords()
})

onBeforeUnmount(() => {
  clearNoticeTimers()
})
</script>

<template>
  <div class="page">
    <div
      v-if="notice.text"
      :class="['notice', `notice-${notice.type}`, { 'notice-fade-out': noticeFading }]"
    >
      {{ notice.text }}
    </div>

    <section class="search-card">
      <div class="search-row">
        <label class="search-item">
          <span class="search-label-inline">账户名称</span>
          <input
            class="search-input search-radius-micro"
            v-model="queryForm.ownerName"
            placeholder="请输入账户名称"
          />
        </label>
        <label class="search-item search-item-secondary">
          <span class="search-label-inline">结算类型</span>
          <select
            class="search-input search-radius-micro"
            :class="{ 'search-input-placeholder': !queryForm.cleanBalanceType }"
            v-model="queryForm.cleanBalanceType"
          >
            <option value="">请选择结算类型</option>
            <option
              v-for="option in cleanBalanceTypeOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </label>
        <div class="search-actions">
          <button class="btn btn-primary search-radius-micro" @click="search" :disabled="loading">
            查询
          </button>
          <button
            class="btn btn-secondary search-radius-micro"
            @click="resetQuery"
            :disabled="loading"
            >重置</button
          >
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-toolbar">
        <h2 class="table-title">销户记录</h2>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>销户编号</th>
              <th>账户名称</th>
              <th>销表数量</th>
              <th>结算类型</th>
              <th>结算金额</th>
              <th>操作人</th>
              <th>销户时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="empty">
                <UiLoadingState :size="18" :thickness="2" :min-height="56" />
              </td>
            </tr>
            <tr v-for="(row, index) in cancelPage.list" :key="row.cancelNo ?? `cancel-${index}`">
              <td>{{ getSerialNumber(index) }}</td>
              <td>{{ row.cancelNo || '--' }}</td>
              <td>{{ row.ownerName || '--' }}</td>
              <td>{{ row.electricMeterAmount ?? '--' }}</td>
              <td>{{ getCleanBalanceTypeLabel(row.cleanBalanceType) }}</td>
              <td>{{ getCleanBalanceAmountText(row) }}</td>
              <td>{{ row.operatorName || '--' }}</td>
              <td>{{ row.cancelTime || '--' }}</td>
              <td>
                <button
                  v-menu-permission="cancelRecordPermissionKeys.detail"
                  class="btn-link"
                  @click="openDetail(row.cancelNo)"
                >
                  详情
                </button>
              </td>
            </tr>
            <tr v-if="!loading && cancelPage.list.length === 0">
              <td colspan="9" class="empty">
                <UiEmptyState :min-height="56" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <CommonPagination
        class="pager"
        :total="cancelPage.total"
        :page-num="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :loading="loading"
        @change="handlePageChange"
      />
    </section>

    <Transition name="account-modal-fade" appear>
      <div v-if="detailVisible" class="modal-mask" @click.self="closeDetail">
        <section class="modal">
          <header class="modal-head">
            <h3>销户详情</h3>
            <button class="btn btn-secondary icon-btn" @click="closeDetail">关闭</button>
          </header>

          <div v-if="detailLoading" class="modal-body modal-body-loading">
            <UiLoadingState :size="20" :thickness="2" :min-height="180" />
          </div>
          <div v-else-if="cancelDetail" class="modal-body detail-modal-body">
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label es-detail-label">销户编号</span>
                <span class="detail-value es-detail-value-box">{{
                  cancelDetail.cancelNo || '--'
                }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label es-detail-label">账户名称</span>
                <span class="detail-value es-detail-value-box">{{
                  cancelDetail.ownerName || '--'
                }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label es-detail-label">销表数量</span>
                <span class="detail-value es-detail-value-box">{{
                  cancelDetail.electricMeterAmount ?? '--'
                }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label es-detail-label">结算类型</span>
                <span class="detail-value es-detail-value-box">{{
                  getCleanBalanceTypeLabel(cancelDetail.cleanBalanceType)
                }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label es-detail-label">结算金额</span>
                <span class="detail-value es-detail-value-box">{{
                  getCleanBalanceAmountText(cancelDetail)
                }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label es-detail-label">操作人</span>
                <span class="detail-value es-detail-value-box">{{
                  cancelDetail.operatorName || '--'
                }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label es-detail-label">销户时间</span>
                <span class="detail-value es-detail-value-box">{{
                  cancelDetail.cancelTime || '--'
                }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label es-detail-label">备注</span>
                <span class="detail-value es-detail-value-box">{{
                  cancelDetail.remark || '--'
                }}</span>
              </div>
            </div>

            <div class="table-wrap" v-if="cancelDetail.meterList.length">
              <table>
                <thead>
                  <tr>
                    <th>空间</th>
                    <th>电表名称</th>
                    <th>电表编号</th>
                    <th>表余额</th>
                    <th>总电量</th>
                    <th>读表时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="meter in cancelDetail.meterList"
                    :key="`${meter.deviceNo}-${meter.showTime}`"
                  >
                    <td>{{ meter.spaceName || '--' }}</td>
                    <td>{{ meter.meterName || '--' }}</td>
                    <td>{{ meter.deviceNo || '--' }}</td>
                    <td>{{ meter.balance ?? '--' }}</td>
                    <td>{{ meter.power ?? '--' }}</td>
                    <td>{{ meter.showTime || '--' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div v-else class="modal-body detail-modal-body">
            <UiEmptyState text="暂无详情数据" :min-height="180" />
          </div>
        </section>
      </div>
    </Transition>
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
  border-radius: var(--es-radius-sm);
  opacity: 1;
  transition: opacity 0.3s ease;
}

.notice-fade-out {
  opacity: 0;
}

.notice-info {
  color: var(--es-color-info-text);
  background: var(--es-color-info-bg);
  border-color: var(--es-color-info-border);
}

.notice-success {
  color: var(--es-color-success-text);
  background: var(--es-color-success-bg);
  border-color: var(--es-color-success-border);
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
  border-radius: var(--es-radius-lg);
  box-shadow: var(--es-shadow-card);
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
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
  border-radius: var(--es-radius-sm);
}

.search-radius-micro {
  border-radius: 5px;
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
}

.table-toolbar {
  display: flex;
  align-items: center;
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
  border-radius: var(--es-radius-sm);
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
  overflow: auto;
  border: 1px solid var(--es-color-border);
  border-radius: var(--es-radius-md);
}

table {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
}

th,
td {
  padding: 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  border-bottom: 1px solid var(--es-color-border);
}

th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

tbody tr:hover {
  background: #f8fbff;
}

.empty {
  padding: 16px 0;
  color: var(--es-color-text-placeholder);
  text-align: center;
}

.pager {
  padding: 0 10px;
}

.modal-mask {
  position: fixed;
  z-index: 30;
  display: flex;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
  inset: 0;
  align-items: center;
  justify-content: center;
}

.account-modal-fade-enter-active,
.account-modal-fade-leave-active {
  transition: opacity 0.18s ease-out;
}

.account-modal-fade-enter-from,
.account-modal-fade-leave-to {
  opacity: 0;
}

.account-modal-fade-enter-to,
.account-modal-fade-leave-from {
  opacity: 1;
}

.account-modal-fade-enter-active .modal,
.account-modal-fade-leave-active .modal {
  transition: opacity 0.2s ease-out, transform 0.2s ease-out;
}

.account-modal-fade-enter-from .modal,
.account-modal-fade-leave-to .modal {
  opacity: 0;
  transform: translateY(8px) scale(0.995);
}

.account-modal-fade-enter-to .modal,
.account-modal-fade-leave-from .modal {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.modal {
  position: relative;
  width: min(920px, 100%);
  max-height: 90vh;
  overflow: auto;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 14px;
  box-shadow: var(--es-shadow-floating);
}

.modal-head {
  display: flex;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
  justify-content: space-between;
  align-items: center;
}

.modal-head h3 {
  margin: 0;
  font-size: var(--es-font-size-md);
  color: var(--es-color-text-primary);
}

.icon-btn {
  min-width: 64px;
}

.modal-body {
  padding: 16px;
}

.detail-modal-body {
  padding-bottom: 32px;
}

.modal-body-loading {
  display: flex;
  min-height: 120px;
  align-items: center;
  justify-content: center;
}

.detail-grid {
  display: grid;
  gap: 10px 56px;
  grid-template-columns: repeat(2, minmax(220px, 1fr));
  margin-bottom: 12px;
}

.detail-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.detail-label {
  font-size: var(--es-font-size-sm);
  white-space: nowrap;
}

.detail-value {
  width: 100%;
}

.btn:focus-visible,
.btn-link:focus-visible,
.search-input:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}

@media (width <= 900px) {
  .search-item {
    width: 100%;
    justify-content: space-between;
  }

  .search-item-secondary {
    margin-left: 0;
  }

  .search-input {
    width: 100%;
    max-width: none;
    min-width: 0;
    flex: 1;
  }

  .search-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }

  .pager {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .account-modal-fade-enter-active,
  .account-modal-fade-leave-active,
  .account-modal-fade-enter-active .modal,
  .account-modal-fade-leave-active .modal {
    transition: none;
  }
}
</style>
