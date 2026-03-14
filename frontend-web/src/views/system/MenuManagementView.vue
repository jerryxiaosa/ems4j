<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { createMenu, fetchMenuDetail, fetchMenuTree, removeMenu, updateMenu } from '@/api/adapters/menu-manage'
import MenuFormModal from '@/components/system/MenuFormModal.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { SystemMenuFormValue, SystemMenuItem } from '@/modules/system/menus/types'

interface FlattenedMenuRow {
  id: number
  parentId: number | null
  name: string
  routePath: string
  categoryValue: string
  categoryName: string
  remark: string
  depth: number
  hasChildren: boolean
}

type MenuSourceTabValue = '1' | '2'

const menuSourceTabs: Array<{ label: string; value: MenuSourceTabValue }> = [
  { label: '后台', value: '1' },
  { label: '移动端', value: '2' }
]
const menuPermissionKeys = {
  addChild: 'system_management_menu_management_add_child',
  edit: 'system_management_menu_management_edit',
  delete: 'system_management_menu_management_delete'
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

const menuTree = ref<SystemMenuItem[]>([])
const loading = ref(false)
const expandedIds = ref<number[]>([])
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const currentMenu = ref<SystemMenuItem | null>(null)
const currentParentMenu = ref<SystemMenuItem | null>(null)
const activeMenuSource = ref<MenuSourceTabValue>('1')

const confirmState = reactive({
  visible: false,
  target: null as SystemMenuItem | null
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

const collectFirstLevelExpandedIds = (nodes: SystemMenuItem[]): number[] => {
  return nodes.filter((node) => node.children?.length).map((node) => node.id)
}

const flattenTree = (nodes: SystemMenuItem[], depth = 0): FlattenedMenuRow[] => {
  const rows: FlattenedMenuRow[] = []
  nodes.forEach((node) => {
    const hasChildren = Boolean(node.children?.length)
    rows.push({
      id: node.id,
      parentId: node.parentId,
      name: node.name,
      routePath: node.routePath,
      categoryValue: node.categoryValue,
      categoryName: node.categoryName,
      remark: node.remark,
      depth,
      hasChildren
    })

    if (hasChildren && expandedIds.value.includes(node.id)) {
      rows.push(...flattenTree(node.children || [], depth + 1))
    }
  })
  return rows
}

const rows = computed(() => flattenTree(menuTree.value))

const toggleExpand = (id: number) => {
  if (expandedIds.value.includes(id)) {
    expandedIds.value = expandedIds.value.filter((item) => item !== id)
    return
  }
  expandedIds.value = [...expandedIds.value, id]
}

const findMenuNode = (nodes: SystemMenuItem[], id: number): SystemMenuItem | null => {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    if (node.children?.length) {
      const matched = findMenuNode(node.children, id)
      if (matched) {
        return matched
      }
    }
  }
  return null
}

const findParentNode = (nodes: SystemMenuItem[], id: number | null): SystemMenuItem | null => {
  if (id == null) {
    return null
  }
  return findMenuNode(nodes, id)
}

const openCreateModal = (row: FlattenedMenuRow) => {
  currentMenu.value = null
  currentParentMenu.value = findMenuNode(menuTree.value, row.id)
  formMode.value = 'create'
  formVisible.value = true
}

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
}

const loadMenus = async () => {
  loading.value = true
  menuTree.value = []
  try {
    const result = await fetchMenuTree(Number(activeMenuSource.value))
    menuTree.value = result
    expandedIds.value = collectFirstLevelExpandedIds(result)
  } catch (error) {
    menuTree.value = []
    expandedIds.value = []
    setNotice('error', `菜单列表加载失败：${getErrorMessage(error)}`)
  } finally {
    loading.value = false
  }
}

watch(
  () => activeMenuSource.value,
  () => {
    void loadMenus()
  },
  { immediate: true }
)

const openEditModal = async (row: FlattenedMenuRow) => {
  try {
    const detail = await fetchMenuDetail(row.id)
    if (!detail) {
      throw new Error('未获取到菜单详情')
    }
    currentMenu.value = detail
    currentParentMenu.value = findParentNode(menuTree.value, row.parentId)
    formMode.value = 'edit'
    formVisible.value = true
  } catch (error) {
    setNotice('error', `菜单详情加载失败：${getErrorMessage(error)}`)
  }
}

const openDeleteConfirm = (row: FlattenedMenuRow) => {
  confirmState.target = findMenuNode(menuTree.value, row.id)
  confirmState.visible = true
}

const closeDeleteConfirm = () => {
  confirmState.visible = false
  confirmState.target = null
}

const handleFormSubmit = async (payload: SystemMenuFormValue) => {
  if (formMode.value === 'create') {
    try {
      await createMenu({
        menuName: payload.name,
        menuKey: payload.key,
        pid: payload.parentId ? Number(payload.parentId) : 0,
        sortNum: Number(payload.sortNum),
        path: payload.routePath || undefined,
        menuSource: Number(payload.platformValue),
        menuType: Number(payload.categoryValue),
        icon: payload.icon || undefined,
        remark: payload.remark || undefined,
        hidden: payload.hiddenValue === 'true',
        permissionCodes: payload.backendApis
      })
      formVisible.value = false
      setNotice('success', '菜单新增成功')
      await loadMenus()
    } catch (error) {
      setNotice('error', `菜单新增失败：${getErrorMessage(error)}`)
    }
    return
  }

  if (payload.id == null) {
    formVisible.value = false
    return
  }

  try {
    await updateMenu(payload.id, {
      menuName: payload.name,
      menuKey: payload.key,
      sortNum: Number(payload.sortNum),
      path: payload.routePath || undefined,
      menuSource: Number(payload.platformValue),
      menuType: Number(payload.categoryValue),
      icon: payload.icon || undefined,
      remark: payload.remark || undefined,
      hidden: payload.hiddenValue === 'true',
      permissionCodes: payload.backendApis
    })
    formVisible.value = false
    setNotice('success', '菜单修改成功')
    await loadMenus()
  } catch (error) {
    setNotice('error', `菜单修改失败：${getErrorMessage(error)}`)
  }
}

const handleConfirmDelete = async () => {
  if (!confirmState.target) {
    return
  }

  const targetId = confirmState.target.id
  closeDeleteConfirm()
  try {
    await removeMenu(targetId)
    setNotice('success', '菜单删除成功')
    await loadMenus()
  } catch (error) {
    setNotice('error', `菜单删除失败：${getErrorMessage(error)}`)
  }
}

const getIndentStyle = (depth: number) => ({
  paddingLeft: `${16 + depth * 28}px`
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
      <div class="tab-nav">
        <button
          v-for="tab in menuSourceTabs"
          :key="tab.value"
          type="button"
          :class="['tab-btn', { 'is-active': activeMenuSource === tab.value }]"
          @click="activeMenuSource = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>菜单名称</th>
              <th class="col-category">类别</th>
              <th class="col-route">路由标识</th>
              <th class="col-remark">备注</th>
              <th class="table-col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" class="empty-cell">
                <UiLoadingState />
              </td>
            </tr>
            <tr v-else-if="rows.length === 0">
              <td colspan="5" class="empty-cell">
                <UiEmptyState text="暂无数据" />
              </td>
            </tr>
            <tr v-for="row in rows" :key="row.id">
              <td>
                <div class="menu-name-cell" :style="getIndentStyle(row.depth)">
                  <button
                    v-if="row.hasChildren"
                    type="button"
                    class="tree-toggle"
                    :aria-label="expandedIds.includes(row.id) ? '收起菜单' : '展开菜单'"
                    @click="toggleExpand(row.id)"
                  >
                    <span :class="['tree-arrow', { 'tree-arrow-expanded': expandedIds.includes(row.id) }]"></span>
                  </button>
                  <span v-else class="tree-toggle tree-toggle-placeholder"></span>
                  <span class="menu-name-text">{{ row.name }}</span>
                </div>
              </td>
              <td
                :class="[
                  'col-category',
                  {
                    'category-menu-text': row.categoryValue === '1',
                    'category-button-text': row.categoryValue === '2'
                  }
                ]"
              >
                {{ row.categoryName || '--' }}
              </td>
              <td class="col-route">{{ row.routePath || '--' }}</td>
              <td class="col-remark" :title="row.remark || '--'">{{ row.remark || '--' }}</td>
              <td class="table-col-action">
                <button
                  v-menu-permission="menuPermissionKeys.addChild"
                  class="btn-link"
                  type="button"
                  @click="openCreateModal(row)"
                >
                  添加下级
                </button>
                <button
                  v-menu-permission="menuPermissionKeys.edit"
                  class="btn-link"
                  type="button"
                  @click="openEditModal(row)"
                >
                  编辑
                </button>
                <button
                  v-menu-permission="menuPermissionKeys.delete"
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

    <MenuFormModal
      v-model="formVisible"
      :mode="formMode"
      :parent-menu="currentParentMenu"
      :menu="currentMenu"
      :fixed-platform-value="activeMenuSource"
      @submit="handleFormSubmit"
    />

    <Transition name="confirm-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="confirm-panel">
          <div class="confirm-title">删除菜单</div>
          <div class="confirm-content">确认删除菜单“{{ confirmState.target?.name || '--' }}”吗？</div>
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

.tab-nav {
  display: flex;
  gap: 28px;
  padding-bottom: 12px;
  margin-bottom: 14px;
  border-bottom: 1px solid var(--es-color-border);
}

.tab-btn {
  position: relative;
  padding: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-secondary);
  cursor: pointer;
  background: transparent;
  border: 0;
}

.tab-btn.is-active {
  color: var(--es-color-primary);
}

.tab-btn.is-active::after {
  position: absolute;
  right: 0;
  bottom: -13px;
  left: 0;
  height: 2px;
  background: var(--es-color-primary);
  border-radius: 999px;
  content: '';
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
  text-align: left;
  color: var(--es-color-text-secondary);
  border-bottom: 1px solid var(--es-color-border);
}

.table thead th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.col-category {
  width: 120px;
}

.category-menu-text {
  color: var(--es-color-success-text) !important;
}

.category-button-text {
  color: var(--es-color-primary) !important;
}

.col-route {
  width: 360px;
}

.col-remark {
  width: 220px;
}

.table-col-action {
  width: 220px;
  white-space: nowrap;
}

.empty-cell {
  padding: 32px 0 !important;
  text-align: center !important;
  color: var(--es-color-text-placeholder) !important;
}

.menu-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 18px;
}

.menu-name-text {
  color: var(--es-color-text-primary);
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

.btn-link {
  color: var(--es-color-primary);
}

.btn-link-danger {
  color: var(--es-color-error-text);
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
  padding: 20px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
}

.confirm-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.confirm-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--es-color-text-secondary);
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}

.btn {
  display: inline-flex;
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: 14px;
  border: 1px solid transparent;
  border-radius: 5px;
  align-items: center;
  justify-content: center;
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
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
  transform: translateY(8px) scale(0.98);
}
</style>
