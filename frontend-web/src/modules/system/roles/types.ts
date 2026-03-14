export interface SystemRoleItem {
  id: number
  name: string
  key: string
  description: string
  isSystem?: boolean
  isDisabled?: boolean
  menuIds?: string[]
}

export interface SystemRoleFormValue {
  id?: number
  name: string
  key?: string
  description: string
}

export interface SystemRolePermissionNode {
  id: string
  label: string
  children?: SystemRolePermissionNode[]
}
