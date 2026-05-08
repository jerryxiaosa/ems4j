<script setup lang="ts">
const DESIGN_WIDTH_PX = 390
const MINI_PROGRAM_WIDTH_RPX = 750
const toRpx = (px: number) => Number(((px * MINI_PROGRAM_WIDTH_RPX) / DESIGN_WIDTH_PX).toFixed(1))

const chartLines = [20, 40, 60, 80]
const trendSegments = [
  { left: toRpx(0), top: toRpx(109), width: toRpx(53), rotate: -25 },
  { left: toRpx(50), top: toRpx(86), width: toRpx(48), rotate: -17 },
  { left: toRpx(95), top: toRpx(73), width: toRpx(55), rotate: -38 },
  { left: toRpx(145), top: toRpx(41), width: toRpx(44), rotate: 23 },
  { left: toRpx(184), top: toRpx(57), width: toRpx(48), rotate: 24 },
  { left: toRpx(228), top: toRpx(77), width: toRpx(50), rotate: -31 }
]
const trendDots = [
  { left: toRpx(0), top: toRpx(109) },
  { left: toRpx(50), top: toRpx(86) },
  { left: toRpx(95), top: toRpx(73) },
  { left: toRpx(145), top: toRpx(41) },
  { left: toRpx(184), top: toRpx(57) },
  { left: toRpx(228), top: toRpx(77) },
  { left: toRpx(270), top: toRpx(53) }
]

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
</script>

<template>
  <view class="home-page">
    <view class="home-header">
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
              <view class="chevron down"></view>
            </view>
            <view class="hero-meta-item">
              <text>共 6 个电表</text>
              <view class="chevron right"></view>
            </view>
          </view>
          <view class="balance-block">
            <view class="balance-label">
              <text>当前余额 (元)</text>
              <view class="icon-eye">
                <view></view>
              </view>
            </view>
            <view class="balance-value">
              <text class="currency">¥</text>
              <text>1,234.56</text>
            </view>
          </view>
          <button class="recharge-button" @click="goRecharge">
            <text>去充值</text>
            <view class="arrow-line"></view>
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
            <text class="segment-item is-active">电量</text>
            <text class="segment-item">电费</text>
          </view>
        </view>

        <view class="chart-wrap">
          <view class="axis-labels">
            <text>kWh</text>
            <text>600</text>
            <text>450</text>
            <text>300</text>
            <text>150</text>
            <text>0</text>
          </view>
          <view class="chart-canvas">
            <view
              v-for="line in chartLines"
              :key="line"
              class="chart-grid-line"
              :style="{ top: `${line}%` }"
            ></view>
            <view
              v-for="segment in trendSegments"
              :key="`${segment.left}-${segment.top}`"
              class="chart-line"
              :style="{
                left: `${segment.left}rpx`,
                top: `${segment.top}rpx`,
                width: `${segment.width}rpx`,
                transform: `rotate(${segment.rotate}deg)`
              }"
            ></view>
            <view
              v-for="point in trendDots"
              :key="`${point.left}-${point.top}`"
              class="chart-point"
              :style="{ left: `${point.left}rpx`, top: `${point.top}rpx` }"
            ></view>
            <view class="chart-tip">
              <text class="tip-date">5月12日</text>
              <text class="tip-value">320 kWh</text>
            </view>
          </view>
          <view class="x-axis">
            <text>5/6</text>
            <text>5/7</text>
            <text>5/8</text>
            <text>5/9</text>
            <text>5/10</text>
            <text>5/11</text>
            <text>5/12</text>
          </view>
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
          <text class="more-link">查看全部›</text>
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

    <view class="tabbar">
      <view class="tab-item is-active">
        <image class="tab-icon-image" src="/static/icons/tab-home-active.svg" mode="aspectFit" />
        <text>首页</text>
      </view>
      <view class="tab-item">
        <image class="tab-icon-image" src="/static/icons/tab-recharge.svg" mode="aspectFit" />
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

// Stitch 设计稿宽度是 390px，小程序满屏宽度是 750rpx。
// 所有来自设计稿的尺寸必须按 1px = 750 / 390 rpx 换算，不能直接把 px 写成同数值 rpx。
@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.home-page {
  width: 750rpx;
  max-width: 100%;
  min-height: 100vh;
  padding-bottom: design-rpx(74);
  background: linear-gradient(180deg, #edf3f8 0%, #ffffff 100%);
  overflow-x: hidden;
}

.home-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: design-rpx(18) design-rpx(16) design-rpx(12);
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
.more-link,
.tab-item {
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
  width: 100%;
  height: calc(100vh - #{design-rpx(112)});
  padding: 0 design-rpx(16);
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

.icon-eye {
  position: relative;
  width: design-rpx(14);
  height: design-rpx(9);
  border: design-rpx(1.5) solid currentColor;
  border-radius: 50%;
}

.icon-eye view {
  position: absolute;
  top: design-rpx(2);
  left: design-rpx(4.5);
  width: design-rpx(4);
  height: design-rpx(4);
  background: currentColor;
  border-radius: 999rpx;
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

.arrow-line {
  position: relative;
  width: design-rpx(16);
  height: design-rpx(1.5);
  background: currentColor;
  border-radius: 999rpx;
}

.arrow-line::after {
  position: absolute;
  right: 0;
  top: design-rpx(-3);
  width: design-rpx(6);
  height: design-rpx(6);
  border-top: design-rpx(1.5) solid currentColor;
  border-right: design-rpx(1.5) solid currentColor;
  transform: rotate(45deg);
  content: "";
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
  height: design-rpx(224);
  margin-top: design-rpx(24);
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
  margin-bottom: design-rpx(14);
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

.tabbar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 50;
  display: flex;
  justify-content: space-between;
  padding: design-rpx(12) design-rpx(24) design-rpx(24);
  background: rgba(255, 255, 255, 0.94);
  border-top: design-rpx(0.5) solid #e7e7f3;
  backdrop-filter: blur(#{design-rpx(10)});
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: design-rpx(6);
  min-width: design-rpx(42);
  font-size: design-rpx(10);
  font-weight: 600;
  color: #6a7a8f;
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
