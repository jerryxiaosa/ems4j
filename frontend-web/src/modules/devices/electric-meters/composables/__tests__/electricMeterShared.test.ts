import type {
  ElectricMeterDetailItem,
  ElectricMeterPageItem
} from '@/modules/devices/electric-meters/types'
import {
  formatCommunicateModel,
  getElectricMeterErrorMessage,
  getOnlineStatusClass,
  getSpaceRegion,
  normalizeElectricMeterDetailRow,
  normalizeElectricMeterRow,
  showNumberForCreate,
  toOptionalNumber
} from '@/modules/devices/electric-meters/composables/electricMeterShared'

const createPageItem = (): ElectricMeterPageItem => ({
  id: 9,
  meterName: 'A1-101总表',
  deviceNo: 'EM-001',
  meterAddress: 11,
  modelId: 12,
  modelName: 'DDSY1352-NB',
  communicateModel: 'nb',
  gatewayId: 20,
  gatewayName: '1号网关',
  portNo: 3,
  imei: '8675309',
  isOnline: true,
  isCutOff: false,
  isCalculate: true,
  calculateType: 2,
  isPrepay: true,
  protectedModel: true,
  pricePlanName: '居民电价',
  warnPlanName: '默认预警',
  electricWarnTypeName: '一级预警',
  spaceId: 6,
  spaceName: '101',
  spaceParentNames: ['A区', '1号楼'],
  offlineDurationText: '3小时',
  accountId: 99,
  ct: 150
})

describe('electricMeterShared', () => {
  test('testFormatCommunicateModel_WhenNormalized_ShouldReturnExpectedLabel', () => {
    expect(formatCommunicateModel(' tcp ')).toBe('TCP')
    expect(formatCommunicateModel('nb')).toBe('NB')
    expect(formatCommunicateModel('4G')).toBe('4G')
    expect(formatCommunicateModel('')).toBe('--')
  })

  test('testGetOnlineStatusClass_WhenStatusChanges_ShouldReturnExpectedClassName', () => {
    expect(getOnlineStatusClass({ onlineStatus: 1 })).toContain('online')
    expect(getOnlineStatusClass({ onlineStatus: 0 })).toContain('offline')
    expect(getOnlineStatusClass({ onlineStatus: null })).toContain('unknown')
  })

  test('testGetSpaceRegion_WhenPathContainsLeaf_ShouldReturnParentSegments', () => {
    expect(getSpaceRegion({ spacePath: 'A区 / 1号楼 / 101', spaceName: '101' })).toBe('A区 > 1号楼')
    expect(getSpaceRegion({ spacePath: 'A区', spaceName: 'A区' })).toBe('A区')
    expect(getSpaceRegion({ spacePath: 'A区 / 1号楼 / 102', spaceName: '101' })).toBe('A区 > 1号楼')
  })

  test('testNormalizeElectricMeterRow_WhenRawItemProvided_ShouldMapToViewItem', () => {
    const row = normalizeElectricMeterRow(createPageItem())

    expect(row).toMatchObject({
      id: 9,
      meterName: 'A1-101总表',
      deviceNo: 'EM-001',
      modelId: 12,
      modelName: 'DDSY1352-NB',
      communicateModel: 'NB',
      ct: '150',
      payType: 1,
      isCalculate: true,
      spaceId: '6',
      spaceName: '101',
      spacePath: 'A区 / 1号楼 / 101',
      onlineStatus: 1,
      onlineStatusName: '在线',
      status: 0,
      statusName: '合闸',
      protectedModel: true,
      pricePlanName: '居民电价',
      warnPlanName: '默认预警',
      electricWarnTypeName: '一级预警',
      accountId: 99
    })
  })

  test('testNormalizeElectricMeterRow_WhenParentNamesIsStringOrMissing_ShouldHandleGracefully', () => {
    const stringParentNames = normalizeElectricMeterRow({
      ...createPageItem(),
      spaceParentNames: 'A区 / 1号楼'
    })
    const missingParentNames = normalizeElectricMeterRow({
      ...createPageItem(),
      spaceParentNames: undefined,
      isOnline: false,
      isCutOff: true
    })

    expect(stringParentNames.spacePath).toBe('A区 / 1号楼 / 101')
    expect(missingParentNames.spacePath).toBe('101')
    expect(missingParentNames.onlineStatusName).toBe('离线')
    expect(missingParentNames.statusName).toBe('断闸')
  })

  test('testNormalizeElectricMeterRow_WhenOptionalFieldsMissing_ShouldUseDefaults', () => {
    const row = normalizeElectricMeterRow({
      id: 10,
      meterName: '',
      deviceNo: '',
      modelId: undefined,
      modelName: '',
      spaceName: '',
      isOnline: undefined,
      isCutOff: undefined
    })

    expect(row.meterName).toBe('--')
    expect(row.deviceNo).toBe('--')
    expect(row.modelId).toBe(0)
    expect(row.modelName).toBe('--')
    expect(row.spaceName).toBe('--')
    expect(row.onlineStatusName).toBe('--')
    expect(row.statusName).toBe('合闸')
  })

  test('testNormalizeElectricMeterDetailRow_WhenFallbackProvided_ShouldMergeLatestReportFields', () => {
    const fallback = normalizeElectricMeterRow(createPageItem())
    fallback.gatewayDeviceNo = 'GW-001'
    fallback.latestReportPower = '10.00'
    const detail: ElectricMeterDetailItem = {
      ...createPageItem(),
      latestReportTime: '2026-03-14 10:00:00',
      latestReportHigherPower: '1.00'
    }

    const row = normalizeElectricMeterDetailRow(detail, fallback)

    expect(row.gatewayDeviceNo).toBe('GW-001')
    expect(row.latestReportPower).toBe('10.00')
    expect(row.latestReportTime).toBe('2026-03-14 10:00:00')
    expect(row.latestReportHigherPower).toBe('1.00')
  })

  test('testToOptionalNumberAndShowNumberForCreate_WhenInputChanges_ShouldReturnExpectedValue', () => {
    expect(toOptionalNumber(' 12 ')).toBe(12)
    expect(toOptionalNumber('')).toBeUndefined()
    expect(toOptionalNumber('abc')).toBeUndefined()
    expect(showNumberForCreate(true, '22')).toBeUndefined()
    expect(showNumberForCreate(false, '22')).toBe(22)
  })

  test('testGetElectricMeterErrorMessage_WhenUnknownError_ShouldReturnDefaultText', () => {
    expect(getElectricMeterErrorMessage(new Error('查询失败'))).toBe('查询失败')
    expect(getElectricMeterErrorMessage('bad')).toBe('请稍后重试')
  })
})
