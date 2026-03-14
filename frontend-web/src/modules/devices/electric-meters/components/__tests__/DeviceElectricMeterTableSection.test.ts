import { mount } from '@vue/test-utils'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import DeviceElectricMeterTableSection from '@/modules/devices/electric-meters/components/DeviceElectricMeterTableSection.vue'

vi.mock('@/composables/usePermission', () => ({
  usePermission: () => ({
    hasMenuPermission: () => true
  })
}))

const createRow = (overrides: Partial<ElectricMeterItem> = {}): ElectricMeterItem => ({
  id: 1,
  meterName: 'A1-101总表',
  deviceNo: 'EM-001',
  meterAddress: '11',
  modelId: 102,
  modelName: 'DTSD1888',
  communicateModel: 'RS485',
  isCt: true,
  ct: '150',
  gatewayId: 1,
  gatewayName: '1号网关',
  gatewayDeviceNo: 'GW-001',
  gatewaySn: 'GW-SN-001',
  portNo: '1',
  imei: '',
  payType: 1,
  isCalculate: true,
  calculateType: '2',
  calculateTypeName: '阶梯计量',
  spaceId: '9',
  spaceName: '101',
  spacePath: 'A区 / 1号楼 / 101',
  onlineStatus: 1,
  onlineStatusName: '在线',
  offlineDuration: '',
  status: 0,
  statusName: '合闸',
  protectedModel: false,
  pricePlanName: '居民电价',
  warnPlanName: '默认预警',
  electricWarnTypeName: '一级预警',
  accountId: 99,
  ...overrides
})

const mountComponent = () => {
  return mount(DeviceElectricMeterTableSection, {
    props: {
      loading: false,
      total: 1,
      queryForm: {
        searchKey: '',
        onlineStatus: '',
        status: '',
        payType: '',
        pageNum: 1,
        pageSize: 10
      },
      pagedRows: [createRow()],
      selectedIds: [1],
      isAllChecked: true,
      moreActionMenu: {
        row: createRow(),
        top: 100,
        left: 200
      }
    },
    global: {
      directives: {
        menuPermission: () => undefined
      },
      stubs: {
        CommonPagination: {
          template: '<button data-test="pager" @click="$emit(\'change\', { pageNum: 2, pageSize: 20 })">pager</button>'
        },
        UiEmptyState: {
          template: '<div data-test="empty" />'
        },
        UiLoadingState: {
          template: '<div data-test="loading" />'
        }
      }
    }
  })
}

describe('DeviceElectricMeterTableSection', () => {
  test('testToolbarAndRowButtons_WhenClicked_ShouldEmitExpectedEvents', async () => {
    const wrapper = mountComponent()
    const buttons = wrapper.findAll('button')

    await buttons[0]!.trigger('click')
    await buttons[1]!.trigger('click')
    await buttons[2]!.trigger('click')
    await buttons[3]!.trigger('click')
    await buttons[4]!.trigger('click')

    expect(wrapper.emitted('batchCommand')?.[0]).toEqual(['batch-merge'])
    expect(wrapper.emitted('batchCommand')?.[1]).toEqual(['batch-cut'])
    expect(wrapper.emitted('batchProtect')?.[0]).toEqual([true])
    expect(wrapper.emitted('batchProtect')?.[1]).toEqual([false])
    expect(wrapper.emitted('create')).toHaveLength(1)

    await wrapper.find('tbody input[type="checkbox"]').trigger('change')
    expect(wrapper.emitted('selectRow')).toBeTruthy()

    const linkButtons = wrapper.findAll('.btn-link')
    await linkButtons[0]!.trigger('click')
    await linkButtons[1]!.trigger('click')
    await linkButtons[2]!.trigger('click')

    expect(wrapper.emitted('detail')).toHaveLength(1)
    expect(wrapper.emitted('edit')).toHaveLength(1)
    expect(wrapper.emitted('delete')).toHaveLength(1)
  })

  test('testFloatingMenuAndPagination_WhenTriggered_ShouldEmitExpectedEvents', async () => {
    const wrapper = mountComponent()

    const moreButtons = wrapper.findAll('.more-action-item')
    await moreButtons[0]!.trigger('click')
    await moreButtons[1]!.trigger('click')
    await moreButtons[2]!.trigger('click')
    await wrapper.get('[data-test="pager"]').trigger('click')

    expect(wrapper.emitted('singleCommand')?.[0]?.[1]).toBe('cut')
    expect(wrapper.emitted('singleProtect')?.[0]?.[1]).toBe(true)
    expect(wrapper.emitted('setCt')).toHaveLength(1)
    expect(wrapper.emitted('pageChange')?.[0]).toEqual([{ pageNum: 2, pageSize: 20 }])
  })
})
