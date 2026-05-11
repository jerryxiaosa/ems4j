<script setup lang="ts">
import { ref } from 'vue'
import AppAmount from '@/components/common/AppAmount.vue'
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import AppTabBar from '@/components/common/AppTabBar.vue'
import AppVisibilityToggle from '@/components/common/AppVisibilityToggle.vue'
import { miniRoute } from '@/utils/route'

const rechargeAmount = ref('')
const isBalanceVisible = ref(true)

const handleBack = () => {
  uni.redirectTo({
    url: miniRoute.home
  })
}

const goPayConfirm = () => {
  const amount = rechargeAmount.value || '200'

  uni.navigateTo({
    url: `${miniRoute.payConfirm}?amount=${encodeURIComponent(amount)}`
  })
}

const goPayRecord = () => {
  uni.navigateTo({
    url: miniRoute.payRecord
  })
}

const toggleBalanceVisible = () => {
  isBalanceVisible.value = !isBalanceVisible.value
}

</script>

<template>
  <view class="recharge-page">
    <AppBackHeader title="充值缴费" @back="handleBack" />

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="account-card">
          <image class="account-hero" src="/static/stitch/pay-confirm-hero.jpg" mode="aspectFill" />
          <view class="account-overlay"></view>
          <view class="account-info">
            <view class="community-row">
              <view class="community-icon">
                <image class="community-icon-image" src="/static/icons/account-house-white.svg" mode="aspectFit" />
              </view>
              <text>星河家园 2 栋住户账户</text>
            </view>
            <view class="balance-label">
              <text>当前余额（元）</text>
              <AppVisibilityToggle :visible="isBalanceVisible" @toggle="toggleBalanceVisible" />
            </view>
            <view class="balance-value">
              <AppAmount :visible="isBalanceVisible" value="1,234.56" size="medium" />
            </view>
          </view>
        </view>

        <view class="form-card">
          <text class="section-title">充值金额</text>
          <view class="amount-input-wrap">
            <text class="amount-currency">¥</text>
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

    <AppTabBar active="recharge" />
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

.page-scroll {
  flex: 1;
  min-height: 0;
}

.content-stack {
  padding: design-rpx(16) design-rpx(22) design-rpx(156);
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
  padding: design-rpx(22) design-rpx(16);
}

.community-row {
  display: flex;
  align-items: center;
  gap: design-rpx(10);
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

.form-card {
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

.amount-currency {
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
  bottom: calc(env(safe-area-inset-bottom) + #{design-rpx(54)});
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

</style>
