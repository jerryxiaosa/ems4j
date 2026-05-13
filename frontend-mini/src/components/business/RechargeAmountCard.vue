<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    amount: string
    serviceFeeRate?: number
  }>(),
  {
    serviceFeeRate: 0
  }
)

const emit = defineEmits<{
  'update:amount': [value: string]
}>()

const feeTip = computed(() => {
  if (props.serviceFeeRate > 0) {
    return `收取 ${(props.serviceFeeRate * 100).toFixed(2).replace(/\.?0+$/, '')}% 的服务费。`
  }

  return '账户充值后，余额可用于电费扣缴和相关服务。'
})

const handleInput = (event: Event) => {
  const detailValue = (event as unknown as { detail?: { value?: string } }).detail?.value
  const targetValue = (event.target as HTMLInputElement | null)?.value

  emit('update:amount', detailValue ?? targetValue ?? '')
}
</script>

<template>
  <view class="form-card">
    <text class="section-title">充值金额</text>
    <view class="amount-input-wrap">
      <text class="amount-currency">¥</text>
      <input
        :value="amount"
        class="amount-input"
        type="number"
        placeholder="请输入充值金额"
        placeholder-class="amount-placeholder"
        @input="handleInput"
      />
      <text class="amount-unit">元</text>
    </view>
    <text class="form-tip">{{ feeTip }}</text>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.form-card {
  margin-top: design-rpx(16);
  padding: design-rpx(20);
  background: #ffffff;
  border: design-rpx(0.5) solid #e7e7f3;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(4) design-rpx(16) rgba(15, 31, 61, 0.04);
}

.section-title {
  display: block;
  margin-bottom: design-rpx(16);
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 700;
}

.amount-input-wrap {
  display: flex;
  align-items: center;
  gap: design-rpx(12);
  height: design-rpx(58);
  padding: 0 design-rpx(16);
  background: #f5f7fd;
  border: design-rpx(0.5) solid #e7e7f3;
  border-radius: design-rpx(12);
}

.amount-currency {
  color: #152234;
  font-size: design-rpx(22);
  font-weight: 600;
}

.amount-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  color: #152234;
  font-size: design-rpx(20);
  font-weight: 500;
}

.amount-placeholder {
  color: #9aa6ba;
  font-weight: 300;
}

.amount-unit {
  color: #8a97ac;
  font-size: design-rpx(15);
  font-weight: 600;
}

.form-tip {
  display: block;
  margin-top: design-rpx(12);
  color: #8a97ac;
  font-size: design-rpx(12);
  line-height: 1.5;
}
</style>
