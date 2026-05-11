<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const rechargeAmount = ref('200')
const hasAgreed = ref(true)
const serviceFeeAmount = '0.00'

const formatAmount = (value: string) => {
  const numericValue = Number(value)

  if (!Number.isFinite(numericValue) || numericValue <= 0) {
    return '200.00'
  }

  return numericValue.toFixed(2)
}

const rechargeAmountText = computed(() => formatAmount(rechargeAmount.value))
const totalAmountText = computed(() => formatAmount(rechargeAmount.value))

const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  uni.redirectTo({
    url: '/pages/recharge/index'
  })
}

const toggleAgreement = () => {
  hasAgreed.value = !hasAgreed.value
}

const handlePay = () => {
  if (!hasAgreed.value) {
    uni.showToast({
      title: '请先阅读并同意用户服务协议和隐私政策',
      icon: 'none'
    })
    return
  }

  uni.redirectTo({
    url: '/pages/pay-success/index'
  })
}

onLoad((query) => {
  const amount = query?.amount

  if (typeof amount === 'string' && amount.trim()) {
    rechargeAmount.value = amount
  }
})
</script>

<template>
  <view class="pay-confirm-page">
    <view class="page-header">
      <button class="back-button" aria-label="返回" @click="handleBack">
        <view class="back-chevron"></view>
      </button>
      <text class="page-title">确认支付</text>
      <view class="header-placeholder"></view>
    </view>

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="meter-card">
          <image class="meter-hero" src="/static/stitch/pay-confirm-hero.jpg" mode="aspectFill" />
          <view class="meter-info">
            <view class="community-row">
              <view class="community-icon">
                <image class="community-icon-image" src="/static/icons/account-house-white.svg" mode="aspectFit" />
              </view>
              <text>星河家园 2 栋住户账户</text>
            </view>
            <text class="room-text">1 单元 101 室</text>
            <text class="meter-no">电表编号：01234567890123456789</text>
          </view>
        </view>

        <view class="section-card payment-card">
          <text class="section-title">支付方式</text>
          <view class="wechat-row">
            <view class="wechat-icon-wrap">
              <image class="wechat-icon" src="/static/icons/wechat-white.svg" mode="aspectFit" />
            </view>
            <view class="wechat-copy">
              <text class="wechat-title">微信支付</text>
              <text class="wechat-balance">可用余额 ¥ 2,345.67</text>
            </view>
            <view class="selected-circle">
              <view></view>
            </view>
          </view>
        </view>

        <view class="section-card amount-card">
          <text class="section-title">订单金额明细</text>
          <view class="amount-row">
            <text>充值金额</text>
            <text>¥ {{ rechargeAmountText }}</text>
          </view>
          <view class="amount-row">
            <view class="label-with-info">
              <text>服务费</text>
              <view class="info-dot">i</view>
            </view>
            <text>¥ {{ serviceFeeAmount }}</text>
          </view>
          <view class="divider"></view>
          <view class="total-row">
            <text>应付金额</text>
            <text>¥ {{ totalAmountText }}</text>
          </view>
        </view>

        <view class="agreement-row">
          <view
            :class="['agreement-check', hasAgreed ? 'is-checked' : '']"
            @click="toggleAgreement"
          ></view>
          <text class="agreement-text">我已阅读并同意</text>
          <text class="agreement-link">《用户服务协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link">《隐私政策》</text>
        </view>
      </view>
    </scroll-view>

    <view class="pay-bar">
      <button class="pay-button" @click="handlePay">
        <text>立即支付</text>
      </button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.pay-confirm-page {
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
  z-index: 20;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  height: design-rpx(86);
  padding: design-rpx(34) design-rpx(20) 0;
  background: transparent;
}

.back-button,
.header-placeholder {
  width: design-rpx(40);
  height: design-rpx(40);
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-chevron {
  width: design-rpx(14);
  height: design-rpx(14);
  border-bottom: design-rpx(2.5) solid #06133d;
  border-left: design-rpx(2.5) solid #06133d;
  transform: rotate(45deg);
}

.page-title {
  position: absolute;
  left: 50%;
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 400;
  line-height: 1;
  transform: translateX(-50%);
}

.page-scroll {
  flex: 1;
  min-height: 0;
}

.content-stack {
  padding: design-rpx(16) design-rpx(22) design-rpx(148);
}

.meter-card {
  position: relative;
  min-height: design-rpx(138);
  overflow: hidden;
  background: #eef6ff;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.04);
}

.meter-hero {
  position: absolute;
  inset: 0;
  z-index: 1;
  width: 100%;
  height: 100%;
  opacity: 0.96;
}

.meter-card::after {
  position: absolute;
  inset: 0;
  z-index: 2;
  background: linear-gradient(90deg, rgba(238, 246, 255, 0.96) 0%, rgba(238, 246, 255, 0.84) 43%, rgba(238, 246, 255, 0.08) 100%);
  content: "";
}

.meter-info {
  position: relative;
  z-index: 3;
  padding: design-rpx(22) design-rpx(16);
}

.community-row {
  display: flex;
  align-items: center;
  gap: design-rpx(10);
  color: #06133d;
  font-size: design-rpx(16);
  font-weight: 600;
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

.room-text {
  display: block;
  margin-top: design-rpx(18);
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 700;
}

.meter-no {
  display: block;
  margin-top: design-rpx(14);
  color: #8a97ac;
  font-size: design-rpx(13);
  font-weight: 500;
}

.section-card {
  margin-top: design-rpx(20);
  padding: design-rpx(18) design-rpx(16);
  background: #ffffff;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(10) design-rpx(32) rgba(6, 19, 61, 0.06);
}

.section-title {
  display: block;
  margin-bottom: design-rpx(18);
  color: #06133d;
  font-size: design-rpx(16);
  font-weight: 700;
}

.wechat-row {
  display: flex;
  align-items: center;
  gap: design-rpx(12);
  min-height: design-rpx(66);
  padding: design-rpx(12);
  background: #f7faff;
  border-radius: design-rpx(12);
}

.wechat-icon-wrap {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(42);
  height: design-rpx(42);
  background: #0fc244;
  border-radius: design-rpx(10);
}

.wechat-icon {
  width: design-rpx(28);
  height: design-rpx(28);
}

.wechat-copy {
  flex: 1;
  min-width: 0;
}

.wechat-title,
.wechat-balance {
  display: block;
}

.wechat-title {
  color: #06133d;
  font-size: design-rpx(16);
  font-weight: 700;
}

.wechat-balance {
  margin-top: design-rpx(4);
  color: #8a97ac;
  font-size: design-rpx(13);
}

.selected-circle {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(28);
  height: design-rpx(28);
  background: #1677ff;
  border-radius: 999rpx;
}

.selected-circle view {
  width: design-rpx(10);
  height: design-rpx(6);
  border-bottom: design-rpx(2.5) solid #ffffff;
  border-left: design-rpx(2.5) solid #ffffff;
  transform: rotate(-45deg) translateY(design-rpx(-1));
}

.amount-card {
  padding-bottom: design-rpx(22);
}

.amount-row,
.total-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.amount-row {
  margin-top: design-rpx(18);
  color: #8a97ac;
  font-size: design-rpx(15);
  font-weight: 500;
}

.amount-row text:last-child {
  color: #06133d;
  font-weight: 600;
}

.label-with-info {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
}

.info-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(16);
  height: design-rpx(16);
  color: #8a97ac;
  font-size: design-rpx(11);
  font-weight: 700;
  border: design-rpx(1.5) solid #a3adc0;
  border-radius: 999rpx;
}

.divider {
  height: design-rpx(0.5);
  margin: design-rpx(22) 0 design-rpx(18);
  background: #e7e7f3;
}

.total-row {
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 700;
}

.total-row text:last-child {
  color: #06133d;
  font-size: design-rpx(26);
  letter-spacing: design-rpx(1);
}

.agreement-row {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: design-rpx(4);
  margin-top: design-rpx(22);
  color: #8a97ac;
  font-size: design-rpx(13);
}

.agreement-check {
  position: relative;
  width: design-rpx(18);
  height: design-rpx(18);
  margin-right: design-rpx(4);
  background: #ffffff;
  border: design-rpx(1.5) solid #cbd5e1;
  border-radius: 999rpx;
  transition: background-color 120ms ease, border-color 120ms ease, transform 120ms ease;
}

.agreement-check:active {
  transform: scale(0.9);
}

.agreement-check.is-checked {
  background: #1677ff;
  border-color: #1677ff;
}

.agreement-check::after {
  position: absolute;
  top: 50%;
  left: 50%;
  width: design-rpx(8);
  height: design-rpx(4);
  border-bottom: design-rpx(2) solid #ffffff;
  border-left: design-rpx(2) solid #ffffff;
  transform: translate(-50%, -62%) rotate(-45deg);
  transform-origin: center;
  content: "";
  opacity: 0;
}

.agreement-check.is-checked::after {
  opacity: 1;
}

.agreement-link {
  color: #3b82f6;
  font-weight: 600;
}

.pay-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 40;
  padding: design-rpx(16) design-rpx(22) design-rpx(44);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1) 0%, #ffffff 28%);
}

.pay-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: design-rpx(56);
  color: #ffffff;
  font-size: design-rpx(20);
  font-weight: 700;
  letter-spacing: design-rpx(1);
  background: linear-gradient(90deg, #1677ff 0%, #0068ff 100%);
  border-radius: 999rpx;
  box-shadow: 0 design-rpx(8) design-rpx(22) rgba(22, 119, 255, 0.28);
}
</style>
