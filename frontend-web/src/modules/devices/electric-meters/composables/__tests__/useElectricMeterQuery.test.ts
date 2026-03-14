import { fetchElectricMeterPage } from '@/api/adapters/device'
import { useElectricMeterQuery } from '@/modules/devices/electric-meters/composables/useElectricMeterQuery'

vi.mock('@/api/adapters/device', () => ({
  fetchElectricMeterPage: vi.fn()
}))

const mockedFetchElectricMeterPage = vi.mocked(fetchElectricMeterPage)

describe('useElectricMeterQuery', () => {
  beforeEach(() => {
    mockedFetchElectricMeterPage.mockReset()
  })

  test('testInitialize_WhenAdapterSucceeds_ShouldLoadRowsAndTotal', async () => {
    mockedFetchElectricMeterPage.mockResolvedValue({
      list: [
        {
          id: 1,
          meterName: 'A1-101总表',
          deviceNo: 'EM-001',
          modelId: 12,
          modelName: 'DDSY1352-NB',
          communicateModel: 'nb',
          isOnline: true,
          isCutOff: false,
          isCalculate: true,
          isPrepay: true,
          spaceId: 9,
          spaceName: '101',
          spaceParentNames: ['A区', '1号楼']
        }
      ],
      total: 1,
      pageNum: 1,
      pageSize: 10
    })

    const composable = useElectricMeterQuery({ setNotice: vi.fn() })
    await composable.initialize()

    expect(mockedFetchElectricMeterPage).toHaveBeenCalledWith({
      searchKey: undefined,
      isOnline: undefined,
      isCutOff: undefined,
      isPrepay: undefined,
      pageNum: 1,
      pageSize: 10
    })
    expect(composable.rows.value).toHaveLength(1)
    expect(composable.total.value).toBe(1)
    expect(composable.rows.value[0]?.communicateModel).toBe('NB')
  })

  test('testLoadMeterPage_WhenFiltersProvided_ShouldTrimAndConvertQuery', async () => {
    mockedFetchElectricMeterPage.mockResolvedValue({
      list: [],
      total: 6,
      pageNum: 3,
      pageSize: 20
    })
    const composable = useElectricMeterQuery({ setNotice: vi.fn() })
    composable.queryForm.searchKey = ' EM '
    composable.queryForm.onlineStatus = 'true'
    composable.queryForm.status = 'false'
    composable.queryForm.payType = 'true'
    composable.queryForm.pageNum = 3
    composable.queryForm.pageSize = 20

    await composable.loadMeterPage()

    expect(mockedFetchElectricMeterPage).toHaveBeenCalledWith({
      searchKey: 'EM',
      isOnline: true,
      isCutOff: false,
      isPrepay: true,
      pageNum: 3,
      pageSize: 20
    })
    expect(composable.total.value).toBe(6)
  })

  test('testSelection_WhenRowsChange_ShouldMaintainExistingIdsOnly', async () => {
    mockedFetchElectricMeterPage.mockResolvedValue({
      list: [
        { id: 1, meterName: '一号表', deviceNo: 'EM-1', modelId: 1, modelName: 'M1', spaceName: '101' },
        { id: 2, meterName: '二号表', deviceNo: 'EM-2', modelId: 1, modelName: 'M1', spaceName: '102' }
      ],
      total: 2,
      pageNum: 1,
      pageSize: 10
    })
    const composable = useElectricMeterQuery({ setNotice: vi.fn() })
    composable.selectedIds.value = [1, 3]

    await composable.loadMeterPage()

    expect(composable.selectedIds.value).toEqual([1])
    composable.toggleRowSelection(composable.rows.value[1]!, true)
    expect(composable.selectedRows.value.map((item) => item.id)).toEqual([1, 2])
    composable.toggleSelectAllOnPage(false)
    expect(composable.selectedIds.value).toEqual([])
  })

  test('testHandleSearchAndReset_WhenTriggered_ShouldResetPagingAndReload', async () => {
    mockedFetchElectricMeterPage.mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10
    })
    const composable = useElectricMeterQuery({ setNotice: vi.fn() })
    composable.queryForm.searchKey = 'abc'
    composable.queryForm.onlineStatus = 'true'
    composable.queryForm.status = 'true'
    composable.queryForm.payType = 'false'
    composable.queryForm.pageNum = 5

    await composable.handleSearch()
    expect(composable.queryForm.pageNum).toBe(1)

    composable.queryForm.pageNum = 4
    composable.queryForm.pageSize = 30
    await composable.handleReset()
    expect(composable.queryForm).toMatchObject({
      searchKey: '',
      onlineStatus: '',
      status: '',
      payType: '',
      pageNum: 1,
      pageSize: 10
    })
  })

  test('testHandlePageChange_WhenLoading_ShouldSkipReload', async () => {
    const composable = useElectricMeterQuery({ setNotice: vi.fn() })
    composable.loading.value = true

    await composable.handlePageChange({
      pageNum: 2,
      pageSize: 20
    })

    expect(mockedFetchElectricMeterPage).not.toHaveBeenCalled()
    expect(composable.queryForm.pageNum).toBe(1)
    expect(composable.queryForm.pageSize).toBe(10)
  })

  test('testHandlePageChange_WhenNotLoading_ShouldUpdatePagingAndReload', async () => {
    mockedFetchElectricMeterPage.mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 2,
      pageSize: 20
    })
    const composable = useElectricMeterQuery({ setNotice: vi.fn() })

    await composable.handlePageChange({
      pageNum: 2,
      pageSize: 20
    })

    expect(composable.queryForm.pageNum).toBe(2)
    expect(composable.queryForm.pageSize).toBe(20)
    expect(composable.getSerialNumber(0)).toBe(21)
  })

  test('testLoadMeterPage_WhenAdapterFails_ShouldResetStateAndSetNotice', async () => {
    mockedFetchElectricMeterPage.mockRejectedValue(new Error('查询失败'))
    const setNotice = vi.fn()
    const composable = useElectricMeterQuery({ setNotice })
    composable.rows.value = [
      {
        id: 1,
        meterName: '旧数据',
        deviceNo: 'OLD',
        meterAddress: '',
        modelId: 1,
        modelName: '旧型号',
        communicateModel: 'TCP',
        isCt: false,
        ct: '',
        gatewayName: '',
        payType: 0,
        isCalculate: false,
        calculateType: '',
        calculateTypeName: '--',
        spaceId: '1',
        spaceName: '101',
        spacePath: 'A区 / 101',
        onlineStatus: 1,
        onlineStatusName: '在线',
        status: 0,
        statusName: '合闸'
      }
    ]
    composable.total.value = 12
    composable.selectedIds.value = [1]

    await composable.loadMeterPage()

    expect(composable.rows.value).toEqual([])
    expect(composable.total.value).toBe(0)
    expect(composable.selectedIds.value).toEqual([])
    expect(setNotice).toHaveBeenCalledWith('error', '查询失败')
  })
})
