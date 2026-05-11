<script setup lang="ts">
const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  goLogin()
}

const goLogin = () => {
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
          src="/static/stitch/check.png"
          mode="aspectFit"
        />

        <view class="error-copy">
          <text class="error-title">账户未开户或状态异常</text>
          <text class="error-desc">如需帮助，请联系物业或相关负责人</text>
        </view>

        <button class="contact-button" @click="goLogin">
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
  color: #06133d;
  background: linear-gradient(180deg, #f5f7fd 0%, #ffffff 100%);
}

.page-header {
  position: relative;
  z-index: 2;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  height: design-rpx(86);
  padding: design-rpx(34) design-rpx(20) 0;
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
  border-bottom: design-rpx(2.5) solid #06133d;
  border-left: design-rpx(2.5) solid #06133d;
  transform: rotate(45deg);
}

.page-title {
  position: absolute;
  left: 50%;
  color: #06133d;
  font-size: design-rpx(20);
  font-weight: 500;
  line-height: 1;
  transform: translateX(-50%);
}

.page-main {
  display: flex;
  flex: 1;
  min-height: 0;
  padding: design-rpx(18) design-rpx(22) design-rpx(36);
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
  border: design-rpx(0.5) solid #e7e7f3;
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(4) design-rpx(20) rgba(15, 31, 61, 0.05);
}

.error-illustration {
  width: design-rpx(318);
  height: design-rpx(294);
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
  width: 100%;
  height: design-rpx(56);
  color: #ffffff;
  font-size: design-rpx(20);
  font-weight: 700;
  letter-spacing: design-rpx(1);
  background: linear-gradient(90deg, #1677ff 0%, #0068ff 100%);
  border-radius: 999rpx;
  box-shadow: 0 design-rpx(8) design-rpx(22) rgba(22, 119, 255, 0.28);
}
</style>
