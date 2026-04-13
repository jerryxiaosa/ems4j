import { flushPromises, mount } from '@vue/test-utils'
import RolePermissionModal from '@/components/system/RolePermissionModal.vue'
import {
  fetchRoleDetail,
  fetchRolePermissionTree,
  saveRoleMenus
} from '@/api/adapters/role'
import type { SystemRoleItem, SystemRolePermissionNode } from '@/modules/system/roles/types'

vi.mock('@/api/adapters/role', () => ({
  fetchRoleDetail: vi.fn(),
  fetchRolePermissionTree: vi.fn(),
  saveRoleMenus: vi.fn()
}))

const mockedFetchRoleDetail = vi.mocked(fetchRoleDetail)
const mockedFetchRolePermissionTree = vi.mocked(fetchRolePermissionTree)
const mockedSaveRoleMenus = vi.mocked(saveRoleMenus)

const permissionTree: SystemRolePermissionNode[] = [
  {
    id: '1',
    label: '设备管理',
    children: [
      { id: '11', label: '电表管理', children: [] },
      { id: '12', label: '网关管理', children: [] }
    ]
  }
]

const createRole = (menuIds: string[]): SystemRoleItem => ({
  id: 8,
  name: '运营',
  key: 'ops',
  description: '',
  menuIds
})

const mountComponent = () =>
  mount(RolePermissionModal, {
    props: {
      modelValue: false,
      role: createRole([])
    },
    global: {
      stubs: {
        UiEmptyState: {
          props: ['text'],
          template: '<div>{{ text }}</div>'
        },
        UiErrorState: {
          props: ['text'],
          template: '<div>{{ text }}</div>'
        },
        UiLoadingState: {
          template: '<div>loading</div>'
        }
      }
    }
  })

describe('RolePermissionModal', () => {
  beforeEach(() => {
    mockedFetchRolePermissionTree.mockReset()
    mockedFetchRoleDetail.mockReset()
    mockedSaveRoleMenus.mockReset()
    mockedFetchRolePermissionTree.mockResolvedValue(permissionTree)
  })

  test('testHandleSubmit_WhenChildChecked_ShouldSubmitParentAndChildIds', async () => {
    mockedFetchRoleDetail.mockResolvedValue(createRole([]))
    mockedSaveRoleMenus.mockResolvedValue(undefined)

    const wrapper = mountComponent()
    await wrapper.setProps({ modelValue: true })
    await flushPromises()

    const checkboxList = wrapper.findAll('input[type="checkbox"]')
    await checkboxList[1].setValue(true)
    await flushPromises()

    await wrapper.get('.btn-primary').trigger('click')

    expect(mockedSaveRoleMenus).toHaveBeenCalledWith(8, ['1', '11'])
  })

  test('testApplyCheckedKeys_WhenParentAndPartialChildReturned_ShouldNotSelectSiblingNode', async () => {
    mockedFetchRoleDetail.mockResolvedValue(createRole(['1', '11']))

    const wrapper = mountComponent()
    await wrapper.setProps({ modelValue: true })
    await flushPromises()

    const checkboxList = wrapper.findAll('input[type="checkbox"]')
    expect((checkboxList[0].element as HTMLInputElement).checked).toBe(false)
    expect((checkboxList[0].element as HTMLInputElement).indeterminate).toBe(true)
    expect((checkboxList[1].element as HTMLInputElement).checked).toBe(true)
    expect((checkboxList[2].element as HTMLInputElement).checked).toBe(false)
  })
})
