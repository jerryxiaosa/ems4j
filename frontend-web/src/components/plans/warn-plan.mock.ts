import type { WarnPlanFormValue, WarnPlanItem } from '@/types/plan'

export const warnPlanRows: WarnPlanItem[] = [
  {
    id: 1,
    name: '默认预警方案',
    firstLevel: 100,
    secondLevel: 50,
    autoClose: true,
    createUser: '系统管理员',
    createTime: '2026-03-01 09:20:00',
    updateUser: '张三',
    updateTime: '2026-03-02 10:18:00',
    remark: '适用于常规园区电费预警场景。'
  },
  {
    id: 2,
    name: '宽松预警方案',
    firstLevel: 200,
    secondLevel: 80,
    autoClose: false,
    createUser: '系统管理员',
    createTime: '2026-03-01 09:40:00',
    updateUser: '李四',
    updateTime: '2026-03-02 14:35:00',
    remark: '适合回款周期较长的项目，预警阈值相对宽松。'
  },
  {
    id: 3,
    name: '严格预警方案',
    firstLevel: 60,
    secondLevel: 20,
    autoClose: true,
    createUser: '系统管理员',
    createTime: '2026-03-01 10:05:00',
    updateUser: '',
    updateTime: '',
    remark: '用于高风险账户，要求更早触发预警。'
  }
]

export const createDefaultWarnPlanForm = (): WarnPlanFormValue => ({
  name: '',
  firstLevel: '',
  secondLevel: '',
  autoClose: 'true',
  remark: ''
})
