<script setup lang="ts">
withDefaults(
  defineProps<{
    text?: string
  }>(),
  {
    text: '去充值'
  }
)

const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <button class="recharge-entry-button" @click="emit('click')">
    <text>{{ text }}</text>
    <text class="recharge-entry-chevron">›</text>
  </button>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.recharge-entry-button {
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

.recharge-entry-chevron {
  display: block;
  margin-left: design-rpx(-2);
  font-size: design-rpx(22);
  font-weight: 500;
  line-height: 1;
}
</style>
