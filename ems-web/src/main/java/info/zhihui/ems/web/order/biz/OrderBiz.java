package info.zhihui.ems.web.order.biz;

import com.wechat.pay.java.core.notification.RequestParam;
import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationDto;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.service.order.core.OrderQueryService;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.order.mapstruct.OrderWebMapper;
import info.zhihui.ems.web.order.vo.EnergyOrderCreateVo;
import info.zhihui.ems.web.order.vo.OrderDetailVo;
import info.zhihui.ems.web.order.vo.OrderCreationResponseVo;
import info.zhihui.ems.web.order.vo.OrderQueryVo;
import info.zhihui.ems.web.order.vo.OrderVo;
import info.zhihui.ems.web.order.vo.TerminationOrderCreateVo;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * 订单业务编排
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBiz {

    private final OrderQueryService orderQueryService;
    private final OrderService orderService;
    private final OrderWebMapper orderWebMapper;

    /**
     * 查询订单列表
     */
    public PageResult<OrderVo> findOrdersPage(OrderQueryVo queryVo, Integer pageNum, Integer pageSize) {
        OrderQueryDto queryDto = orderWebMapper.toOrderQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new OrderQueryDto();
        }
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        return orderWebMapper.toOrderVoPage(orderQueryService.findOrdersPage(queryDto, pageParam));
    }

    /**
     * 查询订单详情
     */
    public OrderDetailVo getOrderDetail(String orderSn) {
        OrderBo order = orderService.getDetail(orderSn);
        return orderWebMapper.toOrderDetailVo(order);
    }

    /**
     * 关闭订单
     */
    public void closeOrder(String orderSn) {
        orderService.close(orderSn);
    }

    /**
     * 处理微信支付回调
     */
    public void answerWeiXinPayNotify(HttpServletRequest request) {
        RequestParam requestParam = buildRequestParam(request);
        OrderThirdPartyNotificationDto notification = new OrderThirdPartyNotificationDto()
                .setPaymentChannel(PaymentChannelEnum.WX_MINI)
                .setData(requestParam);
        orderService.answerPayNotify(notification);
    }

    /**
     * 创建能耗充值订单
     */
    public OrderCreationResponseVo createEnergyTopUpOrder(EnergyOrderCreateVo createVo) {
        OrderCreationResponseDto responseDto =
                orderService.createOrder(orderWebMapper.toEnergyOrderCreationInfoDto(createVo));
        return orderWebMapper.toOrderCreationResponseVo(responseDto);
    }

    /**
     * 创建销户结算订单
     */
    public OrderCreationResponseVo createTerminationOrder(TerminationOrderCreateVo createVo) {
        var dto = orderWebMapper.toTerminationOrderCreationInfoDto(createVo);
        dto.setPaymentChannel(PaymentChannelEnum.OFFLINE);
        OrderCreationResponseDto responseDto = orderService.createOrder(dto);
        return orderWebMapper.toOrderCreationResponseVo(responseDto);
    }

    private RequestParam buildRequestParam(HttpServletRequest request) {
        //随机串
        String nonceStr = request.getHeader("Wechatpay-Nonce");
        log.debug("Wechatpay-Nonce: {}", nonceStr);

        //微信传递过来的签名
        String signature = request.getHeader("Wechatpay-Signature");
        log.debug("Wechatpay-Signature: {}", signature);

        //证书序列号（微信平台）
        String serialNo = request.getHeader("Wechatpay-Serial");
        log.debug("Wechatpay-Serial: {}", serialNo);

        //时间戳
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        log.debug("Wechatpay-Timestamp: {}", timestamp);

        //消息体
        String body = getRequestBody(request);
        log.debug("body: {}", body);

        return new RequestParam.Builder()
                .signature(signature)
                .nonce(nonceStr)
                .timestamp(timestamp)
                .serialNumber(serialNo)
                .body(body)
                .build();
    }

    private String getRequestBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (ServletInputStream inputStream = request.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error("读取微信支付回调请求体异常", e);
            throw new RuntimeException("读取微信支付回调请求体失败", e);
        }

        return sb.toString();
    }
}
