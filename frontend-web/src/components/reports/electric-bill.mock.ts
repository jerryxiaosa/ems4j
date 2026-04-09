import type {
  ElectricBillReportAccountInfo,
  ElectricBillReportDetailResult,
  ElectricBillReportMeterDetail,
  ElectricBillReportPageItem,
  ElectricBillReportPageResult,
  ElectricBillReportQuery
} from '@/types/report'

const MOCK_DELAY_MS = 180

const QUANTITY_ELECTRIC_ACCOUNT_TYPE = 0
const MONTHLY_ELECTRIC_ACCOUNT_TYPE = 1
const MERGED_ELECTRIC_ACCOUNT_TYPE = 2

interface ElectricBillMockRecord {
  accountInfo: Omit<ElectricBillReportAccountInfo, 'dateRangeText'>
  totalDebitAmountText: string
  meterList: ElectricBillReportMeterDetail[]
}

const mockRecordList: ElectricBillMockRecord[] = [
  {
    accountInfo: {
      accountId: 101,
      accountName: '安泰精密制造（无锡）有限公司',
      contactName: '王淑敏',
      contactPhone: '13850001234',
      electricAccountType: QUANTITY_ELECTRIC_ACCOUNT_TYPE,
      electricAccountTypeName: '按需计费',
      monthlyPayAmountText: '—',
      accountBalanceText: '18,420.75',
      meterCount: 3,
      periodConsumePowerText: '29,406.97',
      periodElectricChargeAmountText: '23,342.10',
      periodRechargeAmountText: '12,000.00',
      periodCorrectionAmountText: '-120.00'
    },
    totalDebitAmountText: '23,266.10',
    meterList: [
      {
        meterId: 1001,
        deviceNo: 'EM202506030151',
        meterName: '冲压线 1# 表',
        consumePowerHigherText: '0',
        consumePowerHighText: '12,047.27',
        consumePowerLowText: '7,772.09',
        consumePowerLowerText: '3,522.72',
        consumePowerDeepLowText: '0',
        displayPriceHigherText: '1.0200',
        displayPriceHighText: '0.6680',
        displayPriceLowText: '0.5260',
        displayPriceLowerText: '0.3120',
        displayPriceDeepLowText: '0.2150',
        electricChargeAmountHigherText: '0.00',
        electricChargeAmountHighText: '8,045.58',
        electricChargeAmountLowText: '4,087.12',
        electricChargeAmountLowerText: '1,099.08',
        electricChargeAmountDeepLowText: '0.00',
        totalConsumePowerText: '23,342.08',
        totalElectricChargeAmountText: '13,231.78',
        totalRechargeAmountText: '5,000.00',
        totalCorrectionAmountText: '-80.00'
      },
      {
        meterId: 1002,
        deviceNo: 'EM202506030152',
        meterName: '冲压线 2# 表',
        consumePowerHigherText: '0',
        consumePowerHighText: '3,120.58',
        consumePowerLowText: '2,018.47',
        consumePowerLowerText: '421.30',
        consumePowerDeepLowText: '0',
        displayPriceHigherText: '1.0200',
        displayPriceHighText: '0.6680',
        displayPriceLowText: '0.5260',
        displayPriceLowerText: '0.3120',
        displayPriceDeepLowText: '0.2150',
        electricChargeAmountHigherText: '0.00',
        electricChargeAmountHighText: '2,084.55',
        electricChargeAmountLowText: '1,061.72',
        electricChargeAmountLowerText: '131.44',
        electricChargeAmountDeepLowText: '0.00',
        totalConsumePowerText: '5,560.35',
        totalElectricChargeAmountText: '3,277.71',
        totalRechargeAmountText: '4,000.00',
        totalCorrectionAmountText: '0.00'
      },
      {
        meterId: 1003,
        deviceNo: 'EM202506030153',
        meterName: '空压站表',
        consumePowerHigherText: '0',
        consumePowerHighText: '286.64',
        consumePowerLowText: '133.28',
        consumePowerLowerText: '84.62',
        consumePowerDeepLowText: '0',
        displayPriceHigherText: '1.0200',
        displayPriceHighText: '0.6680',
        displayPriceLowText: '0.5260',
        displayPriceLowerText: '0.3120',
        displayPriceDeepLowText: '0.2150',
        electricChargeAmountHigherText: '0.00',
        electricChargeAmountHighText: '191.48',
        electricChargeAmountLowText: '70.43',
        electricChargeAmountLowerText: '26.70',
        electricChargeAmountDeepLowText: '0.00',
        totalConsumePowerText: '504.54',
        totalElectricChargeAmountText: '288.61',
        totalRechargeAmountText: '3,000.00',
        totalCorrectionAmountText: '-40.00'
      }
    ]
  },
  {
    accountInfo: {
      accountId: 102,
      accountName: '美特富精密拉深技术（无锡）有限公司',
      contactName: '邢中奇',
      contactPhone: '13585000114',
      electricAccountType: MERGED_ELECTRIC_ACCOUNT_TYPE,
      electricAccountTypeName: '合并按需计费',
      monthlyPayAmountText: '—',
      accountBalanceText: '-147,908.41',
      meterCount: 2,
      periodConsumePowerText: '14,023.60',
      periodElectricChargeAmountText: '12,454.25',
      periodRechargeAmountText: '0.00',
      periodCorrectionAmountText: '180.00'
    },
    totalDebitAmountText: '12,694.25',
    meterList: [
      {
        meterId: 2001,
        deviceNo: 'EM202506030151',
        meterName: '美特富 1',
        consumePowerHigherText: '0',
        consumePowerHighText: '9,274.00',
        consumePowerLowText: '0',
        consumePowerLowerText: '0',
        consumePowerDeepLowText: '0',
        displayPriceHigherText: null,
        displayPriceHighText: null,
        displayPriceLowText: null,
        displayPriceLowerText: null,
        displayPriceDeepLowText: null,
        electricChargeAmountHigherText: null,
        electricChargeAmountHighText: null,
        electricChargeAmountLowText: null,
        electricChargeAmountLowerText: null,
        electricChargeAmountDeepLowText: null,
        totalConsumePowerText: '9,274.00',
        totalElectricChargeAmountText: null,
        totalRechargeAmountText: null,
        totalCorrectionAmountText: null
      },
      {
        meterId: 2002,
        deviceNo: 'EM202506030152',
        meterName: '美特富 2',
        consumePowerHigherText: '0',
        consumePowerHighText: '4,749.60',
        consumePowerLowText: '0',
        consumePowerLowerText: '0',
        consumePowerDeepLowText: '0',
        displayPriceHigherText: null,
        displayPriceHighText: null,
        displayPriceLowText: null,
        displayPriceLowerText: null,
        displayPriceDeepLowText: null,
        electricChargeAmountHigherText: null,
        electricChargeAmountHighText: null,
        electricChargeAmountLowText: null,
        electricChargeAmountLowerText: null,
        electricChargeAmountDeepLowText: null,
        totalConsumePowerText: '4,749.60',
        totalElectricChargeAmountText: null,
        totalRechargeAmountText: null,
        totalCorrectionAmountText: null
      }
    ]
  },
  {
    accountInfo: {
      accountId: 103,
      accountName: '启星自动化租赁服务有限公司',
      contactName: '周雪',
      contactPhone: '13900112233',
      electricAccountType: MONTHLY_ELECTRIC_ACCOUNT_TYPE,
      electricAccountTypeName: '包月计费',
      monthlyPayAmountText: '1,800.00',
      accountBalanceText: '2,860.00',
      meterCount: 2,
      periodConsumePowerText: '8,462.10',
      periodElectricChargeAmountText: '3,600.00',
      periodRechargeAmountText: '0.00',
      periodCorrectionAmountText: '0.00'
    },
    totalDebitAmountText: '3,600.00',
    meterList: [
      {
        meterId: 3001,
        deviceNo: 'EM202506060101',
        meterName: '租赁车间 1# 表',
        consumePowerHigherText: '0',
        consumePowerHighText: '4,580.00',
        consumePowerLowText: '1,220.00',
        consumePowerLowerText: '320.00',
        consumePowerDeepLowText: '0',
        displayPriceHigherText: null,
        displayPriceHighText: null,
        displayPriceLowText: null,
        displayPriceLowerText: null,
        displayPriceDeepLowText: null,
        electricChargeAmountHigherText: null,
        electricChargeAmountHighText: null,
        electricChargeAmountLowText: null,
        electricChargeAmountLowerText: null,
        electricChargeAmountDeepLowText: null,
        totalConsumePowerText: '6,120.00',
        totalElectricChargeAmountText: null,
        totalRechargeAmountText: null,
        totalCorrectionAmountText: null
      },
      {
        meterId: 3002,
        deviceNo: 'EM202506060102',
        meterName: '租赁车间 2# 表',
        consumePowerHigherText: '0',
        consumePowerHighText: '1,660.00',
        consumePowerLowText: '540.00',
        consumePowerLowerText: '142.10',
        consumePowerDeepLowText: '0',
        displayPriceHigherText: null,
        displayPriceHighText: null,
        displayPriceLowText: null,
        displayPriceLowerText: null,
        displayPriceDeepLowText: null,
        electricChargeAmountHigherText: null,
        electricChargeAmountHighText: null,
        electricChargeAmountLowText: null,
        electricChargeAmountLowerText: null,
        electricChargeAmountDeepLowText: null,
        totalConsumePowerText: '2,342.10',
        totalElectricChargeAmountText: null,
        totalRechargeAmountText: null,
        totalCorrectionAmountText: null
      }
    ]
  }
]

const delay = async () => {
  await new Promise((resolve) => {
    window.setTimeout(resolve, MOCK_DELAY_MS)
  })
}

const buildDateRangeText = (startDate: string, endDate: string) => {
  return `${startDate} ~ ${endDate}`
}

const toPageItem = (record: ElectricBillMockRecord): ElectricBillReportPageItem => {
  const accountInfo = record.accountInfo
  return {
    accountId: accountInfo.accountId,
    accountName: accountInfo.accountName,
    electricAccountType: accountInfo.electricAccountType,
    electricAccountTypeName: accountInfo.electricAccountTypeName,
    meterCount: accountInfo.meterCount,
    periodConsumePowerText: accountInfo.periodConsumePowerText,
    periodElectricChargeAmountText: accountInfo.periodElectricChargeAmountText,
    periodRechargeAmountText: accountInfo.periodRechargeAmountText,
    periodCorrectionAmountText: accountInfo.periodCorrectionAmountText,
    totalDebitAmountText: record.totalDebitAmountText
  }
}

export const fetchElectricBillReportPageMock = async (
  query: ElectricBillReportQuery
): Promise<ElectricBillReportPageResult> => {
  await delay()

  const keyword = (query.accountNameLike || '').trim()
  const filteredList = mockRecordList.filter((item) => {
    if (!keyword) {
      return true
    }
    return item.accountInfo.accountName.includes(keyword)
  })

  const pageNum = query.pageNum > 0 ? query.pageNum : 1
  const pageSize = query.pageSize > 0 ? query.pageSize : 10
  const startIndex = (pageNum - 1) * pageSize
  const endIndex = startIndex + pageSize

  return {
    list: filteredList.slice(startIndex, endIndex).map(toPageItem),
    total: filteredList.length,
    pageNum,
    pageSize
  }
}

export const fetchElectricBillReportDetailMock = async (
  accountId: number,
  startDate: string,
  endDate: string
): Promise<ElectricBillReportDetailResult> => {
  await delay()

  const matchedRecord = mockRecordList.find((item) => item.accountInfo.accountId === accountId)
  if (!matchedRecord) {
    throw new Error('未找到对应的电费报表详情数据')
  }

  return {
    accountInfo: {
      ...matchedRecord.accountInfo,
      dateRangeText: buildDateRangeText(startDate, endDate)
    },
    meterList: matchedRecord.meterList
  }
}
