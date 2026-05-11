<script setup lang="ts">
import AppBackHeader from '@/components/common/AppBackHeader.vue'
import { miniRoute } from '@/utils/route'

type AgreementSection = {
  title: string
  paragraphs?: string[]
  items?: string[]
}

const agreementSections: AgreementSection[] = [
  {
    title: '一、服务说明',
    paragraphs: [
      'EMS4J 能耗管理系统小程序为用电账户、智能电表、账单查询、充值缴费、缴费记录等场景提供线上服务。具体可用功能以您所在物业、园区、学校、企业或能源服务单位实际开通的功能为准。',
      '本小程序中的账户余额、电表状态、用电量、账单、充值记录等信息，来源于业务系统、支付渠道、计量设备或人工维护数据。由于设备通信、网络传输、系统同步等因素，页面展示可能存在合理延迟。'
    ]
  },
  {
    title: '二、账号与身份',
    items: [
      '您可以通过微信授权、手机号或运营方配置的账户关系使用本小程序。',
      '您应确保使用的微信账号、手机号和关联房间、账户、电表信息真实、合法、有效。',
      '如果发现账户关联错误、账户异常、余额异常或账单异常，请及时联系运营方或客服处理。'
    ]
  },
  {
    title: '三、充值缴费规则',
    items: [
      '您在小程序中发起充值缴费时，应仔细确认账户、电表、充值金额、服务费、应付金额和支付渠道。',
      '支付完成后，充值结果可能因支付渠道回调、业务系统入账或设备通信存在短暂延迟。',
      '如因网络中断、重复支付、账户选择错误等原因需要核查或退款，请以支付渠道记录和运营方业务系统记录为依据。',
      '如果页面展示金额与实际扣费、开票或线下结算规则存在差异，以运营方确认的最终结算信息为准。'
    ]
  },
  {
    title: '四、用电数据与账单',
    items: [
      '用电量、分时电量、账单金额等数据可能来自智能电表、网关、后台计算任务或人工校正。',
      '实时数据仅用于辅助查看，月度账单、结算账单以系统完成结算后的数据为准。',
      '如发现电表读数、账单、电价方案或账户类型异常，请及时联系运营方核验。'
    ]
  },
  {
    title: '五、用户行为规范',
    items: [
      '不得冒用他人身份、账户、电表或支付信息使用本服务。',
      '不得通过脚本、爬虫、抓包、逆向工程等方式干扰小程序、接口或计量设备。',
      '不得上传、传播违法违规、虚假、侵权或影响系统安全稳定的内容。',
      '不得利用系统漏洞进行异常充值、恶意退款、篡改账单或破坏设备控制。'
    ]
  },
  {
    title: '六、服务变更与中断',
    paragraphs: [
      '为保障系统安全、优化体验或满足业务管理需要，我们可能对功能、页面、规则、接口或服务范围进行调整。',
      '因系统维护、设备离线、网络故障、第三方支付服务异常、不可抗力等原因导致服务暂时不可用的，我们会尽力恢复，但不承诺服务在任何时间均不中断。'
    ]
  },
  {
    title: '七、责任限制',
    paragraphs: [
      '在法律允许范围内，对于因您操作不当、账号保管不善、选择错误账户或电表、网络异常、第三方服务异常等原因造成的损失，我们不承担超出法律规定和实际服务能力范围的责任。',
      '本小程序提供的数据展示和操作入口不替代运营方的最终业务管理、线下核验和正式结算文件。'
    ]
  },
  {
    title: '八、协议更新',
    paragraphs: [
      '我们可能根据业务变化、法律法规或产品功能调整本协议。协议更新后，将在小程序页面展示更新后的版本。',
      '如果您继续使用本小程序，即表示您已阅读并接受更新后的协议。如您不同意更新内容，可以停止使用相关服务。'
    ]
  },
  {
    title: '九、联系我们',
    paragraphs: [
      '如您对本协议、账户、充值、账单或隐私保护有疑问，请通过您所在物业、园区、学校、企业或能源服务单位公布的客服渠道联系我们。'
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
    <AppBackHeader title="用户服务协议" @back="handleBack" />

    <scroll-view class="page-scroll" scroll-y enhanced show-scrollbar="false">
      <view class="content-stack">
        <view class="section-card">
          <view
            v-for="section in agreementSections"
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
