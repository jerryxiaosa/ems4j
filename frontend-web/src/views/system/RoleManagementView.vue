<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { createRole, deleteRole, fetchRoleList, updateRole } from '@/api/adapters/role'
import RoleFormModal from '@/components/system/RoleFormModal.vue'
import RolePermissionModal from '@/components/system/RolePermissionModal.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { SystemRoleFormValue, SystemRoleItem } from '@/modules/system/roles/types'

const rolePermissionKeys = {
  create: 'system_management_role_management_create',
  edit: 'system_management_role_management_edit',
  delete: 'system_management_role_management_delete',
  assign: 'system_management_role_management_assign_permissions'
} as const

const roleRows = ref<SystemRoleItem[]>([])
const loading = ref(false)
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const permissionVisible = ref(false)
const currentRole = ref<SystemRoleItem | null>(null)

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
  target: null as SystemRoleItem | null
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

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
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

const openCreateModal = () => {
  currentRole.value = null
  formMode.value = 'create'
  formVisible.value = true
}

const openEditModal = (row: SystemRoleItem) => {
  currentRole.value = row
  formMode.value = 'edit'
  formVisible.value = true
}

const openPermissionModal = (row: SystemRoleItem) => {
  currentRole.value = row
  permissionVisible.value = true
}

const openDeleteConfirm = (row: SystemRoleItem) => {
  confirmState.target = row
  confirmState.visible = true
}

const closeDeleteConfirm = () => {
  confirmState.visible = false
  confirmState.target = null
}

const loadRoles = async () => {
  loading.value = true
  roleRows.value = []
  try {
    roleRows.value = await fetchRoleList()
  } catch (error) {
    roleRows.value = []
    setNotice('error', `角色列表加载失败：${getErrorMessage(error)}`)
  } finally {
    loading.value = false
  }
}

const handleFormSubmit = async (payload: SystemRoleFormValue) => {
  if (formMode.value === 'create') {
    try {
      await createRole({
        roleName: payload.name,
        roleKey: payload.key,
        remark: payload.description || undefined
      })
      formVisible.value = false
      setNotice('success', '角色新增成功')
      await loadRoles()
    } catch (error) {
      setNotice('error', `角色新增失败：${getErrorMessage(error)}`)
    }
    return
  }

  if (payload.id == null) {
    formVisible.value = false
    return
  }

  try {
    await updateRole(payload.id, {
      roleName: payload.name,
      remark: payload.description || undefined
    })
    formVisible.value = false
    setNotice('success', '角色编辑成功')
    await loadRoles()
  } catch (error) {
    setNotice('error', `角色编辑失败：${getErrorMessage(error)}`)
  }
}

const handleConfirmDelete = async () => {
  if (!confirmState.target) {
    return
  }

  const targetId = confirmState.target.id
  closeDeleteConfirm()
  try {
    await deleteRole(targetId)
    setNotice('success', '角色删除成功')
    await loadRoles()
  } catch (error) {
    setNotice('error', `角色删除失败：${getErrorMessage(error)}`)
  }
}

const getSerialNumber = (index: number) => index + 1

onMounted(async () => {
  await loadRoles()
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

    <section class="table-card">
      <div class="table-toolbar">
        <h2 class="table-title">角色管理</h2>
        <button
          v-menu-permission="rolePermissionKeys.create"
          class="btn btn-primary"
          type="button"
          @click="openCreateModal"
        >
          添加
        </button>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th class="table-col-index">序号</th>
              <th class="role-name-col">角色名称</th>
              <th>角色描述</th>
              <th class="table-col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="4" class="empty-cell">
                <UiLoadingState :size="18" :thickness="2" :min-height="72" />
              </td>
            </tr>
            <tr v-else-if="roleRows.length === 0">
              <td colspan="4" class="empty-cell">
                <UiEmptyState :min-height="72" />
              </td>
            </tr>
            <tr v-for="(row, index) in roleRows" :key="row.id">
              <td>{{ getSerialNumber(index) }}</td>
              <td>{{ row.name || '--' }}</td>
              <td class="description-col" :title="row.description">{{ row.description || '--' }}</td>
              <td class="table-col-action">
                <button
                  v-menu-permission="rolePermissionKeys.assign"
                  class="btn-link"
                  type="button"
                  @click="openPermissionModal(row)"
                >
                  分配权限
                </button>
                <button
                  v-menu-permission="rolePermissionKeys.edit"
                  class="btn-link"
                  type="button"
                  @click="openEditModal(row)"
                >
                  编辑
                </button>
                <button
                  v-if="!row.isSystem"
                  v-menu-permission="rolePermissionKeys.delete"
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
    </section>

    <RoleFormModal
      v-model="formVisible"
      :mode="formMode"
      :role="currentRole"
      @submit="handleFormSubmit"
    />

    <RolePermissionModal
      v-model="permissionVisible"
      :role="currentRole"
      @saved="setNotice('success', '角色权限保存成功')"
    />

    <Transition name="confirm-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="confirm-panel">
          <div class="confirm-title">删除角色</div>
          <div class="confirm-content">
            确认删除角色“{{ confirmState.target?.name || '--' }}”吗？
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

.table-card {
  min-width: 0;
  padding: 16px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
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

.table-wrap {
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

.role-name-col {
  width: 240px;
}

.table-col-action {
  width: 204px;
}

.description-col {
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
  padding: 32px 0 !important;
  text-align: center !important;
  color: var(--es-color-text-placeholder) !important;
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
</style>
