<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

type DailyBill = {
  date: string
  weekday: string
  usage: string
  amount: string
}

const selectedMonth = ref('2024年11月')
const isNoData = ref(false)

const dailyBills: DailyBill[] = [
  { date: '11月30日', weekday: '周六', usage: '6.23', amount: '45.62' },
  { date: '11月29日', weekday: '周五', usage: '7.45', amount: '53.21' },
  { date: '11月28日', weekday: '周四', usage: '6.98', amount: '50.17' },
  { date: '11月27日', weekday: '周三', usage: '7.12', amount: '51.08' },
  { date: '11月26日', weekday: '周二', usage: '6.45', amount: '46.32' },
  { date: '11月25日', weekday: '周一', usage: '6.78', amount: '48.67' },
  { date: '11月24日', weekday: '周日', usage: '5.89', amount: '42.31' },
  { date: '11月23日', weekday: '周六', usage: '6.10', amount: '43.89' }
]

const totalUsage = '215.43'
const totalAmount = '1,534.45'

const hasDailyData = computed(() => {
  return !isNoData.value && dailyBills.length > 0
})

const visibleDailyBills = computed(() => {
  return hasDailyData.value ? dailyBills : []
})

const totalUsageText = computed(() => {
  return isNoData.value ? '--' : totalUsage
})

const totalAmountText = computed(() => {
  return isNoData.value ? '--' : totalAmount
})

const averageUsage = computed(() => {
  if (isNoData.value) {
    return '--'
  }

  return (Number(totalUsage) / 30).toFixed(2)
})

const averageAmount = computed(() => {
  if (isNoData.value) {
    return '--'
  }

  return (Number(totalAmount.replace(/,/g, '')) / 30).toFixed(2)
})

const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  uni.redirectTo({
    url: '/pages/billing/index'
  })
}

onLoad((query) => {
  if (query?.month) {
    selectedMonth.value = decodeURIComponent(String(query.month))
  }

  isNoData.value = query?.empty === '1'
})
</script>

<template>
  <view class="billing-detail-page">
    <view class="page-header">
      <button class="back-button" aria-label="返回" @click="handleBack">
        <view class="back-chevron"></view>
      </button>
      <text class="page-title">账单明细</text>
      <view class="header-placeholder"></view>
    </view>

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="month-selector">
          <text>{{ selectedMonth }}</text>
        </view>

        <view class="summary-card">
          <view class="summary-item">
            <view class="summary-head">
              <view class="summary-icon energy-icon">
                <image class="summary-icon-image" src="/static/icons/energy.svg" mode="aspectFit" />
              </view>
              <text class="summary-title">本月电量（kWh）</text>
            </view>
            <view class="summary-value usage-value">
              <text>{{ totalUsageText }}</text>
            </view>
            <text class="summary-sub">日均 {{ averageUsage }}</text>
            <view class="summary-accent usage-accent"></view>
          </view>

          <view class="summary-divider"></view>

          <view class="summary-item">
            <view class="summary-head">
              <view class="summary-icon money-icon">
                <text>¥</text>
              </view>
              <text class="summary-title">本月电费（元）</text>
            </view>
            <view class="summary-value cost-value">
              <text>{{ totalAmountText }}</text>
            </view>
            <text class="summary-sub">日均 {{ averageAmount }}</text>
            <view class="summary-accent cost-accent"></view>
          </view>
        </view>

        <view class="detail-header">
          <text class="section-title">日明细</text>
          <view class="table-head">
            <text>用电量（kWh）</text>
            <text>电费（元）</text>
          </view>
        </view>

        <view class="daily-card" :class="{ 'is-empty': !hasDailyData }">
          <view v-for="item in visibleDailyBills" :key="item.date" class="daily-row">
            <view class="date-cell">
              <text class="date-text">{{ item.date }}</text>
              <text class="weekday-text">{{ item.weekday }}</text>
            </view>
            <text class="usage-cell">{{ item.usage }}</text>
            <text class="amount-cell">¥ {{ item.amount }}</text>
          </view>

          <view v-if="!hasDailyData" class="empty-state">
            <image class="empty-image" src="/static/stitch/empty.png" mode="aspectFit" />
            <text class="empty-text">暂无数据</text>
          </view>
        </view>

        <text v-if="hasDailyData" class="end-text">已到底部</text>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.billing-detail-page {
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
  padding: design-rpx(12) design-rpx(22) design-rpx(34);
}

.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #435170;
  font-size: design-rpx(18);
  font-weight: 500;
}

.summary-card {
  display: flex;
  box-sizing: border-box;
  align-items: stretch;
  width: 100%;
  margin-top: design-rpx(22);
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

.summary-title {
  flex-shrink: 0;
  white-space: nowrap;
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
  color: #152234;
  font-size: design-rpx(24);
  font-weight: 800;
  line-height: 1;
}

.summary-sub {
  display: block;
  margin-top: design-rpx(12);
  color: #6a7a8f;
  font-size: design-rpx(12);
  font-weight: 500;
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

.detail-header {
  display: grid;
  grid-template-columns: 1fr design-rpx(210);
  align-items: end;
  margin: design-rpx(28) design-rpx(8) design-rpx(12);
}

.section-title {
  color: #06133d;
  font-size: design-rpx(20);
  font-weight: 700;
  line-height: 1;
}

.table-head {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: design-rpx(12);
  color: #62708f;
  font-size: design-rpx(13);
  font-weight: 500;
  text-align: right;
}

.daily-card {
  box-sizing: border-box;
  width: 100%;
  overflow: hidden;
  background: #ffffff;
  border: design-rpx(1) solid rgba(219, 228, 242, 0.72);
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.04);
}

.daily-card.is-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - #{design-rpx(390)});
}

.daily-row {
  display: grid;
  grid-template-columns: 1fr design-rpx(92) design-rpx(98);
  align-items: center;
  min-height: design-rpx(52);
  padding: 0 design-rpx(16);
  border-bottom: design-rpx(1) solid #edf1f7;
}

.daily-row:last-child {
  border-bottom: 0;
}

.date-cell {
  display: flex;
  align-items: center;
  gap: design-rpx(18);
  min-width: 0;
}

.date-text {
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 400;
  line-height: 1;
}

.weekday-text {
  color: #7b879a;
  font-size: design-rpx(13);
  font-weight: 400;
  line-height: 1;
}

.usage-cell,
.amount-cell {
  color: #152234;
  font-size: design-rpx(14);
  font-weight: 400;
  line-height: 1;
  text-align: right;
  white-space: nowrap;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: design-rpx(8);
}

.empty-image {
  width: design-rpx(224);
  height: design-rpx(149);
}

.empty-text {
  display: block;
  margin-top: design-rpx(14);
  color: #8a97ac;
  font-size: design-rpx(15);
  font-weight: 400;
  line-height: 1;
}

.end-text {
  display: block;
  margin-top: design-rpx(24);
  color: #8a97ac;
  font-size: design-rpx(12);
  font-weight: 500;
  text-align: center;
}
</style>
