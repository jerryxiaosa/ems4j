<script setup lang="ts">
import { computed } from 'vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import { usePermission } from '@/composables/usePermission'
import {
  electricMeterPermissionKeys,
  getOnlineStatusClass,
  getSpaceRegion
} from '@/modules/devices/electric-meters/composables/electricMeterShared'
import type { ElectricMeterMoreActionMenu } from '@/modules/devices/electric-meters/composables/useElectricMeterActions'
import type { ElectricMeterQueryFormState } from '@/modules/devices/electric-meters/composables/useElectricMeterQuery'

interface Props {
  loading: boolean
  total: number
  queryForm: ElectricMeterQueryFormState
  pagedRows: ElectricMeterItem[]
  selectedIds: number[]
  isAllChecked: boolean
  moreActionMenu: ElectricMeterMoreActionMenu | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  selectAll: [checked: boolean]
  selectRow: [row: ElectricMeterItem, checked: boolean]
  pageChange: [payload: { pageNum: number; pageSize: number }]
  batchCommand: [action: 'batch-cut' | 'batch-merge']
  batchProtect: [protect: boolean]
  create: []
  detail: [row: ElectricMeterItem]
  edit: [row: ElectricMeterItem]
  delete: [row: ElectricMeterItem]
  toggleMore: [row: ElectricMeterItem, event: MouseEvent]
  singleCommand: [row: ElectricMeterItem, action: 'cut' | 'merge']
  singleProtect: [row: ElectricMeterItem, protect: boolean]
  setCt: [row: ElectricMeterItem]
}>()

const { hasMenuPermission } = usePermission()

const canOpenMoreActionMenu = (row: ElectricMeterItem) => {
  const commandPermissionKey =
    row.status === 0
      ? electricMeterPermissionKeys.switchOff
      : electricMeterPermissionKeys.switchOn
  const protectionPermissionKey = row.protectedModel
    ? electricMeterPermissionKeys.disableProtection
    : electricMeterPermissionKeys.enableProtection

  return (
    hasMenuPermission(protectionPermissionKey) ||
    (row.onlineStatus === 1 &&
      (hasMenuPermission(commandPermissionKey) ||
        hasMenuPermission(electricMeterPermissionKeys.setCt)))
  )
}

const getSerialNumber = (index: number) => {
  return (props.queryForm.pageNum - 1) * props.queryForm.pageSize + index + 1
}

const moreActionCommandLabel = computed(() => {
  if (!props.moreActionMenu) {
    return '断闸'
  }

  return props.moreActionMenu.row.status === 0 ? '断闸' : '合闸'
})
</script>

<template>
  <div class="workspace-body">
    <div class="workspace-head">
      <h2 class="workspace-title">智能电表</h2>
      <div class="page-actions">
        <button
          v-menu-permission="electricMeterPermissionKeys.batchSwitchOn"
          class="btn btn-secondary"
          type="button"
          @click="emit('batchCommand', 'batch-merge')"
        >
          批量合闸
        </button>
        <button
          v-menu-permission="electricMeterPermissionKeys.batchSwitchOff"
          class="btn btn-secondary"
          type="button"
          @click="emit('batchCommand', 'batch-cut')"
        >
          批量断闸
        </button>
        <button
          v-menu-permission="electricMeterPermissionKeys.batchEnableProtection"
          class="btn btn-secondary"
          type="button"
          @click="emit('batchProtect', true)"
        >
          保电
        </button>
        <button
          v-menu-permission="electricMeterPermissionKeys.batchDisableProtection"
          class="btn btn-secondary"
          type="button"
          @click="emit('batchProtect', false)"
        >
          取消保电
        </button>
        <button
          v-menu-permission="electricMeterPermissionKeys.create"
          class="btn btn-primary"
          type="button"
          @click="emit('create')"
        >
          添加
        </button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="table meter-table">
        <thead>
          <tr>
            <th class="checkbox-col sticky-col sticky-col-left sticky-checkbox">
              <input
                type="checkbox"
                :checked="isAllChecked"
                @change="emit('selectAll', ($event.target as HTMLInputElement).checked)"
              />
            </th>
            <th class="table-col-index sticky-col sticky-col-left sticky-index">序号</th>
            <th class="meter-col-name sticky-col sticky-col-left sticky-name">电表名称</th>
            <th class="meter-col-no sticky-col sticky-col-left sticky-no">电表编号</th>
            <th>电表型号</th>
            <th class="meter-col-location">所在位置</th>
            <th class="meter-col-region">所属区域</th>
            <th class="meter-col-gateway">接入网关</th>
            <th>是否预付费</th>
            <th class="meter-col-plan">计费方案名称</th>
            <th>是否保电</th>
            <th class="meter-col-plan">预警方案名称</th>
            <th class="meter-col-warn-type">电费预警级别名称</th>
            <th>是否计量</th>
            <th>通讯模式</th>
            <th class="meter-col-status sticky-col sticky-col-right sticky-online">在线状态</th>
            <th class="meter-col-offline-duration sticky-col sticky-col-right sticky-offline-duration">
              离线时长
            </th>
            <th class="meter-col-status sticky-col sticky-col-right sticky-switch-status">表计状态</th>
            <th class="meter-col-action sticky-col sticky-col-right sticky-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="18" class="empty-row">
              <UiLoadingState :size="18" :thickness="2" :min-height="56" />
            </td>
          </tr>
          <tr v-else-if="!pagedRows.length">
            <td colspan="18" class="empty-row">
              <UiEmptyState :min-height="56" />
            </td>
          </tr>
          <tr v-for="(row, index) in pagedRows" :key="row.id">
            <td class="checkbox-col sticky-col sticky-col-left sticky-checkbox">
              <input
                type="checkbox"
                :checked="selectedIds.includes(row.id)"
                @change="emit('selectRow', row, ($event.target as HTMLInputElement).checked)"
              />
            </td>
            <td class="sticky-col sticky-col-left sticky-index">{{ getSerialNumber(index) }}</td>
            <td class="sticky-col sticky-col-left sticky-name">{{ row.meterName }}</td>
            <td class="sticky-col sticky-col-left sticky-no">{{ row.deviceNo }}</td>
            <td>{{ row.modelName }}</td>
            <td class="meter-col-location">{{ row.spaceName || '--' }}</td>
            <td>{{ getSpaceRegion(row) }}</td>
            <td>{{ row.gatewayName || '--' }}</td>
            <td>{{ row.payType === 1 ? '是' : '否' }}</td>
            <td>{{ row.pricePlanName || '--' }}</td>
            <td>{{ row.protectedModel ? '是' : '否' }}</td>
            <td>{{ row.warnPlanName || '--' }}</td>
            <td>{{ row.electricWarnTypeName || '--' }}</td>
            <td>{{ row.isCalculate ? '是' : '否' }}</td>
            <td>{{ row.communicateModel }}</td>
            <td class="sticky-col sticky-col-right sticky-online">
              <span :class="getOnlineStatusClass(row)">
                {{ row.onlineStatusName }}
              </span>
            </td>
            <td class="sticky-col sticky-col-right sticky-offline-duration">
              {{ row.onlineStatus === 0 ? row.offlineDuration || '--' : '--' }}
            </td>
            <td class="sticky-col sticky-col-right sticky-switch-status">{{ row.statusName }}</td>
            <td class="meter-col-action sticky-col sticky-col-right sticky-action">
              <div class="meter-row-actions">
                <button
                  v-menu-permission="electricMeterPermissionKeys.detail"
                  class="btn-link"
                  type="button"
                  @click="emit('detail', row)"
                >
                  详情
                </button>
                <button
                  v-menu-permission="electricMeterPermissionKeys.edit"
                  class="btn-link"
                  type="button"
                  @click="emit('edit', row)"
                >
                  编辑
                </button>
                <button
                  v-menu-permission="electricMeterPermissionKeys.delete"
                  class="btn-link btn-link-danger"
                  type="button"
                  @click="emit('delete', row)"
                >
                  删除
                </button>
                <div v-if="canOpenMoreActionMenu(row)" class="more-action-wrap">
                  <button
                    class="btn-link btn-link-more"
                    type="button"
                    @click.stop="emit('toggleMore', row, $event as MouseEvent)"
                  >
                    ...
                  </button>
                </div>
              </div>
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
      @change="emit('pageChange', $event)"
    />
  </div>

  <div
    v-if="moreActionMenu"
    class="more-action-floating-menu"
    :style="{ top: `${moreActionMenu.top}px`, left: `${moreActionMenu.left}px` }"
  >
    <button
      v-if="
        moreActionMenu.row.onlineStatus === 1 &&
        hasMenuPermission(
          moreActionMenu.row.status === 0
            ? electricMeterPermissionKeys.switchOff
            : electricMeterPermissionKeys.switchOn
        )
      "
      class="more-action-item"
      type="button"
      @click="emit('singleCommand', moreActionMenu.row, moreActionMenu.row.status === 0 ? 'cut' : 'merge')"
    >
      {{ moreActionCommandLabel }}
    </button>
    <button
      v-if="
        hasMenuPermission(
          moreActionMenu.row.protectedModel
            ? electricMeterPermissionKeys.disableProtection
            : electricMeterPermissionKeys.enableProtection
        )
      "
      class="more-action-item"
      type="button"
      @click="emit('singleProtect', moreActionMenu.row, !moreActionMenu.row.protectedModel)"
    >
      {{ moreActionMenu.row.protectedModel ? '取消保电' : '保电' }}
    </button>
    <button
      v-if="
        moreActionMenu.row.onlineStatus === 1 &&
        hasMenuPermission(electricMeterPermissionKeys.setCt)
      "
      class="more-action-item"
      type="button"
      @click="emit('setCt', moreActionMenu.row)"
    >
      设置CT
    </button>
  </div>
</template>

<style scoped>
.workspace-body {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
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
  flex: 0 0 auto;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  min-width: 520px;
  justify-content: flex-end;
  flex-wrap: nowrap;
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
  overflow: scroll visible;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  scrollbar-gutter: stable;
}

.table-wrap::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.table-wrap::-webkit-scrollbar-track {
  background: #eef3fb;
  border-radius: 999px;
}

.table-wrap::-webkit-scrollbar-thumb {
  background: #c3d4ee;
  border-radius: 999px;
}

.table-wrap:hover::-webkit-scrollbar-thumb {
  background: #9fb8df;
}

.table {
  width: max-content;
  min-width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.table th,
.table td {
  padding: 12px 14px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  vertical-align: middle;
  border-bottom: 1px solid var(--es-color-border);
}

.table th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.checkbox-col {
  width: 48px;
  text-align: center !important;
}

.checkbox-col input {
  width: 14px;
  height: 14px;
}

.table-col-index {
  width: 84px;
}

.meter-col-name {
  width: 140px;
}

.meter-col-no {
  width: 140px;
}

.meter-col-region {
  width: 168px;
}

.meter-col-gateway {
  width: 180px;
}

.meter-col-location {
  width: 132px;
}

.meter-col-plan {
  width: 164px;
}

.meter-col-warn-type {
  width: 154px;
}

.meter-col-status {
  width: 88px;
}

.meter-col-offline-duration {
  width: 112px;
}

.meter-col-action {
  width: 232px;
}

.meter-table th.meter-col-action,
.meter-table td.meter-col-action {
  padding-left: 34px;
}

.sticky-col {
  position: sticky;
  z-index: 2;
  background: #fff;
}

.table th.sticky-col {
  z-index: 4;
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.sticky-col-left {
  box-shadow: 1px 0 0 var(--es-color-border);
}

.sticky-col-right {
  box-shadow: -1px 0 0 var(--es-color-border);
}

.sticky-checkbox {
  left: 0;
}

.sticky-index {
  left: 48px;
}

.sticky-name {
  left: 132px;
}

.sticky-no {
  left: 272px;
}

.sticky-action {
  right: 0;
  z-index: 5;
}

.sticky-switch-status {
  right: 232px;
}

.sticky-offline-duration {
  right: 320px;
}

.sticky-online {
  right: 432px;
}

.meter-table tbody tr:hover td {
  background: #fafcff;
}

.meter-table tbody tr:hover .sticky-col {
  background: #fafcff;
}

.empty-row {
  padding: 16px 0;
  color: var(--es-color-text-placeholder) !important;
  text-align: center !important;
}

.meter-row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  white-space: nowrap;
}

.btn-link,
.btn-link-danger,
.btn-link-more {
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  line-height: 1.2;
  color: var(--es-color-primary);
  background: transparent;
  border: 0;
}

.btn-link-danger {
  color: var(--es-color-danger);
}

.btn-link:hover {
  color: var(--es-color-primary-hover);
}

.btn-link-more {
  min-width: 18px;
  font-weight: 600;
  color: var(--es-color-text-secondary);
}

.btn-link-more:hover {
  color: var(--es-color-primary);
}

.btn-link-danger:hover {
  opacity: 0.85;
}

.more-action-floating-menu {
  position: fixed;
  z-index: 70;
  min-width: 104px;
  padding: 6px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: 0 12px 28px rgb(15 23 42 / 14%);
}

.more-action-item {
  display: block;
  width: 100%;
  height: 30px;
  padding: 0 8px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 4px;
}

.more-action-item:hover {
  color: var(--es-color-primary);
  background: #eff6ff;
}

.meter-online-status {
  font-weight: 500;
}

.meter-online-status-online {
  color: var(--es-color-success-text);
}

.meter-online-status-offline {
  color: var(--es-color-error-text);
}

.meter-online-status-unknown {
  font-weight: 500;
  color: var(--es-color-text-placeholder);
}

.pager {
  padding: 12px 0 0;
  margin-top: 12px;
}

.btn:focus-visible,
.btn-link:focus-visible,
.btn-link-danger:focus-visible,
.btn-link-more:focus-visible,
.more-action-item:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}

@media (width <= 1200px) {
  .workspace-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .page-actions {
    width: auto;
    max-width: 100%;
    min-width: 0;
    padding-bottom: 2px;
    overflow-x: auto;
    flex-wrap: nowrap;
  }
}
</style>
