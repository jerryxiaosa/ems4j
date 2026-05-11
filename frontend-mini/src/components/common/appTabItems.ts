export type AppTabKey = 'home' | 'recharge' | 'billing' | 'profile'

export type AppTabItem = {
  key: AppTabKey
  label: string
  url?: string
  icon: {
    default: string
    active: string
  }
}

export const appTabItems: AppTabItem[] = [
  {
    key: 'home',
    label: '首页',
    url: '/pages/home/index',
    icon: {
      default: '/static/icons/tab-home-reference.png',
      active: '/static/icons/tab-home-reference-active.png'
    }
  },
  {
    key: 'recharge',
    label: '充值',
    url: '/pages/recharge/index',
    icon: {
      default: '/static/icons/tab-recharge-reference.png',
      active: '/static/icons/tab-recharge-reference-active.png'
    }
  },
  {
    key: 'billing',
    label: '账单',
    url: '/pages/billing/index',
    icon: {
      default: '/static/icons/tab-billing-reference.png',
      active: '/static/icons/tab-billing-reference-active.png'
    }
  },
  {
    key: 'profile',
    label: '我的',
    url: '/pages/my/index',
    icon: {
      default: '/static/icons/tab-profile-reference.png',
      active: '/static/icons/tab-profile-reference-active.png'
    }
  }
]
