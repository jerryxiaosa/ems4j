<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import AppTabBar from '@/components/common/AppTabBar.vue'
import { getMyProfile } from '@/api/me'
import { getOrderPage } from '@/api/order'
import type { ElectricAccountType } from '@/types/common'
import type { OrderDateRange, OrderRecordItem } from '@/types/order'
import { miniRoute } from '@/utils/route'

const QUANTITY_ELECTRIC_ACCOUNT_TYPE: ElectricAccountType = 0
const filterOptions: { label: string; range: OrderDateRange }[] = [
  { label: '全部', range: 'all' },
  { label: '本年', range: 'currentYear' },
  { label: '近6月', range: 'last6Months' },
  { label: '近3月', range: 'last3Months' }
]
const activeRange = ref<OrderDateRange>('all')
const orderList = ref<OrderRecordItem[]>([])
const electricAccountType = ref<ElectricAccountType>()
const activeServiceFeeOrderNo = ref('')
const pageNum = ref(1)
const pageSize = 10
const shouldShowMeterInfo = computed(() => electricAccountType.value === QUANTITY_ELECTRIC_ACCOUNT_TYPE)

const formatMoney = (amount?: number, amountText?: string) => {
  if (amountText) {
    return amountText
  }

  if (typeof amount === 'number') {
    return amount.toFixed(2)
  }

  return '--'
}

const recordList = computed(() => {
  return orderList.value.map((order) => {
    const isSuccess = order.status === 'SUCCESS'
    const serviceFeeAmount = formatMoney(order.serviceFeeAmount, order.serviceFeeAmountText)
    const serviceFeeNumber = order.serviceFeeAmount ?? Number(order.serviceFeeAmountText ?? 0)
    const hasServiceFeeTip = isSuccess && serviceFeeAmount !== '--' && serviceFeeNumber > 0

    return {
      status: isSuccess ? 'success' : 'fail',
      statusName: order.statusName,
      room: shouldShowMeterInfo.value ? order.meterName || order.location || '电表充值' : '账户充值',
      meterNo: order.meterNo || '-',
      orderNo: order.orderSn,
      time: order.createTime,
      amount: formatMoney(order.payAmount, order.payAmountText),
      hasServiceFeeTip,
      isServiceFeeTipVisible: activeServiceFeeOrderNo.value === order.orderSn,
      serviceFeeNote: hasServiceFeeTip ? `到账金额已扣除服务费 ¥${serviceFeeAmount}` : '',
      payment: isSuccess ? order.paymentChannelName : order.statusName
    }
  })
})
const hasRecords = computed(() => recordList.value.length > 0)

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

const toggleServiceFeeTip = (orderNo: string) => {
  activeServiceFeeOrderNo.value = activeServiceFeeOrderNo.value === orderNo ? '' : orderNo
}

const hideServiceFeeTip = () => {
  activeServiceFeeOrderNo.value = ''
}

const loadAccountType = async () => {
  try {
    const profile = await getMyProfile()
    electricAccountType.value = profile.electricAccountType
  } catch (error) {
    console.error('加载账户类型失败', error)
    electricAccountType.value = QUANTITY_ELECTRIC_ACCOUNT_TYPE
  }
}

const loadOrderPage = async () => {
  try {
    const response = await getOrderPage({
      pageNum: pageNum.value,
      pageSize,
      range: activeRange.value
    })
    orderList.value = response.list
  } catch (error) {
    console.error('加载充值缴费记录失败', error)
    uni.showToast({
      title: '记录加载失败',
      icon: 'none'
    })
  }
}

const selectFilter = async (range: OrderDateRange) => {
  if (activeRange.value === range) {
    return
  }

  activeRange.value = range
  pageNum.value = 1
  await loadOrderPage()
}

onMounted(() => {
  void (async () => {
    await loadAccountType()
    await loadOrderPage()
  })()
})
</script>

<template>
  <view class="pay-record-page" @click="hideServiceFeeTip">
    <AppBackHeader title="充值缴费记录" @back="handleBack" />

    <scroll-view class="record-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="filter-segment">
          <button
            v-for="filter in filterOptions"
            :key="filter.range"
            :class="['filter-item', activeRange === filter.range ? 'is-active' : '']"
            @click="selectFilter(filter.range)"
          >
            <text>{{ filter.label }}</text>
          </button>
        </view>

        <view v-if="hasRecords" class="record-list">
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
                <text class="status-title">{{ record.statusName }}</text>
              </view>
              <view class="record-amount">
                <view class="amount-line">
                  <view
                    v-if="record.hasServiceFeeTip"
                    class="amount-tip"
                    @click.stop="toggleServiceFeeTip(record.orderNo)"
                  >
                    <view class="amount-tip-icon" hover-class="amount-tip-icon-hover">
                      <text>i</text>
                    </view>
                    <view v-if="record.isServiceFeeTipVisible" class="amount-tooltip">
                      <view class="amount-tooltip-arrow"></view>
                      <text>{{ record.serviceFeeNote }}</text>
                    </view>
                  </view>
                  <text class="amount-text">¥ {{ record.amount }}</text>
                </view>
                <text class="amount-label">订单金额</text>
              </view>
            </view>
            <view class="record-detail">
              <view class="room-row">
                <text class="room-title">{{ record.room }}</text>
                <text :class="['payment-text', record.status === 'fail' ? 'is-fail' : '']">{{ record.payment }}</text>
              </view>
              <view v-if="shouldShowMeterInfo" class="record-meta-line">
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

        <view v-else class="empty-state">
          <image class="empty-image" src="/static/stitch/empty.png" mode="aspectFit" />
          <text class="empty-text">暂无数据</text>
        </view>
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
  font-weight: 500;
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

.amount-line {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: design-rpx(5);
}

.amount-tip {
  position: relative;
  z-index: 10;
  display: flex;
  flex-shrink: 0;
  align-items: center;
}

.amount-tip-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(15);
  height: design-rpx(15);
  color: #7b879a;
  font-size: design-rpx(10);
  font-weight: 700;
  line-height: 1;
  border: design-rpx(1) solid #cfd9e8;
  border-radius: 999rpx;
}

.amount-tip-icon-hover {
  background: #eef4fb;
}

.amount-tooltip {
  position: absolute;
  top: design-rpx(23);
  right: design-rpx(-8);
  z-index: 20;
  display: inline-flex;
  align-items: center;
  box-sizing: border-box;
  padding: design-rpx(8) design-rpx(10);
  color: #7b879a;
  font-size: design-rpx(13);
  font-weight: 400;
  line-height: 1.25;
  text-align: left;
  white-space: nowrap;
  background: #ffffff;
  border: design-rpx(1) solid rgba(207, 217, 232, 0.92);
  border-radius: design-rpx(8);
  box-shadow: 0 design-rpx(8) design-rpx(18) rgba(6, 19, 61, 0.12);
}

.amount-tooltip-arrow {
  position: absolute;
  top: design-rpx(-5);
  right: design-rpx(12);
  width: design-rpx(9);
  height: design-rpx(9);
  background: #ffffff;
  border-top: design-rpx(1) solid rgba(207, 217, 232, 0.92);
  border-left: design-rpx(1) solid rgba(207, 217, 232, 0.92);
  transform: rotate(45deg);
}

.amount-text {
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 700;
  line-height: design-rpx(26);
}

.amount-label {
  display: block;
  margin-top: design-rpx(1);
  color: #8a97ac;
  font-size: design-rpx(11);
  font-weight: 500;
  line-height: 1.1;
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: design-rpx(150);
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
</style>
