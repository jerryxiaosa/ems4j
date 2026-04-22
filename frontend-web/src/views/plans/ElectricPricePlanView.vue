<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import {
  addElectricPricePlan,
  deleteElectricPricePlan,
  fetchDefaultStepPrices,
  fetchDefaultElectricPrices,
  fetchElectricPricePlanDetail,
  fetchDefaultElectricTimes,
  fetchElectricPricePlanList,
  updateDefaultElectricPrices,
  updateDefaultElectricTimes,
  updateElectricPricePlan
} from '@/api/adapters/plan'
import UiTableStateOverlay from '@/components/common/UiTableStateOverlay.vue'
import ElectricPricePlanDetailModal from '@/components/plans/ElectricPricePlanDetailModal.vue'
import ElectricPricePlanEditModal from '@/components/plans/ElectricPricePlanEditModal.vue'
import ElectricPricePlanStandardModal from '@/components/plans/ElectricPricePlanStandardModal.vue'
import ElectricPricePlanTimeModal, {
  type ElectricPriceTimeRow
} from '@/components/plans/ElectricPricePlanTimeModal.vue'
import { defaultStandardElectricPrices } from '@/components/plans/electric-price-plan.mock'
import type {
  ElectricPriceDefaultStepPrice,
  ElectricPricePlanFormValue,
  ElectricPricePlanItem,
  ElectricPricePlanSavePayload,
  ElectricPriceStandardPrice
} from '@/types/electric-price-plan'

const electricPricePlanPermissionKeys = {
  create: 'plan_management_electric_price_plan_create',
  detail: 'plan_management_electric_price_plan_detail',
  edit: 'plan_management_electric_price_plan_edit',
  delete: 'plan_management_electric_price_plan_delete',
  standardPriceSetting: 'plan_management_electric_price_plan_standard_price_setting',
  timePeriodSetting: 'plan_management_electric_price_plan_time_period_setting'
} as const

const rows = ref<ElectricPricePlanItem[]>([])
const loading = ref(false)
const standardPrices = ref<ElectricPriceStandardPrice>({ ...defaultStandardElectricPrices })
const formStandardPrices = ref<ElectricPriceStandardPrice>({ ...defaultStandardElectricPrices })
const defaultStepPrices = ref<ElectricPriceDefaultStepPrice>({
  step1End: '',
  step1Ratio: '1',
  step2End: '',
  step2Ratio: '1.2',
  step3End: '',
  step3Ratio: '1.3'
})
const peakTimeRows = ref<ElectricPriceTimeRow[]>([
  { type: 'lower', time: '00:00' },
  { type: 'low', time: '06:00' },
  { type: 'lower', time: '11:00' },
  { type: 'low', time: '13:00' },
  { type: 'high', time: '14:00' },
  { type: 'low', time: '22:00' }
])

const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})
const noticeFading = ref(false)
let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null
const NOTICE_VISIBLE_MS = 5000
const NOTICE_FADE_MS = 300

const queryForm = reactive({
  name: ''
})

const editModalVisible = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const editingPlan = ref<ElectricPricePlanItem | null>(null)
const detailVisible = ref(false)
const detailPlan = ref<ElectricPricePlanItem | null>(null)

const standardSettingVisible = ref(false)
const peakSettingVisible = ref(false)

const confirmState = reactive({
  visible: false,
  targetId: 0,
  title: '',
  content: ''
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

const getSerialNumber = (index: number) => index + 1

const loadStandardPrices = async () => {
  try {
    standardPrices.value = await fetchDefaultElectricPrices()
  } catch (error) {
    standardPrices.value = { ...defaultStandardElectricPrices }
    setNotice('error', error instanceof Error ? error.message : '标准电价加载失败')
  }
}

const loadPeakTimes = async () => {
  try {
    peakTimeRows.value = await fetchDefaultElectricTimes()
  } catch (error) {
    peakTimeRows.value = [
      { type: 'lower', time: '00:00' },
      { type: 'low', time: '06:00' },
      { type: 'lower', time: '11:00' },
      { type: 'low', time: '13:00' },
      { type: 'high', time: '14:00' },
      { type: 'low', time: '22:00' }
    ]
    setNotice('error', error instanceof Error ? error.message : '尖峰平谷时段加载失败')
  }
}

const loadDefaultStepPrices = async () => {
  try {
    defaultStepPrices.value = await fetchDefaultStepPrices()
  } catch (error) {
    defaultStepPrices.value = {
      step1End: '',
      step1Ratio: '1',
      step2End: '',
      step2Ratio: '1.2',
      step3End: '',
      step3Ratio: '1.3'
    }
    setNotice('error', error instanceof Error ? error.message : '默认阶梯电价加载失败')
  }
}

const loadList = async () => {
  loading.value = true
  try {
    rows.value = await fetchElectricPricePlanList({
      name: queryForm.name.trim() || undefined
    })
  } catch (error) {
    rows.value = []
    setNotice('error', error instanceof Error ? error.message : '电价方案列表加载失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  void loadList()
}

const resetQuery = () => {
  queryForm.name = ''
  void loadList()
}

onMounted(() => {
  void loadStandardPrices()
  void loadPeakTimes()
  void loadDefaultStepPrices()
  void loadList()
})

const openCreateModal = () => {
  editMode.value = 'create'
  editingPlan.value = null
  formStandardPrices.value = { ...standardPrices.value }
  editModalVisible.value = true
}

const openEditModal = async (row: ElectricPricePlanItem) => {
  try {
    editingPlan.value = await fetchElectricPricePlanDetail(row.id)
    editMode.value = 'edit'
    formStandardPrices.value = { ...standardPrices.value }
    editModalVisible.value = true
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '电价方案详情加载失败')
  }
}

const openDetailModal = async (row: ElectricPricePlanItem) => {
  try {
    detailPlan.value = await fetchElectricPricePlanDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '电价方案详情加载失败')
  }
}

const buildStepPricePayload = (
  payload: ElectricPricePlanFormValue
): ElectricPricePlanSavePayload['stepPrices'] => {
  if (payload.hasStepPrice !== 'true') {
    return undefined
  }

  return [
    {
      start: 0,
      end: Number(payload.step1End),
      value: Number(payload.step1Ratio)
    },
    {
      start: Number(payload.step1End),
      end: Number(payload.step2End),
      value: Number(payload.step2Ratio)
    },
    {
      start: Number(payload.step2End),
      value: Number(payload.step3Ratio)
    }
  ]
}

const buildSavePayload = (payload: ElectricPricePlanFormValue): ElectricPricePlanSavePayload => {
  const isCustomPrice = payload.isCustomPrice === 'true'
  return {
    name: payload.name,
    priceHigher: Number(payload.priceHigher),
    priceHigh: Number(payload.priceHigh),
    priceLow: Number(payload.priceLow),
    priceLower: Number(payload.priceLower),
    priceDeepLow: Number(payload.priceDeepLow),
    isStep: payload.hasStepPrice === 'true',
    stepPrices: buildStepPricePayload(payload),
    isCustomPrice,
    ...(isCustomPrice
      ? {}
      : {
          priceHigherMultiply: Number(payload.ratioHigher),
          priceHighMultiply: Number(payload.ratioHigh),
          priceLowMultiply: Number(payload.ratioLow),
          priceLowerMultiply: Number(payload.ratioLower),
          priceDeepLowMultiply: Number(payload.ratioDeepLow)
        })
  }
}

const handleSubmitPlan = async (payload: ElectricPricePlanFormValue) => {
  if (editMode.value === 'create') {
    try {
      await addElectricPricePlan(buildSavePayload(payload))
      editModalVisible.value = false
      await loadList()
      setNotice('success', '电价方案添加成功')
    } catch (error) {
      setNotice('error', error instanceof Error ? error.message : '电价方案添加失败')
    }
    return
  }

  if (!payload.id) {
    setNotice('error', '电价方案缺少 ID，无法更新')
    return
  }

  try {
    await updateElectricPricePlan(payload.id, buildSavePayload(payload))
    editModalVisible.value = false
    await loadList()
    setNotice('success', '电价方案信息更新成功')
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '电价方案更新失败')
  }
}

const handleSubmitStandardPrices = async (payload: ElectricPriceStandardPrice) => {
  try {
    await updateDefaultElectricPrices(payload)
    standardPrices.value = { ...payload }
    standardSettingVisible.value = false
    setNotice('success', '标准电价设置成功')
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '标准电价设置失败')
  }
}

const handleSubmitPeakTimes = async (payload: ElectricPriceTimeRow[]) => {
  try {
    await updateDefaultElectricTimes(payload)
    peakTimeRows.value = payload
    peakSettingVisible.value = false
    setNotice('success', '尖峰平谷设置成功')
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '尖峰平谷设置失败')
  }
}

const openDeleteConfirm = (row: ElectricPricePlanItem) => {
  confirmState.visible = true
  confirmState.targetId = row.id
  confirmState.title = '删除确认'
  confirmState.content = `是否删除电价方案“${row.name}”？`
}

const closeConfirm = () => {
  confirmState.visible = false
  confirmState.targetId = 0
  confirmState.title = ''
  confirmState.content = ''
}

const handleConfirmDelete = async () => {
  try {
    await deleteElectricPricePlan(confirmState.targetId)
    closeConfirm()
    await loadList()
    setNotice('success', '电价方案删除成功')
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '电价方案删除失败')
  }
}
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

    <section class="search-card">
      <div class="search-row">
        <label class="search-item">
          <span class="search-label-inline">方案名称</span>
          <input
            v-model="queryForm.name"
            class="search-input"
            type="text"
            placeholder="请输入电价方案名称"
          />
        </label>

        <div class="search-actions">
          <button class="btn btn-primary" type="button" @click="search">查询</button>
          <button class="btn btn-secondary" type="button" @click="resetQuery">重置</button>
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-toolbar table-toolbar-with-actions">
        <h2 class="table-title">电价方案</h2>
        <div class="page-actions">
          <button
            v-menu-permission="electricPricePlanPermissionKeys.standardPriceSetting"
            class="btn btn-secondary"
            type="button"
            @click="standardSettingVisible = true"
          >
            标准电价设置
          </button>
          <button
            v-menu-permission="electricPricePlanPermissionKeys.timePeriodSetting"
            class="btn btn-secondary"
            type="button"
            @click="peakSettingVisible = true"
          >
            尖峰平谷时间设置
          </button>
          <button
            v-menu-permission="electricPricePlanPermissionKeys.create"
            class="btn btn-primary"
            type="button"
            @click="openCreateModal"
          >
            添加
          </button>
        </div>
      </div>

      <div class="table-wrap">
        <UiTableStateOverlay
          :loading="loading"
          :empty="!loading && !rows.length"
        />
        <table class="table">
          <thead>
            <tr>
              <th class="table-col-index">序号</th>
              <th class="electric-plan-col-name">电价方案名称</th>
              <th class="electric-plan-col-price">尖</th>
              <th class="electric-plan-col-price">峰</th>
              <th class="electric-plan-col-price">平</th>
              <th class="electric-plan-col-price">谷</th>
              <th class="electric-plan-col-price">深谷</th>
              <th class="electric-plan-col-step">阶梯电费</th>
              <th class="electric-plan-col-time">创建时间</th>
              <th class="electric-plan-col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in rows" :key="row.id">
              <td>{{ getSerialNumber(index) }}</td>
              <td>{{ row.name }}</td>
              <td>{{ row.priceHigher }}</td>
              <td>{{ row.priceHigh }}</td>
              <td>{{ row.priceLow }}</td>
              <td>{{ row.priceLower }}</td>
              <td>{{ row.priceDeepLow }}</td>
              <td>{{ row.hasStepPrice ? '是' : '否' }}</td>
              <td>{{ row.createTime || '--' }}</td>
              <td class="electric-plan-col-action">
                <button
                  v-menu-permission="electricPricePlanPermissionKeys.detail"
                  class="btn-link"
                  type="button"
                  @click="openDetailModal(row)"
                >
                  详情
                </button>
                <button
                  v-menu-permission="electricPricePlanPermissionKeys.edit"
                  class="btn-link"
                  type="button"
                  @click="openEditModal(row)"
                >
                  编辑
                </button>
                <button
                  v-menu-permission="electricPricePlanPermissionKeys.delete"
                  class="btn-link btn-link-danger"
                  type="button"
                  @click="openDeleteConfirm(row)"
                  >删除</button
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ElectricPricePlanEditModal
      v-model="editModalVisible"
      :mode="editMode"
      :plan="editingPlan"
      :standard-prices="formStandardPrices"
      :default-step-prices="defaultStepPrices"
      @submit="handleSubmitPlan"
    />

    <ElectricPricePlanDetailModal v-model="detailVisible" :plan="detailPlan" />

    <ElectricPricePlanStandardModal
      v-model="standardSettingVisible"
      :prices="standardPrices"
      @submit="handleSubmitStandardPrices"
    />

    <ElectricPricePlanTimeModal
      v-model="peakSettingVisible"
      :rows="peakTimeRows"
      @submit="handleSubmitPeakTimes"
    />

    <Transition name="electric-plan-modal-fade" appear>
      <div v-if="confirmState.visible" class="modal-mask" @click.self.prevent>
        <div class="modal-panel confirm-modal">
          <div class="modal-head">
            <h3 class="modal-title">{{ confirmState.title }}</h3>
            <button class="icon-btn" type="button" @click="closeConfirm">关闭</button>
          </div>
          <div class="modal-body">
            <div class="confirm-summary confirm-summary-error">
              <p class="confirm-content">{{ confirmState.content }}</p>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" type="button" @click="closeConfirm">取消</button>
            <button class="btn btn-primary" type="button" @click="handleConfirmDelete">确定</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.page {
  position: relative;
  display: grid;
  gap: 16px;
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

.search-card,
.table-card {
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
  padding: 16px;
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
}

.search-label-inline {
  flex-shrink: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-secondary);
  white-space: nowrap;
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

.table-card {
  display: grid;
  min-height: calc(100vh - 146px);
  overflow: hidden;
  grid-template-rows: auto minmax(0, 1fr);
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.table-toolbar-with-actions {
  align-items: flex-start;
}

.table-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.btn,
.icon-btn {
  display: inline-flex;
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  align-items: center;
  justify-content: center;
  gap: 6px;
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

.table-wrap {
  position: relative;
  min-height: 120px;
  overflow: hidden auto;
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
  width: 84px;
}

.electric-plan-col-name {
  width: 180px;
}

.electric-plan-col-price {
  width: 80px;
}

.electric-plan-col-step {
  width: 112px;
}

.electric-plan-col-time {
  width: 176px;
}

.electric-plan-col-action {
  width: 160px;
}

.table-loading-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.empty-row {
  padding: 16px 0;
  color: var(--es-color-text-placeholder) !important;
  text-align: center !important;
}

.btn-link {
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  line-height: 1.2;
  color: var(--es-color-primary);
  cursor: pointer;
  background: transparent;
  border: 0;
}

.btn-link + .btn-link {
  margin-left: 12px;
}

.btn-link-danger {
  color: var(--es-color-error-text);
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

.confirm-modal {
  width: min(420px, calc(100vw - 32px));
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

.modal-body {
  padding: 20px;
}

.confirm-summary {
  padding: 14px 16px;
  border-radius: 5px;
}

.confirm-summary-error {
  color: var(--es-color-error-text);
  background: var(--es-color-error-bg);
}

.confirm-content {
  margin: 0;
  line-height: 1.6;
}

.electric-plan-modal-fade-enter-active,
.electric-plan-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.electric-plan-modal-fade-enter-from,
.electric-plan-modal-fade-leave-to {
  opacity: 0;
}

.electric-plan-modal-fade-enter-from .confirm-modal,
.electric-plan-modal-fade-leave-to .confirm-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
