import { nextTick } from 'vue'
import { fetchUserPage } from '@/api/adapters/user'
import { searchOrganizationOptions } from '@/api/adapters/organization'
import { fetchRoleOptions } from '@/api/adapters/role'
import { useUserQuery } from '@/modules/system/users/composables/useUserQuery'

vi.mock('@/api/adapters/user', () => ({
  fetchUserPage: vi.fn()
}))

vi.mock('@/api/adapters/organization', () => ({
  searchOrganizationOptions: vi.fn()
}))

vi.mock('@/api/adapters/role', () => ({
  fetchRoleOptions: vi.fn()
}))

const mockedFetchUserPage = vi.mocked(fetchUserPage)
const mockedSearchOrganizationOptions = vi.mocked(searchOrganizationOptions)
const mockedFetchRoleOptions = vi.mocked(fetchRoleOptions)

describe('useUserQuery', () => {
  beforeEach(() => {
    mockedFetchUserPage.mockReset()
    mockedSearchOrganizationOptions.mockReset()
    mockedFetchRoleOptions.mockReset()
  })

  test('testInitialize_WhenAdaptersSucceed_ShouldLoadOptionsAndUsers', async () => {
    mockedSearchOrganizationOptions.mockResolvedValue([
      { id: 2, name: '研发中心', managerName: '', managerPhone: '' }
    ])
    mockedFetchRoleOptions.mockResolvedValue([{ value: '3', label: '管理员' }])
    mockedFetchUserPage.mockResolvedValue({
      list: [
        {
          id: 9,
          username: 'admin',
          realName: '管理员',
          phone: '138****8000',
          organizationId: '2',
          organizationName: '研发中心',
          roleId: '3',
          roleIds: ['3'],
          roleName: '管理员',
          createTime: '2026-03-01 10:00:00',
          updateTime: '2026-03-01 10:00:00',
          remark: ''
        }
      ],
      total: 1,
      pageNum: 1,
      pageSize: 10
    })
    const setNotice = vi.fn()
    const composable = useUserQuery({ setNotice })

    await composable.initialize()

    expect(mockedSearchOrganizationOptions).toHaveBeenCalledWith('', 200)
    expect(mockedFetchRoleOptions).toHaveBeenCalledTimes(1)
    expect(mockedFetchUserPage).toHaveBeenCalledWith({
      userNameLike: undefined,
      realNameLike: undefined,
      userPhoneLike: undefined,
      organizationId: undefined,
      roleId: undefined,
      pageNum: 1,
      pageSize: 10
    })
    expect(composable.organizationOptions.value).toEqual([{ value: '2', label: '研发中心' }])
    expect(composable.roleOptions.value).toEqual([{ value: '3', label: '管理员' }])
    expect(composable.userRows.value).toHaveLength(1)
    expect(setNotice).not.toHaveBeenCalled()
  })

  test('testOrganizationKeywordWatch_WhenKeywordMatchesOption_ShouldSyncOrganizationId', async () => {
    const composable = useUserQuery({ setNotice: vi.fn() })
    composable.organizationOptions.value = [{ value: '8', label: '测试机构' }]

    composable.queryForm.organizationKeyword = '测试机构'
    await nextTick()
    expect(composable.queryForm.organizationId).toBe('8')

    composable.queryForm.organizationKeyword = '不存在'
    await nextTick()
    expect(composable.queryForm.organizationId).toBe('')
  })

  test('testHandleOrganizationSelect_WhenOptionSelected_ShouldSyncKeywordAndId', () => {
    const composable = useUserQuery({ setNotice: vi.fn() })

    composable.handleOrganizationSelect({
      id: 5,
      name: '园区A',
      managerName: '张三',
      managerPhone: '13800000000'
    })

    expect(composable.queryForm.organizationKeyword).toBe('园区A')
    expect(composable.queryForm.organizationId).toBe('5')
  })

  test('testLoadUsers_WhenFiltersProvided_ShouldTrimAndConvertQuery', async () => {
    mockedFetchUserPage.mockResolvedValue({
      list: [],
      total: 6,
      pageNum: 3,
      pageSize: 20
    })
    const composable = useUserQuery({ setNotice: vi.fn() })
    composable.queryForm.pageNum = 3
    composable.queryForm.pageSize = 20
    composable.appliedFilters.username = ' admin '
    composable.appliedFilters.realName = ' 管理员 '
    composable.appliedFilters.phone = ' 138 '
    composable.appliedFilters.organizationId = '9'
    composable.appliedFilters.roleId = '7'

    await composable.loadUsers()

    expect(mockedFetchUserPage).toHaveBeenCalledWith({
      userNameLike: 'admin',
      realNameLike: '管理员',
      userPhoneLike: '138',
      organizationId: 9,
      roleId: 7,
      pageNum: 3,
      pageSize: 20
    })
    expect(composable.total.value).toBe(6)
    expect(composable.queryForm.pageNum).toBe(3)
    expect(composable.queryForm.pageSize).toBe(20)
  })

  test('testHandleSearch_WhenSearchTriggered_ShouldSyncFiltersAndResetPageNum', async () => {
    mockedFetchUserPage.mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10
    })
    const composable = useUserQuery({ setNotice: vi.fn() })
    composable.queryForm.username = 'tester'
    composable.queryForm.realName = '张三'
    composable.queryForm.phone = '138'
    composable.queryForm.organizationId = '3'
    composable.queryForm.roleId = '5'
    composable.queryForm.pageNum = 9

    await composable.handleSearch()

    expect(composable.appliedFilters).toMatchObject({
      username: 'tester',
      realName: '张三',
      phone: '138',
      organizationId: '3',
      roleId: '5'
    })
    expect(composable.queryForm.pageNum).toBe(1)
  })

  test('testHandleReset_WhenResetTriggered_ShouldClearFiltersAndReload', async () => {
    mockedFetchUserPage.mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10
    })
    const composable = useUserQuery({ setNotice: vi.fn() })
    composable.queryForm.username = 'tester'
    composable.queryForm.realName = '张三'
    composable.queryForm.phone = '138'
    composable.queryForm.organizationKeyword = '研发中心'
    composable.queryForm.organizationId = '2'
    composable.queryForm.roleId = '5'
    composable.appliedFilters.username = 'tester'
    composable.appliedFilters.realName = '张三'
    composable.appliedFilters.phone = '138'
    composable.appliedFilters.organizationId = '2'
    composable.appliedFilters.roleId = '5'

    await composable.handleReset()

    expect(composable.queryForm).toMatchObject({
      username: '',
      realName: '',
      phone: '',
      organizationKeyword: '',
      organizationId: '',
      roleId: '',
      pageNum: 1,
      pageSize: 10
    })
    expect(composable.appliedFilters).toMatchObject({
      username: '',
      realName: '',
      phone: '',
      organizationId: '',
      roleId: ''
    })
  })

  test('testHandlePageChange_WhenLoading_ShouldSkipReload', async () => {
    const composable = useUserQuery({ setNotice: vi.fn() })
    composable.loading.value = true

    await composable.handlePageChange({
      pageNum: 2,
      pageSize: 20
    })

    expect(mockedFetchUserPage).not.toHaveBeenCalled()
    expect(composable.queryForm.pageNum).toBe(1)
    expect(composable.queryForm.pageSize).toBe(10)
  })

  test('testHandlePageChange_WhenNotLoading_ShouldUpdatePagingAndReload', async () => {
    mockedFetchUserPage.mockResolvedValue({
      list: [],
      total: 0,
      pageNum: 2,
      pageSize: 20
    })
    const composable = useUserQuery({ setNotice: vi.fn() })

    await composable.handlePageChange({
      pageNum: 2,
      pageSize: 20
    })

    expect(composable.queryForm.pageNum).toBe(2)
    expect(composable.queryForm.pageSize).toBe(20)
    expect(mockedFetchUserPage).toHaveBeenCalledTimes(1)
    expect(composable.getSerialNumber(0)).toBe(21)
  })

  test('testLoadUsers_WhenAdapterFails_ShouldResetResultAndSetNotice', async () => {
    mockedFetchUserPage.mockRejectedValue(new Error('查询失败'))
    const setNotice = vi.fn()
    const composable = useUserQuery({ setNotice })
    composable.userRows.value = [
      {
        id: 1,
        username: 'old',
        realName: '旧数据',
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
    ]
    composable.total.value = 10

    await composable.loadUsers()

    expect(composable.userRows.value).toEqual([])
    expect(composable.total.value).toBe(0)
    expect(setNotice).toHaveBeenCalledWith('error', '用户列表加载失败：查询失败')
  })

  test('testLoadOptions_WhenAdapterFails_ShouldFallbackToDefaultOptionsAndNotice', async () => {
    mockedSearchOrganizationOptions.mockRejectedValue(new Error('机构失败'))
    mockedFetchRoleOptions.mockRejectedValue(new Error('角色失败'))
    const setNotice = vi.fn()
    const composable = useUserQuery({ setNotice })

    await Promise.all([composable.loadOrganizationOptions(), composable.loadRoleOptions()])

    expect(composable.organizationOptions.value.length).toBeGreaterThan(0)
    expect(composable.roleOptions.value.length).toBeGreaterThan(0)
    expect(setNotice).toHaveBeenCalledWith('error', '机构列表加载失败：机构失败')
    expect(setNotice).toHaveBeenCalledWith('error', '角色列表加载失败：角色失败')
  })
})
