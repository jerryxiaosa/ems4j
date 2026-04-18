import { flushPromises, mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import DeviceElectricMeterEditModal from '@/components/devices/DeviceElectricMeterEditModal.vue'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import { fetchDeviceModelList, fetchGatewayList } from '@/api/adapters/device'
import { fetchSpaceTree } from '@/api/adapters/space'

vi.mock('@/api/adapters/device', async () => {
  const actual = await vi.importActual('@/api/adapters/device')
  return {
    ...actual,
    fetchDeviceModelList: vi.fn(),
    fetchGatewayList: vi.fn()
  }
})

vi.mock('@/api/adapters/space', async () => {
  const actual = await vi.importActual('@/api/adapters/space')
  return {
    ...actual,
    fetchSpaceTree: vi.fn()
  }
})

const mockedFetchDeviceModelList = vi.mocked(fetchDeviceModelList)
const mockedFetchGatewayList = vi.mocked(fetchGatewayList)
const mockedFetchSpaceTree = vi.mocked(fetchSpaceTree)

const createMeter = (overrides: Partial<ElectricMeterItem> = {}): ElectricMeterItem => ({
  id: 1,
  meterName: 'A1-101总表',
  deviceNo: 'GW-2025-001:1:11',
  meterAddress: '11',
  modelId: 102,
  modelName: 'DTSD1888',
  communicateModel: 'RS485',
  isCt: true,
  ct: '150',
  gatewayId: 1,
  gatewayName: '1号网关',
  gatewayDeviceNo: 'GW-2025-001',
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

const mountComponent = (props?: {
  modelValue?: boolean
  mode?: 'create' | 'edit'
  meter?: ElectricMeterItem | null
}) =>
  mount(DeviceElectricMeterEditModal, {
    props: {
      modelValue: props?.modelValue ?? true,
      mode: props?.mode || 'create',
      meter: props?.meter || null
    },
    global: {
      stubs: {
        UiEmptyState: { template: '<div data-test="empty">empty</div>' },
        UiErrorState: { props: ['text'], template: '<div data-test="error">{{ text }}</div>' },
        UiLoadingState: { template: '<div data-test="loading">loading</div>' },
        SpaceTreeSelectNode: { template: '<li data-test="space-node"></li>' }
      }
    }
  })

describe('DeviceElectricMeterEditModal', () => {
  beforeEach(() => {
    mockedFetchDeviceModelList.mockReset()
    mockedFetchGatewayList.mockReset()
    mockedFetchSpaceTree.mockReset()
    mockedFetchDeviceModelList.mockResolvedValue([
      {
        id: 102,
        modelName: 'DTSD1888',
        communicateModel: 'RS485',
        isNb: false,
        isCt: true,
        isPrepay: true
      },
      {
        id: 101,
        modelName: 'DDSY1352-NB',
        communicateModel: 'NB-IoT',
        isNb: true,
        isCt: false,
        isPrepay: true
      }
    ] as never)
    mockedFetchGatewayList.mockResolvedValue([
      {
        id: 1,
        gatewayName: '1号接入网关',
        deviceNo: 'GW-2025-001',
        sn: 'GW-2025-001'
      }
    ] as never)
    mockedFetchSpaceTree.mockResolvedValue([
      {
        id: 9,
        name: '101',
        pathLabel: 'A区 / 1号楼 / 101',
        children: []
      }
    ] as never)
  })

  test('testFieldOrder_WhenCreateMode_ShouldShowModelBeforeMeterName', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const html = wrapper.html()
    expect(html.indexOf('电表型号')).toBeGreaterThan(-1)
    expect(html.indexOf('电表名称')).toBeGreaterThan(-1)
    expect(html.indexOf('电表型号')).toBeLessThan(html.indexOf('电表名称'))
  })

  test('testGatewayMode_WhenEditMode_ShouldHideEditableDeviceNoAndShowGeneratedHint', async () => {
    const wrapper = mountComponent({
      modelValue: false,
      mode: 'edit',
      meter: createMeter()
    })
    await wrapper.setProps({ modelValue: true })
    await nextTick()
    await flushPromises()

    expect(wrapper.find('input[placeholder="请输入电表编号"]').exists()).toBe(false)
    expect(wrapper.text()).toContain('电表编号将自动生成')
  })

  test('testFieldOrder_WhenEditMode_ShouldKeepModelBeforeMeterName', async () => {
    const wrapper = mountComponent({
      mode: 'edit',
      meter: createMeter()
    })
    await flushPromises()

    const html = wrapper.html()
    expect(html.indexOf('电表型号')).toBeLessThan(html.indexOf('电表名称'))
  })
})
