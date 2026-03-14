export interface SystemOrganizationItem {
  id: number
  name: string
  code: string
  typeValue?: string
  typeName: string
  managerName: string
  managerPhone: string
  address?: string
  settledAt?: string
  createTime?: string
  updateTime?: string
  remark: string
}

export interface SystemOrganizationFormValue {
  id?: number
  name: string
  code: string
  typeValue: string
  managerName: string
  managerPhone: string
  address: string
  settledAt: string
  remark: string
}
