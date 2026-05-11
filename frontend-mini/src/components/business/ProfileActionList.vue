<script setup lang="ts">
type ProfileActionCode = 'rechargeRecord' | 'billing' | 'meter'

defineProps<{
  items: Array<{
    title: string
    desc: string
    tone: 'recharge' | 'bill' | 'meter'
    iconSrc: string
    action: ProfileActionCode
  }>
}>()

const emit = defineEmits<{
  action: [action: ProfileActionCode]
}>()
</script>

<template>
  <view class="action-card primary-action-card">
    <view
      v-for="(item, index) in items"
      :key="item.title"
      class="action-row"
      @click="emit('action', item.action)"
    >
      <view :class="['action-icon', item.tone]">
        <image class="action-icon-image" :src="item.iconSrc" mode="aspectFit" />
      </view>
      <view class="action-copy">
        <text class="action-title">{{ item.title }}</text>
        <text class="action-desc">{{ item.desc }}</text>
      </view>
      <view class="action-chevron"></view>
      <view v-if="index < items.length - 1" class="row-divider"></view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.action-card {
  margin-top: design-rpx(18);
  overflow: hidden;
  background: #ffffff;
  border: design-rpx(1) solid rgba(219, 228, 242, 0.72);
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.035);
}

.action-row {
  position: relative;
  display: flex;
  align-items: center;
  min-height: design-rpx(86);
  padding: design-rpx(15) design-rpx(18);
}

.row-divider {
  position: absolute;
  right: design-rpx(18);
  bottom: 0;
  left: design-rpx(84);
  height: design-rpx(0.5);
  background: #edf2f9;
}

.action-icon {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(52);
  height: design-rpx(52);
  margin-right: design-rpx(18);
  border-radius: 999rpx;
}

.action-icon.recharge {
  background: #e9fff5;
}

.action-icon.bill {
  background: #fff7e6;
}

.action-icon.meter {
  background: #eaf4ff;
}

.action-icon-image {
  width: design-rpx(40);
  height: design-rpx(40);
}

.action-copy {
  flex: 1;
  min-width: 0;
}

.action-title,
.action-desc {
  display: block;
}

.action-title {
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 400;
  line-height: 1.2;
}

.action-desc {
  margin-top: design-rpx(10);
  color: #6a7a8f;
  font-size: design-rpx(14);
  font-weight: 400;
  line-height: 1;
}

.action-chevron {
  flex-shrink: 0;
  width: design-rpx(10);
  height: design-rpx(10);
  margin-left: design-rpx(12);
  border-top: design-rpx(2) solid #6f7788;
  border-right: design-rpx(2) solid #6f7788;
  transform: rotate(45deg);
}
</style>
