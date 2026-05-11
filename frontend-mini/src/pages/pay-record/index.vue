<script setup lang="ts">
import { ref } from 'vue'
import AppTabBar from '@/components/common/AppTabBar.vue'

const filterOptions = ['全部', '本年', '近6月', '近3月']
const activeFilter = ref(filterOptions[0])

const recordList = [
  {
    status: 'success',
    room: '1 单元 101 室',
    meterNo: '01234567890123456789',
    orderNo: '2024052014304500001234',
    time: '2024-05-20 14:30:45',
    amount: '48.20',
    payment: '微信支付'
  },
  {
    status: 'success',
    room: '1 单元 102 室',
    meterNo: '01234567890123456790',
    orderNo: '2024051510203000005678',
    time: '2024-05-15 10:20:30',
    amount: '76.50',
    payment: '微信支付'
  },
  {
    status: 'success',
    room: '2 单元 201 室',
    meterNo: '01234567890123456791',
    orderNo: '2024051009151200003456',
    time: '2024-05-10 09:15:12',
    amount: '33.20',
    payment: '微信支付'
  },
  {
    status: 'fail',
    room: '1 单元 101 室',
    meterNo: '01234567890123456789',
    orderNo: '2024050816452200007890',
    time: '2024-05-08 16:45:22',
    amount: '48.20',
    payment: '支付超时'
  },
  {
    status: 'success',
    room: '2 单元 202 室',
    meterNo: '01234567890123456792',
    orderNo: '2024050511053300002345',
    time: '2024-05-05 11:05:33',
    amount: '15.80',
    payment: '微信支付'
  },
  {
    status: 'success',
    room: '3 单元 301 室',
    meterNo: '01234567890123456793',
    orderNo: '2024050213221800006789',
    time: '2024-05-02 13:22:18',
    amount: '28.90',
    payment: '微信支付'
  }
]

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

const selectFilter = (filter: string) => {
  activeFilter.value = filter
}
</script>

<template>
  <view class="pay-record-page">
    <view class="page-header">
      <button class="back-button" aria-label="返回" @click="handleBack">
        <view class="back-chevron"></view>
      </button>
      <text class="page-title">缴费记录</text>
      <view class="header-placeholder"></view>
    </view>

    <scroll-view class="record-scroll" scroll-y enhanced show-scrollbar="false">
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

        <view class="record-list">
          <view v-for="record in recordList" :key="record.orderNo" class="record-card">
            <view class="record-summary">
              <view class="record-status">
                <view :class="['status-icon', record.status === 'success' ? 'is-success' : 'is-fail']">
                  <view v-if="record.status === 'success'" class="check-mark"></view>
                  <view v-else class="cross-mark">
                    <view></view>
                    <view></view>
                  </view>
                </view>
                <text class="status-title">{{ record.status === 'success' ? '支付成功' : '支付失败' }}</text>
              </view>
              <view class="record-amount">
                <text class="amount-text">¥ {{ record.amount }}</text>
              </view>
            </view>
            <view class="record-detail">
              <view class="room-row">
                <text class="room-title">{{ record.room }}</text>
                <text :class="['payment-text', record.status === 'fail' ? 'is-fail' : '']">{{ record.payment }}</text>
              </view>
              <view class="record-meta-line">
                <text class="record-meta-label">电表编号：</text>
                <text class="record-meta-value">{{ record.meterNo }}</text>
              </view>
              <view class="record-meta-line">
                <text class="record-meta-label">订单编号：</text>
                <text class="record-meta-value">{{ record.orderNo }}</text>
              </view>
              <text class="record-time">{{ record.time }}</text>
            </view>
          </view>
        </view>

        <text class="end-text">没有更多了</text>
      </view>
    </scroll-view>

    <AppTabBar active="recharge" />
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.pay-record-page {
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
  font-size: design-rpx(18);
  font-weight: 400;
  transform: translateX(-50%);
}

.record-scroll {
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

.record-list {
  display: flex;
  flex-direction: column;
  gap: design-rpx(12);
  margin-top: design-rpx(16);
}

.record-card {
  box-sizing: border-box;
  width: 100%;
  min-height: design-rpx(142);
  padding: design-rpx(16) design-rpx(18);
  background: #ffffff;
  border: design-rpx(1) solid rgba(219, 228, 242, 0.72);
  border-radius: design-rpx(20);
}

.record-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: design-rpx(10);
}

.record-status {
  display: flex;
  align-items: flex-start;
  gap: design-rpx(12);
  flex: 1;
  min-width: 0;
}

.status-icon {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(26);
  height: design-rpx(26);
  margin-top: design-rpx(1);
  border-radius: 999rpx;
}

.status-icon.is-success {
  background: #16c83f;
}

.status-icon.is-fail {
  background: #ff4d4f;
}

.check-mark {
  width: design-rpx(12);
  height: design-rpx(7);
  border-bottom: design-rpx(3) solid #ffffff;
  border-left: design-rpx(3) solid #ffffff;
  transform: rotate(-45deg) translate(design-rpx(1), design-rpx(-1));
}

.cross-mark {
  position: relative;
  width: design-rpx(13);
  height: design-rpx(13);
}

.cross-mark view {
  position: absolute;
  top: 50%;
  left: 50%;
  width: design-rpx(15);
  height: design-rpx(3);
  background: #ffffff;
  border-radius: 999rpx;
}

.cross-mark view:first-child {
  transform: translate(-50%, -50%) rotate(45deg);
}

.cross-mark view:last-child {
  transform: translate(-50%, -50%) rotate(-45deg);
}

.status-title,
.room-title,
.record-time,
.amount-text,
.payment-text {
  display: block;
}

.status-title {
  flex: 1;
  min-width: 0;
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 700;
  line-height: design-rpx(26);
}

.record-detail {
  min-width: 0;
  margin-left: design-rpx(38);
}

.room-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: design-rpx(10);
  margin-top: design-rpx(7);
}

.room-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-meta-line,
.record-time {
  margin-top: design-rpx(7);
  color: #7b879a;
  font-size: design-rpx(13);
  font-weight: 400;
  line-height: 1.15;
}

.record-meta-line {
  display: flex;
  align-items: center;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
}

.record-meta-label {
  display: block;
  flex-shrink: 0;
}

.record-meta-value {
  display: block;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-amount {
  flex-shrink: 0;
  min-width: design-rpx(68);
  padding-top: 0;
  text-align: right;
}

.amount-text {
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 700;
  line-height: design-rpx(26);
}

.payment-text {
  flex-shrink: 0;
  color: #7b879a;
  font-size: design-rpx(13);
  font-weight: 500;
}

.payment-text.is-fail {
  color: #ff3b45;
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
