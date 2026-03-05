package info.zhihui.ems.web.order;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.web.order.mapstruct.OrderWebMapper;
import info.zhihui.ems.web.order.vo.EnergyTopUpDetailVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class OrderWebMapperTest {

    private final OrderWebMapper orderWebMapper = Mappers.getMapper(OrderWebMapper.class);

    @Test
    @DisplayName("能耗充值明细映射-编码为null时不抛异常并映射为null")
    void testToEnergyTopUpDto_WhenCodeIsNull() {
        EnergyTopUpDetailVo detailVo = new EnergyTopUpDetailVo()
                .setAccountId(1)
                .setBalanceType(null)
                .setOwnerType(null)
                .setOwnerId(2)
                .setOwnerName("测试主体")
                .setElectricAccountType(null)
                .setMeterType(null);

        var dto = orderWebMapper.toEnergyTopUpDto(detailVo);

        assertThat(dto).isNotNull();
        assertThat(dto.getBalanceType()).isNull();
        assertThat(dto.getOwnerType()).isNull();
        assertThat(dto.getElectricAccountType()).isNull();
        assertThat(dto.getMeterType()).isNull();
    }

    @Test
    @DisplayName("能耗充值明细映射-非法编码时不抛异常并映射为null")
    void testToEnergyTopUpDto_WhenCodeIsInvalid() {
        EnergyTopUpDetailVo detailVo = new EnergyTopUpDetailVo()
                .setAccountId(1)
                .setBalanceType(999)
                .setOwnerType(999)
                .setOwnerId(2)
                .setOwnerName("测试主体")
                .setElectricAccountType(999)
                .setMeterType(999);

        var dto = orderWebMapper.toEnergyTopUpDto(detailVo);

        assertThat(dto).isNotNull();
        assertThat(dto.getBalanceType()).isNull();
        assertThat(dto.getOwnerType()).isNull();
        assertThat(dto.getElectricAccountType()).isNull();
        assertThat(dto.getMeterType()).isNull();
    }

    @Test
    @DisplayName("能耗充值明细映射-合法编码可正确映射枚举")
    void testToEnergyTopUpDto_WhenCodeIsValid() {
        EnergyTopUpDetailVo detailVo = new EnergyTopUpDetailVo()
                .setAccountId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerId(2)
                .setOwnerName("测试主体")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode());

        var dto = orderWebMapper.toEnergyTopUpDto(detailVo);

        assertThat(dto).isNotNull();
        assertThat(dto.getBalanceType()).isEqualTo(BalanceTypeEnum.ACCOUNT);
        assertThat(dto.getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE);
        assertThat(dto.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.QUANTITY);
        assertThat(dto.getMeterType()).isEqualTo(MeterTypeEnum.ELECTRIC);
    }
}
