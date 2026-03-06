package info.zhihui.ems.web.order.mapstruct;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderListDto;
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
import info.zhihui.ems.common.enums.CodeEnum;
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
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * 订单 Web 层映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderWebMapper {

    OrderQueryDto toOrderQueryDto(OrderQueryVo queryVo);

    @Mapping(target = "ownerType", source = "ownerType", qualifiedByName = "ownerTypeToCode")
    OrderVo toOrderVo(OrderListDto dto);

    List<OrderVo> toOrderVoList(List<OrderListDto> dtoList);

    default PageResult<OrderVo> toOrderVoPage(PageResult<OrderListDto> pageResult) {
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

    @Mapping(target = "ownerType", source = "ownerType", qualifiedByName = "ownerTypeToCode")
    OrderDetailVo toOrderDetailVo(OrderBo bo);

    @Mapping(target = "energyTopUpDto", source = "energyTopUp")
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
        return CodeEnum.fromCode(channel, PaymentChannelEnum.class);
    }

    default String mapPaymentChannel(PaymentChannelEnum channel) {
        return channel == null ? null : channel.getCode();
    }

    default Integer mapOrderType(OrderTypeEnum orderType) {
        return orderType == null ? null : orderType.getCode();
    }

    default OrderTypeEnum mapOrderType(Integer orderTypeCode) {
        return CodeEnum.fromCode(orderTypeCode, OrderTypeEnum.class);
    }

    default BalanceTypeEnum mapBalanceType(String balanceType) {
        return balanceType == null ? null : BalanceTypeEnum.valueOf(balanceType);
    }

    default BalanceTypeEnum mapBalanceType(Integer balanceTypeCode) {
        return CodeEnum.fromCode(balanceTypeCode, BalanceTypeEnum.class);
    }

    default String mapBalanceType(BalanceTypeEnum balanceType) {
        return balanceType == null ? null : balanceType.name();
    }

    default OwnerTypeEnum mapOwnerType(String ownerType) {
        return ownerType == null ? null : OwnerTypeEnum.valueOf(ownerType);
    }

    default OwnerTypeEnum mapOwnerType(Integer ownerTypeCode) {
        return CodeEnum.fromCode(ownerTypeCode, OwnerTypeEnum.class);
    }

    default String mapOwnerType(OwnerTypeEnum ownerType) {
        return ownerType == null ? null : ownerType.name();
    }

    @Named("ownerTypeToCode")
    default Integer ownerTypeToCode(OwnerTypeEnum ownerType) {
        return ownerType == null ? null : ownerType.getCode();
    }

    default ElectricAccountTypeEnum mapElectricAccountType(String accountType) {
        return accountType == null ? null : ElectricAccountTypeEnum.valueOf(accountType);
    }

    default ElectricAccountTypeEnum mapElectricAccountType(Integer accountTypeCode) {
        return CodeEnum.fromCode(accountTypeCode, ElectricAccountTypeEnum.class);
    }

    default String mapElectricAccountType(ElectricAccountTypeEnum accountType) {
        return accountType == null ? null : accountType.name();
    }

    default MeterTypeEnum mapMeterType(String meterType) {
        return meterType == null ? null : MeterTypeEnum.valueOf(meterType);
    }

    default MeterTypeEnum mapMeterType(Integer meterTypeCode) {
        return CodeEnum.fromCode(meterTypeCode, MeterTypeEnum.class);
    }

    default String mapMeterType(MeterTypeEnum meterType) {
        return meterType == null ? null : meterType.name();
    }
}
