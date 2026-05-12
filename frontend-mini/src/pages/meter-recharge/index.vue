<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AccountHeroCard from '@/components/business/AccountHeroCard.vue'
import MeterSelectCard from '@/components/business/MeterSelectCard.vue'
import RechargeAmountCard from '@/components/business/RechargeAmountCard.vue'
import AppAmount from '@/components/common/AppAmount.vue'
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import AppTabBar from '@/components/common/AppTabBar.vue'
import { getRechargeInit } from '@/api/recharge'
import type { RechargeInitResponse, RechargeMeterOption } from '@/types/recharge'
import { miniRoute } from '@/utils/route'

type MeterOption = {
  id: number
  room: string
  meterNo: string
  balance: string
}

const rechargeAmount = ref('')
const selectedMeterId = ref<number>()
const showMeterSheet = ref(false)
const rechargeInit = ref<RechargeInitResponse>()

const formatMoney = (value?: number) => {
  return (value ?? 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const toMeterOption = (meter: RechargeMeterOption): MeterOption => ({
  id: meter.meterId,
  room: meter.location || meter.meterName,
  meterNo: meter.meterNo ?? '-',
  balance: formatMoney(meter.meterBalance)
})

const meterList = computed(() => rechargeInit.value?.meterOptionList?.map(toMeterOption) ?? [])
const selectedMeter = computed(() => meterList.value.find((meter) => meter.id === selectedMeterId.value) ?? meterList.value[0])
const accountName = computed(() => rechargeInit.value?.electricAccountName ?? '')
const meterCount = computed(() => meterList.value.length)
const serviceFeeRate = computed(() => rechargeInit.value?.serviceFeeRate ?? 0)

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

const goPayConfirm = () => {
  const amount = rechargeAmount.value || '200'
  const meterId = selectedMeter.value?.id

  uni.navigateTo({
    url: `${miniRoute.payConfirm}?amount=${encodeURIComponent(amount)}${meterId ? `&meterId=${encodeURIComponent(String(meterId))}` : ''}`
  })
}

const goPayRecord = () => {
  uni.navigateTo({
    url: miniRoute.payRecord
  })
}

const openMeterSheet = () => {
  showMeterSheet.value = true
}

const closeMeterSheet = () => {
  showMeterSheet.value = false
}

const selectMeter = (meter: MeterOption) => {
  selectedMeterId.value = meter.id
  closeMeterSheet()
}

const loadRechargeInit = async () => {
  try {
    const result = await getRechargeInit()
    rechargeInit.value = result
    selectedMeterId.value = result.selectedMeterId ?? result.meterOptionList?.[0]?.meterId
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '充值信息加载失败',
      icon: 'none'
    })
  }
}

onMounted(() => {
  loadRechargeInit()
})
</script>

<template>
  <view class="meter-recharge-page">
    <AppBackHeader title="充值缴费" @back="handleBack" />

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <AccountHeroCard
          :account-name="accountName"
          show-meter-count
          :meter-count="meterCount"
          @meter-click="openMeterSheet"
        />

        <MeterSelectCard
          :room="selectedMeter?.room ?? ''"
          :meter-no="selectedMeter?.meterNo ?? ''"
          :balance="selectedMeter?.balance ?? '0.00'"
          @click="openMeterSheet"
        />

        <RechargeAmountCard v-model:amount="rechargeAmount" :service-fee-rate="serviceFeeRate" />

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

    <view :class="['sheet-mask', showMeterSheet ? 'is-open' : '']" @click="closeMeterSheet">
      <view :class="['meter-sheet', showMeterSheet ? 'is-open' : '']" @click.stop>
        <view class="sheet-handle"></view>
        <text class="sheet-title">选择电表</text>
        <text class="sheet-subtitle">共 {{ meterCount }} 个电表</text>

        <scroll-view class="meter-list-scroll" scroll-y enhanced show-scrollbar="false">
          <view class="meter-option-list">
            <view
              v-for="meter in meterList"
              :key="meter.id"
              :class="['meter-option-card', meter.id === selectedMeterId ? 'is-selected' : '']"
              @click="selectMeter(meter)"
            >
              <view class="option-radio">
                <view></view>
              </view>
              <view class="option-main">
                <text class="option-room">{{ meter.room }}</text>
                <text class="option-meter-no">电表编号：{{ meter.meterNo }}</text>
              </view>
              <view class="option-balance">
                <text class="option-balance-label">电表余额</text>
                <view class="option-balance-value">
                  <AppAmount :value="meter.balance" size="small" />
                </view>
              </view>
            </view>
          </view>
        </scroll-view>

        <button class="sheet-cancel-button" @click="closeMeterSheet">
          <text>取消</text>
        </button>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.meter-recharge-page {
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

.sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  align-items: flex-end;
  pointer-events: none;
  background: rgba(5, 12, 30, 0);
  opacity: 0;
  transition: opacity 180ms ease, background-color 180ms ease;
}

.sheet-mask.is-open {
  pointer-events: auto;
  background: rgba(5, 12, 30, 0.42);
  opacity: 1;
}

.meter-sheet {
  width: 100%;
  max-height: 86vh;
  padding: design-rpx(16) design-rpx(22) calc(env(safe-area-inset-bottom) + #{design-rpx(22)});
  background: #ffffff;
  border-radius: design-rpx(28) design-rpx(28) 0 0;
  box-shadow: 0 design-rpx(-10) design-rpx(36) rgba(6, 19, 61, 0.12);
  transform: translateY(100%);
  transition: transform 220ms ease;
}

.meter-sheet.is-open {
  transform: translateY(0);
}

.sheet-handle {
  width: design-rpx(46);
  height: design-rpx(6);
  margin: 0 auto design-rpx(24);
  background: #d6dbe8;
  border-radius: 999rpx;
}

.sheet-title,
.sheet-subtitle {
  display: block;
}

.sheet-title {
  color: #06133d;
  font-size: design-rpx(20);
  font-weight: 700;
}

.sheet-subtitle {
  margin-top: design-rpx(12);
  color: #8a97ac;
  font-size: design-rpx(15);
  font-weight: 500;
}

.meter-list-scroll {
  max-height: design-rpx(500);
  margin-top: design-rpx(18);
}

.meter-option-list {
  padding-bottom: design-rpx(10);
}

.meter-option-card {
  display: flex;
  align-items: center;
  gap: design-rpx(12);
  min-height: design-rpx(82);
  padding: design-rpx(14) design-rpx(12);
  margin-bottom: design-rpx(10);
  color: #06133d;
  background: #ffffff;
  border: design-rpx(0.5) solid #e0e5f0;
  border-radius: design-rpx(12);
}

.meter-option-card.is-selected {
  color: #ffffff;
  background: linear-gradient(135deg, #438bff 0%, #1172ff 100%);
  border-color: transparent;
  box-shadow: 0 design-rpx(8) design-rpx(18) rgba(22, 119, 255, 0.2);
}

.option-radio {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(28);
  height: design-rpx(28);
  border: design-rpx(2) solid #d8deeb;
  border-radius: 999rpx;
}

.meter-option-card.is-selected .option-radio {
  background: #ffffff;
  border-color: #ffffff;
}

.option-radio view {
  display: none;
  width: design-rpx(11);
  height: design-rpx(7);
  border-bottom: design-rpx(3) solid #1677ff;
  border-left: design-rpx(3) solid #1677ff;
  transform: rotate(-45deg) translate(design-rpx(1), design-rpx(-1));
}

.meter-option-card.is-selected .option-radio view {
  display: block;
}

.option-main {
  flex: 1;
  min-width: 0;
}

.option-room,
.option-meter-no,
.option-balance-label {
  display: block;
}

.option-room {
  overflow: hidden;
  font-size: design-rpx(17);
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-meter-no {
  margin-top: design-rpx(8);
  overflow: hidden;
  color: #8a97ac;
  font-size: design-rpx(13);
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meter-option-card.is-selected .option-meter-no {
  color: rgba(255, 255, 255, 0.9);
}

.option-balance {
  flex-shrink: 0;
  min-width: design-rpx(82);
  text-align: right;
}

.option-balance-label {
  color: #8a97ac;
  font-size: design-rpx(12);
  font-weight: 500;
}

.option-balance-value {
  display: flex;
  justify-content: flex-end;
  margin-top: design-rpx(8);
}

.meter-option-card.is-selected .option-balance-label {
  color: rgba(255, 255, 255, 0.9);
}

.sheet-cancel-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: design-rpx(56);
  margin-top: design-rpx(14);
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 700;
  background: #ffffff;
  border: design-rpx(0.5) solid #e0e5f0;
  border-radius: 999rpx;
}
</style>
