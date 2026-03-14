import { afterEach, beforeEach, vi } from 'vitest'

const createMemoryStorage = (): Storage => {
  const dataMap = new Map<string, string>()

  return {
    get length() {
      return dataMap.size
    },
    clear() {
      dataMap.clear()
    },
    getItem(key: string) {
      return dataMap.has(key) ? dataMap.get(key)! : null
    },
    key(index: number) {
      return Array.from(dataMap.keys())[index] ?? null
    },
    removeItem(key: string) {
      dataMap.delete(key)
    },
    setItem(key: string, value: string) {
      dataMap.set(key, String(value))
    }
  }
}

beforeEach(() => {
  const localStorageMock = createMemoryStorage()
  const sessionStorageMock = createMemoryStorage()

  Object.defineProperty(window, 'localStorage', {
    configurable: true,
    value: localStorageMock
  })
  Object.defineProperty(window, 'sessionStorage', {
    configurable: true,
    value: sessionStorageMock
  })
  vi.stubGlobal('localStorage', localStorageMock)
  vi.stubGlobal('sessionStorage', sessionStorageMock)
  document.body.innerHTML = ''
  window.history.replaceState({}, '', '/')
})

afterEach(() => {
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
  vi.clearAllMocks()
  document.body.innerHTML = ''
  window.history.replaceState({}, '', '/')
})
