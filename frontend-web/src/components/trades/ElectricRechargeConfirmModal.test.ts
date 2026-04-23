import { mount } from '@vue/test-utils'
import { describe, expect, test } from 'vitest'
import ElectricRechargeConfirmModal from './ElectricRechargeConfirmModal.vue'

describe('ElectricRechargeConfirmModal', () => {
  test('testRender_ShouldShowRechargeSummaryFields', () => {
    const wrapper = mount(ElectricRechargeConfirmModal, {
      props: {
        modelValue: true,
        meterName: '测试电表',
        meterDeviceNo: 'M-1001',
        rechargeAmountText: '200.00',
        saleAmountText: '180.00',
        serviceFeeAmountText: '20.00'
      }
    })

    expect(wrapper.text()).toContain('请确认充值的信息')
    expect(wrapper.text()).toContain('充值金额（元）')
    expect(wrapper.text()).toContain('售电金额（元）')
    expect(wrapper.text()).toContain('服务费金额（元）')
    expect(wrapper.text()).toContain('200.00')
    expect(wrapper.text()).toContain('180.00')
    expect(wrapper.text()).toContain('20.00')
  })
})
