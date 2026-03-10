package info.zhihui.ems.web.order.biz;

import com.wechat.pay.java.core.notification.RequestParam;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.order.dto.OrderCreationResponseDto;
import info.zhihui.ems.business.order.dto.OrderDetailDto;
import info.zhihui.ems.business.order.dto.OrderQueryDto;
import info.zhihui.ems.business.order.dto.creation.EnergyOrderCreationInfoDto;
import info.zhihui.ems.business.order.dto.creation.EnergyTopUpDto;
import info.zhihui.ems.business.order.entity.OrderThirdPartyNotificationDto;
import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.business.order.service.core.OrderQueryService;
import info.zhihui.ems.business.order.service.core.OrderService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.order.mapstruct.OrderWebMapper;
import info.zhihui.ems.web.order.vo.EnergyOrderCreateVo;
import info.zhihui.ems.web.order.vo.OrderDetailVo;
import info.zhihui.ems.web.order.vo.OrderCreationResponseVo;
import info.zhihui.ems.web.order.vo.OrderQueryVo;
import info.zhihui.ems.web.order.vo.OrderVo;
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
    private final AccountInfoService accountInfoService;
    private final ElectricMeterInfoService electricMeterInfoService;

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
        OrderDetailDto orderDetail = orderQueryService.getOrderDetail(orderSn);
        return orderWebMapper.toOrderDetailVo(orderDetail);
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
        EnergyOrderCreationInfoDto creationInfoDto = orderWebMapper.toEnergyOrderCreationInfoDto(createVo);
        validateEnergyTopUpDetail(creationInfoDto.getEnergyTopUpDto());

        OrderCreationResponseDto responseDto = orderService.createOrder(creationInfoDto);
        return orderWebMapper.toOrderCreationResponseVo(responseDto);
    }

    private void validateEnergyTopUpDetail(EnergyTopUpDto detail) {
        AccountBo accountBo = accountInfoService.getById(detail.getAccountId());
        validateAccount(detail, accountBo);

        if (!BalanceTypeEnum.ELECTRIC_METER.equals(detail.getBalanceType())) {
            return;
        }
        if (detail.getMeterId() == null) {
            throw new BusinessRuntimeException("电表充值时电表ID不能为空");
        }

        ElectricMeterBo meterBo = electricMeterInfoService.getDetail(detail.getMeterId());
        validateMeter(detail, meterBo);
    }

    private void validateAccount(EnergyTopUpDto detail, AccountBo accountBo) {
        if (!Objects.equals(accountBo.getOwnerId(), detail.getOwnerId())) {
            throw new BusinessRuntimeException("账户归属者ID不匹配");
        }
        if (accountBo.getOwnerType() != detail.getOwnerType()) {
            throw new BusinessRuntimeException("账户归属者类型不匹配");
        }
        if (accountBo.getElectricAccountType() != detail.getElectricAccountType()) {
            throw new BusinessRuntimeException("电费账户类型不匹配");
        }
        detail.setOwnerName(accountBo.getOwnerName());
    }

    private void validateMeter(EnergyTopUpDto detail, ElectricMeterBo meterBo) {
        if (!Objects.equals(meterBo.getAccountId(), detail.getAccountId())) {
            throw new BusinessRuntimeException("电表不属于当前账户");
        }
        detail.setMeterName(meterBo.getMeterName());
        detail.setDeviceNo(meterBo.getDeviceNo());
        detail.setMeterType(MeterTypeEnum.ELECTRIC);
        detail.setSpaceId(meterBo.getSpaceId());
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
