import { computed, ref } from 'vue'
import {
  createElectricMeter,
  fetchDeviceModelList,
  fetchElectricMeterDetail,
  removeElectricMeter,
  updateElectricMeterCt,
  updateElectricMeterProtect,
  updateElectricMeterSwitch,
  updateElectricMeter
} from '@/api/adapters/device'
import type { ElectricMeterFormValue, ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import { useElectricMeterActions } from '@/modules/devices/electric-meters/composables/useElectricMeterActions'

vi.mock('@/api/adapters/device', () => ({
  createElectricMeter: vi.fn(),
  fetchDeviceModelList: vi.fn(),
  fetchElectricMeterDetail: vi.fn(),
  removeElectricMeter: vi.fn(),
  updateElectricMeterCt: vi.fn(),
  updateElectricMeterProtect: vi.fn(),
  updateElectricMeterSwitch: vi.fn(),
  updateElectricMeter: vi.fn()
}))

const mockedCreateElectricMeter = vi.mocked(createElectricMeter)
const mockedFetchDeviceModelList = vi.mocked(fetchDeviceModelList)
const mockedFetchElectricMeterDetail = vi.mocked(fetchElectricMeterDetail)
const mockedRemoveElectricMeter = vi.mocked(removeElectricMeter)
const mockedUpdateElectricMeterCt = vi.mocked(updateElectricMeterCt)
const mockedUpdateElectricMeterProtect = vi.mocked(updateElectricMeterProtect)
const mockedUpdateElectricMeterSwitch = vi.mocked(updateElectricMeterSwitch)
const mockedUpdateElectricMeter = vi.mocked(updateElectricMeter)

const createRow = (overrides: Partial<ElectricMeterItem> = {}): ElectricMeterItem => ({
  id: 1,
  meterName: 'A1-101总表',
  deviceNo: 'EM-001',
  meterAddress: '11',
  modelId: 102,
  modelName: 'DTSD1888',
  communicateModel: 'RS485',
  isCt: true,
  ct: '150',
  gatewayId: 1,
  gatewayName: '1号网关',
  gatewayDeviceNo: 'GW-001',
  gatewaySn: 'GW-SN-001',
  portNo: '1',
  imei: '',
  payType: 1,
  isCalculate: true,
  calculateType: '2',
  calculateTypeName: '阶梯计量',
  spaceId: '9',
  spaceName: '101',
  spacePath: 'A区 / 1号楼 / 101',
  onlineStatus: 1,
  onlineStatusName: '在线',
  offlineDuration: '',
  status: 0,
  statusName: '合闸',
  protectedModel: false,
  pricePlanName: '居民电价',
  warnPlanName: '默认预警',
  electricWarnTypeName: '一级预警',
  accountId: 99,
  ...overrides
})

const createPayload = (): ElectricMeterFormValue => ({
  id: 1,
  meterName: ' A1-101总表 ',
  deviceNo: ' EM-001 ',
  meterAddress: ' 11 ',
  modelId: '102',
  modelName: 'DTSD1888',
  communicateModel: 'rs485',
  isNb: false,
  isCt: true,
  isPrepay: true,
  ct: ' 150 ',
  spaceId: '9',
  gatewayId: '1',
  portNo: '2',
  imei: ' 8675309 ',
  payType: '1',
  isCalculate: 'true',
  spaceName: '101',
  spacePath: 'A区 / 1号楼 / 101'
})

const createComposable = (rowsSource: ElectricMeterItem[] = [createRow()]) => {
  const rows = ref<ElectricMeterItem[]>(rowsSource)
  const queryForm = {
    pageNum: 2,
    pageSize: 10
  }
  const selectedRows = computed(() => rows.value)
  const setNotice = vi.fn()
  const loadMeterPage = vi.fn().mockResolvedValue(undefined)
  const syncSelection = vi.fn()

  const composable = useElectricMeterActions({
    setNotice,
    loadMeterPage,
    syncSelection,
    rows,
    selectedRows,
    queryForm
  })

  return {
    composable,
    rows,
    queryForm,
    setNotice,
    loadMeterPage,
    syncSelection
  }
}

const createDeferred = <T>() => {
  let resolve!: (value: T | PromiseLike<T>) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((innerResolve, innerReject) => {
    resolve = innerResolve
    reject = innerReject
  })
  return {
    promise,
    resolve,
    reject
  }
}

describe('useElectricMeterActions', () => {
  beforeEach(() => {
    mockedCreateElectricMeter.mockReset()
    mockedFetchDeviceModelList.mockReset()
    mockedFetchElectricMeterDetail.mockReset()
    mockedRemoveElectricMeter.mockReset()
    mockedUpdateElectricMeterCt.mockReset()
    mockedUpdateElectricMeterProtect.mockReset()
    mockedUpdateElectricMeterSwitch.mockReset()
    mockedUpdateElectricMeter.mockReset()
  })

  test('testOpenCreateModal_WhenCalled_ShouldResetEditingState', () => {
    const { composable } = createComposable()
    composable.editingMeter.value = createRow({ id: 3 })

    composable.openCreateModal()

    expect(composable.editMode.value).toBe('create')
    expect(composable.editingMeter.value).toBeNull()
    expect(composable.editModalVisible.value).toBe(true)
  })

  test('testOpenEditModal_WhenCalled_ShouldCloneRowAndShowModal', () => {
    const { composable } = createComposable()
    const row = createRow({ id: 5 })

    composable.openEditModal(row)

    expect(composable.editMode.value).toBe('edit')
    expect(composable.editModalVisible.value).toBe(true)
    expect(composable.editingMeter.value).toEqual(row)
    expect(composable.editingMeter.value).not.toBe(row)
  })

  test('testOpenDetailModal_WhenAdapterSucceeds_ShouldLoadNormalizedDetail', async () => {
    mockedFetchElectricMeterDetail.mockResolvedValue({
      id: 1,
      meterName: 'A1-101总表',
      deviceNo: 'EM-001',
      modelId: 102,
      modelName: 'DTSD1888',
      communicateModel: 'tcp',
      spaceId: 9,
      spaceName: '101',
      spaceParentNames: ['A区', '1号楼'],
      latestReportPower: '11.00',
      latestReportTime: '2026-03-14 10:00:00'
    })
    const { composable, setNotice } = createComposable()
    const row = createRow({ gatewayDeviceNo: 'GW-001', latestReportPower: '10.00' })

    await composable.openDetailModal(row)

    expect(composable.detailModalVisible.value).toBe(true)
    expect(composable.detailLoading.value).toBe(false)
    expect(composable.detailMeter.value?.communicateModel).toBe('TCP')
    expect(composable.detailMeter.value?.latestReportPower).toBe('11.00')
    expect(composable.detailMeter.value?.gatewayDeviceNo).toBe('GW-001')
    expect(setNotice).not.toHaveBeenCalled()
  })

  test('testOpenDetailModal_WhenAdapterFails_ShouldCloseModalAndShowNotice', async () => {
    mockedFetchElectricMeterDetail.mockRejectedValue(new Error('详情失败'))
    const { composable, setNotice } = createComposable()

    await composable.openDetailModal(createRow())

    expect(composable.detailModalVisible.value).toBe(false)
    expect(composable.detailMeter.value).toBeNull()
    expect(setNotice).toHaveBeenCalledWith('error', '详情失败')
  })

  test('testOpenDetailModal_WhenDetailMissing_ShouldShowMissingNotice', async () => {
    mockedFetchElectricMeterDetail.mockResolvedValue(undefined as never)
    const { composable, setNotice } = createComposable()

    await composable.openDetailModal(createRow())

    expect(composable.detailModalVisible.value).toBe(false)
    expect(setNotice).toHaveBeenCalledWith('error', '电表详情不存在')
  })

  test('testToggleMoreActionMenu_WhenTriggeredTwice_ShouldOpenThenClose', () => {
    const { composable } = createComposable()
    const row = createRow({ id: 9 })
    const currentTarget = {
      getBoundingClientRect: () => ({ top: 100, right: 240 })
    }

    const event = { currentTarget } as unknown as MouseEvent
    composable.toggleMoreActionMenu(row, event)
    expect(composable.moreActionMenu.value?.row.id).toBe(9)

    composable.toggleMoreActionMenu(row, event)
    expect(composable.moreActionMenu.value).toBeNull()
  })

  test('testHandleDocumentClick_WhenTargetInsideActionArea_ShouldKeepMenuOpen', () => {
    const { composable } = createComposable()
    const row = createRow({ id: 9 })
    const currentTarget = {
      getBoundingClientRect: () => ({ top: 100, right: 240 })
    }

    composable.toggleMoreActionMenu(row, { currentTarget } as unknown as MouseEvent)
    const event = {
      target: {
        closest: vi.fn((selector: string) => selector === '.meter-row-actions')
      }
    } as unknown as Event

    composable.handleDocumentClick(event)

    expect(composable.moreActionMenu.value?.row.id).toBe(9)
  })

  test('testHandleDocumentClick_WhenTargetOutsideActionArea_ShouldCloseMenu', () => {
    const { composable } = createComposable()
    const row = createRow({ id: 9 })
    const currentTarget = {
      getBoundingClientRect: () => ({ top: 100, right: 240 })
    }

    composable.toggleMoreActionMenu(row, { currentTarget } as unknown as MouseEvent)
    const event = {
      target: {
        closest: vi.fn(() => false)
      }
    } as unknown as Event

    composable.handleDocumentClick(event)

    expect(composable.moreActionMenu.value).toBeNull()
  })

  test('testToggleMoreActionMenu_WhenTargetMissing_ShouldSkip', () => {
    const { composable } = createComposable()

    composable.toggleMoreActionMenu(createRow(), { currentTarget: null } as unknown as MouseEvent)

    expect(composable.moreActionMenu.value).toBeNull()
  })

  test('testHandleSubmitMeter_WhenCreateMode_ShouldTrimPayloadAndReload', async () => {
    mockedCreateElectricMeter.mockResolvedValue(10)
    const { composable, loadMeterPage, queryForm, setNotice } = createComposable()
    composable.editMode.value = 'create'
    composable.editModalVisible.value = true

    await composable.handleSubmitMeter(createPayload())

    expect(mockedCreateElectricMeter).toHaveBeenCalledWith({
      spaceId: 9,
      meterName: 'A1-101总表',
      deviceNo: 'EM-001',
      isCalculate: true,
      calculateType: undefined,
      isPrepay: true,
      modelId: 102,
      gatewayId: 1,
      portNo: 2,
      meterAddress: 11,
      imei: undefined,
      ct: 150
    })
    expect(queryForm.pageNum).toBe(1)
    expect(loadMeterPage).toHaveBeenCalledTimes(1)
    expect(setNotice).toHaveBeenCalledWith('success', '电表添加成功')
  })

  test('testHandleSubmitMeter_WhenCreateModelInvalid_ShouldSkipCreate', async () => {
    const { composable } = createComposable()
    composable.editMode.value = 'create'
    const payload = createPayload()
    payload.modelId = ''
    payload.modelName = ''

    await composable.handleSubmitMeter(payload)

    expect(mockedCreateElectricMeter).not.toHaveBeenCalled()
  })

  test('testHandleSubmitMeter_WhenCreateFails_ShouldShowErrorNotice', async () => {
    mockedCreateElectricMeter.mockRejectedValue(new Error('新增失败'))
    const { composable, setNotice } = createComposable()
    composable.editMode.value = 'create'

    await composable.handleSubmitMeter(createPayload())

    expect(setNotice).toHaveBeenCalledWith('error', '新增失败')
  })

  test('testHandleSubmitMeter_WhenEditMode_ShouldUpdateAndReload', async () => {
    mockedUpdateElectricMeter.mockResolvedValue(undefined)
    const { composable, loadMeterPage, setNotice } = createComposable()
    composable.editMode.value = 'edit'
    composable.editingMeter.value = createRow({ id: 7 })

    await composable.handleSubmitMeter(createPayload())

    expect(mockedUpdateElectricMeter).toHaveBeenCalledWith(7, {
      spaceId: 9,
      meterName: 'A1-101总表',
      isCalculate: true,
      isPrepay: true
    })
    expect(loadMeterPage).toHaveBeenCalledTimes(1)
    expect(setNotice).toHaveBeenCalledWith('success', '电表信息更新成功')
  })

  test('testHandleSubmitMeter_WhenEditFails_ShouldShowErrorNotice', async () => {
    mockedUpdateElectricMeter.mockRejectedValue(new Error('更新失败'))
    const { composable, setNotice } = createComposable()
    composable.editMode.value = 'edit'
    composable.editingMeter.value = createRow({ id: 7 })

    await composable.handleSubmitMeter(createPayload())

    expect(setNotice).toHaveBeenCalledWith('error', '更新失败')
  })

  test('testHandleSubmitMeter_WhenEditingMeterMissing_ShouldCloseModalAndSyncSelection', async () => {
    const { composable, syncSelection } = createComposable()
    composable.editMode.value = 'edit'
    composable.editModalVisible.value = true
    composable.editingMeter.value = null

    await composable.handleSubmitMeter(createPayload())

    expect(composable.editModalVisible.value).toBe(false)
    expect(syncSelection).toHaveBeenCalledTimes(1)
  })

  test('testOpenBatchCommandConfirm_WhenSelectionInvalid_ShouldShowNotice', () => {
    const { composable, setNotice } = createComposable([])

    composable.openBatchCommandConfirm('batch-cut')
    expect(setNotice).toHaveBeenCalledWith('error', '请先勾选要操作的数据')

    const second = createComposable([createRow({ onlineStatus: 0 })])
    second.composable.openBatchCommandConfirm('batch-merge')
    expect(second.setNotice).toHaveBeenCalledWith('error', '无法对离线设备进行操作')
  })

  test('testOpenBatchProtectConfirm_WhenSelectionValid_ShouldOpenConfirm', () => {
    const { composable } = createComposable([createRow({ id: 8 })])

    composable.openBatchProtectConfirm(true)

    expect(composable.confirmState.visible).toBe(true)
    expect(composable.confirmState.action).toBe('batch-protect')
    expect(composable.confirmState.content).toContain('1')
  })

  test('testOpenBatchProtectConfirm_WhenSelectionValidAndProtectFalse_ShouldOpenUnprotectConfirm', () => {
    const { composable } = createComposable([createRow({ id: 18 })])

    composable.openBatchProtectConfirm(false)

    expect(composable.confirmState.visible).toBe(true)
    expect(composable.confirmState.action).toBe('batch-unprotect')
  })

  test('testOpenSingleCommandAndProtectConfirm_WhenTriggered_ShouldOpenConfirmState', () => {
    const { composable } = createComposable()
    const row = createRow({ id: 6 })

    composable.openSingleCommandConfirm(row, 'cut')
    expect(composable.confirmState.action).toBe('cut')
    expect(composable.confirmState.targetId).toBe(6)

    composable.openSingleProtectConfirm(row, true)
    expect(composable.confirmState.action).toBe('protect')
    expect(composable.confirmState.targetId).toBe(6)
  })

  test('testCloseConfirm_WhenCalled_ShouldResetConfirmState', () => {
    const { composable } = createComposable()
    composable.openDeleteConfirm(createRow({ id: 1 }))

    composable.closeConfirm()

    expect(composable.confirmState.visible).toBe(false)
    expect(composable.confirmState.title).toBe('')
    expect(composable.confirmState.action).toBe('')
    expect(composable.confirmState.targetId).toBe(0)
  })

  test('testHandleConfirm_WhenDeleteOnLastRow_ShouldFallbackPageAndReload', async () => {
    mockedRemoveElectricMeter.mockResolvedValue(undefined)
    const { composable, queryForm, loadMeterPage, syncSelection, setNotice } = createComposable([
      createRow({ id: 1 })
    ])
    composable.openDeleteConfirm(createRow({ id: 1 }))

    await composable.handleConfirm()

    expect(mockedRemoveElectricMeter).toHaveBeenCalledWith(1)
    expect(queryForm.pageNum).toBe(1)
    expect(loadMeterPage).toHaveBeenCalledTimes(1)
    expect(syncSelection).toHaveBeenCalledTimes(1)
    expect(setNotice).toHaveBeenCalledWith('success', '电表删除成功')
    expect(composable.confirmState.visible).toBe(false)
  })

  test('testHandleConfirm_WhenDeleteFails_ShouldShowErrorNotice', async () => {
    mockedRemoveElectricMeter.mockRejectedValue(new Error('删除失败'))
    const { composable, setNotice } = createComposable([createRow({ id: 1 })])
    composable.openDeleteConfirm(createRow({ id: 1 }))

    await composable.handleConfirm()

    expect(setNotice).toHaveBeenCalledWith('error', '删除失败')
  })

  test('testHandleConfirm_WhenCutAndProtectSucceed_ShouldShowSuccessNotice', async () => {
    mockedUpdateElectricMeterSwitch.mockResolvedValue(undefined)
    mockedUpdateElectricMeterProtect.mockResolvedValue(undefined)
    const first = createComposable()
    first.composable.openSingleCommandConfirm(createRow({ id: 10 }), 'cut')
    await first.composable.handleConfirm()
    expect(first.setNotice).toHaveBeenCalledWith('success', '断闸指令下发成功')

    const second = createComposable()
    second.composable.openSingleProtectConfirm(createRow({ id: 11 }), true)
    await second.composable.handleConfirm()
    expect(second.setNotice).toHaveBeenCalledWith('success', '保电设置成功')
  })

  test('testHandleConfirm_WhenMergeAndUnprotectSucceed_ShouldShowSuccessNotice', async () => {
    mockedUpdateElectricMeterSwitch.mockResolvedValue(undefined)
    mockedUpdateElectricMeterProtect.mockResolvedValue(undefined)

    const first = createComposable()
    first.composable.openSingleCommandConfirm(createRow({ id: 20 }), 'merge')
    await first.composable.handleConfirm()
    expect(first.setNotice).toHaveBeenCalledWith('success', '合闸指令下发成功')

    const second = createComposable()
    second.composable.openSingleProtectConfirm(createRow({ id: 21 }), false)
    await second.composable.handleConfirm()
    expect(second.setNotice).toHaveBeenCalledWith('success', '取消保电成功')
  })

  test('testHandleConfirm_WhenMergeAndUnprotectFail_ShouldShowErrorNotice', async () => {
    mockedUpdateElectricMeterSwitch.mockRejectedValue(new Error('合闸失败'))
    mockedUpdateElectricMeterProtect.mockRejectedValue(new Error('取消失败'))
    const first = createComposable()
    first.composable.openSingleCommandConfirm(createRow({ id: 12 }), 'merge')
    await first.composable.handleConfirm()
    expect(first.setNotice).toHaveBeenCalledWith('error', '合闸失败')

    const second = createComposable()
    second.composable.openSingleProtectConfirm(createRow({ id: 13 }), false)
    await second.composable.handleConfirm()
    expect(second.setNotice).toHaveBeenCalledWith('error', '取消失败')
  })

  test('testHandleConfirm_WhenSingleCommandSubmitting_ShouldPreventDuplicateSubmit', async () => {
    const deferred = createDeferred<void>()
    mockedUpdateElectricMeterSwitch.mockReturnValue(deferred.promise)
    const { composable } = createComposable()
    composable.openSingleCommandConfirm(createRow({ id: 50 }), 'cut')

    const firstPromise = composable.handleConfirm()
    expect(composable.confirmSubmitting.value).toBe(true)

    await composable.handleConfirm()
    expect(mockedUpdateElectricMeterSwitch).toHaveBeenCalledTimes(1)

    deferred.resolve(undefined)
    await firstPromise
    expect(composable.confirmSubmitting.value).toBe(false)
  })

  test('testHandleConfirm_WhenBatchCutPartiallyFails_ShouldShowPartialFailureNotice', async () => {
    mockedUpdateElectricMeterSwitch.mockImplementation(({ id }) => {
      if (id === 2) {
        return Promise.reject(new Error('失败'))
      }
      return Promise.resolve()
    })
    const { composable, loadMeterPage, syncSelection, setNotice } = createComposable([
      createRow({ id: 1 }),
      createRow({ id: 2 })
    ])
    composable.openBatchCommandConfirm('batch-cut')

    await composable.handleConfirm()

    expect(loadMeterPage).toHaveBeenCalledTimes(1)
    expect(syncSelection).toHaveBeenCalledTimes(1)
    expect(setNotice).toHaveBeenCalledWith('error', '批量断闸部分失败：成功 1 条，失败 1 条')
  })

  test('testHandleConfirm_WhenBatchMergeAndBatchProtectSucceed_ShouldShowSuccessNotice', async () => {
    mockedUpdateElectricMeterSwitch.mockResolvedValue(undefined)
    mockedUpdateElectricMeterProtect.mockResolvedValue(undefined)

    const merge = createComposable([createRow({ id: 1 }), createRow({ id: 2 })])
    merge.composable.openBatchCommandConfirm('batch-merge')
    await merge.composable.handleConfirm()
    expect(merge.setNotice).toHaveBeenCalledWith('success', '批量合闸指令下发成功')

    const protect = createComposable([createRow({ id: 3 }), createRow({ id: 4 })])
    protect.composable.openBatchProtectConfirm(false)
    await protect.composable.handleConfirm()
    expect(protect.setNotice).toHaveBeenCalledWith('success', '批量取消保电成功')
  })

  test('testHandleConfirm_WhenBatchCutSucceeds_ShouldShowSuccessNotice', async () => {
    mockedUpdateElectricMeterSwitch.mockResolvedValue(undefined)
    const { composable, setNotice } = createComposable([createRow({ id: 30 }), createRow({ id: 31 })])
    composable.openBatchCommandConfirm('batch-cut')

    await composable.handleConfirm()

    expect(setNotice).toHaveBeenCalledWith('success', '批量断闸指令下发成功')
  })

  test('testHandleConfirm_WhenBatchProtectSucceeds_ShouldShowSuccessNotice', async () => {
    mockedUpdateElectricMeterProtect.mockResolvedValue(undefined)
    const { composable, setNotice } = createComposable([createRow({ id: 32 })])
    composable.openBatchProtectConfirm(true)

    await composable.handleConfirm()

    expect(setNotice).toHaveBeenCalledWith('success', '批量保电设置成功')
  })

  test('testHandleConfirm_WhenBatchProtectFails_ShouldShowErrorNotice', async () => {
    mockedUpdateElectricMeterProtect.mockRejectedValue(new Error('批量保电失败'))
    const { composable, setNotice } = createComposable([createRow({ id: 1 })])
    composable.openBatchProtectConfirm(true)

    await composable.handleConfirm()

    expect(setNotice).toHaveBeenCalledWith('error', '批量保电失败')
  })

  test('testHandleConfirm_WhenBatchUnprotectFails_ShouldShowErrorNotice', async () => {
    mockedUpdateElectricMeterProtect.mockRejectedValue(new Error('批量取消失败'))
    const { composable, setNotice } = createComposable([createRow({ id: 33 })])
    composable.openBatchProtectConfirm(false)

    await composable.handleConfirm()

    expect(setNotice).toHaveBeenCalledWith('error', '批量取消失败')
  })

  test('testHandleConfirm_WhenActionEmpty_ShouldOnlyCloseConfirm', async () => {
    const { composable } = createComposable()
    composable.confirmState.visible = true

    await composable.handleConfirm()

    expect(composable.confirmState.visible).toBe(false)
    expect(mockedRemoveElectricMeter).not.toHaveBeenCalled()
  })

  test('testOpenCtModalAndHandleSubmitCt_WhenModelSupportsCt_ShouldOpenAndUpdateRow', async () => {
    mockedFetchDeviceModelList.mockResolvedValue([
      { id: 102, modelName: 'DTSD1888', communicateModel: 'RS485', isNb: false, isCt: true, isPrepay: true }
    ])
    mockedUpdateElectricMeterCt.mockResolvedValue(undefined)
    const { composable, rows, setNotice } = createComposable([createRow({ id: 3, ct: '150' })])
    const row = rows.value[0]!

    await composable.openCtModal(row)
    expect(composable.ctModalVisible.value).toBe(true)
    expect(composable.ctMeter.value?.id).toBe(3)

    await composable.handleSubmitCt({ id: 3, ct: '200' })
    expect(mockedUpdateElectricMeterCt).toHaveBeenCalledWith({ meterId: 3, ct: 200 })
    expect(rows.value[0]?.ct).toBe('200')
    expect(composable.ctModalVisible.value).toBe(false)
    expect(setNotice).toHaveBeenCalledWith('success', 'CT 变比设置成功')
  })

  test('testOpenCtModal_WhenModelDoesNotSupportCt_ShouldShowNotice', async () => {
    mockedFetchDeviceModelList.mockResolvedValue([
      { id: 102, modelName: 'DTSD1888', communicateModel: 'RS485', isNb: false, isCt: false, isPrepay: true }
    ])
    const { composable, setNotice } = createComposable()

    await composable.openCtModal(createRow({ modelId: 102 }))

    expect(composable.ctModalVisible.value).toBe(false)
    expect(setNotice).toHaveBeenCalledWith('error', '当前电表型号不支持修改 CT 变比')
  })

  test('testOpenCtModalAndHandleSubmitCt_WhenAdapterFails_ShouldShowErrorNotice', async () => {
    mockedFetchDeviceModelList.mockRejectedValue(new Error('型号失败'))
    mockedUpdateElectricMeterCt.mockRejectedValue(new Error('设置失败'))
    const { composable, setNotice } = createComposable([createRow({ id: 3, ct: '150' })])

    await composable.openCtModal(createRow({ id: 3, modelId: 102 }))
    expect(setNotice).toHaveBeenCalledWith('error', '型号失败')

    await composable.handleSubmitCt({ id: 3, ct: '200' })
    expect(setNotice).toHaveBeenCalledWith('error', '设置失败')
  })

  test('testHandleSubmitCt_WhenSubmitting_ShouldPreventDuplicateSubmit', async () => {
    const deferred = createDeferred<number | undefined>()
    mockedUpdateElectricMeterCt.mockReturnValue(deferred.promise)
    const { composable } = createComposable([createRow({ id: 60, ct: '150' })])

    const firstPromise = composable.handleSubmitCt({ id: 60, ct: '200' })
    expect(composable.ctSubmitting.value).toBe(true)

    await composable.handleSubmitCt({ id: 60, ct: '200' })
    expect(mockedUpdateElectricMeterCt).toHaveBeenCalledTimes(1)

    deferred.resolve(undefined)
    await firstPromise
    expect(composable.ctSubmitting.value).toBe(false)
  })
})
