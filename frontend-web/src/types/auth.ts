export interface LoginForm {
  userName: string
  password: string
  captchaKey: string
  captchaValue: string
}

export interface CaptchaInfo {
  captchaKey: string
  imageBase64: string
}

export interface CurrentUser {
  id: string | number
  userName: string
  realName?: string
  userPhone?: string
}

export interface LoginResult {
  token: string
  refreshToken?: string
}
