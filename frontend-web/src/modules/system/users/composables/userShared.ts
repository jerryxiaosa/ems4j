export const DEFAULT_USER_PAGE_SIZE = 10

export const getUserErrorMessage = (error: unknown): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }

  return '请稍后重试'
}
