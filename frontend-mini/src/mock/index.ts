export const useMock = true
export const useMockAuth = false

export const mockDelay = async () => {
  await new Promise<void>((resolve) => {
    setTimeout(resolve, 120)
  })
}

export const paginateMockList = <T>(list: T[], pageNum = 1, pageSize = 10) => {
  const startIndex = (pageNum - 1) * pageSize

  return {
    list: list.slice(startIndex, startIndex + pageSize),
    pageNum,
    pageSize,
    total: list.length
  }
}
