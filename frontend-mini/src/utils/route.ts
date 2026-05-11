export const miniRoute = {
  login: '/pages/login/index',
  accountError: '/pages/account-error/index',
  home: '/pages/home/index',
  recharge: '/pages/recharge/index',
  meterRecharge: '/pages/meter-recharge/index',
  payConfirm: '/pages/pay-confirm/index',
  paySuccess: '/pages/pay-success/index',
  payFail: '/pages/pay-fail/index',
  payRecord: '/pages/pay-record/index',
  billing: '/pages/billing/index',
  billingDetail: '/pages/billing-detail/index',
  meter: '/pages/meter/index',
  meterDetail: '/pages/meter-detail/index',
  my: '/pages/my/index'
} as const

export type MiniRouteKey = keyof typeof miniRoute
