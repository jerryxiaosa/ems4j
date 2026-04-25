<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import OrganizationPicker from '@/components/common/OrganizationPicker.vue'
import UiTableStateOverlay from '@/components/common/UiTableStateOverlay.vue'
import UserDetailModal from '@/components/system/UserDetailModal.vue'
import UserFormModal from '@/components/system/UserFormModal.vue'
import UserPasswordModal from '@/components/system/UserPasswordModal.vue'
import { useUserCrud } from '@/modules/system/users/composables/useUserCrud'
import { useUserNotice } from '@/modules/system/users/composables/useUserNotice'
import { useUserQuery } from '@/modules/system/users/composables/useUserQuery'

const userPermissionKeys = {
  create: 'system_management_user_management_create',
  detail: 'system_management_user_management_detail',
  edit: 'system_management_user_management_edit',
  delete: 'system_management_user_management_delete',
  resetPassword: 'system_management_user_management_reset_password'
} as const

const isProtectedUser = (username?: string) => username === 'admin'

const { notice, noticeFading, setNotice, dispose } = useUserNotice()
const {
  userRows: pagedRows,
  total,
  loading,
  organizationOptions,
  roleOptions,
  queryForm,
  syncAppliedFilters,
  handleOrganizationSelect,
  loadUsers,
  handleSearch,
  handleReset,
  handlePageChange,
  getSerialNumber,
  initialize
} = useUserQuery({ setNotice })
const {
  formModalVisible,
  formMode,
  currentUser,
  detailVisible,
  passwordVisible,
  confirmState,
  openCreateModal,
  openEditModal,
  openDetailModal,
  openPasswordModal,
  openDeleteConfirm,
  closeDeleteConfirm,
  handleFormSubmit,
  handlePasswordSubmit,
  handleConfirmDelete
} = useUserCrud({
  setNotice,
  loadUsers,
  syncAppliedFilters,
  queryForm,
  total
})

onMounted(async () => {
  await initialize()
})

onBeforeUnmount(() => {
  dispose()
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
            <span class="search-label-inline">用户名</span>
            <input
              v-model="queryForm.username"
              class="search-input"
              placeholder="请输入用户名"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">员工姓名</span>
            <input
              v-model="queryForm.realName"
              class="search-input"
              placeholder="请输入员工姓名"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">手机号码</span>
            <input
              v-model="queryForm.phone"
              class="search-input"
              placeholder="请输入手机号码"
            />
          </label>
          <label class="search-item search-item-org">
            <span class="search-label-inline">机构选择</span>
            <OrganizationPicker
              v-model="queryForm.organizationKeyword"
              placeholder="请选择机构"
              :auto-blur-on-select="true"
              @select="handleOrganizationSelect"
            />
          </label>
          <label class="search-item">
            <span class="search-label-inline">角色名称</span>
            <select
              v-model="queryForm.roleId"
              class="search-input"
              :class="{ 'search-input-placeholder': !queryForm.roleId }"
            >
              <option value="">请选择</option>
              <option v-for="item in roleOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
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
        <h2 class="table-title">用户管理</h2>
        <button
          v-menu-permission="userPermissionKeys.create"
          class="btn btn-primary"
          type="button"
          @click="openCreateModal"
        >
          添加
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
              <th>用户账号</th>
              <th>姓名</th>
              <th>角色</th>
              <th class="org-col">所属机构</th>
              <th>手机号码</th>
              <th>添加时间</th>
              <th class="remark-col">备注</th>
              <th class="table-col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in pagedRows" :key="row.id">
              <td>{{ getSerialNumber(index) }}</td>
              <td>{{ row.username || '--' }}</td>
              <td>{{ row.realName || '--' }}</td>
              <td>{{ row.roleName || '--' }}</td>
              <td class="org-col" :title="row.organizationName">{{ row.organizationName || '--' }}</td>
              <td>{{ row.phone || '--' }}</td>
              <td>{{ row.createTime || '--' }}</td>
              <td class="remark-col" :title="row.remark">{{ row.remark || '--' }}</td>
              <td class="table-col-action">
                <button
                  v-menu-permission="userPermissionKeys.detail"
                  class="btn-link"
                  type="button"
                  @click="openDetailModal(row)"
                >
                  详情
                </button>
                <button
                  v-menu-permission="userPermissionKeys.edit"
                  class="btn-link"
                  type="button"
                  @click="openEditModal(row)"
                >
                  编辑
                </button>
                <button
                  v-if="!isProtectedUser(row.username)"
                  v-menu-permission="userPermissionKeys.resetPassword"
                  class="btn-link"
                  type="button"
                  @click="openPasswordModal(row)"
                >
                  重置密码
                </button>
                <button
                  v-if="!isProtectedUser(row.username)"
                  v-menu-permission="userPermissionKeys.delete"
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

    <UserFormModal
      v-model="formModalVisible"
      :mode="formMode"
      :user="currentUser"
      :organizations="organizationOptions"
      :roles="roleOptions"
      @submit="handleFormSubmit"
    />

    <UserDetailModal v-model="detailVisible" :user="currentUser" />

    <UserPasswordModal v-model="passwordVisible" :user="currentUser" @submit="handlePasswordSubmit" />

    <Transition name="confirm-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="confirm-panel">
          <div class="confirm-title">删除用户</div>
          <div class="confirm-content">
            确认删除用户“{{ confirmState.target?.realName || confirmState.target?.username || '--' }}”吗？
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
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
}

.search-card,
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

.search-item-org {
  width: 320px;
}

.search-item-org :deep(.organization-picker) {
  width: 220px;
}

.search-item-org :deep(.form-control) {
  height: 34px;
}

.search-label-inline {
  font-size: var(--es-font-size-md);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.search-input {
  width: 180px;
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
  width: 188px;
}

.org-col,
.remark-col {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-link,
.btn-link-danger {
  height: auto;
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  line-height: 1.2;
  cursor: pointer;
  background: transparent;
  border: none;
}

.btn-link {
  color: var(--es-color-primary);
}

.btn-link:hover {
  color: var(--es-color-primary-hover);
}

.btn-link-danger {
  color: var(--es-color-error-text);
}

.btn-link-danger:hover {
  opacity: 0.85;
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
