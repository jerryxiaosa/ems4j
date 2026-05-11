<script setup lang="ts">
defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

const handleToggle = () => {
  emit('toggle')
}
</script>

<template>
  <button
    :class="['visibility-button', visible ? 'is-visible' : '']"
    aria-label="切换余额显示"
    @click.stop="handleToggle"
  >
    <view class="visibility-eye-icon"></view>
  </button>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.visibility-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(24);
  height: design-rpx(24);
  padding: 0;
  line-height: 1;
  color: inherit;
  background: transparent;
  border: 0;
  border-radius: 999rpx;
  transition: transform 120ms ease;
}

.visibility-button:active {
  transform: scale(0.92);
}

.visibility-button::after {
  border: 0;
}

.visibility-eye-icon {
  position: relative;
  width: design-rpx(18);
  height: design-rpx(11);
  border: design-rpx(1.5) solid currentColor;
  border-radius: 50%;
}

.visibility-eye-icon::before {
  position: absolute;
  top: 50%;
  left: 50%;
  width: design-rpx(4);
  height: design-rpx(4);
  background: currentColor;
  border-radius: 999rpx;
  content: "";
  transform: translate(-50%, -50%);
}

.visibility-button:not(.is-visible) .visibility-eye-icon::after {
  position: absolute;
  top: 50%;
  left: design-rpx(-2);
  width: design-rpx(22);
  height: design-rpx(2);
  background: currentColor;
  border-radius: 999rpx;
  content: "";
  transform: rotate(-35deg);
}
</style>
