import { flushPromises, mount } from '@vue/test-utils'
import { reactive, ref } from 'vue'
import UserManagementView from '@/views/system/UserManagementView.vue'
import { useUserCrud } from '@/modules/system/users/composables/useUserCrud'
import { useUserNotice } from '@/modules/system/users/composables/useUserNotice'
import { useUserQuery } from '@/modules/system/users/composables/useUserQuery'

vi.mock('@/modules/system/users/composables/useUserNotice', () => ({
  useUserNotice: vi.fn()
}))

vi.mock('@/modules/system/users/composables/useUserQuery', () => ({
  useUserQuery: vi.fn()
}))

vi.mock('@/modules/system/users/composables/useUserCrud', () => ({
  useUserCrud: vi.fn()
}))

const mockedUseUserNotice = vi.mocked(useUserNotice)
const mockedUseUserQuery = vi.mocked(useUserQuery)
const mockedUseUserCrud = vi.mocked(useUserCrud)

const mountComponent = () => {
  return mount(UserManagementView, {
    global: {
      directives: {
        menuPermission: {
          mounted() {},
          updated() {}
        }
      },
      stubs: {
        CommonPagination: {
          template: '<div data-test="pagination-stub" />'
        },
        OrganizationPicker: {
          props: ['modelValue'],
          emits: ['update:modelValue', 'select'],
          template: '<div data-test="organization-picker-stub" />'
        },
        UiTableStateOverlay: {
          props: ['loading', 'empty'],
          template:
            '<div data-test="table-overlay-stub" :data-loading="String(loading)" :data-empty="String(empty)" />'
        },
        UserDetailModal: {
          template: '<div data-test="user-detail-modal-stub" />'
        },
        UserFormModal: {
          template: '<div data-test="user-form-modal-stub" />'
        },
        UserPasswordModal: {
          template: '<div data-test="user-password-modal-stub" />'
        },
        Transition: {
          template: '<div><slot /></div>'
        }
      }
    }
  })
}

describe('UserManagementView', () => {
  beforeEach(() => {
    mockedUseUserNotice.mockReset()
    mockedUseUserQuery.mockReset()
    mockedUseUserCrud.mockReset()

    mockedUseUserNotice.mockReturnValue({
      notice: reactive({ text: '', type: 'info' as const }),
      noticeFading: ref(false),
      setNotice: vi.fn(),
      dispose: vi.fn()
    })

    mockedUseUserQuery.mockReturnValue({
      userRows: ref([
        {
          id: 1,
          username: 'admin',
          realName: '系统管理员',
          phone: '13800000000',
          organizationId: '1',
          organizationName: '总部',
          roleId: '1',
          roleIds: ['1'],
          roleName: '超级管理员',
          createTime: '2026-01-01 08:00:00',
          updateTime: '2026-01-01 08:00:00',
          remark: ''
        },
        {
          id: 2,
          username: 'tester',
          realName: '测试用户',
          phone: '13900000000',
          organizationId: '1',
          organizationName: '总部',
          roleId: '2',
          roleIds: ['2'],
          roleName: '普通用户',
          createTime: '2026-01-02 08:00:00',
          updateTime: '2026-01-02 08:00:00',
          remark: ''
        }
      ]),
      total: ref(2),
      loading: ref(false),
      organizationOptions: ref([]),
      roleOptions: ref([]),
      queryForm: reactive({
        username: '',
        realName: '',
        phone: '',
        organizationKeyword: '',
        organizationId: '',
        roleId: '',
        pageNum: 1,
        pageSize: 10
      }),
      syncAppliedFilters: vi.fn(),
      handleOrganizationSelect: vi.fn(),
      loadUsers: vi.fn().mockResolvedValue(undefined),
      handleSearch: vi.fn(),
      handleReset: vi.fn(),
      handlePageChange: vi.fn(),
      getSerialNumber: vi.fn((index: number) => index + 1),
      initialize: vi.fn().mockResolvedValue(undefined)
    })

    mockedUseUserCrud.mockReturnValue({
      formModalVisible: ref(false),
      formMode: ref('create'),
      currentUser: ref(null),
      detailVisible: ref(false),
      passwordVisible: ref(false),
      confirmState: reactive({
        visible: false,
        target: null
      }),
      openCreateModal: vi.fn(),
      openEditModal: vi.fn(),
      openDetailModal: vi.fn(),
      openPasswordModal: vi.fn(),
      openDeleteConfirm: vi.fn(),
      closeDeleteConfirm: vi.fn(),
      handleFormSubmit: vi.fn(),
      handlePasswordSubmit: vi.fn(),
      handleConfirmDelete: vi.fn()
    })
  })

  test('testRenderActions_WhenUserNameIsAdmin_ShouldHideDeleteAndResetPassword', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const rows = wrapper.findAll('tbody tr')
    expect(rows).toHaveLength(2)
    expect(rows[0]!.text()).toContain('admin')
    expect(rows[0]!.text()).not.toContain('重置密码')
    expect(rows[0]!.text()).not.toContain('删除')
    expect(rows[1]!.text()).toContain('tester')
    expect(rows[1]!.text()).toContain('重置密码')
    expect(rows[1]!.text()).toContain('删除')
  })
})
