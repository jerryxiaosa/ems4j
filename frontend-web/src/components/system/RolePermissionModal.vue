<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { fetchRoleDetail, fetchRolePermissionTree, saveRoleMenus } from '@/api/adapters/role'
import RolePermissionTreeNode from '@/components/system/RolePermissionTreeNode.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiErrorState from '@/components/common/UiErrorState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { SystemRoleItem, SystemRolePermissionNode } from '@/modules/system/roles/types'

const props = defineProps<{
  modelValue: boolean
  role: SystemRoleItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const checkedKeys = reactive<Record<string, boolean>>({})
const indeterminateKeys = reactive<Record<string, boolean>>({})
const loading = ref(false)
const saving = ref(false)
const errorText = ref('')
const permissionTree = ref<SystemRolePermissionNode[]>([])

const walkTree = (nodes: SystemRolePermissionNode[], callback: (node: SystemRolePermissionNode) => void) => {
  nodes.forEach((node) => {
    callback(node)
    if (node.children?.length) {
      walkTree(node.children, callback)
    }
  })
}

const resetTreeState = () => {
  walkTree(permissionTree.value, (node) => {
    delete checkedKeys[node.id]
    delete indeterminateKeys[node.id]
  })
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (visible) {
      await loadData()
      return
    }
    resetTreeState()
    permissionTree.value = []
    errorText.value = ''
    loading.value = false
    saving.value = false
  }
)

const setSubtreeChecked = (node: SystemRolePermissionNode, checked: boolean) => {
  checkedKeys[node.id] = checked
  indeterminateKeys[node.id] = false
  node.children?.forEach((child) => setSubtreeChecked(child, checked))
}

const syncNodeState = (node: SystemRolePermissionNode): { checked: boolean; indeterminate: boolean } => {
  if (!node.children?.length) {
    indeterminateKeys[node.id] = false
    return {
      checked: !!checkedKeys[node.id],
      indeterminate: false
    }
  }

  const childStates = node.children.map((child) => syncNodeState(child))
  const allChecked = childStates.every((item) => item.checked)
  const someChecked = childStates.some((item) => item.checked || item.indeterminate)

  checkedKeys[node.id] = allChecked
  indeterminateKeys[node.id] = !allChecked && someChecked

  return {
    checked: checkedKeys[node.id],
    indeterminate: indeterminateKeys[node.id]
  }
}

const syncTreeState = () => {
  permissionTree.value.forEach((node) => {
    syncNodeState(node)
  })
}

const applyCheckedKeys = (menuIds: string[]) => {
  resetTreeState()
  const selectedIds = new Set(menuIds)
  walkTree(permissionTree.value, (node) => {
    checkedKeys[node.id] = selectedIds.has(node.id)
    indeterminateKeys[node.id] = false
  })
  syncTreeState()
}

const getErrorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请稍后重试'
}

const loadData = async () => {
  if (!props.role?.id) {
    permissionTree.value = []
    return
  }

  loading.value = true
  errorText.value = ''

  try {
    const [tree, detail] = await Promise.all([
      fetchRolePermissionTree(),
      fetchRoleDetail(props.role.id)
    ])
    permissionTree.value = tree
    applyCheckedKeys(detail?.menuIds || [])
  } catch (error) {
    permissionTree.value = []
    errorText.value = `权限树加载失败：${getErrorMessage(error)}`
  } finally {
    loading.value = false
  }
}

const collectCheckedIds = (nodes: SystemRolePermissionNode[]): string[] => {
  const result: string[] = []
  walkTree(nodes, (node) => {
    if (checkedKeys[node.id] || indeterminateKeys[node.id]) {
      result.push(node.id)
    }
  })
  return result
}

const handleToggleNode = (payload: { node: SystemRolePermissionNode; checked: boolean }) => {
  setSubtreeChecked(payload.node, payload.checked)
  syncTreeState()
}

const close = () => emit('update:modelValue', false)

const handleSubmit = async () => {
  if (!props.role?.id) {
    close()
    return
  }

  saving.value = true
  try {
    await saveRoleMenus(props.role.id, collectCheckedIds(permissionTree.value))
    emit('saved')
    close()
  } catch (error) {
    errorText.value = `保存权限失败：${getErrorMessage(error)}`
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <Transition name="role-permission-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel role-permission-modal">
        <div class="modal-head">
          <h3 class="modal-title">分配权限</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="role-name">当前角色：{{ role?.name || '--' }}</div>
          <UiLoadingState v-if="loading" :size="20" :thickness="2" :min-height="120" />
          <UiErrorState v-else-if="errorText" :text="errorText" :min-height="120" />
          <UiEmptyState
            v-else-if="permissionTree.length === 0"
            text="暂无权限数据"
            :min-height="120"
          />
          <div v-else class="permission-tree">
            <RolePermissionTreeNode
              v-for="group in permissionTree"
              :key="group.id"
              :node="group"
              :checked-keys="checkedKeys"
              :indeterminate-keys="indeterminateKeys"
              @toggle="handleToggleNode"
            />
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="close">取消</button>
          <button class="btn btn-primary" type="button" :disabled="saving" @click="handleSubmit">
            确定
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
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

.role-permission-modal {
  display: grid;
  width: min(760px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.modal-head,
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
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

.icon-btn,
.btn {
  display: inline-flex;
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  align-items: center;
  justify-content: center;
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

.modal-body {
  padding: 20px;
  overflow: auto;
}

.role-name {
  margin-bottom: 14px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.permission-tree {
  display: grid;
  gap: 10px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.role-permission-fade-enter-active,
.role-permission-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.role-permission-fade-enter-from,
.role-permission-fade-leave-to {
  opacity: 0;
}

.role-permission-fade-enter-from .role-permission-modal,
.role-permission-fade-leave-to .role-permission-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
