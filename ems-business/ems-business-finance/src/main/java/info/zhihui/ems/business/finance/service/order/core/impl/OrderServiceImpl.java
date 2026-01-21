package info.zhihui.ems.business.finance.service.order.core.impl;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.creation.OrderCreationInfoDto;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationResponseDto;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.exception.PayAmountException;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.business.finance.service.order.handler.completion.OrderCompletionHandler;
import info.zhihui.ems.business.finance.service.order.handler.creation.OrderCreationHandler;
import info.zhihui.ems.business.finance.service.order.thirdparty.OrderThirdPartyHandler;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.OrderChangeStatusMessage;
import info.zhihui.ems.mq.api.service.MqService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * <p>该类负责处理订单创建的核心业务逻辑，采用策略模式设计：</p>
 * <ul>
 *   <li>根据订单创建信息的类型，动态选择对应的订单创建处理器</li>
 *   <li>根据支付渠道，动态选择对应的第三方支付处理器</li>
 * </ul>
 *
 * @author jerryxiaosa
 */
@Service
@Slf4j
@Validated
public class OrderServiceImpl implements OrderService {

    private static final String LOCK_ORDER_TEMPLATE = "LOCK:ORDER:%s";

    /**
     * 订单创建处理器映射表
     * <p>key: 订单创建信息DTO的类型，value: 对应的订单创建处理器</p>
     * <p>用于根据不同的订单类型的创建信息（如充值订单、销户订单等）选择相应的处理逻辑</p>
     */
    private final Map<Class<? extends OrderCreationInfoDto>, OrderCreationHandler> creationHandlerMap;

    /**
     * 订单完成处理器映射表
     * <p>key: 订单类型枚举，value: 对应的订单完成处理器</p>
     * <p>用于根据订单类型（如充值订单、销户订单等）选择相应的处理逻辑</p>
     */
    private final Map<OrderTypeEnum, OrderCompletionHandler> completionHandlerMap;

    /**
     * 第三方支付处理器映射表
     * <p>key: 支付渠道枚举，value: 对应的第三方支付处理器</p>
     * <p>用于根据不同的支付渠道（如微信支付、支付宝等）选择相应的支付处理逻辑</p>
     */
    private final Map<PaymentChannelEnum, OrderThirdPartyHandler<? extends OrderCreationResponseDto>> thirdPartyHandlerMap;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final LockTemplate lockTemplate;
    private final MqService mqService;

    /**
     * 构造函数，初始化订单服务
     *
     * <p>通过依赖注入获取所有的订单创建处理器和第三方支付处理器，
     * 并构建对应的映射表以支持策略模式的动态选择</p>
     *
     * @param createHandlers        所有订单创建处理器的列表，Spring会自动注入所有实现了OrderCreationHandler接口的Bean
     * @param thirdPartyHandlerList 所有第三方支付处理器的列表，Spring会自动注入所有实现了OrderThirdPartyHandler接口的Bean
     */
    @Autowired
    public OrderServiceImpl(List<OrderCreationHandler> createHandlers,
                            List<OrderCompletionHandler> completionHandlerList,
                            List<OrderThirdPartyHandler<? extends OrderCreationResponseDto>> thirdPartyHandlerList,
                            OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            LockTemplate lockTemplate,
                            MqService mqService
    ) {
        this.creationHandlerMap = toHandlerMap(createHandlers, OrderCreationHandler::getSupportedParam);
        this.completionHandlerMap = toHandlerMap(completionHandlerList, OrderCompletionHandler::getOrderType);
        this.thirdPartyHandlerMap = toHandlerMap(thirdPartyHandlerList, OrderThirdPartyHandler::getPaymentChannel);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.lockTemplate = lockTemplate;
        this.mqService = mqService;
    }

    /**
     * 将处理器列表转换为不可变的映射
     */
    private static <K, V> Map<K, V> toHandlerMap(List<V> handlers, Function<V, K> keyExtractor) {
        return handlers.stream().collect(Collectors.toUnmodifiableMap(keyExtractor, Function.identity()));
    }

    /**
     * 获取订单详情
     *
     * @param orderSn 订单编号
     * @return 订单详情
     */
    @Override
    public OrderBo getDetail(@NotEmpty String orderSn) {
        OrderEntity orderEntity = orderRepository.selectByOrderSn(orderSn);
        if (orderEntity == null || Boolean.TRUE.equals(orderEntity.getIsDeleted())) {
            throw new NotFoundException("订单信息不存在");
        }
        return orderMapper.toBo(orderEntity);
    }

    /**
     * 创建订单
     *
     * <p>该方法实现了订单创建的完整流程：</p>
     * <ol>
     *   <li>根据订单创建信息的类型，选择对应的订单创建处理器</li>
     *   <li>调用处理器处理订单创建逻辑，生成订单业务对象</li>
     *   <li>根据订单的支付渠道，选择对应的第三方支付处理器</li>
     *   <li>调用第三方支付处理器，完成支付相关处理并返回结果</li>
     * </ol>
     *
     * @param orderCreationInfoDto 订单创建信息DTO，包含创建订单所需的所有信息
     *                             不同类型的订单对应不同的DTO子类（如能耗订单、房屋租赁订单等）
     * @return 订单创建响应DTO，包含订单创建结果和支付相关信息
     * @throws BusinessRuntimeException 当不支持的订单类型或支付渠道时抛出业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreationResponseDto createOrder(@Valid @NotNull OrderCreationInfoDto orderCreationInfoDto) {
        OrderCreationHandler creationHandler = resolveCreationHandler(orderCreationInfoDto);
        OrderBo orderBo = creationHandler.createOrder(orderCreationInfoDto);

        OrderThirdPartyHandler<? extends OrderCreationResponseDto> thirdPartyHandler = resolveThirdPartyHandler(orderCreationInfoDto.getPaymentChannel());
        return thirdPartyHandler.onCreate(orderBo);
    }

    /**
     * 响应三方支付回调通知
     *
     * @param notification 三方支付回调通知DTO，包含支付渠道、支付渠道返回的支付结果等信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void answerPayNotify(@Valid @NotNull OrderThirdPartyNotificationDto notification) {
        OrderThirdPartyHandler<? extends OrderCreationResponseDto> handler = resolveThirdPartyHandler(notification.getPaymentChannel());
        OrderThirdPartyNotificationResponseDto response = handler.onPayNotify(notification.getData());
        processWithLock(response.getOrderSn(), orderBo -> {
            if (OrderStatusEnum.SUCCESS.equals(orderBo.getOrderStatus()) || OrderStatusEnum.CLOSED.equals(orderBo.getOrderStatus())) {
                log.info("订单[{}]已经是成功状态，退出支付响应流程", response.getOrderSn());
                return;
            }

            if (isPayAmountMismatch(orderBo.getUserPayAmount(), response.getPayOrderAmount())) {
                log.error("订单支付金额不一致，应付{}(单位：元)，实付{}（单位：元）", orderBo.getUserPayAmount(), response.getPayOrderAmount());
                changeOrderStatus(orderBo, OrderStatusEnum.PAY_ERROR);
                return;
            }

            changeOrderStatus(orderBo, response.getOrderStatus());
        });

    }

    /**
     * 完成订单
     * 会根据订单的支付渠道进行校验
     *
     * @param orderSn 订单编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(@NotEmpty String orderSn) {
        processWithLock(orderSn, orderBo -> {
            if (OrderStatusEnum.SUCCESS.equals(orderBo.getOrderStatus()) || OrderStatusEnum.CLOSED.equals(orderBo.getOrderStatus())) {
                log.info("订单[{}]已经是完成状态，退出完成流程", orderSn);
                return;
            }

            OrderThirdPartyHandler<? extends OrderCreationResponseDto> handler = resolveThirdPartyHandler(orderBo.getPaymentChannel());
            try {
                handler.onCheckComplete(orderBo);
                changeOrderStatus(orderBo, OrderStatusEnum.SUCCESS);
            } catch (PayAmountException e) {
                log.error("订单[{}]支付金额校验失败，标记为PAY_ERROR", orderSn, e);
                changeOrderStatus(orderBo, OrderStatusEnum.PAY_ERROR);
            }
        });
    }

    /**
     * 关闭订单
     *
     * @param orderSn 订单编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(@NotEmpty String orderSn) {
        processWithLock(orderSn, orderBo -> {
            if (OrderStatusEnum.CLOSED.equals(orderBo.getOrderStatus())) {
                log.info("订单[{}]已经是关闭状态，退出关闭流程", orderSn);
                return;
            }

            OrderThirdPartyHandler<? extends OrderCreationResponseDto> handler = resolveThirdPartyHandler(orderBo.getPaymentChannel());
            handler.onClose(orderBo);

            changeOrderStatus(orderBo, OrderStatusEnum.CLOSED);
        });
    }

    /**
     * 封装订单加锁、加载、解锁的通用流程，调用方通过回调完成各自业务逻辑。
     */
    private void processWithLock(String orderSn, Consumer<OrderBo> action) {
        Lock lock = getOrderLock(orderSn);
        try {
            OrderBo orderBo = getDetail(orderSn);
            action.accept(orderBo);
        } finally {
            lock.unlock();
        }
    }

    private Lock getOrderLock(String orderSn) {
        Lock lock = lockTemplate.getLock(String.format(LOCK_ORDER_TEMPLATE, orderSn));
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("订单已被暂时锁定，请稍后重试");
        }

        return lock;
    }

    private boolean isPayAmountMismatch(BigDecimal expectedAmount, BigDecimal actualPaid) {
        if (expectedAmount == null || actualPaid == null) {
            return false;
        }
        return expectedAmount.compareTo(actualPaid) != 0;
    }

    private OrderCreationHandler resolveCreationHandler(OrderCreationInfoDto orderCreationInfoDto) {
        OrderCreationHandler handler = creationHandlerMap.get(orderCreationInfoDto.getClass());
        if (handler != null) {
            return handler;
        }

        throw new BusinessRuntimeException("不支持的订单类型: " + orderCreationInfoDto.getClass().getSimpleName());
    }

    private OrderThirdPartyHandler<? extends OrderCreationResponseDto> resolveThirdPartyHandler(PaymentChannelEnum paymentChannel) {
        if (paymentChannel == null) {
            throw new BusinessRuntimeException("订单支付渠道为空，无法处理");
        }

        OrderThirdPartyHandler<? extends OrderCreationResponseDto> thirdPartyHandler = thirdPartyHandlerMap.get(paymentChannel);
        if (thirdPartyHandler == null) {
            throw new BusinessRuntimeException("不支持的支付渠道: " + paymentChannel.name());
        }

        return thirdPartyHandler;
    }

    private void changeOrderStatus(OrderBo orderBo, OrderStatusEnum orderStatus) {
        log.info("订单[{}]状态由 {} 改为 {}", orderBo.getOrderSn(), orderBo.getOrderStatus(), orderStatus);
        changeOrderDbStatus(orderBo, orderStatus);

        if (OrderStatusEnum.SUCCESS.equals(orderStatus)) {
            sendSuccessMessage(orderBo);
        } else {
            sendStatusBroadcast(orderBo);
        }
    }

    private void changeOrderDbStatus(OrderBo orderBo, OrderStatusEnum orderStatus) {
        if (OrderStatusEnum.SUCCESS.equals(orderBo.getOrderStatus()) || OrderStatusEnum.CLOSED.equals(orderBo.getOrderStatus())) {
            log.error("订单已完成，无法改变订单状态。订单号：{}, 目标状态：{}, 当前状态：{}", orderBo.getOrderSn(), orderStatus, orderBo.getOrderStatus());
            throw new BusinessRuntimeException("订单已完成，无法改变订单状态");
        }

        OrderEntity orderEntity = new OrderEntity()
                .setOrderSn(orderBo.getOrderSn())
                .setOrderStatus(orderStatus.name());
        if (OrderStatusEnum.SUCCESS.equals(orderStatus)) {
            LocalDateTime now = LocalDateTime.now();
            orderEntity.setOrderSuccessTime(now);

            orderBo.setOrderSuccessTime(now);
            orderBo.setOrderStatus(orderStatus);
        } else {
            orderBo.setOrderStatus(orderStatus);
        }

        int updated = orderRepository.updateByOrderSn(orderEntity);
        if (updated != 1) {
            throw new BusinessRuntimeException("更新订单状态失败，订单号：" + orderBo.getOrderSn());
        }

    }

    private void sendSuccessMessage(OrderBo orderBo) {
        OrderCompletionHandler completionHandler = completionHandlerMap.get(orderBo.getOrderType());

        if (completionHandler == null) {
            log.warn("订单[{}]成功，但缺少完成处理器，orderType={}", orderBo.getOrderSn(), orderBo.getOrderType());
            return;
        }

        MqMessage mqMessage = completionHandler.createMessageAfterOrderSuccess(orderBo);
        if (mqMessage == null) {
            log.debug("订单[{}]成功，完成处理器未生成消息", orderBo.getOrderSn());
            return;
        }

        mqService.sendTransactionMessage(new TransactionMessageDto()
                .setBusinessType(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT)
                .setSn(orderBo.getOrderSn())
                .setMessage(mqMessage)
        );
        log.info("订单[{}]成功，发送事务消息：{}", orderBo.getOrderSn(), mqMessage);
    }

    private void sendStatusBroadcast(OrderBo orderBo) {
        MqMessage broadcastMessage = new MqMessage()
                .setMessageDestination(OrderConstant.ORDER_DESTINATION)
                .setRoutingIdentifier(OrderConstant.ROUTING_KEY_ORDER_STATUS)
                .setPayload(new OrderChangeStatusMessage()
                        .setOrderSn(orderBo.getOrderSn())
                        .setOrderStatus(orderBo.getOrderStatus().name())
                );
        mqService.sendMessageAfterCommit(broadcastMessage);
    }

}
