<script setup lang="ts">
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import { miniRoute } from '@/utils/route'

type PolicySection = {
  title: string
  paragraphs?: string[]
  items?: string[]
}

const privacySections: PolicySection[] = [
  {
    title: '一、我们如何收集和使用您的信息',
    paragraphs: [
      '为了向您提供账户查看、用电查询、充值缴费、账单明细、缴费记录和电表管理等服务，我们会在必要范围内收集和使用与服务相关的信息。'
    ],
    items: [
      '账号识别信息：微信 OpenID、会话标识、授权状态，用于登录、识别用户身份和保持登录状态。',
      '账户关联信息：房间、账户名称、账户编号、电表编号、账户状态、账户余额，用于展示您可管理的账户和电表。',
      '用电与账单信息：用电量、分时电量、账单月份、账单金额、结算状态，用于账单查询和用能分析。',
      '充值缴费信息：充值金额、服务费、应付金额、支付渠道、订单编号、支付状态、支付时间，用于完成支付、展示记录和处理售后核查。',
      '设备与日志信息：设备型号、系统版本、网络状态、访问时间、操作日志、异常日志，用于安全风控、问题排查和服务稳定性保障。'
    ]
  },
  {
    title: '二、我们不会主动收集的信息',
    items: [
      '除非具体功能明确提示并获得您的授权，我们不会主动收集您的精确地理位置。',
      '我们不会主动读取您的通讯录、相册、麦克风、摄像头等与能耗管理无关的信息。',
      '支付银行卡号、支付密码等敏感支付凭证由微信支付等支付机构处理，本小程序不会保存该类完整支付凭证。'
    ]
  },
  {
    title: '三、第三方服务',
    paragraphs: [
      '为实现微信登录、微信支付、小程序运行环境和消息能力，本小程序会调用微信提供的基础能力。第三方服务可能按照其隐私规则处理必要信息。'
    ],
    items: [
      '微信登录能力：用于识别微信用户身份。',
      '微信支付能力：用于完成充值缴费支付和支付结果通知。',
      '小程序运行环境：用于页面渲染、网络请求、安全校验和异常排查。'
    ]
  },
  {
    title: '四、信息存储与保护',
    items: [
      '我们会在实现服务目的所必需的期限内保存您的信息。',
      '充值订单、账单、用电数据等业务记录可能根据财务、审计、争议处理和运营管理需要保存更长时间。',
      '我们会采用访问控制、传输加密、权限隔离、日志审计等措施保护信息安全。',
      '如果发生可能影响您权益的信息安全事件，我们会按法律法规要求及时处理并进行必要告知。'
    ]
  },
  {
    title: '五、信息共享、转让和公开披露',
    paragraphs: [
      '我们不会将您的个人信息出售给任何第三方。仅在以下必要场景中，可能共享或披露相关信息。'
    ],
    items: [
      '为完成支付、退款、对账或交易核查，向支付机构、运营方或相关服务方提供必要订单信息。',
      '为处理账户、电表、账单、设备异常，向您所在物业、园区、学校、企业或能源服务单位提供必要核查信息。',
      '根据法律法规、监管要求、司法机关或行政机关依法提出的要求进行披露。',
      '在取得您明确同意的其他场景下进行共享。'
    ]
  },
  {
    title: '六、您的权利',
    items: [
      '您可以在小程序中查看账户、电表、账单、充值缴费记录等信息。',
      '如发现信息错误，您可以联系运营方申请更正。',
      '您可以撤回授权或停止使用相关服务，但可能导致部分功能不可用。',
      '如您希望注销账户关系、删除信息或获取进一步说明，可以通过运营方公布的客服渠道提交申请。'
    ]
  },
  {
    title: '七、未成年人保护',
    paragraphs: [
      '本小程序主要面向具备相应民事行为能力的账户使用人。如未成年人需要使用，应在监护人指导和同意下使用。监护人发现未成年人信息被不当收集或使用的，可以联系我们处理。'
    ]
  },
  {
    title: '八、政策更新',
    paragraphs: [
      '我们可能根据法律法规、产品功能或服务范围变化更新本政策。更新后会在小程序中展示新版本。若涉及重要变化，我们会以适当方式提示您。',
      '如您继续使用本小程序，即表示您已阅读并理解更新后的隐私政策。'
    ]
  },
  {
    title: '九、联系我们',
    paragraphs: [
      '如果您对个人信息处理、隐私保护或本政策有疑问，可以通过您所在物业、园区、学校、企业或能源服务单位公布的客服渠道联系我们。'
    ]
  }
]

const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack()
    return
  }

  uni.redirectTo({
    url: miniRoute.login
  })
}
</script>

<template>
  <view class="policy-page">
    <AppBackHeader title="隐私政策" @back="handleBack" />

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="section-card">
          <view
            v-for="section in privacySections"
            :key="section.title"
            class="policy-section"
          >
            <text class="section-title">{{ section.title }}</text>
            <text
              v-for="paragraph in section.paragraphs"
              :key="paragraph"
              class="section-paragraph"
            >
              {{ paragraph }}
            </text>
            <view
              v-for="item in section.items"
              :key="item"
              class="section-item"
            >
              <view class="item-dot"></view>
              <text>{{ item }}</text>
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

.policy-page {
  display: flex;
  flex-direction: column;
  width: 750rpx;
  max-width: 100%;
  height: 100vh;
  overflow: hidden;
  color: #06133d;
  background: linear-gradient(180deg, #f6faff 0%, #ffffff 100%);
}

.page-scroll {
  flex: 1;
  min-height: 0;
}

.content-stack {
  padding: design-rpx(16) design-rpx(22) design-rpx(36);
}

.section-card {
  background: #ffffff;
  border: design-rpx(1) solid rgba(219, 228, 242, 0.72);
  border-radius: design-rpx(20);
  box-shadow: 0 design-rpx(8) design-rpx(24) rgba(6, 19, 61, 0.04);
}

.section-title,
.section-paragraph {
  display: block;
}

.section-card {
  padding: design-rpx(20);
}

.policy-section + .policy-section {
  margin-top: design-rpx(24);
}

.section-title {
  color: #152234;
  font-size: design-rpx(16);
  font-weight: 700;
  line-height: 1.4;
}

.section-paragraph {
  margin-top: design-rpx(10);
  color: #526176;
  font-size: design-rpx(14);
  font-weight: 400;
  line-height: 1.75;
}

.section-item {
  display: flex;
  align-items: flex-start;
  gap: design-rpx(8);
  margin-top: design-rpx(10);
  color: #526176;
  font-size: design-rpx(14);
  font-weight: 400;
  line-height: 1.75;
}

.section-item text {
  flex: 1;
}

.item-dot {
  flex-shrink: 0;
  width: design-rpx(5);
  height: design-rpx(5);
  margin-top: design-rpx(10);
  background: #1677ff;
  border-radius: 999rpx;
}
</style>
