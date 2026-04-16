import { flushPromises, mount } from '@vue/test-utils'
import DeviceCategoryView from '@/views/devices/DeviceCategoryView.vue'
import { fetchDeviceModelPage, fetchDeviceTypeTree } from '@/api/adapters/device'

vi.mock('@/api/adapters/device', () => ({
  fetchDeviceTypeTree: vi.fn(),
  fetchDeviceModelPage: vi.fn()
}))

const mockedFetchDeviceTypeTree = vi.mocked(fetchDeviceTypeTree)
const mockedFetchDeviceModelPage = vi.mocked(fetchDeviceModelPage)

const mountComponent = () => {
  return mount(DeviceCategoryView, {
    global: {
      stubs: {
        DeviceCategoryTreeNode: {
          props: ['node'],
          emits: ['toggle', 'select'],
          template:
            '<button class="tree-node-stub" type="button" @click="$emit(\'select\', node.id)">{{ node.label }}</button>'
        },
        CommonPagination: {
          template: '<div data-test="pagination-stub" />'
        },
        UiTableStateOverlay: {
          props: ['loading', 'empty'],
          template:
            '<div data-test="table-overlay-stub" :data-loading="String(loading)" :data-empty="String(empty)" />'
        }
      }
    }
  })
}

describe('DeviceCategoryView', () => {
  beforeEach(() => {
    mockedFetchDeviceTypeTree.mockReset()
    mockedFetchDeviceModelPage.mockReset()
  })

  test('testLoadDeviceCategories_OnMounted_ShouldFetchTreeAndFirstLeafModels', async () => {
    mockedFetchDeviceTypeTree.mockResolvedValue([
      {
        id: 11,
        pid: 0,
        typeName: '单相电表',
        typeKey: 'single_meter',
        level: 2
      },
      {
        id: 12,
        pid: 0,
        typeName: '三相电表',
        typeKey: 'three_meter',
        level: 2
      }
    ])
    mockedFetchDeviceModelPage.mockResolvedValue({
      list: [
        {
          id: 101,
          manufacturerName: '威胜',
          modelName: 'DDSY-1'
        }
      ],
      total: 1,
      pageNum: 1,
      pageSize: 10
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchDeviceTypeTree).toHaveBeenCalledTimes(1)
    expect(mockedFetchDeviceModelPage).toHaveBeenCalledTimes(1)
    expect(mockedFetchDeviceModelPage).toHaveBeenCalledWith({
      typeIds: [11],
      manufacturerName: undefined,
      modelName: undefined,
      pageNum: 1,
      pageSize: 10
    })
    expect(wrapper.text()).toContain('当前分类：单相电表')
    expect(wrapper.text()).toContain('威胜')
    expect(wrapper.text()).toContain('DDSY-1')
  })

  test('testHandleTreeSelect_WhenClickAnotherLeaf_ShouldReloadTableWithNewType', async () => {
    mockedFetchDeviceTypeTree.mockResolvedValue([
      {
        id: 11,
        pid: 0,
        typeName: '单相电表',
        typeKey: 'single_meter',
        level: 2
      },
      {
        id: 12,
        pid: 0,
        typeName: '三相电表',
        typeKey: 'three_meter',
        level: 2
      }
    ])
    mockedFetchDeviceModelPage
      .mockResolvedValueOnce({
        list: [
          {
            id: 101,
            manufacturerName: '威胜',
            modelName: 'DDSY-1'
          }
        ],
        total: 1,
        pageNum: 1,
        pageSize: 10
      })
      .mockResolvedValueOnce({
        list: [
          {
            id: 102,
            manufacturerName: '林洋',
            modelName: 'DTSY-3'
          }
        ],
        total: 1,
        pageNum: 1,
        pageSize: 10
      })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.findAll('.tree-node-stub')[1]!.trigger('click')
    await flushPromises()

    expect(mockedFetchDeviceModelPage).toHaveBeenCalledTimes(2)
    expect(mockedFetchDeviceModelPage).toHaveBeenLastCalledWith({
      typeIds: [12],
      manufacturerName: undefined,
      modelName: undefined,
      pageNum: 1,
      pageSize: 10
    })
    expect(wrapper.text()).toContain('当前分类：三相电表')
    expect(wrapper.text()).toContain('林洋')
    expect(wrapper.text()).toContain('DTSY-3')
  })
})
