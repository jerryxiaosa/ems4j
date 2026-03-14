<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'DeviceCategoryTreeNode'
})

interface DeviceCategoryTreeNodeData {
  id: number
  label: string
  children?: DeviceCategoryTreeNodeData[]
}

const props = withDefaults(
  defineProps<{
    node: DeviceCategoryTreeNodeData
    expandedNodeIds: number[]
    selectedNodeId: number
    depth?: number
  }>(),
  {
    depth: 0
  }
)

const emit = defineEmits<{
  toggle: [nodeId: number]
  select: [nodeId: number]
}>()

const hasChildren = computed(() => Boolean(props.node.children?.length))
const isLeaf = computed(() => !hasChildren.value)
const isExpanded = computed(() => props.expandedNodeIds.includes(props.node.id))
const isSelected = computed(() => props.selectedNodeId === props.node.id)
const rowStyle = computed(() => ({
  paddingLeft: `${props.depth * 16}px`
}))

const handleToggle = () => {
  emit('toggle', props.node.id)
}

const handleSelect = () => {
  if (!isLeaf.value) {
    return
  }
  emit('select', props.node.id)
}
</script>

<template>
  <li class="tree-node">
    <div :class="['tree-node-row', { 'tree-node-row-selected': isSelected }]" :style="rowStyle">
      <button
        v-if="hasChildren"
        class="tree-toggle"
        type="button"
        :aria-label="isExpanded ? '收起分类' : '展开分类'"
        @click="handleToggle"
      >
        <span :class="['tree-arrow', { 'tree-arrow-expanded': isExpanded }]"></span>
      </button>
      <span v-else class="tree-toggle tree-toggle-placeholder"></span>
      <button v-if="isLeaf" class="tree-label tree-label-leaf" type="button" @click="handleSelect">
        {{ node.label }}
      </button>
      <span v-else class="tree-label tree-label-text">
        {{ node.label }}
      </span>
    </div>

    <ul v-if="hasChildren && isExpanded" class="tree-children">
      <DeviceCategoryTreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        :expanded-node-ids="expandedNodeIds"
        :selected-node-id="selectedNodeId"
        @toggle="emit('toggle', $event)"
        @select="emit('select', $event)"
      />
    </ul>
  </li>
</template>

<style scoped>
.tree-node {
  list-style: none;
}

.tree-node + .tree-node {
  margin-top: 4px;
}

.tree-children {
  padding: 0;
  margin: 0;
  list-style: none;
}

.tree-node-row {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  align-items: center;
  min-height: 38px;
  padding-right: 8px;
  border-radius: 5px;
  transition: background-color 0.2s ease;
}

.tree-node-row-selected {
  background: #eff6ff;
}

.tree-toggle,
.tree-label {
  padding: 0;
  font: inherit;
  background: transparent;
  border: 0;
}

.tree-toggle {
  width: 24px;
  height: 24px;
  color: var(--es-color-text-secondary);
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

.tree-toggle-placeholder {
  display: inline-block;
}

.tree-label {
  font-size: var(--es-font-size-md);
  color: var(--es-color-text-primary);
  text-align: left;
}

.tree-label-leaf {
  cursor: pointer;
}

.tree-label-text {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
}

.tree-node-row-selected .tree-label {
  font-weight: 600;
  color: var(--es-color-primary);
}

.tree-toggle:focus-visible,
.tree-label:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}
</style>
