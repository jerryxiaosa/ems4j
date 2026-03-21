<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import DeviceElectricMeterCtModal from '@/components/devices/DeviceElectricMeterCtModal.vue'
import DeviceElectricMeterDetailModal from '@/components/devices/DeviceElectricMeterDetailModal.vue'
import DeviceElectricMeterEditModal from '@/components/devices/DeviceElectricMeterEditModal.vue'
import DeviceElectricMeterSearchPanel from '@/modules/devices/electric-meters/components/DeviceElectricMeterSearchPanel.vue'
import DeviceElectricMeterTableSection from '@/modules/devices/electric-meters/components/DeviceElectricMeterTableSection.vue'
import { useElectricMeterActions } from '@/modules/devices/electric-meters/composables/useElectricMeterActions'
import { useElectricMeterNotice } from '@/modules/devices/electric-meters/composables/useElectricMeterNotice'
import { useElectricMeterQuery } from '@/modules/devices/electric-meters/composables/useElectricMeterQuery'

const { notice, noticeFading, setNotice, dispose } = useElectricMeterNotice()
const {
  rows,
  loading,
  total,
  queryForm,
  selectedIds,
  pagedRows,
  selectedRows,
  isAllChecked,
  initialize,
  loadMeterPage,
  toggleSelectAllOnPage,
  toggleRowSelection,
  syncSelection,
  handleSearch,
  handleReset,
  handlePageChange
} = useElectricMeterQuery({ setNotice })
const {
  editModalVisible,
  editMode,
  editingMeter,
  detailModalVisible,
  detailMeter,
  detailLoading,
  moreActionMenu,
  ctModalVisible,
  ctMeter,
  confirmSubmitting,
  ctSubmitting,
  confirmState,
  closeConfirm,
  handleDocumentClick,
  openBatchProtectConfirm,
  openCreateModal,
  openEditModal,
  openDetailModal,
  toggleMoreActionMenu,
  handleSubmitMeter,
  openDeleteConfirm,
  openSingleCommandConfirm,
  openSingleProtectConfirm,
  openBatchCommandConfirm,
  handleConfirm,
  openCtModal,
  handleSubmitCt
} = useElectricMeterActions({
  setNotice,
  loadMeterPage,
  syncSelection,
  rows,
  selectedRows,
  queryForm
})

onBeforeUnmount(() => {
  dispose()
  document.removeEventListener('click', handleDocumentClick)
})

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
  void initialize()
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
      <DeviceElectricMeterSearchPanel
        :query-form="queryForm"
        @search="handleSearch"
        @reset="handleReset"
      />

      <DeviceElectricMeterTableSection
        :loading="loading"
        :total="total"
        :query-form="queryForm"
        :paged-rows="pagedRows"
        :selected-ids="selectedIds"
        :is-all-checked="isAllChecked"
        :more-action-menu="moreActionMenu"
        @select-all="toggleSelectAllOnPage"
        @select-row="toggleRowSelection"
        @page-change="handlePageChange"
        @batch-command="openBatchCommandConfirm"
        @batch-protect="openBatchProtectConfirm"
        @create="openCreateModal"
        @detail="openDetailModal"
        @edit="openEditModal"
        @delete="openDeleteConfirm"
        @toggle-more="toggleMoreActionMenu"
        @single-command="openSingleCommandConfirm"
        @single-protect="openSingleProtectConfirm"
        @set-ct="openCtModal"
      />
    </section>

    <DeviceElectricMeterEditModal
      v-model="editModalVisible"
      :mode="editMode"
      :meter="editingMeter"
      @submit="handleSubmitMeter"
    />

    <DeviceElectricMeterDetailModal
      v-model="detailModalVisible"
      :meter="detailMeter"
      :loading="detailLoading"
    />

    <DeviceElectricMeterCtModal
      v-model="ctModalVisible"
      :meter="ctMeter"
      :submitting="ctSubmitting"
      @submit="handleSubmitCt"
    />

    <Transition name="meter-modal-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="modal-panel confirm-modal">
          <div class="modal-head">
            <h3 class="modal-title">{{ confirmState.title }}</h3>
            <button class="icon-btn" type="button" :disabled="confirmSubmitting" @click="closeConfirm">
              关闭
            </button>
          </div>
          <div class="modal-body">
            <div :class="['confirm-summary', `confirm-summary-${confirmState.type}`]">
              <p class="confirm-content">{{ confirmState.content }}</p>
              <p v-if="confirmState.subContent" class="confirm-sub-content">
                {{ confirmState.subContent }}
              </p>
            </div>
          </div>
          <div class="modal-actions">
            <button
              class="btn btn-secondary"
              type="button"
              :disabled="confirmSubmitting"
              @click="closeConfirm"
            >
              取消
            </button>
            <button
              class="btn btn-primary"
              type="button"
              :disabled="confirmSubmitting"
              @click="handleConfirm"
            >
              {{ confirmSubmitting ? '处理中...' : confirmState.confirmText }}
            </button>
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

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 41;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
}

.confirm-modal {
  width: min(460px, calc(100vw - 32px));
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

.icon-btn {
  height: 32px;
  min-width: 56px;
  padding: 0 12px;
  color: var(--es-color-text-secondary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.modal-body {
  padding: 20px;
}

.confirm-summary {
  padding: 16px;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.confirm-summary-info {
  background: var(--es-color-info-bg);
  border-color: var(--es-color-info-border);
}

.confirm-summary-error {
  background: var(--es-color-error-bg);
  border-color: var(--es-color-error-border);
}

.confirm-content,
.confirm-sub-content {
  margin: 0;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
}

.confirm-sub-content {
  margin-top: 8px;
  color: var(--es-color-text-secondary);
}

.meter-modal-fade-enter-active,
.meter-modal-fade-leave-active {
  transition: opacity 0.18s ease-out;
}

.meter-modal-fade-enter-active .modal-panel,
.meter-modal-fade-leave-active .modal-panel {
  transition: transform 0.2s ease-out, opacity 0.2s ease-out;
}

.meter-modal-fade-enter-from,
.meter-modal-fade-leave-to {
  opacity: 0;
}

.meter-modal-fade-enter-from .modal-panel,
.meter-modal-fade-leave-to .modal-panel {
  opacity: 0;
  transform: translateY(8px) scale(0.995);
}

.btn:focus-visible,
.icon-btn:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}
</style>
