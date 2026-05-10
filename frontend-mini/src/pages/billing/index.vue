<script setup lang="ts">
import { ref } from 'vue'
import AppTabBar from '@/components/common/AppTabBar.vue'

type BillMonth = {
  month: string
  tag?: string
  amount?: string
  usage?: string
}

const filterOptions = ['全部', '本年', '近6月', '近3月']
const activeFilter = ref(filterOptions[0])

const billMonths: BillMonth[] = [
  { month: '2024年12月', tag: '本月结算中' },
  { month: '2024年11月', amount: '1,534.45', usage: '215.43' },
  { month: '2024年10月', amount: '1,856.23', usage: '263.12' },
  { month: '2024年9月', amount: '1,245.33', usage: '198.67' },
  { month: '2024年8月', amount: '1,650.00', usage: '205.21' },
  { month: '2024年6月', amount: '1,503.45', usage: '230.14' },
  { month: '2024年5月', amount: '1,245.30', usage: '192.56' }
]

const handleBack = () => {
  uni.redirectTo({
    url: '/pages/home/index'
  })
}

const selectFilter = (filter: string) => {
  activeFilter.value = filter
}

const openBillDetail = () => {
  uni.showToast({
    title: '账单明细待接入',
    icon: 'none'
  })
}
</script>

<template>
  <view class="billing-page">
    <view class="page-header">
      <button class="back-button" aria-label="返回" @click="handleBack">
        <view class="back-chevron"></view>
      </button>
      <text class="page-title">账单</text>
      <view class="header-placeholder"></view>
    </view>

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="filter-segment">
          <button
            v-for="filter in filterOptions"
            :key="filter"
            :class="['filter-item', activeFilter === filter ? 'is-active' : '']"
            @click="selectFilter(filter)"
          >
            <text>{{ filter }}</text>
          </button>
        </view>

        <view class="summary-card">
          <view class="summary-item">
            <view class="summary-head">
              <view class="summary-icon energy-icon">
                <image class="summary-icon-image" src="/static/icons/energy.svg" mode="aspectFit" />
              </view>
              <text>总用电量（kWh）</text>
            </view>
            <view class="summary-value usage-value">
              <text>1,544.63</text>
            </view>
            <view class="summary-accent usage-accent"></view>
          </view>

          <view class="summary-divider"></view>

          <view class="summary-item">
            <view class="summary-head">
              <view class="summary-icon money-icon">
                <text>¥</text>
              </view>
              <text>总电费（元）</text>
            </view>
            <view class="summary-value cost-value">
              <text class="currency">¥</text>
              <text>12,345.56</text>
            </view>
            <view class="summary-accent cost-accent"></view>
          </view>
        </view>

        <text class="year-title">2024年</text>

        <view class="bill-list">
          <view v-for="bill in billMonths" :key="bill.month" class="bill-card" @click="openBillDetail">
            <view class="month-row">
              <view class="month-bill-icon"></view>
              <text class="month-title">{{ bill.month }}</text>
              <text v-if="bill.tag" class="month-tag">{{ bill.tag }}</text>
            </view>

            <view class="bill-metrics">
              <view class="metric-block">
                <view class="metric-copy">
                  <text class="metric-label">用电量</text>
                  <text class="metric-value">{{ bill.usage ?? '--' }} kWh</text>
                </view>
              </view>

              <view class="card-divider"></view>

              <view class="metric-block">
                <view class="metric-copy">
                  <text class="metric-label">电费</text>
                  <text class="metric-value">¥ {{ bill.amount ?? '--' }}</text>
                </view>
              </view>

              <view class="right-chevron"></view>
            </view>
          </view>
        </view>

        <text class="end-text">没有更多了</text>
      </view>
    </scroll-view>

    <AppTabBar active="billing" />
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.billing-page {
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
  padding: design-rpx(16) design-rpx(22) design-rpx(88);
}

.filter-segment {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: design-rpx(4);
  padding: design-rpx(4);
  background: #eef4fb;
  border-radius: design-rpx(18);
}

.filter-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: design-rpx(36);
  color: #5d6f8c;
  font-size: design-rpx(14);
  font-weight: 600;
  line-height: 1;
  border-radius: design-rpx(14);
}

.filter-item.is-active {
  color: #1677ff;
  background: #ffffff;
  box-shadow: 0 design-rpx(4) design-rpx(12) rgba(6, 19, 61, 0.06);
}

.summary-card {
  display: flex;
  box-sizing: border-box;
  align-items: stretch;
  width: 100%;
  margin-top: design-rpx(16);
  padding: design-rpx(20) design-rpx(16) design-rpx(18);
  background: #ffffff;
  border: design-rpx(1) solid rgba(219, 228, 242, 0.7);
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.05);
}

.summary-item {
  position: relative;
  flex: 1;
  min-width: 0;
  padding-bottom: design-rpx(14);
}

.summary-divider {
  width: design-rpx(1);
  margin: 0 design-rpx(14);
  background: #edf1f7;
}

.summary-head {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
  color: #62708f;
  font-size: design-rpx(13);
  font-weight: 600;
}

.summary-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(22);
  height: design-rpx(22);
  color: #ffffff;
  font-size: design-rpx(12);
  font-weight: 800;
  border-radius: 999rpx;
}

.energy-icon {
  background: #2563eb;
  box-shadow: 0 design-rpx(4) design-rpx(10) rgba(37, 99, 235, 0.2);
}

.money-icon {
  background: #14b86a;
  box-shadow: 0 design-rpx(4) design-rpx(10) rgba(20, 184, 106, 0.2);
}

.summary-icon text {
  display: block;
  line-height: 1;
}

.summary-icon-image {
  width: design-rpx(14);
  height: design-rpx(14);
}

.summary-value {
  display: flex;
  align-items: baseline;
  gap: design-rpx(3);
  margin-top: design-rpx(12);
  font-size: design-rpx(24);
  font-weight: 800;
  line-height: 1;
}

.currency {
  font-size: design-rpx(15);
}

.cost-value {
  color: #152234;
}

.usage-value {
  color: #152234;
}

.summary-accent {
  position: absolute;
  bottom: 0;
  left: 0;
  width: design-rpx(74);
  height: design-rpx(4);
  border-radius: 999rpx;
}

.cost-accent {
  background: linear-gradient(90deg, #14b86a 0%, rgba(20, 184, 106, 0) 100%);
}

.usage-accent {
  background: linear-gradient(90deg, #2563eb 0%, rgba(37, 99, 235, 0) 100%);
}

.year-title {
  display: block;
  margin: design-rpx(22) 0 design-rpx(12);
  color: #152234;
  font-size: design-rpx(18);
  font-weight: 700;
}

.bill-list {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  width: 100%;
  gap: design-rpx(12);
}

.bill-card {
  box-sizing: border-box;
  width: 100%;
  padding: design-rpx(16);
  background: #ffffff;
  border: design-rpx(1) solid rgba(219, 228, 242, 0.72);
  border-radius: design-rpx(20);
}

.month-row {
  display: flex;
  align-items: center;
  gap: design-rpx(8);
}

.month-bill-icon {
  position: relative;
  flex-shrink: 0;
  width: design-rpx(18);
  height: design-rpx(18);
  background: rgba(22, 119, 255, 0.06);
  border: design-rpx(1.2) solid rgba(22, 119, 255, 0.5);
  border-radius: design-rpx(5);
}

.month-bill-icon::before,
.month-bill-icon::after {
  position: absolute;
  left: design-rpx(4);
  height: design-rpx(1.2);
  background: rgba(22, 119, 255, 0.55);
  border-radius: 999rpx;
  content: "";
}

.month-bill-icon::before {
  top: design-rpx(6);
  width: design-rpx(9);
}

.month-bill-icon::after {
  top: design-rpx(11);
  width: design-rpx(6);
}

.month-title {
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 300;
  line-height: 1;
}

.month-tag {
  padding: design-rpx(3) design-rpx(8);
  color: #1677ff;
  font-size: design-rpx(10);
  font-weight: 700;
  line-height: 1;
  background: #eaf3ff;
  border-radius: 999rpx;
}

.bill-metrics {
  display: grid;
  grid-template-columns: 1fr design-rpx(1) 1fr design-rpx(14);
  gap: design-rpx(12);
  align-items: center;
  margin-top: design-rpx(16);
}

.metric-block {
  display: flex;
  align-items: center;
  min-width: 0;
}

.metric-copy {
  display: flex;
  flex-direction: column;
  gap: design-rpx(5);
  min-width: 0;
}

.metric-label {
  color: #7b879a;
  font-size: design-rpx(12);
  font-weight: 500;
  line-height: 1;
}

.metric-value {
  color: #152234;
  font-size: design-rpx(14);
  font-weight: 300;
  line-height: 1;
  white-space: nowrap;
}

.card-divider {
  width: design-rpx(1);
  height: design-rpx(32);
  background: #edf1f7;
}

.right-chevron {
  width: design-rpx(9);
  height: design-rpx(9);
  border-top: design-rpx(2) solid #bdc7d8;
  border-right: design-rpx(2) solid #bdc7d8;
  transform: rotate(45deg);
}

.end-text {
  display: block;
  margin-top: design-rpx(22);
  color: #a1acbd;
  font-size: design-rpx(12);
  font-weight: 500;
  text-align: center;
}

</style>
