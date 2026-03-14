export interface ElectricMeterModelOption {
  id: number
  modelName: string
  communicateModel: string
  isNb: boolean
  isCt: boolean
  isPrepay: boolean
}

export interface ElectricMeterGatewayOption {
  id: number
  gatewayName: string
  deviceNo: string
  sn?: string
}

export interface ElectricMeterOption {
  value: string
  label: string
}

export interface ElectricMeterItem {
  id: number
  meterName: string
  deviceNo: string
  meterAddress: string
  modelId: number
  modelName: string
  communicateModel: string
  isCt: boolean
  ct?: string
  gatewayId?: number
  gatewayName?: string
  gatewayDeviceNo?: string
  gatewaySn?: string
  portNo?: string
  imei?: string
  payType: 0 | 1
  isCalculate: boolean
  calculateType: string
  calculateTypeName: string
  spaceId: string
  spaceName: string
  spacePath: string
  onlineStatus: 0 | 1 | null
  onlineStatusName: string
  offlineDuration?: string
  status: 0 | 1
  statusName: string
  protectedModel?: boolean
  pricePlanName?: string
  warnPlanName?: string
  electricWarnTypeName?: string
  accountId?: number | null
  latestReportPower?: string
  latestReportTime?: string
  latestReportHigherPower?: string
  latestReportHighPower?: string
  latestReportLowPower?: string
  latestReportLowerPower?: string
  latestReportDeepLowPower?: string
}

export interface ElectricMeterFormValue {
  id?: number
  meterName: string
  deviceNo: string
  meterAddress: string
  modelId: string
  modelName?: string
  communicateModel?: string
  isNb?: boolean
  isCt?: boolean
  isPrepay?: boolean
  ct: string
  spaceId: string
  gatewayId: string
  portNo: string
  imei: string
  payType: '1' | '0'
  isCalculate: 'true' | 'false'
  spaceName?: string
  spacePath?: string
}

export const onlineStatusOptions: ElectricMeterOption[] = [
  { value: 'true', label: '在线' },
  { value: 'false', label: '离线' }
]

export const meterSwitchStatusOptions: ElectricMeterOption[] = [
  { value: 'false', label: '合闸' },
  { value: 'true', label: '断闸' }
]

export const booleanSelectOptions: ElectricMeterOption[] = [
  { value: 'true', label: '是' },
  { value: 'false', label: '否' }
]

export const spaceOptions: ElectricMeterOption[] = [
  { value: 'space-a-101', label: 'A区 / 1号楼 / 101' },
  { value: 'space-a-201', label: 'A区 / 2号楼 / 201' },
  { value: 'space-b-301', label: 'B区 / 3号楼 / 301' },
  { value: 'space-b-502', label: 'B区 / 5号楼 / 502' },
  { value: 'space-c-warehouse', label: 'C区 / 仓储区 / 总配电房' }
]

export const gatewayOptions: ElectricMeterGatewayOption[] = [
  { id: 1, gatewayName: '1号接入网关', deviceNo: 'GW-2025-001', sn: 'GW-2025-001' },
  { id: 2, gatewayName: '2号接入网关', deviceNo: 'GW-2025-002', sn: 'GW-2025-002' },
  { id: 3, gatewayName: '3号接入网关', deviceNo: 'GW-2025-003', sn: 'GW-2025-003' }
]

export const modelOptions: ElectricMeterModelOption[] = [
  {
    id: 101,
    modelName: 'DDSY1352-NB',
    communicateModel: 'NB-IoT',
    isNb: true,
    isCt: false,
    isPrepay: true
  },
  {
    id: 102,
    modelName: 'DTSD1888',
    communicateModel: 'RS485',
    isNb: false,
    isCt: true,
    isPrepay: true
  },
  {
    id: 103,
    modelName: 'DDS238-4',
    communicateModel: 'RS485',
    isNb: false,
    isCt: false,
    isPrepay: false
  },
  {
    id: 104,
    modelName: 'DTS541-4G',
    communicateModel: '4G',
    isNb: true,
    isCt: true,
    isPrepay: true
  }
]

export const electricMeterRows: ElectricMeterItem[] = [
  {
    id: 1,
    meterName: 'A1-101总表',
    deviceNo: 'EM-A1101-01',
    meterAddress: '11',
    modelId: 102,
    modelName: 'DTSD1888',
    communicateModel: 'RS485',
    isCt: true,
    ct: '150',
    gatewayId: 1,
    gatewaySn: 'GW-2025-001',
    portNo: '1',
    payType: 1,
    isCalculate: true,
    calculateType: 'peak-valley',
    calculateTypeName: '峰谷计量',
    spaceId: 'space-a-101',
    spaceName: '101',
    spacePath: 'A区 / 1号楼 / 101',
    onlineStatus: 1,
    onlineStatusName: '在线',
    offlineDuration: '',
    status: 0,
    statusName: '合闸',
    pricePlanName: '峰谷电价方案',
    warnPlanName: '默认预警',
    electricWarnTypeName: '一级预警',
    accountId: 5001
  },
  {
    id: 2,
    meterName: 'A1-102分表',
    deviceNo: 'EM-A1102-02',
    meterAddress: '12',
    modelId: 101,
    modelName: 'DDSY1352-NB',
    communicateModel: 'NB-IoT',
    isCt: false,
    imei: '867530900000001',
    payType: 1,
    isCalculate: true,
    calculateType: 'step',
    calculateTypeName: '阶梯计量',
    spaceId: 'space-a-101',
    spaceName: '102',
    spacePath: 'A区 / 1号楼 / 102',
    onlineStatus: 0,
    onlineStatusName: '离线',
    offlineDuration: '3小时18分',
    status: 1,
    statusName: '断闸',
    pricePlanName: '阶梯电价方案',
    warnPlanName: '宿舍预警',
    electricWarnTypeName: '二级预警',
    accountId: null
  },
  {
    id: 3,
    meterName: 'A2-201总表',
    deviceNo: 'EM-A2201-01',
    meterAddress: '21',
    modelId: 103,
    modelName: 'DDS238-4',
    communicateModel: 'RS485',
    isCt: false,
    gatewayId: 1,
    gatewaySn: 'GW-2025-001',
    portNo: '2',
    payType: 0,
    isCalculate: true,
    calculateType: 'step',
    calculateTypeName: '阶梯计量',
    spaceId: 'space-a-201',
    spaceName: '201',
    spacePath: 'A区 / 2号楼 / 201',
    onlineStatus: 1,
    onlineStatusName: '在线',
    offlineDuration: '',
    status: 0,
    statusName: '合闸',
    pricePlanName: '标准电价方案',
    warnPlanName: '默认预警',
    electricWarnTypeName: '一级预警',
    accountId: null
  },
  {
    id: 4,
    meterName: 'A2-202分表',
    deviceNo: 'EM-A2202-02',
    meterAddress: '22',
    modelId: 104,
    modelName: 'DTS541-4G',
    communicateModel: '4G',
    isCt: true,
    ct: '200',
    imei: '867530900000104',
    payType: 1,
    isCalculate: false,
    calculateType: 'demand',
    calculateTypeName: '需量计量',
    spaceId: 'space-a-201',
    spaceName: '202',
    spacePath: 'A区 / 2号楼 / 202',
    onlineStatus: 1,
    onlineStatusName: '在线',
    offlineDuration: '',
    status: 1,
    statusName: '断闸',
    pricePlanName: '4G专用方案',
    warnPlanName: '园区预警',
    electricWarnTypeName: '三级预警',
    accountId: null
  },
  {
    id: 5,
    meterName: 'B3-301总表',
    deviceNo: 'EM-B3301-01',
    meterAddress: '31',
    modelId: 102,
    modelName: 'DTSD1888',
    communicateModel: 'RS485',
    isCt: true,
    ct: '300',
    gatewayId: 2,
    gatewaySn: 'GW-2025-002',
    portNo: '5',
    payType: 1,
    isCalculate: true,
    calculateType: 'peak-valley',
    calculateTypeName: '峰谷计量',
    spaceId: 'space-b-301',
    spaceName: '301',
    spacePath: 'B区 / 3号楼 / 301',
    onlineStatus: 1,
    onlineStatusName: '在线',
    offlineDuration: '',
    status: 0,
    statusName: '合闸',
    pricePlanName: '峰谷电价方案',
    warnPlanName: '默认预警',
    electricWarnTypeName: '一级预警',
    accountId: 5003
  },
  {
    id: 6,
    meterName: 'B5-502分表',
    deviceNo: 'EM-B5502-02',
    meterAddress: '52',
    modelId: 101,
    modelName: 'DDSY1352-NB',
    communicateModel: 'NB-IoT',
    isCt: false,
    imei: '867530900000106',
    payType: 1,
    isCalculate: true,
    calculateType: 'step',
    calculateTypeName: '阶梯计量',
    spaceId: 'space-b-502',
    spaceName: '502',
    spacePath: 'B区 / 5号楼 / 502',
    onlineStatus: 0,
    onlineStatusName: '离线',
    offlineDuration: '1天02小时',
    status: 0,
    statusName: '合闸',
    pricePlanName: '阶梯电价方案',
    warnPlanName: '宿舍预警',
    electricWarnTypeName: '二级预警',
    accountId: null
  },
  {
    id: 7,
    meterName: '仓储总配电表',
    deviceNo: 'EM-CWHS-01',
    meterAddress: '61',
    modelId: 102,
    modelName: 'DTSD1888',
    communicateModel: 'RS485',
    isCt: true,
    ct: '500',
    gatewayId: 3,
    gatewaySn: 'GW-2025-003',
    portNo: '8',
    payType: 0,
    isCalculate: true,
    calculateType: 'demand',
    calculateTypeName: '需量计量',
    spaceId: 'space-c-warehouse',
    spaceName: '总配电房',
    spacePath: 'C区 / 仓储区 / 总配电房',
    onlineStatus: 1,
    onlineStatusName: '在线',
    offlineDuration: '',
    status: 0,
    statusName: '合闸',
    pricePlanName: '需量电价方案',
    warnPlanName: '仓储预警',
    electricWarnTypeName: '一级预警',
    accountId: null
  }
]

export const createDefaultMeterForm = (): ElectricMeterFormValue => ({
  meterName: '',
  deviceNo: '',
  meterAddress: '',
  modelId: '',
  ct: '',
  spaceId: '',
  gatewayId: '',
  portNo: '',
  imei: '',
  payType: '0',
  isCalculate: 'true'
})

export const findModelOption = (
  modelId: string | number | undefined
): ElectricMeterModelOption | undefined => {
  return modelOptions.find((item) => String(item.id) === String(modelId))
}

export const findGatewayOption = (
  gatewayId: string | number | undefined
): ElectricMeterGatewayOption | undefined => {
  return gatewayOptions.find((item) => String(item.id) === String(gatewayId))
}

export const findOptionLabel = (
  options: ElectricMeterOption[],
  value: string | undefined
): string => {
  if (!value) {
    return '—'
  }
  return options.find((item) => item.value === value)?.label || value
}

export const findSpaceLabel = (spaceId: string | undefined): string => {
  if (!spaceId) {
    return '—'
  }
  return spaceOptions.find((item) => item.value === spaceId)?.label || spaceId
}
