<script setup lang="ts">
type AmountSize = 'large' | 'medium' | 'small'

withDefaults(
  defineProps<{
    value: string | number
    visible?: boolean
    currency?: string
    size?: AmountSize
  }>(),
  {
    visible: true,
    currency: '¥',
    size: 'large'
  }
)
</script>

<template>
  <view :class="['app-amount', `is-${size}`]">
    <template v-if="visible">
      <text v-if="currency" class="app-amount-currency">{{ currency }}</text>
      <text class="app-amount-value">{{ value }}</text>
    </template>
    <text v-else class="app-amount-value">--</text>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.app-amount {
  display: flex;
  align-items: baseline;
  color: inherit;
}

.app-amount.is-large {
  gap: design-rpx(2);
}

.app-amount.is-large .app-amount-currency {
  font-size: design-rpx(24);
  font-weight: 700;
  line-height: 1;
}

.app-amount.is-large .app-amount-value {
  font-size: design-rpx(38);
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: design-rpx(0.5);
}

.app-amount.is-medium {
  gap: design-rpx(6);
}

.app-amount.is-medium .app-amount-currency {
  font-size: design-rpx(22);
  font-weight: 400;
  line-height: 1;
}

.app-amount.is-medium .app-amount-value {
  font-size: design-rpx(30);
  font-weight: 400;
  line-height: 1;
}

.app-amount.is-small {
  gap: design-rpx(4);
}

.app-amount.is-small .app-amount-currency,
.app-amount.is-small .app-amount-value {
  font-size: design-rpx(18);
  font-weight: 400;
  line-height: 1;
}
</style>
