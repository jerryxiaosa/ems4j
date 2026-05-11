<script setup lang="ts">
import { computed } from 'vue'
import { miniRoute } from '@/utils/route'

const props = defineProps<{
  status: 'success' | 'fail'
}>()

const isSuccess = computed(() => props.status === 'success')

const pageTitle = computed(() => (isSuccess.value ? '支付成功' : '支付失败'))
const messageTitle = computed(() => (isSuccess.value ? '支付成功' : '支付失败'))
const messageDesc = computed(() => (isSuccess.value ? '缴费已到账，感谢您的使用' : '缴费未完成，请重试'))
const primaryButtonText = computed(() => (isSuccess.value ? '返回首页' : '重新支付'))
const secondaryButtonText = computed(() => (isSuccess.value ? '查看缴费记录' : '返回首页'))

const goHome = () => {
  uni.redirectTo({
    url: miniRoute.home
  })
}

const retryPay = () => {
  uni.redirectTo({
    url: miniRoute.payConfirm
  })
}

const goPayRecord = () => {
  uni.redirectTo({
    url: miniRoute.payRecord
  })
}

const handlePrimary = () => {
  if (isSuccess.value) {
    goHome()
    return
  }

  retryPay()
}

const handleSecondary = () => {
  if (isSuccess.value) {
    goPayRecord()
    return
  }

  goHome()
}
</script>

<template>
  <view class="pay-result-page">
    <view class="page-header">
      <text class="page-title">{{ pageTitle }}</text>
    </view>

    <view class="result-body">
      <view :class="['result-visual', isSuccess ? 'is-success' : 'is-fail']">
        <view class="halo"></view>
        <view class="status-icon">
          <view v-if="isSuccess" class="check-mark"></view>
          <view v-else class="cross-mark">
            <view></view>
            <view></view>
          </view>
        </view>
        <view class="spark spark-one"></view>
        <view class="spark spark-two"></view>
        <view class="spark spark-three"></view>
        <view class="spark spark-four"></view>
      </view>

      <view class="result-copy">
        <text class="message-title">{{ messageTitle }}</text>
        <text class="message-desc">{{ messageDesc }}</text>
      </view>
    </view>

    <view class="action-panel">
      <button class="primary-button" @click="handlePrimary">
        <text>{{ primaryButtonText }}</text>
      </button>
      <button class="secondary-button" @click="handleSecondary">
        <text>{{ secondaryButtonText }}</text>
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.pay-result-page {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 750rpx;
  max-width: 100%;
  height: 100vh;
  overflow: hidden;
  color: #06133d;
  background: linear-gradient(180deg, #f6faff 0%, #ffffff 100%);
}

.page-header {
  position: relative;
  z-index: 2;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  height: design-rpx(86);
  padding-top: design-rpx(34);
}

.page-title {
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 400;
  line-height: 1;
}

.result-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  padding-top: design-rpx(150);
}

.result-visual {
  position: relative;
  width: design-rpx(180);
  height: design-rpx(180);
}

.halo {
  position: absolute;
  inset: 0;
  opacity: 0.2;
  border-radius: 999rpx;
}

.status-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(118);
  height: design-rpx(118);
  border-radius: 999rpx;
  transform: translate(-50%, -50%);
}

.is-success .halo {
  background: #10c66f;
}

.is-success .status-icon {
  background: linear-gradient(135deg, #16d77b 0%, #05bd62 100%);
  box-shadow: 0 design-rpx(14) design-rpx(30) rgba(5, 189, 98, 0.2);
}

.is-fail .halo {
  background: #ff6b73;
}

.is-fail .status-icon {
  background: linear-gradient(135deg, #ff6b73 0%, #ff424d 100%);
  box-shadow: 0 design-rpx(14) design-rpx(30) rgba(255, 66, 77, 0.2);
}

.check-mark {
  width: design-rpx(58);
  height: design-rpx(34);
  border-bottom: design-rpx(10) solid #ffffff;
  border-left: design-rpx(10) solid #ffffff;
  border-radius: design-rpx(4);
  transform: rotate(-45deg) translate(design-rpx(2), design-rpx(-4));
}

.cross-mark {
  position: relative;
  width: design-rpx(64);
  height: design-rpx(64);
}

.cross-mark view {
  position: absolute;
  top: 50%;
  left: 50%;
  width: design-rpx(68);
  height: design-rpx(10);
  background: #ffffff;
  border-radius: 999rpx;
  transform-origin: center;
}

.cross-mark view:first-child {
  transform: translate(-50%, -50%) rotate(45deg);
}

.cross-mark view:last-child {
  transform: translate(-50%, -50%) rotate(-45deg);
}

.spark {
  position: absolute;
  width: design-rpx(14);
  height: design-rpx(14);
  opacity: 0.35;
  border-radius: design-rpx(5);
  transform: rotate(45deg);
}

.is-success .spark {
  background: #10c66f;
}

.is-fail .spark {
  background: #ff6b73;
}

.spark-one {
  top: design-rpx(20);
  left: design-rpx(-18);
}

.spark-two {
  top: design-rpx(34);
  right: design-rpx(-4);
}

.spark-three {
  bottom: design-rpx(26);
  left: design-rpx(-46);
}

.spark-four {
  right: design-rpx(-36);
  bottom: design-rpx(62);
}

.result-copy {
  margin-top: design-rpx(42);
  text-align: center;
}

.message-title,
.message-desc {
  display: block;
}

.message-title {
  color: #06133d;
  font-size: design-rpx(23);
  font-weight: 800;
}

.message-desc {
  margin-top: design-rpx(18);
  color: #6f7d95;
  font-size: design-rpx(15);
  font-weight: 400;
}

.action-panel {
  flex-shrink: 0;
  padding: 0 design-rpx(22) design-rpx(64);
}

.primary-button,
.secondary-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: design-rpx(56);
  border-radius: 999rpx;
}

.primary-button {
  color: #ffffff;
  font-size: design-rpx(20);
  font-weight: 700;
  letter-spacing: design-rpx(1);
  background: linear-gradient(90deg, #1677ff 0%, #0068ff 100%);
  box-shadow: 0 design-rpx(8) design-rpx(22) rgba(22, 119, 255, 0.28);
}

.secondary-button {
  margin-top: design-rpx(22);
  color: #1677ff;
  font-size: design-rpx(18);
  font-weight: 700;
  background: rgba(255, 255, 255, 0.72);
  border: design-rpx(1) solid rgba(22, 119, 255, 0.24);
}
</style>
