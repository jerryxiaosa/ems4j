import type { RouteRecordRaw } from 'vue-router'

export const tradeRoutes: RouteRecordRaw[] = [
  {
    path: '/trade/recharge',
    component: () => import('@/views/trades/TradeRechargeView.vue'),
    meta: { requiresAuth: true, title: '电费充值' }
  },
  {
    path: '/trade/order-flows',
    component: () => import('@/views/trades/TradeOrderFlowView.vue'),
    meta: { requiresAuth: true, title: '订单流水' }
  },
  {
    path: '/trade/consumption-records',
    component: () => import('@/views/trades/TradeConsumptionRecordsView.vue'),
    meta: { requiresAuth: true, title: '消费记录' }
  }
]
