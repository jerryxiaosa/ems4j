export interface WarnPlanItem {
  id: number
  name: string
  firstLevel: number
  secondLevel: number
  autoClose: boolean
  createUser?: string
  createTime?: string
  updateUser?: string
  updateTime?: string
  remark?: string
}

export interface WarnPlanFormValue {
  id?: number
  name: string
  firstLevel: string
  secondLevel: string
  autoClose: 'true' | 'false'
  remark: string
}

export interface WarnPlanQuery {
  name?: string
}

export interface WarnPlanSavePayload {
  id?: number
  name: string
  firstLevel: number
  secondLevel: number
  autoClose: boolean
  remark?: string
}
