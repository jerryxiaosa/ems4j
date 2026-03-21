<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { fetchDeviceModelList, fetchGatewayList } from '@/api/adapters/device'
import { fetchSpaceTree } from '@/api/adapters/space'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiErrorState from '@/components/common/UiErrorState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import SpaceTreeSelectNode from '@/components/devices/SpaceTreeSelectNode.vue'
import {
  createDefaultMeterForm,
  gatewayOptions as fallbackGatewayOptions,
  modelOptions as fallbackModelOptions,
  type ElectricMeterGatewayOption,
  type ElectricMeterModelOption,
  type ElectricMeterFormValue,
  type ElectricMeterItem
} from '@/components/devices/electric-meter.mock'
import type { SpaceTreeItem } from '@/types/space'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  meter: ElectricMeterItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: ElectricMeterFormValue]
}>()

const form = reactive<ElectricMeterFormValue>(createDefaultMeterForm())
const errors = reactive<Record<string, string>>({})
const modelOptions = ref<ElectricMeterModelOption[]>([])
const modelOptionsLoading = ref(false)
const gatewayOptions = ref<ElectricMeterGatewayOption[]>([])
const gatewayOptionsLoading = ref(false)
const spaceTree = ref<SpaceTreeItem[]>([])
const spaceTreeLoading = ref(false)
const spaceTreeError = ref('')
const spaceTreeVisible = ref(false)
const expandedSpaceIds = ref<number[]>([])
const spacePickerRef = ref<HTMLElement | null>(null)

const resetErrors = () => {
  Object.keys(errors).forEach((key) => {
    delete errors[key]
  })
}

const resetForm = () => {
  const source = createDefaultMeterForm()
  Object.assign(form, source)
  resetErrors()
}

const fillForm = (meter: ElectricMeterItem | null) => {
  resetForm()
  if (!meter) {
    return
  }

  form.id = meter.id
  form.meterName = meter.meterName
  form.deviceNo = meter.deviceNo || ''
  form.meterAddress = meter.meterAddress
  form.modelId = String(meter.modelId)
  form.ct = meter.ct || ''
  form.spaceId = meter.spaceId
  form.gatewayId = meter.gatewayId ? String(meter.gatewayId) : ''
  form.portNo = meter.portNo || ''
  form.imei = meter.imei || ''
  form.payType = String(meter.payType) as '1' | '0'
  form.isCalculate = String(meter.isCalculate) as 'true' | 'false'
  form.spaceName = meter.spaceName || ''
  form.spacePath = meter.spacePath || ''
}

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      spaceTreeVisible.value = false
      return
    }
    fillForm(props.meter)
    void ensureModelOptions()
    void ensureGatewayOptions()
    void ensureSpaceTree()
  }
)

watch(
  () => props.meter,
  (meter) => {
    if (!props.modelValue) {
      return
    }
    fillForm(meter)
  }
)

const selectedModel = computed(() =>
  modelOptions.value.find((item) => String(item.id) === String(form.modelId))
)
const isEditMode = computed(() => props.mode === 'edit')
const normalizeCommunicateModelKey = (value: string | undefined) => {
  const source = (value || '').trim().toLowerCase()
  if (!source) {
    return ''
  }
  if (source === 'nb' || source.includes('nb') || source.includes('4g')) {
    return 'nb'
  }
  if (source === 'tcp' || source.includes('tcp') || source.includes('485')) {
    return 'tcp'
  }
  return source
}
const communicateModelKey = computed(() =>
  normalizeCommunicateModelKey(selectedModel.value?.communicateModel)
)
const showCtField = computed(() => Boolean(selectedModel.value?.isCt))
const showGatewayFields = computed(() => communicateModelKey.value === 'tcp')
const showImeiField = computed(() => communicateModelKey.value === 'nb')
const canTogglePrepay = computed(() => Boolean(selectedModel.value?.isPrepay))
const dialogTitle = computed(() => (props.mode === 'create' ? '添加电表' : '编辑电表'))
const communicateModelText = computed(() => {
  if (communicateModelKey.value === 'tcp') {
    return 'TCP'
  }
  if (communicateModelKey.value === 'nb') {
    return 'NB'
  }
  return selectedModel.value?.communicateModel || '—'
})

const findSpaceNodeById = (
  nodes: SpaceTreeItem[],
  spaceId: string | undefined
): SpaceTreeItem | null => {
  if (!spaceId) {
    return null
  }

  for (const node of nodes) {
    if (String(node.id) === String(spaceId)) {
      return node
    }

    if (node.children?.length) {
      const matched = findSpaceNodeById(node.children, spaceId)
      if (matched) {
        return matched
      }
    }
  }

  return null
}

const selectedSpaceNode = computed(() => findSpaceNodeById(spaceTree.value, form.spaceId))
const selectedSpaceDisplayText = computed(() => {
  if (selectedSpaceNode.value?.pathLabel) {
    return selectedSpaceNode.value.pathLabel
  }

  if (form.spacePath) {
    return form.spacePath
  }

  if (form.spaceName) {
    return form.spaceName
  }

  return ''
})

const close = () => {
  emit('update:modelValue', false)
}

const ensureGatewayOptions = async () => {
  if (gatewayOptionsLoading.value || gatewayOptions.value.length > 0) {
    return
  }

  gatewayOptionsLoading.value = true

  try {
    const list = await fetchGatewayList()
    gatewayOptions.value = list.map((item) => ({
      id: item.id,
      gatewayName: item.gatewayName || `网关-${item.id}`,
      deviceNo: item.deviceNo || item.sn || '',
      sn: item.sn || item.deviceNo || ''
    }))
  } catch (_error) {
    gatewayOptions.value = [...fallbackGatewayOptions]
  } finally {
    gatewayOptionsLoading.value = false
  }
}

const ensureModelOptions = async () => {
  if (modelOptionsLoading.value || modelOptions.value.length > 0) {
    return
  }

  modelOptionsLoading.value = true

  try {
    const list = await fetchDeviceModelList({
      typeKey: 'electricMeter'
    })

    modelOptions.value = list.map((item) => {
      const fallback =
        fallbackModelOptions.find(
          (option) =>
            option.id === item.id || (item.modelName && option.modelName === item.modelName)
        ) || null

      return {
        id: item.id,
        modelName: item.modelName || fallback?.modelName || `型号-${item.id}`,
        communicateModel: item.communicateModel || fallback?.communicateModel || '',
        isNb:
          item.isNb ??
          (normalizeCommunicateModelKey(item.communicateModel) === 'nb'
            ? true
            : normalizeCommunicateModelKey(item.communicateModel) === 'tcp'
            ? false
            : fallback?.isNb ?? false),
        isCt: item.isCt ?? fallback?.isCt ?? false,
        isPrepay: item.isPrepay ?? fallback?.isPrepay ?? true
      }
    })
  } catch (_error) {
    modelOptions.value = [...fallbackModelOptions]
  } finally {
    modelOptionsLoading.value = false
  }
}

const ensureSpaceTree = async () => {
  if (spaceTreeLoading.value || spaceTree.value.length > 0) {
    return
  }

  spaceTreeLoading.value = true
  spaceTreeError.value = ''

  try {
    const list = await fetchSpaceTree()
    spaceTree.value = list
    expandedSpaceIds.value = []
  } catch (error) {
    spaceTreeError.value = error instanceof Error ? error.message : '设备地址树加载失败'
  } finally {
    spaceTreeLoading.value = false
  }
}

const handleDocumentClick = (event: MouseEvent) => {
  if (!spaceTreeVisible.value || !spacePickerRef.value) {
    return
  }

  const target = event.target
  if (target instanceof Node && !spacePickerRef.value.contains(target)) {
    spaceTreeVisible.value = false
  }
}

const toggleSpaceTreeVisible = async () => {
  if (spaceTreeVisible.value) {
    spaceTreeVisible.value = false
    return
  }

  await ensureSpaceTree()
  spaceTreeVisible.value = true
}

const toggleSpaceNode = (id: number) => {
  if (expandedSpaceIds.value.includes(id)) {
    expandedSpaceIds.value = expandedSpaceIds.value.filter((item) => item !== id)
    return
  }

  expandedSpaceIds.value = [...expandedSpaceIds.value, id]
}

const handleSelectSpace = (node: SpaceTreeItem) => {
  form.spaceId = String(node.id)
  form.spaceName = node.name
  form.spacePath = node.pathLabel
  delete errors.spaceId
  spaceTreeVisible.value = false
}

watch(
  () => form.spaceId,
  (value) => {
    if (!value) {
      form.spaceName = ''
      form.spacePath = ''
      return
    }

    const node = findSpaceNodeById(spaceTree.value, value)
    if (node) {
      form.spaceName = node.name
      form.spacePath = node.pathLabel
    }
  }
)

onMounted(() => {
  if (typeof document !== 'undefined') {
    document.addEventListener('mousedown', handleDocumentClick)
  }
})

onBeforeUnmount(() => {
  if (typeof document !== 'undefined') {
    document.removeEventListener('mousedown', handleDocumentClick)
  }
})

const validate = () => {
  resetErrors()

  if (!form.meterName.trim()) {
    errors.meterName = '请输入电表名称'
  }
  if (!form.spaceId) {
    errors.spaceId = '请选择设备位置'
  }

  if (!isEditMode.value) {
    if (!form.deviceNo.trim()) {
      errors.deviceNo = '请输入电表编号'
    }
    if (!form.modelId) {
      errors.modelId = '请选择电表型号'
    }
    if (!form.meterAddress.trim()) {
      errors.meterAddress = '请输入电表地址码'
    } else if (!/^(?:[1-9]|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/.test(form.meterAddress.trim())) {
      errors.meterAddress = '电表地址码需为 1-255 的整数'
    }
    if (showCtField.value && form.ct.trim()) {
      if (!/^[1-9][0-9]*$/.test(form.ct.trim()) || Number(form.ct.trim()) >= 65535) {
        errors.ct = 'CT 变比需为小于 65535 的正整数'
      }
    }
    if (showGatewayFields.value) {
      if (!form.gatewayId) {
        errors.gatewayId = '请选择接入网关'
      }
      if (!form.portNo.trim()) {
        errors.portNo = '请输入串口号'
      }
    }
    if (showImeiField.value && !form.imei.trim()) {
      errors.imei = '请输入 IMEI'
    }
  }
  return Object.keys(errors).length === 0
}

const handleSubmit = () => {
  if (!validate()) {
    return
  }
  emit('submit', {
    ...form,
    modelName: selectedModel.value?.modelName || '',
    communicateModel: selectedModel.value?.communicateModel || '',
    isNb: selectedModel.value?.isNb,
    isCt: selectedModel.value?.isCt,
    isPrepay: selectedModel.value?.isPrepay,
    spaceName: selectedSpaceNode.value?.name || form.spaceName,
    spacePath: selectedSpaceNode.value?.pathLabel || form.spacePath
  })
}

const handleModelChange = () => {
  resetErrors()
  form.ct = ''
  form.gatewayId = ''
  form.portNo = ''
  form.imei = ''
  if (!selectedModel.value?.isPrepay) {
    form.payType = '0'
  }
}

const getRequiredLabelClass = (required: boolean) => ({
  'field-label-required': required
})
</script>

<template>
  <Transition name="meter-modal-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel meter-edit-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid single-column">
            <label class="field">
              <span :class="['field-label', getRequiredLabelClass(true)]">电表名称</span>
              <input
                v-model="form.meterName"
                class="form-control"
                type="text"
                placeholder="请输入电表名称"
              />
              <span v-if="errors.meterName" class="field-error">{{ errors.meterName }}</span>
            </label>

            <label class="field">
              <span :class="['field-label', getRequiredLabelClass(!isEditMode)]"
                >电表编号（必须和电表实际编号一致）</span
              >
              <input
                v-model="form.deviceNo"
                class="form-control"
                type="text"
                placeholder="请输入电表编号"
                :disabled="isEditMode"
              />
              <span v-if="errors.deviceNo" class="field-error">{{ errors.deviceNo }}</span>
            </label>

            <div ref="spacePickerRef" class="field field-tree-select">
              <span :class="['field-label', getRequiredLabelClass(true)]">设备位置</span>
              <button
                type="button"
                class="tree-select-trigger"
                :class="{ 'is-placeholder': !form.spaceId, 'is-open': spaceTreeVisible }"
                @click="toggleSpaceTreeVisible"
              >
                <span class="tree-select-text">
                  {{ selectedSpaceDisplayText || '请选择设备位置' }}
                </span>
                <span class="tree-select-caret">▾</span>
              </button>
              <div v-if="spaceTreeVisible" class="tree-select-panel">
                <UiLoadingState
                  v-if="spaceTreeLoading"
                  class="tree-select-loading"
                  :size="16"
                  :thickness="2"
                  :min-height="48"
                />
                <UiErrorState
                  v-else-if="spaceTreeError"
                  class="tree-select-error"
                  :text="spaceTreeError"
                  :min-height="48"
                />
                <UiEmptyState
                  v-else-if="spaceTree.length === 0"
                  class="tree-select-empty"
                  text="暂无空间数据"
                  :min-height="48"
                />
                <ul v-else class="tree-select-list">
                  <SpaceTreeSelectNode
                    v-for="node in spaceTree"
                    :key="node.id"
                    :node="node"
                    :expanded-ids="expandedSpaceIds"
                    :selected-id="selectedSpaceNode?.id"
                    @toggle="toggleSpaceNode"
                    @select="handleSelectSpace"
                  />
                </ul>
              </div>
              <span v-if="errors.spaceId" class="field-error">{{ errors.spaceId }}</span>
            </div>

            <label class="field">
              <span class="field-label-row">
                <span :class="['field-label', getRequiredLabelClass(true)]">电表型号</span>
                <UiLoadingState
                  v-if="modelOptionsLoading"
                  inline
                  :size="14"
                  :thickness="2"
                />
              </span>
              <select
                v-model="form.modelId"
                class="form-select"
                :class="{ 'is-placeholder': !form.modelId }"
                :disabled="modelOptionsLoading || isEditMode"
                @change="handleModelChange"
              >
                <option value="">请选择电表型号</option>
                <option v-for="item in modelOptions" :key="item.id" :value="String(item.id)">
                  {{ item.modelName }}
                </option>
              </select>
              <span v-if="errors.modelId" class="field-error">{{ errors.modelId }}</span>
            </label>

            <label class="field">
              <span class="field-label">通讯模式</span>
              <input
                class="form-control"
                type="text"
                :value="communicateModelText"
                disabled
                readonly
              />
            </label>

            <label v-if="showCtField" class="field">
              <span class="field-label">CT变比</span>
              <input
                v-model="form.ct"
                class="form-control"
                type="text"
                placeholder="请输入 CT 变比（选填）"
                :disabled="isEditMode"
              />
              <span v-if="errors.ct" class="field-error">{{ errors.ct }}</span>
            </label>

            <label v-if="showGatewayFields" class="field">
              <span class="field-label-row">
                <span :class="['field-label', getRequiredLabelClass(true)]">接入网关</span>
                <UiLoadingState
                  v-if="gatewayOptionsLoading"
                  inline
                  :size="14"
                  :thickness="2"
                />
              </span>
              <select
                v-model="form.gatewayId"
                class="form-select"
                :class="{ 'is-placeholder': !form.gatewayId }"
                :disabled="gatewayOptionsLoading || isEditMode"
              >
                <option value="">请选择接入网关</option>
                <option v-for="item in gatewayOptions" :key="item.id" :value="String(item.id)">
                  {{ item.gatewayName }} - {{ item.deviceNo || item.sn || '—' }}
                </option>
              </select>
              <span v-if="errors.gatewayId" class="field-error">{{ errors.gatewayId }}</span>
            </label>

            <label v-if="showGatewayFields" class="field">
              <span :class="['field-label', getRequiredLabelClass(!isEditMode)]">串口号</span>
              <input
                v-model="form.portNo"
                class="form-control"
                type="text"
                placeholder="请输入串口号"
                :disabled="isEditMode"
              />
              <span v-if="errors.portNo" class="field-error">{{ errors.portNo }}</span>
            </label>

            <label v-if="showImeiField" class="field">
              <span :class="['field-label', getRequiredLabelClass(!isEditMode)]">IMEI</span>
              <input
                v-model="form.imei"
                class="form-control"
                type="text"
                placeholder="请输入 IMEI"
                :disabled="isEditMode"
              />
              <span v-if="errors.imei" class="field-error">{{ errors.imei }}</span>
            </label>

            <label class="field">
              <span :class="['field-label', getRequiredLabelClass(!isEditMode)]">电表地址码</span>
              <input
                v-model="form.meterAddress"
                class="form-control"
                type="text"
                maxlength="3"
                placeholder="请输入电表地址码"
                :disabled="isEditMode"
              />
              <span v-if="errors.meterAddress" class="field-error">{{ errors.meterAddress }}</span>
            </label>

            <div class="field">
              <span :class="['field-label', getRequiredLabelClass(true)]">是否预付费</span>
              <div class="radio-group">
                <label class="radio-item">
                  <input
                    v-model="form.payType"
                    type="radio"
                    value="1"
                    :disabled="!canTogglePrepay"
                  />
                  <span>是</span>
                </label>
                <label class="radio-item">
                  <input
                    v-model="form.payType"
                    type="radio"
                    value="0"
                    :disabled="!canTogglePrepay"
                  />
                  <span>否</span>
                </label>
              </div>
              <span v-if="!canTogglePrepay" class="field-tip">当前型号不支持修改预付费类型</span>
            </div>

            <div class="field">
              <span :class="['field-label', getRequiredLabelClass(true)]">是否计量</span>
              <div class="radio-group">
                <label class="radio-item">
                  <input v-model="form.isCalculate" type="radio" value="true" />
                  <span>是</span>
                </label>
                <label class="radio-item">
                  <input v-model="form.isCalculate" type="radio" value="false" />
                  <span>否</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="close">取消</button>
          <button class="btn btn-primary" type="button" @click="handleSubmit">确定</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
}

.meter-edit-modal {
  display: grid;
  width: min(640px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  grid-template-rows: auto minmax(0, 1fr) auto;
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
  overflow: auto;
}

.form-grid {
  display: grid;
  gap: 16px;
}

.single-column {
  grid-template-columns: minmax(0, 1fr);
}

.field {
  position: relative;
  display: grid;
  gap: 8px;
}

.field-label {
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.field-label-row {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.field-label-required::before {
  margin-right: 4px;
  color: #dc2626;
  content: '*';
}

.form-control,
.form-select {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-control::placeholder {
  color: var(--es-color-text-placeholder);
}

.form-select.is-placeholder {
  color: var(--es-color-text-placeholder);
}

.form-control:focus,
.form-select:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.field-tree-select {
  position: relative;
}

.tree-select-trigger {
  display: flex;
  width: 100%;
  min-height: 36px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  text-align: left;
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.tree-select-trigger:hover {
  border-color: #bfdbfe;
}

.tree-select-trigger.is-open,
.tree-select-trigger:focus-visible {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.tree-select-trigger.is-placeholder .tree-select-text {
  color: var(--es-color-text-placeholder);
}

.tree-select-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-select-caret {
  font-size: 12px;
  color: var(--es-color-text-secondary);
  transition: transform 0.18s ease;
  flex-shrink: 0;
}

.tree-select-trigger.is-open .tree-select-caret {
  transform: rotate(180deg);
}

.tree-select-panel {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: 0;
  z-index: 8;
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: 0 14px 32px rgb(15 23 42 / 14%);
}

.tree-select-loading,
.tree-select-error,
.tree-select-empty {
  padding: 12px;
  font-size: var(--es-font-size-sm);
}

.tree-select-loading,
.tree-select-empty {
  color: var(--es-color-text-secondary);
}

.tree-select-error {
  color: var(--es-color-error-text);
}

.tree-select-list {
  max-height: 260px;
  padding: 8px;
  margin: 0;
  overflow: auto;
  list-style: none;
}

.radio-group {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 36px;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.radio-item input:disabled {
  cursor: not-allowed;
}

.radio-item input:disabled + span {
  color: #9aa6b6;
}

.field-tip {
  font-size: var(--es-font-size-xs);
  color: var(--es-color-text-placeholder);
}

.field-error {
  font-size: var(--es-font-size-xs);
  color: var(--es-color-error-text);
}

.btn {
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #f8fbff;
  border-color: var(--es-color-border-strong);
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
</style>
