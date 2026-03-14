import type { SystemUserFormValue, SystemUserItem } from '@/modules/system/users/types'
import type { SystemOption } from '@/types/system'

export const systemOrganizationOptions: SystemOption[] = [
  { value: 'org-base', label: 'Base园区' },
  { value: 'org-l2-1', label: '第二级园区1' },
  { value: 'org-su-test', label: '苏州测试gc园区' },
  { value: 'org-wx-kx', label: '无锡国科数智园' },
  { value: 'org-js-lte', label: '江苏利特尔能源园区' }
]

export const systemRoleOptions: SystemOption[] = [
  { value: 'role-super', label: '超级管理员' },
  { value: 'role-enterprise', label: '企业管理员' },
  { value: 'role-park', label: '园区管理员' },
  { value: 'role-employee', label: '员工' },
  { value: 'role-visitor', label: '访客' }
]

export const createDefaultSystemUserForm = (): SystemUserFormValue => ({
  username: '',
  password: '',
  realName: '',
  phone: '',
  userGender: '',
  certificatesType: '',
  certificatesNo: '',
  organizationId: '',
  roleIds: [],
  remark: ''
})

export const createSystemUserRows = (): SystemUserItem[] => [
  {
    id: 1,
    username: 'u_hs2ucn9hz',
    realName: '企业员工',
    phone: '13525463222',
    organizationId: 'org-su-test',
    organizationName: '苏州测试gc园区',
    roleId: 'role-enterprise',
    roleIds: ['role-enterprise'],
    roleName: '企业管理员',
    createTime: '2026-02-11 15:17:55',
    updateTime: '2026-02-11 15:17:55',
    remark: ''
  },
  {
    id: 2,
    username: 'u_z7pbcfhn2',
    realName: '上传图片测试',
    phone: '13654889988',
    organizationId: 'org-base',
    organizationName: 'Base园区',
    roleId: 'role-employee',
    roleIds: ['role-employee'],
    roleName: '员工',
    createTime: '2026-01-22 14:42:01',
    updateTime: '2026-01-23 08:10:00',
    remark: ''
  },
  {
    id: 3,
    username: 'wxw',
    realName: 'wxw',
    phone: '15906138819',
    organizationId: 'org-base',
    organizationName: 'Base园区',
    roleId: 'role-visitor',
    roleIds: ['role-visitor'],
    roleName: '访客',
    createTime: '2025-10-15 15:54:15',
    updateTime: '2025-10-15 15:54:15',
    remark: ''
  },
  {
    id: 4,
    username: 'qytest',
    realName: '企业管理员测试',
    phone: '13201564916',
    organizationId: 'org-wx-kx',
    organizationName: '无锡国科数智园',
    roleId: 'role-enterprise',
    roleIds: ['role-enterprise'],
    roleName: '企业管理员',
    createTime: '2025-07-29 11:56:54',
    updateTime: '2025-08-01 09:20:00',
    remark: '企业侧测试账号'
  },
  {
    id: 5,
    username: 'yqtest',
    realName: '园区管理员测试',
    phone: '13421558889',
    organizationId: 'org-base',
    organizationName: 'Base园区',
    roleId: 'role-park',
    roleIds: ['role-park'],
    roleName: '园区管理员',
    createTime: '2025-07-29 11:54:48',
    updateTime: '2025-07-29 11:54:48',
    remark: ''
  },
  {
    id: 6,
    username: 'zdl',
    realName: '赵大龙',
    phone: '13222523151',
    organizationId: 'org-base',
    organizationName: 'Base园区',
    roleId: 'role-park',
    roleIds: ['role-park'],
    roleName: '园区管理员',
    createTime: '2025-07-22 10:45:10',
    updateTime: '2025-07-24 18:15:22',
    remark: ''
  },
  {
    id: 7,
    username: 'wzh12311',
    realName: 'wzh12312',
    phone: '17612222343',
    organizationId: 'org-js-lte',
    organizationName: '江苏利特尔能源园区',
    roleId: 'role-super',
    roleIds: ['role-super'],
    roleName: '超级管理员',
    createTime: '2025-05-22 16:00:16',
    updateTime: '2025-05-26 09:10:03',
    remark: '总部平台管理员'
  },
  {
    id: 8,
    username: 'u_pu6owgq1',
    realName: '孙骁骁',
    phone: '13156489888',
    organizationId: 'org-base',
    organizationName: 'Base园区',
    roleId: 'role-employee',
    roleIds: ['role-employee'],
    roleName: '员工',
    createTime: '2025-04-23 15:04:04',
    updateTime: '2025-04-23 15:04:04',
    remark: '运维值班账号'
  },
  {
    id: 9,
    username: 'u_ty3mnbk6',
    realName: '新的用户2',
    phone: '19142388888',
    organizationId: 'org-l2-1',
    organizationName: '第二级园区1',
    roleId: 'role-employee',
    roleIds: ['role-employee'],
    roleName: '员工',
    createTime: '2025-03-25 16:40:19',
    updateTime: '2025-03-25 16:40:19',
    remark: ''
  },
  {
    id: 10,
    username: '田用户新',
    realName: '田用户新',
    phone: '17312592973',
    organizationId: 'org-l2-1',
    organizationName: '第二级园区1',
    roleId: 'role-employee',
    roleIds: ['role-employee'],
    roleName: '员工',
    createTime: '2025-03-25 16:35:11',
    updateTime: '2025-03-26 10:15:00',
    remark: ''
  },
  {
    id: 11,
    username: 'sys_demo_01',
    realName: '系统演示A',
    phone: '13800001111',
    organizationId: 'org-su-test',
    organizationName: '苏州测试gc园区',
    roleId: 'role-park',
    roleIds: ['role-park'],
    roleName: '园区管理员',
    createTime: '2025-02-18 09:21:35',
    updateTime: '2025-02-18 09:21:35',
    remark: '用于页面演示'
  },
  {
    id: 12,
    username: 'sys_demo_02',
    realName: '系统演示B',
    phone: '13800002222',
    organizationId: 'org-wx-kx',
    organizationName: '无锡国科数智园',
    roleId: 'role-employee',
    roleIds: ['role-employee'],
    roleName: '员工',
    createTime: '2025-01-09 13:44:58',
    updateTime: '2025-01-10 11:20:31',
    remark: ''
  }
]
