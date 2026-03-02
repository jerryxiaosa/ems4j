package info.zhihui.ems.business.account;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.CancelAccountDto;
import info.zhihui.ems.business.account.dto.CancelAccountResponseDto;
import info.zhihui.ems.business.account.dto.OpenAccountDto;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountManagerService;
import info.zhihui.ems.business.device.dto.MeterCancelDetailDto;
import info.zhihui.ems.business.device.dto.MeterOpenDetailDto;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.qo.ElectricMeterQo;
import info.zhihui.ems.business.device.qo.ElectricMeterResetAccountQo;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.finance.entity.order.OrderDetailTerminationEntity;
import info.zhihui.ems.business.finance.entity.order.OrderEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.repository.order.OrderDetailTerminationRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccountManagerServiceImpl集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
public class AccountManagerServiceImplIntegrationTest {

    @Autowired
    private AccountManagerService accountManagerService;

    @Autowired
    private AccountInfoService accountInfoService;

    @Autowired
    private ElectricMeterRepository electricMeterRepository;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailTerminationRepository orderDetailTerminationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试openAccount方法的参数验证
     */
    @Test
    void testOpenAccount_ParameterValidation() {
        // 测试整个参数为null
        ConstraintViolationException nullParamException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.openAccount(null)
        );
        assertTrue(nullParamException.getMessage().contains("openAccountDto") ||
                nullParamException.getMessage().contains("null"));

        // 测试ownerId为null
        OpenAccountDto ownerIdNullDto = new OpenAccountDto()
                .setOwnerId(null)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));
        ConstraintViolationException ownerIdException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.openAccount(ownerIdNullDto)
        );
        assertTrue(ownerIdException.getMessage().contains("业主id不能为空"));

        // 测试ownerType为null
        OpenAccountDto ownerTypeNullDto = new OpenAccountDto()
                .setOwnerId(1)
                .setOwnerType(null)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));
        ConstraintViolationException ownerTypeException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.openAccount(ownerTypeNullDto)
        );
        assertTrue(ownerTypeException.getMessage().contains("业主类型不能为空"));

        // 测试electricAccountType为null
        OpenAccountDto accountTypeNullDto = new OpenAccountDto()
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricAccountType(null)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));
        ConstraintViolationException accountTypeException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.openAccount(accountTypeNullDto)
        );
        assertTrue(accountTypeException.getMessage().contains("电费账户类型不能为空"));

        // 测试多重校验错误
        OpenAccountDto multipleErrorsDto = new OpenAccountDto()
                .setOwnerId(null)
                .setOwnerType(null)
                .setElectricAccountType(null)
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));
        ConstraintViolationException multipleException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.openAccount(multipleErrorsDto)
        );
        String message = multipleException.getMessage();
        assertTrue(message.contains("电价计费类型不能为空") ||
                message.contains("业主ID不能为空") ||
                message.contains("业主类型不能为空"));
    }

    /**
     * 测试开户成功场景
     */
    @Test
    void testOpenAccount_Success() {
        electricMeterRepository.resetMeterAccountInfo(new ElectricMeterResetAccountQo().setMeterIds(List.of(1, 2)));
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setContactName("张三")
                .setContactPhone("13800138000")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMonthlyPayAmount(new BigDecimal("100.00"))
                .setElectricMeterList(List.of(
                        new MeterOpenDetailDto()
                                .setMeterId(1)
                                ,
                        new MeterOpenDetailDto()
                                .setMeterId(2)

                ));

        // When
        accountManagerService.openAccount(openAccountDto);

        // Then - 查询数据库验证账户数据
        AccountBo account = accountInfoService.getById(5);
        assertNotNull(account);
        assertEquals(OwnerTypeEnum.ENTERPRISE, account.getOwnerType());
        assertEquals("测试企业", account.getOwnerName());
        assertEquals("张三", account.getContactName());
        assertEquals("13800138000", account.getContactPhone());
        assertEquals(new BigDecimal("100.00"), account.getMonthlyPayAmount().setScale(2, RoundingMode.DOWN));

        // 查询数据库验证电表数据
        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(account.getId())));
        assertNotNull(meters);
        assertEquals(2, meters.size());

        // 验证电表1的数据
        ElectricMeterEntity meter1 = meters.stream()
                .filter(m -> m.getId().equals(1))
                .findFirst()
                .orElse(null);
        assertNotNull(meter1);
        assertEquals(account.getId(), meter1.getAccountId());

        // 验证电表2的数据
        ElectricMeterEntity meter2 = meters.stream()
                .filter(m -> m.getId().equals(2))
                .findFirst()
                .orElse(null);
        assertNotNull(meter2);
        assertEquals(account.getId(), meter2.getAccountId());
    }

    // ==================== closeAccount 方法测试用例 ====================

    /**
     * 测试closeAccount方法的参数验证
     */
    @Test
    void testCancelAccount_ParameterValidation() {
        // 测试整个参数为null
        ConstraintViolationException nullParamException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.cancelAccount(null)
        );
        assertTrue(nullParamException.getMessage().contains("closeAccountDto") ||
                nullParamException.getMessage().contains("null"));

        // 测试accountId为null
        CancelAccountDto accountIdNullDto = new CancelAccountDto()
                .setAccountId(null)
                .setRemark("测试销户")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));
        ConstraintViolationException accountIdException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.cancelAccount(accountIdNullDto)
        );
        assertTrue(accountIdException.getMessage().contains("账户ID不能为空"));

        // 测试电表ID为null
        CancelAccountDto meterIdNullDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("测试销户")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(null)));
        ConstraintViolationException meterIdException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.cancelAccount(meterIdNullDto)
        );
        assertTrue(meterIdException.getMessage().contains("电表ID不能为空"));

        // 测试电表列表为空
        CancelAccountDto emptyMeterListDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("测试销户")
                .setMeterList(Collections.emptyList());
        ConstraintViolationException emptyMeterListException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.cancelAccount(emptyMeterListDto)
        );
        assertTrue(emptyMeterListException.getMessage().contains("销户电表列表不能为空"));

        // 测试多重校验错误
        CancelAccountDto multipleErrorsDto = new CancelAccountDto()
                .setAccountId(null)
                .setRemark("测试销户")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(null)));
        ConstraintViolationException multipleException = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.cancelAccount(multipleErrorsDto)
        );
        String message = multipleException.getMessage();
        assertTrue(message.contains("账户ID不能为空") ||
                message.contains("电表ID不能为空"));
    }

    /**
     * 测试电表信息校验异常 - 电表不属于该账户
     */
    @Test
    void testCancelAccount_DeviceNotBelongToAccount() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("测试销户")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(99999))); // 不存在的电表ID

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            accountManagerService.cancelAccount(cancelAccountDto);
        });

        assertTrue(exception.getMessage().contains("输入的销户电表信息有误，请检查电表ID是否正确"));

    }

    /**
     * 测试多重校验错误
     */
    @Test
    void testCancelAccount_MultipleValidationErrors() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(null)
                .setRemark("测试销户")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(null)));

        // When & Then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> accountManagerService.cancelAccount(cancelAccountDto)
        );

        String message = exception.getMessage();
        assertTrue(message.contains("账户ID不能为空") ||
                message.contains("电表ID不能为空"));
    }

    /**
     * 测试按量计费账户正常销户（退费场景）
     */
    @Test
    void testCancelAccount_QuantityAccountRefund() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("按量计费销户退费测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)
                        .setPowerHigher(new BigDecimal("100.50"))));

        // When
        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        // Then - 验证返回结果
        assertNotNull(response);
        assertNotNull(response.getCancelNo());
        assertNotNull(response.getCleanBalanceType());
        assertNotNull(response.getAmount());

        // 验证退费场景的清算类型和金额
        assertTrue(response.getAmount().compareTo(BigDecimal.ZERO) >= 0,
                "退费场景金额应该大于等于0，实际金额: " + response.getAmount());

        // 查询数据库验证账户状态
        AccountBo account = accountInfoService.getById(1);
        assertNotNull(account, "账户不应为空");
        // 验证账户具体属性
        assertNotNull(account.getId(), "账户ID不应为空");
        assertNotNull(account.getOwnerType(), "账户类型不应为空");
        assertNotNull(account.getOwnerId(), "账户归属者ID不应为空");
        assertNotNull(account.getElectricAccountType(), "电费计费类型不应为空");
        assertNotNull(account.getOwnerName(), "账户归属者名称不应为空");

        // 查询数据库验证电表状态
        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(1)));
        assertNotNull(meters, "电表列表不应为空");

        // 验证电表状态已更新
        ElectricMeterEntity targetMeter = meters.stream()
                .filter(m -> !m.getId().equals(1))
                .findFirst()
                .orElse(null);
        assertNotNull(targetMeter, "目标电表应存在");
        // 验证电表具体数据
        assertNotNull(targetMeter.getId(), "电表ID不应为空");
        assertNotNull(targetMeter.getIsOnline(), "电表在线状态不应为空");
        // 验证电表与账户的关联关系
        assertEquals(1, targetMeter.getAccountId(), "电表应关联到正确的账户");

        // 校验生成的结算订单
        OrderDetailTerminationEntity orderDetail = orderDetailTerminationRepository.selectOne(
                new LambdaQueryWrapper<OrderDetailTerminationEntity>()
                        .eq(OrderDetailTerminationEntity::getCancelNo, response.getCancelNo()));
        assertNotNull(orderDetail, "应生成结算订单详情");
        assertOrderDetail(orderDetail, response.getCancelNo(), cancelAccountDto, account, response.getAmount());

        OrderEntity orderEntity = orderRepository.selectOne(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getOrderSn, orderDetail.getOrderSn()));
        assertNotNull(orderEntity, "应生成结算订单");
        assertOrderEntity(orderEntity, orderDetail.getOrderSn(), response.getAmount());
    }

    /**
     * 测试按量计费账户正常销户（补缴场景）
     */
    @Test
    void testCancelAccount_QuantityAccountPay() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("按量计费销户补缴测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)
                        .setPowerHigher(new BigDecimal("200.75"))));

        // When
        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        // Then - 验证返回结果
        assertNotNull(response);
        assertNotNull(response.getCancelNo());
        assertNotNull(response.getCleanBalanceType());
        assertNotNull(response.getAmount());

        // 验证补缴场景的清算类型和金额
        assertTrue(response.getAmount().compareTo(BigDecimal.ZERO) >= 0,
                "补缴场景金额应该大于等于0，实际金额: " + response.getAmount());

        // 查询数据库验证账户状态
        AccountBo account = accountInfoService.getById(1);
        assertNotNull(account, "账户不应为空");
        // 验证账户具体属性
        assertNotNull(account.getId(), "账户ID不应为空");
        assertNotNull(account.getOwnerType(), "账户类型不应为空");
        assertNotNull(account.getOwnerId(), "账户归属者ID不应为空");
        assertNotNull(account.getElectricAccountType(), "电费计费类型不应为空");
        assertNotNull(account.getOwnerName(), "账户归属者名称不应为空");

        // 查询数据库验证电表状态
        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(1)));
        assertNotNull(meters, "电表列表不应为空");

        // 验证电表状态已更新
        ElectricMeterEntity targetMeter = meters.stream()
                .filter(meter -> !meter.getId().equals(1))
                .findFirst()
                .orElse(null);
        assertNotNull(targetMeter, "目标电表应存在");
        // 验证电表具体数据
        assertNotNull(targetMeter.getId(), "电表ID不应为空");
        assertNotNull(targetMeter.getIsOnline(), "电表在线状态不应为空");
        // 验证电表与账户的关联关系
        assertEquals(1, targetMeter.getAccountId(), "电表应关联到正确的账户");

        // 校验生成的结算订单
        OrderDetailTerminationEntity orderDetail = orderDetailTerminationRepository.selectOne(
                new LambdaQueryWrapper<OrderDetailTerminationEntity>()
                        .eq(OrderDetailTerminationEntity::getCancelNo, response.getCancelNo()));
        assertNotNull(orderDetail, "应生成结算订单详情");
        assertOrderDetail(orderDetail, response.getCancelNo(), cancelAccountDto, account, response.getAmount());

        OrderEntity orderEntity = orderRepository.selectOne(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getOrderSn, orderDetail.getOrderSn()));
        assertNotNull(orderEntity, "应生成结算订单");
        assertOrderEntity(orderEntity, orderDetail.getOrderSn(), response.getAmount());
    }

    /**
     * 测试包月计费账户部分销户
     */
    @Test
    void testCloseAccount_MonthlyAccountPartialCancel() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("包月计费部分销户测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));

        // When
        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        // Then - 验证返回结果
        assertNotNull(response);
        assertNotNull(response.getCancelNo());
        assertNotNull(response.getCleanBalanceType());
        assertNotNull(response.getAmount());

        // 查询数据库验证账户状态
        AccountBo account = accountInfoService.getById(1);
        assertNotNull(account);
        assertEquals(ElectricAccountTypeEnum.MONTHLY, account.getElectricAccountType());
        assertEquals(new BigDecimal("500.00"), account.getMonthlyPayAmount().setScale(2, RoundingMode.DOWN));
        assertEquals(OwnerTypeEnum.ENTERPRISE, account.getOwnerType());
        assertEquals(1001, account.getOwnerId());
        assertEquals("账户1", account.getOwnerName());
        assertEquals(1, account.getWarnPlanId());
        assertEquals(WarnTypeEnum.NONE, account.getElectricWarnType());

        // 查询数据库验证电表状态
        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(1)));
        assertNotNull(meters);
        ElectricMeterEntity closeMeter = electricMeterRepository.selectById(1);
        assertNotNull(closeMeter);
        assertEquals(101, closeMeter.getSpaceId(), "电表空间ID应为101");
        assertEquals("1号楼电表", closeMeter.getMeterName(), "电表名称应为'1号楼电表'");
        assertEquals("SN001:1:1", closeMeter.getDeviceNo(), "电表编号应为'SN001:1:1'");
        assertEquals(Boolean.TRUE, closeMeter.getIsOnline(), "电表应为在线状态");
        assertEquals("1001", closeMeter.getIotId(), "电表IoT ID应为1001");
        assertEquals(1, closeMeter.getModelId(), "电表型号ID应为1");
        assertEquals("DDS102", closeMeter.getProductCode(), "电表型号名称应为'DDS102'");
        assertEquals("Modbus", closeMeter.getCommunicateModel(), "通信模式应为'Modbus'");
        assertEquals(1, closeMeter.getGatewayId(), "网关ID应为1");
        assertEquals(1, closeMeter.getPortNo(), "端口号应为1");
        assertEquals(1, closeMeter.getMeterAddress(), "电表地址应为1");
        assertEquals(1, closeMeter.getCt(), "CT变比应为1");
        // 变更了
        assertFalse(closeMeter.getProtectedModel(), "保护模式应为false");
        assertNull(closeMeter.getPricePlanId(), "电价方案ID应为null");
        assertNull(closeMeter.getWarnPlanId(), "告警方案ID应为null");
        assertNull(closeMeter.getWarnType(), "告警类型应为'null'");
        assertNull(closeMeter.getAccountId(), "销户后电表的账户ID应为null");

        // 校验生成的结算订单（部分销户金额应为0）
        OrderDetailTerminationEntity orderDetail = orderDetailTerminationRepository.selectOne(
                new LambdaQueryWrapper<OrderDetailTerminationEntity>()
                        .eq(OrderDetailTerminationEntity::getCancelNo, response.getCancelNo()));
        assertNotNull(orderDetail, "应生成结算订单详情");

        OrderEntity orderEntity = orderRepository.selectOne(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getOrderSn, orderDetail.getOrderSn()));
        assertNotNull(orderEntity, "应生成结算订单");
        assertOrderEntity(orderEntity, orderDetail.getOrderSn(), BigDecimal.ZERO);
        assertOrderDetail(orderDetail, response.getCancelNo(), cancelAccountDto, account, BigDecimal.ZERO);
    }

    private void assertOrderEntity(OrderEntity entity, String orderSn, BigDecimal expectedAmount) {
        assertEquals(orderSn, entity.getOrderSn());
        assertEquals(requestContext.getUserId(), entity.getUserId());
        assertEquals(requestContext.getUserRealName(), entity.getUserRealName());
        assertEquals(requestContext.getUserPhone(), entity.getUserPhone());
        assertEquals(requestContext.getUserId().toString(), entity.getThirdPartyUserId());
        assertEquals(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT.getCode(), entity.getOrderType());
        assertEquals(0, entity.getOrderAmount().compareTo(expectedAmount));
        assertEquals("CNY", entity.getCurrency());
        assertEquals(0, entity.getServiceRate().compareTo(BigDecimal.ZERO));
        assertEquals(0, entity.getServiceAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, entity.getUserPayAmount().compareTo(expectedAmount));
        assertEquals(PaymentChannelEnum.OFFLINE.name(), entity.getPaymentChannel());
        assertEquals(OrderStatusEnum.NOT_PAY.name(), entity.getOrderStatus());
        assertNull(entity.getRemark());
    }

    private void assertOrderDetail(OrderDetailTerminationEntity detail, String cancelNo, CancelAccountDto dto,
                                   AccountBo account, BigDecimal expectedAmount) {
        assertNotNull(detail.getOrderSn());
        assertEquals(cancelNo, detail.getCancelNo());
        assertEquals(dto.getAccountId(), detail.getAccountId());
        assertEquals(0, detail.getSettlementAmount().compareTo(expectedAmount));
        assertEquals(dto.getRemark(), detail.getCloseReason());
        assertEquals(JacksonUtil.toJson(List.of(1)), detail.getSnapshotPayload());
        assertEquals(account.getOwnerId(), detail.getOwnerId());
        assertEquals(account.getOwnerType().getCode(), detail.getOwnerType());
        assertEquals(account.getOwnerName(), detail.getOwnerName());
    }

    /**
     * 测试包月计费账户全部销户（退费场景）
     */
    @Test
    void testCloseAccount_MonthlyAccountFullCancelRefund() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("包月计费全部销户退费测试")
                .setMeterList(List.of(
                        new MeterCancelDetailDto().setMeterId(1)
                ));

        // When
        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        // Then - 验证返回结果
        assertNotNull(response);
        assertNotNull(response.getCancelNo());
        assertNotNull(response.getCleanBalanceType());
        assertNotNull(response.getAmount());

        // 查询数据库验证账户状态
        AccountBo account = accountInfoService.getById(1);
        assertNotNull(account);

        // 查询数据库验证电表状态
        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(1)));
        assertNotNull(meters);
    }

    /**
     * 验证逻辑删除会填充delete_time
     */
    @Test
    void testCancelAccount_FullCancel_DeleteTimeFilled() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        Integer accountId = 3;

        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(accountId)));
        assertNotNull(meters, "电表列表不应为空");
        assertFalse(meters.isEmpty(), "账户应存在电表以触发全部销户");

        List<MeterCancelDetailDto> meterList = meters.stream()
                .map(meter -> {
                    MeterCancelDetailDto detailDto = new MeterCancelDetailDto()
                            .setMeterId(meter.getId());
                    if (!Boolean.TRUE.equals(meter.getIsOnline())) {
                        detailDto.setPowerHigher(new BigDecimal("10.00"));
                    }
                    return detailDto;
                })
                .toList();

        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(accountId)
                .setRemark("全部销户删除时间填充测试")
                .setMeterList(meterList);

        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);
        assertNotNull(response);
        assertNotNull(response.getCancelNo());

        Boolean isDeleted = jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM energy_account WHERE id = ?", Boolean.class, accountId);
        Timestamp deleteTime = jdbcTemplate.queryForObject(
                "SELECT delete_time FROM energy_account WHERE id = ?", Timestamp.class, accountId);

        assertEquals(Boolean.TRUE, isDeleted, "逻辑删除标记应为true");
        assertNotNull(deleteTime, "delete_time 应被填充");
    }

    /**
     * 测试电表离线时手动输入电量的销户
     */
    @Test
    void testCancelAccount_OfflineMeterWithManualPower() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("电表离线手动输入电量测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)
                        .setPowerHigher(new BigDecimal("1000.50"))));

        // When
        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        // Then - 验证返回结果
        assertNotNull(response);
        assertNotNull(response.getCancelNo());
        assertNotNull(response.getCleanBalanceType());
        assertNotNull(response.getAmount());

        // 查询数据库验证账户状态
        AccountBo account = accountInfoService.getById(1);
        assertNotNull(account, "账户不应为空");
        // 验证账户具体属性
        assertNotNull(account.getId(), "账户ID不应为空");
        assertNotNull(account.getOwnerType(), "账户类型不应为空");
        assertNotNull(account.getOwnerId(), "账户归属者ID不应为空");
        assertNotNull(account.getElectricAccountType(), "电费计费类型不应为空");

        // 查询数据库验证电表状态
        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(1)));
        assertNotNull(meters, "电表列表不应为空");

        // 验证手动输入的电量数据被正确处理
        ElectricMeterEntity targetMeter = meters.stream()
                .filter(meter -> !meter.getId().equals(1))
                .findFirst()
                .orElse(null);
        assertNotNull(targetMeter, "目标电表应存在");
        // 验证电表具体数据
        assertNotNull(targetMeter.getId(), "电表ID不应为空");
        assertNotNull(targetMeter.getIsOnline(), "电表在线状态不应为空");
        // 验证电表与账户的关联关系
        assertEquals(1, targetMeter.getAccountId(), "电表应关联到正确的账户");
    }

    /**
     * 测试余额计算和清算类型
     */
    @Test
    void testCancelAccount_BalanceCalculationRefund() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("余额计算退费测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                                .setMeterId(1)
                                .setPowerHigher(new BigDecimal("80.30"))
                        )
                );

        // When
        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        // Then - 验证返回结果
        assertNotNull(response);
        assertNotNull(response.getCancelNo());
        assertNotNull(response.getCleanBalanceType());
        assertNotNull(response.getAmount());

        // 验证退费场景的清算类型和金额
        assertEquals(0, response.getAmount().compareTo(BigDecimal.ZERO), "退费场景金额应该等于0，实际金额: " + response.getAmount());
        assertEquals("SKIP", response.getCleanBalanceType().name(), "退费场景清算类型应为REFUND");

        // 查询数据库验证账户状态
        AccountBo account = accountInfoService.getById(1);
        assertNotNull(account, "账户不应为空");
        // 验证账户具体属性
        assertNotNull(account.getId(), "账户ID不应为空");
        assertNotNull(account.getOwnerType(), "账户类型不应为空");
        assertNotNull(account.getOwnerId(), "账户归属者ID不应为空");
        assertNotNull(account.getElectricAccountType(), "电费计费类型不应为空");
        assertNotNull(account.getOwnerName(), "账户归属者名称不应为空");

        // 查询数据库验证电表状态
        List<ElectricMeterEntity> meters = electricMeterRepository.findList(
                new ElectricMeterQo().setAccountIds(List.of(1)));
        assertNotNull(meters, "电表列表不应为空");

        // 验证电表状态已更新
        ElectricMeterEntity targetMeter = meters.stream()
                .filter(meter -> !meter.getId().equals(1))
                .findFirst()
                .orElse(null);
        assertNotNull(targetMeter, "目标电表应存在");
        // 验证电表具体数据
        assertNotNull(targetMeter.getId(), "电表ID不应为空");
        assertNotNull(targetMeter.getIsOnline(), "电表在线状态不应为空");
        assertEquals(1, targetMeter.getAccountId(), "电表应关联到正确的账户");

        // 校验结算订单（退费金额为0）
        OrderDetailTerminationEntity orderDetail = orderDetailTerminationRepository.selectOne(
                new LambdaQueryWrapper<OrderDetailTerminationEntity>()
                        .eq(OrderDetailTerminationEntity::getCancelNo, response.getCancelNo()));
        assertNotNull(orderDetail);
        assertOrderDetail(orderDetail, response.getCancelNo(), cancelAccountDto, account, BigDecimal.ZERO);

        OrderEntity orderEntity = orderRepository.selectOne(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getOrderSn, orderDetail.getOrderSn()));
        assertNotNull(orderEntity);
        assertOrderEntity(orderEntity, orderDetail.getOrderSn(), BigDecimal.ZERO);
    }

    /**
     * 测试余额计算和清算类型 - 补缴场景
     */
    @Test
    void testCancelAccount_BalanceCalculationPay() {
        RequestContextSetter.doSet(1, new UserRequestData("张三", "13800000001"));
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(3)
                .setRemark("余额计算补缴测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(3).setPowerLow(new BigDecimal("500"))
                ));

        AccountBo accountBeforeClose = accountInfoService.getById(3);

        // When
        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        // Then - 验证返回结果
        assertNotNull(response);
        assertNotNull(response.getCancelNo());
        assertNotNull(response.getCleanBalanceType());
        assertNotNull(response.getAmount());

        // 验证补缴场景的清算类型和金额
        assertTrue(response.getAmount().compareTo(BigDecimal.ZERO) < 0,
                "补缴场景金额应该小于0，实际金额: " + response.getAmount());
        assertEquals("PAY", response.getCleanBalanceType().name(), "补缴场景清算类型应为PAY");
        assertEquals(new BigDecimal("435.00").negate(), response.getAmount());

        // 查询账户信息
        // 校验订单记录（补缴金额为负数）
        OrderDetailTerminationEntity payOrderDetail = orderDetailTerminationRepository.selectOne(
                new LambdaQueryWrapper<OrderDetailTerminationEntity>()
                        .eq(OrderDetailTerminationEntity::getCancelNo, response.getCancelNo()));
        assertNotNull(payOrderDetail);
        assertNotNull(payOrderDetail.getOrderSn());
        assertEquals(response.getCancelNo(), payOrderDetail.getCancelNo());
        assertEquals(cancelAccountDto.getAccountId(), payOrderDetail.getAccountId());
        assertEquals(0, payOrderDetail.getSettlementAmount().compareTo(response.getAmount()));
        assertEquals(cancelAccountDto.getRemark(), payOrderDetail.getCloseReason());
        assertEquals(JacksonUtil.toJson(List.of(3)), payOrderDetail.getSnapshotPayload());
        assertEquals(accountBeforeClose.getOwnerId(), payOrderDetail.getOwnerId());
        assertEquals(accountBeforeClose.getOwnerType().getCode(), payOrderDetail.getOwnerType());
        assertEquals(accountBeforeClose.getOwnerName(), payOrderDetail.getOwnerName());

        OrderEntity payOrderEntity = orderRepository.selectOne(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getOrderSn, payOrderDetail.getOrderSn()));
        assertNotNull(payOrderEntity);
        assertOrderEntity(payOrderEntity, payOrderDetail.getOrderSn(), response.getAmount());

        // 全部销户后账户应被删除
        assertThrows(NotFoundException.class, () -> accountInfoService.getById(3));

    }

    /**
     * 测试系统异常处理
     */
    @Test
    void testCancelAccount_SystemException() {
        // Given - 使用极端参数来触发系统异常
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(-1) // 使用负数ID可能触发异常
                .setRemark("系统异常测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(-1)));

        // When & Then - 验证系统异常被正确处理
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            accountManagerService.cancelAccount(cancelAccountDto);
        });

        assertTrue(exception.getMessage().contains("能耗账户数据不存在"));
    }

}
