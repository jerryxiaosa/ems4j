import { mount } from '@vue/test-utils'
import { describe, expect, test } from 'vitest'
import ElectricServiceRateModal from './ElectricServiceRateModal.vue'

describe('ElectricServiceRateModal', () => {
  test('testHandleSubmit_WhenRateEqualsOneHundred_ShouldShowValidationError', async () => {
    const wrapper = mount(ElectricServiceRateModal, {
      props: {
        modelValue: true,
        serviceRate: '10'
      }
    })

    await wrapper.get('input').setValue('100')
    await wrapper.get('.btn-primary').trigger('click')

    expect(wrapper.text()).toContain('服务费比例需为大于等于 0 且小于 100 的数字')
    expect(wrapper.emitted('submit')).toBeFalsy()
  })

  test('testHandleSubmit_WhenRateHasMoreThanSixFractionDigits_ShouldShowValidationError', async () => {
    const wrapper = mount(ElectricServiceRateModal, {
      props: {
        modelValue: true,
        serviceRate: '10'
      }
    })

    await wrapper.get('input').setValue('12.1234567')
    await wrapper.get('.btn-primary').trigger('click')

    expect(wrapper.text()).toContain('服务费比例最多保留 6 位小数')
    expect(wrapper.emitted('submit')).toBeFalsy()
  })
})
