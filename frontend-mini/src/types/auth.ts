export type MiniLoginRequest = {
  loginCode: string
  phoneCode: string
}

export type MiniLoginResponse = {
  accessToken: string
  expireIn: number
}
