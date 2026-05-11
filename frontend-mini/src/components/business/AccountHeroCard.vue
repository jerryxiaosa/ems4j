<script setup lang="ts">
import AppAmount from '@/components/common/AppAmount.vue'
import AppVisibilityToggle from '@/components/common/AppVisibilityToggle.vue'

withDefaults(
  defineProps<{
    accountName: string
    balance?: string | number
    balanceVisible?: boolean
    meterCount?: number
    showBalance?: boolean
    showMeterCount?: boolean
  }>(),
  {
    balance: '',
    balanceVisible: true,
    meterCount: 0,
    showBalance: false,
    showMeterCount: false
  }
)

const emit = defineEmits<{
  toggleBalance: []
  meterClick: []
}>()
</script>

<template>
  <view class="account-card">
    <image class="account-hero" src="/static/stitch/pay-confirm-hero.jpg" mode="aspectFill" />
    <view class="account-overlay"></view>
    <view class="account-info">
      <view class="community-row">
        <view class="community-icon">
          <image class="community-icon-image" src="/static/icons/account-house-white.svg" mode="aspectFit" />
        </view>
        <text>{{ accountName }}</text>
      </view>

      <template v-if="showBalance">
        <view class="balance-label">
          <text>当前余额（元）</text>
          <AppVisibilityToggle :visible="balanceVisible" @toggle="emit('toggleBalance')" />
        </view>
        <view class="balance-value">
          <AppAmount :visible="balanceVisible" :value="balance" size="medium" />
        </view>
      </template>

      <view v-if="showMeterCount" class="meter-count-entry" @click="emit('meterClick')">
        <text>共 {{ meterCount }} 个电表</text>
        <view class="right-chevron light"></view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.account-card {
  position: relative;
  min-height: design-rpx(138);
  overflow: hidden;
  color: #06133d;
  background: #eef6ff;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.04);
}

.account-hero {
  position: absolute;
  inset: 0;
  z-index: 1;
  width: 100%;
  height: 100%;
  opacity: 0.96;
}

.account-overlay {
  position: absolute;
  inset: 0;
  z-index: 2;
  background: linear-gradient(90deg, rgba(238, 246, 255, 0.96) 0%, rgba(238, 246, 255, 0.84) 43%, rgba(238, 246, 255, 0.08) 100%);
}

.account-info {
  position: relative;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: design-rpx(22) design-rpx(16);
}

.community-row {
  display: flex;
  align-items: center;
  gap: design-rpx(10);
  max-width: 100%;
  color: #06133d;
  font-size: design-rpx(16);
  font-weight: 400;
}

.community-row text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.community-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(20);
  height: design-rpx(20);
  background: linear-gradient(135deg, #3a8bff 0%, #1768f2 100%);
  border-radius: design-rpx(5);
}

.community-icon-image {
  width: design-rpx(14);
  height: design-rpx(14);
}

.balance-label {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
  margin-top: design-rpx(22);
  color: #8a97ac;
  font-size: design-rpx(13);
  font-weight: 500;
}

.balance-value {
  margin-top: design-rpx(22);
  color: #06133d;
}

.right-chevron {
  flex-shrink: 0;
  width: design-rpx(8);
  height: design-rpx(8);
  border-right: design-rpx(2) solid currentColor;
  border-bottom: design-rpx(2) solid currentColor;
  transform: rotate(-45deg);
}

.light {
  color: currentColor;
}

.meter-count-entry {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
  margin-top: design-rpx(44);
  padding: design-rpx(8) 0;
  color: #06133d;
  font-size: design-rpx(13);
  font-weight: 400;
  line-height: 1;
}
</style>
