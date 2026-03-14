import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { usePermissionStore } from '@/stores/permission'

export const usePermission = () => {
  const permissionStore = usePermissionStore()
  const { buttonMenuKeys, pageMenus, allowedPaths, firstAccessiblePath, loaded } =
    storeToRefs(permissionStore)

  const hasMenuPermission = (menuKey: string) => permissionStore.hasMenuPermission(menuKey)
  const hasPathPermission = (path: string) => permissionStore.hasPath(path)

  return {
    permissionStore,
    buttonMenuKeys,
    pageMenus,
    allowedPaths,
    firstAccessiblePath,
    loaded,
    hasMenuPermission,
    hasPathPermission,
    hasAnyMenuPermission: (menuKeys: string[]) => menuKeys.some(hasMenuPermission),
    hasAllMenuPermissions: (menuKeys: string[]) => menuKeys.every(hasMenuPermission),
    buttonPermissionSet: computed(() => new Set(buttonMenuKeys.value))
  }
}
