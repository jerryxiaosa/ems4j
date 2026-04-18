import { flushPromises, mount } from '@vue/test-utils'
import RoleManagementView from '@/views/system/RoleManagementView.vue'
import { fetchRoleList } from '@/api/adapters/role'

vi.mock('@/api/adapters/role', () => ({
  fetchRoleList: vi.fn(),
  createRole: vi.fn(),
  updateRole: vi.fn(),
  deleteRole: vi.fn()
}))

const mockedFetchRoleList = vi.mocked(fetchRoleList)

const mountComponent = () => {
  return mount(RoleManagementView, {
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
        RoleFormModal: {
          props: ['modelValue', 'mode', 'role'],
          template:
            '<div data-test="role-form-modal-stub" :data-visible="String(modelValue)" :data-mode="mode" :data-role-name="role?.name || \'\'" />'
        },
        RolePermissionModal: {
          props: ['modelValue', 'role'],
          template:
            '<div data-test="role-permission-modal-stub" :data-visible="String(modelValue)" :data-role-name="role?.name || \'\'" />'
        },
        Transition: {
          template: '<div><slot /></div>'
        }
      }
    }
  })
}

describe('RoleManagementView', () => {
  beforeEach(() => {
    mockedFetchRoleList.mockReset()
  })

  test('testLoadRoles_OnMounted_ShouldFetchListAndRenderRows', async () => {
    mockedFetchRoleList.mockResolvedValue([
      {
        id: 1,
        name: '管理员',
        key: 'admin',
        description: '系统管理员',
        isSystem: true
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchRoleList).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('管理员')
    expect(wrapper.text()).toContain('系统管理员')
  })

  test('testOpenPermissionModal_WhenClickAssign_ShouldOpenPermissionModal', async () => {
    mockedFetchRoleList.mockResolvedValue([
      {
        id: 2,
        name: '财务角色',
        key: 'finance',
        description: '负责充值和账单',
        isSystem: false
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.btn-link').trigger('click')
    await flushPromises()

    const modal = wrapper.get('[data-test="role-permission-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-role-name')).toBe('财务角色')
  })
})
