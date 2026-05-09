<script setup lang="ts">
type TabKey = 'home' | 'recharge' | 'billing' | 'profile'

const props = defineProps<{
  active: TabKey
}>()

const tabs: Array<{ key: TabKey; label: string; icon: string; url?: string }> = [
  { key: 'home', label: '首页', icon: 'home', url: '/pages/home/index' },
  { key: 'recharge', label: '充值', icon: 'recharge', url: '/pages/recharge/index' },
  { key: 'billing', label: '账单', icon: 'billing', url: '/pages/billing/index' },
  { key: 'profile', label: '我的', icon: 'profile' }
]

const handleTabClick = (tab: (typeof tabs)[number]) => {
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
      v-for="tab in tabs"
      :key="tab.key"
      :class="['app-tab-item', tab.key === active ? 'is-active' : '']"
      @click="handleTabClick(tab)"
    >
      <view :class="['app-tab-icon', `is-${tab.icon}`]"></view>
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
  padding: design-rpx(10) design-rpx(30) calc(env(safe-area-inset-bottom) + #{design-rpx(18)});
  background: rgba(255, 255, 255, 0.96);
  border-top: design-rpx(0.5) solid #e7e7f3;
  backdrop-filter: blur(#{design-rpx(10)});
}

.app-tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: design-rpx(7);
  min-width: design-rpx(48);
  color: #535a6a;
  font-size: design-rpx(15);
  font-weight: 500;
  line-height: 1;
}

.app-tab-item.is-active {
  color: #1677ff;
  font-weight: 600;
}

.app-tab-icon {
  position: relative;
  width: design-rpx(30);
  height: design-rpx(30);
  color: currentColor;
}

.app-tab-icon::before,
.app-tab-icon::after {
  position: absolute;
  box-sizing: border-box;
  content: "";
}

.is-home::before {
  top: design-rpx(9);
  left: design-rpx(6);
  width: design-rpx(18);
  height: design-rpx(15);
  border: design-rpx(2.2) solid currentColor;
  border-top: 0;
  border-radius: design-rpx(3);
}

.is-home::after {
  top: design-rpx(4);
  left: design-rpx(6);
  width: design-rpx(18);
  height: design-rpx(18);
  border-top: design-rpx(2.2) solid currentColor;
  border-left: design-rpx(2.2) solid currentColor;
  border-radius: design-rpx(2);
  transform: rotate(45deg);
}

.is-recharge::before {
  top: design-rpx(2);
  left: design-rpx(8);
  width: design-rpx(15);
  height: design-rpx(26);
  background: currentColor;
  clip-path: polygon(62% 0, 7% 54%, 42% 54%, 26% 100%, 96% 41%, 58% 41%);
}

.is-recharge::after {
  top: design-rpx(5);
  left: design-rpx(10);
  width: design-rpx(11);
  height: design-rpx(20);
  background: #ffffff;
  clip-path: polygon(58% 8%, 24% 49%, 56% 49%, 44% 79%, 78% 38%, 48% 38%);
}

.is-billing::before {
  top: design-rpx(3);
  left: design-rpx(4);
  width: design-rpx(19);
  height: design-rpx(23);
  border: design-rpx(2.2) solid currentColor;
  border-radius: design-rpx(4);
  background-image:
    linear-gradient(currentColor, currentColor),
    linear-gradient(currentColor, currentColor),
    linear-gradient(currentColor, currentColor);
  background-repeat: no-repeat;
  background-position:
    design-rpx(4) design-rpx(6),
    design-rpx(4) design-rpx(12),
    design-rpx(4) design-rpx(18);
  background-size:
    design-rpx(10) design-rpx(2),
    design-rpx(10) design-rpx(2),
    design-rpx(8) design-rpx(2);
}

.is-billing::after {
  right: design-rpx(1);
  bottom: design-rpx(2);
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(14);
  height: design-rpx(14);
  border: design-rpx(2.2) solid currentColor;
  border-radius: 50%;
  color: currentColor;
  font-size: design-rpx(9);
  font-weight: 600;
  line-height: 1;
  content: "¥";
}

.is-profile::before {
  top: design-rpx(2);
  left: 50%;
  width: design-rpx(12);
  height: design-rpx(12);
  border: design-rpx(2.2) solid currentColor;
  border-radius: 50%;
  transform: translateX(-50%);
}

.is-profile::after {
  right: design-rpx(4);
  bottom: design-rpx(3);
  left: design-rpx(4);
  height: design-rpx(12);
  border: design-rpx(2.2) solid currentColor;
  border-radius: design-rpx(10) design-rpx(10) design-rpx(3) design-rpx(3);
}

.app-tab-label {
  display: block;
}
</style>
