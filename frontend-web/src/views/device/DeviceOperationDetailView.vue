<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchDeviceOperationDetail,
  fetchDeviceOperationExecuteRecordList,
  retryDeviceOperation
} from '@/api/adapters/device-operation'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type {
  DeviceOperationDetail,
  DeviceOperationExecuteRecord
} from '@/types/device-operation'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const errorMessage = ref('')
const detail = ref<DeviceOperationDetail | null>(null)
const retrying = ref(false)
const executeRecords = ref<DeviceOperationExecuteRecord[]>([])
const executeRecordModalVisible = ref(false)
const executeRecordLoading = ref(false)
const executeRecordErrorMessage = ref('')
const executeRecordLoaded = ref(false)
const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})

const detailFields = computed(() => {
  const currentDetail = detail.value
  if (!currentDetail) {
    return []
  }
  return [
    { label: '设备编号', value: currentDetail.deviceNo, type: 'text' },
    { label: '设备名称', value: currentDetail.deviceName, type: 'text' },
    { label: '设备类型', value: currentDetail.deviceTypeName, type: 'text' },
    { label: '空间名称', value: currentDetail.spaceName, type: 'text' },
    { label: '操作类型', value: currentDetail.commandTypeName, type: 'text' },
    { label: '操作人员', value: currentDetail.operateUserName, type: 'text' },
    { label: '操作时间', value: currentDetail.createTime, type: 'text' },
    { label: '命令来源', value: currentDetail.commandSourceName, type: 'text' },
    {
      label: '确保成功',
      value: currentDetail.ensureSuccess === true ? '是' : '否',
      type: 'text'
    },
    { label: '成功时间', value: currentDetail.successTime, type: 'text' },
    { label: '最后执行时间', value: currentDetail.lastExecuteTime, type: 'text' },
    {
      label: '执行状态',
      value: currentDetail.isRunning === true ? '执行中' : '已执行',
      type: 'executeStatus'
    },
    { label: '操作状态', value: currentDetail.successName, type: 'status' },
    {
      label: '执行次数',
      value: currentDetail.executeTimes ?? '--',
      type: 'executeTimes',
      clickable: currentDetail.executeTimes !== null && currentDetail.executeTimes !== undefined
    },
    { label: '备注', value: currentDetail.remark, type: 'text', fullWidth: true }
  ]
})

const retryDisabledReason = computed(() => {
  const currentDetail = detail.value
  if (!currentDetail) {
    return '暂无可重试的设备操作'
  }
  if (currentDetail.success === true) {
    return '当前操作已成功'
  }
  if (currentDetail.isRunning === true) {
    return '当前操作正在执行中'
  }
  if (
    currentDetail.executeTimes !== undefined &&
    currentDetail.executeTimes !== null &&
    currentDetail.maxExecuteTimes !== undefined &&
    currentDetail.maxExecuteTimes !== null &&
    currentDetail.executeTimes >= currentDetail.maxExecuteTimes
  ) {
    return '当前操作已达到重试上限'
  }
  if (currentDetail.success !== false) {
    return '当前状态不可重试'
  }
  return ''
})

const retryButtonText = computed(() => {
  const currentDetail = detail.value
  if (retrying.value) {
    return '重试中'
  }
  if (!currentDetail) {
    return '重试'
  }
  if (currentDetail.success === true) {
    return '已成功'
  }
  if (currentDetail.isRunning === true) {
    return '执行中'
  }
  if (
    currentDetail.executeTimes !== undefined &&
    currentDetail.executeTimes !== null &&
    currentDetail.maxExecuteTimes !== undefined &&
    currentDetail.maxExecuteTimes !== null &&
    currentDetail.executeTimes >= currentDetail.maxExecuteTimes
  ) {
    return '已达上限'
  }
  return '重试'
})

const commandDataPretty = computed(() => {
  const source = detail.value?.commandData || ''
  if (!source || source === '--') {
    return '--'
  }
  try {
    return JSON.stringify(JSON.parse(source), null, 2)
  } catch {
    return source
  }
})

const parseIdFromRoute = (): number | null => {
  const id = route.query.id
  const idValue = Array.isArray(id) ? id[0] : id
  if (!idValue) {
    return null
  }
  const parsed = Number(idValue)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

const getStatusTagClass = (value: boolean | undefined) => {
  if (value === true) {
    return 'status-tag-success'
  }
  if (value === false) {
    return 'status-tag-failed'
  }
  return 'status-tag-pending'
}

const getDetailStatusClass = (value: boolean | undefined) => {
  if (value === true) {
    return 'detail-value-status-success'
  }
  if (value === false) {
    return 'detail-value-status-failed'
  }
  return 'detail-value-status-pending'
}

const getExecuteStatusClass = (value: boolean | undefined) => {
  if (value === true) {
    return 'detail-value-status-running'
  }
  return 'detail-value-status-idle'
}

const closeExecuteRecordModal = () => {
  executeRecordModalVisible.value = false
}

const loadExecuteRecords = async (id: number) => {
  executeRecordLoading.value = true
  executeRecordErrorMessage.value = ''
  try {
    executeRecords.value = await fetchDeviceOperationExecuteRecordList(id)
    executeRecordLoaded.value = true
  } catch (error) {
    executeRecords.value = []
    executeRecordErrorMessage.value = (error as Error)?.message || '执行记录加载失败'
  } finally {
    executeRecordLoading.value = false
  }
}

const openExecuteRecordModal = async () => {
  const id = detail.value?.id
  if (!id) {
    return
  }
  executeRecordModalVisible.value = true
  if (executeRecordLoaded.value || executeRecordLoading.value) {
    return
  }
  await loadExecuteRecords(id)
}

const loadDetail = async () => {
  const id = parseIdFromRoute()
  if (!id) {
    errorMessage.value = '缺少有效的操作记录 ID'
    detail.value = null
    executeRecords.value = []
    return
  }

  loading.value = true
  errorMessage.value = ''
  detail.value = null
  executeRecords.value = []
  executeRecordModalVisible.value = false
  executeRecordErrorMessage.value = ''
  executeRecordLoaded.value = false
  try {
    detail.value = await fetchDeviceOperationDetail(id)
  } catch (error) {
    errorMessage.value = (error as Error)?.message || '设备操作详情加载失败'
  } finally {
    loading.value = false
  }
}

const handleRetry = async () => {
  const id = detail.value?.id
  if (!id || retrying.value || retryDisabledReason.value) {
    return
  }

  retrying.value = true
  try {
    await retryDeviceOperation(id)
    notice.type = 'success'
    notice.text = '设备操作重试已提交'
    await loadDetail()
  } catch (error) {
    notice.type = 'error'
    notice.text = (error as Error)?.message || '设备操作重试失败'
  } finally {
    retrying.value = false
  }
}

const goBack = () => {
  router.push('/device-operations')
}

onMounted(async () => {
  await loadDetail()
})
</script>

<template>
  <div class="page">
    <div v-if="notice.text" :class="['notice', `notice-${notice.type}`]">
      {{ notice.text }}
    </div>

    <header class="page-header">
      <h2 class="page-title">设备操作详情</h2>
      <div class="page-actions">
        <button
          class="btn btn-primary"
          :disabled="!!retryDisabledReason || retrying"
          :title="retryDisabledReason || '重试设备操作'"
          @click="handleRetry"
        >
          {{ retryButtonText }}
        </button>
        <button class="btn btn-secondary" @click="goBack">返回列表</button>
      </div>
    </header>

    <section v-if="loading" class="section-card">
      <UiLoadingState :size="22" :thickness="2" :min-height="180" />
    </section>

    <section v-else-if="errorMessage" class="section-card">
      <UiEmptyState :text="errorMessage" :min-height="180" />
    </section>

    <template v-else-if="detail">
      <section class="section-card">
        <h3 class="section-title">基本信息</h3>
        <div class="detail-grid">
          <div
            v-for="field in detailFields"
            :key="field.label"
            :class="['detail-item', { 'detail-item-full': field.fullWidth }]"
          >
            <span class="detail-label es-detail-label">{{ field.label }}</span>
            <span
              v-if="field.type === 'status'"
              data-test="operation-status"
              class="detail-value es-detail-value-box detail-value-status"
              :class="getDetailStatusClass(detail?.success)"
            >
              {{ field.value }}
            </span>
            <span
              v-else-if="field.type === 'executeStatus'"
              class="detail-value es-detail-value-box detail-value-status"
              :class="getExecuteStatusClass(detail?.isRunning)"
            >
              {{ field.value }}
            </span>
            <button
              v-else-if="field.type === 'executeTimes' && field.clickable"
              data-test="execute-times-button"
              type="button"
              class="detail-value es-detail-value-box detail-value-button detail-value-button-full"
              @click="openExecuteRecordModal"
            >
              {{ field.value }}次
            </button>
            <span v-else class="detail-value es-detail-value-box">{{ field.value }}</span>
          </div>
        </div>
      </section>

      <section class="section-card">
        <h3 class="section-title">指令详情</h3>
        <pre class="command-box">{{ commandDataPretty }}</pre>
      </section>
    </template>

    <section v-else class="section-card">
      <UiEmptyState text="暂无详情数据" :min-height="180" />
    </section>

    <Transition name="record-modal-fade" appear>
      <div
        v-if="executeRecordModalVisible"
        data-test="execute-record-modal"
        class="modal-mask"
        @click.self="closeExecuteRecordModal"
      >
        <div class="modal-panel">
          <div class="modal-head">
            <h3 class="modal-title">执行/重试记录</h3>
            <button class="btn btn-secondary" type="button" @click="closeExecuteRecordModal">
              关闭
            </button>
          </div>
          <div class="modal-body">
            <div class="table-wrap modal-table-wrap">
              <table data-test="execute-record-table">
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>执行时间</th>
                    <th>下发方式</th>
                    <th>下发结果</th>
                    <th>失败原因</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="executeRecordLoading">
                    <td colspan="5" class="empty">
                      <UiLoadingState :size="18" :thickness="2" :min-height="56" />
                    </td>
                  </tr>
                  <tr v-else-if="executeRecordErrorMessage">
                    <td colspan="5" class="empty">
                      <UiEmptyState :text="executeRecordErrorMessage" :min-height="56" />
                    </td>
                  </tr>
                  <tr v-else-if="executeRecords.length === 0">
                    <td colspan="5" class="empty">
                      <UiEmptyState text="暂无执行记录" :min-height="56" />
                    </td>
                  </tr>
                  <tr v-for="(row, index) in executeRecords" :key="`${row.id}-${index}`">
                    <td>{{ index + 1 }}</td>
                    <td>{{ row.runTime }}</td>
                    <td>{{ row.commandSourceName }}</td>
                    <td>
                      <span class="status-tag" :class="getStatusTagClass(row.success)">
                        {{ row.successName }}
                      </span>
                    </td>
                    <td>{{ row.reason }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
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
  padding: 8px 12px;
  font-size: var(--es-font-size-md);
  border: 1px solid transparent;
  border-radius: var(--es-radius-sm);
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

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-actions {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.section-card {
  padding: 16px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: var(--es-radius-lg);
  box-shadow: var(--es-shadow-card);
}

.section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 56px;
}

.detail-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.detail-item-full {
  grid-column: 1 / -1;
}

.detail-value-status,
.detail-value-button {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 12px;
  background: transparent;
}

.detail-value-status {
  width: 100%;
}

.detail-value-button {
  font-size: var(--es-font-size-sm);
  color: #2563eb;
  text-decoration: underline;
  border: 1px solid var(--es-color-border);
  cursor: pointer;
}

.detail-value-button-full {
  width: 100%;
  justify-content: flex-start;
}

.detail-value-button:hover {
  color: #1d4ed8;
  border-color: #93c5fd;
}

.detail-value-button:focus-visible {
  outline: 2px solid #93c5fd;
  outline-offset: 2px;
  border-radius: 4px;
}

.detail-value-status-success {
  color: var(--es-color-success-text);
}

.detail-value-status-failed {
  color: var(--es-color-error-text);
}

.detail-value-status-pending {
  color: var(--es-color-text-secondary);
}

.detail-value-status-running {
  color: #0f766e;
}

.detail-value-status-idle {
  color: var(--es-color-text-secondary);
}

.detail-value {
  width: 100%;
  min-height: 36px;
}

.detail-label {
  min-width: 0;
}

.command-box {
  margin: 0;
  min-height: 100px;
  padding: 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8fbff;
  border: 1px solid var(--es-color-border);
  border-radius: var(--es-radius-md);
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .detail-item {
    grid-template-columns: 88px minmax(0, 1fr);
  }
}

.table-wrap {
  overflow: auto;
  border: 1px solid var(--es-color-border);
  border-radius: var(--es-radius-md);
}

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
  width: min(920px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: var(--es-radius-lg);
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

.modal-body {
  padding: 16px;
  overflow: auto;
}

.modal-table-wrap {
  min-height: 160px;
}

table {
  width: 100%;
  min-width: 680px;
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

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
  border-color: var(--es-color-primary);
}

.btn-primary:hover {
  background: var(--es-color-primary-hover);
  border-color: var(--es-color-primary-hover);
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn:focus-visible {
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

@media (min-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
