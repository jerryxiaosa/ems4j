<script setup lang="ts">
const handleBack = () => {
  uni.redirectTo({
    url: '/pages/login/index'
  })
}
</script>

<template>
  <view class="account-error-page">
    <view class="page-header">
      <button class="back-button" aria-label="返回" @click="handleBack">
        <view class="back-chevron"></view>
      </button>
      <text class="page-title">账户异常</text>
      <view class="header-placeholder"></view>
    </view>

    <view class="page-main">
      <view class="error-card">
        <image
          class="error-illustration"
          src="/static/stitch/account-error-illustration.png"
          mode="aspectFit"
        />

        <view class="error-copy">
          <text class="error-title">账户未开户或状态异常</text>
          <text class="error-desc">如需帮助，请联系物业或相关负责人</text>
        </view>

        <button class="contact-button" @click="handleBack">
          <text>返回登录页</text>
        </button>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.account-error-page {
  display: flex;
  flex-direction: column;
  width: 750rpx;
  max-width: 100%;
  height: 100vh;
  overflow: hidden;
  color: #0f1f3d;
  background: linear-gradient(180deg, #f5f7fd 0%, #ffffff 100%);
}

.page-header {
  position: relative;
  z-index: 2;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  height: design-rpx(76);
  padding: design-rpx(22) design-rpx(20) 0;
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
  border-bottom: design-rpx(3) solid #0f1f3d;
  border-left: design-rpx(3) solid #0f1f3d;
  transform: rotate(45deg);
}

.page-title {
  position: absolute;
  left: 50%;
  color: #0f1f3d;
  font-size: design-rpx(18);
  font-weight: 600;
  line-height: 1;
  transform: translateX(-50%);
}

.page-main {
  display: flex;
  flex: 1;
  min-height: 0;
  padding: design-rpx(18) design-rpx(24) design-rpx(36);
}

.error-card {
  position: relative;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  min-height: 0;
  padding: design-rpx(78) design-rpx(24) design-rpx(64);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.78);
  border: design-rpx(1) dashed rgba(139, 165, 204, 0.45);
  border-radius: design-rpx(28);
  box-shadow: 0 design-rpx(4) design-rpx(20) rgba(15, 31, 61, 0.05);
}

.error-illustration {
  width: design-rpx(318);
  height: design-rpx(272);
  margin-bottom: design-rpx(48);
}

.error-copy {
  width: 100%;
  margin-bottom: design-rpx(48);
  text-align: center;
}

.error-title,
.error-desc {
  display: block;
}

.error-title {
  margin-bottom: design-rpx(16);
  color: #0f1f3d;
  font-size: design-rpx(22);
  font-weight: 700;
  line-height: 1.28;
}

.error-desc {
  color: #8a97ac;
  font-size: design-rpx(15);
  font-weight: 500;
  line-height: 1.5;
}

.contact-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: design-rpx(236);
  height: design-rpx(56);
  color: #ffffff;
  font-size: design-rpx(18);
  font-weight: 600;
  letter-spacing: design-rpx(2);
  background: linear-gradient(90deg, #2f86ff 0%, #1768f2 100%);
  border-radius: 999rpx;
  box-shadow: 0 design-rpx(8) design-rpx(22) rgba(37, 99, 235, 0.26);
}
</style>
