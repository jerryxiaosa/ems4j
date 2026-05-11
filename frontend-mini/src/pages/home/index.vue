<script setup lang="ts">
import { computed, ref } from 'vue'
import AppTabBar from '@/components/common/AppTabBar.vue'
import TrendLineChart from '@/components/chart/TrendLineChart.vue'

type TrendType = 'usage' | 'cost'

const DESIGN_WIDTH = 390

const designPxToRpx = (px: number) => {
  return (px * 750) / DESIGN_WIDTH
}

const runtimePxToRpx = (px: number, windowWidth: number) => {
  return (px * 750) / windowWidth
}

const createHomeHeaderPaddingTop = () => {
  const designTopRpx = designPxToRpx(48)

  try {
    const systemInfo = uni.getSystemInfoSync()
    const statusBarHeight = systemInfo.statusBarHeight ?? 0
    const windowWidth = systemInfo.windowWidth || DESIGN_WIDTH

    if (!statusBarHeight) {
      return `${designTopRpx}rpx`
    }

    // Do not stack env(safe-area-inset-top) with the design offset.
    // On real devices that double offset pushes the title too far down.
    const safeTopRpx = runtimePxToRpx(statusBarHeight + 8, windowWidth)
    return `${Math.max(designTopRpx, safeTopRpx)}rpx`
  } catch {
    return `${designTopRpx}rpx`
  }
}

const homeHeaderStyle = {
  paddingTop: createHomeHeaderPaddingTop()
}

const activeTrendType = ref<TrendType>('usage')
const isBalanceVisible = ref(true)

const trendCategories = ['5/6', '5/7', '5/8', '5/9', '5/10', '5/11', '5/12']
const trendDataMap: Record<TrendType, { label: string; seriesName: string; unit: string; max: number; values: number[] }> = {
  usage: {
    label: '电量',
    seriesName: '电量',
    unit: 'kWh',
    max: 12000,
    values: [3600, 5200, 6400, 9600, 8200, 6000, 8800]
  },
  cost: {
    label: '电费',
    seriesName: '电费',
    unit: '元',
    max: 12000,
    values: [2520, 3640, 4480, 6720, 5740, 4200, 5880]
  }
}
const trendTypes: TrendType[] = ['usage', 'cost']
const activeTrend = computed(() => trendDataMap[activeTrendType.value])

const summaryCards = [
  {
    label: '上月总电量',
    value: '3,268',
    unit: 'kWh',
    trend: '较上月 12.5%',
    trendClass: 'is-up',
    waveSrc: '/static/icons/summary-wave-usage.svg'
  },
  {
    label: '上月总电费',
    value: '¥ 2,186.40',
    unit: '',
    trend: '较上月 8.3%',
    trendClass: 'is-down',
    waveSrc: '/static/icons/summary-wave-cost.svg'
  }
]

const goRecharge = () => {
  uni.navigateTo({
    url: '/pages/recharge/index'
  })
}

const goPayRecord = () => {
  uni.navigateTo({
    url: '/pages/pay-record/index'
  })
}

const goMeterList = () => {
  uni.navigateTo({
    url: '/pages/meter/index'
  })
}

const switchTrendType = (trendType: TrendType) => {
  activeTrendType.value = trendType
}

const toggleBalanceVisible = () => {
  isBalanceVisible.value = !isBalanceVisible.value
}
</script>

<template>
  <view class="home-page">
    <view class="home-header" :style="homeHeaderStyle">
      <view>
        <text class="home-title">EMS4J 能耗管理系统</text>
        <text class="home-subtitle">智慧用能 · 节能高效</text>
      </view>
    </view>

    <scroll-view class="home-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="hero-card">
        <image class="hero-image" src="/static/stitch/hero-building.png" mode="aspectFit" />
        <view class="hero-content">
          <view class="hero-meta">
            <view class="hero-meta-item">
              <text>星河家园 2 栋住户账户</text>
            </view>
            <view class="hero-meta-item" @click="goMeterList">
              <text>共 6 个电表</text>
              <view class="chevron right"></view>
            </view>
          </view>
          <view class="balance-block">
            <view class="balance-label">
              <text>当前余额 (元)</text>
              <uni-icons
                class="balance-eye-icon"
                :type="isBalanceVisible ? 'eye' : 'eye-slash'"
                :size="18"
                color="#ffffff"
                @click="toggleBalanceVisible"
              />
            </view>
            <view class="balance-value">
              <template v-if="isBalanceVisible">
                <text class="currency">¥</text>
                <text>1,234.56</text>
              </template>
              <text v-else>--</text>
            </view>
          </view>
          <button class="recharge-button" @click="goRecharge">
            <text>去充值</text>
            <text class="recharge-chevron">›</text>
          </button>
        </view>
      </view>

      <view class="card trend-card">
        <view class="card-head">
          <view>
            <text class="card-title">近七日用能趋势</text>
            <text class="title-underline"></text>
          </view>
          <view class="segment">
            <text
              v-for="trendType in trendTypes"
              :key="trendType"
              :class="['segment-item', activeTrendType === trendType ? 'is-active' : '']"
              @click="switchTrendType(trendType)"
            >
              {{ trendDataMap[trendType].label }}
            </text>
          </view>
        </view>

        <view class="chart-wrap">
          <TrendLineChart
            canvas-id="homeTrendChart"
            :categories="trendCategories"
            :values="activeTrend.values"
            :series-name="activeTrend.seriesName"
            :unit="activeTrend.unit"
            :max="activeTrend.max"
          />
        </view>
      </view>

      <view class="summary-grid">
        <view v-for="item in summaryCards" :key="item.label" :class="['summary-card', item.trendClass]">
          <view class="summary-head">
            <view :class="['summary-icon', item.trendClass === 'is-up' ? 'energy-icon' : 'money-icon']">
              <image
                v-if="item.trendClass === 'is-up'"
                class="summary-icon-image"
                src="/static/icons/energy.svg"
                mode="aspectFit"
              />
              <text v-else>¥</text>
            </view>
            <text class="summary-label">{{ item.label }}</text>
          </view>
          <view class="summary-value">
            <text>{{ item.value }}</text>
            <text v-if="item.unit" class="summary-unit">{{ item.unit }}</text>
          </view>
          <view :class="['summary-trend', item.trendClass]">
            <view class="trend-arrow"></view>
            <text>{{ item.trend }}</text>
          </view>
          <image class="summary-wave" :src="item.waveSrc" mode="scaleToFill" />
        </view>
      </view>

      <view class="card recent-card">
        <view class="card-head compact">
          <text class="card-title">最近充值缴费</text>
          <text class="more-link" @click="goPayRecord">查看全部›</text>
        </view>
        <view class="recent-row">
          <view class="recent-icon">
            <image class="recent-icon-image" src="/static/icons/wechat.svg" mode="aspectFit" />
          </view>
          <view class="recent-main">
            <view class="recent-title-row">
              <text class="recent-title">微信充值</text>
              <text class="recent-amount">¥200.00</text>
            </view>
            <text class="recent-sub">到账 ¥198.00 · 支付成功 · 04-23 10:24</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <AppTabBar active="home" />
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

// Stitch 设计稿宽度是 390px，小程序满屏宽度是 750rpx。
// 所有来自设计稿的尺寸必须按 1px = 750 / 390 rpx 换算，不能直接把 px 写成同数值 rpx。
@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.home-page {
  display: flex;
  flex-direction: column;
  width: 750rpx;
  max-width: 100%;
  height: 100vh;
  background: linear-gradient(180deg, #edf3f8 0%, #ffffff 100%);
  overflow: hidden;
}

.home-header {
  display: flex;
  flex-shrink: 0;
  justify-content: flex-start;
  align-items: center;
  padding: design-rpx(48) design-rpx(16) design-rpx(12);
}

.home-title,
.home-subtitle,
.card-title,
.summary-label,
.summary-value,
.summary-trend,
.recent-title,
.recent-sub,
.recent-amount,
.more-link {
  display: block;
}

.home-title {
  font-size: design-rpx(20);
  font-weight: 700;
  line-height: 1.4;
  color: #152234;
}

.home-subtitle {
  margin-top: design-rpx(2);
  font-size: design-rpx(12);
  color: #6a7a8f;
}

.home-scroll {
  flex: 1;
  min-height: 0;
  width: 100%;
  height: 0;
  padding: 0 design-rpx(16) design-rpx(84);
  box-sizing: border-box;
  overflow-x: hidden;
}

.hero-card {
  position: relative;
  width: 100%;
  min-height: design-rpx(184);
  padding: design-rpx(20);
  overflow: hidden;
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb 0%, #a5c9ff 100%);
  border-radius: design-rpx(24);
  box-shadow: 0 design-rpx(12) design-rpx(30) rgba(37, 99, 235, 0.15);
}

.hero-image {
  position: absolute;
  right: design-rpx(-82);
  bottom: design-rpx(-126);
  width: design-rpx(380);
  height: design-rpx(380);
  opacity: 0.95;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-meta {
  display: flex;
  justify-content: space-between;
  gap: design-rpx(8);
  font-size: design-rpx(12);
  font-weight: 600;
  opacity: 0.9;
}

.hero-meta-item {
  display: flex;
  align-items: center;
  gap: design-rpx(4);
  min-width: 0;
}

.hero-meta-item:first-child {
  flex: 1;
}

.hero-meta-item:last-child {
  flex-shrink: 0;
}

.hero-meta-item text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chevron {
  width: design-rpx(7);
  height: design-rpx(7);
  border-right: design-rpx(1.5) solid currentColor;
  border-bottom: design-rpx(1.5) solid currentColor;
}

.chevron.down {
  margin-top: design-rpx(-2);
  transform: rotate(45deg);
}

.chevron.right {
  transform: rotate(-45deg);
}

.balance-block {
  margin: design-rpx(16) 0 design-rpx(24);
}

.balance-label {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
  margin-bottom: design-rpx(4);
  font-size: design-rpx(12);
  opacity: 0.82;
}

.balance-eye-icon {
  opacity: 0.92;
  transition: opacity 120ms ease, transform 120ms ease;
}

.balance-eye-icon:active {
  transform: scale(0.92);
}

.balance-value {
  display: flex;
  align-items: baseline;
  gap: design-rpx(2);
  font-size: design-rpx(38);
  font-weight: 800;
  line-height: 1.08;
}

.currency {
  font-size: design-rpx(24);
  font-weight: 700;
}

.recharge-button {
  display: inline-flex;
  align-items: center;
  gap: design-rpx(8);
  height: design-rpx(42);
  padding: 0 design-rpx(24);
  color: #004ac6;
  font-size: design-rpx(14);
  font-weight: 800;
  background: #ffffff;
  border-radius: 999rpx;
  box-shadow: 0 design-rpx(6) design-rpx(15) rgba(12, 43, 71, 0.12);
}

.recharge-chevron {
  display: block;
  margin-left: design-rpx(-2);
  font-size: design-rpx(22);
  font-weight: 500;
  line-height: 1;
}

.card {
  width: 100%;
  margin-top: design-rpx(14);
  padding: design-rpx(20);
  background: #ffffff;
  border-radius: design-rpx(24);
  box-shadow: 0 design-rpx(4) design-rpx(20) rgba(12, 43, 71, 0.04);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: design-rpx(12);
}

.card-head.compact {
  align-items: center;
}

.card-title {
  font-size: design-rpx(16);
  font-weight: 700;
  color: #152234;
}

.title-underline {
  display: block;
  width: design-rpx(24);
  height: design-rpx(4);
  margin-top: design-rpx(4);
  background: #004ac6;
  border-radius: 999rpx;
}

.segment {
  display: flex;
  padding: design-rpx(2);
  background: #ededf9;
  border-radius: 999rpx;
}

.segment-item {
  min-width: design-rpx(40);
  padding: design-rpx(4) design-rpx(16);
  font-size: design-rpx(12);
  font-weight: 600;
  color: #6a7a8f;
  text-align: center;
  border-radius: 999rpx;
}

.segment-item.is-active {
  color: #ffffff;
  background: #004ac6;
  box-shadow: 0 design-rpx(2) design-rpx(6) rgba(0, 74, 198, 0.18);
}

.chart-wrap {
  position: relative;
  margin-top: design-rpx(24);
  transform: translateX(design-rpx(-14));
}

.axis-labels {
  position: absolute;
  top: 0;
  bottom: design-rpx(24);
  left: 0;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  font-size: design-rpx(10);
  color: #6a7a8f;
}

.chart-canvas {
  position: absolute;
  inset: 0 0 design-rpx(24) design-rpx(32);
}

.chart-grid-line {
  position: absolute;
  right: 0;
  left: 0;
  height: design-rpx(0.5);
  background: #f1f5f9;
}

.chart-line {
  position: absolute;
  height: design-rpx(2.5);
  background: #004ac6;
  border-radius: 999rpx;
  transform-origin: left center;
}

.chart-point {
  position: absolute;
  width: design-rpx(8);
  height: design-rpx(8);
  margin: design-rpx(-4) 0 0 design-rpx(-4);
  background: #ffffff;
  border: design-rpx(2) solid #004ac6;
  border-radius: 999rpx;
  box-shadow: 0 design-rpx(2) design-rpx(6) rgba(0, 74, 198, 0.16);
}

.chart-tip {
  position: absolute;
  top: design-rpx(18);
  right: 15%;
  padding: design-rpx(8) design-rpx(12);
  background: #ffffff;
  border: design-rpx(0.5) solid #e7e7f3;
  border-radius: design-rpx(12);
  box-shadow: 0 design-rpx(7) design-rpx(18) rgba(12, 43, 71, 0.12);
}

.tip-date,
.tip-value {
  display: block;
}

.tip-date {
  margin-bottom: design-rpx(1);
  font-size: design-rpx(10);
  color: #6a7a8f;
}

.tip-value {
  font-size: design-rpx(12);
  font-weight: 800;
  color: #152234;
}

.x-axis {
  position: absolute;
  right: 0;
  bottom: 0;
  left: design-rpx(32);
  display: flex;
  justify-content: space-between;
  font-size: design-rpx(10);
  color: #6a7a8f;
}

.summary-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: design-rpx(12);
  margin-top: design-rpx(14);
}

.summary-card {
  position: relative;
  min-height: design-rpx(124);
  padding: design-rpx(16);
  overflow: hidden;
  background: #f3f3fe;
  border: design-rpx(0.5) solid rgba(195, 198, 215, 0.35);
  border-radius: design-rpx(24);
}

.summary-card.is-down {
  background: #eefcf7;
  border-color: rgba(20, 184, 106, 0.1);
}

.summary-head {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: design-rpx(10);
  margin-bottom: design-rpx(12);
}

.summary-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(36);
  height: design-rpx(36);
  color: #ffffff;
  font-size: design-rpx(16);
  font-weight: 800;
  background: #2563eb;
  border-radius: 999rpx;
  box-shadow: 0 design-rpx(4) design-rpx(10) rgba(37, 99, 235, 0.2);
}

.summary-icon text {
  display: block;
  line-height: 1;
}

.summary-icon-image {
  width: design-rpx(22);
  height: design-rpx(22);
}

.money-icon {
  background: #14b86a;
}

.summary-label {
  font-size: design-rpx(12);
  font-weight: 700;
  color: #6a7a8f;
}

.summary-value {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: baseline;
  gap: design-rpx(3);
  margin-bottom: design-rpx(4);
  font-size: design-rpx(20);
  font-weight: 800;
  color: #152234;
}

.summary-unit {
  font-size: design-rpx(11);
  font-weight: 400;
  color: #6a7a8f;
}

.summary-trend {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: design-rpx(4);
  font-size: design-rpx(10);
  font-weight: 700;
}

.summary-trend.is-up {
  color: #ef4444;
}

.summary-trend.is-down {
  color: #14b86a;
}

.trend-arrow {
  width: 0;
  height: 0;
  border-right: design-rpx(4) solid transparent;
  border-left: design-rpx(4) solid transparent;
}

.summary-trend.is-up .trend-arrow {
  border-bottom: design-rpx(8) solid #ef4444;
}

.summary-trend.is-down .trend-arrow {
  border-top: design-rpx(8) solid #14b86a;
}

.summary-wave {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1;
  width: 100%;
  height: design-rpx(34);
  opacity: 0.1;
}

.recent-card {
  margin-bottom: design-rpx(30);
}

.more-link {
  font-size: design-rpx(12);
  font-weight: 600;
  color: #6a7a8f;
}

.recent-row {
  display: flex;
  gap: design-rpx(12);
  align-items: center;
  margin-top: design-rpx(16);
}

.recent-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(48);
  height: design-rpx(48);
  color: #14b86a;
  background: rgba(20, 184, 106, 0.1);
  border-radius: 999rpx;
}

.recent-icon-image {
  width: design-rpx(28);
  height: design-rpx(28);
}

.recent-main {
  flex: 1;
  min-width: 0;
}

.recent-title-row {
  display: flex;
  justify-content: space-between;
  gap: design-rpx(10);
  align-items: center;
}

.recent-title,
.recent-amount {
  font-size: design-rpx(14);
  font-weight: 800;
  color: #152234;
}

.recent-sub {
  margin-top: design-rpx(4);
  font-size: design-rpx(11);
  color: #6a7a8f;
}

</style>
