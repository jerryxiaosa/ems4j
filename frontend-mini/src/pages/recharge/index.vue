<script setup lang="ts">
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
        <view class="balance-card">
          <image class="balance-illustration" src="/static/stitch/pay-illustration.png" mode="aspectFit" />

          <view class="balance-content">
            <view class="account-pill">
              <view class="apartment-icon">
                <view></view>
              </view>
              <text>星河家园 2 栋住户账户</text>
              <view class="pill-chevron"></view>
            </view>

            <view class="balance-info">
              <view class="balance-label-row">
                <text>当前余额 (元)</text>
                <view class="eye-icon">
                  <view></view>
                </view>
              </view>
              <text class="balance-value">¥ 1,234.56</text>
            </view>
          </view>
        </view>

        <view class="form-card">
          <text class="section-title">充值金额</text>
          <view class="amount-input-wrap">
            <text class="currency-mark">¥</text>
            <input class="amount-input" type="number" placeholder="请输入充值金额" placeholder-class="amount-placeholder" />
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

        <view class="record-link">
          <text>充值缴费记录</text>
          <view class="record-chevron"></view>
        </view>
      </view>
    </scroll-view>

    <view class="pay-bar">
      <button class="pay-button">
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
  color: #152234;
  background: #f5f7fd;
}

.page-header {
  position: relative;
  z-index: 20;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  height: design-rpx(72);
  padding: design-rpx(28) design-rpx(16) 0;
  background: #ffffff;
  border-bottom: design-rpx(0.5) solid #e7e7f3;
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
  width: design-rpx(13);
  height: design-rpx(13);
  border-bottom: design-rpx(2.5) solid #152234;
  border-left: design-rpx(2.5) solid #152234;
  transform: rotate(45deg);
}

.page-title {
  position: absolute;
  left: 50%;
  color: #152234;
  font-size: design-rpx(18);
  font-weight: 700;
  line-height: 1;
  transform: translateX(-50%);
}

.page-scroll {
  flex: 1;
  min-height: 0;
}

.content-stack {
  padding: design-rpx(16) design-rpx(16) design-rpx(184);
}

.balance-card {
  position: relative;
  min-height: design-rpx(220);
  padding: design-rpx(40) design-rpx(24) design-rpx(58);
  overflow: hidden;
  background: linear-gradient(90deg, #5a91f0 0%, #82adfa 100%);
  border-radius: design-rpx(16);
  box-shadow: 0 design-rpx(10) design-rpx(26) rgba(47, 112, 230, 0.18);
}

.balance-illustration {
  position: absolute;
  right: design-rpx(-118);
  bottom: design-rpx(-124);
  z-index: 1;
  width: design-rpx(408);
  height: design-rpx(408);
  opacity: 0.92;
}

.balance-content {
  position: relative;
  z-index: 2;
}

.account-pill {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
  width: design-rpx(292);
  max-width: design-rpx(292);
  height: design-rpx(34);
  padding: 0 design-rpx(12);
  margin-bottom: design-rpx(32);
  color: #ffffff;
  font-size: design-rpx(14);
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999rpx;
}

.apartment-icon {
  position: relative;
  width: design-rpx(14);
  height: design-rpx(14);
  border: design-rpx(1.5) solid #ffffff;
  border-radius: design-rpx(2);
}

.apartment-icon view {
  position: absolute;
  right: design-rpx(3);
  bottom: 0;
  left: design-rpx(3);
  height: design-rpx(5);
  background: rgba(255, 255, 255, 0.85);
}

.pill-chevron {
  width: design-rpx(7);
  height: design-rpx(7);
  border-right: design-rpx(1.5) solid #ffffff;
  border-bottom: design-rpx(1.5) solid #ffffff;
  transform: rotate(45deg) translateY(design-rpx(-2));
}

.balance-info {
  color: #ffffff;
}

.balance-label-row {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
  margin-bottom: design-rpx(8);
  font-size: design-rpx(14);
  font-weight: 600;
  opacity: 0.92;
}

.eye-icon {
  position: relative;
  width: design-rpx(18);
  height: design-rpx(12);
  border: design-rpx(1.5) solid rgba(255, 255, 255, 0.9);
  border-radius: 999rpx;
}

.eye-icon view {
  position: absolute;
  top: 50%;
  left: 50%;
  width: design-rpx(5);
  height: design-rpx(5);
  background: #ffffff;
  border-radius: 999rpx;
  transform: translate(-50%, -50%);
}

.balance-value {
  display: block;
  font-size: design-rpx(36);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: design-rpx(-1);
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
  border-radius: design-rpx(16);
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
  font-size: design-rpx(24);
  font-weight: 700;
}

.amount-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  color: #152234;
  font-size: design-rpx(20);
  font-weight: 600;
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
  font-weight: 800;
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
  font-weight: 700;
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
  height: design-rpx(54);
  color: #ffffff;
  font-size: design-rpx(18);
  font-weight: 700;
  background: linear-gradient(90deg, #004ac6 0%, #2563eb 100%);
  border-radius: design-rpx(12);
  box-shadow: 0 design-rpx(6) design-rpx(18) rgba(0, 74, 198, 0.28);
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
