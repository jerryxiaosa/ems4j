import { ref } from 'vue'
import {
  createUser,
  deleteUser,
  fetchUserDetailRaw,
  resetUserPassword,
  updateUser
} from '@/api/adapters/user'
import type { SystemUserFormValue, SystemUserItem } from '@/modules/system/users/types'
import { useUserCrud } from '@/modules/system/users/composables/useUserCrud'

vi.mock('@/api/adapters/user', () => ({
  createUser: vi.fn(),
  deleteUser: vi.fn(),
  fetchUserDetailRaw: vi.fn(),
  resetUserPassword: vi.fn(),
  updateUser: vi.fn()
}))

const mockedCreateUser = vi.mocked(createUser)
const mockedDeleteUser = vi.mocked(deleteUser)
const mockedFetchUserDetailRaw = vi.mocked(fetchUserDetailRaw)
const mockedResetUserPassword = vi.mocked(resetUserPassword)
const mockedUpdateUser = vi.mocked(updateUser)

const createPayload = (): SystemUserFormValue => ({
  id: 7,
  username: ' admin ',
  password: ' Password@123 ',
  realName: ' 管理员 ',
  phone: ' 13800000000 ',
  userGender: '1',
  certificatesType: '2',
  certificatesNo: ' E1234567 ',
  organizationId: '9',
  roleIds: ['3', '5'],
  remark: ' 备注 '
})

const createQueryForm = () => ({
  username: '',
  realName: '',
  phone: '',
  organizationKeyword: '',
  organizationId: '',
  roleId: '',
  pageNum: 3,
  pageSize: 10
})

describe('useUserCrud', () => {
  beforeEach(() => {
    mockedCreateUser.mockReset()
    mockedDeleteUser.mockReset()
    mockedFetchUserDetailRaw.mockReset()
    mockedResetUserPassword.mockReset()
    mockedUpdateUser.mockReset()
  })

  test('testOpenCreateModal_WhenCalled_ShouldResetCurrentUserAndShowForm', () => {
    const crud = useUserCrud({
      setNotice: vi.fn(),
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })

    crud.currentUser.value = {
      id: 1,
      username: 'old',
      realName: '旧用户',
      phone: '138****0000',
      organizationId: '1',
      organizationName: '旧机构',
      roleId: '1',
      roleIds: ['1'],
      roleName: '旧角色',
      createTime: '',
      updateTime: '',
      remark: ''
    }

    crud.openCreateModal()

    expect(crud.formMode.value).toBe('create')
    expect(crud.currentUser.value).toBeNull()
    expect(crud.formModalVisible.value).toBe(true)
  })

  test('testOpenEditModal_WhenDetailLoaded_ShouldShowFormWithRawUser', async () => {
    mockedFetchUserDetailRaw.mockResolvedValue({
      id: 2,
      username: 'raw-user',
      realName: '原始用户',
      phone: '13800001111',
      organizationId: '2',
      organizationName: '研发中心',
      roleId: '3',
      roleIds: ['3'],
      roleName: '管理员',
      createTime: '',
      updateTime: '',
      remark: ''
    })
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })

    await crud.openEditModal({
      id: 2,
      username: 'masked-user',
      realName: '脱敏用户',
      phone: '138****1111',
      organizationId: '2',
      organizationName: '研发中心',
      roleId: '3',
      roleIds: ['3'],
      roleName: '管理员',
      createTime: '',
      updateTime: '',
      remark: ''
    })

    expect(crud.formMode.value).toBe('edit')
    expect(crud.currentUser.value?.phone).toBe('13800001111')
    expect(crud.formModalVisible.value).toBe(true)
    expect(setNotice).not.toHaveBeenCalled()
  })

  test('testOpenEditModal_WhenDetailLoadFails_ShouldShowNotice', async () => {
    mockedFetchUserDetailRaw.mockRejectedValue(new Error('详情失败'))
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })

    await crud.openEditModal({
      id: 3,
      username: 'tester',
      realName: '测试用户',
      phone: '138****2222',
      organizationId: '1',
      organizationName: '机构',
      roleId: '1',
      roleIds: ['1'],
      roleName: '角色',
      createTime: '',
      updateTime: '',
      remark: ''
    })

    expect(crud.formModalVisible.value).toBe(false)
    expect(setNotice).toHaveBeenCalledWith('error', '用户详情加载失败：详情失败')
  })

  test('testHandleFormSubmit_WhenCreateMode_ShouldTrimPayloadAndReload', async () => {
    mockedCreateUser.mockResolvedValue(10)
    const setNotice = vi.fn()
    const loadUsers = vi.fn().mockResolvedValue(undefined)
    const syncAppliedFilters = vi.fn()
    const queryForm = createQueryForm()
    const crud = useUserCrud({
      setNotice,
      loadUsers,
      syncAppliedFilters,
      queryForm,
      total: ref(0)
    })
    crud.formMode.value = 'create'
    crud.formModalVisible.value = true

    await crud.handleFormSubmit(createPayload())

    expect(mockedCreateUser).toHaveBeenCalledWith({
      userName: 'admin',
      password: 'Password@123',
      realName: '管理员',
      userPhone: '13800000000',
      userGender: 1,
      certificatesType: 2,
      certificatesNo: 'E1234567',
      organizationId: 9,
      roleIds: [3, 5],
      remark: '备注'
    })
    expect(queryForm.pageNum).toBe(1)
    expect(syncAppliedFilters).toHaveBeenCalledTimes(1)
    expect(loadUsers).toHaveBeenCalledTimes(1)
    expect(crud.formModalVisible.value).toBe(false)
    expect(setNotice).toHaveBeenCalledWith('success', '用户新增成功')
  })

  test('testHandleFormSubmit_WhenEditMode_ShouldUpdateAndReload', async () => {
    mockedUpdateUser.mockResolvedValue()
    const setNotice = vi.fn()
    const loadUsers = vi.fn().mockResolvedValue(undefined)
    const crud = useUserCrud({
      setNotice,
      loadUsers,
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })
    crud.formMode.value = 'edit'
    crud.formModalVisible.value = true

    await crud.handleFormSubmit(createPayload())

    expect(mockedUpdateUser).toHaveBeenCalledWith(7, {
      realName: '管理员',
      userPhone: '13800000000',
      userGender: 1,
      certificatesType: 2,
      certificatesNo: 'E1234567',
      organizationId: 9,
      roleIds: [3, 5],
      remark: '备注'
    })
    expect(loadUsers).toHaveBeenCalledTimes(1)
    expect(crud.formModalVisible.value).toBe(false)
    expect(setNotice).toHaveBeenCalledWith('success', '用户编辑成功')
  })

  test('testHandleFormSubmit_WhenEditPayloadWithoutId_ShouldOnlyCloseModal', async () => {
    const crud = useUserCrud({
      setNotice: vi.fn(),
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })
    crud.formMode.value = 'edit'
    crud.formModalVisible.value = true
    const payload = createPayload()
    delete payload.id

    await crud.handleFormSubmit(payload)

    expect(mockedUpdateUser).not.toHaveBeenCalled()
    expect(crud.formModalVisible.value).toBe(false)
  })

  test('testHandlePasswordSubmit_WhenUserMissing_ShouldShowNotice', async () => {
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })

    await crud.handlePasswordSubmit({
      password: ' Password@123 ',
      confirmPassword: ' Password@123 '
    })

    expect(mockedResetUserPassword).not.toHaveBeenCalled()
    expect(setNotice).toHaveBeenCalledWith('error', '缺少用户ID，无法重置密码')
  })

  test('testHandlePasswordSubmit_WhenSuccess_ShouldResetPassword', async () => {
    mockedResetUserPassword.mockResolvedValue()
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })
    crud.currentUser.value = {
      id: 5,
      username: 'tester',
      realName: '测试用户',
      phone: '138****1111',
      organizationId: '1',
      organizationName: '机构',
      roleId: '1',
      roleIds: ['1'],
      roleName: '角色',
      createTime: '',
      updateTime: '',
      remark: ''
    }
    crud.passwordVisible.value = true

    await crud.handlePasswordSubmit({
      password: ' Password@123 ',
      confirmPassword: ' Password@123 '
    })

    expect(mockedResetUserPassword).toHaveBeenCalledWith(5, {
      newPassword: 'Password@123'
    })
    expect(crud.passwordVisible.value).toBe(false)
    expect(setNotice).toHaveBeenCalledWith('success', '密码重置成功')
  })

  test('testHandlePasswordSubmit_WhenAdapterFails_ShouldShowErrorNotice', async () => {
    mockedResetUserPassword.mockRejectedValue(new Error('重置失败'))
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })
    crud.currentUser.value = {
      id: 5,
      username: 'tester',
      realName: '测试用户',
      phone: '138****1111',
      organizationId: '1',
      organizationName: '机构',
      roleId: '1',
      roleIds: ['1'],
      roleName: '角色',
      createTime: '',
      updateTime: '',
      remark: ''
    }

    await crud.handlePasswordSubmit({
      password: ' Password@123 ',
      confirmPassword: ' Password@123 '
    })

    expect(setNotice).toHaveBeenCalledWith('error', '密码重置失败：重置失败')
    expect(crud.passwordVisible.value).toBe(false)
  })

  test('testHandleConfirmDelete_WhenCurrentPageOutOfRange_ShouldFallbackToPreviousPage', async () => {
    mockedDeleteUser.mockResolvedValue()
    const setNotice = vi.fn()
    const loadUsers = vi.fn().mockResolvedValue(undefined)
    const queryForm = createQueryForm()
    queryForm.pageNum = 3
    queryForm.pageSize = 10
    const total = ref(21)
    const crud = useUserCrud({
      setNotice,
      loadUsers,
      syncAppliedFilters: vi.fn(),
      queryForm,
      total
    })
    const target: SystemUserItem = {
      id: 9,
      username: 'tester',
      realName: '测试用户',
      phone: '138****1111',
      organizationId: '1',
      organizationName: '机构',
      roleId: '1',
      roleIds: ['1'],
      roleName: '角色',
      createTime: '',
      updateTime: '',
      remark: ''
    }
    crud.openDeleteConfirm(target)

    await crud.handleConfirmDelete()

    expect(mockedDeleteUser).toHaveBeenCalledWith(9)
    expect(crud.confirmState.visible).toBe(false)
    expect(crud.confirmState.target).toBeNull()
    expect(queryForm.pageNum).toBe(2)
    expect(loadUsers).toHaveBeenCalledTimes(1)
    expect(setNotice).toHaveBeenCalledWith('success', '用户删除成功')
  })

  test('testHandleConfirmDelete_WhenTargetMissing_ShouldSkipDelete', async () => {
    const crud = useUserCrud({
      setNotice: vi.fn(),
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })

    await crud.handleConfirmDelete()

    expect(mockedDeleteUser).not.toHaveBeenCalled()
  })

  test('testHandleConfirmDelete_WhenDeleteFails_ShouldShowErrorNotice', async () => {
    mockedDeleteUser.mockRejectedValue(new Error('删除失败'))
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(5)
    })
    crud.openDeleteConfirm({
      id: 4,
      username: 'tester',
      realName: '测试用户',
      phone: '138****1111',
      organizationId: '1',
      organizationName: '机构',
      roleId: '1',
      roleIds: ['1'],
      roleName: '角色',
      createTime: '',
      updateTime: '',
      remark: ''
    })

    await crud.handleConfirmDelete()

    expect(setNotice).toHaveBeenCalledWith('error', '用户删除失败：删除失败')
  })

  test('testHandleFormSubmit_WhenCreateFails_ShouldShowErrorNotice', async () => {
    mockedCreateUser.mockRejectedValue(new Error('新增失败'))
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })
    crud.formMode.value = 'create'
    crud.formModalVisible.value = true

    await crud.handleFormSubmit(createPayload())

    expect(setNotice).toHaveBeenCalledWith('error', '用户新增失败：新增失败')
    expect(crud.formModalVisible.value).toBe(true)
  })

  test('testHandleFormSubmit_WhenEditFails_ShouldShowErrorNotice', async () => {
    mockedUpdateUser.mockRejectedValue(new Error('编辑失败'))
    const setNotice = vi.fn()
    const crud = useUserCrud({
      setNotice,
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })
    crud.formMode.value = 'edit'
    crud.formModalVisible.value = true

    await crud.handleFormSubmit(createPayload())

    expect(setNotice).toHaveBeenCalledWith('error', '用户编辑失败：编辑失败')
    expect(crud.formModalVisible.value).toBe(true)
  })

  test('testOpenDetailAndPasswordModal_WhenCalled_ShouldUpdateVisibleState', () => {
    const crud = useUserCrud({
      setNotice: vi.fn(),
      loadUsers: vi.fn().mockResolvedValue(undefined),
      syncAppliedFilters: vi.fn(),
      queryForm: createQueryForm(),
      total: ref(0)
    })
    const row: SystemUserItem = {
      id: 8,
      username: 'tester',
      realName: '测试用户',
      phone: '138****1111',
      organizationId: '1',
      organizationName: '机构',
      roleId: '1',
      roleIds: ['1'],
      roleName: '角色',
      createTime: '',
      updateTime: '',
      remark: ''
    }

    crud.openDetailModal(row)
    expect(crud.currentUser.value).toEqual(row)
    expect(crud.detailVisible.value).toBe(true)

    crud.openPasswordModal(row)
    expect(crud.currentUser.value).toEqual(row)
    expect(crud.passwordVisible.value).toBe(true)
  })
})
