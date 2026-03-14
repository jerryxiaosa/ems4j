<script setup lang="ts">
import { computed } from 'vue'
import type { SystemOrganizationItem } from '@/modules/system/organizations/types'

interface DetailCell {
  label: string
  value: string
}

const props = defineProps<{
  modelValue: boolean
  organization: SystemOrganizationItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const rows = computed(() => {
  const item = props.organization
  if (!item) {
    return []
  }

  const cells: DetailCell[] = [
    { label: '机构名称', value: item.name || '--' },
    { label: '机构编码', value: item.code || '--' },
    { label: '机构类型', value: item.typeName || '--' },
    { label: '负责人名称', value: item.managerName || '--' },
    { label: '负责人电话', value: item.managerPhone || '--' },
    { label: '机构地址', value: item.address || '--' },
    { label: '入驻日期', value: item.settledAt || '--' },
    { label: '创建时间', value: item.createTime || '--' },
    { label: '修改时间', value: item.updateTime || '--' }
  ]

  return [
    [cells[0], cells[1]],
    [cells[2], cells[3]],
    [cells[4], cells[5]],
    [cells[6], cells[7]],
    [cells[8], null]
  ] as Array<[DetailCell, DetailCell | null]>
})

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="organization-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel organization-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">机构详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="summary-grid">
            <div v-for="[leftCell, rightCell] in rows" :key="leftCell.label" class="summary-row">
              <div class="summary-item">
                <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
              </div>
              <div v-if="rightCell" class="summary-item">
                <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
              </div>
            </div>
            <div class="summary-row summary-row-full">
              <div class="summary-item summary-item-full">
                <span class="summary-label es-detail-label">备注说明</span>
                <span class="summary-value es-detail-value-box summary-remark">{{
                  organization?.remark || '--'
                }}</span>
              </div>
            </div>
          </div>
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

.organization-detail-modal {
  display: flex;
  width: min(820px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  flex-direction: column;
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
}

.modal-title {
  margin: 0;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.icon-btn {
  height: 36px;
  min-width: 64px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.modal-body {
  flex: 1;
  min-height: 0;
  padding: 20px 20px 32px;
  overflow: auto;
}

.summary-grid {
  display: grid;
  gap: 10px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 56px;
}

.summary-row-full {
  grid-template-columns: minmax(0, 1fr);
}

.summary-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
  min-width: 0;
}

.summary-item-full {
  grid-template-columns: 96px minmax(0, 1fr);
}

.summary-remark {
  min-height: 68px;
  align-items: flex-start;
  line-height: 1.6;
}

.organization-detail-fade-enter-active,
.organization-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.organization-detail-fade-enter-from,
.organization-detail-fade-leave-to {
  opacity: 0;
}

.organization-detail-fade-enter-from .organization-detail-modal,
.organization-detail-fade-leave-to .organization-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
