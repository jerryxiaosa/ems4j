export interface SpaceTreeItem {
  id: number
  pid: number
  name: string
  fullPath?: string
  type?: number
  sortIndex?: number
  ownAreaId?: number
  parentsIds?: number[]
  parentsNames?: string[]
  pathLabel: string
  children?: SpaceTreeItem[]
}
