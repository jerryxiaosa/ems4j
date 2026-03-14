import { useElectricMeterNotice } from '@/modules/devices/electric-meters/composables/useElectricMeterNotice'

describe('useElectricMeterNotice', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  test('testSetNotice_WhenTimerExpires_ShouldFadeAndClearNotice', () => {
    const { notice, noticeFading, setNotice } = useElectricMeterNotice({
      visibleMs: 1000,
      fadeMs: 200
    })

    setNotice('success', '操作成功')

    expect(notice.type).toBe('success')
    expect(notice.text).toBe('操作成功')
    expect(noticeFading.value).toBe(false)

    vi.advanceTimersByTime(1000)
    expect(noticeFading.value).toBe(true)

    vi.advanceTimersByTime(200)
    expect(notice.type).toBe('info')
    expect(notice.text).toBe('')
    expect(noticeFading.value).toBe(false)
  })

  test('testDispose_WhenTimerPending_ShouldStopFurtherMutation', () => {
    const { notice, noticeFading, setNotice, dispose } = useElectricMeterNotice({
      visibleMs: 1000,
      fadeMs: 200
    })

    setNotice('error', '待清理')
    dispose()
    vi.runAllTimers()

    expect(notice.type).toBe('error')
    expect(notice.text).toBe('待清理')
    expect(noticeFading.value).toBe(false)
  })

  test('testSetNotice_WhenCalledAgain_ShouldResetPreviousTimer', () => {
    const { notice, noticeFading, setNotice } = useElectricMeterNotice({
      visibleMs: 1000,
      fadeMs: 200
    })

    setNotice('error', '第一次')
    vi.advanceTimersByTime(600)

    setNotice('success', '第二次')
    vi.advanceTimersByTime(600)

    expect(notice.type).toBe('success')
    expect(notice.text).toBe('第二次')
    expect(noticeFading.value).toBe(false)
  })

  test('testClearNoticeTimers_WhenClearTimerExists_ShouldStopClearStage', () => {
    const { notice, noticeFading, setNotice, clearNoticeTimers } = useElectricMeterNotice({
      visibleMs: 1000,
      fadeMs: 200
    })

    setNotice('error', '待消失')
    vi.advanceTimersByTime(1000)
    clearNoticeTimers()
    vi.runAllTimers()

    expect(notice.type).toBe('error')
    expect(notice.text).toBe('待消失')
    expect(noticeFading.value).toBe(true)
  })

  test('testSetNotice_WhenUseDefaultOptions_ShouldStillClearNotice', () => {
    const { notice, setNotice } = useElectricMeterNotice()

    setNotice('info', '默认配置')
    vi.advanceTimersByTime(5000)
    vi.advanceTimersByTime(300)

    expect(notice.type).toBe('info')
    expect(notice.text).toBe('')
  })
})
