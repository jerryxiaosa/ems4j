<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import {
  createGateway,
  fetchGatewayDetail,
  fetchGatewayPage,
  removeGateway,
  updateGateway
} from '@/api/adapters/device'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import DeviceGatewayDetailModal from '@/components/devices/DeviceGatewayDetailModal.vue'
import DeviceGatewayEditModal from '@/components/devices/DeviceGatewayEditModal.vue'
import {
  type GatewayDetailDeviceItem,
  gatewayOnlineStatusOptions,
  type GatewayFormValue,
  type GatewayItem
} from '@/components/devices/gateway.mock'
import type { GatewayDetailItem, GatewayItem as GatewayPageItem } from '@/types/device'

const gatewayPermissionKeys = {
  create: 'device_management_gateway_create',
  detail: 'device_management_gateway_detail',
  edit: 'device_management_gateway_edit',
  delete: 'device_management_gateway_delete'
} as const

const rows = ref<GatewayItem[]>([])
const loading = ref(false)
const total = ref(0)
const queryForm = reactive({
  searchKey: '',
  onlineStatus: '',
  pageNum: 1,
  pageSize: 10
})

const editModalVisible = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const editingGateway = ref<GatewayItem | null>(null)
const detailModalVisible = ref(false)
const detailGateway = ref<GatewayItem | null>(null)
const detailDevices = ref<GatewayDetailDeviceItem[]>([])
const detailLoading = ref(false)

const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})
const noticeFading = ref(false)
let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null
const NOTICE_VISIBLE_MS = 5000
const NOTICE_FADE_MS = 300

const confirmState = reactive({
  visible: false,
  targetId: 0,
  title: '',
  content: ''
})

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

onBeforeUnmount(() => {
  clearNoticeTimers()
})

const normalizeSpaceParentNames = (value: string[] | string | undefined): string[] => {
  if (Array.isArray(value)) {
    return value.map((item) => item.trim()).filter(Boolean)
  }

  if (typeof value === 'string') {
    return value
      .split(/[>/,]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }

  return []
}

const normalizeGatewayRow = (item: {
  id: number
  gatewayName?: string
  deviceNo?: string
  modelId?: number
  modelName?: string
  spaceId?: number
  spaceName?: string
  spaceParentNames?: string[] | string
  communicateModel?: string
  sn?: string
  imei?: string
  configInfo?: string
  isOnline?: boolean
}): GatewayItem => {
  const parentNames = normalizeSpaceParentNames(item.spaceParentNames)
  const spaceName = item.spaceName || '—'
  const spacePath = [...parentNames, spaceName].filter(Boolean).join(' / ') || spaceName
  const onlineStatus = item.isOnline === true ? 1 : item.isOnline === false ? 0 : null

  return {
    id: item.id,
    gatewayName: item.gatewayName || '—',
    deviceNo: item.deviceNo || '—',
    modelId: item.modelId || 0,
    modelName: item.modelName || '—',
    spaceId: item.spaceId != null ? String(item.spaceId) : '',
    spaceName,
    spacePath,
    communicateModel: item.communicateModel || '—',
    sn: item.sn || '',
    imei: item.imei || '',
    configInfo: item.configInfo || '',
    onlineStatus,
    onlineStatusName: onlineStatus === 1 ? '在线' : onlineStatus === 0 ? '离线' : '--',
    deviceAmount: 0
  }
}

const normalizeGatewayDetailDevices = (
  meterList: GatewayDetailItem['meterList'] | undefined
): GatewayDetailDeviceItem[] => {
  if (!Array.isArray(meterList)) {
    return []
  }

  return meterList.map((item) => ({
    id: item.id,
    deviceName: item.meterName || '--',
    deviceType: '智能电表',
    deviceNo: item.deviceNo || '--',
    portNo: item.portNo != null ? String(item.portNo) : '--',
    meterAddress: item.meterAddress != null ? String(item.meterAddress) : '--',
    isOnline: item.isOnline
  }))
}

const pagedRows = computed(() => rows.value)

const getSerialNumber = (index: number) => (queryForm.pageNum - 1) * queryForm.pageSize + index + 1

const getOnlineStatusClass = (row: Pick<GatewayItem, 'onlineStatus'>) => {
  if (row.onlineStatus === 1) {
    return 'gateway-online-status gateway-online-status-online'
  }
  if (row.onlineStatus === 0) {
    return 'gateway-online-status gateway-online-status-offline'
  }
  return 'gateway-online-status gateway-online-status-unknown'
}

const getSpaceRegion = (row: Pick<GatewayItem, 'spacePath' | 'spaceName'>) => {
  const segments = row.spacePath
    .split(' / ')
    .map((item) => item.trim())
    .filter(Boolean)

  if (segments.length <= 1) {
    return row.spacePath || '—'
  }

  if (segments[segments.length - 1] === row.spaceName) {
    return segments.slice(0, -1).join(' > ') || '—'
  }

  return segments.slice(0, -1).join(' > ') || '—'
}

const getSpaceLocation = (row: Pick<GatewayItem, 'spaceName'>) => row.spaceName || '—'

const loadGatewayPage = async () => {
  loading.value = true

  try {
    const page = await fetchGatewayPage({
      searchKey: queryForm.searchKey.trim() || undefined,
      isOnline: queryForm.onlineStatus ? queryForm.onlineStatus === 'true' : undefined,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })

    rows.value = page.list.map(normalizeGatewayRow)
    total.value = page.total
  } catch (error) {
    rows.value = []
    total.value = 0
    setNotice('error', error instanceof Error ? error.message : '智能网关分页查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  void loadGatewayPage()
}

const handleReset = () => {
  queryForm.searchKey = ''
  queryForm.onlineStatus = ''
  queryForm.pageNum = 1
  void loadGatewayPage()
}

const handlePageChange = (payload: { pageNum: number; pageSize: number }) => {
  if (loading.value) {
    return
  }
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  void loadGatewayPage()
}

onMounted(() => {
  void loadGatewayPage()
})

const openCreateModal = () => {
  editMode.value = 'create'
  editingGateway.value = null
  editModalVisible.value = true
}

const openEditModal = (row: GatewayItem) => {
  editMode.value = 'edit'
  editingGateway.value = { ...row }
  editModalVisible.value = true
}

const openDetailModal = async (row: GatewayItem) => {
  detailGateway.value = { ...row }
  detailDevices.value = []
  detailModalVisible.value = true
  detailLoading.value = true

  try {
    const detail = await fetchGatewayDetail(row.id)
    if (!detail) {
      detailDevices.value = []
      return
    }

    detailGateway.value = normalizeGatewayRow(detail)
    detailDevices.value = normalizeGatewayDetailDevices(detail.meterList)
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '智能网关详情查询失败')
  } finally {
    detailLoading.value = false
  }
}

const handleSubmitGateway = async (payload: GatewayFormValue) => {
  if (editMode.value === 'create') {
    try {
      await createGateway({
        spaceId: Number(payload.spaceId),
        gatewayName: payload.gatewayName.trim(),
        modelId: Number(payload.modelId),
        deviceNo: payload.deviceNo.trim(),
        sn: payload.sn.trim() || undefined,
        imei: payload.imei.trim() || undefined,
        configInfo: payload.configInfo.trim()
      })

      editModalVisible.value = false
      queryForm.pageNum = 1
      await loadGatewayPage()
      setNotice('success', '智能网关添加成功')
    } catch (error) {
      setNotice('error', error instanceof Error ? error.message : '智能网关添加失败')
    }
    return
  }

  if (!payload.id) {
    return
  }

  try {
    await updateGateway(payload.id, {
      id: payload.id,
      spaceId: Number(payload.spaceId),
      gatewayName: payload.gatewayName.trim(),
      modelId: Number(payload.modelId),
      deviceNo: payload.deviceNo.trim(),
      sn: payload.sn.trim() || undefined,
      imei: payload.imei.trim() || undefined,
      configInfo: payload.configInfo.trim() || undefined
    })

    editModalVisible.value = false
    await loadGatewayPage()
    setNotice('success', '智能网关信息更新成功')
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '智能网关信息更新失败')
  }
}

const openDeleteConfirm = (row: GatewayItem) => {
  confirmState.visible = true
  confirmState.targetId = row.id
  confirmState.title = '删除确认'
  confirmState.content = `是否删除智能网关“${row.gatewayName}”？`
}

const closeConfirm = () => {
  confirmState.visible = false
  confirmState.targetId = 0
  confirmState.title = ''
  confirmState.content = ''
}

const handleConfirmDelete = async () => {
  try {
    await removeGateway(confirmState.targetId)

    const shouldFallbackPage = rows.value.length <= 1 && queryForm.pageNum > 1
    if (shouldFallbackPage) {
      queryForm.pageNum -= 1
    }

    closeConfirm()
    await loadGatewayPage()
    setNotice('success', '智能网关删除成功')
  } catch (error) {
    closeConfirm()
    setNotice('error', error instanceof Error ? error.message : '智能网关删除失败')
  }
}
</script>

<template>
  <div class="page">
    <transition name="page-notice-fade">
      <div
        v-if="notice.text"
        :class="['page-notice', `page-notice-${notice.type}`, { 'is-fading': noticeFading }]"
      >
        {{ notice.text }}
      </div>
    </transition>

    <section class="workspace-card">
      <header class="workspace-search">
        <div class="search-row">
          <label class="search-item">
            <span class="search-label-inline">网关名称/编号</span>
            <input
              v-model="queryForm.searchKey"
              class="search-input"
              type="text"
              placeholder="请输入网关名称/编号"
            />
          </label>

          <label class="search-item search-item-secondary">
            <span class="search-label-inline">在线状态</span>
            <select
              v-model="queryForm.onlineStatus"
              class="search-input search-input-select"
              :class="{ 'search-input-placeholder': !queryForm.onlineStatus }"
            >
              <option value="">请选择</option>
              <option
                v-for="item in gatewayOnlineStatusOptions"
                :key="item.value"
                :value="item.value"
                >{{ item.label }}</option
              >
            </select>
          </label>

          <div class="search-actions">
            <button class="btn btn-primary" type="button" @click="handleSearch">查询</button>
            <button class="btn btn-secondary" type="button" @click="handleReset">重置</button>
          </div>
        </div>
      </header>

      <div class="workspace-body">
        <div class="workspace-head">
          <h2 class="workspace-title">智能网关</h2>
          <div class="page-actions">
            <button
              v-menu-permission="gatewayPermissionKeys.create"
              class="btn btn-primary"
              type="button"
              @click="openCreateModal"
            >
              添加
            </button>
          </div>
        </div>

        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th class="table-col-index sticky-col sticky-col-left sticky-index">序号</th>
                <th class="gateway-col-name sticky-col sticky-col-left sticky-name">网关名称</th>
                <th class="gateway-col-model sticky-col sticky-col-left sticky-model">网关型号</th>
                <th class="gateway-col-device-no sticky-col sticky-col-left sticky-device-no"
                  >网关编号</th
                >
                <th class="gateway-col-location">所在位置</th>
                <th class="gateway-col-region">所属区域</th>
                <th class="gateway-col-sn">网关SN</th>
                <th class="gateway-col-imei">IMEI</th>
                <th class="gateway-col-online sticky-col sticky-col-right sticky-online"
                  >在线状态</th
                >
                <th class="gateway-col-action sticky-col sticky-col-right sticky-action">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="10" class="empty-row">
                  <UiLoadingState :size="18" :thickness="2" :min-height="56" />
                </td>
              </tr>
              <tr v-else-if="!pagedRows.length">
                <td colspan="10" class="empty-row">
                  <UiEmptyState :min-height="56" />
                </td>
              </tr>
              <tr v-for="(row, index) in pagedRows" :key="row.id">
                <td class="sticky-col sticky-col-left sticky-index">{{
                  getSerialNumber(index)
                }}</td>
                <td class="sticky-col sticky-col-left sticky-name">{{ row.gatewayName }}</td>
                <td class="sticky-col sticky-col-left sticky-model">{{ row.modelName }}</td>
                <td class="sticky-col sticky-col-left sticky-device-no">{{ row.deviceNo }}</td>
                <td>{{ getSpaceLocation(row) }}</td>
                <td>{{ getSpaceRegion(row) }}</td>
                <td>{{ row.sn || '—' }}</td>
                <td>{{ row.imei || '—' }}</td>
                <td class="sticky-col sticky-col-right sticky-online">
                  <span :class="getOnlineStatusClass(row)">
                    {{ row.onlineStatusName }}
                  </span>
                </td>
                <td class="gateway-col-action sticky-col sticky-col-right sticky-action">
                  <button
                    v-menu-permission="gatewayPermissionKeys.detail"
                    class="btn-link"
                    type="button"
                    @click="openDetailModal(row)"
                  >
                    详情
                  </button>
                  <button
                    v-menu-permission="gatewayPermissionKeys.edit"
                    class="btn-link"
                    type="button"
                    @click="openEditModal(row)"
                  >
                    编辑
                  </button>
                  <button
                    v-menu-permission="gatewayPermissionKeys.delete"
                    class="btn-link btn-link-danger"
                    type="button"
                    @click="openDeleteConfirm(row)"
                    >删除</button
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>
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

    <DeviceGatewayEditModal
      v-model="editModalVisible"
      :mode="editMode"
      :gateway="editingGateway"
      @submit="handleSubmitGateway"
    />

    <DeviceGatewayDetailModal
      v-model="detailModalVisible"
      :gateway="detailGateway"
      :devices="detailDevices"
      :loading="detailLoading"
    />

    <Transition name="gateway-modal-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="modal-panel confirm-modal">
          <div class="modal-head">
            <h3 class="modal-title">{{ confirmState.title }}</h3>
            <button class="icon-btn" type="button" @click="closeConfirm">关闭</button>
          </div>
          <div class="modal-body">
            <div class="confirm-summary confirm-summary-error">
              <p class="confirm-content">{{ confirmState.content }}</p>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" type="button" @click="closeConfirm">取消</button>
            <button class="btn btn-primary" type="button" @click="handleConfirmDelete">确定</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.page {
  position: relative;
}

.page-notice {
  position: fixed;
  top: 86px;
  left: 50%;
  z-index: 60;
  max-width: min(520px, calc(100vw - 32px));
  min-width: 260px;
  padding: 12px 16px;
  font-size: var(--es-font-size-sm);
  text-align: center;
  border: 1px solid transparent;
  border-radius: 5px;
  transform: translateX(-50%);
  box-shadow: 0 16px 40px rgb(15 23 42 / 14%);
  transition: opacity 0.3s ease;
}

.page-notice.is-fading {
  opacity: 0;
}

.page-notice-info {
  color: var(--es-color-info-text);
  background: var(--es-color-info-bg);
  border-color: var(--es-color-info-border);
}

.page-notice-success {
  color: var(--es-color-success-text);
  background: var(--es-color-success-bg);
  border-color: var(--es-color-success-border);
}

.page-notice-error {
  color: var(--es-color-error-text);
  background: var(--es-color-error-bg);
  border-color: var(--es-color-error-border);
}

.page-notice-fade-enter-active,
.page-notice-fade-leave-active {
  transition: opacity 0.2s ease;
}

.page-notice-fade-enter-from,
.page-notice-fade-leave-to {
  opacity: 0;
}

.workspace-card {
  display: grid;
  min-height: calc(100vh - 146px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.workspace-search {
  padding: 16px;
  border-bottom: 1px solid var(--es-color-border);
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
  min-width: 0;
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
  border-radius: 5px;
}

.search-input-select {
  width: 120px;
  max-width: 120px;
  min-width: 120px;
}

.search-input::placeholder {
  color: var(--es-color-text-placeholder);
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
  flex-shrink: 0;
}

.workspace-body {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-height: 0;
  padding: 16px;
}

.workspace-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.workspace-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn,
.icon-btn {
  display: inline-flex;
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.icon-btn,
.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-primary:hover {
  background: var(--es-color-primary-hover);
  border-color: var(--es-color-primary-hover);
}

.table-wrap {
  min-height: 0;
  overflow: scroll auto;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  scrollbar-gutter: stable;
}

.table-wrap::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.table-wrap::-webkit-scrollbar-track {
  background: #eef3fb;
  border-radius: 999px;
}

.table-wrap::-webkit-scrollbar-thumb {
  background: #c3d4ee;
  border-radius: 999px;
}

.table-wrap:hover::-webkit-scrollbar-thumb {
  background: #9fb8df;
}

.table {
  width: max-content;
  min-width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.table th,
.table td {
  padding: 12px 14px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  vertical-align: middle;
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

.gateway-col-name {
  width: 180px;
}

.gateway-col-model {
  width: 180px;
}

.gateway-col-device-no {
  width: 160px;
}

.gateway-col-location {
  width: 140px;
}

.gateway-col-region {
  width: 220px;
}

.gateway-col-sn {
  width: 180px;
}

.gateway-col-imei {
  width: 180px;
}

.gateway-col-online {
  width: 100px;
}

.gateway-col-action {
  width: 160px;
}

.sticky-col {
  position: sticky;
  z-index: 2;
  background: #fff;
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
  z-index: 4;
}

.sticky-name {
  left: 84px;
  z-index: 4;
}

.sticky-model {
  left: 264px;
  z-index: 4;
}

.sticky-device-no {
  left: 444px;
  z-index: 4;
}

.sticky-online {
  right: 160px;
  z-index: 4;
}

.sticky-action {
  right: 0;
  z-index: 4;
}

.table tbody tr:hover td {
  background: #fafcff;
}

.table tbody tr:hover .sticky-col {
  background: #fafcff;
}

.empty-row {
  padding: 16px 0;
  color: var(--es-color-text-placeholder) !important;
  text-align: center !important;
}

.gateway-online-status {
  font-weight: 600;
}

.gateway-online-status-online {
  color: var(--es-color-success-text);
}

.gateway-online-status-offline {
  color: var(--es-color-error-text);
}

.gateway-online-status-unknown {
  font-weight: 500;
  color: var(--es-color-text-placeholder);
}

.btn-link {
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  line-height: 1.2;
  color: var(--es-color-primary);
  cursor: pointer;
  background: transparent;
  border: 0;
}

.btn-link + .btn-link {
  margin-left: 12px;
}

.btn-link-danger {
  color: var(--es-color-error-text);
}

.btn-link:hover {
  color: var(--es-color-primary-hover);
}

.btn-link-danger:hover {
  opacity: 0.85;
}

.pager {
  padding: 16px;
  border-top: 1px solid var(--es-color-border);
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

.confirm-modal {
  width: min(420px, calc(100vw - 32px));
  background: var(--es-color-bg-elevated);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
}

.modal-head,
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--es-color-border);
}

.modal-actions {
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid var(--es-color-border);
  border-bottom: 0;
}

.modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.modal-body {
  padding: 20px;
}

.confirm-summary {
  padding: 14px 16px;
  border: 1px solid transparent;
  border-radius: 5px;
}

.confirm-summary-error {
  color: var(--es-color-error-text);
  background: var(--es-color-error-bg);
  border-color: var(--es-color-error-border);
}

.confirm-content {
  margin: 0;
  font-size: var(--es-font-size-sm);
}

.gateway-modal-fade-enter-active,
.gateway-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.gateway-modal-fade-enter-from,
.gateway-modal-fade-leave-to {
  opacity: 0;
}

.gateway-modal-fade-enter-from .confirm-modal,
.gateway-modal-fade-leave-to .confirm-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
