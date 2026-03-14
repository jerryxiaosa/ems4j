import { reactive, ref } from 'vue'

export type ElectricMeterNoticeType = 'info' | 'success' | 'error'

interface UseElectricMeterNoticeOptions {
  visibleMs?: number
  fadeMs?: number
}

interface NoticeState {
  type: ElectricMeterNoticeType
  text: string
}

export const useElectricMeterNotice = (options: UseElectricMeterNoticeOptions = {}) => {
  const visibleMs = options.visibleMs ?? 5000
  const fadeMs = options.fadeMs ?? 300

  const notice = reactive<NoticeState>({
    type: 'info',
    text: ''
  })
  const noticeFading = ref(false)
  let noticeFadeTimer: number | null = null
  let noticeClearTimer: number | null = null

  const clearNoticeTimers = () => {
    if (noticeFadeTimer !== null) {
      window.clearTimeout(noticeFadeTimer)
      noticeFadeTimer = null
    }

    if (noticeClearTimer !== null) {
      window.clearTimeout(noticeClearTimer)
      noticeClearTimer = null
    }
  }

  const setNotice = (type: ElectricMeterNoticeType, text: string) => {
    clearNoticeTimers()
    noticeFading.value = false
    notice.type = type
    notice.text = text

    noticeFadeTimer = window.setTimeout(() => {
      noticeFading.value = true
      noticeClearTimer = window.setTimeout(() => {
        notice.type = 'info'
        notice.text = ''
        noticeFading.value = false
        noticeClearTimer = null
      }, fadeMs)
      noticeFadeTimer = null
    }, visibleMs)
  }

  return {
    notice,
    noticeFading,
    setNotice,
    clearNoticeTimers,
    dispose: clearNoticeTimers
  }
}
