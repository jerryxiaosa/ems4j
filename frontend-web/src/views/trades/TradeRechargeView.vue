<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchAccountDetail } from '@/api/adapters/account'
import type { OrganizationOption } from '@/api/adapters/organization'
import { searchAccountOptions } from '@/api/adapters/account'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import {
  createEnergyTopUpOrder,
  fetchDefaultServiceRate,
  updateDefaultServiceRate
} from '@/api/adapters/trade'
import OrganizationPicker from '@/components/common/OrganizationPicker.vue'
import ElectricRechargeConfirmModal from '@/components/trades/ElectricRechargeConfirmModal.vue'
import ElectricRechargeMeterModal from '@/components/trades/ElectricRechargeMeterModal.vue'
import ElectricServiceRateModal from '@/components/trades/ElectricServiceRateModal.vue'
import {
  defaultServiceRate,
  type RechargeEnterpriseDetail,
  type RechargeMeterItem
} from '@/components/trades/electric-recharge.mock'
import type { AccountItem, AccountMeter } from '@/types/account'
import type { CurrentUser } from '@/types/auth'

const authStore = useAuthStore()
const router = useRouter()
const tradeRechargePermissionKeys = {
  changeMeter: 'trade_management_electric_recharge_change_meter',
  serviceFeeSetting: 'trade_management_electric_recharge_service_fee_setting',
  confirmRecharge: 'trade_management_electric_recharge_confirm_recharge'
} as const
const enterpriseKeyword = ref('')
const selectedEnterprise = ref<RechargeEnterpriseDetail | null>(null)
const selectedMeter = ref<RechargeMeterItem | null>(null)
const confirmModalVisible = ref(false)
const meterModalVisible = ref(false)
const serviceRateModalVisible = ref(false)
const serviceRateSetting = ref(String(defaultServiceRate))
const selectingEnterprise = ref(false)
const submitting = ref(false)
let selectEnterpriseRequestId = 0
let orderEnumLoadingPromise: Promise<void> | null = null

const paymentChannelOptions = ref<EnumOption[]>([])
const balanceTypeOptions = ref<EnumOption[]>([])
const meterTypeOptions = ref<EnumOption[]>([])

const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})
const noticeFading = ref(false)
let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null

const NOTICE_VISIBLE_MS = 5000
const NOTICE_FADE_MS = 300

const form = reactive({
  payAmount: '',
  serviceRate: serviceRateSetting.value,
  paymentMethod: '现金支付'
})

const getErrorMessage = (error: unknown) => {
  return (error as Error)?.message || '请求失败'
}

const normalizeText = (value: unknown) => {
  if (value === undefined || value === null) {
    return ''
  }
  const text = String(value).trim()
  if (text === '' || text === '--' || text === '-' || text === '—') {
    return ''
  }
  return text
}

const toNumber = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }
  return null
}

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
      notice.text = ''
      noticeFading.value = false
      noticeClearTimer = null
    }, NOTICE_FADE_MS)
    noticeFadeTimer = null
  }, NOTICE_VISIBLE_MS)
}

const parseDecimal = (value: string) => {
  const source = value.trim()
  if (!source) {
    return 0
  }
  const parsed = Number(source)
  return Number.isFinite(parsed) ? parsed : 0
}

const parseOptionalDecimal = (value: string) => {
  const source = value.trim()
  if (!source) {
    return null
  }
  const parsed = Number(source)
  return Number.isFinite(parsed) ? parsed : null
}

const formatMoney = (value: number) => value.toFixed(2)
const wait = (ms: number) =>
  new Promise<void>((resolve) => {
    window.setTimeout(resolve, ms)
  })

const toPercentDisplay = (ratio: number) => {
  return String(Number((ratio * 100).toFixed(6)))
}

const getFractionLength = (value: string) => {
  const parts = value.trim().split('.')
  return parts[1]?.length ?? 0
}

const toRatioPayload = (percentText: string) => {
  const source = percentText.trim()
  if (!source) {
    return 0
  }

  const [integerPart, fractionPart = ''] = source.split('.')
  const digits = `${integerPart}${fractionPart}`.replace(/^0+/, '') || '0'
  const decimalPlaces = fractionPart.length + 2
  const paddedDigits = digits.padStart(decimalPlaces + 1, '0')
  const pointIndex = paddedDigits.length - decimalPlaces
  const rawRatioText = `${paddedDigits.slice(0, pointIndex)}.${paddedDigits.slice(pointIndex)}`
  const [ratioIntegerPart, ratioFractionPart = ''] = rawRatioText.split('.')
  const ratioText = `${ratioIntegerPart}.${ratioFractionPart.slice(0, 8)}`
    .replace(/\.?0+$/, '')
    .replace(/^$/, '0')

  return Number(ratioText)
}

const isNeedMeter = computed(() => {
  const accountType = selectedEnterprise.value?.electricAccountType
  if (typeof accountType === 'number') {
    return accountType === 0
  }

  const label = selectedEnterprise.value?.electricAccountTypeName || ''
  return /按需/.test(label) && !/合并|包月|月租/.test(label)
})

const serviceFeeAmount = computed(() => {
  const amount = parseDecimal(form.payAmount)
  const rate = parseDecimal(form.serviceRate)
  return amount > 0 && rate >= 0 ? formatMoney((amount * rate) / 100) : ''
})

const saleAmount = computed(() => {
  const amount = parseDecimal(form.payAmount)
  const fee = Number(serviceFeeAmount.value || 0)
  return amount > 0 ? formatMoney(Math.max(amount - fee, 0)) : ''
})

const rechargeAmountText = computed(() => {
  const amount = parseDecimal(form.payAmount)
  return amount > 0 ? formatMoney(amount) : '--'
})

const summaryRows = computed(() => {
  const enterprise = selectedEnterprise.value
  if (!enterprise) {
    return [
      { label: '账户名称', value: '--' },
      { label: '负责人', value: '--' },
      { label: '手机号', value: '--' },
      { label: '电费余额（元）', value: '--' },
      { label: '计费类型', value: '--' },
      { label: '电表数量', value: '--' }
    ]
  }

  return [
    { label: '账户名称', value: enterprise.name || '--' },
    { label: '负责人', value: enterprise.contactName || '--' },
    { label: '手机号', value: enterprise.contactPhone || '--' },
    { label: '电费余额（元）', value: enterprise.electricBalanceAmountText || '--' },
    { label: '计费类型', value: enterprise.electricAccountTypeName || '--' },
    { label: '电表数量', value: String(enterprise.meterCount ?? '--') }
  ]
})

const currentMeterRows = computed(() => selectedEnterprise.value?.meters || [])

const normalizeSpaceParentNames = (value: string[] | string | undefined): string[] => {
  if (Array.isArray(value)) {
    return value.map((item) => item.trim()).filter(Boolean)
  }
  if (typeof value === 'string') {
    return value
      .split(/[>/,]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
  return []
}

const normalizeRechargeMeter = (meter: AccountMeter, fallbackIndex: number): RechargeMeterItem => {
  const parentNames = normalizeSpaceParentNames(meter.spaceParentNames)

  return {
    id: meter.id ?? fallbackIndex + 1,
    roomNo: meter.spaceName || '--',
    regionName: parentNames.join(' > ') || '--',
    meterName: meter.meterName || '--',
    deviceNo: meter.deviceNo || '--',
    spaceId: meter.spaceId,
    meterType: meter.meterType,
    meterTypeName: meter.meterTypeName,
    balanceAmountText: meter.meterBalanceAmountText || '--',
    lastMonthAmountText: '--'
  }
}

const normalizeRechargeEnterpriseDetail = (
  account: AccountItem,
  option: OrganizationOption
): RechargeEnterpriseDetail => {
  const meters = (account.meterList || []).map(normalizeRechargeMeter)

  return {
    id: account.id ?? option.id,
    accountId: account.id ?? option.id,
    ownerId: account.ownerId,
    ownerType: account.ownerType,
    ownerTypeName: account.ownerTypeName,
    name: account.ownerName || option.name,
    contactName: account.contactName || option.managerName || '--',
    contactPhone: account.contactPhone || option.managerPhone || '--',
    electricAccountType: account.electricAccountType,
    electricBalanceAmountText: account.electricBalanceAmountText || '--',
    electricAccountTypeName: account.electricAccountTypeName || '--',
    meterCount: meters.length || Number(account.openedMeterCount || 0),
    meters
  }
}

const createFallbackEnterprise = (option: OrganizationOption): RechargeEnterpriseDetail => {
  return {
    id: option.id,
    accountId: option.id,
    name: option.name,
    contactName: option.managerName || '--',
    contactPhone: option.managerPhone || '--',
    ownerId: undefined,
    ownerType: undefined,
    ownerTypeName: '',
    electricAccountType: undefined,
    electricBalanceAmountText: '--',
    electricAccountTypeName: '--',
    meterCount: 0,
    meters: []
  }
}

const selectEnterprise = async (option: OrganizationOption) => {
  const requestId = ++selectEnterpriseRequestId
  selectingEnterprise.value = true

  enterpriseKeyword.value = option.name
  selectedEnterprise.value = null
  selectedMeter.value = null
  form.payAmount = ''
  form.serviceRate = serviceRateSetting.value

  try {
    const account = await fetchAccountDetail(option.id)
    if (requestId !== selectEnterpriseRequestId) {
      return
    }
    const enterprise = normalizeRechargeEnterpriseDetail(account, option)
    selectedEnterprise.value = enterprise
    selectedMeter.value = enterprise.meters[0] || null
  } catch (error) {
    if (requestId !== selectEnterpriseRequestId) {
      return
    }
    selectedEnterprise.value = createFallbackEnterprise(option)
    selectedMeter.value = null
    setNotice('error', error instanceof Error ? error.message : '机构信息加载失败')
  } finally {
    if (requestId === selectEnterpriseRequestId) {
      selectingEnterprise.value = false
    }
  }
}

watch(enterpriseKeyword, (value) => {
  if (selectedEnterprise.value && value.trim() !== selectedEnterprise.value.name) {
    selectedEnterprise.value = null
    selectedMeter.value = null
  }
})

const openServiceRateSetting = () => {
  serviceRateModalVisible.value = true
}

const loadDefaultServiceRate = async () => {
  try {
    const serviceRate = await fetchDefaultServiceRate()
    if (serviceRate !== null) {
      const value = toPercentDisplay(serviceRate)
      serviceRateSetting.value = value
      form.serviceRate = value
      return
    }
    serviceRateSetting.value = String(defaultServiceRate)
    form.serviceRate = serviceRateSetting.value
  } catch (error) {
    serviceRateSetting.value = String(defaultServiceRate)
    form.serviceRate = serviceRateSetting.value
    setNotice('error', error instanceof Error ? error.message : '服务费比例加载失败')
  }
}

const loadOrderEnumOptions = async () => {
  if (orderEnumLoadingPromise) {
    return orderEnumLoadingPromise
  }

  orderEnumLoadingPromise = (async () => {
    const [paymentChannels, balanceTypes, meterTypes] = await Promise.all([
      fetchEnumOptionsByKey('paymentChannel'),
      fetchEnumOptionsByKey('balanceType'),
      fetchEnumOptionsByKey('meterType')
    ])
    paymentChannelOptions.value = paymentChannels
    balanceTypeOptions.value = balanceTypes
    meterTypeOptions.value = meterTypes
  })()

  try {
    await orderEnumLoadingPromise
  } finally {
    orderEnumLoadingPromise = null
  }
}

const findOptionValueByLabel = (options: EnumOption[], label: unknown): string | null => {
  const target = normalizeText(label)
  if (!target) {
    return null
  }
  return options.find((option) => option.label.trim() === target)?.value || null
}

const findOptionValueByLabelMatcher = (options: EnumOption[], matcher: RegExp): string | null => {
  return options.find((option) => matcher.test(option.label))?.value || null
}

const resolveOwnerTypeValue = (enterprise: RechargeEnterpriseDetail): number | null => {
  const ownerType = toNumber(enterprise.ownerType)
  if (ownerType !== null) {
    return ownerType
  }

  const ownerTypeName = enterprise.ownerTypeName || ''
  if (/企业/.test(ownerTypeName)) {
    return 0
  }

  return null
}

const resolveElectricAccountTypeValue = (enterprise: RechargeEnterpriseDetail): number | null => {
  const accountType = toNumber(enterprise.electricAccountType)
  if (accountType !== null) {
    return accountType
  }

  const typeName = enterprise.electricAccountTypeName || ''
  if (/按需/.test(typeName) && !/合并/.test(typeName)) {
    return 0
  }
  if (/包月|月租/.test(typeName)) {
    return 1
  }
  if (/合并/.test(typeName)) {
    return 2
  }

  return null
}

const resolvePaymentChannelValue = (): string => {
  return (
    findOptionValueByLabel(paymentChannelOptions.value, form.paymentMethod) ||
    findOptionValueByLabelMatcher(paymentChannelOptions.value, /现金/) ||
    findOptionValueByLabelMatcher(paymentChannelOptions.value, /offline|cash/i) ||
    paymentChannelOptions.value[0]?.value ||
    ''
  )
}

const resolveBalanceTypeValue = (needMeter: boolean): number | null => {
  const isMeterBalanceOption = (option: EnumOption) => {
    return /电表|表计|meter/i.test(option.label) || /ELECTRIC_METER/i.test(option.value)
  }
  const isAccountBalanceOption = (option: EnumOption) => {
    return /账户|账号|account/i.test(option.label) || /ELECTRIC_ACCOUNT/i.test(option.value)
  }

  const matched = balanceTypeOptions.value.find(
    needMeter ? isMeterBalanceOption : isAccountBalanceOption
  )
  if (!matched) {
    return null
  }
  return toNumber(matched.value)
}

const resolveMeterTypeValue = (meter: RechargeMeterItem): number | undefined => {
  const meterType = toNumber(meter.meterType)
  if (
    meterType !== null &&
    meterTypeOptions.value.some((option) => {
      return toNumber(option.value) === meterType
    })
  ) {
    return meterType
  }

  const meterTypeName = normalizeText(meter.meterTypeName)
  if (meterTypeName) {
    const matchedOption = meterTypeOptions.value.find(
      (option) => option.label.trim() === meterTypeName
    )
    const matchedValue = toNumber(matchedOption?.value)
    if (matchedValue !== null) {
      return matchedValue
    }
  }

  return undefined
}

const ensureOrderEnumOptions = async () => {
  if (
    paymentChannelOptions.value.length &&
    balanceTypeOptions.value.length &&
    meterTypeOptions.value.length
  ) {
    return
  }

  try {
    await loadOrderEnumOptions()
  } catch (error) {
    throw new Error(`充值相关枚举加载失败：${getErrorMessage(error)}`)
  }
}

const ensureCurrentUser = async (): Promise<CurrentUser> => {
  if (!authStore.user) {
    await authStore.loadCurrentUser()
  }

  if (!authStore.user) {
    throw new Error('未获取到登录用户信息，请重新登录')
  }
  return authStore.user
}

const buildEnergyTopUpPayload = async (orderAmount: number) => {
  const enterprise = selectedEnterprise.value
  if (!enterprise) {
    throw new Error('请先选择账户')
  }

  await ensureOrderEnumOptions()
  const user = await ensureCurrentUser()

  const userId = toNumber(user.id)
  if (userId === null) {
    throw new Error('当前用户ID无效，无法创建充值订单')
  }

  const userPhone = normalizeText(user.userPhone)
  if (!userPhone) {
    throw new Error('当前登录用户手机号为空，无法创建充值订单')
  }

  const userRealName = normalizeText(user.realName) || normalizeText(user.userName)
  if (!userRealName) {
    throw new Error('当前登录用户姓名为空，无法创建充值订单')
  }

  const accountId = toNumber(enterprise.accountId ?? enterprise.id)
  if (accountId === null) {
    throw new Error('账户ID缺失，无法创建充值订单')
  }

  const ownerId = toNumber(enterprise.ownerId)
  if (ownerId === null) {
    throw new Error('账户归属ID缺失，无法创建充值订单')
  }

  const ownerType = resolveOwnerTypeValue(enterprise)
  if (ownerType === null) {
    throw new Error('账户归属类型无法匹配系统枚举，请联系后端确认 ownerType')
  }

  const electricAccountType = resolveElectricAccountTypeValue(enterprise)
  if (electricAccountType === null) {
    throw new Error('电费计费类型无法匹配系统枚举，请联系后端确认 electricAccountType')
  }

  const paymentChannel = resolvePaymentChannelValue()
  if (!paymentChannel) {
    throw new Error('支付渠道无法匹配系统枚举，请联系后端确认 paymentChannel')
  }

  const needMeter = isNeedMeter.value
  const balanceType = resolveBalanceTypeValue(needMeter)
  if (balanceType === null) {
    throw new Error('账户余额类型无法匹配系统枚举，请联系后端确认 balanceType')
  }

  const ownerName = normalizeText(enterprise.name)
  if (!ownerName) {
    throw new Error('账户归属名称为空，无法创建充值订单')
  }

  const energyTopUp: {
    accountId: number
    balanceType: number
    ownerType: number
    ownerId: number
    ownerName: string
    electricAccountType: number
    serviceRate: number
    meterId?: number
    meterType?: number
    meterName?: string
    deviceNo?: string
    spaceId?: number
  } = {
    accountId,
    balanceType,
    ownerType,
    ownerId,
    ownerName,
    electricAccountType,
    serviceRate: toRatioPayload(form.serviceRate)
  }

  if (needMeter) {
    if (!selectedMeter.value) {
      throw new Error('请选择电表')
    }

    const meterId = toNumber(selectedMeter.value.id)
    if (meterId === null) {
      throw new Error('所选电表ID无效，无法创建充值订单')
    }

    energyTopUp.meterId = meterId
    const meterType = resolveMeterTypeValue(selectedMeter.value)
    if (meterType === undefined) {
      throw new Error('所选电表类型缺失或无效，无法创建充值订单')
    }
    energyTopUp.meterType = meterType

    const meterName = normalizeText(selectedMeter.value.meterName)
    if (meterName) {
      energyTopUp.meterName = meterName
    }

    const deviceNo = normalizeText(selectedMeter.value.deviceNo)
    if (deviceNo) {
      energyTopUp.deviceNo = deviceNo
    }

    const spaceId = toNumber(selectedMeter.value.spaceId)
    if (spaceId !== null) {
      energyTopUp.spaceId = spaceId
    }
  }

  return {
    userId,
    userPhone,
    userRealName,
    thirdPartyUserId: String(userId),
    orderAmount,
    paymentChannel,
    energyTopUp
  }
}

const handleServiceRateSubmit = async (serviceRate: string) => {
  try {
    await updateDefaultServiceRate(toRatioPayload(serviceRate))
    serviceRateSetting.value = serviceRate
    form.serviceRate = serviceRate
    serviceRateModalVisible.value = false
    setNotice('success', '电费服务费设置成功')
  } catch (error) {
    setNotice('error', error instanceof Error ? error.message : '电费服务费设置失败')
  }
}

const openMeterModal = () => {
  meterModalVisible.value = true
}

const handleMeterConfirm = (meter: RechargeMeterItem) => {
  selectedMeter.value = meter
}

const validateSubmit = (): number | null => {
  if (!selectedEnterprise.value) {
    setNotice('error', '请先选择账户')
    return null
  }
  if (isNeedMeter.value && !selectedMeter.value) {
    setNotice('error', '请选择电表')
    return null
  }
  const orderAmount = parseDecimal(form.payAmount)
  if (orderAmount <= 0) {
    setNotice('error', '请输入充值金额')
    return null
  }
  const serviceRate = parseOptionalDecimal(form.serviceRate)
  if (serviceRate === null) {
    setNotice('error', '请输入服务费比例')
    return null
  }
  if (serviceRate < 0 || serviceRate >= 100) {
    setNotice('error', '服务费比例需为大于等于 0 且小于 100 的数字')
    return null
  }
  if (getFractionLength(form.serviceRate) > 6) {
    setNotice('error', '服务费比例最多保留 6 位小数')
    return null
  }
  return orderAmount
}

const submitRechargeOrder = async (orderAmount: number) => {
  if (submitting.value) {
    return
  }
  try {
    submitting.value = true
    const payload = await buildEnergyTopUpPayload(orderAmount)
    const result = await createEnergyTopUpOrder(payload)
    confirmModalVisible.value = false
    setNotice(
      'success',
      result.orderSn ? `充值订单创建成功，订单号：${result.orderSn}` : '充值订单创建成功'
    )
    await wait(2000)
    await router.push('/trade/order-flows')
  } catch (error) {
    setNotice('error', getErrorMessage(error))
  } finally {
    submitting.value = false
  }
}

const handleSubmit = async () => {
  const orderAmount = validateSubmit()
  if (orderAmount === null) {
    return
  }

  if (isNeedMeter.value) {
    confirmModalVisible.value = true
    return
  }

  await submitRechargeOrder(orderAmount)
}

const handleConfirmSubmit = async () => {
  const orderAmount = validateSubmit()
  if (orderAmount === null) {
    return
  }
  await submitRechargeOrder(orderAmount)
}

onBeforeUnmount(() => {
  clearNoticeTimers()
})

onMounted(() => {
  void loadDefaultServiceRate()
  void loadOrderEnumOptions()
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
      <div class="workspace-search">
        <div class="search-row search-row-recharge">
          <label class="search-item search-item-enterprise">
            <span class="search-label-inline search-label-required">选择账户</span>
            <OrganizationPicker
              v-model="enterpriseKeyword"
              placeholder="请选择账户"
              :disabled="selectingEnterprise"
              :search-fn="searchAccountOptions"
              @select="selectEnterprise"
            />
          </label>

          <div class="search-actions">
            <button
              v-menu-permission="tradeRechargePermissionKeys.serviceFeeSetting"
              class="btn btn-primary"
              type="button"
              @click="openServiceRateSetting"
            >
              电费服务费设置
            </button>
          </div>
        </div>
      </div>

      <div class="workspace-body">
        <section class="info-card">
          <div class="summary-grid summary-grid-three">
            <div
              v-for="row in summaryRows"
              :key="row.label"
              class="summary-item summary-item-compact"
            >
              <span class="summary-label es-detail-label">{{ row.label }}</span>
              <span class="summary-value es-detail-value-box">{{ row.value }}</span>
            </div>
          </div>
        </section>

        <section v-if="isNeedMeter" class="info-card">
          <div class="section-head">
            <button
              v-menu-permission="tradeRechargePermissionKeys.changeMeter"
              class="btn btn-primary"
              type="button"
              @click="openMeterModal"
            >
              更换电表
            </button>
          </div>
          <div class="summary-grid summary-grid-meter">
            <div class="summary-item summary-item-compact">
              <span class="summary-label es-detail-label">电表名称</span>
              <span class="summary-value es-detail-value-box">{{
                selectedMeter?.meterName || '--'
              }}</span>
            </div>
            <div class="summary-item summary-item-compact">
              <span class="summary-label es-detail-label">电表表号</span>
              <span class="summary-value es-detail-value-box">{{
                selectedMeter?.deviceNo || '--'
              }}</span>
            </div>
            <div class="summary-item summary-item-compact">
              <span class="summary-label es-detail-label">房间编号</span>
              <span class="summary-value es-detail-value-box">{{
                selectedMeter?.roomNo || '--'
              }}</span>
            </div>
            <div class="summary-item summary-item-compact">
              <span class="summary-label es-detail-label">电表余额（元）</span>
              <span class="summary-value es-detail-value-box">{{
                selectedMeter?.balanceAmountText || '--'
              }}</span>
            </div>
          </div>
        </section>

        <section class="form-card">
          <div class="form-grid">
            <label class="form-field">
              <span class="field-label search-label-required">充值金额（元）</span>
              <input
                v-model="form.payAmount"
                class="form-control"
                type="text"
                placeholder="请输入充值金额"
              />
            </label>
            <label class="form-field">
              <span class="field-label">售电金额（元）</span>
              <input :value="saleAmount" class="form-control" type="text" readonly />
            </label>
            <label class="form-field">
              <span class="field-label search-label-required">服务费比例（%）</span>
              <input
                v-model="form.serviceRate"
                class="form-control"
                type="text"
                placeholder="请输入服务费比例"
              />
            </label>
            <label class="form-field">
              <span class="field-label">服务费金额（元）</span>
              <input :value="serviceFeeAmount" class="form-control" type="text" readonly />
            </label>
            <label class="form-field">
              <span class="field-label search-label-required">付款方式</span>
              <select v-model="form.paymentMethod" class="form-control" disabled>
                <option value="现金支付">现金支付</option>
              </select>
            </label>
          </div>

          <div class="form-actions">
            <button
              v-menu-permission="tradeRechargePermissionKeys.confirmRecharge"
              class="btn btn-primary"
              type="button"
              :disabled="submitting"
              @click="handleSubmit"
              >确认充值</button
            >
          </div>
        </section>
      </div>
    </section>

    <ElectricRechargeMeterModal
      v-model="meterModalVisible"
      :meters="currentMeterRows"
      :selected-meter-id="selectedMeter?.id || null"
      @confirm="handleMeterConfirm"
    />
    <ElectricRechargeConfirmModal
      v-model="confirmModalVisible"
      :meter-name="selectedMeter?.meterName || '--'"
      :meter-device-no="selectedMeter?.deviceNo || '--'"
      :recharge-amount-text="rechargeAmountText"
      :sale-amount-text="saleAmount || '--'"
      :service-fee-amount-text="serviceFeeAmount || '--'"
      :submitting="submitting"
      @confirm="handleConfirmSubmit"
    />
    <ElectricServiceRateModal
      v-model="serviceRateModalVisible"
      :service-rate="serviceRateSetting"
      @submit="handleServiceRateSubmit"
    />
  </div>
</template>

<style scoped>
.page {
  position: relative;
}

.page-notice {
  position: fixed;
  top: 12px;
  left: 50%;
  z-index: 44;
  max-width: calc(100vw - 32px);
  min-width: 240px;
  padding: 10px 16px;
  color: var(--es-color-text-primary);
  text-align: center;
  background: #fff;
  border: 1px solid transparent;
  border-radius: 5px;
  transform: translateX(-50%);
  box-shadow: 0 10px 24px rgb(15 23 42 / 12%);
  transition: opacity 0.3s ease, transform 0.3s ease;
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

.page-notice-info {
  color: var(--es-color-info-text);
  background: var(--es-color-info-bg);
  border-color: var(--es-color-info-border);
}

.page-notice.is-fading {
  opacity: 0;
  transform: translate(-50%, -6px);
}

.workspace-card {
  min-height: calc(100vh - 128px);
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: 0 6px 18px rgb(15 23 42 / 4%);
}

.workspace-search {
  padding: 16px 20px;
  border-bottom: 1px solid var(--es-color-border);
}

.search-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.search-row-recharge .search-item-enterprise {
  flex: 1;
  max-width: 520px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-label-inline,
.field-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.search-label-required::before,
.field-label.search-label-required::before {
  margin-right: 4px;
  color: var(--es-color-danger);
  content: '*';
}

.search-input,
.form-control {
  width: 100%;
  height: 32px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.workspace-body {
  padding: 16px 20px 20px;
}

.info-card,
.form-card {
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.info-card {
  padding: 20px;
}

.summary-grid {
  display: grid;
  gap: 12px 32px;
}

.summary-grid-three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary-grid-meter {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.summary-item-compact {
  display: grid;
  grid-template-columns: 118px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.section-head {
  margin-bottom: 14px;
}

.summary-value {
  width: 100%;
}

.form-card {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: minmax(0, 560px);
  gap: 16px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.btn {
  height: 32px;
  padding: 0 16px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

@media (width <= 1200px) {
  .summary-grid-three,
  .summary-grid-meter {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (width <= 768px) {
  .search-row,
  .search-item {
    flex-direction: column;
    align-items: stretch;
  }

  .summary-grid-three,
  .summary-grid-meter {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
