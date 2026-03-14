import { reactive } from 'vue'
import { mount } from '@vue/test-utils'
import DeviceElectricMeterSearchPanel from '@/modules/devices/electric-meters/components/DeviceElectricMeterSearchPanel.vue'

describe('DeviceElectricMeterSearchPanel', () => {
  test('testInputAndSelect_WhenUpdated_ShouldSyncQueryForm', async () => {
    const queryForm = reactive({
      searchKey: '',
      onlineStatus: '',
      status: '',
      payType: '',
      pageNum: 1,
      pageSize: 10
    })

    const wrapper = mount(DeviceElectricMeterSearchPanel, {
      props: {
        queryForm
      }
    })

    await wrapper.find('input').setValue('EM-001')
    const selects = wrapper.findAll('select')
    await selects[0]!.setValue('true')
    await selects[1]!.setValue('false')
    await selects[2]!.setValue('true')

    expect(queryForm).toMatchObject({
      searchKey: 'EM-001',
      onlineStatus: 'true',
      status: 'false',
      payType: 'true'
    })
  })

  test('testButtons_WhenClicked_ShouldEmitSearchAndReset', async () => {
    const wrapper = mount(DeviceElectricMeterSearchPanel, {
      props: {
        queryForm: reactive({
          searchKey: '',
          onlineStatus: '',
          status: '',
          payType: '',
          pageNum: 1,
          pageSize: 10
        })
      }
    })

    const buttons = wrapper.findAll('button')
    await buttons[0]!.trigger('click')
    await buttons[1]!.trigger('click')

    expect(wrapper.emitted('search')).toHaveLength(1)
    expect(wrapper.emitted('reset')).toHaveLength(1)
  })
})
