<script setup lang="ts">
import { computed } from 'vue'
import type { SpaceTreeItem } from '@/types/space'

defineOptions({
  name: 'SpaceTreeSelectNode'
})

const props = withDefaults(
  defineProps<{
    node: SpaceTreeItem
    expandedIds: number[]
    selectedId?: number | null
    depth?: number
  }>(),
  {
    depth: 0,
    selectedId: null
  }
)

const emit = defineEmits<{
  toggle: [id: number]
  select: [node: SpaceTreeItem]
}>()

const hasChildren = computed(() => Boolean(props.node.children?.length))
const isExpanded = computed(() => props.expandedIds.includes(props.node.id))
const isSelected = computed(() => props.selectedId === props.node.id)
const rowStyle = computed(() => ({
  paddingLeft: `${props.depth * 16}px`
}))

const handleToggle = () => {
  emit('toggle', props.node.id)
}

const handleSelect = () => {
  emit('select', props.node)
}
</script>

<template>
  <li class="space-tree-node">
    <div :class="['space-tree-row', { 'space-tree-row-selected': isSelected }]" :style="rowStyle">
      <button
        v-if="hasChildren"
        class="space-tree-toggle"
        type="button"
        :aria-label="isExpanded ? '收起空间' : '展开空间'"
        @click.stop="handleToggle"
      >
        <span :class="['space-tree-arrow', { 'space-tree-arrow-expanded': isExpanded }]"></span>
      </button>
      <span v-else class="space-tree-toggle space-tree-toggle-placeholder"></span>
      <button class="space-tree-label" type="button" @click="handleSelect">
        {{ node.name }}
      </button>
    </div>

    <ul v-if="hasChildren && isExpanded" class="space-tree-children">
      <SpaceTreeSelectNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :depth="depth + 1"
        :expanded-ids="expandedIds"
        :selected-id="selectedId"
        @toggle="emit('toggle', $event)"
        @select="emit('select', $event)"
      />
    </ul>
  </li>
</template>

<style scoped>
.space-tree-node {
  list-style: none;
}

.space-tree-node + .space-tree-node {
  margin-top: 2px;
}

.space-tree-children {
  padding: 0;
  margin: 0;
  list-style: none;
}

.space-tree-row {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  align-items: center;
  min-height: 32px;
  padding-right: 8px;
  border-radius: 5px;
}

.space-tree-row-selected {
  background: #eff6ff;
}

.space-tree-toggle,
.space-tree-label {
  padding: 0;
  font: inherit;
  background: transparent;
  border: 0;
}

.space-tree-toggle {
  width: 24px;
  height: 24px;
  color: var(--es-color-text-secondary);
}

.space-tree-toggle-placeholder {
  display: inline-block;
}

.space-tree-arrow {
  display: inline-block;
  width: 0;
  height: 0;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  border-left: 6px solid currentcolor;
  transition: transform 0.2s ease;
}

.space-tree-arrow-expanded {
  transform: rotate(90deg);
}

.space-tree-label {
  min-height: 24px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  text-align: left;
  cursor: pointer;
}

.space-tree-row-selected .space-tree-label {
  font-weight: 600;
  color: var(--es-color-primary);
}

.space-tree-toggle:focus-visible,
.space-tree-label:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}
</style>
