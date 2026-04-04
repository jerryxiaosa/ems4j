package info.zhihui.ems.web.order.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.order.dto.OrderCreationResponseDto;
import info.zhihui.ems.business.order.dto.creation.EnergyOrderCreationInfoDto;
import info.zhihui.ems.business.order.dto.creation.EnergyTopUpDto;
import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.business.order.service.core.OrderQueryService;
import info.zhihui.ems.business.order.service.core.OrderService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.web.order.mapstruct.OrderWebMapper;
import info.zhihui.ems.web.order.vo.EnergyOrderCreateVo;
import info.zhihui.ems.web.order.vo.EnergyTopUpDetailVo;
import info.zhihui.ems.web.order.vo.OrderCreationResponseVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderBizTest {

    @InjectMocks
    private OrderBiz orderBiz;

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderWebMapper orderWebMapper;

    @Mock
    private AccountInfoService accountInfoService;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Test
    @DisplayName("创建充值订单_电表归属账户不匹配时应拒绝")
    void testCreateEnergyTopUpOrder_WhenMeterAccountMismatch_ShouldThrowException() {
        EnergyOrderCreateVo createVo = buildCreateVo(new BigDecimal("0.12"));
        EnergyOrderCreationInfoDto creationInfoDto = buildCreationInfoDto(new BigDecimal("0.12"));
        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(101)
                .setAccountId(2);

        when(orderWebMapper.toEnergyOrderCreationInfoDto(createVo)).thenReturn(creationInfoDto);
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterInfoService.getDetail(101)).thenReturn(meterBo);

        assertThatThrownBy(() -> orderBiz.createEnergyTopUpOrder(createVo))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("电表不属于当前账户");

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("创建充值订单_电表充值时应以后端电表信息为准")
    void testCreateEnergyTopUpOrder_WhenElectricMeterTopUp_ShouldUseBackendMeterInfo() {
        EnergyOrderCreateVo createVo = buildCreateVo(new BigDecimal("0.12"));
        EnergyOrderCreationInfoDto creationInfoDto = buildCreationInfoDto(new BigDecimal("0.12"));
        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("后端企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(101)
                .setAccountId(1)
                .setMeterName("后端电表")
                .setDeviceNo("DEVICE-101")
                .setSpaceId(201);
        OrderCreationResponseDto responseDto = new OrderCreationResponseDto().setOrderSn("ORDER-001");
        OrderCreationResponseVo responseVo = new OrderCreationResponseVo().setOrderSn("ORDER-001");

        when(orderWebMapper.toEnergyOrderCreationInfoDto(createVo)).thenReturn(creationInfoDto);
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterInfoService.getDetail(101)).thenReturn(meterBo);
        when(orderService.createOrder(creationInfoDto)).thenReturn(responseDto);
        when(orderWebMapper.toOrderCreationResponseVo(responseDto)).thenReturn(responseVo);

        OrderCreationResponseVo result = orderBiz.createEnergyTopUpOrder(createVo);

        assertThat(result).isSameAs(responseVo);
        assertThat(creationInfoDto.getEnergyTopUpDto().getOwnerName()).isEqualTo("后端企业");
        assertThat(creationInfoDto.getEnergyTopUpDto().getMeterName()).isEqualTo("后端电表");
        assertThat(creationInfoDto.getEnergyTopUpDto().getDeviceNo()).isEqualTo("DEVICE-101");
        assertThat(creationInfoDto.getEnergyTopUpDto().getSpaceId()).isEqualTo(201);
        verify(orderService).createOrder(creationInfoDto);
    }

    private EnergyOrderCreateVo buildCreateVo(BigDecimal serviceRate) {
        EnergyTopUpDetailVo detailVo = new EnergyTopUpDetailVo()
                .setAccountId(1)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerId(1001)
                .setOwnerName("企业A")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setServiceRate(serviceRate)
                .setMeterId(101)
                .setMeterName("前端电表")
                .setDeviceNo("FRONT-DEVICE")
                .setSpaceId(999);
        return new EnergyOrderCreateVo()
                .setUserId(1)
                .setUserPhone("13800000000")
                .setUserRealName("张三")
                .setThirdPartyUserId("third-user")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.getCode())
                .setEnergyTopUp(detailVo);
    }

    private EnergyOrderCreationInfoDto buildCreationInfoDto(BigDecimal serviceRate) {
        EnergyTopUpDto topUpDto = new EnergyTopUpDto()
                .setAccountId(1)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setOwnerName("企业A")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setServiceRate(serviceRate)
                .setMeterId(101)
                .setMeterName("前端电表")
                .setDeviceNo("FRONT-DEVICE")
                .setSpaceId(999);
        EnergyOrderCreationInfoDto creationInfoDto = new EnergyOrderCreationInfoDto();
        creationInfoDto.setUserId(1);
        creationInfoDto.setUserPhone("13800000000");
        creationInfoDto.setUserRealName("张三");
        creationInfoDto.setThirdPartyUserId("third-user");
        creationInfoDto.setOrderAmount(new BigDecimal("100.00"));
        creationInfoDto.setPaymentChannel(PaymentChannelEnum.OFFLINE);
        creationInfoDto.setEnergyTopUpDto(topUpDto);
        return creationInfoDto;
    }
}
