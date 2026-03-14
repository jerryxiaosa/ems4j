export interface SystemSpaceItem {
  id: number
  parentId: number | null
  name: string
  typeValue: string
  typeName: string
  area: number
  sortNum: number
  children?: SystemSpaceItem[]
}

export interface SystemSpaceFormValue {
  id?: number
  parentId: string
  parentName: string
  name: string
  typeValue: string
  typeName: string
  area: string
  sortNum: string
}
