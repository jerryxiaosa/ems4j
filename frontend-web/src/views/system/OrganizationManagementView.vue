<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import {
  createOrganization,
  deleteOrganization,
  fetchOrganizationDetail,
  fetchOrganizationPage,
  updateOrganization
} from '@/api/adapters/organization-manage'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiTableStateOverlay from '@/components/common/UiTableStateOverlay.vue'
import OrganizationDetailModal from '@/components/system/OrganizationDetailModal.vue'
import OrganizationFormModal from '@/components/system/OrganizationFormModal.vue'
import type { SystemOrganizationFormValue, SystemOrganizationItem } from '@/modules/system/organizations/types'

const DEFAULT_PAGE_SIZE = 10
const organizationPermissionKeys = {
  create: 'system_management_organization_management_create',
  detail: 'system_management_organization_management_detail',
  edit: 'system_management_organization_management_edit',
  delete: 'system_management_organization_management_delete'
} as const

const queryForm = reactive({
  organizationName: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
})

const appliedFilters = reactive({
  organizationName: ''
})

const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})
const noticeFading = ref(false)
let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null
const NOTICE_VISIBLE_MS = 5000
const NOTICE_FADE_MS = 300

const allRows = ref<SystemOrganizationItem[]>([])
const loading = ref(false)
const organizationTypeOptions = ref<EnumOption[]>([])
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const detailVisible = ref(false)
const currentOrganization = ref<SystemOrganizationItem | null>(null)

const confirmState = reactive({
  visible: false,
  target: null as SystemOrganizationItem | null
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

const total = ref(0)

const pagedRows = computed(() => allRows.value)

const getSerialNumber = (index: number) => (queryForm.pageNum - 1) * queryForm.pageSize + index + 1

const handleSearch = () => {
  appliedFilters.organizationName = queryForm.organizationName
  queryForm.pageNum = 1
  void loadOrganizations()
}

const handleReset = () => {
  queryForm.organizationName = ''
  appliedFilters.organizationName = ''
  queryForm.pageNum = 1
  queryForm.pageSize = DEFAULT_PAGE_SIZE
  void loadOrganizations()
}

const handlePageChange = (payload: { pageNum: number; pageSize: number }) => {
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  void loadOrganizations()
}

const openCreateModal = () => {
  currentOrganization.value = null
  formMode.value = 'create'
  formVisible.value = true
}

const openDetailModal = async (row: SystemOrganizationItem) => {
  try {
    currentOrganization.value = await fetchOrganizationDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    setNotice('error', `机构详情加载失败：${getErrorMessage(error)}`)
  }
}

const openEditModal = async (row: SystemOrganizationItem) => {
  try {
    currentOrganization.value = await fetchOrganizationDetail(row.id)
    formMode.value = 'edit'
    formVisible.value = true
  } catch (error) {
    setNotice('error', `机构详情加载失败：${getErrorMessage(error)}`)
  }
}

const openDeleteConfirm = (row: SystemOrganizationItem) => {
  confirmState.target = row
  confirmState.visible = true
}

const closeDeleteConfirm = () => {
  confirmState.visible = false
  confirmState.target = null
}

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
}

const loadOrganizations = async () => {
  loading.value = true
  allRows.value = []
  try {
    const result = await fetchOrganizationPage({
      organizationNameLike: appliedFilters.organizationName.trim() || undefined,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    allRows.value = result.list
    total.value = result.total
    queryForm.pageNum = result.pageNum || queryForm.pageNum
    queryForm.pageSize = result.pageSize || queryForm.pageSize
  } catch (error) {
    allRows.value = []
    total.value = 0
    setNotice('error', `机构列表加载失败：${getErrorMessage(error)}`)
  } finally {
    loading.value = false
  }
}

const handleFormSubmit = async (payload: SystemOrganizationFormValue) => {
  if (formMode.value === 'create') {
    try {
      await createOrganization({
        organizationName: payload.name,
        creditCode: payload.code,
        organizationType: Number(payload.typeValue),
        organizationAddress: payload.address || undefined,
        managerName: payload.managerName,
        managerPhone: payload.managerPhone,
        entryDate: payload.settledAt || undefined,
        remark: payload.remark || undefined
      })
      formVisible.value = false
      setNotice('success', '机构新增成功')
      queryForm.pageNum = 1
      await loadOrganizations()
    } catch (error) {
      setNotice('error', `机构新增失败：${getErrorMessage(error)}`)
    }
    return
  }

  if (payload.id == null) {
    formVisible.value = false
    return
  }

  try {
    await updateOrganization(payload.id, {
      organizationName: payload.name,
      creditCode: payload.code,
      organizationType: Number(payload.typeValue),
      organizationAddress: payload.address || undefined,
      managerName: payload.managerName,
      managerPhone: payload.managerPhone,
      entryDate: payload.settledAt || undefined,
      remark: payload.remark || undefined
    })
    formVisible.value = false
    setNotice('success', '机构修改成功')
    await loadOrganizations()
  } catch (error) {
    setNotice('error', `机构修改失败：${getErrorMessage(error)}`)
  }
}

const handleConfirmDelete = async () => {
  if (!confirmState.target) {
    return
  }

  const targetId = confirmState.target.id
  closeDeleteConfirm()
  try {
    await deleteOrganization(targetId)
    const nextTotal = Math.max(0, total.value - 1)
    const maxPageNum = Math.max(1, Math.ceil(nextTotal / queryForm.pageSize))
    if (queryForm.pageNum > maxPageNum) {
      queryForm.pageNum = maxPageNum
    }
    setNotice('success', '机构删除成功')
    await loadOrganizations()
  } catch (error) {
    setNotice('error', `机构删除失败：${getErrorMessage(error)}`)
  }
}

onMounted(async () => {
  const [types] = await Promise.all([fetchEnumOptionsByKey('organizationType'), loadOrganizations()])
  organizationTypeOptions.value = types
})
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

    <section class="search-card">
      <div class="search-row">
        <div class="search-fields">
          <label class="search-item">
            <span class="search-label-inline">机构名称</span>
            <input
              v-model="queryForm.organizationName"
              class="search-input"
              placeholder="请输入机构名称"
            />
          </label>
        </div>

        <div class="search-actions">
          <button class="btn btn-primary" type="button" @click="handleSearch">查询</button>
          <button class="btn btn-secondary" type="button" @click="handleReset">重置</button>
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-toolbar">
        <h2 class="table-title">机构信息</h2>
        <button
          v-menu-permission="organizationPermissionKeys.create"
          class="btn btn-primary"
          type="button"
          @click="openCreateModal"
        >
          新增
        </button>
      </div>

      <div class="table-wrap">
        <UiTableStateOverlay
          :loading="loading"
          :empty="!loading && pagedRows.length === 0"
        />
        <table class="table">
          <thead>
            <tr>
              <th class="table-col-index">序号</th>
              <th>机构名称</th>
              <th>机构编码</th>
              <th>机构类型</th>
              <th>负责人名称</th>
              <th>负责人电话</th>
              <th class="remark-col">备注说明</th>
              <th class="table-col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in pagedRows" :key="row.id">
              <td>{{ getSerialNumber(index) }}</td>
              <td>{{ row.name || '--' }}</td>
              <td>{{ row.code || '--' }}</td>
              <td>{{ row.typeName || '--' }}</td>
              <td>{{ row.managerName || '--' }}</td>
              <td>{{ row.managerPhone || '--' }}</td>
              <td class="remark-col" :title="row.remark">{{ row.remark || '--' }}</td>
              <td class="table-col-action">
                <button
                  v-menu-permission="organizationPermissionKeys.detail"
                  class="btn-link"
                  type="button"
                  @click="openDetailModal(row)"
                >
                  详情
                </button>
                <button
                  v-menu-permission="organizationPermissionKeys.edit"
                  class="btn-link"
                  type="button"
                  @click="openEditModal(row)"
                >
                  修改
                </button>
                <button
                  v-menu-permission="organizationPermissionKeys.delete"
                  class="btn-link btn-link-danger"
                  type="button"
                  @click="openDeleteConfirm(row)"
                >
                  删除
                </button>
              </td>
            </tr>
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

    <OrganizationFormModal
      v-model="formVisible"
      :mode="formMode"
      :organization="currentOrganization"
      @submit="handleFormSubmit"
    />

    <OrganizationDetailModal v-model="detailVisible" :organization="currentOrganization" />

    <Transition name="confirm-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="confirm-panel">
          <div class="confirm-title">删除机构</div>
          <div class="confirm-content">
            确认删除机构“{{ confirmState.target?.name || '--' }}”吗？
          </div>
          <div class="confirm-actions">
            <button class="btn btn-secondary" type="button" @click="closeDeleteConfirm">取消</button>
            <button class="btn btn-primary" type="button" @click="handleConfirmDelete">确定</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 12px;
  min-width: 0;
  overflow: hidden;
}

.page-notice {
  position: fixed;
  top: 84px;
  left: 50%;
  z-index: 60;
  max-width: min(720px, calc(100vw - 48px));
  padding: 10px 16px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  transform: translateX(-50%);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.page-notice-info {
  color: var(--es-color-info-text);
  background: #eef6ff;
  border-color: #bfdbfe;
}

.page-notice-success {
  color: var(--es-color-success-text);
  background: #ecfdf3;
  border-color: #86efac;
}

.page-notice-error {
  color: var(--es-color-error-text);
  background: #fef2f2;
  border-color: #fca5a5;
}

.page-notice.is-fading {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}

.page-notice-fade-enter-active,
.page-notice-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-notice-fade-enter-from,
.page-notice-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}

.search-card,
.table-card {
  min-width: 0;
  padding: 16px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
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
  width: 220px;
  height: 34px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.search-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn {
  display: inline-flex;
  height: 34px;
  min-width: 72px;
  padding: 0 14px;
  font-size: var(--es-font-size-sm);
  white-space: nowrap;
  border: 1px solid transparent;
  border-radius: 5px;
  align-items: center;
  justify-content: center;
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
  position: relative;
  min-height: 120px;
  overflow: auto;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.table th,
.table td {
  padding: 10px;
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
  width: 64px;
}

.table-col-action {
  width: 156px;
}

.remark-col {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-link,
.btn-link-danger {
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  cursor: pointer;
  background: transparent;
  border: none;
}

.btn-link {
  color: var(--es-color-primary);
}

.btn-link-danger {
  color: var(--es-color-error-text);
}

.btn-link + .btn-link,
.btn-link + .btn-link-danger,
.btn-link-danger + .btn-link,
.btn-link-danger + .btn-link-danger {
  margin-left: 12px;
}

.empty-cell {
  padding: 0 !important;
}

.pager {
  padding: 0 10px;
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

.confirm-panel {
  width: min(420px, calc(100vw - 32px));
  background: var(--es-color-bg-elevated);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
}

.confirm-title {
  padding: 16px 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.confirm-content {
  padding: 14px 20px 24px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 0 20px 20px;
}

.confirm-fade-enter-active,
.confirm-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}

.confirm-fade-enter-from .confirm-panel,
.confirm-fade-leave-to .confirm-panel {
  transform: translateY(10px) scale(0.98);
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
