<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import DeviceCategoryTreeNode from '@/components/devices/DeviceCategoryTreeNode.vue'
import { fetchDeviceModelPage, fetchDeviceTypeTree } from '@/api/adapters/device'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiTableStateOverlay from '@/components/common/UiTableStateOverlay.vue'
import type { DeviceModelItem } from '@/types/device'

interface DeviceCategoryNode {
  id: number
  label: string
  typeKey?: string
  level?: number
  children?: DeviceCategoryNode[]
}

const DEFAULT_TREE_NODES: DeviceCategoryNode[] = [
  {
    id: 100,
    label: '电表品类',
    children: [
      { id: 110, label: '单相电表' },
      { id: 120, label: '三相电表' }
    ]
  },
  {
    id: 200,
    label: '网关品类',
    children: [
      { id: 210, label: '边缘网关' },
      { id: 220, label: '采集网关' }
    ]
  },
  {
    id: 300,
    label: '传感设备',
    children: [
      { id: 310, label: '环境传感器' },
      { id: 320, label: '状态采集器' }
    ]
  }
]

const queryForm = reactive({
  manufacturerName: '',
  modelName: '',
  pageNum: 1,
  pageSize: 10
})

const treeData = ref<DeviceCategoryNode[]>(DEFAULT_TREE_NODES)
const expandedNodeIds = ref<number[]>([])
const selectedNodeId = ref<number>(0)
const treeCollapsed = ref(false)
const tableLoading = ref(false)
const tableRows = ref<DeviceModelItem[]>([])
const total = ref(0)

const findNodeById = (nodes: DeviceCategoryNode[], id: number): DeviceCategoryNode | null => {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    if (node.children?.length) {
      const matched = findNodeById(node.children, id)
      if (matched) {
        return matched
      }
    }
  }
  return null
}

const isLeafNode = (node: DeviceCategoryNode): boolean => {
  return !node.children?.length
}

const collectLeafNodes = (nodes: DeviceCategoryNode[]): DeviceCategoryNode[] => {
  const leafNodes: DeviceCategoryNode[] = []

  const walk = (items: DeviceCategoryNode[]) => {
    for (const item of items) {
      if (item.children?.length) {
        walk(item.children)
        continue
      }
      leafNodes.push(item)
    }
  }

  walk(nodes)
  return leafNodes
}

const collectExpandableNodeIds = (nodes: DeviceCategoryNode[]): number[] => {
  const ids: number[] = []

  const walk = (items: DeviceCategoryNode[]) => {
    for (const item of items) {
      if (item.children?.length) {
        ids.push(item.id)
        walk(item.children)
      }
    }
  }

  walk(nodes)
  return ids
}

const buildTreeNodes = (
  items: Array<{ id: number; pid: number; typeName: string; typeKey: string; level?: number }>
): DeviceCategoryNode[] => {
  const nodeMap = new Map<number, DeviceCategoryNode>()
  const orderedIds: number[] = []

  items.forEach((item) => {
    if (!nodeMap.has(item.id)) {
      orderedIds.push(item.id)
    }

    nodeMap.set(item.id, {
      id: item.id,
      label: item.typeName,
      typeKey: item.typeKey,
      level: item.level,
      children: []
    })
  })

  const roots: DeviceCategoryNode[] = []
  orderedIds.forEach((id) => {
    const source = items.find((item) => item.id === id)
    const node = nodeMap.get(id)
    if (!source || !node) {
      return
    }

    const parent = nodeMap.get(source.pid)
    if (parent && parent !== node) {
      parent.children = parent.children || []
      parent.children.push(node)
      return
    }

    roots.push(node)
  })

  const normalizeChildren = (nodes: DeviceCategoryNode[]) => {
    nodes.forEach((node) => {
      if (!node.children?.length) {
        delete node.children
        return
      }
      normalizeChildren(node.children)
    })
  }

  normalizeChildren(roots)
  return roots
}

const resolveInitialSelectedNodeId = (nodes: DeviceCategoryNode[]): number => {
  return collectLeafNodes(nodes)[0]?.id ?? 0
}

const applyTreeData = (nodes: DeviceCategoryNode[]) => {
  treeData.value = nodes
  expandedNodeIds.value = collectExpandableNodeIds(nodes)

  const currentNode = findNodeById(nodes, selectedNodeId.value)
  if (currentNode && isLeafNode(currentNode)) {
    selectedNodeId.value = currentNode.id
    return
  }

  selectedNodeId.value = resolveInitialSelectedNodeId(nodes)
}

const selectedNode = computed(() => {
  return findNodeById(treeData.value, selectedNodeId.value)
})

const selectedLeafId = computed(() => {
  return selectedNode.value && isLeafNode(selectedNode.value) ? selectedNode.value.id : undefined
})

const getSerialNumber = (index: number) => {
  return (queryForm.pageNum - 1) * queryForm.pageSize + index + 1
}

const runTableLoad = async () => {
  if (!selectedLeafId.value) {
    tableRows.value = []
    total.value = 0
    return
  }

  tableLoading.value = true
  try {
    const page = await fetchDeviceModelPage({
      typeIds: [selectedLeafId.value],
      manufacturerName: queryForm.manufacturerName.trim() || undefined,
      modelName: queryForm.modelName.trim() || undefined,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })

    tableRows.value = page.list
    total.value = page.total
    queryForm.pageNum = page.pageNum ?? queryForm.pageNum
    queryForm.pageSize = page.pageSize ?? queryForm.pageSize
  } finally {
    tableLoading.value = false
  }
}

const handleTreeSelect = async (nodeId: number) => {
  const node = findNodeById(treeData.value, nodeId)
  if (!node || !isLeafNode(node)) {
    return
  }

  selectedNodeId.value = nodeId
  queryForm.manufacturerName = ''
  queryForm.modelName = ''
  queryForm.pageNum = 1
  await runTableLoad()
}

const toggleExpand = (nodeId: number) => {
  if (expandedNodeIds.value.includes(nodeId)) {
    expandedNodeIds.value = expandedNodeIds.value.filter((id) => id !== nodeId)
    return
  }
  expandedNodeIds.value = [...expandedNodeIds.value, nodeId]
}

const toggleTreePanel = () => {
  treeCollapsed.value = !treeCollapsed.value
}

const handleSearch = async () => {
  queryForm.pageNum = 1
  await runTableLoad()
}

const handleReset = async () => {
  queryForm.manufacturerName = ''
  queryForm.modelName = ''
  queryForm.pageNum = 1
  await runTableLoad()
}

const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
  if (tableLoading.value) {
    return
  }
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  await runTableLoad()
}

const loadTreeData = async () => {
  const items = await fetchDeviceTypeTree()
  const nodes = buildTreeNodes(items)
  if (!nodes.length) {
    tableRows.value = []
    total.value = 0
    return
  }

  applyTreeData(nodes)
  queryForm.manufacturerName = ''
  queryForm.modelName = ''
  queryForm.pageNum = 1
  await runTableLoad()
}

onMounted(async () => {
  applyTreeData(DEFAULT_TREE_NODES)

  try {
    await loadTreeData()
  } catch (_error) {
    tableRows.value = []
    total.value = 0
  }
})
</script>

<template>
  <div class="page">
    <div :class="['tree-shell', { 'tree-shell-collapsed': treeCollapsed }]">
      <section :class="['tree-card', { 'tree-card-collapsed': treeCollapsed }]">
        <header class="tree-card-head">
          <h2 v-if="!treeCollapsed" class="tree-card-title">品类信息</h2>
          <button
            class="tree-panel-toggle"
            type="button"
            :aria-label="treeCollapsed ? '展开品类树' : '收起品类树'"
            @click="toggleTreePanel"
          >
            <span class="tree-panel-toggle-icon"></span>
          </button>
        </header>
        <div v-if="!treeCollapsed" class="tree-card-body">
          <ul class="tree-root">
            <DeviceCategoryTreeNode
              v-for="node in treeData"
              :key="node.id"
              :node="node"
              :expanded-node-ids="expandedNodeIds"
              :selected-node-id="selectedNodeId"
              @toggle="toggleExpand"
              @select="handleTreeSelect"
            />
          </ul>
        </div>
      </section>
    </div>

    <section class="workspace-card">
      <header class="workspace-search">
        <div class="search-row">
          <label class="search-item">
            <span class="search-label-inline">厂商名称</span>
            <input
              id="manufacturerName"
              v-model="queryForm.manufacturerName"
              class="search-input"
              type="text"
              placeholder="请输入厂商名称"
            />
          </label>

          <label class="search-item search-item-secondary">
            <span class="search-label-inline">型号名称</span>
            <input
              id="modelName"
              v-model="queryForm.modelName"
              class="search-input"
              type="text"
              placeholder="请输入型号名称"
            />
          </label>

          <div class="search-actions">
            <button class="btn btn-primary" type="button" @click="handleSearch">查询</button>
            <button class="btn btn-secondary" type="button" @click="handleReset">重置</button>
          </div>
        </div>
      </header>

      <div class="workspace-body">
        <div class="workspace-head">
          <div class="workspace-title-group">
            <h2 class="workspace-title">设备品类</h2>
            <span class="workspace-current">当前分类：{{ selectedNode?.label || '—' }}</span>
          </div>
        </div>

        <div class="table-wrap">
          <UiTableStateOverlay
            :loading="tableLoading"
            :empty="!tableLoading && !tableRows.length"
          />
          <table class="table">
            <thead>
              <tr>
                <th class="table-col-index">序号</th>
                <th>厂商名称</th>
                <th>型号名称</th>
              </tr>
            </thead>
            <tbody>
              <template v-if="tableRows.length > 0">
                <tr v-for="(row, index) in tableRows" :key="row.id">
                  <td>{{ getSerialNumber(index) }}</td>
                  <td>{{ row.manufacturerName || '--' }}</td>
                  <td>{{ row.modelName || '--' }}</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>

      <CommonPagination
        class="pager"
        :total="total"
        :page-num="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :loading="tableLoading"
        @change="handlePageChange"
      />
    </section>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  gap: 16px;
  min-height: calc(100vh - 146px);
}

.tree-shell {
  display: flex;
  width: 236px;
  flex: 0 0 236px;
  align-items: stretch;
}

.tree-shell-collapsed {
  display: block;
  width: 24px;
  flex-basis: 24px;
}

.tree-card,
.workspace-card {
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
}

.tree-card {
  display: flex;
  width: 100%;
  min-height: 100%;
  overflow: hidden;
  flex-direction: column;
}

.tree-card-collapsed {
  display: block;
  min-height: auto;
  overflow: visible;
  background: transparent;
  border: 0;
  box-shadow: none;
}

.tree-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 52px;
  padding: 0 10px 0 12px;
  border-bottom: 1px solid var(--es-color-border);
}

.tree-card-collapsed .tree-card-head {
  min-height: 24px;
  padding: 0;
  background: transparent;
  border-bottom: 0;
  justify-content: center;
}

.tree-card-title,
.table-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.tree-card-body {
  flex: 1 1 auto;
  padding: 12px;
  overflow: auto;
}

.tree-panel-toggle {
  display: inline-flex;
  width: 24px;
  height: 24px;
  padding: 0;
  color: var(--es-color-primary);
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 4px;
  align-items: center;
  justify-content: center;
}

.tree-panel-toggle-icon {
  display: inline-block;
  width: 0;
  height: 0;
  border-top: 5px solid transparent;
  border-right: 7px solid currentcolor;
  border-bottom: 5px solid transparent;
}

.tree-card-collapsed .tree-panel-toggle-icon {
  border-right: 0;
  border-left: 7px solid currentcolor;
}

.tree-panel-toggle:hover {
  color: var(--es-color-primary-hover);
  background: #eff6ff;
  border-color: #93c5fd;
}

.tree-root {
  padding: 0;
  margin: 0;
  list-style: none;
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
  margin: 0;
}

.search-item-secondary {
  margin-left: 10px;
}

.search-label-inline {
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-secondary);
  white-space: nowrap;
  flex-shrink: 0;
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

.workspace-card {
  display: grid;
  min-width: 0;
  min-height: calc(100vh - 146px);
  overflow: hidden;
  grid-template-rows: auto minmax(0, 1fr) auto;
  flex: 1 1 auto;
}

.workspace-search {
  padding: 16px;
  border-bottom: 1px solid var(--es-color-border);
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

.workspace-title-group {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.workspace-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.workspace-current {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.btn {
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

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
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
  width: 140px;
}

.empty-row {
  padding: 16px 0;
  color: var(--es-color-text-placeholder) !important;
  text-align: center !important;
}

.table-loading-wrap {
  display: inline-flex;
  width: 100%;
  align-items: center;
  justify-content: center;
}

.pager {
  padding: 12px 16px 16px;
}

.btn:focus-visible,
.tree-panel-toggle:focus-visible,
.search-input:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}

@media (width <= 1100px) {
  .page {
    flex-direction: column;
  }

  .tree-shell,
  .tree-shell-collapsed {
    width: 100%;
    flex-basis: auto;
  }

  .tree-card {
    order: 2;
  }

  .workspace-card {
    order: 1;
    min-height: auto;
  }
}

@media (width <= 900px) {
  .search-item,
  .search-input {
    width: 100%;
  }

  .search-item {
    justify-content: space-between;
  }

  .search-item-secondary {
    margin-left: 0;
  }

  .search-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }

  .workspace-search,
  .workspace-body {
    padding: 14px;
  }

  .pager {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
