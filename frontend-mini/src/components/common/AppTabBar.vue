<script setup lang="ts">
import { appTabItems, type AppTabKey, type AppTabItem } from './appTabItems'

const props = defineProps<{
  active: AppTabKey
}>()

const getTabIcon = (tab: AppTabItem) => (tab.key === props.active ? tab.icon.active : tab.icon.default)

const handleTabClick = (tab: AppTabItem) => {
  if (tab.key === props.active) {
    return
  }

  if (!tab.url) {
    uni.showToast({
      title: '页面待接入',
      icon: 'none'
    })
    return
  }

  uni.redirectTo({
    url: tab.url
  })
}
</script>

<template>
  <view class="app-tabbar">
    <view
      v-for="tab in appTabItems"
      :key="tab.key"
      :class="['app-tab-item', tab.key === active ? 'is-active' : '']"
      @click="handleTabClick(tab)"
    >
      <image class="app-tab-icon" :src="getTabIcon(tab)" mode="aspectFit" />
      <text class="app-tab-label">{{ tab.label }}</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.app-tabbar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 50;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: design-rpx(8) design-rpx(30) calc(env(safe-area-inset-bottom) + #{design-rpx(4)});
  background: rgba(255, 255, 255, 0.96);
  border-top: design-rpx(0.5) solid #e7e7f3;
  backdrop-filter: blur(#{design-rpx(10)});
}

.app-tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: design-rpx(3);
  min-width: design-rpx(44);
  color: #535a6a;
  font-size: design-rpx(12);
  font-weight: 400;
  line-height: 1;
}

.app-tab-item.is-active {
  color: #1677ff;
  font-weight: 500;
}

.app-tab-icon {
  width: design-rpx(26);
  height: design-rpx(26);
}

.app-tab-label {
  display: block;
}
</style>
