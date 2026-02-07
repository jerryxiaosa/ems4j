package info.zhihui.ems.web.order.mapstruct;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.OrderQueryDto;
import info.zhihui.ems.business.finance.dto.order.creation.EnergyOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.EnergyTopUpDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationSettlementDto;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.order.vo.EnergyOrderCreateVo;
import info.zhihui.ems.web.order.vo.EnergyTopUpDetailVo;
import info.zhihui.ems.web.order.vo.OrderCreationResponseVo;
import info.zhihui.ems.web.order.vo.OrderDetailVo;
import info.zhihui.ems.web.order.vo.OrderQueryVo;
import info.zhihui.ems.web.order.vo.OrderVo;
import info.zhihui.ems.web.order.vo.TerminationOrderCreateVo;
import info.zhihui.ems.web.order.vo.TerminationSettlementVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * 订单 Web 层映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderWebMapper {

    OrderQueryDto toOrderQueryDto(OrderQueryVo queryVo);

    OrderVo toOrderVo(OrderBo bo);

    List<OrderVo> toOrderVoList(List<OrderBo> bos);

    default PageResult<OrderVo> toOrderVoPage(PageResult<OrderBo> pageResult) {
        if (pageResult == null) {
            return new PageResult<OrderVo>().setPageNum(0).setPageSize(0).setTotal(0L).setList(Collections.emptyList());
        }
        List<OrderVo> list = toOrderVoList(pageResult.getList());
        return new PageResult<OrderVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(list == null ? Collections.emptyList() : list);
    }

    OrderDetailVo toOrderDetailVo(OrderBo bo);

    EnergyOrderCreationInfoDto toEnergyOrderCreationInfoDto(EnergyOrderCreateVo createVo);

    EnergyTopUpDto toEnergyTopUpDto(EnergyTopUpDetailVo detailVo);

    TerminationOrderCreationInfoDto toTerminationOrderCreationInfoDto(TerminationOrderCreateVo createVo);

    TerminationSettlementDto toTerminationSettlementDto(TerminationSettlementVo settlementVo);

    OrderCreationResponseVo toOrderCreationResponseVo(OrderCreationResponseDto responseDto);

    default OrderStatusEnum mapOrderStatus(String status) {
        return status == null ? null : OrderStatusEnum.valueOf(status);
    }

    default String mapOrderStatus(OrderStatusEnum status) {
        return status == null ? null : status.name();
    }

    default PaymentChannelEnum mapPaymentChannel(String channel) {
        return channel == null ? null : PaymentChannelEnum.valueOf(channel);
    }

    default String mapPaymentChannel(PaymentChannelEnum channel) {
        return channel == null ? null : channel.name();
    }

    default Integer mapOrderType(OrderTypeEnum orderType) {
        return orderType == null ? null : orderType.getCode();
    }

    default BalanceTypeEnum mapBalanceType(String balanceType) {
        return balanceType == null ? null : BalanceTypeEnum.valueOf(balanceType);
    }

    default String mapBalanceType(BalanceTypeEnum balanceType) {
        return balanceType == null ? null : balanceType.name();
    }

    default OwnerTypeEnum mapOwnerType(String ownerType) {
        return ownerType == null ? null : OwnerTypeEnum.valueOf(ownerType);
    }

    default String mapOwnerType(OwnerTypeEnum ownerType) {
        return ownerType == null ? null : ownerType.name();
    }

    default ElectricAccountTypeEnum mapElectricAccountType(String accountType) {
        return accountType == null ? null : ElectricAccountTypeEnum.valueOf(accountType);
    }

    default String mapElectricAccountType(ElectricAccountTypeEnum accountType) {
        return accountType == null ? null : accountType.name();
    }

    default MeterTypeEnum mapMeterType(String meterType) {
        return meterType == null ? null : MeterTypeEnum.valueOf(meterType);
    }

    default String mapMeterType(MeterTypeEnum meterType) {
        return meterType == null ? null : meterType.name();
    }
}
