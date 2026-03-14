import { defineStore } from 'pinia'
import { fetchCurrentUserMenus } from '@/api/adapters/permission'
import type { CurrentUserMenuItem, CurrentUserMenuTreeNode } from '@/types/permission'

const CURRENT_MENU_SOURCE = 1

let loadMenusPromise: Promise<void> | null = null

const normalizePath = (path: string): string => {
  const value = path.trim()
  if (!value || value === '/') {
    return value
  }
  return value.replace(/\/+$/, '')
}

const sortMenuNodes = (nodes: CurrentUserMenuTreeNode[]): CurrentUserMenuTreeNode[] => {
  return nodes
    .map((node) => ({
      ...node,
      children: sortMenuNodes(node.children || [])
    }))
    .sort((left, right) => {
      if (left.sortNum !== right.sortNum) {
        return left.sortNum - right.sortNum
      }
      return left.id - right.id
    })
}

const buildMenuTree = (menus: CurrentUserMenuItem[]): CurrentUserMenuTreeNode[] => {
  const nodeMap = new Map<number, CurrentUserMenuTreeNode>()
  const roots: CurrentUserMenuTreeNode[] = []

  menus.forEach((item) => {
    nodeMap.set(item.id, {
      ...item,
      children: []
    })
  })

  nodeMap.forEach((node) => {
    if (node.parentId !== null) {
      const parentNode = nodeMap.get(node.parentId)
      if (parentNode) {
        parentNode.children.push(node)
        return
      }
    }
    roots.push(node)
  })

  return sortMenuNodes(roots)
}

const collectVisiblePageMenus = (
  menus: CurrentUserMenuTreeNode[],
  registeredPathSet: Set<string>
): CurrentUserMenuTreeNode[] => {
  const walk = (nodes: CurrentUserMenuTreeNode[]): CurrentUserMenuTreeNode[] => {
    return nodes
      .filter((node) => node.menuType === 1)
      .map((node) => {
        const visibleChildren = walk(node.children || [])
        return {
          ...node,
          children: visibleChildren
        }
      })
      .filter((node) => {
        if (node.hidden) {
          return false
        }
        if (node.path) {
          return registeredPathSet.has(normalizePath(node.path))
        }
        return node.children.length > 0
      })
  }

  return walk(menus)
}

const collectAllowedPaths = (menus: CurrentUserMenuItem[], registeredPathSet: Set<string>): string[] => {
  return [
    ...new Set(
      menus
        .filter((item) => item.menuType === 1 && item.path)
        .map((item) => normalizePath(item.path))
        .filter((path) => registeredPathSet.has(path))
    )
  ]
}

const collectAllMenuKeys = (menus: CurrentUserMenuItem[]): string[] => {
  return [...new Set(menus.map((item) => item.menuKey).filter(Boolean))]
}

const collectButtonMenuKeys = (menus: CurrentUserMenuItem[]): string[] => {
  return [
    ...new Set(menus.filter((item) => item.menuType === 2).map((item) => item.menuKey).filter(Boolean))
  ]
}

const findFirstVisiblePath = (menus: CurrentUserMenuTreeNode[]): string => {
  const walk = (nodes: CurrentUserMenuTreeNode[]): string => {
    for (const node of nodes) {
      if (node.path) {
        return node.path
      }
      const childPath = walk(node.children || [])
      if (childPath) {
        return childPath
      }
    }
    return ''
  }

  return walk(menus)
}

interface PermissionState {
  rawMenus: CurrentUserMenuItem[]
  pageMenus: CurrentUserMenuTreeNode[]
  allowedPaths: string[]
  allMenuKeys: string[]
  buttonMenuKeys: string[]
  firstAccessiblePath: string
  registeredPaths: string[]
  loaded: boolean
  permissionVersion: number
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    rawMenus: [],
    pageMenus: [],
    allowedPaths: [],
    allMenuKeys: [],
    buttonMenuKeys: [],
    firstAccessiblePath: '',
    registeredPaths: [],
    loaded: false,
    permissionVersion: 0
  }),
  getters: {
    hasPath: (state) => (path: string) => state.allowedPaths.includes(normalizePath(path)),
    hasMenuPermission: (state) => (menuKey: string) => state.allMenuKeys.includes(menuKey)
  },
  actions: {
    setMenus(menus: CurrentUserMenuItem[]) {
      const tree = buildMenuTree(menus)
      const registeredPathSet = new Set(this.registeredPaths.map(normalizePath).filter(Boolean))
      const visiblePageMenus = collectVisiblePageMenus(tree, registeredPathSet)
      const allowedPaths = collectAllowedPaths(menus, registeredPathSet)

      this.rawMenus = menus
      this.pageMenus = visiblePageMenus
      this.allowedPaths = allowedPaths
      this.allMenuKeys = collectAllMenuKeys(menus)
      this.buttonMenuKeys = collectButtonMenuKeys(menus)
      this.firstAccessiblePath = findFirstVisiblePath(visiblePageMenus) || allowedPaths[0] || ''
      this.loaded = true
      this.permissionVersion += 1
    },
    async loadMenus(options?: { force?: boolean; registeredPaths?: string[] }) {
      const force = options?.force ?? false
      if (options?.registeredPaths) {
        this.registeredPaths = [...new Set(options.registeredPaths.map(normalizePath).filter(Boolean))]
      }

      if (this.loaded && !force) {
        return
      }

      if (loadMenusPromise && !force) {
        return loadMenusPromise
      }

      loadMenusPromise = (async () => {
        const menus = await fetchCurrentUserMenus(CURRENT_MENU_SOURCE)
        this.setMenus(menus)
      })()

      try {
        await loadMenusPromise
      } finally {
        loadMenusPromise = null
      }
    },
    async refreshMenus() {
      await this.loadMenus({ force: true })
    },
    clear() {
      this.rawMenus = []
      this.pageMenus = []
      this.allowedPaths = []
      this.allMenuKeys = []
      this.buttonMenuKeys = []
      this.firstAccessiblePath = ''
      this.registeredPaths = []
      this.loaded = false
      this.permissionVersion += 1
      loadMenusPromise = null
    }
  }
})
