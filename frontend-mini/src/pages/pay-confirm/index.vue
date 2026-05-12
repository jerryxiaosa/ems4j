<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import AccountHeroCard from '@/components/business/AccountHeroCard.vue'
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import { createTopUpOrder } from '@/api/order'
import { getRechargeInit } from '@/api/recharge'
import type { TopUpOrderResponse } from '@/types/order'
import type { RechargeInitResponse } from '@/types/recharge'
import { miniRoute } from '@/utils/route'

const rechargeAmount = ref('200')
const selectedMeterId = ref<number>()
const hasAgreed = ref(true)
const isBalanceVisible = ref(true)
const rechargeInit = ref<RechargeInitResponse>()
const topUpOrder = ref<TopUpOrderResponse>()

const formatMoney = (value?: number) => {
  return (value ?? 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const normalizeAmount = (value: string) => {
  const numericValue = Number(value)

  if (!Number.isFinite(numericValue) || numericValue <= 0) {
    return 200
  }

  return numericValue
}

const accountName = computed(() => rechargeInit.value?.electricAccountName ?? '')
const accountBalance = computed(() => formatMoney(rechargeInit.value?.accountBalance))
const rechargeAmountText = computed(() => formatMoney(topUpOrder.value?.payAmount ?? normalizeAmount(rechargeAmount.value)))
const totalAmountText = computed(() => formatMoney(topUpOrder.value?.payAmount ?? normalizeAmount(rechargeAmount.value)))
const serviceFeeAmount = computed(() => formatMoney(topUpOrder.value?.serviceFeeAmount))

const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  uni.redirectTo({
    url: miniRoute.recharge
  })
}

const toggleAgreement = () => {
  hasAgreed.value = !hasAgreed.value
}

const toggleBalanceVisible = () => {
  isBalanceVisible.value = !isBalanceVisible.value
}

const openUserAgreement = () => {
  uni.navigateTo({
    url: miniRoute.userAgreement
  })
}

const openPrivacyPolicy = () => {
  uni.navigateTo({
    url: miniRoute.privacyPolicy
  })
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
    url: miniRoute.paySuccess
  })
}

const loadPayConfirmData = async () => {
  try {
    const payAmount = normalizeAmount(rechargeAmount.value)
    const [init, order] = await Promise.all([
      getRechargeInit(),
      createTopUpOrder({
        payAmount,
        meterId: selectedMeterId.value
      })
    ])

    rechargeInit.value = init
    topUpOrder.value = order
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '订单信息加载失败',
      icon: 'none'
    })
  }
}

onLoad((query) => {
  const amount = query?.amount
  const meterId = query?.meterId

  if (typeof amount === 'string' && amount.trim()) {
    rechargeAmount.value = amount
  }

  if (typeof meterId === 'string' && meterId.trim()) {
    const numericMeterId = Number(meterId)
    selectedMeterId.value = Number.isFinite(numericMeterId) ? numericMeterId : undefined
  }

  loadPayConfirmData()
})
</script>

<template>
  <view class="pay-confirm-page">
    <AppBackHeader title="确认支付" @back="handleBack" />

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <AccountHeroCard
          :account-name="accountName"
          :balance="accountBalance"
          show-balance
          :balance-visible="isBalanceVisible"
          @toggle-balance="toggleBalanceVisible"
        />

        <view class="section-card payment-card">
          <text class="section-title">支付方式</text>
          <view class="wechat-row">
            <view class="wechat-icon-wrap">
              <image class="wechat-icon" src="/static/icons/wechat-white.svg" mode="aspectFit" />
            </view>
            <view class="wechat-copy">
              <text class="wechat-title">微信支付</text>
              <text class="wechat-balance">点击下方按钮后拉起微信支付</text>
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
          <text class="agreement-link" @click="openUserAgreement">《用户服务协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link" @click="openPrivacyPolicy">《隐私政策》</text>
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

.page-scroll {
  flex: 1;
  min-height: 0;
}

.content-stack {
  padding: design-rpx(16) design-rpx(22) design-rpx(148);
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
  font-weight: 500;
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
