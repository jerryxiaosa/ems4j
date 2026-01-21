package info.zhihui.ems.business.finance.service.order.thirdparty.wx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.WxOrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxPayConfig;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxPrepayQuery;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationResponseDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyPrepayEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.exception.PayAmountException;
import info.zhihui.ems.business.finance.repository.order.OrderThirdPartyPrepayRepository;
import info.zhihui.ems.business.finance.service.order.thirdparty.OrderThirdPartyHandler;
import info.zhihui.ems.business.finance.service.order.thirdparty.wx.sdk.WxMiniProgramPaySdk;
import info.zhihui.ems.business.finance.utils.MoneyUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.foundation.system.service.ConfigService;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.message.order.delay.OrderDelayCheckMessage;
import info.zhihui.ems.mq.api.service.MqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.WX_PAY_CONFIG;

/**
 * 微信小程序第三方支付处理。
 *
 * @author jerryxiaosa
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WxMiniProgramHandler implements OrderThirdPartyHandler<WxOrderCreationResponseDto> {
    private final WxMiniProgramPaySdk sdk;
    private final ConfigService configService;
    private final OrderThirdPartyPrepayRepository orderThirdPartyPrepayRepository;
    private final MqService mqService;

    private static final long EXPIRE_MINUTES = 15L;

    /**
     * 获取支付渠道类型
     *
     * @return 支付渠道枚举
     */
    @Override
    public PaymentChannelEnum getPaymentChannel() {
        return PaymentChannelEnum.WX_MINI;
    }

    /**
     * 创建订单时的处理逻辑
     * 在第三方支付平台创建对应的支付订单
     *
     * @param orderBo 订单业务对象
     * @return 订单创建响应DTO
     */
    @Override
    public WxOrderCreationResponseDto onCreate(OrderBo orderBo) {
        if (!orderBo.getOrderStatus().equals(OrderStatusEnum.NOT_PAY)) {
            throw new BusinessRuntimeException("订单[" + orderBo.getOrderSn() + "]不是未支付状态，无法支付");
        }

        PrepayWithRequestPaymentResponse prepayResponse = getAndSavePrepay(orderBo);

        sendDelayMessageToCheck(orderBo.getOrderSn());

        WxOrderCreationResponseDto res = new WxOrderCreationResponseDto();
        res.setPrepayWithRequestPaymentResponse(prepayResponse)
                .setOrderSn(orderBo.getOrderSn())
                .setOrderTypeEnum(orderBo.getOrderType())
                .setPaymentChannel(getPaymentChannel())
                .setOrderPayStopTime(orderBo.getOrderPayStopTime());

        return res;
    }

    /**
     * 订单完成检查时的处理逻辑
     * 用于验证订单状态和执行完成后的业务逻辑
     *
     * @param orderBo 订单业务对象
     */
    @Override
    public void onCheckComplete(OrderBo orderBo) {
        WxPayConfig wxPayConfig = configService.getValueByKey(WX_PAY_CONFIG, new TypeReference<>() {
        });
        Transaction transaction = sdk.queryOrderByOutTradeNo(orderBo.getOrderSn(), wxPayConfig);

        if (!Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            throw new BusinessRuntimeException("微信订单[" + orderBo.getOrderSn() + "]没有支付完成");
        }

        Integer wxAmount = transaction.getAmount().getTotal();
        Integer selfOrderAmount = MoneyUtil.yuan2fen(orderBo.getUserPayAmount());

        orderThirdPartyPrepayRepository.updateByOrderSn(new OrderThirdPartyPrepayEntity()
                .setThirdPartySn(transaction.getTransactionId())
                .setOrderSn(orderBo.getOrderSn())
        );

        if (!Objects.equals(wxAmount, selfOrderAmount)) {
            log.error("微信订单[{}]支付金额不一致，应付{}(单位：分)，实付{}（单位：分）", orderBo.getOrderSn(), selfOrderAmount, wxAmount);
            throw new PayAmountException("微信订单[" + orderBo.getOrderSn() + "]支付金额不一致");
        }

        log.info("微信订单[{}]校验通过", orderBo.getOrderSn());
    }

    /**
     * 处理第三方支付平台的支付通知
     * 接收并处理来自第三方支付平台的异步通知
     *
     * @param notification 支付通知对象，具体类型由实现类定义
     * @return 订单状态对象
     */
    @Override
    public OrderThirdPartyNotificationResponseDto onPayNotify(Object notification) {
        RequestParam requestParam = (RequestParam) notification;
        WxPayConfig wxPayConfig = configService.getValueByKey(WX_PAY_CONFIG, new TypeReference<>() {
        });
        Transaction transaction = sdk.parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class);
        log.info("付款的回调内容是：{}", transaction);
        TransactionAmount transactionAmount = transaction.getAmount();

        if (!transactionAmount.getCurrency().equalsIgnoreCase("CNY")) {
            throw new BusinessRuntimeException("支付币种异常");
        }

        orderThirdPartyPrepayRepository.updateByOrderSn(new OrderThirdPartyPrepayEntity()
                .setThirdPartySn(transaction.getTransactionId())
                .setOrderSn(transaction.getOutTradeNo())
        );

        // 这里的三方订单金额应该与orderBo的用户支付金额一致
        // 所以这里关注的是订单金额，而不是支付金额
        Integer total = transactionAmount.getTotal();
        Integer payMount = transactionAmount.getPayerTotal();
        log.info("订单号: {}, 三方订单金额: {}, 实际支付金额: {}", transaction.getOutTradeNo(), total, payMount);
        Transaction.TradeStateEnum state = transaction.getTradeState();
        log.info("微信订单[{}]状态: {}", transaction.getOutTradeNo(), state);

        log.info("微信订单[{}]回调解析成功", transaction.getOutTradeNo());
        return new OrderThirdPartyNotificationResponseDto()
                .setOrderSn(transaction.getOutTradeNo())
                .setOrderStatus(covertWxOrderState(state))
                .setPayOrderAmount(MoneyUtil.fen2yuan(total));
    }

    private OrderStatusEnum covertWxOrderState(Transaction.TradeStateEnum state) {
        if (Transaction.TradeStateEnum.SUCCESS.equals(state)) {
            return OrderStatusEnum.SUCCESS;
        } else if (Transaction.TradeStateEnum.NOTPAY.equals(state)) {
            return OrderStatusEnum.NOT_PAY;
        } else if (Transaction.TradeStateEnum.CLOSED.equals(state)) {
            return OrderStatusEnum.CLOSED;
        } else {
            return OrderStatusEnum.PAY_ERROR;
        }
    }

    /**
     * 关闭订单时的处理逻辑
     * 在第三方支付平台关闭对应的支付订单
     *
     * @param orderBo 订单业务对象
     */
    @Override
    public void onClose(OrderBo orderBo) {
        try {
            WxPayConfig wxPayConfig = configService.getValueByKey(WX_PAY_CONFIG, new TypeReference<>() {
            });
            Transaction transaction = sdk.queryOrderByOutTradeNo(orderBo.getOrderSn(), wxPayConfig);

            // 已经关闭了就退出
            if (Transaction.TradeStateEnum.CLOSED.equals(transaction.getTradeState())) {
                log.info("对应的微信订单[{}]已是关闭状态，无需处理", orderBo.getOrderSn());
                return;
            }

            sdk.closePay(orderBo.getOrderSn(), wxPayConfig);
            log.info("对应的微信订单[{}]关闭成功", orderBo.getOrderSn());
        } catch (Exception e) {
            log.error("关闭微信订单[{}]失败：", orderBo.getOrderSn(), e);
            throw new BusinessRuntimeException("关闭微信订单[" + orderBo.getOrderSn() + "]异常：" + e.getMessage());
        }
    }

    private PrepayWithRequestPaymentResponse getAndSavePrepay(OrderBo orderBo) {
        WxPrepayQuery prepayQuery = new WxPrepayQuery()
                .setOutTradeNo(orderBo.getOrderSn())
                .setDescription(orderBo.getOrderType().getInfo())
                .setExpireTime(orderBo.getOrderPayStopTime())
                // 需要支付的金额为OrderBo计算的用户支付金额
                .setAmount(orderBo.getUserPayAmount())
                .setOpenId(orderBo.getThirdPartyUserId());
        WxPayConfig wxPayConfig = configService.getValueByKey(WX_PAY_CONFIG, new TypeReference<>() {
        });

        String prepayId = sdk.getPrepayId(prepayQuery, wxPayConfig);
        OrderThirdPartyPrepayEntity orderThirdPartyPrepayEntity = new OrderThirdPartyPrepayEntity()
                .setOrderSn(orderBo.getOrderSn())
                .setPrepayId(prepayId)
                .setThirdPartyUserId(orderBo.getThirdPartyUserId())
                .setPrepayAt(LocalDateTime.now());
        orderThirdPartyPrepayRepository.insert(orderThirdPartyPrepayEntity);

        return sdk.getPrepayResponseByPrePayId(prepayId, wxPayConfig);
    }

    /**
     * 发送延迟消息，校验订单状态
     */
    private void sendDelayMessageToCheck(String orderSn) {
        Object payload = new OrderDelayCheckMessage().setOrderSn(orderSn).setDelaySeconds(EXPIRE_MINUTES * 60);
        mqService.sendMessageAfterCommit(new MqMessage()
                .setMessageDestination(OrderConstant.ORDER_DELAY_DESTINATION)
                .setRoutingIdentifier(OrderConstant.ROUTING_DELAYED_ORDER_CHECK)
                .setPayload(payload)
        );

    }

}