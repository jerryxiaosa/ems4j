import { mount } from '@vue/test-utils'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import { electricMeterPermissionKeys } from '@/modules/devices/electric-meters/composables/electricMeterShared'
import DeviceElectricMeterTableSection from '@/modules/devices/electric-meters/components/DeviceElectricMeterTableSection.vue'

const allowedMenuKeys = new Set<string>(Object.values(electricMeterPermissionKeys))

vi.mock('@/composables/usePermission', () => ({
  usePermission: () => ({
    hasMenuPermission: (menuKey: string) => allowedMenuKeys.has(menuKey)
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

const mountComponent = (overrides: {
  row?: ElectricMeterItem
  moreActionMenu?: { row: ElectricMeterItem; top: number; left: number } | null
} = {}) => {
  const row = overrides.row ?? createRow()
  const moreActionMenu =
    overrides.moreActionMenu === undefined
      ? {
          row,
          top: 100,
          left: 200
        }
      : overrides.moreActionMenu

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
      pagedRows: [row],
      selectedIds: [1],
      isAllChecked: true,
      moreActionMenu
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
  beforeEach(() => {
    allowedMenuKeys.clear()
    Object.values(electricMeterPermissionKeys).forEach((menuKey) => {
      allowedMenuKeys.add(menuKey)
    })
  })

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

    const actionButtons = wrapper.findAll('.meter-row-actions > button')
    expect(actionButtons[0]!.attributes('aria-label')).toBe('用电趋势')

    await wrapper.get('button[aria-label="用电趋势"]').trigger('click')
    await wrapper.get('.meter-row-actions > button:nth-child(2)').trigger('click')
    await wrapper.get('.meter-row-actions > button:nth-child(3)').trigger('click')

    expect(wrapper.emitted('detail')).toHaveLength(1)
    expect(wrapper.emitted('edit')).toHaveLength(1)
    expect(wrapper.emitted('trend')).toHaveLength(1)
    expect(wrapper.text()).not.toContain('用电趋势')
    expect(wrapper.find('.btn-link-trend svg').exists()).toBe(true)
  })

  test('testFloatingMenuAndPagination_WhenTriggered_ShouldEmitExpectedEvents', async () => {
    const wrapper = mountComponent()

    const moreButtons = wrapper.findAll('.more-action-item')
    await moreButtons[0]!.trigger('click')
    await moreButtons[1]!.trigger('click')
    await moreButtons[2]!.trigger('click')
    await moreButtons[3]!.trigger('click')
    await wrapper.get('[data-test="pager"]').trigger('click')

    expect(wrapper.emitted('singleCommand')?.[0]?.[1]).toBe('cut')
    expect(wrapper.emitted('singleProtect')?.[0]?.[1]).toBe(true)
    expect(wrapper.emitted('setCt')).toHaveLength(1)
    expect(wrapper.emitted('delete')).toHaveLength(1)
    expect(wrapper.emitted('pageChange')?.[0]).toEqual([{ pageNum: 2, pageSize: 20 }])
  })

  test('testMoreActionButton_WhenOnlyDeletePermission_ShouldStillRender', () => {
    allowedMenuKeys.clear()
    allowedMenuKeys.add(electricMeterPermissionKeys.delete)

    const wrapper = mountComponent({
      row: createRow({
        onlineStatus: 0,
        onlineStatusName: '离线',
        protectedModel: false
      }),
      moreActionMenu: null
    })

    expect(wrapper.find('.btn-link-more').exists()).toBe(true)
  })

  test('testEmptyState_WhenNoRows_ShouldRenderCenteredOverlay', () => {
    const wrapper = mount(DeviceElectricMeterTableSection, {
      props: {
        loading: false,
        total: 0,
        queryForm: {
          searchKey: '',
          onlineStatus: '',
          status: '',
          payType: '',
          pageNum: 1,
          pageSize: 10
        },
        pagedRows: [],
        selectedIds: [],
        isAllChecked: false,
        moreActionMenu: null
      },
      global: {
        directives: {
          menuPermission: () => undefined
        },
        stubs: {
          CommonPagination: {
            template: '<div data-test="pager" />'
          },
          UiTableStateOverlay: {
            props: ['loading', 'empty'],
            template:
              '<div v-if="loading" data-test="loading-overlay" /><div v-else-if="empty" class="table-empty-overlay"><div data-test="empty" /></div>'
          }
        }
      }
    })

    expect(wrapper.find('.table-empty-overlay').exists()).toBe(true)
    expect(wrapper.find('[data-test="empty"]').exists()).toBe(true)
  })

  test('testLoadingState_WhenLoading_ShouldRenderCenteredOverlay', () => {
    const wrapper = mount(DeviceElectricMeterTableSection, {
      props: {
        loading: true,
        total: 0,
        queryForm: {
          searchKey: '',
          onlineStatus: '',
          status: '',
          payType: '',
          pageNum: 1,
          pageSize: 10
        },
        pagedRows: [],
        selectedIds: [],
        isAllChecked: false,
        moreActionMenu: null
      },
      global: {
        directives: {
          menuPermission: () => undefined
        },
        stubs: {
          CommonPagination: {
            template: '<div data-test="pager" />'
          },
          UiTableStateOverlay: {
            props: ['loading', 'empty'],
            template:
              '<div v-if="loading" data-test="loading-overlay" /><div v-else-if="empty" class="table-empty-overlay"><div data-test="empty" /></div>'
          }
        }
      }
    })

    expect(wrapper.find('[data-test="loading-overlay"]').exists()).toBe(true)
    expect(wrapper.find('.table-empty-overlay').exists()).toBe(false)
  })
})
