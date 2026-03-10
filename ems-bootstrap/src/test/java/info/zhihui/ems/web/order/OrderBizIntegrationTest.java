package info.zhihui.ems.web.order;

import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.business.order.repository.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.web.order.biz.OrderBiz;
import info.zhihui.ems.web.order.vo.EnergyOrderCreateVo;
import info.zhihui.ems.web.order.vo.EnergyTopUpDetailVo;
import info.zhihui.ems.web.order.vo.OrderCreationResponseVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OrderBizIntegrationTest {

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    @Test
    @DisplayName("创建能耗充值订单应正确映射能耗充值明细并成功落库")
    void testCreateEnergyTopUpOrder_ShouldMapEnergyTopUpDto() {
        EnergyTopUpDetailVo energyTopUpDetailVo = new EnergyTopUpDetailVo()
                .setAccountId(10)
                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerId(20)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED.getCode())
                .setMeterId(30)
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterName("测试电表")
                .setDeviceNo("EM-0001")
                .setSpaceId(40);
        EnergyOrderCreateVo createVo = new EnergyOrderCreateVo()
                .setUserId(1)
                .setUserPhone("13800000000")
                .setUserRealName("张三")
                .setThirdPartyUserId("tp-1")
                .setOrderAmount(new BigDecimal("100.00"))
                .setPaymentChannel(PaymentChannelEnum.OFFLINE.getCode())
                .setEnergyTopUp(energyTopUpDetailVo);

        OrderCreationResponseVo responseVo = orderBiz.createEnergyTopUpOrder(createVo);

        assertThat(responseVo).isNotNull();
        assertThat(responseVo.getOrderSn()).isNotBlank();

        var detailEntity = orderDetailEnergyTopUpRepository.selectByOrderSn(responseVo.getOrderSn());
        assertThat(detailEntity).isNotNull();
        assertThat(detailEntity.getAccountId()).isEqualTo(10);
        assertThat(detailEntity.getBalanceType()).isEqualTo(BalanceTypeEnum.ACCOUNT.getCode());
        assertThat(detailEntity.getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE.getCode());
        assertThat(detailEntity.getOwnerId()).isEqualTo(20);
        assertThat(detailEntity.getOwnerName()).isEqualTo("测试企业");
        assertThat(detailEntity.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.MERGED.getCode());
        assertThat(detailEntity.getMeterId()).isEqualTo(30);
        assertThat(detailEntity.getMeterType()).isEqualTo(MeterTypeEnum.ELECTRIC.getCode());
        assertThat(detailEntity.getMeterName()).isEqualTo("测试电表");
        assertThat(detailEntity.getDeviceNo()).isEqualTo("EM-0001");
        assertThat(detailEntity.getSpaceId()).isEqualTo(40);
    }
}
