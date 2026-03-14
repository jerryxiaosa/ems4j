<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { cancelAccount, fetchAccountDetail, fetchAccountPage } from '@/api/adapters/account'
import { fetchLatestPowerRecord } from '@/api/adapters/device'
import { fetchEnumOptionsByKey } from '@/api/adapters/system'
import OpenAccountModal from '@/components/accounts/OpenAccountModal.vue'
import CommonPagination from '@/components/common/CommonPagination.vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import { usePermission } from '@/composables/usePermission'
import type { EnumOption } from '@/api/adapters/system'
import type { AccountItem, AccountMeter, AccountPageResult } from '@/types/account'
import type { LatestPowerRecord } from '@/types/device'

const { hasMenuPermission } = usePermission()
const accountPermissionKeys = {
  open: 'account_management_account_info_open',
  detail: 'account_management_account_info_detail',
  cancel: 'account_management_account_info_cancel',
  offlineCancel: 'account_management_account_info_offline_cancel'
} as const

const notice = reactive({
  type: 'info' as 'info' | 'success' | 'error',
  text: ''
})
const noticeFading = ref(false)

let noticeFadeTimer: number | null = null
let noticeClearTimer: number | null = null
let offlineLatestPowerRequestId = 0

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

const getErrorMessage = (error: unknown) => {
  return (error as Error)?.message || '请求失败'
}

const toNumber = (value: string): number | undefined => {
  const source = value.trim()
  if (!source) {
    return undefined
  }

  const parsed = Number(source)
  return Number.isFinite(parsed) ? parsed : undefined
}

const normalizeText = (value: unknown) => {
  if (typeof value === 'string' && value.trim()) {
    return value.trim()
  }
  if (typeof value === 'number' && Number.isFinite(value)) {
    return String(value)
  }
  return '—'
}

const parseAmount = (value: unknown) => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string') {
    const normalized = value
      .replace(/,/g, '')
      .replace(/[^\d.-]/g, '')
      .trim()
    if (!normalized) {
      return 0
    }
    const parsed = Number(normalized)
    return Number.isFinite(parsed) ? parsed : 0
  }
  return 0
}

const formatAmount = (value: number) => {
  return value.toFixed(2)
}

const formatEnergyReferenceText = (value: string) => {
  return value === '—' ? value : `${value} kWh`
}

const wait = (ms: number) => {
  return new Promise<void>((resolve) => {
    window.setTimeout(resolve, ms)
  })
}

interface CancelDialogMeterRow {
  id: number
  spaceNo: string
  spaceParentName: string
  meterName: string
  deviceNo: string
  meterTypeName: string
  meterBalanceText: string
  meterBalanceValue: number
  onlineStatus: '1' | '0'
  onlineStatusText: string
  offlineDurationText: string
}

interface OfflineCancelFormState {
  meterId: number
  meterName: string
  deviceNo: string
  spaceNo: string
  spaceParentName: string
  meterBalanceText: string
  offlineDurationText: string
  recentTotalPowerText: string
  recentPowerHigherText: string
  recentPowerHighText: string
  recentPowerLowText: string
  recentPowerLowerText: string
  recentPowerDeepLowText: string
  powerHigher: string
  powerHigh: string
  powerLow: string
  powerLower: string
  powerDeepLow: string
}

const MONTHLY_ELECTRIC_ACCOUNT_TYPE = 1
const MERGED_ELECTRIC_ACCOUNT_TYPE = 2

const queryForm = reactive({
  ownerName: '',
  electricAccountType: '',
  pageNum: 1,
  pageSize: 10
})

const electricAccountTypeOptions = ref<EnumOption[]>([])
const cleanBalanceTypeOptions = ref<EnumOption[]>([])

const accountPage = reactive<AccountPageResult>({
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
})

const loading = ref(false)

const loadAccounts = async () => {
  loading.value = true
  accountPage.list = []
  try {
    const result = await fetchAccountPage({
      ownerName: queryForm.ownerName.trim() || undefined,
      electricAccountType: toNumber(queryForm.electricAccountType),
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })

    accountPage.list = result.list
    accountPage.total = result.total
    accountPage.pageNum = result.pageNum ?? queryForm.pageNum
    accountPage.pageSize = result.pageSize ?? queryForm.pageSize
    queryForm.pageNum = accountPage.pageNum
    queryForm.pageSize = accountPage.pageSize
  } catch (error) {
    setNotice('error', `账户列表加载失败：${getErrorMessage(error)}`)
  } finally {
    loading.value = false
  }
}

const currentPageSize = computed(() => {
  return accountPage.pageSize || queryForm.pageSize || 10
})

const getSerialNumber = (index: number) => {
  const pageNum = queryForm.pageNum > 0 ? queryForm.pageNum : 1
  return (pageNum - 1) * currentPageSize.value + index + 1
}

const getOwnerTypeLabel = (account: AccountItem) => {
  if (account.ownerTypeName) {
    return account.ownerTypeName
  }
  return account.ownerType ?? '--'
}

const getElectricAccountTypeLabel = (account: AccountItem) => {
  if (account.electricAccountTypeName) {
    return account.electricAccountTypeName
  }
  return account.electricAccountType ?? '--'
}

const getElectricPricePlanLabel = (account: AccountItem) => {
  if (account.electricPricePlanName) {
    return account.electricPricePlanName
  }
  return '--'
}

const getOpenedMeterCount = (account: AccountItem) => {
  if (account.openedMeterCount !== undefined) {
    return account.openedMeterCount
  }
  return account.meterList.length
}

const getTotalOpenableMeterCount = (account: AccountItem) => {
  if (account.totalOpenableMeterCount !== undefined) {
    return account.totalOpenableMeterCount
  }
  return '--'
}

const getElectricBalanceAmountText = (account: AccountItem) => {
  if (
    typeof account.electricBalanceAmountText === 'string' &&
    account.electricBalanceAmountText.length > 0
  ) {
    return account.electricBalanceAmountText
  }
  return '--'
}

const getWarnPlanLabel = (account: AccountItem) => {
  if (account.warnPlanName) {
    return account.warnPlanName
  }
  if (account.warnPlanId !== undefined && account.warnPlanId !== null) {
    return account.warnPlanId
  }
  return '--'
}

const getSpaceParentNamesLabel = (value?: string | string[]) => {
  if (Array.isArray(value)) {
    const parts = value.map((item) => `${item}`.trim()).filter(Boolean)
    return parts.length ? parts.join(' > ') : '--'
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) {
      return '--'
    }

    if (trimmed.includes(',')) {
      const parts = trimmed
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
      return parts.length ? parts.join(' > ') : '--'
    }

    return trimmed
  }

  return '--'
}

const getMeterOnlineStatusLabel = (meter: AccountMeter) => {
  if (meter.isOnline === true) {
    return '在线'
  }
  if (meter.isOnline === false) {
    return '离线'
  }
  return '--'
}

const getMeterOnlineStatusClass = (meter: AccountMeter) => {
  if (meter.isOnline === true) {
    return 'meter-online-status meter-online-status-online'
  }
  if (meter.isOnline === false) {
    return 'meter-online-status meter-online-status-offline'
  }
  return 'meter-online-status'
}

const search = async () => {
  queryForm.pageNum = 1
  await loadAccounts()
}

const resetQuery = async () => {
  queryForm.ownerName = ''
  queryForm.electricAccountType = ''
  queryForm.pageNum = 1
  await loadAccounts()
}

const loadElectricAccountTypeOptions = async () => {
  try {
    electricAccountTypeOptions.value = await fetchEnumOptionsByKey('electricAccountType')
  } catch (error) {
    setNotice('error', `电价计费类型加载失败：${getErrorMessage(error)}`)
  }
}

const loadCleanBalanceTypeOptions = async () => {
  try {
    cleanBalanceTypeOptions.value = await fetchEnumOptionsByKey('cleanBalanceType')
  } catch (error) {
    setNotice('error', `结算类型枚举加载失败：${getErrorMessage(error)}`)
  }
}

const getCleanBalanceTypeLabel = (value?: number) => {
  if (value === undefined || value === null) {
    return '--'
  }
  const target = String(value)
  return cleanBalanceTypeOptions.value.find((item) => item.value === target)?.label || target
}

const getCleanBalanceAmountText = (payload: {
  cleanBalanceAmountText?: string
  amount?: number
}) => {
  if (typeof payload.cleanBalanceAmountText === 'string' && payload.cleanBalanceAmountText.trim()) {
    return payload.cleanBalanceAmountText.trim()
  }
  if (payload.amount !== undefined && payload.amount !== null) {
    return String(payload.amount)
  }
  return '--'
}

const handlePageChange = async (payload: { pageNum: number; pageSize: number }) => {
  if (loading.value) {
    return
  }
  queryForm.pageNum = payload.pageNum
  queryForm.pageSize = payload.pageSize
  await loadAccounts()
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const accountDetail = ref<AccountItem | null>(null)
const showDetailElectricPricePlan = computed(() => {
  if (!accountDetail.value) {
    return false
  }
  return accountDetail.value.electricAccountType !== MONTHLY_ELECTRIC_ACCOUNT_TYPE
})
const showDetailMonthlyPayAmount = computed(() => {
  if (!accountDetail.value) {
    return false
  }
  return accountDetail.value.electricAccountType === MONTHLY_ELECTRIC_ACCOUNT_TYPE
})

const openDetail = async (account: AccountItem) => {
  if (account.id === undefined) {
    setNotice('error', '当前账户缺少 ID')
    return
  }

  detailVisible.value = true
  detailLoading.value = true
  accountDetail.value = null

  try {
    accountDetail.value = await fetchAccountDetail(account.id)
  } catch (error) {
    setNotice('error', `账户详情加载失败：${getErrorMessage(error)}`)
  } finally {
    detailLoading.value = false
  }
}

const closeDetail = () => {
  detailVisible.value = false
}

const openVisible = ref(false)

const openOpenDialog = () => {
  openVisible.value = true
}

const handleOpenAccountSuccess = async (payload: { accountId: number }) => {
  setNotice('success', `开户成功，账户 ID：${payload.accountId}`)
  await loadAccounts()
}

const cancelVisible = ref(false)
const canceling = ref(false)
const cancelInitLoading = ref(false)
const selectedAccount = ref<AccountItem | null>(null)
const cancelMeterLoading = ref(false)
const cancelRemark = ref('')
const offlineCancelVisible = ref(false)
const offlineCancelSubmitting = ref(false)
const currentNoticeHost = computed<'page' | 'detail' | 'cancel' | 'offline'>(() => {
  if (offlineCancelVisible.value) {
    return 'offline'
  }
  if (cancelVisible.value) {
    return 'cancel'
  }
  if (detailVisible.value) {
    return 'detail'
  }
  return 'page'
})
const showPageNotice = computed(() => {
  return Boolean(notice.text) && currentNoticeHost.value === 'page'
})
const showDetailNotice = computed(() => {
  return Boolean(notice.text) && currentNoticeHost.value === 'detail'
})
const showCancelNotice = computed(() => {
  return Boolean(notice.text) && currentNoticeHost.value === 'cancel'
})
const showOfflineNotice = computed(() => {
  return Boolean(notice.text) && currentNoticeHost.value === 'offline'
})

const cancelSummary = reactive({
  ownerName: '—',
  contactName: '—',
  contactPhone: '—',
  electricAccountTypeName: '—',
  electricAccountType: undefined as number | undefined,
  openedElectricMeterCount: '0',
  electricBalanceText: '—',
  electricBalanceValue: 0
})

const cancelFilterInput = reactive({
  spaceNo: ''
})

const cancelFilterApplied = reactive({
  spaceNo: ''
})

const cancelSourceMeters = ref<CancelDialogMeterRow[]>([])
const selectedCancelMeterIds = ref<number[]>([])
const offlineCancelForm = reactive<OfflineCancelFormState>({
  meterId: 0,
  meterName: '—',
  deviceNo: '—',
  spaceNo: '—',
  spaceParentName: '—',
  meterBalanceText: '—',
  offlineDurationText: '—',
  recentTotalPowerText: '—',
  recentPowerHigherText: '—',
  recentPowerHighText: '—',
  recentPowerLowText: '—',
  recentPowerLowerText: '—',
  recentPowerDeepLowText: '—',
  powerHigher: '',
  powerHigh: '',
  powerLow: '',
  powerLower: '',
  powerDeepLow: ''
})

const resetCancelSummary = () => {
  cancelSummary.ownerName = '—'
  cancelSummary.contactName = '—'
  cancelSummary.contactPhone = '—'
  cancelSummary.electricAccountTypeName = '—'
  cancelSummary.electricAccountType = undefined
  cancelSummary.openedElectricMeterCount = '0'
  cancelSummary.electricBalanceText = '—'
  cancelSummary.electricBalanceValue = 0
}

const resetCancelFilters = () => {
  cancelFilterInput.spaceNo = ''
  cancelFilterApplied.spaceNo = ''
}

const resetCancelState = () => {
  cancelRemark.value = ''
  cancelSourceMeters.value = []
  selectedCancelMeterIds.value = []
  resetCancelFilters()
  resetCancelSummary()
  resetOfflineCancelForm()
}

const getCancelSpaceParentLabel = (value: AccountMeter['spaceParentNames']) => {
  const label = getSpaceParentNamesLabel(value)
  if (!label || label === '--') {
    return '—'
  }
  return label
}

const getCancelMeterTypeLabel = (meter: AccountMeter) => {
  const meterWithType = meter as AccountMeter & {
    meterTypeName?: string
    meterType?: number | string
  }
  if (meterWithType.meterTypeName) {
    return meterWithType.meterTypeName
  }
  if (meterWithType.meterType !== undefined && meterWithType.meterType !== null) {
    return String(meterWithType.meterType)
  }
  return '电表'
}

const buildCancelMeterRows = (account: AccountItem): CancelDialogMeterRow[] => {
  const sourceMeters: AccountMeter[] = account.meterList || []
  return sourceMeters.map((meter, index) => {
    const safeId = typeof meter.id === 'number' ? meter.id : (account.id || 1) * 1000 + index + 1
    const meterBalanceValue = parseAmount(meter.meterBalanceAmountText || meter.balance)
    const onlineStatus: '1' | '0' = meter.isOnline === false ? '0' : '1'
    const meterBalanceText =
      typeof meter.meterBalanceAmountText === 'string' && meter.meterBalanceAmountText.trim()
        ? meter.meterBalanceAmountText.trim()
        : formatAmount(meterBalanceValue)

    return {
      id: safeId,
      spaceNo: normalizeText(meter.spaceName),
      spaceParentName: getCancelSpaceParentLabel(meter.spaceParentNames),
      meterName: normalizeText(meter.meterName),
      deviceNo: normalizeText(meter.deviceNo),
      meterTypeName: getCancelMeterTypeLabel(meter),
      meterBalanceText,
      meterBalanceValue,
      onlineStatus,
      onlineStatusText: onlineStatus === '1' ? '在线' : '离线',
      offlineDurationText:
        typeof meter.offlineDurationText === 'string' && meter.offlineDurationText.trim()
          ? meter.offlineDurationText.trim()
          : '--'
    }
  })
}

const isOnlineMeterRow = (row: CancelDialogMeterRow) => row.onlineStatus === '1'

const getCancelMeterOnlineStatusClass = (row: CancelDialogMeterRow) => {
  if (row.onlineStatus === '1') {
    return 'meter-online-status meter-online-status-online'
  }
  if (row.onlineStatus === '0') {
    return 'meter-online-status meter-online-status-offline'
  }
  return 'meter-online-status'
}

const filteredCancelMeterRows = computed(() => {
  const spaceNoKeyword = cancelFilterApplied.spaceNo.trim().toLowerCase()
  return cancelSourceMeters.value.filter((row) => {
    if (spaceNoKeyword && !row.spaceNo.toLowerCase().includes(spaceNoKeyword)) {
      return false
    }
    return true
  })
})

const cancelVisibleMeterIds = computed(() => {
  return filteredCancelMeterRows.value.map((item) => item.id)
})

const cancelSelectableMeterIds = computed(() => {
  return filteredCancelMeterRows.value.filter(isOnlineMeterRow).map((item) => item.id)
})

const allCancelMetersSelected = computed(() => {
  if (!cancelSelectableMeterIds.value.length) {
    return false
  }
  return cancelSelectableMeterIds.value.every((id) => selectedCancelMeterIds.value.includes(id))
})

const partiallyCancelMetersSelected = computed(() => {
  if (!cancelSelectableMeterIds.value.length) {
    return false
  }
  const selectedCount = cancelSelectableMeterIds.value.filter((id) =>
    selectedCancelMeterIds.value.includes(id)
  ).length
  return selectedCount > 0 && selectedCount < cancelSelectableMeterIds.value.length
})

const selectedCancelMeterCount = computed(() => {
  return selectedCancelMeterIds.value.length
})

const unselectedCancelMeterCount = computed(() => {
  const count = cancelSourceMeters.value.length - selectedCancelMeterIds.value.length
  return count > 0 ? count : 0
})

const expectedSettlementAmount = computed(() => {
  const selectedSet = new Set(selectedCancelMeterIds.value)
  const selectedBalance = cancelSourceMeters.value.reduce((sum, row) => {
    if (!selectedSet.has(row.id)) {
      return sum
    }
    return sum + row.meterBalanceValue
  }, 0)

  if (
    selectedCancelMeterIds.value.length > 0 &&
    selectedCancelMeterIds.value.length === cancelSourceMeters.value.length &&
    (cancelSummary.electricAccountType === MONTHLY_ELECTRIC_ACCOUNT_TYPE ||
      cancelSummary.electricAccountType === MERGED_ELECTRIC_ACCOUNT_TYPE)
  ) {
    return cancelSummary.electricBalanceValue
  }

  return selectedBalance
})

const runCancelFilter = async () => {
  cancelMeterLoading.value = true
  cancelFilterApplied.spaceNo = cancelFilterInput.spaceNo
  await wait(180)
  cancelMeterLoading.value = false
}

const handleCancelSearch = async () => {
  await runCancelFilter()
}

const handleCancelReset = async () => {
  resetCancelFilters()
  await runCancelFilter()
}

const handleCancelToggleAll = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked
  const selectedSet = new Set(selectedCancelMeterIds.value)

  for (const meterId of cancelSelectableMeterIds.value) {
    if (checked) {
      selectedSet.add(meterId)
    } else {
      selectedSet.delete(meterId)
    }
  }

  selectedCancelMeterIds.value = Array.from(selectedSet)
}

const handleCancelToggleRow = (row: CancelDialogMeterRow, event: Event) => {
  if (row.onlineStatus !== '1') {
    return
  }

  const checked = (event.target as HTMLInputElement).checked
  const selectedSet = new Set(selectedCancelMeterIds.value)
  const meterId = row.id

  if (checked) {
    selectedSet.add(meterId)
  } else {
    selectedSet.delete(meterId)
  }
  selectedCancelMeterIds.value = Array.from(selectedSet)
}

const fillCancelSummary = (account: AccountItem) => {
  cancelSummary.ownerName = normalizeText(account.ownerName)
  cancelSummary.contactName = normalizeText(account.contactName)
  cancelSummary.contactPhone = normalizeText(account.contactPhone)
  cancelSummary.electricAccountTypeName = normalizeText(getElectricAccountTypeLabel(account))
  cancelSummary.electricAccountType = account.electricAccountType
  cancelSummary.openedElectricMeterCount = String(getOpenedMeterCount(account))
  cancelSummary.electricBalanceText = normalizeText(getElectricBalanceAmountText(account))
  cancelSummary.electricBalanceValue = parseAmount(account.electricBalanceAmountText)
}

const refreshCancelAccountDetail = async (accountId: number) => {
  cancelInitLoading.value = true
  try {
    const detail = await fetchAccountDetail(accountId)
    selectedAccount.value = detail
    fillCancelSummary(detail)
    cancelSourceMeters.value = buildCancelMeterRows(detail)
    const validOnlineIds = new Set(
      cancelSourceMeters.value.filter((item) => item.onlineStatus === '1').map((item) => item.id)
    )
    selectedCancelMeterIds.value = selectedCancelMeterIds.value.filter((id) =>
      validOnlineIds.has(id)
    )
  } catch (error) {
    setNotice('error', `销户信息加载失败：${getErrorMessage(error)}`)
    cancelSourceMeters.value = []
    selectedCancelMeterIds.value = []
  } finally {
    cancelInitLoading.value = false
  }
}

const openCancelDialog = async (account: AccountItem) => {
  if (account.id === undefined || account.id === null) {
    setNotice('error', '当前账户缺少 ID')
    return
  }

  resetCancelState()
  cancelVisible.value = true
  await refreshCancelAccountDetail(account.id)
}

const closeCancelDialog = () => {
  if (canceling.value || offlineCancelSubmitting.value) {
    return
  }

  offlineCancelVisible.value = false
  cancelVisible.value = false
  selectedAccount.value = null
  resetCancelState()
}

const handleCancelMaskClick = () => {
  // 按业务规则，点击遮罩不关闭弹窗。
}

const toRequiredNonNegativeNumber = (value: string, label: string): number => {
  const source = value.trim()
  if (!source) {
    throw new Error(`请输入${label}`)
  }

  const parsed = Number(source)
  if (!Number.isFinite(parsed) || parsed < 0) {
    throw new Error(`${label}必须是大于等于0的数字`)
  }

  return parsed
}

const resetOfflineCancelForm = () => {
  offlineCancelForm.meterId = 0
  offlineCancelForm.meterName = '—'
  offlineCancelForm.deviceNo = '—'
  offlineCancelForm.spaceNo = '—'
  offlineCancelForm.spaceParentName = '—'
  offlineCancelForm.meterBalanceText = '—'
  offlineCancelForm.offlineDurationText = '—'
  offlineCancelForm.recentTotalPowerText = '—'
  offlineCancelForm.recentPowerHigherText = '—'
  offlineCancelForm.recentPowerHighText = '—'
  offlineCancelForm.recentPowerLowText = '—'
  offlineCancelForm.recentPowerLowerText = '—'
  offlineCancelForm.recentPowerDeepLowText = '—'
  offlineCancelForm.powerHigher = ''
  offlineCancelForm.powerHigh = ''
  offlineCancelForm.powerLow = ''
  offlineCancelForm.powerLower = ''
  offlineCancelForm.powerDeepLow = ''
}

const applyLatestPowerRecord = (record: LatestPowerRecord) => {
  offlineCancelForm.recentTotalPowerText = record.power || '—'
  offlineCancelForm.recentPowerHigherText = record.powerHigher || '—'
  offlineCancelForm.recentPowerHighText = record.powerHigh || '—'
  offlineCancelForm.recentPowerLowText = record.powerLow || '—'
  offlineCancelForm.recentPowerLowerText = record.powerLower || '—'
  offlineCancelForm.recentPowerDeepLowText = record.powerDeepLow || '—'
}

const loadOfflineLatestPowerRecord = async (meterId: number) => {
  const requestId = ++offlineLatestPowerRequestId

  try {
    const record = await fetchLatestPowerRecord(meterId)
    if (
      requestId !== offlineLatestPowerRequestId ||
      !offlineCancelVisible.value ||
      offlineCancelForm.meterId !== meterId
    ) {
      return
    }
    applyLatestPowerRecord(record)
  } catch (error) {
    if (
      requestId !== offlineLatestPowerRequestId ||
      !offlineCancelVisible.value ||
      offlineCancelForm.meterId !== meterId
    ) {
      return
    }
    setNotice('error', `最近上报读数获取失败：${getErrorMessage(error)}`)
  }
}

const openOfflineCancelDialog = (row: CancelDialogMeterRow) => {
  if (row.onlineStatus !== '0') {
    return
  }
  resetOfflineCancelForm()
  offlineCancelForm.meterId = row.id
  offlineCancelForm.meterName = row.meterName
  offlineCancelForm.deviceNo = row.deviceNo
  offlineCancelForm.spaceNo = row.spaceNo
  offlineCancelForm.spaceParentName = row.spaceParentName
  offlineCancelForm.meterBalanceText = row.meterBalanceText
  offlineCancelForm.offlineDurationText = row.offlineDurationText
  offlineCancelVisible.value = true
  void loadOfflineLatestPowerRecord(row.id)
}

const closeOfflineCancelDialog = () => {
  if (offlineCancelSubmitting.value) {
    return
  }
  offlineLatestPowerRequestId += 1
  offlineCancelVisible.value = false
  resetOfflineCancelForm()
}

const submitOfflineCancel = async () => {
  if (!selectedAccount.value?.id) {
    setNotice('error', '缺少账户ID，无法提交离线销户')
    return
  }

  try {
    const payload = {
      meterId: offlineCancelForm.meterId,
      powerHigher: toRequiredNonNegativeNumber(offlineCancelForm.powerHigher, '尖电量'),
      powerHigh: toRequiredNonNegativeNumber(offlineCancelForm.powerHigh, '峰电量'),
      powerLow: toRequiredNonNegativeNumber(offlineCancelForm.powerLow, '平电量'),
      powerLower: toRequiredNonNegativeNumber(offlineCancelForm.powerLower, '谷电量'),
      powerDeepLow: toRequiredNonNegativeNumber(offlineCancelForm.powerDeepLow, '深谷电量')
    }

    offlineCancelSubmitting.value = true
    const result = await cancelAccount({
      accountId: selectedAccount.value.id,
      remark: cancelRemark.value.trim() || undefined,
      meterList: [payload]
    })

    setNotice(
      'success',
      `离线销户成功，销户编号：${result.cancelNo || '--'}，结算金额：${getCleanBalanceAmountText(
        result
      )}`
    )
    offlineCancelVisible.value = false
    resetOfflineCancelForm()
    await refreshCancelAccountDetail(selectedAccount.value.id)
    await loadAccounts()
  } catch (error) {
    setNotice('error', `离线销户失败：${getErrorMessage(error)}`)
  } finally {
    offlineCancelSubmitting.value = false
  }
}

const submitCancel = async () => {
  if (!selectedAccount.value?.id) {
    setNotice('error', '缺少账户ID，无法提交销户')
    return
  }

  if (!selectedCancelMeterIds.value.length) {
    setNotice('error', '请至少选择一个表具后再提交销户')
    return
  }

  canceling.value = true
  try {
    const selectedSet = new Set(selectedCancelMeterIds.value)
    const selectedRows = cancelSourceMeters.value.filter((row) => selectedSet.has(row.id))
    const hasOffline = selectedRows.some((row) => row.onlineStatus === '0')
    if (hasOffline) {
      throw new Error('离线电表不支持批量销户，请使用“离线销户”单独处理')
    }

    const result = await cancelAccount({
      accountId: selectedAccount.value.id,
      remark: cancelRemark.value.trim() || undefined,
      meterList: selectedRows.map((row) => ({ meterId: row.id }))
    })

    setNotice(
      'success',
      `销户成功，销户编号：${result.cancelNo || '--'}，结算类型：${getCleanBalanceTypeLabel(
        result.cleanBalanceType
      )}，结算金额：${getCleanBalanceAmountText(result)}`
    )
    offlineCancelVisible.value = false
    cancelVisible.value = false
    selectedAccount.value = null
    resetCancelState()
    await loadAccounts()
  } catch (error) {
    setNotice('error', `销户失败：${getErrorMessage(error)}`)
  } finally {
    canceling.value = false
  }
}

onMounted(async () => {
  await loadElectricAccountTypeOptions()
  await loadCleanBalanceTypeOptions()
  await loadAccounts()
})

onBeforeUnmount(() => {
  clearNoticeTimers()
})
</script>

<template>
  <div class="page">
    <div
      v-if="showPageNotice"
      :class="[
        'notice',
        'notice-page',
        `notice-${notice.type}`,
        { 'notice-fade-out': noticeFading }
      ]"
    >
      {{ notice.text }}
    </div>

    <section class="search-card">
      <div class="search-row">
        <label class="search-item">
          <span class="search-label-inline">账户名称</span>
          <input
            class="search-input search-radius-micro"
            v-model="queryForm.ownerName"
            placeholder="请输入账户名称"
          />
        </label>
        <label class="search-item search-item-secondary">
          <span class="search-label-inline">电费计价类型</span>
          <select
            class="search-input search-radius-micro"
            :class="{ 'search-input-placeholder': !queryForm.electricAccountType }"
            v-model="queryForm.electricAccountType"
          >
            <option value="">请选择电价计费类型</option>
            <option
              v-for="option in electricAccountTypeOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </label>
        <div class="search-actions">
          <button class="btn btn-primary search-radius-micro" @click="search" :disabled="loading">
            查询
          </button>
          <button
            class="btn btn-secondary search-radius-micro"
            @click="resetQuery"
            :disabled="loading"
          >
            重置
          </button>
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-toolbar">
        <h2 class="table-title">账户信息</h2>
        <button
          v-menu-permission="accountPermissionKeys.open"
          class="btn btn-primary search-radius-micro"
          @click="openOpenDialog"
        >
          开户
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>账户名称</th>
              <th>账户类型</th>
              <th>联系人</th>
              <th>联系方式</th>
              <th>电价计费类型</th>
              <th>电价方案</th>
              <th>电费余额</th>
              <th>已开户电表数</th>
              <th>总电表数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="11" class="empty">
                <UiLoadingState :size="18" :thickness="2" :min-height="56" />
              </td>
            </tr>
            <tr v-for="(row, index) in accountPage.list" :key="row.id ?? `${row.ownerId}-${index}`">
              <td>{{ getSerialNumber(index) }}</td>
              <td>{{ row.ownerName || '--' }}</td>
              <td>{{ getOwnerTypeLabel(row) }}</td>
              <td>{{ row.contactName || '--' }}</td>
              <td>{{ row.contactPhone || '--' }}</td>
              <td>{{ getElectricAccountTypeLabel(row) }}</td>
              <td>{{ getElectricPricePlanLabel(row) }}</td>
              <td>{{ getElectricBalanceAmountText(row) }}</td>
              <td>{{ getOpenedMeterCount(row) }}</td>
              <td>{{ getTotalOpenableMeterCount(row) }}</td>
              <td class="actions">
                <button
                  v-menu-permission="accountPermissionKeys.detail"
                  class="btn-link"
                  @click="openDetail(row)"
                >
                  详情
                </button>
                <button
                  v-menu-permission="accountPermissionKeys.cancel"
                  class="btn-link btn-link-danger"
                  @click="openCancelDialog(row)"
                >
                  销户
                </button>
              </td>
            </tr>
            <tr v-if="!loading && accountPage.list.length === 0">
              <td colspan="11" class="empty">
                <UiEmptyState :min-height="56" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <CommonPagination
        class="pager"
        :total="accountPage.total"
        :page-num="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :loading="loading"
        @change="handlePageChange"
      />
    </section>

    <Transition name="account-modal-fade" appear>
      <div v-if="detailVisible" class="modal-mask" @click.self.prevent>
        <section class="modal modal-detail">
          <div
            v-if="showDetailNotice"
            :class="[
              'notice',
              'notice-modal',
              `notice-${notice.type}`,
              { 'notice-fade-out': noticeFading }
            ]"
          >
            {{ notice.text }}
          </div>
          <header class="modal-head">
            <h3>账户详情</h3>
            <button class="btn btn-secondary icon-btn" @click="closeDetail">关闭</button>
          </header>

          <div v-if="detailLoading" class="modal-body modal-body-loading">
            <UiLoadingState :size="20" :thickness="2" :min-height="180" />
          </div>
          <div v-else-if="accountDetail" class="modal-body detail-modal-body">
            <div class="detail-summary-grid">
              <div class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">账户名称</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  accountDetail.ownerName || '--'
                }}</span>
              </div>
              <div class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">账户类型</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  getOwnerTypeLabel(accountDetail)
                }}</span>
              </div>
              <div class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">联系人</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  accountDetail.contactName || '--'
                }}</span>
              </div>
              <div class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">联系方式</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  accountDetail.contactPhone || '--'
                }}</span>
              </div>
              <div class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">电价计费类型</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  getElectricAccountTypeLabel(accountDetail)
                }}</span>
              </div>
              <div v-if="showDetailElectricPricePlan" class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">电价方案</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  getElectricPricePlanLabel(accountDetail)
                }}</span>
              </div>
              <div v-if="showDetailMonthlyPayAmount" class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">包月金额</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  accountDetail.monthlyPayAmount ?? '--'
                }}</span>
              </div>
              <div class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">电费余额</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  getElectricBalanceAmountText(accountDetail)
                }}</span>
              </div>
              <div class="detail-summary-item">
                <span class="detail-summary-label es-detail-label">预警方案</span>
                <span class="detail-summary-value es-detail-value-box">{{
                  getWarnPlanLabel(accountDetail)
                }}</span>
              </div>
            </div>

            <div class="detail-section">
              <div class="detail-section-title es-detail-section-title">
                开户信息（总电表数：{{
                  getTotalOpenableMeterCount(accountDetail)
                }}，已开户电表数：{{ getOpenedMeterCount(accountDetail) }}）
              </div>

              <div class="table-wrap detail-meter-table-wrap es-detail-table-wrap">
                <table class="es-detail-table">
                  <thead>
                    <tr>
                      <th>序号</th>
                      <th>所在位置</th>
                      <th>所属区域</th>
                      <th>电表名称</th>
                      <th>电表编号</th>
                      <th>电表余额</th>
                      <th>电费预警级别</th>
                      <th>是否在线</th>
                      <th>CT变比</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="(meter, meterIndex) in accountDetail.meterList"
                      :key="meter.id ?? meterIndex"
                    >
                      <td>{{ meterIndex + 1 }}</td>
                      <td>{{ meter.spaceName || '--' }}</td>
                      <td>{{ getSpaceParentNamesLabel(meter.spaceParentNames) }}</td>
                      <td>{{ meter.meterName || '--' }}</td>
                      <td>{{ meter.deviceNo || '--' }}</td>
                      <td>{{ meter.meterBalanceAmountText || meter.balance || '--' }}</td>
                      <td>{{ meter.warnTypeName || meter.warnType || '--' }}</td>
                      <td>
                        <span :class="getMeterOnlineStatusClass(meter)">
                          {{ getMeterOnlineStatusLabel(meter) }}
                        </span>
                      </td>
                      <td>{{ meter.ct ?? '--' }}</td>
                    </tr>
                    <tr v-if="accountDetail.meterList.length === 0">
                      <td colspan="9" class="empty">
                        <UiEmptyState text="暂无开户电表数据" :min-height="56" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          <div v-else class="modal-body detail-modal-body">
            <UiEmptyState text="暂无详情数据" :min-height="180" />
          </div>
        </section>
      </div>
    </Transition>

    <OpenAccountModal v-model="openVisible" @success="handleOpenAccountSuccess" />

    <Transition name="account-modal-fade" appear>
      <div v-if="cancelVisible" class="modal-mask" @click.self.prevent="handleCancelMaskClick">
        <section class="modal modal-cancel">
          <div
            v-if="showCancelNotice"
            :class="[
              'notice',
              'notice-modal',
              `notice-${notice.type}`,
              { 'notice-fade-out': noticeFading }
            ]"
          >
            {{ notice.text }}
          </div>
          <header class="modal-head">
            <h3>批量销户</h3>
            <button
              class="btn btn-secondary icon-btn"
              @click="closeCancelDialog"
              :disabled="canceling || offlineCancelSubmitting"
            >
              关闭
            </button>
          </header>

          <div class="modal-body cancel-modal-body">
            <div v-if="cancelInitLoading" class="cancel-init-loading">
              <UiLoadingState :size="20" :thickness="2" :min-height="64" />
            </div>

            <template v-else>
              <section class="cancel-panel">
                <div class="cancel-panel-title">账户信息</div>

                <label class="cancel-form-item cancel-form-item-full">
                  <span class="cancel-form-label">
                    <span class="required-star">*</span>
                    机构名称：
                  </span>
                  <input class="form-input" :value="cancelSummary.ownerName" readonly />
                </label>

                <div class="cancel-info-grid">
                  <div class="cancel-info-item">
                    <span class="cancel-info-label">负责人：</span>
                    <span class="cancel-info-value">{{ cancelSummary.contactName }}</span>
                  </div>
                  <div class="cancel-info-item">
                    <span class="cancel-info-label">手机号码：</span>
                    <span class="cancel-info-value">{{ cancelSummary.contactPhone }}</span>
                  </div>
                  <div class="cancel-info-item">
                    <span class="cancel-info-label">电价计费类型：</span>
                    <span class="cancel-info-value">{{
                      cancelSummary.electricAccountTypeName
                    }}</span>
                  </div>
                  <div class="cancel-info-item">
                    <span class="cancel-info-label">开户电表数量：</span>
                    <span class="cancel-info-value">{{
                      cancelSummary.openedElectricMeterCount
                    }}</span>
                  </div>
                  <div class="cancel-info-item">
                    <span class="cancel-info-label">电费余额（元）：</span>
                    <span class="cancel-info-value">{{ cancelSummary.electricBalanceText }}</span>
                  </div>
                </div>
              </section>

              <section class="cancel-panel">
                <div class="cancel-panel-title">
                  选择销户信息（已选表具数：{{ selectedCancelMeterCount }}，未选表具数：{{
                    unselectedCancelMeterCount
                  }}）
                </div>

                <div class="cancel-query-row">
                  <label class="cancel-inline-field">
                    <span class="cancel-inline-label">所在位置</span>
                    <input
                      class="form-input cancel-compact-input"
                      v-model="cancelFilterInput.spaceNo"
                      placeholder="请输入所在位置"
                    />
                  </label>

                  <div class="cancel-query-actions">
                    <button
                      class="btn btn-primary"
                      @click="handleCancelSearch"
                      :disabled="canceling || cancelMeterLoading"
                    >
                      查询
                    </button>
                    <button
                      class="btn btn-secondary"
                      @click="handleCancelReset"
                      :disabled="canceling || cancelMeterLoading"
                    >
                      重置
                    </button>
                  </div>
                </div>

                <div class="table-wrap cancel-meter-table-wrap">
                  <table class="cancel-meter-table">
                    <thead>
                      <tr>
                        <th class="checkbox-col">
                          <input
                            type="checkbox"
                            :checked="allCancelMetersSelected"
                            :indeterminate.prop="partiallyCancelMetersSelected"
                            @change="handleCancelToggleAll"
                          />
                        </th>
                        <th>序号</th>
                        <th>所在位置</th>
                        <th>所属区域</th>
                        <th>表具名称</th>
                        <th>表具编号</th>
                        <th>表具类型</th>
                        <th class="cancel-col-balance">表具余额（元）</th>
                        <th>在线状态</th>
                        <th class="cancel-col-offline">离线时长</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-if="cancelMeterLoading">
                        <td colspan="11" class="empty">
                          <UiLoadingState :size="18" :thickness="2" :min-height="56" />
                        </td>
                      </tr>
                      <tr v-else-if="filteredCancelMeterRows.length === 0">
                        <td colspan="11" class="empty">
                          <UiEmptyState :min-height="56" />
                        </td>
                      </tr>
                      <tr v-else v-for="(row, index) in filteredCancelMeterRows" :key="row.id">
                        <td class="checkbox-col">
                          <input
                            type="checkbox"
                            :checked="selectedCancelMeterIds.includes(row.id)"
                            :disabled="row.onlineStatus !== '1'"
                            @change="handleCancelToggleRow(row, $event)"
                          />
                        </td>
                        <td>{{ index + 1 }}</td>
                        <td>{{ row.spaceNo }}</td>
                        <td>{{ row.spaceParentName }}</td>
                        <td>{{ row.meterName }}</td>
                        <td>{{ row.deviceNo }}</td>
                        <td>{{ row.meterTypeName }}</td>
                        <td class="cancel-col-balance">{{ row.meterBalanceText }}</td>
                        <td>
                          <span :class="getCancelMeterOnlineStatusClass(row)">
                            {{ row.onlineStatusText }}
                          </span>
                        </td>
                        <td class="cancel-col-offline">{{ row.offlineDurationText }}</td>
                        <td>
                          <button
                            v-if="row.onlineStatus === '0'"
                            v-menu-permission="accountPermissionKeys.offlineCancel"
                            class="btn-link"
                            type="button"
                            @click="openOfflineCancelDialog(row)"
                          >
                            离线销户
                          </button>
                          <span v-else>—</span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <p class="cancel-note">
                  <span class="required-star">*</span>
                  销户时，请手动结算退费或补缴余额，一旦完成销户，将不退费，请谨慎操作；离线电表销户，请点击离线销户
                </p>
              </section>

              <div class="cancel-bottom">
                <div class="cancel-expected"
                  >本次预计结算：{{ formatAmount(expectedSettlementAmount) }} 元</div
                >
                <label class="cancel-remark-row">
                  <span class="cancel-inline-label">备注：</span>
                  <input class="form-input" v-model="cancelRemark" placeholder="请输入备注内容" />
                </label>
              </div>
            </template>
          </div>

          <footer class="cancel-footer">
            <button
              class="btn btn-secondary"
              type="button"
              @click="closeCancelDialog"
              :disabled="canceling || offlineCancelSubmitting"
            >
              取消
            </button>
            <button
              class="btn btn-primary"
              type="button"
              @click="submitCancel"
              :disabled="
                canceling ||
                cancelInitLoading ||
                offlineCancelSubmitting ||
                selectedCancelMeterIds.length === 0
              "
            >
              确定销户
            </button>
          </footer>
        </section>
      </div>
    </Transition>

    <Transition name="account-modal-fade" appear>
      <div v-if="offlineCancelVisible" class="modal-mask" @click.self.prevent>
        <section class="modal offline-cancel-modal">
          <div
            v-if="showOfflineNotice"
            :class="[
              'notice',
              'notice-modal',
              `notice-${notice.type}`,
              { 'notice-fade-out': noticeFading }
            ]"
          >
            {{ notice.text }}
          </div>
          <header class="modal-head">
            <h3>离线销户</h3>
            <button
              class="btn btn-secondary icon-btn"
              @click="closeOfflineCancelDialog"
              :disabled="offlineCancelSubmitting"
            >
              关闭
            </button>
          </header>

          <div class="modal-body">
            <div class="offline-summary-grid">
              <div class="offline-summary-item">
                <span class="offline-summary-label">表具名称</span>
                <span class="offline-summary-value">{{ offlineCancelForm.meterName }}</span>
              </div>
              <div class="offline-summary-item">
                <span class="offline-summary-label">表具编号</span>
                <span class="offline-summary-value">{{ offlineCancelForm.deviceNo }}</span>
              </div>
              <div class="offline-summary-item">
                <span class="offline-summary-label">所在位置</span>
                <span class="offline-summary-value">{{ offlineCancelForm.spaceNo }}</span>
              </div>
              <div class="offline-summary-item">
                <span class="offline-summary-label">所属区域</span>
                <span class="offline-summary-value">{{ offlineCancelForm.spaceParentName }}</span>
              </div>
              <div class="offline-summary-item">
                <span class="offline-summary-label">表具余额</span>
                <span class="offline-summary-value">{{ offlineCancelForm.meterBalanceText }}</span>
              </div>
              <div class="offline-summary-item">
                <span class="offline-summary-label">离线时长</span>
                <span class="offline-summary-value">{{
                  offlineCancelForm.offlineDurationText
                }}</span>
              </div>
            </div>

            <section class="offline-input-panel">
              <div class="offline-total-reference">
                <span class="offline-total-reference-label">离线前最近上报总电量</span>
                <span class="offline-total-reference-value">
                  {{ formatEnergyReferenceText(offlineCancelForm.recentTotalPowerText) }}
                </span>
              </div>

              <div class="offline-form-grid">
                <label class="offline-field">
                  <span class="offline-label">尖电量</span>
                  <div class="offline-field-control">
                    <input
                      class="form-input"
                      v-model="offlineCancelForm.powerHigher"
                      placeholder="请输入尖电量"
                    />
                    <span class="offline-field-reference">
                      最近上报：{{
                        formatEnergyReferenceText(offlineCancelForm.recentPowerHigherText)
                      }}
                    </span>
                  </div>
                </label>
                <label class="offline-field">
                  <span class="offline-label">峰电量</span>
                  <div class="offline-field-control">
                    <input
                      class="form-input"
                      v-model="offlineCancelForm.powerHigh"
                      placeholder="请输入峰电量"
                    />
                    <span class="offline-field-reference">
                      最近上报：{{
                        formatEnergyReferenceText(offlineCancelForm.recentPowerHighText)
                      }}
                    </span>
                  </div>
                </label>
                <label class="offline-field">
                  <span class="offline-label">平电量</span>
                  <div class="offline-field-control">
                    <input
                      class="form-input"
                      v-model="offlineCancelForm.powerLow"
                      placeholder="请输入平电量"
                    />
                    <span class="offline-field-reference">
                      最近上报：{{
                        formatEnergyReferenceText(offlineCancelForm.recentPowerLowText)
                      }}
                    </span>
                  </div>
                </label>
                <label class="offline-field">
                  <span class="offline-label">谷电量</span>
                  <div class="offline-field-control">
                    <input
                      class="form-input"
                      v-model="offlineCancelForm.powerLower"
                      placeholder="请输入谷电量"
                    />
                    <span class="offline-field-reference">
                      最近上报：{{
                        formatEnergyReferenceText(offlineCancelForm.recentPowerLowerText)
                      }}
                    </span>
                  </div>
                </label>
                <div class="offline-field offline-field-note-wrap">
                  <span class="offline-label">深谷电量</span>
                  <div class="offline-field-control">
                    <input
                      class="form-input"
                      v-model="offlineCancelForm.powerDeepLow"
                      placeholder="请输入深谷电量"
                    />
                    <span class="offline-field-reference">
                      最近上报：{{
                        formatEnergyReferenceText(offlineCancelForm.recentPowerDeepLowText)
                      }}
                    </span>
                  </div>
                  <div class="offline-input-note">
                    <span class="required-star">*</span>
                    离线销户需要手动记录电表当前读数
                  </div>
                </div>
                <div class="offline-field offline-field-empty" aria-hidden="true"></div>
              </div>
            </section>
          </div>

          <footer class="cancel-footer">
            <button
              class="btn btn-secondary"
              type="button"
              @click="closeOfflineCancelDialog"
              :disabled="offlineCancelSubmitting"
            >
              取消
            </button>
            <button
              class="btn btn-primary"
              type="button"
              @click="submitOfflineCancel"
              :disabled="offlineCancelSubmitting"
            >
              确定销户
            </button>
          </footer>
        </section>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.notice {
  padding: 10px 12px;
  font-size: var(--es-font-size-md);
  border: 1px solid transparent;
  border-radius: 5px;
  opacity: 1;
  box-shadow: 0 8px 24px rgb(15 23 42 / 16%);
  transition: opacity 0.3s ease;
}

.notice-page {
  position: fixed;
  top: 16px;
  left: 50%;
  z-index: 80;
  width: min(760px, calc(100vw - 48px));
  pointer-events: none;
  transform: translateX(-50%);
}

.notice-modal {
  position: absolute;
  top: 10px;
  left: 50%;
  z-index: 4;
  width: calc(100% - 32px);
  pointer-events: none;
  transform: translateX(-50%);
}

.notice-fade-out {
  opacity: 0;
}

.notice-info {
  color: var(--es-color-info-text);
  background: var(--es-color-info-bg);
  border-color: var(--es-color-info-border);
}

.notice-success {
  color: var(--es-color-success-text);
  background: var(--es-color-success-bg);
  border-color: var(--es-color-success-border);
}

.notice-error {
  color: var(--es-color-error-text);
  background: var(--es-color-error-bg);
  border-color: var(--es-color-error-border);
}

.search-card,
.table-card {
  padding: 16px;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-card);
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
  margin: 0;
}

.search-item-secondary {
  margin-left: 10px;
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

.search-radius-micro {
  border-radius: 5px !important;
}

.search-input::placeholder {
  color: var(--es-color-text-placeholder);
}

.search-input-placeholder {
  color: var(--es-color-text-placeholder);
}

.search-input option {
  color: var(--es-color-text-primary);
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

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.table-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.btn {
  display: inline-flex;
  height: 36px;
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
  border-color: var(--es-color-primary);
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

.btn-danger {
  color: #fff;
  background: var(--es-color-danger);
  border-color: var(--es-color-danger);
}

.btn-danger:hover {
  opacity: 0.88;
}

.btn-link {
  height: auto;
  padding: 0;
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  line-height: 1.2;
  color: var(--es-color-primary);
  background: transparent;
  border: none;
}

.btn-link:hover {
  color: var(--es-color-primary-hover);
}

.btn-link-danger {
  color: var(--es-color-danger);
}

.btn-link-danger:hover {
  opacity: 0.85;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.table-wrap {
  overflow: auto;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

table {
  width: 100%;
  min-width: 1080px;
  border-collapse: collapse;
}

th,
td {
  padding: 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  border-bottom: 1px solid var(--es-color-border);
}

th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

tbody tr:hover {
  background: #f8fbff;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.empty {
  padding: 16px 0;
  color: var(--es-color-text-placeholder);
  text-align: center;
}

.pager {
  padding: 0 10px;
}

.modal-mask {
  position: fixed;
  z-index: 30;
  display: flex;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
  inset: 0;
  align-items: center;
  justify-content: center;
}

.account-modal-fade-enter-active,
.account-modal-fade-leave-active {
  transition: opacity 0.18s ease-out;
}

.account-modal-fade-enter-from,
.account-modal-fade-leave-to {
  opacity: 0;
}

.account-modal-fade-enter-to,
.account-modal-fade-leave-from {
  opacity: 1;
}

.account-modal-fade-enter-active .modal,
.account-modal-fade-leave-active .modal {
  transition: opacity 0.2s ease-out, transform 0.2s ease-out;
}

.account-modal-fade-enter-from .modal,
.account-modal-fade-leave-to .modal {
  opacity: 0;
  transform: translateY(8px) scale(0.995);
}

.account-modal-fade-enter-to .modal,
.account-modal-fade-leave-from .modal {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.modal {
  position: relative;
  width: min(920px, 100%);
  max-height: 90vh;
  overflow: auto;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
}

.modal-detail {
  width: min(1120px, calc(100vw - 32px));
}

.modal-head {
  display: flex;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
  justify-content: space-between;
  align-items: center;
}

.modal-head h3 {
  margin: 0;
  font-size: var(--es-font-size-md);
  color: var(--es-color-text-primary);
}

.icon-btn {
  min-width: 64px;
}

.modal-body {
  padding: 16px;
}

.detail-modal-body {
  padding-bottom: 32px;
}

.modal-body-loading {
  display: flex;
  min-height: 120px;
  align-items: center;
  justify-content: center;
}

.detail-summary-grid {
  display: grid;
  gap: 10px 56px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 14px;
}

.detail-summary-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.detail-summary-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.detail-summary-value {
  display: flex;
  min-height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #f8fafc;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  align-items: center;
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

.detail-section {
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.detail-section-title {
  padding: 12px 14px;
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
  background: var(--es-color-table-header-bg);
  border-bottom: 1px solid var(--es-color-border);
}

.detail-meter-table-wrap {
  max-height: 300px;
  margin: 0;
  overflow: auto;
  border: 0;
  border-radius: 5px;
}

.form-input {
  width: 100%;
  height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-input:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.required-star {
  line-height: 1;
  color: #ef4444;
}

.modal-cancel {
  display: flex;
  width: min(1240px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  flex-direction: column;
}

.cancel-modal-body {
  display: grid;
  overflow: auto;
  flex: 1;
  gap: 12px;
}

.cancel-panel {
  padding: 14px;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.cancel-panel-title {
  margin-bottom: 12px;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.cancel-form-item {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  margin: 0 0 12px;
}

.cancel-form-item-full {
  width: 100%;
}

.cancel-form-label {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  white-space: nowrap;
}

.cancel-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 48px;
}

.cancel-info-item {
  display: flex;
  align-items: center;
  min-height: 26px;
}

.cancel-info-label {
  min-width: 112px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  white-space: nowrap;
}

.cancel-info-value {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.cancel-query-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.cancel-inline-field {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.cancel-inline-label {
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-primary);
  white-space: nowrap;
}

.cancel-compact-input {
  width: 170px;
  max-width: 170px;
  min-width: 170px;
}

.cancel-query-actions {
  display: flex;
  margin-left: auto;
  align-items: center;
  gap: 8px;
}

.cancel-meter-table-wrap {
  max-height: 310px;
}

.cancel-meter-table {
  min-width: 1040px;
}

.cancel-meter-table .checkbox-col {
  width: 44px;
  text-align: center;
}

.cancel-meter-table .checkbox-col input {
  width: 14px;
  height: 14px;
}

.cancel-meter-table th.cancel-col-balance,
.cancel-meter-table td.cancel-col-balance {
  width: 110px;
  min-width: 110px;
  padding-right: 11px;
  white-space: nowrap;
}

.cancel-meter-table th.cancel-col-offline,
.cancel-meter-table td.cancel-col-offline {
  width: 90px;
  min-width: 90px;
  padding-left: 11px;
  white-space: nowrap;
}

.table-loading-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.cancel-note {
  margin: 12px 0 0;
  font-size: var(--es-font-size-sm);
  line-height: 1.5;
  color: var(--es-color-text-secondary);
}

.cancel-bottom {
  display: grid;
  gap: 8px;
}

.cancel-expected {
  font-size: 28px;
  font-weight: 700;
  color: var(--es-color-text-primary);
}

.cancel-remark-row {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
}

.cancel-footer {
  display: flex;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid var(--es-color-border);
  justify-content: flex-end;
  gap: 8px;
}

.offline-cancel-modal {
  width: min(1120px, calc(100vw - 32px));
}

.offline-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 56px;
  margin-bottom: 16px;
}

.offline-summary-item {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  align-items: center;
  gap: 6px;
}

.offline-summary-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.offline-summary-value {
  display: flex;
  min-height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #f8fafc;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  align-items: center;
}

.offline-input-panel {
  padding: 14px;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.offline-total-reference {
  display: flex;
  min-height: 34px;
  padding: 0 10px;
  margin-bottom: 12px;
  background: #f3f8ff;
  border: 1px solid #dbeafe;
  border-radius: 5px;
  align-items: center;
  gap: 10px;
}

.offline-total-reference-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.offline-total-reference-value {
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.offline-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 56px;
}

.offline-field {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  align-items: center;
  gap: 6px;
}

.offline-field-note-wrap {
  align-items: start;
}

.offline-field-control {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
}

.offline-field-control .form-input {
  min-width: 0;
}

.offline-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  white-space: nowrap;
}

.offline-field-reference {
  font-size: 12px;
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.offline-input-note {
  margin-top: 6px;
  font-size: var(--es-font-size-sm);
  line-height: 1.5;
  color: var(--es-color-text-secondary);
  grid-column: 1 / -1;
}

.offline-field-empty {
  visibility: hidden;
}

.btn:focus-visible,
.btn-link:focus-visible,
.search-input:focus-visible,
.form-input:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}

@media (width <= 900px) {
  .search-item {
    width: 100%;
    justify-content: space-between;
  }

  .search-item-secondary {
    margin-left: 0;
  }

  .search-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }

  .table-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .pager {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-summary-grid {
    grid-template-columns: 1fr;
  }

  .detail-summary-item {
    grid-template-columns: 92px minmax(0, 1fr);
  }

  .cancel-info-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .cancel-form-item {
    grid-template-columns: 100px minmax(0, 1fr);
  }

  .cancel-query-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }

  .cancel-remark-row {
    grid-template-columns: 1fr;
  }

  .offline-summary-grid,
  .offline-form-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .offline-summary-item,
  .offline-field {
    grid-template-columns: 72px minmax(0, 1fr);
  }

  .offline-field-control {
    grid-template-columns: 1fr;
    gap: 6px;
    align-items: start;
  }

  .offline-field-reference {
    white-space: normal;
  }

  .offline-input-note {
    grid-column: 1 / -1;
  }

  .offline-field-empty {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .account-modal-fade-enter-active,
  .account-modal-fade-leave-active,
  .account-modal-fade-enter-active .modal,
  .account-modal-fade-leave-active .modal {
    transition: none;
  }
}
</style>
