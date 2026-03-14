<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { PERMISSION_DENIED_EVENT } from '@/api/raw/types'
import { usePermissionStore } from '@/stores/permission'
import { getAccessToken } from '@/utils/token'

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()

let refreshingMenus = false

const handlePermissionDenied = async () => {
  if (!getAccessToken() || refreshingMenus) {
    return
  }

  refreshingMenus = true

  try {
    await permissionStore.refreshMenus()

    if (route.meta?.requiresAuth && !permissionStore.hasPath(route.path)) {
      const fallbackPath = permissionStore.firstAccessiblePath
      await router.replace(fallbackPath || '/login')
    }
  } catch (_error) {
    // Keep the original request error behavior. Permission refresh is best effort only.
  } finally {
    refreshingMenus = false
  }
}

onMounted(() => {
  window.addEventListener(PERMISSION_DENIED_EVENT, handlePermissionDenied)
})

onBeforeUnmount(() => {
  window.removeEventListener(PERMISSION_DENIED_EVENT, handlePermissionDenied)
})
</script>

<template>
  <RouterView />
</template>
