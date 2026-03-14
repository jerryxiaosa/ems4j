export interface CurrentUserMenuItem {
  id: number
  parentId: number | null
  menuName: string
  menuKey: string
  path: string
  sortNum: number
  menuType: number
  icon: string
  hidden: boolean
  menuSource: number
}

export interface CurrentUserMenuTreeNode extends CurrentUserMenuItem {
  children: CurrentUserMenuTreeNode[]
}
