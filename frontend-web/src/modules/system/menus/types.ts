export interface SystemMenuItem {
  id: number
  parentId: number | null
  name: string
  key: string
  routePath: string
  backendApi: string
  permissionCodes: string[]
  categoryValue: string
  categoryName: string
  platformValue: string
  platformName: string
  hiddenValue: string
  hiddenName: string
  icon: string
  sortNum: number
  remark: string
  children?: SystemMenuItem[]
}

export interface SystemMenuFormValue {
  id?: number
  parentId: string
  parentName: string
  name: string
  key: string
  routePath: string
  backendApis: string[]
  categoryValue: string
  platformValue: string
  hiddenValue: string
  icon: string
  sortNum: string
  remark: string
}
