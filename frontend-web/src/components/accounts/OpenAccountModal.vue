<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import {
  appendAccountMeters,
  fetchOwnerAccountStatus,
  fetchOwnerCandidateMeters,
  openAccount
} from '@/api/adapters/account'
import type { OrganizationOption } from '@/api/adapters/organization'
import { fetchElectricPricePlanOptions, fetchWarnPlanOptions } from '@/api/adapters/plan'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import type { OpenAccountPayload, OwnerCandidateMeter } from '@/types/account'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import OrganizationPicker from '@/components/common/OrganizationPicker.vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success', payload: { accountId: number }): void
}>()

const MONTHLY_TYPE_LABEL_MATCHER = /(包月|月租)/
const ENTERPRISE_OWNER_TYPE = 0

const getErrorMessage = (error: unknown) => {
  return (error as Error)?.message || '请求失败'
}

const isNonNegativeNumberText = (value: string) => {
  const source = value.trim()
  if (!source) {
    return false
  }
  const parsed = Number(source)
  return Number.isFinite(parsed) && parsed >= 0
}

const initLoading = ref(false)
const meterLoading = ref(false)
const submitLoading = ref(false)

const enterpriseKeyword = ref('')

const electricAccountTypeOptions = ref<EnumOption[]>([])
const electricPricePlanOptions = ref<EnumOption[]>([])
const warnPlanOptions = ref<EnumOption[]>([])
const meterTypeOptions = ref<EnumOption[]>([])

const form = reactive({
  enterpriseId: '',
  enterpriseName: '',
  contactName: '',
  contactPhone: '',
  electricAccountType: '',
  monthlyPayAmount: '',
  electricPricePlanId: '',
  warnPlanId: ''
})

const meterQuery = reactive({
  spaceNameLike: ''
})

const hasExistingAccount = ref(false)
const existingAccountId = ref<number | null>(null)
const meterRows = ref<OwnerCandidateMeter[]>([])
const selectedMeterIds = ref<number[]>([])

const notice = reactive({
  type: 'info' as 'info' | 'error',
  text: ''
})
const noticeFading = ref(false)

const formErrors = reactive({
  enterpriseName: '',
  electricAccountType: '',
  monthlyPayAmount: '',
  electricPricePlanId: '',
  warnPlanId: '',
  meterSelection: ''
})

let meterLoadRequestId = 0
let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null

const NOTICE_VISIBLE_MS = 5000
const NOTICE_FADE_MS = 300

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

const clearNotice = () => {
  clearNoticeTimers()
  noticeFading.value = false
  notice.type = 'info'
  notice.text = ''
}

const setNotice = (type: 'info' | 'error', text: string) => {
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

const resetFormErrors = () => {
  formErrors.enterpriseName = ''
  formErrors.electricAccountType = ''
  formErrors.monthlyPayAmount = ''
  formErrors.electricPricePlanId = ''
  formErrors.warnPlanId = ''
  formErrors.meterSelection = ''
}

const monthlyElectricAccountTypeValue = computed(() => {
  const matched = electricAccountTypeOptions.value.find((option) =>
    MONTHLY_TYPE_LABEL_MATCHER.test(option.label)
  )
  return matched?.value || '1'
})

const defaultNonMonthlyElectricAccountTypeValue = computed(() => {
  const matched = electricAccountTypeOptions.value.find(
    (option) => !MONTHLY_TYPE_LABEL_MATCHER.test(option.label)
  )
  return matched?.value || ''
})

const isMonthlyElectricAccountType = (value: string) => {
  return Boolean(value) && value === monthlyElectricAccountTypeValue.value
}

const showMonthlyPayAmount = computed(() => {
  return isMonthlyElectricAccountType(form.electricAccountType)
})

const showElectricPricePlan = computed(() => {
  return !showMonthlyPayAmount.value
})

const showWarnPlan = computed(() => {
  return !showMonthlyPayAmount.value
})

const accountConfigLocked = computed(() => {
  return hasExistingAccount.value
})

const isSelectableMeter = (row: OwnerCandidateMeter) => {
  return row.isOnline !== false
}

const selectableMeterIds = computed(() => {
  return meterRows.value.filter(isSelectableMeter).map((row) => row.id)
})

const selectedMeterCount = computed(() => {
  return selectedMeterIds.value.length
})

const unselectedMeterCount = computed(() => {
  const count = selectableMeterIds.value.length - selectedMeterIds.value.length
  return count > 0 ? count : 0
})

const allMetersSelected = computed(() => {
  return (
    selectableMeterIds.value.length > 0 &&
    selectedMeterIds.value.length === selectableMeterIds.value.length
  )
})

const partiallyMetersSelected = computed(() => {
  return (
    selectedMeterIds.value.length > 0 &&
    selectedMeterIds.value.length < selectableMeterIds.value.length
  )
})

const canSubmit = computed(() => {
  if (!form.enterpriseId) return false
  if (selectedMeterIds.value.length < 1) return false
  if (hasExistingAccount.value) {
    return existingAccountId.value !== null
  }
  if (!form.electricAccountType) return false
  if (showElectricPricePlan.value && !form.electricPricePlanId) return false
  if (showWarnPlan.value && !form.warnPlanId) return false
  if (showMonthlyPayAmount.value && !isNonNegativeNumberText(form.monthlyPayAmount)) return false
  return true
})

const meterEmptyText = computed(() => {
  if (!form.enterpriseId) {
    return '请先选择机构'
  }
  return hasExistingAccount.value ? '暂无可追加电表' : '暂无候选电表'
})

const resetEnterpriseDependentState = () => {
  hasExistingAccount.value = false
  existingAccountId.value = null
  form.contactName = ''
  form.contactPhone = ''
  form.electricAccountType = ''
  form.monthlyPayAmount = ''
  form.electricPricePlanId = ''
  form.warnPlanId = ''
  meterQuery.spaceNameLike = ''
  meterRows.value = []
  selectedMeterIds.value = []
  formErrors.meterSelection = ''
}

const resetAllState = () => {
  clearNotice()
  resetFormErrors()

  initLoading.value = false
  meterLoading.value = false
  submitLoading.value = false

  enterpriseKeyword.value = ''

  form.enterpriseId = ''
  form.enterpriseName = ''
  form.contactName = ''
  form.contactPhone = ''
  form.electricAccountType = ''
  form.monthlyPayAmount = ''
  form.electricPricePlanId = ''
  form.warnPlanId = ''

  hasExistingAccount.value = false
  existingAccountId.value = null

  meterQuery.spaceNameLike = ''
  meterRows.value = []
  selectedMeterIds.value = []
}

const loadBaseOptions = async () => {
  initLoading.value = true
  clearNotice()

  try {
    const [electricTypes, pricePlans, warnPlans, meterTypes] = await Promise.all([
      fetchEnumOptionsByKey('electricAccountType'),
      fetchElectricPricePlanOptions(),
      fetchWarnPlanOptions(),
      fetchEnumOptionsByKey('meterType')
    ])

    electricAccountTypeOptions.value = electricTypes
    electricPricePlanOptions.value = pricePlans
    warnPlanOptions.value = warnPlans
    meterTypeOptions.value = meterTypes
  } catch (error) {
    setNotice('error', `基础数据加载失败：${getErrorMessage(error)}`)
  } finally {
    initLoading.value = false
  }
}

const initOnOpen = async () => {
  resetAllState()
  await loadBaseOptions()
}

const closeModal = () => {
  if (submitLoading.value) {
    return
  }
  emit('update:modelValue', false)
}

const handleMaskClick = () => {
  // 按确认规则：点击遮罩不关闭弹框
}

const clearEnterpriseSelectionByManualInput = () => {
  if (!form.enterpriseId) {
    return
  }

  form.enterpriseId = ''
  form.enterpriseName = ''
  resetEnterpriseDependentState()
}

watch(enterpriseKeyword, (nextValue) => {
  formErrors.enterpriseName = ''

  if (nextValue.trim() !== form.enterpriseName) {
    clearEnterpriseSelectionByManualInput()
  }
})

const loadMeters = async () => {
  if (!form.enterpriseId) {
    meterRows.value = []
    selectedMeterIds.value = []
    return
  }

  const requestId = ++meterLoadRequestId
  meterLoading.value = true

  try {
    const ownerId = Number(form.enterpriseId)
    const remoteRows = await fetchOwnerCandidateMeters({
      ownerType: ENTERPRISE_OWNER_TYPE,
      ownerId,
      spaceNameLike: meterQuery.spaceNameLike.trim() || undefined
    })

    if (requestId !== meterLoadRequestId) {
      return
    }

    meterRows.value = remoteRows

    const validIds = new Set(remoteRows.filter(isSelectableMeter).map((row) => row.id))
    selectedMeterIds.value = selectedMeterIds.value.filter((id) => validIds.has(id))
  } catch (error) {
    if (requestId === meterLoadRequestId) {
      setNotice('error', `开户电表加载失败：${getErrorMessage(error)}`)
      meterRows.value = []
      selectedMeterIds.value = []
    }
  } finally {
    if (requestId === meterLoadRequestId) {
      meterLoading.value = false
    }
  }
}

const selectEnterprise = async (option: OrganizationOption) => {
  const isChanged = form.enterpriseId !== String(option.id)

  enterpriseKeyword.value = option.name
  form.enterpriseId = String(option.id)
  form.enterpriseName = option.name
  formErrors.enterpriseName = ''
  clearNotice()

  if (isChanged) {
    resetEnterpriseDependentState()
  }

  form.contactName = option.managerName
  form.contactPhone = option.managerPhone

  try {
    const status = await fetchOwnerAccountStatus({
      ownerType: ENTERPRISE_OWNER_TYPE,
      ownerId: option.id
    })
    const hasAccountFlag = status.hasAccount === true
    hasExistingAccount.value = hasAccountFlag
    existingAccountId.value =
      hasAccountFlag && typeof status.accountId === 'number' ? status.accountId : null

    if (hasAccountFlag) {
      form.electricAccountType =
        status.electricAccountType !== undefined ? String(status.electricAccountType) : ''
      form.warnPlanId = status.warnPlanId !== undefined ? String(status.warnPlanId) : ''

      if (isMonthlyElectricAccountType(form.electricAccountType)) {
        form.monthlyPayAmount = (status.monthlyPayAmountText || '').trim()
        form.electricPricePlanId = ''
      } else {
        form.electricPricePlanId =
          status.electricPricePlanId !== undefined ? String(status.electricPricePlanId) : ''
        form.monthlyPayAmount = ''
      }
      if (existingAccountId.value === null) {
        setNotice('error', '该主体已开户，但账户ID缺失，无法进行追加电表。')
      } else {
        setNotice('info', '该主体已开户，本次仅可追加电表，计费配置不可修改。')
      }
    }
  } catch (error) {
    hasExistingAccount.value = false
    existingAccountId.value = null
    setNotice('error', `账户状态获取失败：${getErrorMessage(error)}`)
  }

  await loadMeters()
}

const handleElectricAccountTypeChange = () => {
  if (accountConfigLocked.value) {
    return
  }
  formErrors.electricAccountType = ''

  if (showMonthlyPayAmount.value) {
    form.electricPricePlanId = ''
    formErrors.electricPricePlanId = ''
    return
  }

  if (!showMonthlyPayAmount.value) {
    form.monthlyPayAmount = ''
    formErrors.monthlyPayAmount = ''
  }
}

const ensureElectricAccountTypeForPlanSelection = () => {
  if (accountConfigLocked.value) {
    return
  }
  if (form.electricAccountType) {
    return
  }
  const fallback = defaultNonMonthlyElectricAccountTypeValue.value
  if (!fallback) {
    return
  }
  form.electricAccountType = fallback
  formErrors.electricAccountType = ''
}

const handleElectricPricePlanChange = () => {
  ensureElectricAccountTypeForPlanSelection()
  formErrors.electricPricePlanId = ''
}

const handleWarnPlanChange = () => {
  ensureElectricAccountTypeForPlanSelection()
  formErrors.warnPlanId = ''
}

const handleMeterQuerySearch = async () => {
  clearNotice()
  if (!form.enterpriseId) {
    setNotice('error', '请先选择机构')
    return
  }

  await loadMeters()
}

const handleMeterQueryReset = async () => {
  meterQuery.spaceNameLike = ''

  if (!form.enterpriseId) {
    meterRows.value = []
    selectedMeterIds.value = []
    return
  }

  await loadMeters()
}

const handleToggleAllRows = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked
  if (checked) {
    selectedMeterIds.value = [...selectableMeterIds.value]
  } else {
    selectedMeterIds.value = []
  }
  formErrors.meterSelection = ''
}

const handleToggleRow = (row: OwnerCandidateMeter, event: Event) => {
  if (!isSelectableMeter(row)) {
    return
  }

  const checked = (event.target as HTMLInputElement).checked
  const current = new Set(selectedMeterIds.value)

  if (checked) {
    current.add(row.id)
  } else {
    current.delete(row.id)
  }

  selectedMeterIds.value = Array.from(current)
  formErrors.meterSelection = ''
}

const validateBeforeSubmit = () => {
  resetFormErrors()
  let valid = true

  if (!form.enterpriseId) {
    formErrors.enterpriseName = '请选择机构名称'
    valid = false
  }

  if (!hasExistingAccount.value) {
    if (!form.electricAccountType) {
      formErrors.electricAccountType = '请选择电价计费类型'
      valid = false
    }

    if (showMonthlyPayAmount.value && !isNonNegativeNumberText(form.monthlyPayAmount)) {
      formErrors.monthlyPayAmount = '请输入有效的包月金额'
      valid = false
    }

    if (showElectricPricePlan.value && !form.electricPricePlanId) {
      formErrors.electricPricePlanId = '请选择电价方案'
      valid = false
    }

    if (showWarnPlan.value && !form.warnPlanId) {
      formErrors.warnPlanId = '请选择费用预警方案'
      valid = false
    }
  } else if (existingAccountId.value === null) {
    setNotice('error', '已开户主体缺少账户ID，无法追加电表')
    valid = false
  }

  if (selectedMeterIds.value.length < 1) {
    formErrors.meterSelection = '请至少选择一个电表'
    valid = false
  }

  return valid
}

const buildOpenAccountPayload = (): OpenAccountPayload => {
  const contactName = form.contactName.trim()
  const contactPhone = form.contactPhone.trim()
  const payload: OpenAccountPayload = {
    ownerId: Number(form.enterpriseId),
    ownerType: ENTERPRISE_OWNER_TYPE,
    ownerName: form.enterpriseName,
    contactName: contactName || undefined,
    contactPhone: contactPhone || undefined,
    electricAccountType: Number(form.electricAccountType),
    electricMeterList: selectedMeterIds.value.map((meterId) => ({ meterId }))
  }

  if (showElectricPricePlan.value && form.electricPricePlanId) {
    payload.electricPricePlanId = Number(form.electricPricePlanId)
  }

  if (showMonthlyPayAmount.value && form.monthlyPayAmount.trim()) {
    payload.monthlyPayAmount = Number(form.monthlyPayAmount.trim())
  }

  if (showWarnPlan.value && form.warnPlanId) {
    payload.warnPlanId = Number(form.warnPlanId)
  }

  return payload
}

const handleSubmit = async () => {
  clearNotice()

  if (!validateBeforeSubmit()) {
    return
  }

  submitLoading.value = true
  try {
    if (hasExistingAccount.value && existingAccountId.value !== null) {
      await appendAccountMeters(existingAccountId.value, {
        inheritHistoryPower: false,
        electricMeterList: selectedMeterIds.value.map((meterId) => ({ meterId }))
      })
      emit('success', { accountId: existingAccountId.value })
    } else {
      const payload = buildOpenAccountPayload()
      const accountId = await openAccount(payload)
      emit('success', { accountId })
    }
    emit('update:modelValue', false)
  } catch (error) {
    setNotice('error', `开户失败：${getErrorMessage(error)}`)
  } finally {
    submitLoading.value = false
  }
}

const getMeterTypeLabel = (row: OwnerCandidateMeter) => {
  const explicit = (row.meterTypeName || '').trim()
  if (explicit) {
    return explicit
  }
  const meterTypeValue = row.meterType
  if (meterTypeValue === undefined || meterTypeValue === null || meterTypeValue === '') {
    return '--'
  }
  const matched = meterTypeOptions.value.find((item) => item.value === String(meterTypeValue))
  return matched?.label || String(meterTypeValue)
}

const getSpaceParentNamesLabel = (value: OwnerCandidateMeter['spaceParentNames']) => {
  if (Array.isArray(value)) {
    return value.filter((item) => item).join(' > ') || '--'
  }
  if (typeof value === 'string' && value.trim()) {
    const parts = value
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item)
    return parts.join(' > ') || '--'
  }
  return '--'
}

const getMeterOnlineStatusLabel = (row: OwnerCandidateMeter) => {
  if (row.isOnline === true) {
    return '在线'
  }
  if (row.isOnline === false) {
    return '离线'
  }
  return '--'
}

const getMeterOnlineStatusClass = (row: OwnerCandidateMeter) => {
  if (row.isOnline === true) {
    return 'meter-online-status meter-online-status-online'
  }
  if (row.isOnline === false) {
    return 'meter-online-status meter-online-status-offline'
  }
  return 'meter-online-status'
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      void initOnOpen()
      return
    }

    resetAllState()
  }
)

onBeforeUnmount(() => {
  clearNoticeTimers()
})
</script>

<template>
  <Transition name="open-modal-fade" appear>
    <div v-if="modelValue" class="open-modal-mask" @click.self.prevent="handleMaskClick">
      <section
        class="open-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="open-account-title"
      >
        <div
          v-if="notice.text"
          class="open-notice"
          :class="[`open-notice-${notice.type}`, { 'open-notice-fade-out': noticeFading }]"
        >
          {{ notice.text }}
        </div>
        <header class="open-modal-head">
          <h3 id="open-account-title">开户</h3>
          <button
            type="button"
            class="btn btn-secondary icon-btn"
            @click="closeModal"
            :disabled="submitLoading"
            aria-label="关闭"
          >
            关闭
          </button>
        </header>
        <div class="open-modal-body">
          <section class="panel">
            <div class="field">
              <label class="field-label">
                <span class="required-star">*</span>
                <span>机构名称</span>
              </label>
              <OrganizationPicker
                v-model="enterpriseKeyword"
                placeholder="请选择机构"
                :auto-blur-on-select="true"
                :disabled="submitLoading"
                @select="selectEnterprise"
              />
              <div v-if="formErrors.enterpriseName" class="field-error">{{
                formErrors.enterpriseName
              }}</div>
            </div>

            <div class="field-grid field-grid-2">
              <div class="field">
                <label class="field-label">负责人</label>
                <input
                  class="form-control is-readonly"
                  v-model="form.contactName"
                  placeholder="选择机构后自动带出"
                  readonly
                />
              </div>
              <div class="field">
                <label class="field-label">手机号</label>
                <input
                  class="form-control is-readonly"
                  v-model="form.contactPhone"
                  placeholder="选择机构后自动带出"
                  readonly
                />
              </div>
            </div>

            <div class="field">
              <label class="field-label">
                <span class="required-star">*</span>
                <span>电价计费类型</span>
              </label>
              <select
                v-model="form.electricAccountType"
                class="form-control form-select"
                :class="{ 'is-placeholder': !form.electricAccountType }"
                :disabled="submitLoading || accountConfigLocked"
                @change="handleElectricAccountTypeChange"
              >
                <option value="">请选择</option>
                <option
                  v-for="option in electricAccountTypeOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
              <div v-if="formErrors.electricAccountType" class="field-error">{{
                formErrors.electricAccountType
              }}</div>
            </div>

            <p class="field-tip">
              *电费按量计费根据用电价计费；包月计费机构支付月租费，所有电表不限电量使用
            </p>
          </section>

          <section class="panel">
            <div class="section-title">选择收费方案</div>

            <div class="field" v-if="showElectricPricePlan">
              <label class="field-label">
                <span class="required-star">*</span>
                <span>电价方案</span>
              </label>
              <select
                v-model="form.electricPricePlanId"
                class="form-control form-select"
                :class="{ 'is-placeholder': !form.electricPricePlanId }"
                :disabled="submitLoading || accountConfigLocked"
                @change="handleElectricPricePlanChange"
              >
                <option value="">请选择</option>
                <option
                  v-for="option in electricPricePlanOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
              <div v-if="formErrors.electricPricePlanId" class="field-error">{{
                formErrors.electricPricePlanId
              }}</div>
            </div>

            <div class="field" v-if="showMonthlyPayAmount">
              <label class="field-label">
                <span class="required-star">*</span>
                <span>包月金额</span>
              </label>
              <input
                v-model="form.monthlyPayAmount"
                class="form-control"
                placeholder="请输入包月金额"
                :disabled="submitLoading || accountConfigLocked"
                :readonly="accountConfigLocked"
                @input="formErrors.monthlyPayAmount = ''"
              />
              <div v-if="formErrors.monthlyPayAmount" class="field-error">{{
                formErrors.monthlyPayAmount
              }}</div>
            </div>

            <div class="field" v-if="showWarnPlan">
              <label class="field-label">
                <span class="required-star">*</span>
                <span>费用预警方案</span>
              </label>
              <select
                v-model="form.warnPlanId"
                class="form-control form-select"
                :class="{ 'is-placeholder': !form.warnPlanId }"
                :disabled="submitLoading || accountConfigLocked"
                @change="handleWarnPlanChange"
              >
                <option value="">请选择</option>
                <option v-for="option in warnPlanOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
              <div v-if="formErrors.warnPlanId" class="field-error">{{
                formErrors.warnPlanId
              }}</div>
            </div>
          </section>

          <section class="panel">
            <div class="section-title section-title-between">
              <span
                >选择开户信息（已选表具数：{{ selectedMeterCount }}，未选表具数：{{
                  unselectedMeterCount
                }}）</span
              >
            </div>

            <div class="meter-query-row">
              <label class="inline-field">
                <span class="inline-field-label">所在位置</span>
                <input
                  v-model="meterQuery.spaceNameLike"
                  class="form-control compact"
                  placeholder="请输入所在位置"
                  :disabled="submitLoading || !form.enterpriseId"
                />
              </label>

              <div class="meter-query-actions">
                <button
                  type="button"
                  class="btn btn-primary"
                  :disabled="submitLoading || meterLoading"
                  @click="handleMeterQuerySearch"
                >
                  查询
                </button>
                <button
                  type="button"
                  class="btn btn-secondary"
                  :disabled="submitLoading || meterLoading"
                  @click="handleMeterQueryReset"
                >
                  重置
                </button>
              </div>
            </div>

            <div v-if="formErrors.meterSelection" class="field-error meter-error">{{
              formErrors.meterSelection
            }}</div>

            <div class="meter-table-wrap">
              <table class="meter-table">
                <thead>
                  <tr>
                    <th class="checkbox-col">
                      <input
                        type="checkbox"
                        :checked="allMetersSelected"
                        :indeterminate.prop="partiallyMetersSelected"
                        :disabled="!selectableMeterIds.length || submitLoading || meterLoading"
                        @change="handleToggleAllRows"
                      />
                    </th>
                    <th>序号</th>
                    <th>所在位置</th>
                    <th>所属区域</th>
                    <th>表具名称</th>
                    <th>表具编号</th>
                    <th>表具类型</th>
                    <th>在线状态</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="meterLoading">
                    <td colspan="8" class="empty-row">
                      <UiLoadingState :size="18" :thickness="2" :min-height="72" />
                    </td>
                  </tr>
                  <tr v-else-if="!meterRows.length">
                    <td colspan="8" class="empty-row">
                      <UiEmptyState :text="meterEmptyText" :min-height="72" />
                    </td>
                  </tr>
                  <template v-else>
                    <tr v-for="(row, index) in meterRows" :key="row.id">
                      <td class="checkbox-col">
                        <input
                          type="checkbox"
                          :checked="selectedMeterIds.includes(row.id)"
                          :disabled="submitLoading || !isSelectableMeter(row)"
                          @change="handleToggleRow(row, $event)"
                        />
                      </td>
                      <td>{{ index + 1 }}</td>
                      <td>{{ row.spaceName || '--' }}</td>
                      <td>{{ getSpaceParentNamesLabel(row.spaceParentNames) }}</td>
                      <td>{{ row.meterName || '--' }}</td>
                      <td>{{ row.deviceNo || '--' }}</td>
                      <td>{{ getMeterTypeLabel(row) }}</td>
                      <td>
                        <span :class="getMeterOnlineStatusClass(row)">
                          {{ getMeterOnlineStatusLabel(row) }}
                        </span>
                      </td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>

            <p class="meter-note">*选择电表后点击“确定开户”，表具将正式用于该账户预付费管理。</p>
          </section>
        </div>

        <footer class="open-modal-footer">
          <button
            type="button"
            class="btn btn-secondary"
            :disabled="submitLoading"
            @click="closeModal"
            >取消</button
          >
          <button
            type="button"
            class="btn btn-primary"
            :disabled="!canSubmit || submitLoading || initLoading"
            @click="handleSubmit"
          >
            确定开户
          </button>
        </footer>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.open-modal-mask {
  position: fixed;
  z-index: 35;
  display: flex;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
  inset: 0;
  align-items: center;
  justify-content: center;
}

.open-modal {
  position: relative;
  display: flex;
  width: min(1120px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  flex-direction: column;
}

.open-modal-fade-enter-active,
.open-modal-fade-leave-active {
  transition: opacity 0.18s ease-out;
}

.open-modal-fade-enter-from,
.open-modal-fade-leave-to {
  opacity: 0;
}

.open-modal-fade-enter-to,
.open-modal-fade-leave-from {
  opacity: 1;
}

.open-modal-fade-enter-active .open-modal,
.open-modal-fade-leave-active .open-modal {
  transition: opacity 0.2s ease-out, transform 0.2s ease-out;
}

.open-modal-fade-enter-from .open-modal,
.open-modal-fade-leave-to .open-modal {
  opacity: 0;
  transform: translateY(8px) scale(0.995);
}

.open-modal-fade-enter-to .open-modal,
.open-modal-fade-leave-from .open-modal {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.open-modal-head {
  display: flex;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
  align-items: center;
  justify-content: space-between;
}

.open-modal-head h3 {
  margin: 0;
  font-size: var(--es-font-size-md);
  color: var(--es-color-text-primary);
}

.icon-btn {
  min-width: 64px;
}

.open-modal-body {
  padding: 16px;
  overflow: auto;
  background: #fff;
  flex: 1;
}

.open-modal-footer {
  display: flex;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid var(--es-color-border);
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.open-notice {
  position: absolute;
  top: 54px;
  left: 50%;
  z-index: 4;
  width: calc(100% - 32px);
  padding: 10px 12px;
  font-size: var(--es-font-size-sm);
  pointer-events: none;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  opacity: 1;
  transform: translateX(-50%);
  box-shadow: 0 8px 24px rgb(15 23 42 / 16%);
  transition: opacity 0.3s ease;
}

.open-notice-fade-out {
  opacity: 0;
}

.open-notice-error {
  color: #b42318;
  background: #fff5f5;
  border-color: #f5c2c7;
}

.open-notice-info {
  color: var(--es-color-info-text);
  background: var(--es-color-info-bg);
  border-color: var(--es-color-info-border);
}

.panel {
  padding: 14px;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.panel + .panel {
  margin-top: 12px;
}

.section-title {
  margin-bottom: 12px;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.section-title-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field-grid {
  display: grid;
  gap: 12px;
}

.field-grid-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.field {
  margin: 0;
}

.panel > .field + .field,
.panel > .field-grid + .field,
.panel > .field + .field-grid {
  margin-top: 12px;
}

.field-label {
  display: inline-flex;
  margin-bottom: 6px;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-primary);
  align-items: center;
  gap: 2px;
}

.required-star {
  line-height: 1;
  color: #ef4444;
}

.field-tip {
  margin: 12px 0 0;
  font-size: var(--es-font-size-sm);
  line-height: 1.45;
  color: var(--es-color-text-secondary);
}

.field-error {
  margin-top: 4px;
  font-size: 12px;
  color: #dc2626;
}

.form-control {
  width: 100%;
  height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-control:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.form-control::placeholder {
  color: var(--es-color-text-placeholder);
}

.form-select {
  padding-right: 32px;
  background-image: linear-gradient(45deg, transparent 50%, #94a3b8 50%),
    linear-gradient(135deg, #94a3b8 50%, transparent 50%);
  background-position: calc(100% - 18px) 15px, calc(100% - 13px) 15px;
  background-repeat: no-repeat;
  background-size: 5px 5px, 5px 5px;
  appearance: none;
}

.form-select.is-placeholder {
  color: var(--es-color-text-placeholder);
}

.form-select option {
  color: var(--es-color-text-primary);
}

.compact {
  width: 180px;
}

.meter-query-row {
  display: flex;
  align-items: end;
  gap: 12px;
  flex-wrap: wrap;
}

.inline-field {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.inline-field-label {
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-primary);
  white-space: nowrap;
}

.meter-query-actions {
  display: flex;
  margin-left: auto;
  gap: 8px;
}

.table-loading-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.meter-error {
  margin-top: 8px;
}

.meter-table-wrap {
  max-height: 280px;
  margin-top: 10px;
  overflow: auto;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.meter-table {
  width: 100%;
  min-width: 900px;
  border-collapse: collapse;
}

.meter-table th,
.meter-table td {
  padding: 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  text-align: left;
  border-bottom: 1px solid var(--es-color-border);
}

.meter-table thead th {
  position: sticky;
  top: 0;
  z-index: 1;
  font-weight: 600;
  background: var(--es-color-table-header-bg);
}

.meter-table tbody tr:hover td {
  background: #f8fbff;
}

.checkbox-col {
  width: 44px;
  text-align: center !important;
}

.checkbox-col input {
  width: 14px;
  height: 14px;
}

.empty-row {
  padding: 28px 0 !important;
  color: var(--es-color-text-placeholder) !important;
  text-align: center !important;
}

.meter-online-status {
  font-weight: 500;
}

.meter-online-status-online {
  color: var(--es-color-success-text);
}

.meter-online-status-offline {
  color: var(--es-color-danger);
}

.meter-note {
  margin: 10px 0 0;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.btn {
  display: inline-flex;
  height: 36px;
  padding: 0 14px;
  font-size: var(--es-font-size-sm);
  cursor: pointer;
  border: 1px solid transparent;
  border-radius: 5px;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
  border-color: var(--es-color-primary);
}

.btn-primary:hover:not(:disabled) {
  background: var(--es-color-primary-hover);
  border-color: var(--es-color-primary-hover);
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

.btn-secondary:hover:not(:disabled) {
  color: var(--es-color-primary);
  background: #f5f9ff;
  border-color: #9fb9ee;
}

.btn:focus-visible,
.picker-option:focus-visible,
.picker-toggle-btn:focus-visible,
.form-control:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}

@media (width <= 900px) {
  .open-modal {
    width: min(1120px, calc(100vw - 16px));
    max-height: calc(100vh - 16px);
  }

  .open-modal-mask {
    padding: 8px;
  }

  .field-grid-2 {
    grid-template-columns: 1fr;
  }

  .meter-query-row {
    align-items: stretch;
  }

  .inline-field {
    width: 100%;
    justify-content: space-between;
  }

  .compact {
    width: 100%;
    flex: 1;
  }

  .meter-query-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }
}

@media (prefers-reduced-motion: reduce) {
  .open-modal-fade-enter-active,
  .open-modal-fade-leave-active,
  .open-modal-fade-enter-active .open-modal,
  .open-modal-fade-leave-active .open-modal {
    transition: none;
  }
}
</style>
