<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { fetchDeviceModelList } from '@/api/adapters/device'
import { fetchSpaceTree } from '@/api/adapters/space'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiErrorState from '@/components/common/UiErrorState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import SpaceTreeSelectNode from '@/components/devices/SpaceTreeSelectNode.vue'
import {
  createDefaultGatewayForm,
  gatewayModelOptions as fallbackGatewayModelOptions,
  gatewaySpaceTree as fallbackGatewaySpaceTree,
  type GatewayModelOption,
  type GatewayFormValue,
  type GatewayItem
} from '@/components/devices/gateway.mock'
import type { SpaceTreeItem } from '@/types/space'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  gateway: GatewayItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: GatewayFormValue]
}>()

const form = reactive<GatewayFormValue>(createDefaultGatewayForm())
const errors = reactive<Record<string, string>>({})
const modelOptions = ref<GatewayModelOption[]>([])
const modelOptionsLoading = ref(false)
const spaceTreeVisible = ref(false)
const spaceTreeLoading = ref(false)
const spaceTreeError = ref('')
const expandedSpaceIds = ref<number[]>([])
const spacePickerRef = ref<HTMLElement | null>(null)
const spaceTree = ref<SpaceTreeItem[]>([])

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

const resetForm = () => {
  Object.assign(form, createDefaultGatewayForm())
  resetErrors()
}

const fillForm = (gateway: GatewayItem | null) => {
  resetForm()
  if (!gateway) {
    return
  }

  form.id = gateway.id
  form.gatewayName = gateway.gatewayName
  form.modelId = String(gateway.modelId)
  form.modelName = gateway.modelName
  form.deviceNo = gateway.deviceNo
  form.spaceId = gateway.spaceId
  form.spaceName = gateway.spaceName
  form.spacePath = gateway.spacePath
  form.communicateModel = gateway.communicateModel
  form.sn = gateway.sn
  form.imei = gateway.imei || ''
  form.deviceSecret = ''
  form.configInfo = gateway.configInfo
}

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      spaceTreeVisible.value = false
      return
    }
    fillForm(props.gateway)
    void ensureModelOptions()
    void ensureSpaceTree()
  }
)

watch(
  () => props.gateway,
  (gateway) => {
    if (props.modelValue) {
      fillForm(gateway)
    }
  }
)

const isEditMode = computed(() => props.mode === 'edit')
const dialogTitle = computed(() => (isEditMode.value ? '编辑智能网关' : '添加智能网关'))
const selectedModel = computed(() =>
  modelOptions.value.find((item) => String(item.id) === String(form.modelId))
)
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
const showImeiField = computed(
  () => normalizeCommunicateModelKey(selectedModel.value?.communicateModel) === 'nb'
)

const handleModelChange = () => {
  const model = selectedModel.value
  form.modelName = model?.modelName || ''
  form.communicateModel = model?.communicateModel || ''
  if (!showImeiField.value) {
    form.imei = ''
  }
}

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
  return ''
})

const ensureModelOptions = async () => {
  if (modelOptionsLoading.value || modelOptions.value.length > 0) {
    return
  }

  modelOptionsLoading.value = true

  try {
    const list = await fetchDeviceModelList({
      typeKey: 'gateway'
    })

    modelOptions.value = list.map((item) => {
      const fallback =
        fallbackGatewayModelOptions.find(
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
            : fallback?.isNb ?? false)
      }
    })
  } catch (_error) {
    modelOptions.value = [...fallbackGatewayModelOptions]
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
    spaceTree.value = [...fallbackGatewaySpaceTree]
    spaceTreeError.value = error instanceof Error ? error.message : '设备位置树加载失败'
  } finally {
    spaceTreeLoading.value = false
  }
}

const toggleExpand = (id: number) => {
  if (expandedSpaceIds.value.includes(id)) {
    expandedSpaceIds.value = expandedSpaceIds.value.filter((item) => item !== id)
  } else {
    expandedSpaceIds.value = [...expandedSpaceIds.value, id]
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

const handleSelectSpace = (node: SpaceTreeItem) => {
  form.spaceId = String(node.id)
  form.spaceName = node.name
  form.spacePath = node.pathLabel
  errors.spaceId = ''
  spaceTreeVisible.value = false
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

const getRequiredLabelClass = (required: boolean) => ({ 'field-label-required': required })

const validate = () => {
  resetErrors()

  if (!form.modelId) {
    errors.modelId = '请选择网关型号'
  }
  if (!form.gatewayName.trim()) {
    errors.gatewayName = '请输入网关名称'
  }
  if (!form.deviceNo.trim()) {
    errors.deviceNo = '请输入网关编号'
  }
  if (!form.spaceId) {
    errors.spaceId = '请选择设备位置'
  }
  if (!form.sn.trim()) {
    errors.sn = '请输入网关SN'
  }
  if (showImeiField.value && !form.imei.trim()) {
    errors.imei = '请输入IMEI'
  }
  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const handleSubmit = () => {
  if (!validate()) {
    return
  }

  emit('submit', {
    ...form,
    gatewayName: form.gatewayName.trim(),
    sn: form.sn.trim(),
    imei: form.imei.trim(),
    deviceSecret: form.deviceSecret.trim(),
    configInfo: form.configInfo.trim(),
    modelName: selectedModel.value?.modelName || form.modelName,
    communicateModel: selectedModel.value?.communicateModel || form.communicateModel,
    spacePath: selectedSpaceNode.value?.pathLabel || form.spacePath,
    spaceName: selectedSpaceNode.value?.name || form.spaceName
  })
}
</script>

<template>
  <Transition name="open-modal-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel gateway-edit-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field">
              <span :class="['field-label', getRequiredLabelClass(true)]">网关名称</span>
              <input
                v-model="form.gatewayName"
                class="form-control"
                type="text"
                placeholder="请输入网关名称"
              />
              <span v-if="errors.gatewayName" class="field-error">{{ errors.gatewayName }}</span>
            </label>

            <label v-if="!isEditMode" class="field">
              <span :class="['field-label', getRequiredLabelClass(true)]">网关编号</span>
              <input
                v-model="form.deviceNo"
                class="form-control"
                type="text"
                placeholder="请输入网关编号"
              />
              <span v-if="errors.deviceNo" class="field-error">{{ errors.deviceNo }}</span>
            </label>

            <div v-else class="field">
              <span class="field-label">网关编号</span>
              <div class="readonly-control es-readonly-box">{{ form.deviceNo || '—' }}</div>
            </div>

            <label class="field" ref="spacePickerRef">
              <span :class="['field-label', getRequiredLabelClass(true)]">设备位置</span>
              <button
                class="form-control tree-trigger"
                :class="{ 'is-placeholder': !form.spaceId, 'is-open': spaceTreeVisible }"
                type="button"
                @click.stop="toggleSpaceTreeVisible"
              >
                <span>{{ selectedSpaceDisplayText || '请选择设备位置' }}</span>
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
                  v-else-if="spaceTreeError && spaceTree.length === 0"
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
                    :selected-id="Number(form.spaceId || 0) || null"
                    @toggle="toggleExpand"
                    @select="handleSelectSpace"
                  />
                </ul>
              </div>
              <span v-if="errors.spaceId" class="field-error">{{ errors.spaceId }}</span>
            </label>

            <label class="field">
              <span class="field-label-row">
                <span :class="['field-label', getRequiredLabelClass(true)]">网关型号</span>
                <UiLoadingState
                  v-if="modelOptionsLoading"
                  inline
                  :size="14"
                  :thickness="2"
                />
              </span>
              <select
                v-model="form.modelId"
                class="form-control"
                :class="{ 'is-placeholder': !form.modelId, 'is-disabled': isEditMode }"
                :disabled="modelOptionsLoading || isEditMode"
                @change="handleModelChange"
              >
                <option value="">请选择网关型号</option>
                <option v-for="item in modelOptions" :key="item.id" :value="String(item.id)">
                  {{ item.modelName }}
                </option>
              </select>
              <span v-if="errors.modelId" class="field-error">{{ errors.modelId }}</span>
            </label>

            <label class="field">
              <span :class="['field-label', getRequiredLabelClass(true)]">网关SN</span>
              <input
                v-model="form.sn"
                class="form-control"
                type="text"
                placeholder="请输入网关SN"
              />
              <span v-if="errors.sn" class="field-error">{{ errors.sn }}</span>
            </label>

            <div class="field">
              <span class="field-label">网关模式</span>
              <div class="readonly-control es-readonly-box">{{ form.communicateModel || '—' }}</div>
            </div>

            <label v-if="showImeiField" class="field">
              <span :class="['field-label', getRequiredLabelClass(true)]">IMEI</span>
              <input
                v-model="form.imei"
                class="form-control"
                type="text"
                placeholder="请输入IMEI"
              />
              <span v-if="errors.imei" class="field-error">{{ errors.imei }}</span>
            </label>

            <label class="field">
              <span class="field-label">设备密钥</span>
              <input
                v-model="form.deviceSecret"
                class="form-control"
                type="text"
                :placeholder="isEditMode ? '留空表示不修改设备密钥' : '请输入设备密钥（选填）'"
              />
              <span v-if="errors.deviceSecret" class="field-error">{{ errors.deviceSecret }}</span>
            </label>
          </div>

          <label class="field field-full">
            <span class="field-label">配置信息</span>
            <textarea
              v-model="form.configInfo"
              class="form-control textarea-control"
              rows="6"
              placeholder="请输入配置信息（选填）"
            ></textarea>
            <span v-if="errors.configInfo" class="field-error">{{ errors.configInfo }}</span>
          </label>
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
  z-index: 42;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
}

.gateway-edit-modal {
  display: grid;
  width: min(760px, calc(100vw - 32px));
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

.icon-btn,
.btn {
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.btn {
  border-color: transparent;
}

.btn-secondary {
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 20px;
}

.field {
  position: relative;
  display: grid;
  gap: 8px;
}

.field-full {
  margin-top: 16px;
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
  color: var(--es-color-error-text);
  content: '*';
}

.form-control,
.readonly-control {
  width: 100%;
  min-height: 36px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-control {
  outline: none;
}

.form-control::placeholder,
.form-control.is-placeholder,
.tree-trigger.is-placeholder {
  color: var(--es-color-text-placeholder);
}

.form-control:focus,
.tree-trigger.is-open {
  border-color: var(--es-color-primary);
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.readonly-control {
  display: flex;
  align-items: center;
}

.tree-trigger {
  display: flex;
  text-align: left;
  cursor: pointer;
  background: #fff;
  align-items: center;
  justify-content: space-between;
}

.tree-select-panel {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  left: 0;
  z-index: 3;
  max-height: 240px;
  padding: 8px;
  overflow: auto;
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
}

.tree-select-list {
  padding: 0;
  margin: 0;
  list-style: none;
}

.tree-select-loading,
.tree-select-error,
.tree-select-empty {
  padding: 12px 8px;
  font-size: var(--es-font-size-sm);
}

.tree-select-loading,
.tree-select-empty {
  color: var(--es-color-text-placeholder);
}

.tree-select-error {
  color: var(--es-color-error-text);
}

.textarea-control {
  min-height: 124px;
  padding: 10px 12px;
  resize: vertical;
}

.field-error {
  font-size: var(--es-font-size-xs);
  color: var(--es-color-error-text);
}

.open-modal-fade-enter-active,
.open-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.open-modal-fade-enter-from,
.open-modal-fade-leave-to {
  opacity: 0;
}

.open-modal-fade-enter-from .gateway-edit-modal,
.open-modal-fade-leave-to .gateway-edit-modal {
  transform: translateY(10px) scale(0.98);
}

@media (width <= 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
