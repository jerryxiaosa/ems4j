<script setup lang="ts">
import type { SystemRolePermissionNode } from '@/modules/system/roles/types'

defineOptions({
  name: 'RolePermissionTreeNode'
})

const props = defineProps<{
  node: SystemRolePermissionNode
  checkedKeys: Record<string, boolean>
  indeterminateKeys: Record<string, boolean>
}>()

const emit = defineEmits<{
  toggle: [payload: { node: SystemRolePermissionNode; checked: boolean }]
}>()

const handleToggle = (event: Event) => {
  emit('toggle', {
    node: props.node,
    checked: (event.target as HTMLInputElement).checked
  })
}
</script>

<template>
  <div class="permission-node-wrap">
    <label :class="['permission-node', { 'is-parent': node.children?.length }]">
      <input
        :checked="!!checkedKeys[node.id]"
        :indeterminate.prop="!!indeterminateKeys[node.id]"
        type="checkbox"
        @change="handleToggle"
      />
      <span>{{ node.label }}</span>
    </label>

    <div v-if="node.children?.length" class="permission-node-children">
      <RolePermissionTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :checked-keys="checkedKeys"
        :indeterminate-keys="indeterminateKeys"
        @toggle="emit('toggle', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
.permission-node-wrap {
  display: grid;
  gap: 10px;
}

.permission-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
}

.permission-node.is-parent {
  font-weight: 600;
}

.permission-node-children {
  display: grid;
  gap: 10px;
  margin-left: 12px;
  padding-left: 16px;
  border-left: 1px dashed var(--es-color-border);
}
</style>
