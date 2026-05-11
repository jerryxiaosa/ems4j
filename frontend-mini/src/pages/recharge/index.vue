<script setup lang="ts">
import { ref } from 'vue'
import AccountHeroCard from '@/components/business/AccountHeroCard.vue'
import RechargeAmountCard from '@/components/business/RechargeAmountCard.vue'
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import AppTabBar from '@/components/common/AppTabBar.vue'
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
        <AccountHeroCard
          account-name="星河家园 2 栋住户账户"
          balance="1,234.56"
          show-balance
          :balance-visible="isBalanceVisible"
          @toggle-balance="toggleBalanceVisible"
        />

        <RechargeAmountCard v-model:amount="rechargeAmount" />

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
