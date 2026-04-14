<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import { fetchDeviceOperationPage, retryDeviceOperation } from '@/api/adapters/device-operation'
import CommonPagination from '@/components/common/CommonPagination.vue'
import DeviceOperationDetailModal from '@/components/devices/DeviceOperationDetailModal.vue'
import UiTableStateOverlay from '@/components/common/UiTableStateOverlay.vue'
import type { DeviceOperationItem, DeviceOperationPageResult } from '@/types/device-operation'
import {
  loadDeviceOperationFilterOptions,
  resolveDeviceTypeQueryValue
} from '@/views/device/device-operation-filter'

const commandTypeOptions = ref<EnumOption[]>([])
const deviceTypeOptions = ref<EnumOption[]>([])

const statusOptions = [
  { value: 'success', label: '成功' },
  { value: 'failed', label: '失败' }
]

const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})
const noticeFading = ref(false)
let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null
const NOTICE_VISIBLE_MS = 5000
const NOTICE_FADE_MS = 300

const queryForm = reactive({
  operateUserName: '',
  commandType: '',
  success: '',
  deviceType: '',
  deviceNo: '',
  deviceName: '',
  spaceName: '',
  pageNum: 1,
  pageSize: 10
})

const appliedFilters = reactive({
  operateUserName: '',
  commandType: '',
  success: '',
  deviceType: '',
  deviceNo: '',
  deviceName: '',
  spaceName: ''
})

const operationPage = reactive<DeviceOperationPageResult>({
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
})
const loading = ref(false)
const retryingIds = ref<number[]>([])
const detailModalVisible = ref(false)
const currentDetailOperationId = ref<number | null>(null)

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

const toNumber = (value: string): number | undefined => {
  const source = value.trim()
  if (!source) {
    return undefined
  }
  const parsed = Number(source)
  return Number.isFinite(parsed) ? parsed : undefined
}

const parseSuccess = (value: string): boolean | undefined => {
  if (value === 'success') {
    return true
  }
  if (value === 'failed') {
    return false
  }
  return undefined
}

const getErrorMessage = (error: unknown) => {
  return (error as Error)?.message || '请求失败'
}

const loadFilterOptions = async () => {
  try {
    const options = await loadDeviceOperationFilterOptions(fetchEnumOptionsByKey)
    commandTypeOptions.value = options.commandTypeOptions
    deviceTypeOptions.value = options.deviceTypeOptions
  } catch (error) {
    setNotice('error', `设备操作筛选项加载失败：${getErrorMessage(error)}`)
  }
}

const loadOperationPage = async () => {
  loading.value = true
  operationPage.list = []
  try {
    const result = await fetchDeviceOperationPage({
      operateUserName: appliedFilters.operateUserName.trim() || undefined,
      commandType: toNumber(appliedFilters.commandType),
      success: parseSuccess(appliedFilters.success),
      deviceType: resolveDeviceTypeQueryValue(appliedFilters.deviceType),
      deviceNo: appliedFilters.deviceNo.trim() || undefined,
      deviceName: appliedFilters.deviceName.trim() || undefined,
      spaceName: appliedFilters.spaceName.trim() || undefined,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    operationPage.list = result.list
    operationPage.total = result.total
    operationPage.pageNum = result.pageNum ?? queryForm.pageNum
    operationPage.pageSize = result.pageSize ?? queryForm.pageSize
    queryForm.pageNum = operationPage.pageNum
    queryForm.pageSize = operationPage.pageSize
  } catch (error) {
    setNotice('error', `设备操作记录加载失败：${getErrorMessage(error)}`)
  } finally {
    loading.value = false
  }
}

const currentPageSize = computed(() => {
  return operationPage.pageSize || queryForm.pageSize || 10
})

const getSerialNumber = (index: number) => {
  return (queryForm.pageNum - 1) * currentPageSize.value + index + 1
}

const search = async () => {
  appliedFilters.operateUserName = queryForm.operateUserName
  appliedFilters.commandType = queryForm.commandType
  appliedFilters.success = queryForm.success
  appliedFilters.deviceType = queryForm.deviceType
  appliedFilters.deviceNo = queryForm.deviceNo
  appliedFilters.deviceName = queryForm.deviceName
  appliedFilters.spaceName = queryForm.spaceName
  queryForm.pageNum = 1
  await loadOperationPage()
}

const resetQuery = async () => {
  queryForm.operateUserName = ''
  queryForm.commandType = ''
  queryForm.success = ''
  queryForm.deviceType = ''
  queryForm.deviceNo = ''
  queryForm.deviceName = ''
  queryForm.spaceName = ''
  queryForm.pageNum = 1

  appliedFilters.operateUserName = ''
  appliedFilters.commandType = ''
  appliedFilters.success = ''
  appliedFilters.deviceType = ''
  appliedFilters.deviceNo = ''
  appliedFilters.deviceName = ''
  appliedFilters.spaceName = ''
  await loadOperationPage()
}

const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
  if (loading.value) {
    return
  }
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  await loadOperationPage()
}

const openDetail = (row: DeviceOperationItem) => {
  currentDetailOperationId.value = row.id
  detailModalVisible.value = true
}

const closeDetailModal = () => {
  detailModalVisible.value = false
}

const handleDetailRetried = async () => {
  await loadOperationPage()
}

const isRetrying = (id: number) => {
  return retryingIds.value.includes(id)
}

const getRetryDisabledReason = (row: DeviceOperationItem) => {
  if (row.success === true) {
    return '当前操作已成功'
  }
  if (row.isRunning === true) {
    return '当前操作正在执行中'
  }
  if (row.ensureSuccess !== true) {
    return '当前命令不支持重试'
  }
  if (
    row.executeTimes !== undefined &&
    row.executeTimes !== null &&
    row.maxExecuteTimes !== undefined &&
    row.maxExecuteTimes !== null &&
    row.executeTimes >= row.maxExecuteTimes
  ) {
    return '当前操作已达到重试上限'
  }
  if (row.success !== false) {
    return '当前状态不可重试'
  }
  return ''
}

const getRetryButtonText = (row: DeviceOperationItem) => {
  if (isRetrying(row.id)) {
    return '重试中'
  }
  return '重试'
}

const shouldShowRetryAction = (row: DeviceOperationItem) => {
  return isRetrying(row.id) || !getRetryDisabledReason(row)
}

const handleRetry = async (row: DeviceOperationItem) => {
  const disabledReason = getRetryDisabledReason(row)
  if (disabledReason || isRetrying(row.id)) {
    return
  }

  retryingIds.value = [...retryingIds.value, row.id]
  try {
    await retryDeviceOperation(row.id)
    setNotice('success', '设备操作重试已提交')
    await loadOperationPage()
  } catch (error) {
    setNotice('error', `设备操作重试失败：${getErrorMessage(error)}`)
  } finally {
    retryingIds.value = retryingIds.value.filter((id) => id !== row.id)
  }
}

onMounted(async () => {
  await loadFilterOptions()
  await loadOperationPage()
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
          <span class="search-label-inline">操作人员</span>
          <input
            v-model="queryForm.operateUserName"
            class="search-input search-radius-micro"
            placeholder="请输入操作人员"
          />
        </label>
        <label class="search-item">
          <span class="search-label-inline">操作类型</span>
          <select
            v-model="queryForm.commandType"
            class="search-input search-radius-micro"
            :class="{ 'search-input-placeholder': !queryForm.commandType }"
          >
            <option value="">请选择操作类型</option>
            <option v-for="item in commandTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>
        <label class="search-item">
          <span class="search-label-inline">操作状态</span>
          <select
            v-model="queryForm.success"
            class="search-input search-radius-micro"
            :class="{ 'search-input-placeholder': !queryForm.success }"
          >
            <option value="">请选择操作状态</option>
            <option v-for="item in statusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>
        <label class="search-item">
          <span class="search-label-inline">设备类型</span>
          <select
            v-model="queryForm.deviceType"
            class="search-input search-radius-micro"
            :class="{ 'search-input-placeholder': !queryForm.deviceType }"
          >
            <option value="">请选择设备类型</option>
            <option v-for="item in deviceTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>
        <label class="search-item">
          <span class="search-label-inline">设备编号</span>
          <input
            v-model="queryForm.deviceNo"
            class="search-input search-radius-micro"
            placeholder="请输入设备编号"
          />
        </label>
        <label class="search-item">
          <span class="search-label-inline">设备名称</span>
          <input
            v-model="queryForm.deviceName"
            class="search-input search-radius-micro"
            placeholder="请输入设备名称"
          />
        </label>
        <label class="search-item">
          <span class="search-label-inline">空间名称</span>
          <input
            v-model="queryForm.spaceName"
            class="search-input search-radius-micro"
            placeholder="请输入空间名称"
          />
        </label>
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
        <h2 class="table-title">设备操作记录</h2>
      </div>
      <div class="table-wrap">
        <UiTableStateOverlay
          :loading="loading"
          :empty="!loading && operationPage.list.length === 0"
          empty-text="暂无设备操作记录"
        />
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>设备编号</th>
              <th>设备名称</th>
              <th>设备类型</th>
              <th>空间名称</th>
              <th>操作类型</th>
              <th>操作状态</th>
              <th>操作人员</th>
              <th>操作时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in operationPage.list" :key="`${row.id}-${index}`">
              <td>{{ getSerialNumber(index) }}</td>
              <td>{{ row.deviceNo }}</td>
              <td>{{ row.deviceName }}</td>
              <td>{{ row.deviceTypeName }}</td>
              <td>{{ row.spaceName }}</td>
              <td>{{ row.commandTypeName }}</td>
              <td>
                <span
                  class="status-tag"
                  :class="{
                    'status-tag-success': row.success === true,
                    'status-tag-failed': row.success === false,
                    'status-tag-pending': row.success === undefined
                  }"
                >
                  {{ row.successName }}
                </span>
              </td>
              <td>{{ row.operateUserName }}</td>
              <td>{{ row.createTime }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn-link" @click="openDetail(row)">详情</button>
                  <button
                    v-if="shouldShowRetryAction(row)"
                    class="btn-link"
                    :class="{ 'btn-link-disabled': isRetrying(row.id) }"
                    :disabled="isRetrying(row.id)"
                    :title="isRetrying(row.id) ? '设备操作重试提交中' : '重试设备操作'"
                    @click="handleRetry(row)"
                  >
                    {{ getRetryButtonText(row) }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <CommonPagination
        class="pager"
        :total="operationPage.total"
        :page-num="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :loading="loading"
        @change="handlePageChange"
      />
    </section>

    <DeviceOperationDetailModal
      v-model="detailModalVisible"
      :operation-id="currentDetailOperationId"
      @retried="handleDetailRetried"
    />
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.notice {
  padding: 8px 12px;
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
}

.search-label-inline {
  flex-shrink: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-secondary);
}

.search-input {
  width: 172px;
  height: 36px;
  min-width: 172px;
  max-width: 172px;
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
  margin-bottom: 12px;
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
  align-items: center;
  justify-content: center;
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
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

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
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

.btn-link-disabled,
.btn-link:disabled {
  color: var(--es-color-text-placeholder);
  cursor: not-allowed;
  pointer-events: none;
}

.action-buttons {
  display: inline-flex;
  gap: 12px;
  align-items: center;
}

.btn-link:focus-visible,
.btn:focus-visible,
.search-input:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 1px;
}

.status-tag {
  display: inline-flex;
  min-width: 56px;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.6;
}

.status-tag-success {
  color: var(--es-color-success-text);
  background: var(--es-color-success-bg);
}

.status-tag-failed {
  color: var(--es-color-error-text);
  background: var(--es-color-error-bg);
}

.status-tag-pending {
  color: var(--es-color-text-secondary);
  background: #f1f5f9;
}

.table-wrap {
  position: relative;
  min-height: 120px;
  overflow: auto;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: var(--es-radius-md);
}

table {
  width: 100%;
  min-width: 1120px;
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

.empty {
  text-align: center;
}

.pager {
  margin-top: 12px;
}

@media (max-width: 768px) {
  .search-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-start;
  }
}
</style>
