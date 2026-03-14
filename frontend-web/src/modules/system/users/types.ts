export interface SystemUserItem {
  id: number
  username: string
  realName: string
  phone: string
  organizationId: string
  organizationName: string
  roleId: string
  roleIds: string[]
  roleName: string
  createTime: string
  updateTime: string
  remark: string
  genderName?: string
  certificatesTypeText?: string
  certificatesNo?: string
}

export interface SystemUserFormValue {
  id?: number
  username: string
  password: string
  realName: string
  phone: string
  userGender: string
  certificatesType: string
  certificatesNo: string
  organizationId: string
  roleIds: string[]
  remark: string
}

export interface SystemUserPasswordFormValue {
  password: string
  confirmPassword: string
}
