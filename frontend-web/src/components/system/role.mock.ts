import type {
  SystemRoleFormValue,
  SystemRoleItem,
  SystemRolePermissionNode
} from '@/modules/system/roles/types'

export const systemRoleMockList: SystemRoleItem[] = [
  {
    id: 1,
    name: '超级管理员',
    key: 'super_admin',
    description: '拥有所有系统权限，可进行全局管理。'
  },
  {
    id: 2,
    name: '园区管理员',
    key: 'park_admin',
    description: '负责园区运营管理，可处理预付费、设备与基础数据。'
  },
  {
    id: 3,
    name: '机构管理员',
    key: 'organization_admin',
    description: '负责所属机构下的账户、充值及消费记录查询。'
  },
  {
    id: 4,
    name: '财务人员',
    key: 'finance_operator',
    description: '负责订单流水、消费记录及对账处理。'
  }
]

export const createDefaultSystemRoleForm = (): SystemRoleFormValue => ({
  name: '',
  key: '',
  description: ''
})

export const systemRolePermissionTree: SystemRolePermissionNode[] = [
  {
    id: 'accounts',
    label: '账户管理',
    children: [
      { id: 'accounts.info', label: '账户信息' },
      { id: 'accounts.cancel-records', label: '销户记录' }
    ]
  },
  {
    id: 'devices',
    label: '设备管理',
    children: [
      { id: 'devices.electric-meters', label: '智能电表' },
      { id: 'devices.gateways', label: '智能网关' },
      { id: 'devices.categories', label: '设备品类' }
    ]
  },
  {
    id: 'plans',
    label: '方案管理',
    children: [
      { id: 'plans.electric', label: '电价方案' },
      { id: 'plans.warn', label: '预警方案' }
    ]
  },
  {
    id: 'trade',
    label: '交易管理',
    children: [
      { id: 'trade.recharge', label: '电费充值' },
      { id: 'trade.order-flows', label: '订单流水' },
      { id: 'trade.consumption-records', label: '消费记录' }
    ]
  },
  {
    id: 'system',
    label: '系统管理',
    children: [
      { id: 'system.users', label: '用户管理' },
      { id: 'system.roles', label: '角色管理' },
      { id: 'system.menus', label: '菜单管理' },
      { id: 'system.spaces', label: '空间管理' },
      { id: 'system.organizations', label: '机构管理' }
    ]
  }
]
