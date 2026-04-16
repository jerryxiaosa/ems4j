import { flushPromises, mount } from '@vue/test-utils'
import SpaceManagementView from '@/views/system/SpaceManagementView.vue'
import { fetchSpaceDetail, fetchSpaceTree } from '@/api/adapters/space-manage'

vi.mock('@/api/adapters/space-manage', () => ({
  fetchSpaceTree: vi.fn(),
  fetchSpaceDetail: vi.fn(),
  createSpace: vi.fn(),
  updateSpace: vi.fn(),
  deleteSpace: vi.fn()
}))

const mockedFetchSpaceTree = vi.mocked(fetchSpaceTree)
const mockedFetchSpaceDetail = vi.mocked(fetchSpaceDetail)

const mountComponent = () => {
  return mount(SpaceManagementView, {
    global: {
      directives: {
        menuPermission: {
          mounted() {},
          updated() {}
        }
      },
      stubs: {
        UiTableStateOverlay: {
          props: ['loading', 'empty'],
          template:
            '<div data-test="table-overlay-stub" :data-loading="String(loading)" :data-empty="String(empty)" />'
        },
        SpaceFormModal: {
          props: ['modelValue', 'mode', 'parentSpace', 'space'],
          template:
            '<div data-test="space-form-modal-stub" :data-visible="String(modelValue)" :data-mode="mode" :data-space-name="space?.name || \'\'" :data-parent-name="parentSpace?.name || \'\'" />'
        },
        Transition: {
          template: '<div><slot /></div>'
        }
      }
    }
  })
}

describe('SpaceManagementView', () => {
  beforeEach(() => {
    mockedFetchSpaceTree.mockReset()
    mockedFetchSpaceDetail.mockReset()
  })

  test('testLoadSpaces_OnMounted_ShouldFetchTreeAndRenderExpandedRows', async () => {
    mockedFetchSpaceTree.mockResolvedValue([
      {
        id: 1,
        parentId: null,
        name: 'Base园区',
        typeValue: '1',
        typeName: '园区',
        area: 0,
        sortNum: 1,
        children: [
          {
            id: 2,
            parentId: 1,
            name: 'A1厂房',
            typeValue: '2',
            typeName: '厂房',
            area: 1200,
            sortNum: 1,
            children: []
          }
        ]
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchSpaceTree).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('Base园区')
    expect(wrapper.text()).toContain('A1厂房')
    expect(wrapper.text()).toContain('厂房')
    expect(wrapper.text()).toContain('1200')
  })

  test('testOpenEditModal_WhenClickEdit_ShouldFetchDetailAndOpenFormModal', async () => {
    mockedFetchSpaceTree.mockResolvedValue([
      {
        id: 1,
        parentId: null,
        name: 'Base园区',
        typeValue: '1',
        typeName: '园区',
        area: 0,
        sortNum: 1,
        children: []
      }
    ])
    mockedFetchSpaceDetail.mockResolvedValue({
      id: 1,
      parentId: null,
      name: 'Base园区',
      typeValue: '1',
      typeName: '园区',
      area: 0,
      sortNum: 1,
      children: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.findAll('.btn-link')[1]!.trigger('click')
    await flushPromises()

    expect(mockedFetchSpaceDetail).toHaveBeenCalledWith(1)
    const modal = wrapper.get('[data-test="space-form-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-mode')).toBe('edit')
    expect(modal.attributes('data-space-name')).toBe('Base园区')
  })
})
