import type { SystemOrganizationFormValue, SystemOrganizationItem } from '@/modules/system/organizations/types'

export const systemOrganizationMockList: SystemOrganizationItem[] = [
  {
    id: 1,
    name: '苏州园区运营中心',
    code: 'ORG001',
    typeValue: '1',
    typeName: '园区运营方',
    managerName: '张伟',
    managerPhone: '13800138000',
    address: '苏州工业园区星湖街 99 号',
    settledAt: '2025-01-10',
    createTime: '2025-01-10 09:00:00',
    updateTime: '2025-02-18 15:30:00',
    remark: '负责园区整体运营与预付费业务管理'
  },
  {
    id: 2,
    name: '机构测试一部',
    code: 'ORG002',
    typeValue: '2',
    typeName: '入驻机构',
    managerName: '李敏',
    managerPhone: '13900139000',
    address: '苏州工业园区金鸡湖大道 18 号',
    settledAt: '2025-03-18',
    createTime: '2025-03-18 10:20:00',
    updateTime: '2025-03-21 11:15:00',
    remark: '机构侧测试账号归属部门'
  },
  {
    id: 3,
    name: '新能源服务中心',
    code: 'ORG003',
    typeValue: '3',
    typeName: '服务机构',
    managerName: '王强',
    managerPhone: '13700137000',
    address: '苏州工业园区独墅湖科教创新区 6 号',
    settledAt: '2025-06-02',
    createTime: '2025-06-02 08:45:00',
    updateTime: '2025-06-12 17:40:00',
    remark: '负责设备运维与能耗分析服务'
  }
]

export const createDefaultSystemOrganizationForm = (): SystemOrganizationFormValue => ({
  name: '',
  code: '',
  typeValue: '',
  managerName: '',
  managerPhone: '',
  address: '',
  settledAt: '',
  remark: ''
})
