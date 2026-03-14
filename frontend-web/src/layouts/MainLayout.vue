<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import MenuSvgIcon from '@/components/common/MenuSvgIcon.vue'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'
import type { CurrentUserMenuTreeNode } from '@/types/permission'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const permissionStore = usePermissionStore()
const { pageMenus } = storeToRefs(permissionStore)

const isPathMatched = (menuPath: string, currentPath: string) => {
  return currentPath === menuPath || currentPath.startsWith(`${menuPath}/`)
}

const getNodeKey = (node: CurrentUserMenuTreeNode) => {
  return node.menuKey || String(node.id)
}

const findTopLevelMenuLabelByPath = (currentPath: string) => {
  let bestMatch: { label: string; pathLength: number } | null = null

  const walk = (nodes: CurrentUserMenuTreeNode[], topLabel: string) => {
    for (const node of nodes) {
      if (node.path && isPathMatched(node.path, currentPath)) {
        const pathLength = node.path.length
        if (!bestMatch || pathLength > bestMatch.pathLength) {
          bestMatch = { label: topLabel, pathLength }
        }
      }

      if (Array.isArray(node.children) && node.children.length > 0) {
        walk(node.children, topLabel)
      }
    }
  }

  for (const menu of pageMenus.value) {
    walk([menu], menu.menuName)
  }

  const resolvedMatch = bestMatch as { label: string; pathLength: number } | null
  return resolvedMatch?.label
}

const currentUserName = computed(() => {
  return authStore.user?.realName || authStore.user?.userName || '未登录用户'
})

const headerBreadcrumbItems = computed(() => {
  const matchedTitles = route.matched
    .map((record) => {
      const title = record.meta?.title
      return typeof title === 'string' ? title : ''
    })
    .filter(Boolean)

  const dedupedTitles: string[] = []
  for (const title of matchedTitles) {
    if (dedupedTitles[dedupedTitles.length - 1] !== title) {
      dedupedTitles.push(title)
    }
  }

  const topLevelMenuLabel = findTopLevelMenuLabelByPath(route.path)
  const breadcrumbItems = topLevelMenuLabel ? [topLevelMenuLabel, ...dedupedTitles] : dedupedTitles

  const dedupedBreadcrumbItems: string[] = []
  for (const item of breadcrumbItems) {
    if (item && dedupedBreadcrumbItems[dedupedBreadcrumbItems.length - 1] !== item) {
      dedupedBreadcrumbItems.push(item)
    }
  }

  if (dedupedBreadcrumbItems.length > 0) {
    return dedupedBreadcrumbItems
  }

  const currentTitle = route.meta?.title
  return typeof currentTitle === 'string' ? [currentTitle] : ['能耗预付费管理系统']
})

onMounted(async () => {
  if (authStore.isLoggedIn && !authStore.user) {
    try {
      await authStore.loadCurrentUser()
    } catch (_error) {
      authStore.clearSession()
      router.replace('/login')
    }
  }
})

const logout = async () => {
  await authStore.logout()
  router.replace('/login')
}

const isRouteNode = (node: CurrentUserMenuTreeNode) => {
  return typeof node.path === 'string' && node.path.length > 0
}

const isGroupNode = (node: CurrentUserMenuTreeNode) => {
  return !isRouteNode(node) && node.children.length > 0
}

const createCollapsedState = (nodes: CurrentUserMenuTreeNode[]) => {
  const state: Record<string, boolean> = {}

  const walk = (items: CurrentUserMenuTreeNode[]) => {
    for (const item of items) {
      if (isGroupNode(item)) {
        state[getNodeKey(item)] = false
        walk(item.children)
      }
    }
  }

  walk(nodes)
  return state
}

const findExpandedKeysByPath = (nodes: CurrentUserMenuTreeNode[], currentPath: string): string[] => {
  const walk = (items: CurrentUserMenuTreeNode[], ancestors: string[] = []): string[] | null => {
    for (const item of items) {
      if (isRouteNode(item) && isPathMatched(item.path, currentPath)) {
        return ancestors
      }

      if (isGroupNode(item)) {
        const match = walk(item.children, [...ancestors, getNodeKey(item)])
        if (match) {
          return match
        }
      }
    }

    return null
  }

  return walk(nodes) || []
}

const createExpandedStateByPath = (menus: CurrentUserMenuTreeNode[], currentPath: string) => {
  const state = createCollapsedState(menus)
  for (const key of findExpandedKeysByPath(menus, currentPath)) {
    state[key] = true
  }
  return state
}

const expandedNodeMap = ref<Record<string, boolean>>({})

const isExpanded = (key: string) => {
  return expandedNodeMap.value[key] ?? false
}

const toggleExpand = (key: string) => {
  expandedNodeMap.value[key] = !isExpanded(key)
}

watch(
  pageMenus,
  (menus) => {
    expandedNodeMap.value = createExpandedStateByPath(menus, route.path)
  },
  { immediate: true }
)

watch(
  () => route.path,
  (currentPath) => {
    const matchedKeys = findExpandedKeysByPath(pageMenus.value, currentPath)
    if (!matchedKeys.length) {
      return
    }

    for (const key of matchedKeys) {
      expandedNodeMap.value[key] = true
    }
  }
)
</script>

<template>
  <div class="layout">
    <aside class="menu">
      <h1 class="title">能耗预付费管理系统</h1>
      <div class="sub-title">Energy Prepayment System</div>
      <nav class="menu-nav">
        <template v-for="item in pageMenus" :key="item.id">
          <RouterLink
            v-if="isRouteNode(item)"
            :to="item.path"
            class="menu-link menu-link-top"
            active-class="menu-link-active"
          >
            <span class="menu-toggle-title-wrap">
              <span v-if="item.icon" class="menu-group-icon" aria-hidden="true">
                <MenuSvgIcon :icon-key="item.icon" :size="18" />
              </span>
              <span>{{ item.menuName }}</span>
            </span>
          </RouterLink>
          <div v-else-if="isGroupNode(item)" class="menu-group">
            <button
              type="button"
              class="menu-group-title menu-toggle menu-toggle-level1"
              :aria-expanded="isExpanded(getNodeKey(item))"
              @click="toggleExpand(getNodeKey(item))"
            >
              <span class="menu-toggle-title-wrap">
                <span v-if="item.icon" class="menu-group-icon" aria-hidden="true">
                  <MenuSvgIcon :icon-key="item.icon" :size="18" />
                </span>
                <span>{{ item.menuName }}</span>
              </span>
              <span
                class="menu-toggle-icon"
                :class="{ 'menu-toggle-icon-expanded': isExpanded(getNodeKey(item)) }"
              >
                ▾
              </span>
            </button>
            <div v-show="isExpanded(getNodeKey(item))" class="menu-group-content">
              <template v-for="child in item.children" :key="child.id">
                <RouterLink
                  v-if="isRouteNode(child)"
                  :to="child.path"
                  class="menu-link menu-link-level1"
                  active-class="menu-link-active"
                >
                  {{ child.menuName }}
                </RouterLink>
                <template v-else-if="isGroupNode(child)">
                  <button
                    type="button"
                    class="menu-subgroup-title menu-toggle menu-toggle-level2"
                    :aria-expanded="isExpanded(getNodeKey(child))"
                    @click="toggleExpand(getNodeKey(child))"
                  >
                    <span>{{ child.menuName }}</span>
                    <span
                      class="menu-toggle-icon"
                      :class="{ 'menu-toggle-icon-expanded': isExpanded(getNodeKey(child)) }"
                    >
                      ▾
                    </span>
                  </button>
                  <div v-show="isExpanded(getNodeKey(child))" class="menu-subgroup-content">
                    <template v-for="grand in child.children" :key="grand.id">
                      <RouterLink
                        v-if="isRouteNode(grand)"
                        :to="grand.path"
                        class="menu-link menu-link-level2"
                        active-class="menu-link-active"
                      >
                        {{ grand.menuName }}
                      </RouterLink>
                    </template>
                  </div>
                </template>
              </template>
            </div>
          </div>
        </template>
      </nav>
    </aside>
    <section class="main">
      <header class="header">
        <div class="header-title" aria-label="当前位置">
          <template v-for="(item, index) in headerBreadcrumbItems" :key="`${item}-${index}`">
            <span v-if="index > 0" class="header-breadcrumb-separator">/</span>
            <span
              class="header-breadcrumb-item"
              :class="{
                'header-breadcrumb-item-current': index === headerBreadcrumbItems.length - 1
              }"
            >
              {{ item }}
            </span>
          </template>
        </div>
        <div class="header-right">
          <div class="user">{{ currentUserName }}</div>
          <button class="logout-btn" @click="logout">退出登录</button>
        </div>
      </header>
      <div class="content">
        <RouterView />
      </div>
    </section>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  width: 100%;
  height: 100%;
}

.menu {
  width: 252px;
  padding: 18px 12px;
  overflow: auto;
  color: var(--es-color-menu-text);
  background: var(--es-color-menu-bg);
  border-right: 1px solid var(--es-color-menu-border);
}

.title {
  padding: 0 8px;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.01em;
  color: var(--es-color-menu-title);
}

.sub-title {
  padding: 0 8px;
  margin-top: 4px;
  margin-bottom: 14px;
  font-size: var(--es-font-size-xs);
  color: var(--es-color-menu-subtitle);
}

.menu-group-title {
  padding: 9px 10px 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--es-color-menu-group-title);
}

.menu-group-content {
  display: grid;
  padding-bottom: 1px;
}

.menu-subgroup-title {
  padding: 9px 10px 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--es-color-menu-subtitle);
}

.menu-subgroup-content {
  display: grid;
}

.menu-toggle {
  display: flex;
  width: 100%;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--es-radius-sm);
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.menu-toggle-title-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.menu-group-icon {
  display: inline-flex;
  width: 24px;
  height: 24px;
  color: rgb(203 213 225 / 90%);
  transition: color 0.2s ease;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.menu-group-icon svg {
  width: 24px;
  height: 24px;
  fill: none;
  stroke: currentcolor;
  stroke-width: 1.5;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.menu-toggle:hover {
  background: rgb(51 65 85 / 35%);
  border-color: rgb(71 85 105 / 60%);
}

.menu-toggle-level1:hover .menu-group-icon {
  color: #dbeafe;
}

.menu-toggle-level1[aria-expanded='true'] .menu-group-icon {
  color: #bfdbfe;
}

.menu-toggle-level1 {
  margin: 2px 0 4px;
}

.menu-toggle-level2 {
  margin: 2px 0 4px 8px;
}

.menu-toggle-icon {
  font-size: 12px;
  line-height: 1;
  color: var(--es-color-menu-subtitle);
  transition: transform 0.2s ease;
}

.menu-toggle-icon-expanded {
  transform: rotate(180deg);
}

.menu-link {
  display: block;
  padding: 9px 10px;
  margin-bottom: 5px;
  font-size: var(--es-font-size-menu);
  font-weight: 500;
  color: var(--es-color-menu-text);
  text-decoration: none;
  border: 1px solid transparent;
  border-radius: var(--es-radius-sm);
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.menu-link:hover {
  color: var(--es-color-menu-active-text);
  background: var(--es-color-menu-hover-bg);
}

.menu-link-active {
  color: var(--es-color-menu-active-text);
  background: var(--es-color-menu-active-bg);
  border-color: var(--es-color-menu-active-border);
}

.menu-group-content > .menu-link:last-child,
.menu-subgroup-content > .menu-link:last-child {
  margin-bottom: 2px;
}

.menu-link-level1 {
  margin-left: 28px;
}

.menu-link-level2 {
  margin-left: 18px;
}

.menu-link-top {
  display: inline-flex;
  width: 100%;
  align-items: center;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  display: flex;
  height: 58px;
  padding: 0 16px;
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
  border-bottom: 1px solid var(--es-color-border);
  box-shadow: 0 4px 12px rgb(29 78 216 / 6%);
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.header-breadcrumb-item {
  font-weight: 500;
  color: var(--es-color-text-secondary);
}

.header-breadcrumb-item-current {
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.header-breadcrumb-separator {
  font-weight: 400;
  color: var(--es-color-text-placeholder);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user {
  font-size: var(--es-font-size-sm);
  font-weight: 500;
  color: var(--es-color-text-secondary);
}

.logout-btn {
  height: 36px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border-strong);
  border-radius: var(--es-radius-sm);
  transition: border-color 0.2s ease, color 0.2s ease, background-color 0.2s ease;
}

.logout-btn:hover {
  color: var(--es-color-primary);
  background: #f5f9ff;
  border-color: #9fb9ee;
}

.content {
  padding: 16px;
  overflow: hidden auto;
  background: linear-gradient(135deg, rgb(59 130 246 / 8%), transparent 42%),
    repeating-linear-gradient(90deg, transparent, transparent 55px, rgb(29 78 216 / 4%) 56px),
    var(--es-color-bg-page);
  flex: 1;
}

.menu-link:focus-visible,
.menu-toggle:focus-visible,
.logout-btn:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}

@media (width <= 900px) {
  .layout {
    flex-direction: column;
  }

  .menu {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--es-color-menu-border);
  }

  .header {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
    padding: 10px 16px;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
