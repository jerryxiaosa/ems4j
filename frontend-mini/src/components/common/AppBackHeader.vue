<script setup lang="ts">
defineProps<{
  title: string
}>()

const emit = defineEmits<{
  back: []
}>()
</script>

<template>
  <view class="page-header">
    <button class="back-button" aria-label="返回" @click="emit('back')">
      <view class="back-chevron"></view>
    </button>
    <text class="page-title">{{ title }}</text>
    <view class="header-placeholder"></view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
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
  padding: 0;
  background: transparent;
}

.back-button::after {
  border: 0;
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
  line-height: 1;
  transform: translateX(-50%);
}
</style>
