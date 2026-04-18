import { flushPromises, mount } from '@vue/test-utils'
import AccountInfoView from '@/views/accounts/AccountInfoView.vue'
import { fetchAccountDetail, fetchAccountPage } from '@/api/adapters/account'
import { fetchEnumOptionsByKey } from '@/api/adapters/system'
import { fetchLatestPowerRecord } from '@/api/adapters/device'

vi.mock('@/api/adapters/account', () => ({
  fetchAccountPage: vi.fn(),
  fetchAccountDetail: vi.fn(),
  cancelAccount: vi.fn()
}))

vi.mock('@/api/adapters/system', () => ({
  fetchEnumOptionsByKey: vi.fn()
}))

vi.mock('@/api/adapters/device', () => ({
  fetchLatestPowerRecord: vi.fn()
}))

vi.mock('@/composables/usePermission', () => ({
  usePermission: () => ({
    hasMenuPermission: () => true
  })
}))

const mockedFetchAccountPage = vi.mocked(fetchAccountPage)
const mockedFetchAccountDetail = vi.mocked(fetchAccountDetail)
const mockedFetchEnumOptionsByKey = vi.mocked(fetchEnumOptionsByKey)
const mockedFetchLatestPowerRecord = vi.mocked(fetchLatestPowerRecord)

const mountComponent = () => {
  return mount(AccountInfoView, {
    global: {
      directives: {
        menuPermission: {
          mounted() {},
          updated() {}
        }
      },
      stubs: {
        CommonPagination: {
          template: '<div data-test="pagination-stub" />'
        },
        UiTableStateOverlay: {
          props: ['loading', 'empty'],
          template:
            '<div data-test="table-overlay-stub" :data-loading="String(loading)" :data-empty="String(empty)" />'
        },
        UiLoadingState: {
          template: '<div data-test="loading-state-stub" />'
        },
        UiEmptyState: {
          props: ['text'],
          template: '<div data-test="empty-state-stub">{{ text }}</div>'
        },
        OpenAccountModal: {
          template: '<div data-test="open-account-modal-stub" />'
        }
      }
    }
  })
}

describe('AccountInfoView', () => {
  beforeEach(() => {
    mockedFetchAccountPage.mockReset()
    mockedFetchAccountDetail.mockReset()
    mockedFetchEnumOptionsByKey.mockReset()
    mockedFetchLatestPowerRecord.mockReset()
    mockedFetchLatestPowerRecord.mockResolvedValue({})
    mockedFetchEnumOptionsByKey.mockImplementation(async (key: string) => {
      if (key === 'electricAccountType') {
        return [
          { label: '按需计费', value: '0' },
          { label: '包月计费', value: '1' }
        ]
      }
      if (key === 'cleanBalanceType') {
        return [{ label: '退费', value: '1' }]
      }
      return []
    })
  })

  test('testLoadAccounts_OnMounted_ShouldFetchPageAndRenderRows', async () => {
    mockedFetchAccountPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          id: 88,
          ownerType: 0,
          ownerTypeName: '企业',
          ownerId: 1001,
          ownerName: '测试企业A',
          contactName: '张三',
          contactPhone: '13800000000',
          electricAccountType: 0,
          electricAccountTypeName: '按需计费',
          electricPricePlanName: '标准电价',
          electricBalanceAmountText: '100.00',
          openedMeterCount: 1,
          totalOpenableMeterCount: 2,
          meterList: []
        }
      ]
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchAccountPage).toHaveBeenCalledWith(
      expect.objectContaining({
        ownerName: undefined,
        pageNum: 1,
        pageSize: 10
      })
    )
    expect(wrapper.text()).toContain('测试企业A')
    expect(wrapper.text()).toContain('按需计费')
    expect(wrapper.text()).toContain('标准电价')
  })

  test('testOpenDetail_WhenClickDetail_ShouldFetchAccountDetailAndRenderModal', async () => {
    mockedFetchAccountPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          id: 88,
          ownerType: 0,
          ownerTypeName: '企业',
          ownerId: 1001,
          ownerName: '测试企业A',
          contactName: '张三',
          contactPhone: '13800000000',
          electricAccountType: 0,
          electricAccountTypeName: '按需计费',
          electricPricePlanName: '标准电价',
          electricBalanceAmountText: '100.00',
          openedMeterCount: 1,
          totalOpenableMeterCount: 2,
          meterList: []
        }
      ]
    })
    mockedFetchAccountDetail.mockResolvedValue({
      id: 88,
      ownerType: 0,
      ownerTypeName: '企业',
      ownerId: 1001,
      ownerName: '测试企业A',
      contactName: '张三',
      contactPhone: '13800000000',
      electricAccountType: 0,
      electricAccountTypeName: '按需计费',
      electricPricePlanName: '标准电价',
      electricBalanceAmountText: '100.00',
      openedMeterCount: 1,
      totalOpenableMeterCount: 2,
      warnPlanName: '默认预警方案',
      meterList: [
        {
          id: 701,
          meterName: '1号表',
          deviceNo: 'EM-701',
          spaceName: '101',
          spaceParentNames: ['A区', '1号楼'],
          meterBalanceAmountText: '55.20',
          warnTypeName: '一级预警',
          isOnline: true,
          ct: 100
        }
      ]
    })

    const wrapper = mountComponent()
    await flushPromises()

    const detailButton = wrapper.findAll('.btn-link').find((button) => button.text() === '详情')
    expect(detailButton).toBeDefined()
    await detailButton!.trigger('click')
    await flushPromises()

    expect(mockedFetchAccountDetail).toHaveBeenCalledWith(88)
    expect(wrapper.text()).toContain('账户详情')
    expect(wrapper.text()).toContain('默认预警方案')
    expect(wrapper.text()).toContain('1号表')
  })
})
