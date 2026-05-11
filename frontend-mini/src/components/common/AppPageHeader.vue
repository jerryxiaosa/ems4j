<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<template>
  <view class="page-header">
    <text class="page-title">{{ title }}</text>
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
  justify-content: center;
  height: design-rpx(86);
  padding: design-rpx(34) design-rpx(20) 0;
  background: transparent;
}

.page-title {
  color: #06133d;
  font-size: design-rpx(18);
  font-weight: 400;
  line-height: 1;
}
</style>
