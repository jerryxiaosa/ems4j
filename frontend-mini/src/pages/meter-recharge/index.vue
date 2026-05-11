<script setup lang="ts">
import { computed, ref } from 'vue'
import AppTabBar from '@/components/common/AppTabBar.vue'

type MeterOption = {
  id: string
  room: string
  meterNo: string
  balance: string
}

const meterList: MeterOption[] = [
  { id: '101', room: '1 单元 101 室', meterNo: '01234567890123456789', balance: '48.20' },
  { id: '102', room: '1 单元 102 室', meterNo: '01234567890123456790', balance: '12.00' },
  { id: '201', room: '2 单元 201 室', meterNo: '01234567890123456791', balance: '76.50' },
  { id: '202', room: '2 单元 202 室', meterNo: '01234567890123456792', balance: '33.20' },
  { id: '301', room: '3 单元 301 室', meterNo: '01234567890123456793', balance: '15.80' },
  { id: '302', room: '3 单元 302 室', meterNo: '01234567890123456794', balance: '28.90' }
]

const rechargeAmount = ref('')
const selectedMeterId = ref(meterList[0].id)
const showMeterSheet = ref(false)

const selectedMeter = computed(() => meterList.find((meter) => meter.id === selectedMeterId.value) ?? meterList[0])

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
</script>

<template>
  <view class="meter-recharge-page">
    <view class="page-header">
      <button class="back-button" aria-label="返回" @click="handleBack">
        <view class="back-chevron"></view>
      </button>
      <text class="page-title">充值缴费</text>
      <view class="header-placeholder"></view>
    </view>

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
              <text>账户余额（元）</text>
              <view class="eye-icon"></view>
            </view>
            <view class="balance-value">
              <text class="currency-mark">¥</text>
              <text>1,234.56</text>
            </view>
          </view>
        </view>

        <view class="form-card">
          <text class="section-title">选择电表</text>
          <view class="selected-meter-card" @click="openMeterSheet">
            <view class="selected-check">
              <view></view>
            </view>
            <view class="meter-main">
              <view class="meter-title-row">
                <text class="meter-room">{{ selectedMeter.room }}</text>
                <view class="right-chevron light"></view>
              </view>
              <text class="meter-no">电表编号：{{ selectedMeter.meterNo }}</text>
            </view>
            <view class="meter-balance">
              <text class="balance-caption">当前余额</text>
              <text class="balance-amount">¥ {{ selectedMeter.balance }}</text>
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

    <view :class="['sheet-mask', showMeterSheet ? 'is-open' : '']" @click="closeMeterSheet">
      <view :class="['meter-sheet', showMeterSheet ? 'is-open' : '']" @click.stop>
        <view class="sheet-handle"></view>
        <text class="sheet-title">选择电表</text>
        <text class="sheet-subtitle">共 {{ meterList.length }} 个电表</text>

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
                <text class="option-balance-label">当前余额</text>
                <text class="option-balance-value">¥ {{ meter.balance }}</text>
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
  font-weight: 500;
  line-height: 1;
  transform: translateX(-50%);
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
  font-weight: 600;
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

.down-chevron,
.right-chevron {
  flex-shrink: 0;
  width: design-rpx(8);
  height: design-rpx(8);
  border-right: design-rpx(2) solid currentColor;
  border-bottom: design-rpx(2) solid currentColor;
}

.down-chevron {
  transform: rotate(45deg) translateY(design-rpx(-2));
}

.right-chevron {
  transform: rotate(-45deg);
}

.light {
  color: currentColor;
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

.eye-icon {
  position: relative;
  width: design-rpx(16);
  height: design-rpx(10);
  border: design-rpx(1.5) solid currentColor;
  border-radius: 50%;
}

.eye-icon::after {
  position: absolute;
  top: design-rpx(2.5);
  left: design-rpx(5.5);
  width: design-rpx(4);
  height: design-rpx(4);
  background: currentColor;
  border-radius: 999rpx;
  content: "";
}

.balance-value {
  display: flex;
  align-items: baseline;
  gap: design-rpx(6);
  margin-top: design-rpx(12);
  color: #06133d;
  font-size: design-rpx(30);
  font-weight: 800;
  line-height: 1;
}

.currency-mark {
  font-size: design-rpx(22);
  font-weight: 700;
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
  display: block;
  margin-top: design-rpx(8);
  font-size: design-rpx(18);
  font-weight: 800;
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
.option-balance-label,
.option-balance-value {
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
  margin-top: design-rpx(8);
  font-size: design-rpx(18);
  font-weight: 800;
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
