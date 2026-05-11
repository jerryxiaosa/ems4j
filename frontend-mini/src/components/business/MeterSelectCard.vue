<script setup lang="ts">
import AppAmount from '@/components/common/AppAmount.vue'

defineProps<{
  room: string
  meterNo: string
  balance: string | number
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <view class="form-card">
    <text class="section-title">选择电表</text>
    <view class="selected-meter-card" @click="emit('click')">
      <view class="selected-check">
        <view></view>
      </view>
      <view class="meter-main">
        <view class="meter-title-row">
          <text class="meter-room">{{ room }}</text>
          <view class="right-chevron light"></view>
        </view>
        <text class="meter-no">电表编号：{{ meterNo }}</text>
      </view>
      <view class="meter-balance">
        <text class="balance-caption">电表余额</text>
        <view class="balance-amount">
          <AppAmount :value="balance" size="small" />
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.form-card {
  margin-top: design-rpx(16);
  padding: design-rpx(20);
  background: #ffffff;
  border: design-rpx(0.5) solid #e7e7f3;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(4) design-rpx(16) rgba(15, 31, 61, 0.04);
}

.section-title {
  display: block;
  margin-bottom: design-rpx(16);
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 700;
}

.selected-meter-card {
  display: flex;
  align-items: center;
  gap: design-rpx(12);
  min-height: design-rpx(78);
  padding: design-rpx(14) design-rpx(12);
  color: #ffffff;
  background: linear-gradient(135deg, #438bff 0%, #1172ff 100%);
  border-radius: design-rpx(12);
  box-shadow: 0 design-rpx(8) design-rpx(18) rgba(22, 119, 255, 0.2);
}

.selected-check {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(28);
  height: design-rpx(28);
  background: #ffffff;
  border-radius: 999rpx;
}

.selected-check view {
  width: design-rpx(11);
  height: design-rpx(7);
  border-bottom: design-rpx(3) solid #1677ff;
  border-left: design-rpx(3) solid #1677ff;
  transform: rotate(-45deg) translate(design-rpx(1), design-rpx(-1));
}

.meter-main {
  flex: 1;
  min-width: 0;
}

.meter-title-row {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
}

.meter-room {
  display: block;
  overflow: hidden;
  font-size: design-rpx(17);
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meter-no {
  display: block;
  margin-top: design-rpx(8);
  overflow: hidden;
  font-size: design-rpx(13);
  font-weight: 500;
  opacity: 0.92;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meter-balance {
  flex-shrink: 0;
  min-width: design-rpx(86);
  text-align: right;
}

.balance-caption {
  display: block;
  font-size: design-rpx(12);
  font-weight: 500;
  opacity: 0.9;
}

.balance-amount {
  display: flex;
  justify-content: flex-end;
  margin-top: design-rpx(8);
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
</style>
