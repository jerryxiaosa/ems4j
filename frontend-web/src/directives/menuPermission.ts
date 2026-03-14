import type { Directive, DirectiveBinding, WatchStopHandle } from 'vue'
import { watch } from 'vue'
import { pinia } from '@/stores'
import { usePermissionStore } from '@/stores/permission'

type MenuPermissionValue = string | string[] | null | undefined

type MenuPermissionElement = HTMLElement & {
  __menuPermissionOriginalDisplay?: string
  __menuPermissionValue?: MenuPermissionValue
  __menuPermissionStopHandle?: WatchStopHandle
}

const normalizeMenuKeys = (value: MenuPermissionValue) => {
  if (Array.isArray(value)) {
    return value.map((item) => String(item || '').trim()).filter(Boolean)
  }

  if (typeof value === 'string') {
    const normalized = value.trim()
    return normalized ? [normalized] : []
  }

  return []
}

const applyMenuPermission = (el: MenuPermissionElement) => {
  if (el.__menuPermissionOriginalDisplay === undefined) {
    el.__menuPermissionOriginalDisplay = el.style.display || ''
  }

  const permissionStore = usePermissionStore(pinia)
  const menuKeys = normalizeMenuKeys(el.__menuPermissionValue)
  const visible =
    menuKeys.length === 0 || menuKeys.some((menuKey) => permissionStore.hasMenuPermission(menuKey))

  el.style.display = visible ? el.__menuPermissionOriginalDisplay : 'none'
}

const menuPermissionDirective: Directive<MenuPermissionElement, MenuPermissionValue> = {
  mounted(el, binding) {
    const permissionStore = usePermissionStore(pinia)
    el.__menuPermissionValue = binding.value
    applyMenuPermission(el)
    el.__menuPermissionStopHandle = watch(
      () => permissionStore.permissionVersion,
      () => {
        applyMenuPermission(el)
      }
    )
  },
  updated(el, binding) {
    el.__menuPermissionValue = binding.value
    applyMenuPermission(el)
  },
  beforeUnmount(el) {
    el.__menuPermissionStopHandle?.()
    delete el.__menuPermissionStopHandle
  }
}

export default menuPermissionDirective
