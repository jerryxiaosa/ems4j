import { flushPromises, mount } from '@vue/test-utils'
import LoginView from '@/views/LoginView.vue'
import { fetchCaptcha } from '@/api/adapters/auth'

const { mockReplace, mockLogin, mockRoute } = vi.hoisted(() => ({
  mockReplace: vi.fn(),
  mockLogin: vi.fn(),
  mockRoute: { query: {} as Record<string, string> }
}))

vi.mock('@/api/adapters/auth', () => ({
  fetchCaptcha: vi.fn()
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    login: mockLogin
  })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    replace: mockReplace
  }),
  useRoute: () => mockRoute
}))

const mockedFetchCaptcha = vi.mocked(fetchCaptcha)

const mountComponent = () => {
  return mount(LoginView, {
    global: {
      stubs: {
        UiSpinner: {
          template: '<div data-test="spinner-stub" />'
        }
      }
    }
  })
}

describe('LoginView', () => {
  beforeEach(() => {
    mockedFetchCaptcha.mockReset()
    mockLogin.mockReset()
    mockReplace.mockReset()
    mockRoute.query = {}
  })

  test('testSubmit_WhenLoginSucceeds_ShouldRedirectToTargetPath', async () => {
    mockedFetchCaptcha.mockResolvedValue({
      captchaKey: 'captcha-key-1',
      imageBase64: 'data:image/png;base64,aaa'
    })
    mockLogin.mockResolvedValue(undefined)
    mockRoute.query = {
      redirect: '/reports/electric-bill'
    }

    const wrapper = mountComponent()
    await flushPromises()

    const inputs = wrapper.findAll('input')
    await inputs[0]!.setValue('admin')
    await inputs[1]!.setValue('123456')
    await inputs[2]!.setValue('ABCD')
    await wrapper.get('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockedFetchCaptcha).toHaveBeenCalledTimes(1)
    expect(mockLogin).toHaveBeenCalledWith({
      userName: 'admin',
      password: '123456',
      captchaValue: 'ABCD',
      captchaKey: 'captcha-key-1'
    })
    expect(mockReplace).toHaveBeenCalledWith('/reports/electric-bill')
  })

  test('testSubmit_WhenLoginFails_ShouldRefreshCaptchaAndClearCaptchaInput', async () => {
    mockedFetchCaptcha
      .mockResolvedValueOnce({
        captchaKey: 'captcha-key-1',
        imageBase64: 'data:image/png;base64,aaa'
      })
      .mockResolvedValueOnce({
        captchaKey: 'captcha-key-2',
        imageBase64: 'data:image/png;base64,bbb'
      })
    mockLogin.mockRejectedValue(new Error('用户名或密码错误'))

    const wrapper = mountComponent()
    await flushPromises()

    const inputs = wrapper.findAll('input')
    await inputs[0]!.setValue('admin')
    await inputs[1]!.setValue('bad-password')
    await inputs[2]!.setValue('WXYZ')
    await wrapper.get('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockLogin).toHaveBeenCalledTimes(1)
    expect(mockLogin.mock.calls[0]?.[0].userName).toBe('admin')
    expect(mockLogin.mock.calls[0]?.[0].password).toBe('bad-password')
    expect(mockedFetchCaptcha).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('用户名或密码错误')
    expect((wrapper.findAll('input')[2]!.element as HTMLInputElement).value).toBe('')
    expect((wrapper.get('img').element as HTMLImageElement).getAttribute('src')).toBe(
      'data:image/png;base64,bbb'
    )
    expect(mockReplace).not.toHaveBeenCalled()
  })
})
