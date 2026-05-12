<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import TrendLineChart from '@/components/chart/TrendLineChart.vue'
import { getMeterDetail } from '@/api/meter'
import type { MeterDetailResponse } from '@/types/meter'
import { miniRoute } from '@/utils/route'

type MeterStatus = 'normal' | 'offline'

type MeterDetail = {
  id: string
  name: string
  meterNo: string
  location: string
  status: MeterStatus
  totalEnergy: string
  energySegments: EnergySegment[]
  todayEnergy: string
  todayEnergySegments: EnergySegment[]
  todayUsageValues: number[]
  updateTime: string
  updateClock: string
}

type EnergySegment = {
  name: string
  value: string
  tone: 'tip' | 'peak' | 'flat' | 'valley' | 'deep'
}

const todayUsageCategories = ['0', '4', '8', '12', '16', '20', '24']

const meterId = ref(101)
const meterDetail = ref<MeterDetailResponse>()

const formatNumber = (value?: number | null) => {
  if (value === undefined || value === null) {
    return '--'
  }

  return value.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const meter = computed(() => {
  const detail = meterDetail.value

  if (!detail) {
    return {
      id: String(meterId.value),
      name: '',
      meterNo: '',
      location: '',
      status: 'offline' as MeterStatus,
      statusText: '--',
      totalEnergy: '--',
      energySegments: [],
      todayEnergy: '--',
      todayEnergySegments: [],
      todayUsageValues: [0, 0, 0, 0, 0, 0, 0],
      updateTime: '--',
      updateClock: '--'
    }
  }

  return {
    id: String(detail.meterId),
    name: detail.meterName,
    meterNo: detail.meterNo,
    location: detail.location,
    status: detail.isOnline ? 'normal' : 'offline',
    statusText: detail.isOnline ? '在线' : '离线',
    totalEnergy: formatNumber(detail.totalEnergy),
    energySegments: [
      { name: '尖', value: formatNumber(detail.sharpEnergy), tone: 'tip' },
      { name: '峰', value: formatNumber(detail.peakEnergy), tone: 'peak' },
      { name: '平', value: formatNumber(detail.flatEnergy), tone: 'flat' },
      { name: '谷', value: formatNumber(detail.valleyEnergy), tone: 'valley' },
      { name: '深谷', value: formatNumber(detail.deepValleyEnergy), tone: 'deep' }
    ] as EnergySegment[],
    todayEnergy: formatNumber(detail.todayTotalEnergy),
    todayEnergySegments: [
      { name: '尖', value: formatNumber(detail.todaySharpEnergy), tone: 'tip' },
      { name: '峰', value: formatNumber(detail.todayPeakEnergy), tone: 'peak' },
      { name: '平', value: formatNumber(detail.todayFlatEnergy), tone: 'flat' },
      { name: '谷', value: formatNumber(detail.todayValleyEnergy), tone: 'valley' },
      { name: '深谷', value: formatNumber(detail.todayDeepValleyEnergy), tone: 'deep' }
    ] as EnergySegment[],
    todayUsageValues: detail.todayUsageTrend,
    updateTime: detail.updateTime,
    updateClock: detail.updateTime.slice(11, 16)
  } satisfies MeterDetail & { statusText: string }
})

const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  uni.redirectTo({
    url: miniRoute.meter
  })
}

const loadMeterDetail = async () => {
  try {
    meterDetail.value = await getMeterDetail(meterId.value)
  } catch (error) {
    console.error('加载电表详情失败', error)
    uni.showToast({
      title: '电表详情加载失败',
      icon: 'none'
    })
  }
}

onLoad((query) => {
  if (query?.id) {
    const parsedMeterId = Number(query.id)
    if (!Number.isNaN(parsedMeterId)) {
      meterId.value = parsedMeterId
    }
  }

  void loadMeterDetail()
})
</script>

<template>
  <view class="meter-detail-page">
    <AppBackHeader title="电表详情" @back="handleBack" />

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="meter-hero-card">
          <view class="meter-icon-wrap">
            <image class="meter-icon" src="/static/icons/meter.png" mode="aspectFit" />
          </view>
          <view class="meter-main">
            <view class="meter-title-row">
              <text class="meter-title">{{ meter.name }}</text>
              <text :class="['status-pill', meter.status === 'normal' ? 'is-normal' : 'is-offline']">
                {{ meter.statusText }}
              </text>
            </view>
            <view class="meter-meta-line">
              <text class="meter-meta-label">电表编号：</text>
              <text class="meter-meta-value">{{ meter.meterNo }}</text>
            </view>
            <view class="meter-meta-line">
              <text class="meter-meta-label">所在位置：</text>
              <text class="meter-meta-value">{{ meter.location }}</text>
            </view>
          </view>
        </view>

        <view class="total-row">
          <view class="total-head">
            <view class="total-title-row">
              <text class="section-title">总电量</text>
              <view class="total-value">
                <text>{{ meter.totalEnergy }}</text>
                <text class="unit">kWh</text>
              </view>
            </view>
            <text class="update-time">更新时间：{{ meter.updateTime }}</text>
          </view>

          <view class="segment-section">
            <text class="segment-section-title">总电量分项</text>
            <view class="segment-list">
              <view v-for="segment in meter.energySegments" :key="segment.name" class="segment-item">
                <view class="segment-name-wrap">
                  <view :class="['segment-dot', segment.tone]"></view>
                  <text class="segment-name">{{ segment.name }}</text>
                </view>
                <view class="segment-value-wrap">
                  <text class="segment-value">{{ segment.value }}</text>
                  <text class="segment-unit">kWh</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="usage-card">
          <view class="usage-card-head">
            <text class="section-title">今日用电</text>
            <view class="today-value">
              <text>{{ meter.todayEnergy }}</text>
              <text class="unit">kWh</text>
            </view>
          </view>

          <view class="today-chart-wrap">
            <TrendLineChart
              canvas-id="meterTodayUsageChart"
              :categories="todayUsageCategories"
              :values="meter.todayUsageValues"
              series-name="今日用电"
              unit="kWh"
              :max="4"
              :width-rpx="620"
              :height-rpx="270"
            />
          </view>

          <view class="segment-section today-segment-section">
            <text class="segment-section-title">今日分项</text>
            <view class="today-segment-grid">
              <view v-for="segment in meter.todayEnergySegments" :key="segment.name" class="today-segment-item">
                <view class="today-segment-name-wrap">
                  <view :class="['segment-dot', segment.tone]"></view>
                  <text class="today-segment-name">{{ segment.name }}</text>
                </view>
                <view class="today-segment-value-wrap">
                  <text class="today-segment-value">{{ segment.value }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.meter-detail-page {
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
  height: 0;
  min-height: 0;
}

.content-stack {
  box-sizing: border-box;
  min-height: 100%;
  padding: design-rpx(28) design-rpx(22) design-rpx(34);
}

.meter-hero-card {
  display: flex;
  box-sizing: border-box;
  align-items: center;
  width: 100%;
  min-height: design-rpx(110);
  padding: design-rpx(16) design-rpx(18);
  background: rgba(255, 255, 255, 0.88);
  border: design-rpx(0.5) solid #e7edf6;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.045);
}

.meter-icon-wrap {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(66);
  height: design-rpx(66);
  margin-right: design-rpx(16);
  background: #eaf4ff;
  border-radius: 999rpx;
}

.meter-icon {
  width: design-rpx(50);
  height: design-rpx(50);
}

.meter-main {
  flex: 1;
  min-width: 0;
}

.meter-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: design-rpx(12);
}

.meter-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: #06133d;
  font-size: design-rpx(16);
  font-weight: 700;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-pill {
  flex-shrink: 0;
  min-width: design-rpx(44);
  padding: design-rpx(5) design-rpx(10);
  font-size: design-rpx(13);
  font-weight: 600;
  line-height: 1;
  text-align: center;
  border-radius: 999rpx;
}

.status-pill.is-normal {
  color: #11a646;
  background: #def8e7;
}

.status-pill.is-offline {
  color: #ff3b45;
  background: #ffe5e7;
}

.meter-meta-line {
  display: flex;
  align-items: center;
  width: 100%;
  margin-top: design-rpx(10);
  overflow: hidden;
  color: #7b879a;
  font-size: design-rpx(13);
  font-weight: 400;
  line-height: 1.15;
  white-space: nowrap;
}

.meter-meta-label {
  display: block;
  flex-shrink: 0;
}

.meter-meta-value {
  display: block;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.total-row {
  box-sizing: border-box;
  width: 100%;
  margin-top: design-rpx(24);
  padding: design-rpx(18);
  background: rgba(255, 255, 255, 0.94);
  border: design-rpx(0.5) solid #e7edf6;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.04);
}

.total-head {
  min-width: 0;
}

.total-title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: design-rpx(12);
}

.section-title {
  display: block;
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 700;
  line-height: 1;
}

.update-time {
  display: block;
  margin-top: design-rpx(12);
  color: #4a5d91;
  font-size: design-rpx(13);
  font-weight: 400;
  line-height: 1;
  white-space: nowrap;
}

.total-value {
  display: flex;
  align-items: baseline;
  flex-shrink: 0;
  gap: design-rpx(5);
  color: #06133d;
  font-size: design-rpx(24);
  font-weight: 700;
  line-height: 1;
}

.unit {
  color: #3f5288;
  font-size: design-rpx(17);
  font-weight: 400;
}

.segment-section {
  margin-top: design-rpx(18);
}

.today-segment-section {
  margin: 0 design-rpx(18) design-rpx(18);
  padding-top: design-rpx(16);
  border-top: design-rpx(0.5) solid #edf2f9;
}

.today-segment-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: design-rpx(6);
  margin-top: design-rpx(10);
}

.today-segment-item {
  display: flex;
  box-sizing: border-box;
  flex-direction: column;
  align-items: center;
  min-width: 0;
  padding: design-rpx(9) design-rpx(3);
  background: #f7faff;
  border: design-rpx(0.5) solid #edf2f9;
  border-radius: design-rpx(12);
}

.today-segment-name-wrap,
.today-segment-value-wrap {
  display: flex;
  align-items: baseline;
  min-width: 0;
}

.today-segment-name-wrap {
  gap: design-rpx(5);
}

.today-segment-value-wrap {
  justify-content: center;
  width: 100%;
  margin-top: design-rpx(7);
}

.today-segment-name {
  color: #6a7a8f;
  font-size: design-rpx(12);
  font-weight: 500;
  line-height: 1;
}

.today-segment-value {
  color: #06133d;
  font-size: design-rpx(12);
  font-weight: 300;
  line-height: 1;
  white-space: nowrap;
}

.segment-section-title {
  display: block;
  color: #6a7a8f;
  font-size: design-rpx(13);
  font-weight: 500;
  line-height: 1;
}

.segment-list {
  display: flex;
  flex-direction: column;
  gap: design-rpx(8);
  margin-top: design-rpx(10);
}

.segment-item {
  display: flex;
  box-sizing: border-box;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
  min-height: design-rpx(42);
  padding: 0 design-rpx(12);
  background: #f7faff;
  border: design-rpx(0.5) solid #edf2f9;
  border-radius: design-rpx(12);
}

.segment-name-wrap,
.segment-value-wrap {
  display: flex;
  align-items: baseline;
  min-width: 0;
}

.segment-name-wrap {
  gap: design-rpx(8);
}

.segment-value-wrap {
  flex-shrink: 0;
  justify-content: flex-end;
  width: design-rpx(140);
  gap: design-rpx(4);
}

.segment-dot {
  flex-shrink: 0;
  width: design-rpx(8);
  height: design-rpx(8);
  border-radius: 999rpx;
}

.segment-dot.tip {
  background: #f06423;
}

.segment-dot.peak {
  background: #f6a800;
}

.segment-dot.flat {
  background: #14b86a;
}

.segment-dot.valley {
  background: #1677ff;
}

.segment-dot.deep {
  background: #6857ff;
}

.segment-name {
  color: #6a7a8f;
  font-size: design-rpx(13);
  font-weight: 500;
  line-height: 1;
}

.segment-value {
  color: #06133d;
  font-size: design-rpx(14);
  font-weight: 600;
  line-height: 1;
}

.segment-unit {
  color: #8a97ac;
  font-size: design-rpx(11);
  font-weight: 400;
  line-height: 1;
}

.usage-card {
  margin-top: design-rpx(24);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.94);
  border: design-rpx(0.5) solid #e7edf6;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.04);
}

.usage-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: design-rpx(18) design-rpx(18) 0;
}

.today-value {
  display: flex;
  align-items: baseline;
  flex-shrink: 0;
  gap: design-rpx(6);
  color: #06133d;
  font-size: design-rpx(24);
  font-weight: 700;
  line-height: 1;
}

.today-chart-wrap {
  margin-top: design-rpx(12);
  padding: 0 design-rpx(8) design-rpx(10);
  overflow: hidden;
}
</style>
