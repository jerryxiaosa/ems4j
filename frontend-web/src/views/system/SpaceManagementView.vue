<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { createSpace, deleteSpace, fetchSpaceDetail, fetchSpaceTree, updateSpace } from '@/api/adapters/space-manage'
import SpaceFormModal from '@/components/system/SpaceFormModal.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { SystemSpaceFormValue, SystemSpaceItem } from '@/modules/system/spaces/types'

interface FlattenedSpaceRow {
  id: number
  parentId: number | null
  name: string
  typeValue: string
  typeName: string
  area: number
  sortNum: number
  depth: number
  hasChildren: boolean
}

const spacePermissionKeys = {
  addChild: 'system_management_space_management_add_child',
  edit: 'system_management_space_management_edit',
  delete: 'system_management_space_management_delete'
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

const spaceTree = ref<SystemSpaceItem[]>([])
const expandedIds = ref<number[]>([])
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const currentSpace = ref<SystemSpaceItem | null>(null)
const currentParentSpace = ref<SystemSpaceItem | null>(null)
const loading = ref(false)

const confirmState = reactive({
  visible: false,
  target: null as SystemSpaceItem | null
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

const collectAllExpandedIds = (nodes: SystemSpaceItem[]): number[] => {
  const ids: number[] = []
  const walk = (list: SystemSpaceItem[]) => {
    list.forEach((node) => {
      if (node.children?.length) {
        ids.push(node.id)
        walk(node.children)
      }
    })
  }
  walk(nodes)
  return ids
}

const flattenTree = (nodes: SystemSpaceItem[], depth = 0): FlattenedSpaceRow[] => {
  const rows: FlattenedSpaceRow[] = []
  nodes.forEach((node) => {
    const hasChildren = Boolean(node.children?.length)
    rows.push({
      id: node.id,
      parentId: node.parentId,
      name: node.name,
      typeValue: node.typeValue,
      typeName: node.typeName,
      area: node.area,
      sortNum: node.sortNum,
      depth,
      hasChildren
    })

    if (hasChildren && expandedIds.value.includes(node.id)) {
      rows.push(...flattenTree(node.children || [], depth + 1))
    }
  })
  return rows
}

const rows = computed(() => flattenTree(spaceTree.value))

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
}

const toggleExpand = (id: number) => {
  if (expandedIds.value.includes(id)) {
    expandedIds.value = expandedIds.value.filter((item) => item !== id)
    return
  }
  expandedIds.value = [...expandedIds.value, id]
}

const findSpaceNode = (nodes: SystemSpaceItem[], id: number): SystemSpaceItem | null => {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    if (node.children?.length) {
      const matched = findSpaceNode(node.children, id)
      if (matched) {
        return matched
      }
    }
  }
  return null
}

const findParentNode = (nodes: SystemSpaceItem[], id: number | null): SystemSpaceItem | null => {
  if (id == null) {
    return null
  }
  return findSpaceNode(nodes, id)
}

const openCreateModal = (row: FlattenedSpaceRow) => {
  currentSpace.value = null
  currentParentSpace.value = findSpaceNode(spaceTree.value, row.id)
  formMode.value = 'create'
  formVisible.value = true
}

const openEditModal = async (row: FlattenedSpaceRow) => {
  try {
    currentSpace.value = await fetchSpaceDetail(row.id)
    currentParentSpace.value = findParentNode(spaceTree.value, row.parentId)
    formMode.value = 'edit'
    formVisible.value = true
  } catch (error) {
    setNotice('error', `空间详情加载失败：${getErrorMessage(error)}`)
  }
}

const openDeleteConfirm = (row: FlattenedSpaceRow) => {
  confirmState.target = findSpaceNode(spaceTree.value, row.id)
  confirmState.visible = true
}

const closeDeleteConfirm = () => {
  confirmState.visible = false
  confirmState.target = null
}

const loadSpaces = async () => {
  loading.value = true
  spaceTree.value = []
  try {
    const tree = await fetchSpaceTree()
    spaceTree.value = tree
    expandedIds.value = collectAllExpandedIds(tree)
  } catch (error) {
    spaceTree.value = []
    expandedIds.value = []
    setNotice('error', `空间列表加载失败：${getErrorMessage(error)}`)
  } finally {
    loading.value = false
  }
}

const handleFormSubmit = async (payload: SystemSpaceFormValue) => {
  if (formMode.value === 'create') {
    try {
      await createSpace({
        name: payload.name,
        pid: Number(payload.parentId),
        type: Number(payload.typeValue),
        area: payload.area ? Number(payload.area) : 0,
        sortIndex: Number(payload.sortNum)
      })
      formVisible.value = false
      setNotice('success', '空间新增成功')
      await loadSpaces()
    } catch (error) {
      setNotice('error', `空间新增失败：${getErrorMessage(error)}`)
    }
    return
  }

  if (payload.id == null) {
    formVisible.value = false
    return
  }

  try {
    await updateSpace(payload.id, {
      name: payload.name,
      pid: Number(payload.parentId),
      type: Number(payload.typeValue),
      area: payload.area ? Number(payload.area) : 0,
      sortIndex: Number(payload.sortNum)
    })
    formVisible.value = false
    setNotice('success', '空间修改成功')
    await loadSpaces()
  } catch (error) {
    setNotice('error', `空间修改失败：${getErrorMessage(error)}`)
  }
}

const handleConfirmDelete = async () => {
  if (!confirmState.target) {
    return
  }

  const targetId = confirmState.target.id
  closeDeleteConfirm()
  try {
    await deleteSpace(targetId)
    setNotice('success', '空间删除成功')
    await loadSpaces()
  } catch (error) {
    setNotice('error', `空间删除失败：${getErrorMessage(error)}`)
  }
}

const getIndentStyle = (depth: number) => ({
  paddingLeft: `${16 + depth * 28}px`
})

onMounted(() => {
  void loadSpaces()
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
        <h2 class="table-title">空间信息</h2>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>空间名称</th>
              <th>空间类型</th>
              <th>空间面积（㎡）</th>
              <th class="table-col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="4" class="empty-cell">
                <UiLoadingState text="加载中" />
              </td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="4" class="empty-cell">
                <UiEmptyState text="暂无数据" />
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td>
                <div class="space-name-cell" :style="getIndentStyle(row.depth)">
                  <button
                    v-if="row.hasChildren"
                    type="button"
                    class="tree-toggle"
                    :aria-label="expandedIds.includes(row.id) ? '收起空间' : '展开空间'"
                    @click="toggleExpand(row.id)"
                  >
                    <span :class="['tree-arrow', { 'tree-arrow-expanded': expandedIds.includes(row.id) }]"></span>
                  </button>
                  <span v-else class="tree-toggle tree-toggle-placeholder"></span>
                  <span class="space-name-text">{{ row.name }}</span>
                </div>
              </td>
              <td>{{ row.typeName || '--' }}</td>
              <td>{{ row.area > 0 ? row.area : '--' }}</td>
              <td class="table-col-action">
                <button
                  v-menu-permission="spacePermissionKeys.addChild"
                  class="btn-link"
                  type="button"
                  @click="openCreateModal(row)"
                >
                  添加子级
                </button>
                <button
                  v-menu-permission="spacePermissionKeys.edit"
                  class="btn-link"
                  type="button"
                  @click="openEditModal(row)"
                >
                  编辑
                </button>
                <button
                  v-menu-permission="spacePermissionKeys.delete"
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

    <SpaceFormModal
      v-model="formVisible"
      :mode="formMode"
      :parent-space="currentParentSpace"
      :space="currentSpace"
      @submit="handleFormSubmit"
    />

    <Transition name="confirm-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="confirm-panel">
          <div class="confirm-title">删除空间</div>
          <div class="confirm-content">
            确认删除空间“{{ confirmState.target?.name || '--' }}”吗？
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

.table-col-action {
  width: 188px;
}

.empty-cell {
  padding: 32px 0 !important;
  text-align: center !important;
  color: var(--es-color-text-placeholder) !important;
}

.space-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 18px;
}

.tree-toggle {
  display: inline-flex;
  width: 18px;
  height: 18px;
  padding: 0;
  color: var(--es-color-text-secondary);
  background: transparent;
  border: 0;
  align-items: center;
  justify-content: center;
}

.tree-toggle-placeholder {
  cursor: default;
}

.tree-arrow {
  display: inline-block;
  width: 0;
  height: 0;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  border-left: 6px solid currentcolor;
  transition: transform 0.2s ease;
}

.tree-arrow-expanded {
  transform: rotate(90deg);
}

.space-name-text {
  color: var(--es-color-text-primary);
}

.btn,
.icon-btn {
  display: inline-flex;
  height: 34px;
  min-width: 72px;
  padding: 0 14px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-secondary,
.icon-btn {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
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
