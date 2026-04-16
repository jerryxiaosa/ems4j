import { flushPromises, mount } from '@vue/test-utils'
import MenuManagementView from '@/views/system/MenuManagementView.vue'
import { fetchMenuDetail, fetchMenuTree } from '@/api/adapters/menu-manage'

vi.mock('@/api/adapters/menu-manage', () => ({
  fetchMenuTree: vi.fn(),
  fetchMenuDetail: vi.fn(),
  createMenu: vi.fn(),
  updateMenu: vi.fn(),
  removeMenu: vi.fn()
}))

const mockedFetchMenuTree = vi.mocked(fetchMenuTree)
const mockedFetchMenuDetail = vi.mocked(fetchMenuDetail)

const mountComponent = () => {
  return mount(MenuManagementView, {
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
        MenuFormModal: {
          props: ['modelValue', 'mode', 'parentMenu', 'menu', 'fixedPlatformValue'],
          template:
            '<div data-test="menu-form-modal-stub" :data-visible="String(modelValue)" :data-mode="mode" :data-menu-name="menu?.name || \'\'" :data-parent-name="parentMenu?.name || \'\'" :data-platform="fixedPlatformValue" />'
        },
        Transition: {
          template: '<div><slot /></div>'
        }
      }
    }
  })
}

describe('MenuManagementView', () => {
  beforeEach(() => {
    mockedFetchMenuTree.mockReset()
    mockedFetchMenuDetail.mockReset()
  })

  test('testLoadMenus_OnMounted_ShouldFetchTreeAndRenderExpandedRows', async () => {
    mockedFetchMenuTree.mockResolvedValue([
      {
        id: 1,
        parentId: null,
        name: '系统管理',
        key: 'system',
        routePath: '/system',
        backendApi: '',
        permissionCodes: [],
        categoryValue: '1',
        categoryName: '菜单',
        platformValue: '1',
        platformName: '后台',
        hiddenValue: 'false',
        hiddenName: '否',
        icon: 'setting',
        sortNum: 1,
        remark: '系统菜单',
        children: [
          {
            id: 2,
            parentId: 1,
            name: '菜单管理',
            key: 'menu',
            routePath: '/system/menu',
            backendApi: '',
            permissionCodes: [],
            categoryValue: '1',
            categoryName: '菜单',
            platformValue: '1',
            platformName: '后台',
            hiddenValue: 'false',
            hiddenName: '否',
            icon: 'menu',
            sortNum: 1,
            remark: '菜单配置',
            children: []
          }
        ]
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchMenuTree).toHaveBeenCalledTimes(1)
    expect(mockedFetchMenuTree).toHaveBeenCalledWith(1)
    expect(wrapper.text()).toContain('系统管理')
    expect(wrapper.text()).toContain('菜单管理')
    expect(wrapper.text()).toContain('系统菜单')
  })

  test('testOpenEditModal_WhenClickEdit_ShouldFetchDetailAndOpenFormModal', async () => {
    mockedFetchMenuTree.mockResolvedValue([
      {
        id: 1,
        parentId: null,
        name: '系统管理',
        key: 'system',
        routePath: '/system',
        backendApi: '',
        permissionCodes: [],
        categoryValue: '1',
        categoryName: '菜单',
        platformValue: '1',
        platformName: '后台',
        hiddenValue: 'false',
        hiddenName: '否',
        icon: 'setting',
        sortNum: 1,
        remark: '系统菜单',
        children: []
      }
    ])
    mockedFetchMenuDetail.mockResolvedValue({
      id: 1,
      parentId: null,
      name: '系统管理',
      key: 'system',
      routePath: '/system',
      backendApi: '',
      permissionCodes: ['system:menu:page'],
      categoryValue: '1',
      categoryName: '菜单',
      platformValue: '1',
      platformName: '后台',
      hiddenValue: 'false',
      hiddenName: '否',
      icon: 'setting',
      sortNum: 1,
      remark: '系统菜单',
      children: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.findAll('.btn-link')[1]!.trigger('click')
    await flushPromises()

    expect(mockedFetchMenuDetail).toHaveBeenCalledWith(1)
    const modal = wrapper.get('[data-test="menu-form-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-mode')).toBe('edit')
    expect(modal.attributes('data-menu-name')).toBe('系统管理')
    expect(modal.attributes('data-platform')).toBe('1')
  })
})
