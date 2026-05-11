import { miniRoute } from '@/utils/route'

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
    url: miniRoute.home,
    icon: {
      default: '/static/icons/tab-home-reference.png',
      active: '/static/icons/tab-home-reference-active.png'
    }
  },
  {
    key: 'recharge',
    label: '充值',
    url: miniRoute.recharge,
    icon: {
      default: '/static/icons/tab-recharge-reference.png',
      active: '/static/icons/tab-recharge-reference-active.png'
    }
  },
  {
    key: 'billing',
    label: '账单',
    url: miniRoute.billing,
    icon: {
      default: '/static/icons/tab-billing-reference.png',
      active: '/static/icons/tab-billing-reference-active.png'
    }
  },
  {
    key: 'profile',
    label: '我的',
    url: miniRoute.my,
    icon: {
      default: '/static/icons/tab-profile-reference.png',
      active: '/static/icons/tab-profile-reference-active.png'
    }
  }
]
