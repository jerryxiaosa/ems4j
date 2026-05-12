<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import { getMeterList } from '@/api/meter'
import type { MeterListItem } from '@/types/meter'
import { miniRoute } from '@/utils/route'

const meterList = ref<MeterListItem[]>([])

const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  uni.redirectTo({
    url: miniRoute.home
  })
}

const loadMeterList = async () => {
  try {
    const response = await getMeterList()
    meterList.value = response.list
  } catch (error) {
    console.error('加载电表列表失败', error)
    uni.showToast({
      title: '电表加载失败',
      icon: 'none'
    })
  }
}

const getMeterStatusText = (meter: MeterListItem) => {
  return meter.isOnline ? '在线' : '离线'
}

const openMeterDetail = (meter: MeterListItem) => {
  uni.navigateTo({
    url: `${miniRoute.meterDetail}?id=${encodeURIComponent(String(meter.meterId))}`
  })
}

onMounted(() => {
  void loadMeterList()
})
</script>

<template>
  <view class="meter-page">
    <AppBackHeader title="我的电表" @back="handleBack" />

    <scroll-view class="meter-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="meter-list">
          <view
            v-for="meter in meterList"
            :key="meter.meterId"
            class="meter-card"
            @click="openMeterDetail(meter)"
          >
            <view class="meter-icon-wrap">
              <image class="meter-icon" src="/static/icons/meter.png" mode="aspectFit" />
            </view>

            <view class="meter-copy">
              <view class="meter-title-row">
                <text class="meter-title">{{ meter.meterName }}</text>
                <text :class="['status-pill', meter.isOnline ? 'is-normal' : 'is-offline']">
                  {{ getMeterStatusText(meter) }}
                </text>
              </view>
              <view class="meter-meta-line">
                <text class="meter-meta-label">电表编号：</text>
                <text class="meter-meta-value">{{ meter.meterNo || '-' }}</text>
              </view>
              <view class="meter-meta-line">
                <text class="meter-meta-label">所在位置：</text>
                <text class="meter-meta-value">{{ meter.location || '-' }}</text>
              </view>
            </view>
            <view class="meter-action" aria-hidden="true">
              <view class="meter-chevron"></view>
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

.meter-page {
  display: flex;
  flex-direction: column;
  width: 750rpx;
  max-width: 100%;
  height: 100vh;
  overflow: hidden;
  color: #06133d;
  background: linear-gradient(180deg, #f6faff 0%, #ffffff 100%);
}

.meter-scroll {
  flex: 1;
  min-height: 0;
}

.content-stack {
  padding: design-rpx(36) design-rpx(18) design-rpx(34);
}

.meter-list {
  display: flex;
  flex-direction: column;
  gap: design-rpx(12);
}

.meter-card {
  display: flex;
  box-sizing: border-box;
  align-items: center;
  width: 100%;
  min-height: design-rpx(110);
  padding: design-rpx(16) design-rpx(18);
  background: #ffffff;
  border: design-rpx(1) solid rgba(219, 228, 242, 0.72);
  border-radius: design-rpx(20);
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

.meter-copy {
  flex: 1;
  min-width: 0;
}

.meter-action {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(24);
  height: design-rpx(66);
  margin-left: design-rpx(10);
}

.meter-chevron {
  width: design-rpx(8);
  height: design-rpx(8);
  border-top: design-rpx(2) solid #9aa8bd;
  border-right: design-rpx(2) solid #9aa8bd;
  transform: rotate(45deg);
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
</style>
