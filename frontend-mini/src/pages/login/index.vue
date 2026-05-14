<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { miniLogin, saveMiniAccessToken, setMockMiniLoginScenario } from '@/api/auth'
import { getWechatLoginCode, getWechatPhoneCode, type WechatPhoneCodeEvent } from '@/platform/auth/wechatAuth'
import { miniRoute } from '@/utils/route'

const hasAgreed = ref(true)
const isLoggingIn = ref(false)
const MINI_ACCOUNT_ERROR_CODES = new Set([-11003, -11004, -11005, -11011, -11013])

const toggleAgreement = () => {
  hasAgreed.value = !hasAgreed.value
}

const showAgreementToast = () => {
  uni.showToast({
    title: '请先阅读并同意用户协议和隐私政策',
    icon: 'none'
  })
}

const isErrorObject = (error: unknown): error is Record<string, unknown> => {
  return typeof error === 'object' && error !== null
}

const getErrorCode = (error: unknown) => {
  if (!isErrorObject(error) || typeof error.code !== 'number') {
    return undefined
  }

  return error.code
}

const getErrorMessage = (error: unknown) => {
  if (!isErrorObject(error) || typeof error.message !== 'string') {
    return ''
  }

  return error.message
}

const isAccountError = (error: unknown) => {
  const errorCode = getErrorCode(error)
  if (errorCode !== undefined) {
    return MINI_ACCOUNT_ERROR_CODES.has(errorCode)
  }

  return /账户|开户|异常/.test(getErrorMessage(error))
}

const handleLoginWithoutAgreement = () => {
  showAgreementToast()
}

const handleWechatPhoneLogin = async (event: WechatPhoneCodeEvent) => {
  if (!hasAgreed.value) {
    showAgreementToast()
    return
  }

  if (isLoggingIn.value) {
    return
  }

  isLoggingIn.value = true

  try {
    const phoneCode = getWechatPhoneCode(event)
    const loginCode = await getWechatLoginCode()
    const loginResponse = await miniLogin({
      loginCode,
      phoneCode
    })

    saveMiniAccessToken(loginResponse)
    uni.redirectTo({
      url: miniRoute.home
    })
  } catch (error) {
    if (isAccountError(error)) {
      uni.redirectTo({
        url: miniRoute.accountError
      })
      return
    }

    uni.showToast({
      title: getErrorMessage(error) || '登录失败，请重试',
      icon: 'none'
    })
  } finally {
    isLoggingIn.value = false
  }
}

const openUserAgreement = () => {
  uni.navigateTo({
    url: miniRoute.userAgreement
  })
}

const openPrivacyPolicy = () => {
  uni.navigateTo({
    url: miniRoute.privacyPolicy
  })
}

onLoad((query) => {
  setMockMiniLoginScenario(query?.mockLogin === 'accountError' ? 'accountError' : 'success')
})
</script>

<template>
  <view class="login-page">
    <view class="hero-section">
      <view class="title-area">
        <text class="page-title">EMS4J 能耗管理系统</text>
        <text class="page-subtitle">智慧用能 · 节能高效</text>
      </view>

      <view class="feature-list">
        <view class="feature-item">
          <view class="feature-icon shield-icon">
            <image class="feature-icon-image" src="/static/icons/login-shield.svg" mode="aspectFit" />
          </view>
          <view class="feature-copy">
            <text class="feature-title">安全可靠</text>
            <text class="feature-desc">数据加密传输，多重安全防护</text>
          </view>
        </view>

        <view class="feature-item">
          <view class="feature-icon chart-icon">
            <image class="feature-icon-image" src="/static/icons/login-chart.svg" mode="aspectFit" />
          </view>
          <view class="feature-copy">
            <text class="feature-title">智能管理</text>
            <text class="feature-desc">实时监控能耗，助力节能增效</text>
          </view>
        </view>
      </view>

      <image class="hero-image" src="/static/stitch/hero-building.png" mode="aspectFit" />
    </view>

    <view class="login-panel">
      <button
        v-if="hasAgreed"
        :class="['wechat-login-button', isLoggingIn ? 'is-logging-in' : '']"
        open-type="getPhoneNumber"
        :disabled="isLoggingIn"
        @getphonenumber="handleWechatPhoneLogin"
      >
        <image class="wechat-login-icon" src="/static/icons/wechat-white.svg" mode="aspectFit" />
        <text>微信一键登录</text>
      </button>
      <button
        v-else
        :class="['wechat-login-button', isLoggingIn ? 'is-logging-in' : '']"
        :disabled="isLoggingIn"
        @click="handleLoginWithoutAgreement"
      >
        <image class="wechat-login-icon" src="/static/icons/wechat-white.svg" mode="aspectFit" />
        <text>微信一键登录</text>
      </button>

      <view class="agreement-row">
        <view
          :class="['checked-circle', hasAgreed ? 'is-checked' : '']"
          @click="toggleAgreement"
        ></view>
        <text class="agreement-text">我已阅读并同意</text>
        <text class="agreement-link" @click="openUserAgreement">《用户服务协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link" @click="openPrivacyPolicy">《隐私政策》</text>
      </view>

      <view class="welcome-block">
        <text class="welcome-title">欢迎使用 EMS4J 能耗管理系统</text>
        <text class="welcome-subtitle">一站式能耗监测与管理平台</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@use "sass:math";

@function design-rpx($px) {
  @return math.div($px * 750rpx, 390);
}

.login-page {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 750rpx;
  max-width: 100%;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(180deg, #e6f0fa 0%, #f5f9ff 30%, #ffffff 100%);
}

.hero-section {
  position: relative;
  flex-shrink: 0;
  height: design-rpx(404);
  padding: design-rpx(58) design-rpx(24) 0;
}

.title-area {
  position: relative;
  z-index: 2;
  margin-bottom: design-rpx(32);
}

.page-title,
.page-subtitle,
.feature-title,
.feature-desc,
.welcome-title,
.welcome-subtitle {
  display: block;
}

.page-title {
  margin-bottom: design-rpx(8);
  color: #152234;
  font-size: design-rpx(18);
  font-weight: 600;
  line-height: 1.25;
  text-shadow: 0 design-rpx(1) design-rpx(2) rgba(0, 0, 0, 0.05);
}

.page-subtitle {
  color: #6a7a8f;
  font-size: design-rpx(14);
  letter-spacing: design-rpx(1);
}

.feature-list {
  position: relative;
  z-index: 2;
  width: 64%;
  margin-top: design-rpx(18);
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: design-rpx(16);
  padding: design-rpx(8) 0;
}

.feature-item + .feature-item {
  margin-top: design-rpx(36);
}

.feature-icon {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: design-rpx(44);
  height: design-rpx(44);
  color: #2563eb;
  background: #ffffff;
  border-radius: design-rpx(12);
  box-shadow: 0 design-rpx(2) design-rpx(8) rgba(12, 43, 71, 0.08);
}

.feature-icon-image {
  width: design-rpx(22);
  height: design-rpx(22);
}

.feature-copy {
  min-width: 0;
  padding-top: design-rpx(4);
}

.feature-title {
  color: #2563eb;
  font-size: design-rpx(15);
  font-weight: 700;
  line-height: 1.2;
}

.feature-desc {
  margin-top: design-rpx(4);
  color: #6a7a8f;
  font-size: design-rpx(11);
  line-height: 1.25;
  white-space: nowrap;
}

.hero-image {
  position: absolute;
  top: design-rpx(76);
  right: design-rpx(-80);
  z-index: 1;
  width: design-rpx(456);
  height: design-rpx(456);
}

.login-panel {
  position: relative;
  z-index: 3;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  min-height: 0;
  margin-top: 0;
  padding: design-rpx(20) design-rpx(24) design-rpx(26);
  background: rgba(255, 255, 255, 0.92);
  border: design-rpx(0.5) solid rgba(255, 255, 255, 0.5);
  border-radius: design-rpx(40) design-rpx(40) 0 0;
  box-shadow: 0 design-rpx(-10) design-rpx(40) rgba(0, 0, 0, 0.03);
}

.wechat-login-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: design-rpx(8);
  width: 100%;
  height: design-rpx(56);
  color: #ffffff;
  font-size: design-rpx(18);
  font-weight: 600;
  letter-spacing: design-rpx(1);
  background: linear-gradient(90deg, #60d179 0%, #2ab64f 100%);
  border-radius: 999rpx;
  box-shadow: 0 design-rpx(4) design-rpx(14) rgba(42, 182, 79, 0.39);
}

.wechat-login-button.is-logging-in {
  color: rgba(255, 255, 255, 0.72);
}

.wechat-login-icon {
  width: design-rpx(24);
  height: design-rpx(24);
}

.agreement-row {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-top: design-rpx(24);
  margin-bottom: design-rpx(48);
  white-space: nowrap;
}

.checked-circle {
  position: relative;
  flex-shrink: 0;
  width: design-rpx(18);
  height: design-rpx(18);
  margin-right: design-rpx(8);
  background: #ffffff;
  border: design-rpx(1.5) solid #cbd5e1;
  border-radius: 999rpx;
  transition: background-color 120ms ease, border-color 120ms ease, transform 120ms ease;
}

.checked-circle:active {
  transform: scale(0.9);
}

.checked-circle.is-checked {
  background: #07c160;
  border-color: #07c160;
}

.checked-circle::after {
  position: absolute;
  top: 50%;
  left: 50%;
  width: design-rpx(8);
  height: design-rpx(4);
  border-left: design-rpx(2) solid #ffffff;
  border-bottom: design-rpx(2) solid #ffffff;
  transform: translate(-50%, -62%) rotate(-45deg);
  transform-origin: center;
  content: "";
  opacity: 0;
}

.checked-circle.is-checked::after {
  opacity: 1;
}

.agreement-text,
.agreement-link {
  font-size: design-rpx(12);
}

.agreement-text {
  color: #6a7a8f;
}

.agreement-link {
  margin: 0 design-rpx(2);
  color: #3b82f6;
}

.welcome-block {
  margin-top: auto;
  margin-bottom: design-rpx(24);
  text-align: center;
}

.welcome-title {
  margin-bottom: design-rpx(8);
  color: #152234;
  font-size: design-rpx(18);
  font-weight: 600;
}

.welcome-subtitle {
  color: #6a7a8f;
  font-size: design-rpx(14);
}

</style>
