import { mount } from '@vue/test-utils'
import ElectricRechargeConfirmModal from '@/components/trades/ElectricRechargeConfirmModal.vue'

describe('ElectricRechargeConfirmModal', () => {
  test('testRenderPaymentBreakdown_WhenConfirmingMeterRecharge_ShouldUseUnifiedAmountLabels', () => {
    const wrapper = mount(ElectricRechargeConfirmModal, {
      props: {
        modelValue: true,
        meterName: '1号表',
        meterDeviceNo: 'EM-701',
        payAmountText: '200.00',
        topUpAmountText: '180.00',
        serviceFeeAmountText: '20.00'
      }
    })

    expect(wrapper.text()).toContain('请确认充值的信息')
    expect(wrapper.text()).toContain('支付金额（元）')
    expect(wrapper.text()).toContain('到账金额（元）')
    expect(wrapper.text()).toContain('服务费（元）')
    expect(wrapper.text()).toContain('200.00')
    expect(wrapper.text()).toContain('180.00')
    expect(wrapper.text()).toContain('20.00')
  })
})
