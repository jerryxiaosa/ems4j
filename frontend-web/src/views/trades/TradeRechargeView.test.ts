import { defineComponent } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import TradeRechargeView from '@/views/trades/TradeRechargeView.vue'
import { fetchAccountDetail } from '@/api/adapters/account'
import { fetchEnumOptionsByKey } from '@/api/adapters/system'
import {
  createEnergyTopUpOrder,
  fetchDefaultServiceRate,
  updateDefaultServiceRate
} from '@/api/adapters/trade'

const { pushMock, authStoreMock } = vi.hoisted(() => {
  return {
    pushMock: vi.fn(),
    authStoreMock: {
      user: {
        id: 9,
        userName: 'admin',
        realName: '管理员',
        userPhone: '13800000000'
      },
      loadCurrentUser: vi.fn()
    }
  }
})

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  })
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authStoreMock
}))

vi.mock('@/api/adapters/account', () => ({
  fetchAccountDetail: vi.fn(),
  searchAccountOptions: vi.fn()
}))

vi.mock('@/api/adapters/system', () => ({
  fetchEnumOptionsByKey: vi.fn()
}))

vi.mock('@/api/adapters/trade', () => ({
  createEnergyTopUpOrder: vi.fn(),
  fetchDefaultServiceRate: vi.fn(),
  updateDefaultServiceRate: vi.fn()
}))

const mockedFetchAccountDetail = vi.mocked(fetchAccountDetail)
const mockedFetchEnumOptionsByKey = vi.mocked(fetchEnumOptionsByKey)
const mockedCreateEnergyTopUpOrder = vi.mocked(createEnergyTopUpOrder)
const mockedFetchDefaultServiceRate = vi.mocked(fetchDefaultServiceRate)
const mockedUpdateDefaultServiceRate = vi.mocked(updateDefaultServiceRate)

const accountPickerOption = {
  id: 88,
  name: '测试企业A',
  managerName: '张三',
  managerPhone: '13800000000'
}

const OrganizationPickerStub = defineComponent({
  emits: ['update:modelValue', 'select'],
  template:
    '<button data-test="pick-account" type="button" @click="$emit(\'update:modelValue\', \'测试企业A\'); $emit(\'select\', { id: 88, name: \'测试企业A\', managerName: \'张三\', managerPhone: \'13800000000\' })">选择账户</button>'
})

const ElectricRechargeConfirmModalStub = defineComponent({
  props: ['modelValue', 'meterName', 'meterDeviceNo', 'amountText', 'submitting'],
  emits: ['update:modelValue', 'confirm'],
  template:
    '<div data-test="confirm-modal" :data-visible="String(modelValue)" :data-meter-name="String(meterName)" :data-device-no="String(meterDeviceNo)" :data-amount="String(amountText)"><button data-test="confirm-submit" type="button" @click="$emit(\'confirm\')">提交确认</button></div>'
})

const mountComponent = () => {
  return mount(TradeRechargeView, {
    global: {
      directives: {
        menuPermission: {
          mounted() {},
          updated() {}
        }
      },
      stubs: {
        OrganizationPicker: OrganizationPickerStub,
        ElectricRechargeConfirmModal: ElectricRechargeConfirmModalStub,
        ElectricRechargeMeterModal: {
          props: ['modelValue', 'meters', 'selectedMeterId'],
          template:
            '<div data-test="meter-modal-stub" :data-visible="String(modelValue)" :data-meter-count="String((meters || []).length)" />'
        },
        ElectricServiceRateModal: {
          props: ['modelValue', 'serviceRate'],
          template:
            '<div data-test="service-rate-modal-stub" :data-visible="String(modelValue)" :data-service-rate="String(serviceRate)" />'
        }
      }
    }
  })
}

const mockEnumOptions = () => {
  mockedFetchEnumOptionsByKey.mockImplementation(async (key: string) => {
    if (key === 'paymentChannel') {
      return [{ value: 'OFFLINE', label: '现金支付' }]
    }
    if (key === 'balanceType') {
      return [
        { value: '0', label: '账户余额' },
        { value: '1', label: '电表余额' }
      ]
    }
    if (key === 'meterType') {
      return [{ value: '2', label: '单相表' }]
    }
    return []
  })
}

describe('TradeRechargeView', () => {
  beforeEach(() => {
    vi.useRealTimers()
    pushMock.mockReset()
    authStoreMock.loadCurrentUser.mockReset()
    authStoreMock.user = {
      id: 9,
      userName: 'admin',
      realName: '管理员',
      userPhone: '13800000000'
    }
    mockedFetchAccountDetail.mockReset()
    mockedFetchEnumOptionsByKey.mockReset()
    mockedCreateEnergyTopUpOrder.mockReset()
    mockedFetchDefaultServiceRate.mockReset()
    mockedUpdateDefaultServiceRate.mockReset()
    mockedFetchDefaultServiceRate.mockResolvedValue(0.1)
    mockedUpdateDefaultServiceRate.mockResolvedValue()
    mockEnumOptions()
  })

  test('testSelectQuantityAccount_WhenConfirmRecharge_ShouldShowMeterSectionAndOpenConfirmModal', async () => {
    mockedFetchAccountDetail.mockResolvedValue({
      id: 88,
      ownerId: 1001,
      ownerType: 0,
      ownerTypeName: '企业',
      ownerName: accountPickerOption.name,
      contactName: '张三',
      contactPhone: '13800000000',
      electricAccountType: 0,
      electricAccountTypeName: '按需计费',
      electricBalanceAmountText: '300.00',
      meterList: [
        {
          id: 701,
          meterName: '1号表',
          deviceNo: 'EM-701',
          spaceId: 9001,
          spaceName: '101',
          spaceParentNames: ['A区', '1号楼'],
          meterType: 2,
          meterTypeName: '单相表',
          meterBalanceAmountText: '55.20'
        }
      ]
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('[data-test="pick-account"]').trigger('click')
    await flushPromises()

    expect(mockedFetchAccountDetail).toHaveBeenCalledWith(88)
    expect(wrapper.text()).toContain('按需计费')
    expect(wrapper.text()).toContain('1号表')
    expect(wrapper.text()).toContain('更换电表')

    await wrapper.get('input[placeholder="请输入付款金额"]').setValue('200')
    const actionButtons = wrapper.findAll('button')
    const confirmButton = actionButtons.find((button) => button.text() === '确认充值')
    expect(confirmButton).toBeDefined()
    await confirmButton!.trigger('click')
    await flushPromises()

    const confirmModal = wrapper.get('[data-test="confirm-modal"]')
    expect(confirmModal.attributes('data-visible')).toBe('true')
    expect(confirmModal.attributes('data-meter-name')).toBe('1号表')
    expect(confirmModal.attributes('data-amount')).toBe('200.00')
  })

  test('testSubmitMergedAccount_WhenConfirmRecharge_ShouldCreateOrderAndNavigateToOrderFlows', async () => {
    vi.useFakeTimers()
    mockedFetchAccountDetail.mockResolvedValue({
      id: 88,
      ownerId: 1001,
      ownerType: 0,
      ownerTypeName: '企业',
      ownerName: accountPickerOption.name,
      contactName: '张三',
      contactPhone: '13800000000',
      electricAccountType: 2,
      electricAccountTypeName: '合并计费',
      electricBalanceAmountText: '800.00',
      meterList: []
    })
    mockedCreateEnergyTopUpOrder.mockResolvedValue({
      orderSn: 'ORDER-001',
      paymentChannel: 'OFFLINE'
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('[data-test="pick-account"]').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('合并计费')
    expect(wrapper.text()).not.toContain('更换电表')

    await wrapper.get('input[placeholder="请输入付款金额"]').setValue('200')
    const actionButtons = wrapper.findAll('button')
    const confirmButton = actionButtons.find((button) => button.text() === '确认充值')
    expect(confirmButton).toBeDefined()
    await confirmButton!.trigger('click')
    await flushPromises()

    expect(mockedCreateEnergyTopUpOrder).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: 9,
        userPhone: '13800000000',
        userRealName: '管理员',
        orderAmount: 200,
        paymentChannel: 'OFFLINE',
        energyTopUp: expect.objectContaining({
          accountId: 88,
          ownerId: 1001,
          ownerType: 0,
          ownerName: accountPickerOption.name,
          electricAccountType: 2,
          balanceType: 0,
          serviceRate: 0.1
        })
      })
    )
    expect(mockedCreateEnergyTopUpOrder.mock.calls[0]?.[0].energyTopUp.meterId).toBeUndefined()

    await vi.advanceTimersByTimeAsync(2000)
    await flushPromises()

    expect(pushMock).toHaveBeenCalledWith('/trade/order-flows')
    expect(wrapper.get('[data-test="confirm-modal"]').attributes('data-visible')).toBe('false')
  })

  test('testHandleSubmit_WhenPayAmountMissing_ShouldShowErrorNoticeAndSkipCreateOrder', async () => {
    mockedFetchAccountDetail.mockResolvedValue({
      id: 88,
      ownerId: 1001,
      ownerType: 0,
      ownerTypeName: '企业',
      ownerName: accountPickerOption.name,
      contactName: '张三',
      contactPhone: '13800000000',
      electricAccountType: 2,
      electricAccountTypeName: '合并计费',
      electricBalanceAmountText: '800.00',
      meterList: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('[data-test="pick-account"]').trigger('click')
    await flushPromises()

    const confirmButton = wrapper.findAll('button').find((button) => button.text() === '确认充值')
    expect(confirmButton).toBeDefined()
    await confirmButton!.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('请输入付款金额')
    expect(mockedCreateEnergyTopUpOrder).not.toHaveBeenCalled()
    expect(pushMock).not.toHaveBeenCalled()
  })

  test('testSubmitMergedAccount_WhenCreateOrderFails_ShouldShowErrorNoticeAndNotNavigate', async () => {
    mockedFetchAccountDetail.mockResolvedValue({
      id: 88,
      ownerId: 1001,
      ownerType: 0,
      ownerTypeName: '企业',
      ownerName: accountPickerOption.name,
      contactName: '张三',
      contactPhone: '13800000000',
      electricAccountType: 2,
      electricAccountTypeName: '合并计费',
      electricBalanceAmountText: '800.00',
      meterList: []
    })
    mockedCreateEnergyTopUpOrder.mockRejectedValue(new Error('创建订单失败'))

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('[data-test="pick-account"]').trigger('click')
    await flushPromises()

    await wrapper.get('input[placeholder="请输入付款金额"]').setValue('200')
    const confirmButton = wrapper.findAll('button').find((button) => button.text() === '确认充值')
    expect(confirmButton).toBeDefined()
    await confirmButton!.trigger('click')
    await flushPromises()

    expect(mockedCreateEnergyTopUpOrder).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('创建订单失败')
    expect(pushMock).not.toHaveBeenCalled()
  })
})
