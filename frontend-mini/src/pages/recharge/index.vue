<script setup lang="ts">
import { ref } from 'vue'

const rechargeAmount = ref('')

const handleBack = () => {
  uni.redirectTo({
    url: '/pages/home/index'
  })
}

const goHome = () => {
  uni.redirectTo({
    url: '/pages/home/index'
  })
}

const goPayConfirm = () => {
  const amount = rechargeAmount.value || '200'

  uni.navigateTo({
    url: `/pages/pay-confirm/index?amount=${encodeURIComponent(amount)}`
  })
}

const goPayRecord = () => {
  uni.navigateTo({
    url: '/pages/pay-record/index'
  })
}
</script>

<template>
  <view class="recharge-page">
    <view class="page-header">
      <button class="back-button" aria-label="返回" @click="handleBack">
        <view class="back-chevron"></view>
      </button>
      <text class="page-title">充值缴费</text>
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

        <view class="form-card">
          <text class="section-title">充值金额</text>
          <view class="amount-input-wrap">
            <text class="currency-mark">¥</text>
            <input
              v-model="rechargeAmount"
              class="amount-input"
              type="number"
              placeholder="请输入充值金额"
              placeholder-class="amount-placeholder"
            />
            <text class="amount-unit">元</text>
          </view>
          <text class="form-tip">账户充值后，余额可用于电费扣缴和相关服务。</text>
        </view>

        <view class="notice-card">
          <view class="notice-icon">i</view>
          <view class="notice-copy">
            <text class="notice-title">温馨提示</text>
            <text class="notice-desc">账户充值后，余额可用于电费扣缴和相关服务。</text>
          </view>
        </view>

        <view class="record-link" @click="goPayRecord">
          <text>充值缴费记录</text>
          <view class="record-chevron"></view>
        </view>
      </view>
    </scroll-view>

    <view class="pay-bar">
      <button class="pay-button" @click="goPayConfirm">
        <text>去支付</text>
      </button>
    </view>

    <view class="tabbar">
      <view class="tab-item" @click="goHome">
        <image class="tab-icon-image" src="/static/icons/tab-home.svg" mode="aspectFit" />
        <text>首页</text>
      </view>
      <view class="tab-item is-active">
        <image class="tab-icon-image" src="/static/icons/tab-recharge-active.svg" mode="aspectFit" />
        <text>充值</text>
      </view>
      <view class="tab-item">
        <image class="tab-icon-image" src="/static/icons/tab-bills.svg" mode="aspectFit" />
        <text>账单</text>
      </view>
      <view class="tab-item">
        <image class="tab-icon-image" src="/static/icons/tab-profile.svg" mode="aspectFit" />
        <text>我的</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.recharge-page {
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
  font-size: design-rpx(20);
  font-weight: 700;
  line-height: 1;
  transform: translateX(-50%);
}

.page-scroll {
  flex: 1;
  min-height: 0;
}

.content-stack {
  padding: design-rpx(16) design-rpx(22) design-rpx(184);
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

.form-card,
.notice-card {
  margin-top: design-rpx(16);
  background: #ffffff;
  border: design-rpx(0.5) solid #e7e7f3;
  box-shadow: 0 design-rpx(4) design-rpx(16) rgba(15, 31, 61, 0.04);
}

.form-card {
  padding: design-rpx(20);
  border-radius: design-rpx(20);
}

.section-title {
  display: block;
  margin-bottom: design-rpx(16);
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 700;
}

.amount-input-wrap {
  display: flex;
  align-items: center;
  gap: design-rpx(12);
  height: design-rpx(58);
  padding: 0 design-rpx(16);
  background: #f5f7fd;
  border: design-rpx(0.5) solid #e7e7f3;
  border-radius: design-rpx(12);
}

.currency-mark {
  color: #152234;
  font-size: design-rpx(22);
  font-weight: 600;
}

.amount-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  color: #152234;
  font-size: design-rpx(20);
  font-weight: 500;
}

.amount-placeholder {
  color: #9aa6ba;
  font-weight: 400;
}

.amount-unit {
  color: #8a97ac;
  font-size: design-rpx(15);
  font-weight: 600;
}

.form-tip {
  display: block;
  margin-top: design-rpx(12);
  color: #8a97ac;
  font-size: design-rpx(12);
  line-height: 1.5;
}

.notice-card {
  display: flex;
  gap: design-rpx(12);
  padding: design-rpx(16);
  background: #f5f9ff;
  border-radius: design-rpx(12);
}

.notice-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(20);
  height: design-rpx(20);
  margin-top: design-rpx(2);
  color: #2563eb;
  font-size: design-rpx(13);
  font-weight: 700;
  border: design-rpx(1.5) solid #2563eb;
  border-radius: 999rpx;
}

.notice-copy {
  flex: 1;
  min-width: 0;
}

.notice-title,
.notice-desc {
  display: block;
}

.notice-title {
  margin-bottom: design-rpx(6);
  color: #152234;
  font-size: design-rpx(14);
  font-weight: 600;
}

.notice-desc {
  color: #8a97ac;
  font-size: design-rpx(12);
  line-height: 1.55;
}

.record-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: design-rpx(6);
  padding: design-rpx(24) 0;
  color: #8a97ac;
  font-size: design-rpx(14);
  font-weight: 600;
}

.record-chevron {
  width: design-rpx(7);
  height: design-rpx(7);
  border-top: design-rpx(1.5) solid #8a97ac;
  border-right: design-rpx(1.5) solid #8a97ac;
  transform: rotate(45deg);
}

.pay-bar {
  position: fixed;
  right: 0;
  bottom: design-rpx(82);
  left: 0;
  z-index: 48;
  padding: design-rpx(16);
  background: #ffffff;
  border-top: design-rpx(0.5) solid #e7e7f3;
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

.tabbar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 50;
  display: flex;
  justify-content: space-between;
  padding: design-rpx(12) design-rpx(24) design-rpx(24);
  background: rgba(255, 255, 255, 0.96);
  border-top: design-rpx(0.5) solid #e7e7f3;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: design-rpx(6);
  min-width: design-rpx(42);
  color: #6a7a8f;
  font-size: design-rpx(10);
  font-weight: 600;
}

.tab-item.is-active {
  color: #2563eb;
  font-weight: 800;
}

.tab-icon-image {
  width: design-rpx(24);
  height: design-rpx(24);
}
</style>
