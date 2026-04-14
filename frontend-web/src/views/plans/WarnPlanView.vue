<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import {
  addWarnPlan,
  deleteWarnPlan,
  fetchWarnPlanDetail,
  fetchWarnPlanList,
  updateWarnPlan
} from '@/api/adapters/plan'
import UiTableStateOverlay from '@/components/common/UiTableStateOverlay.vue'
import WarnPlanDetailModal from '@/components/plans/WarnPlanDetailModal.vue'
import WarnPlanEditModal from '@/components/plans/WarnPlanEditModal.vue'
import type { WarnPlanFormValue, WarnPlanItem } from '@/types/plan'

const warnPlanPermissionKeys = {
  create: 'plan_management_warn_plan_create',
  detail: 'plan_management_warn_plan_detail',
  edit: 'plan_management_warn_plan_edit',
  delete: 'plan_management_warn_plan_delete'
} as const

const rows = ref<WarnPlanItem[]>([])
const loading = ref(false)

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
  name: ''
})
const editModalVisible = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const editingPlan = ref<WarnPlanItem | null>(null)
const detailVisible = ref(false)
const detailPlan = ref<WarnPlanItem | null>(null)

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

const getSerialNumber = (index: number) => index + 1

const loadRows = async () => {
  loading.value = true
  try {
    rows.value = await fetchWarnPlanList({
      name: queryForm.name.trim() || undefined
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '预警方案列表加载失败'
    setNotice('error', message)
  } finally {
    loading.value = false
  }
}

const search = async () => {
  await loadRows()
}

const resetQuery = async () => {
  queryForm.name = ''
  await loadRows()
}

const openCreateModal = () => {
  editMode.value = 'create'
  editingPlan.value = null
  editModalVisible.value = true
}

const openEditModal = async (row: WarnPlanItem) => {
  try {
    editingPlan.value = await fetchWarnPlanDetail(row.id)
    editMode.value = 'edit'
    editModalVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '预警方案详情加载失败'
    setNotice('error', message)
  }
}

const openDetailModal = async (row: WarnPlanItem) => {
  try {
    detailPlan.value = await fetchWarnPlanDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '预警方案详情加载失败'
    setNotice('error', message)
  }
}

const handleSubmitPlan = async (payload: WarnPlanFormValue) => {
  const requestPayload = {
    name: payload.name,
    firstLevel: Number(payload.firstLevel),
    secondLevel: Number(payload.secondLevel),
    autoClose: payload.autoClose === 'true',
    remark: payload.remark || undefined
  }

  try {
    if (editMode.value === 'create') {
      await addWarnPlan(requestPayload)
      setNotice('success', '预警方案添加成功')
    } else if (payload.id) {
      await updateWarnPlan(payload.id, {
        id: payload.id,
        ...requestPayload
      })
      setNotice('success', '预警方案信息更新成功')
    }

    editModalVisible.value = false
    await loadRows()
  } catch (error) {
    const message = error instanceof Error ? error.message : '预警方案保存失败'
    setNotice('error', message)
  }
}

const openDeleteConfirm = (row: WarnPlanItem) => {
  confirmState.visible = true
  confirmState.targetId = row.id
  confirmState.title = '删除确认'
  confirmState.content = `是否删除预警方案“${row.name}”？`
}

const closeConfirm = () => {
  confirmState.visible = false
  confirmState.targetId = 0
  confirmState.title = ''
  confirmState.content = ''
}

const handleConfirmDelete = async () => {
  try {
    await deleteWarnPlan(confirmState.targetId)
    closeConfirm()
    setNotice('success', '预警方案删除成功')
    await loadRows()
  } catch (error) {
    const message = error instanceof Error ? error.message : '预警方案删除失败'
    setNotice('error', message)
  }
}

onMounted(() => {
  void loadRows()
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

    <section class="workspace-card">
      <header class="workspace-search">
        <div class="search-row">
          <label class="search-item">
            <span class="search-label-inline">方案名称</span>
            <input
              v-model="queryForm.name"
              class="search-input"
              type="text"
              placeholder="请输入预警方案名称"
            />
          </label>

          <div class="search-actions">
            <button class="btn btn-primary" type="button" @click="search">查询</button>
            <button class="btn btn-secondary" type="button" @click="resetQuery">重置</button>
          </div>
        </div>
      </header>

      <div class="workspace-body">
        <div class="workspace-head">
          <h2 class="workspace-title">预警方案</h2>
          <div class="page-actions">
            <button
              v-menu-permission="warnPlanPermissionKeys.create"
              class="btn btn-primary"
              type="button"
              @click="openCreateModal"
            >
              添加
            </button>
          </div>
        </div>

        <div class="table-wrap">
          <UiTableStateOverlay
            :loading="loading"
            :empty="!loading && !rows.length"
          />
          <table class="table">
            <thead>
              <tr>
                <th class="table-col-index">序号</th>
                <th class="warn-plan-col-name">预警方案名称</th>
                <th class="warn-plan-col-first-level">第一预警余额（元）</th>
                <th class="warn-plan-col-second-level">第二预警余额（元）</th>
                <th class="warn-plan-col-auto-close">欠费断闸</th>
                <th class="warn-plan-col-update-time">创建时间</th>
                <th class="warn-plan-col-remark">备注</th>
                <th class="warn-plan-col-action">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in rows" :key="row.id">
                <td>{{ getSerialNumber(index) }}</td>
                <td class="warn-plan-name-cell">{{ row.name }}</td>
                <td>{{ row.firstLevel }}</td>
                <td>{{ row.secondLevel }}</td>
                <td>{{ row.autoClose ? '是' : '否' }}</td>
                <td class="warn-plan-cell-update-time">{{ row.createTime || '--' }}</td>
                <td class="warn-plan-remark-cell">{{ row.remark || '--' }}</td>
                <td class="warn-plan-col-action">
                  <button
                    v-menu-permission="warnPlanPermissionKeys.detail"
                    class="btn-link"
                    type="button"
                    @click="openDetailModal(row)"
                  >
                    详情
                  </button>
                  <button
                    v-menu-permission="warnPlanPermissionKeys.edit"
                    class="btn-link"
                    type="button"
                    @click="openEditModal(row)"
                  >
                    编辑
                  </button>
                  <button
                    v-menu-permission="warnPlanPermissionKeys.delete"
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
    </section>

    <WarnPlanEditModal
      v-model="editModalVisible"
      :mode="editMode"
      :plan="editingPlan"
      @submit="handleSubmitPlan"
    />

    <WarnPlanDetailModal v-model="detailVisible" :plan="detailPlan" />

    <Transition name="warn-plan-modal-fade" appear>
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
  grid-template-rows: auto minmax(0, 1fr);
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

.search-input::placeholder {
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

.table-wrap {
  position: relative;
  min-height: 120px;
  overflow: hidden auto;
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
  padding: 12px 14px;
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
  width: 84px;
}

.warn-plan-col-name {
  width: 144px;
}

.warn-plan-col-auto-close {
  width: 112px;
}

.warn-plan-col-first-level,
.warn-plan-col-second-level {
  width: 136px;
}

.warn-plan-col-action {
  width: 160px;
}

.warn-plan-col-update-time {
  width: 176px;
}

.warn-plan-col-remark {
  width: 200px;
}

.warn-plan-cell-update-time {
  padding-left: 10px;
}

.warn-plan-remark-cell {
  word-break: break-word;
  white-space: normal;
}

.warn-plan-name-cell {
  word-break: break-word;
  white-space: normal;
  overflow-wrap: anywhere;
}

.table-loading-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.empty-row {
  padding: 16px 0;
  color: var(--es-color-text-placeholder) !important;
  text-align: center !important;
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
  overflow: hidden;
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
  background: var(--es-color-error-bg);
  border: 1px solid var(--es-color-error-border);
  border-radius: 5px;
}

.confirm-content {
  margin: 0;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-error-text);
}

.warn-plan-modal-fade-enter-active,
.warn-plan-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.warn-plan-modal-fade-enter-from,
.warn-plan-modal-fade-leave-to {
  opacity: 0;
}

.warn-plan-modal-fade-enter-from .confirm-modal,
.warn-plan-modal-fade-leave-to .confirm-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
