<script setup lang="ts">
import { ref } from 'vue'
import AppTabBar from '@/components/common/AppTabBar.vue'

type PrimaryAction = {
  title: string
  desc: string
  tone: 'recharge' | 'bill' | 'meter'
  icon: 'charge' | 'bill' | 'meter'
  action: 'rechargeRecord' | 'billing' | 'meter'
}

type SecondaryAction = {
  title: string
  value?: string
  action: 'version' | 'logout'
}

const isBalanceVisible = ref(true)

const primaryActions: PrimaryAction[] = [
  {
    title: '我的充值',
    desc: '查看充值缴费记录',
    tone: 'recharge',
    icon: 'charge',
    action: 'rechargeRecord'
  },
  {
    title: '我的账单',
    desc: '查看用电明细和充值记录',
    tone: 'bill',
    icon: 'bill',
    action: 'billing'
  },
  {
    title: '我的电表',
    desc: '管理我的电表和用电信息',
    tone: 'meter',
    icon: 'meter',
    action: 'meter'
  }
]

const goPage = (url: string) => {
  uni.navigateTo({
    url
  })
}

const handlePrimaryAction = (action: PrimaryAction['action']) => {
  const actionUrlMap: Record<PrimaryAction['action'], string> = {
    rechargeRecord: '/pages/pay-record/index',
    billing: '/pages/billing/index',
    meter: '/pages/meter/index'
  }

  goPage(actionUrlMap[action])
}

const secondaryActions: SecondaryAction[] = [
  {
    title: '版本信息',
    value: 'V1.0.0',
    action: 'version'
  },
  {
    title: '退出登录',
    action: 'logout'
  }
]

const handleSecondaryAction = (action: SecondaryAction['action']) => {
  if (action === 'version') {
    uni.showToast({
      title: '当前版本 V1.0.0',
      icon: 'none'
    })
    return
  }

  uni.reLaunch({
    url: '/pages/login/index'
  })
}

const goRecharge = () => {
  goPage('/pages/recharge/index')
}

const toggleBalanceVisible = () => {
  isBalanceVisible.value = !isBalanceVisible.value
}
</script>

<template>
  <view class="my-page">
    <view class="page-head">
      <text class="page-title">我的</text>
    </view>

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="profile-card">
          <view class="avatar-wrap">
            <image class="avatar-image" src="/static/icons/profile-avatar.png" mode="aspectFill" />
          </view>

          <view class="profile-main">
            <text class="profile-name">微信用户</text>
            <text class="profile-phone">手机：138 **** 5678</text>
          </view>
        </view>

        <view class="balance-card">
          <image class="balance-bg" src="/static/icons/profile-charge-bg.png" mode="aspectFit" />
          <view class="balance-content">
            <view class="balance-label-row">
              <text>账户余额（元）</text>
              <button
                :class="['balance-eye-button', isBalanceVisible ? 'is-visible' : '']"
                aria-label="切换余额显示"
                @click="toggleBalanceVisible"
              >
                <view class="balance-eye-icon"></view>
              </button>
            </view>
            <text class="balance-value">{{ isBalanceVisible ? '120.00' : '--' }}</text>
          </view>
          <button class="balance-button" @click="goRecharge">去充值</button>
          <view class="balance-wave"></view>
        </view>

        <view class="action-card primary-action-card">
          <view
            v-for="(item, index) in primaryActions"
            :key="item.title"
            class="action-row"
            @click="handlePrimaryAction(item.action)"
          >
            <view :class="['action-icon', item.tone]">
              <image
                v-if="item.icon === 'charge'"
                class="action-icon-image"
                src="/static/icons/profile-charge.png"
                mode="aspectFit"
              />
              <image
                v-else-if="item.icon === 'bill'"
                class="action-icon-image"
                src="/static/icons/profile-bill.png"
                mode="aspectFit"
              />
              <image
                v-else-if="item.icon === 'meter'"
                class="action-icon-image"
                src="/static/icons/meter.png"
                mode="aspectFit"
              />
            </view>
            <view class="action-copy">
              <text class="action-title">{{ item.title }}</text>
              <text class="action-desc">{{ item.desc }}</text>
            </view>
            <view class="action-chevron"></view>
            <view v-if="index < primaryActions.length - 1" class="row-divider"></view>
          </view>
        </view>

        <view class="action-card secondary-action-card">
          <view
            v-for="(item, index) in secondaryActions"
            :key="item.title"
            class="action-row secondary-row"
            @click="handleSecondaryAction(item.action)"
          >
            <text class="secondary-title">{{ item.title }}</text>
            <text v-if="item.value" class="secondary-value">{{ item.value }}</text>
            <view class="action-chevron"></view>
            <view v-if="index < secondaryActions.length - 1" class="row-divider"></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <AppTabBar active="profile" />
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.my-page {
  display: flex;
  flex-direction: column;
  width: 750rpx;
  max-width: 100%;
  height: 100vh;
  overflow: hidden;
  color: #06133d;
  background: linear-gradient(180deg, #f4f9ff 0%, #ffffff 100%);
}

.page-head {
  display: flex;
  flex-shrink: 0;
  align-items: flex-end;
  height: design-rpx(88);
  padding: 0 design-rpx(22) design-rpx(14);
}

.page-title {
  display: block;
  color: #06133d;
  font-size: design-rpx(24);
  font-weight: 800;
  line-height: 1;
}

.page-scroll {
  flex: 1;
  min-height: 0;
  height: 0;
}

.content-stack {
  box-sizing: border-box;
  min-height: 100%;
  padding: design-rpx(18) design-rpx(22) design-rpx(94);
}

.profile-card {
  display: flex;
  align-items: center;
  min-height: design-rpx(104);
}

.avatar-wrap {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(86);
  height: design-rpx(86);
  overflow: hidden;
  background: transparent;
  border-radius: 999rpx;
}

.avatar-image {
  display: block;
  width: 100%;
  height: 100%;
}

.profile-main {
  flex: 1;
  min-width: 0;
  margin-left: design-rpx(18);
}

.profile-name,
.profile-phone,
.balance-label-row,
.balance-value,
.action-title,
.action-desc,
.secondary-title,
.secondary-value {
  display: block;
}

.profile-name {
  color: #06133d;
  font-size: design-rpx(22);
  font-weight: 700;
  line-height: 1.2;
}

.profile-phone {
  margin-top: design-rpx(10);
  color: #6a7a8f;
  font-size: design-rpx(14);
  font-weight: 400;
  line-height: 1;
}

.balance-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  min-height: design-rpx(132);
  margin-top: design-rpx(10);
  overflow: hidden;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb 0%, #6ea8ff 58%, #a5c9ff 100%);
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(12) design-rpx(30) rgba(37, 99, 235, 0.15);
}

.balance-bg {
  position: absolute;
  z-index: 1;
  right: design-rpx(-12);
  bottom: design-rpx(-34);
  width: design-rpx(132);
  height: design-rpx(132);
  opacity: 0.88;
}

.balance-content {
  position: relative;
  z-index: 2;
  flex: 1;
  padding: design-rpx(28) design-rpx(20);
}

.balance-label-row {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
  color: rgba(255, 255, 255, 0.86);
  font-size: design-rpx(14);
  font-weight: 400;
  line-height: 1;
}

.balance-eye-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(24);
  height: design-rpx(24);
  padding: 0;
  line-height: 1;
  background: transparent;
  border: 0;
  border-radius: 999rpx;
}

.balance-eye-button::after {
  border: 0;
}

.balance-eye-icon {
  position: relative;
  width: design-rpx(18);
  height: design-rpx(11);
  border: design-rpx(1.8) solid rgba(255, 255, 255, 0.84);
  border-radius: 50%;
}

.balance-eye-icon::before {
  position: absolute;
  top: 50%;
  left: 50%;
  width: design-rpx(5);
  height: design-rpx(5);
  background: rgba(255, 255, 255, 0.84);
  border-radius: 999rpx;
  content: "";
  transform: translate(-50%, -50%);
}

.balance-eye-button:not(.is-visible) .balance-eye-icon::after {
  position: absolute;
  top: 50%;
  left: design-rpx(-2);
  width: design-rpx(22);
  height: design-rpx(2);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 999rpx;
  content: "";
  transform: rotate(-35deg);
}

.balance-value {
  margin-top: design-rpx(24);
  color: #ffffff;
  font-size: design-rpx(38);
  font-weight: 800;
  line-height: 1;
  letter-spacing: design-rpx(0.5);
}

.balance-button {
  position: relative;
  z-index: 3;
  flex-shrink: 0;
  min-width: design-rpx(88);
  height: design-rpx(42);
  margin: design-rpx(22) design-rpx(20) 0 0;
  padding: 0 design-rpx(20);
  color: #1677ff;
  font-size: design-rpx(16);
  font-weight: 600;
  line-height: design-rpx(42);
  background: #ffffff;
  border-radius: 999rpx;
}

.balance-wave {
  position: absolute;
  right: design-rpx(92);
  bottom: design-rpx(20);
  left: design-rpx(92);
  z-index: 1;
  height: design-rpx(34);
  opacity: 0.18;
  background:
    radial-gradient(70% 70% at 18% 50%, rgba(255, 255, 255, 0.7) 0%, rgba(255, 255, 255, 0) 68%),
    linear-gradient(12deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.32) 52%, rgba(255, 255, 255, 0) 100%);
  border-radius: 999rpx;
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

.secondary-row {
  min-height: design-rpx(74);
}

.row-divider {
  position: absolute;
  right: design-rpx(18);
  bottom: 0;
  left: design-rpx(84);
  height: design-rpx(0.5);
  background: #edf2f9;
}

.secondary-row .row-divider {
  left: design-rpx(18);
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

.action-title {
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 700;
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

.secondary-title {
  flex: 1;
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 400;
  line-height: 1;
}

.secondary-value {
  margin-right: design-rpx(14);
  color: #8a97ac;
  font-size: design-rpx(15);
  font-weight: 400;
  line-height: 1;
}
</style>
