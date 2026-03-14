import {
  createUser,
  deleteUser,
  fetchUserDetail,
  fetchUserDetailRaw,
  fetchUserPage,
  resetUserPassword,
  updateUser
} from '@/api/adapters/user'
import {
  createUserRaw,
  deleteUserRaw,
  getUserDetailRaw,
  getUserDetailRawRaw,
  getUserPageRaw,
  resetUserPasswordRaw,
  updateUserRaw
} from '@/api/raw/user'
import { SUCCESS_CODE } from '@/api/raw/types'

vi.mock('@/api/raw/user', () => ({
  createUserRaw: vi.fn(),
  deleteUserRaw: vi.fn(),
  getUserDetailRaw: vi.fn(),
  getUserDetailRawRaw: vi.fn(),
  getUserPageRaw: vi.fn(),
  resetUserPasswordRaw: vi.fn(),
  updateUserRaw: vi.fn()
}))

const mockedCreateUserRaw = vi.mocked(createUserRaw)
const mockedDeleteUserRaw = vi.mocked(deleteUserRaw)
const mockedGetUserDetailRaw = vi.mocked(getUserDetailRaw)
const mockedGetUserDetailRawRaw = vi.mocked(getUserDetailRawRaw)
const mockedGetUserPageRaw = vi.mocked(getUserPageRaw)
const mockedResetUserPasswordRaw = vi.mocked(resetUserPasswordRaw)
const mockedUpdateUserRaw = vi.mocked(updateUserRaw)

describe('user adapter', () => {
  beforeEach(() => {
    mockedCreateUserRaw.mockReset()
    mockedDeleteUserRaw.mockReset()
    mockedGetUserDetailRaw.mockReset()
    mockedGetUserDetailRawRaw.mockReset()
    mockedGetUserPageRaw.mockReset()
    mockedResetUserPasswordRaw.mockReset()
    mockedUpdateUserRaw.mockReset()
  })

  test('testFetchUserPage_WhenRawPayloadReturned_ShouldNormalizeUserPage', async () => {
    mockedGetUserPageRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        page: '2',
        limit: '20',
        totalCount: '1',
        records: [
          {
            id: '9',
            userName: ' admin ',
            realName: ' 管理员 ',
            userPhone: '138****8000',
            organizationId: '7',
            organizationName: ' 总部 ',
            userGender: 2,
            remark: ' 备注 ',
            certificatesTypeText: ' 身份证 ',
            certificatesNo: ' 110**********1234 ',
            createTime: '2026-03-14T12:13:14',
            updateTime: '2026-03-15 08:00:01',
            roles: [
              { id: '3', roleName: ' 管理员 ' },
              { id: 5, roleName: ' 审核员 ' }
            ]
          }
        ]
      }
    } as never)

    const result = await fetchUserPage({
      userNameLike: 'adm',
      realNameLike: '管理',
      userPhoneLike: '138',
      organizationId: 7,
      roleId: 3,
      pageNum: 2,
      pageSize: 20
    })

    expect(mockedGetUserPageRaw).toHaveBeenCalledWith({
      userNameLike: 'adm',
      realNameLike: '管理',
      userPhoneLike: '138',
      organizationId: 7,
      roleId: 3,
      pageNum: 2,
      pageSize: 20
    })
    expect(result).toEqual({
      list: [
        {
          id: 9,
          username: 'admin',
          realName: '管理员',
          phone: '138****8000',
          organizationId: '7',
          organizationName: '总部',
          roleId: '3',
          roleIds: ['3', '5'],
          roleName: '管理员、审核员',
          createTime: '2026-03-14 12:13:14',
          updateTime: '2026-03-15 08:00:01',
          remark: '备注',
          genderName: '女',
          certificatesTypeText: '身份证',
          certificatesNo: '110**********1234'
        }
      ],
      total: 1,
      pageNum: 2,
      pageSize: 20
    })
  })

  test('testFetchUserDetail_WhenRawPayloadReturned_ShouldNormalizeDetail', async () => {
    mockedGetUserDetailRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        id: 11,
        userName: 'jerry',
        realName: 'Jerry',
        userPhone: '138****5678',
        organizationId: 2,
        organizationName: '园区',
        userGender: 1,
        certificatesTypeText: '护照',
        certificatesNo: 'E1****67',
        roles: [{ id: 8, roleName: '运维' }]
      }
    } as never)

    const result = await fetchUserDetail(11)

    expect(mockedGetUserDetailRaw).toHaveBeenCalledWith(11)
    expect(result).toMatchObject({
      id: 11,
      username: 'jerry',
      realName: 'Jerry',
      phone: '138****5678',
      organizationId: '2',
      organizationName: '园区',
      roleId: '8',
      roleIds: ['8'],
      roleName: '运维',
      genderName: '男',
      certificatesTypeText: '护照',
      certificatesNo: 'E1****67'
    })
  })

  test('testFetchUserDetailRaw_WhenRawApiCalled_ShouldUseNomaskEndpointResult', async () => {
    mockedGetUserDetailRawRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        id: 12,
        userName: 'raw-user',
        realName: 'Raw User',
        userPhone: '13800001234',
        organizationId: 9,
        organizationName: '研发中心',
        roles: [{ id: 6, roleName: '开发' }]
      }
    } as never)

    const result = await fetchUserDetailRaw(12)

    expect(mockedGetUserDetailRawRaw).toHaveBeenCalledWith(12)
    expect(result).toMatchObject({
      id: 12,
      username: 'raw-user',
      phone: '13800001234',
      organizationId: '9',
      roleIds: ['6'],
      roleName: '开发'
    })
  })

  test('testFetchUserDetail_WhenRawHasEmptyRoleAndInvalidTime_ShouldNormalizeFallbackFields', async () => {
    mockedGetUserDetailRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        id: 'not-a-number',
        userName: ' fallback-user ',
        realName: null,
        userPhone: null,
        organizationId: 'invalid-org',
        organizationName: undefined,
        userGender: 0,
        certificatesTypeText: null,
        certificatesNo: undefined,
        createTime: 'invalid-date-T',
        updateTime: '',
        roles: []
      }
    } as never)

    const result = await fetchUserDetail(13)

    expect(result).toEqual({
      id: 0,
      username: 'fallback-user',
      realName: '',
      phone: '',
      organizationId: '',
      organizationName: '',
      roleId: '',
      roleIds: [],
      roleName: '',
      createTime: 'invalid-date- ',
      updateTime: '',
      remark: '',
      genderName: '未知',
      certificatesTypeText: '',
      certificatesNo: ''
    })
  })

  test('testCreateUser_WhenPayloadProvided_ShouldPassThroughAndReturnId', async () => {
    mockedCreateUserRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: 101
    } as never)

    const result = await createUser({
      userName: 'new-user',
      password: '123456',
      realName: '新用户',
      userPhone: '13800001111',
      userGender: 1,
      certificatesType: 1,
      certificatesNo: '110101199001011234',
      organizationId: 2,
      roleIds: [1, 2],
      remark: '备注'
    })

    expect(mockedCreateUserRaw).toHaveBeenCalledWith({
      userName: 'new-user',
      password: '123456',
      realName: '新用户',
      userPhone: '13800001111',
      userGender: 1,
      certificatesType: 1,
      certificatesNo: '110101199001011234',
      organizationId: 2,
      roleIds: [1, 2],
      remark: '备注'
    })
    expect(result).toBe(101)
  })

  test('testUpdateUser_WhenPayloadProvided_ShouldPassThrough', async () => {
    mockedUpdateUserRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: null
    } as never)

    await updateUser(8, {
      realName: '更新后的用户',
      userPhone: '13800009999',
      userGender: 0,
      certificatesType: 2,
      certificatesNo: 'E1234567',
      organizationId: 5,
      roleIds: [3],
      remark: '已更新'
    })

    expect(mockedUpdateUserRaw).toHaveBeenCalledWith(8, {
      realName: '更新后的用户',
      userPhone: '13800009999',
      userGender: 0,
      certificatesType: 2,
      certificatesNo: 'E1234567',
      organizationId: 5,
      roleIds: [3],
      remark: '已更新'
    })
  })

  test('testDeleteUser_WhenIdProvided_ShouldCallRawApi', async () => {
    mockedDeleteUserRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: null
    } as never)

    await deleteUser(6)

    expect(mockedDeleteUserRaw).toHaveBeenCalledWith(6)
  })

  test('testResetUserPassword_WhenPayloadProvided_ShouldPassThrough', async () => {
    mockedResetUserPasswordRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: null
    } as never)

    await resetUserPassword(5, {
      newPassword: 'new-password'
    })

    expect(mockedResetUserPasswordRaw).toHaveBeenCalledWith(5, {
      newPassword: 'new-password'
    })
  })
})
