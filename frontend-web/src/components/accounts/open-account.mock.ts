export interface MockSelectOption {
  label: string
  value: string
}

export interface MockEnterpriseOption {
  id: number
  name: string
}

export interface MockEnterpriseDetail extends MockEnterpriseOption {
  contactName: string
  contactPhone: string
}

export interface MockOpenableMeterRow {
  id: number
  roomNo: string
  regionPath: string
  meterName: string
  deviceNo: string
  ct: string
  meterType: string
  meterTypeName: string
  enterpriseId: number
}

export interface MockOpenableMeterQuery {
  enterpriseId: number
  roomNo?: string
  meterType?: string
}

export interface MockOpenAccountSubmitPayload {
  ownerId: number
  ownerType: number
  ownerName: string
  electricAccountType: number
  monthlyPayAmount?: number
  electricPricePlanId?: number
  warnPlanId: number
  electricMeterList: Array<{ meterId: number }>
}

const delay = async (ms = 260) => {
  await new Promise((resolve) => setTimeout(resolve, ms))
}

const enterprises: MockEnterpriseDetail[] = [
  { id: 1001, name: '港湾科创园运营有限公司', contactName: '张敏', contactPhone: '13800001234' },
  { id: 1002, name: '蓝海制造（园区）有限公司', contactName: '李强', contactPhone: '13800004567' },
  { id: 1003, name: '智联低碳科技有限公司', contactName: '王宁', contactPhone: '13800007890' },
  { id: 1004, name: '晨光电子商务服务中心', contactName: '刘丹', contactPhone: '13800005678' },
  { id: 1005, name: '未来能源管理有限公司', contactName: '陈伟', contactPhone: '13800009876' }
]

const electricAccountTypeOptions: MockSelectOption[] = [
  { label: '按量计费', value: '1' },
  { label: '合并计费', value: '2' },
  { label: '包月计费', value: '3' }
]

const electricPricePlanOptions: MockSelectOption[] = [
  { label: '园区标准电价方案', value: '1' },
  { label: '峰谷电价方案', value: '2' },
  { label: '商业综合体电价方案', value: '3' }
]

const warnPlanOptions: MockSelectOption[] = [
  { label: '默认预警方案', value: '1' },
  { label: '宽松预警方案', value: '2' },
  { label: '严格预警方案', value: '3' }
]

const meterTypeOptions: MockSelectOption[] = [
  { label: '单相电表', value: 'single' },
  { label: '三相电表', value: 'three-phase' },
  { label: '多功能电表', value: 'multi' }
]

const allMeters: MockOpenableMeterRow[] = [
  {
    id: 50101,
    roomNo: 'A1-101',
    regionPath: '1号楼 > 1层',
    meterName: 'A1-101总表',
    deviceNo: 'EM-A1101-01',
    ct: '100/5',
    meterType: 'three-phase',
    meterTypeName: '三相电表',
    enterpriseId: 1001
  },
  {
    id: 50102,
    roomNo: 'A1-102',
    regionPath: '1号楼 > 1层',
    meterName: 'A1-102总表',
    deviceNo: 'EM-A1102-01',
    ct: '80/5',
    meterType: 'single',
    meterTypeName: '单相电表',
    enterpriseId: 1001
  },
  {
    id: 50103,
    roomNo: 'A2-305',
    regionPath: '2号楼 > 3层',
    meterName: 'A2-305分表',
    deviceNo: 'EM-A2305-02',
    ct: '60/5',
    meterType: 'multi',
    meterTypeName: '多功能电表',
    enterpriseId: 1001
  },
  {
    id: 50201,
    roomNo: 'B1-201',
    regionPath: '3号楼 > 2层',
    meterName: 'B1-201总表',
    deviceNo: 'EM-B1201-01',
    ct: '150/5',
    meterType: 'three-phase',
    meterTypeName: '三相电表',
    enterpriseId: 1002
  },
  {
    id: 50202,
    roomNo: 'B1-202',
    regionPath: '3号楼 > 2层',
    meterName: 'B1-202总表',
    deviceNo: 'EM-B1202-01',
    ct: '150/5',
    meterType: 'three-phase',
    meterTypeName: '三相电表',
    enterpriseId: 1002
  },
  {
    id: 50301,
    roomNo: 'C2-501',
    regionPath: '5号楼 > 5层',
    meterName: 'C2-501总表',
    deviceNo: 'EM-C2501-01',
    ct: '100/5',
    meterType: 'multi',
    meterTypeName: '多功能电表',
    enterpriseId: 1003
  },
  {
    id: 50302,
    roomNo: 'C2-502',
    regionPath: '5号楼 > 5层',
    meterName: 'C2-502分表',
    deviceNo: 'EM-C2502-02',
    ct: '40/5',
    meterType: 'single',
    meterTypeName: '单相电表',
    enterpriseId: 1003
  }
]

export const searchEnterprisesMock = async (keyword: string) => {
  await delay(220)
  const source = keyword.trim().toLowerCase()
  const filtered = source
    ? enterprises.filter((item) => item.name.toLowerCase().includes(source))
    : enterprises

  return filtered.slice(0, 10).map(({ id, name }) => ({ id, name }))
}

export const getEnterpriseDetailMock = async (enterpriseId: number) => {
  await delay(180)
  const detail = enterprises.find((item) => item.id === enterpriseId)
  if (!detail) {
    throw new Error('机构信息不存在')
  }
  return detail
}

export const getElectricAccountTypeOptionsMock = async () => {
  await delay(120)
  return electricAccountTypeOptions
}

export const getElectricPricePlanOptionsMock = async () => {
  await delay(160)
  return electricPricePlanOptions
}

export const getWarnPlanOptionsMock = async () => {
  await delay(160)
  return warnPlanOptions
}

export const getMeterTypeOptionsMock = async () => {
  await delay(120)
  return meterTypeOptions
}

export const getOpenableMetersMock = async (query: MockOpenableMeterQuery) => {
  await delay(260)
  const roomNoLike = query.roomNo?.trim().toLowerCase() || ''
  const meterType = query.meterType?.trim() || ''

  return allMeters.filter((item) => {
    if (item.enterpriseId !== query.enterpriseId) {
      return false
    }

    if (roomNoLike && !item.roomNo.toLowerCase().includes(roomNoLike)) {
      return false
    }

    if (meterType && item.meterType !== meterType) {
      return false
    }

    return true
  })
}

let mockAccountIdSeed = 9000

export const submitOpenAccountMock = async (_payload: MockOpenAccountSubmitPayload) => {
  await delay(420)
  mockAccountIdSeed += 1
  return {
    accountId: mockAccountIdSeed
  }
}
