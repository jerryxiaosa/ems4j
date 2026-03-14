import {
  NOT_LOGIN_CODE,
  PERMISSION_DENIED_EVENT,
  PERMISSION_ERROR_CODE,
  SUCCESS_CODE,
  normalizePageResult,
  unwrapEnvelope
} from '@/api/raw/types'
import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken } from '@/utils/token'

describe('raw/types', () => {
  test('testUnwrapEnvelope_WhenPayloadIsNotEnvelope_ShouldReturnOriginalValue', () => {
    const payload = { id: 1, name: 'tester' }

    expect(unwrapEnvelope(payload)).toEqual(payload)
  })

  test('testUnwrapEnvelope_WhenPayloadIsPrimitive_ShouldReturnOriginalValue', () => {
    expect(unwrapEnvelope('plain-text')).toBe('plain-text')
  })

  test('testUnwrapEnvelope_WhenPayloadIsSuccessEnvelope_ShouldReturnData', () => {
    const payload = {
      success: true,
      code: SUCCESS_CODE,
      data: {
        id: 1,
        name: 'tester'
      }
    }

    expect(unwrapEnvelope(payload)).toEqual(payload.data)
  })

  test('testUnwrapEnvelope_WhenNotLogin_ShouldClearTokenAndRedirect', () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined)
    setAccessToken('access-token')
    setRefreshToken('refresh-token')
    window.history.replaceState({}, '', '/dashboard?tab=user')

    expect(() =>
      unwrapEnvelope({
        success: false,
        code: NOT_LOGIN_CODE,
        message: '登录已失效'
      })
    ).toThrow('登录已失效')

    expect(getAccessToken()).toBe('')
    expect(getRefreshToken()).toBe('')
  })

  test('testUnwrapEnvelope_WhenNotLoginOnLoginPage_ShouldNotRedirect', () => {
    window.history.replaceState({}, '', '/login')

    expect(() =>
      unwrapEnvelope({
        success: false,
        code: NOT_LOGIN_CODE,
        message: '登录已失效'
      })
    ).toThrow('登录已失效')
  })

  test('testUnwrapEnvelope_WhenPermissionDenied_ShouldDispatchEvent', () => {
    const eventHandler = vi.fn()
    window.addEventListener(PERMISSION_DENIED_EVENT, eventHandler)

    expect(() =>
      unwrapEnvelope({
        success: false,
        code: PERMISSION_ERROR_CODE,
        message: '无权限'
      })
    ).toThrow('无权限')

    expect(eventHandler).toHaveBeenCalledTimes(1)
    window.removeEventListener(PERMISSION_DENIED_EVENT, eventHandler)
  })

  test('testUnwrapEnvelope_WhenBusinessFailed_ShouldThrowBusinessError', () => {
    try {
      unwrapEnvelope({
        success: false,
        code: 500001,
        message: '业务失败'
      })
      throw new Error('expected to throw')
    } catch (error) {
      expect(error).toBeInstanceOf(Error)
      expect((error as Error).message).toBe('业务失败')
      expect((error as Error & { code?: number }).code).toBe(500001)
    }
  })

  test('testNormalizePageResult_WhenRecordsShape_ShouldNormalizeFields', () => {
    const result = normalizePageResult({
      page: '2',
      limit: '20',
      totalCount: '5',
      records: [{ id: 1 }]
    } as never)

    expect(result).toEqual({
      list: [{ id: 1 }],
      total: 5,
      pageNum: 2,
      pageSize: 20
    })
  })

  test('testNormalizePageResult_WhenItemsShape_ShouldNormalizeFields', () => {
    const result = normalizePageResult({
      pageNum: 1,
      pageSize: 10,
      totalSize: 3,
      items: [{ id: 7 }]
    } as never)

    expect(result).toEqual({
      list: [{ id: 7 }],
      total: 3,
      pageNum: 1,
      pageSize: 10
    })
  })

  test('testNormalizePageResult_WhenListShape_ShouldKeepOriginalList', () => {
    const result = normalizePageResult({
      list: [{ id: 3 }],
      total: 1,
      pageNum: 1,
      pageSize: 5
    })

    expect(result).toEqual({
      list: [{ id: 3 }],
      total: 1,
      pageNum: 1,
      pageSize: 5
    })
  })

  test('testNormalizePageResult_WhenPayloadIsEmpty_ShouldReturnDefaults', () => {
    expect(normalizePageResult(undefined)).toEqual({
      list: [],
      total: 0,
      pageNum: undefined,
      pageSize: undefined
    })
  })
})
