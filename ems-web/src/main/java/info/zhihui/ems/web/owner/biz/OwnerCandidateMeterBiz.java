package info.zhihui.ems.web.owner.biz;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.web.owner.vo.OwnerCandidateMeterQueryVo;
import info.zhihui.ems.web.owner.vo.OwnerCandidateMeterVo;
import info.zhihui.ems.web.util.OfflineDurationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 主体候选电表业务编排
 */
@Service
@RequiredArgsConstructor
public class OwnerCandidateMeterBiz {

    private final AccountAdditionalInfoService accountAdditionalInfoService;

    /**
     * 查询主体候选电表列表
     */
    public List<OwnerCandidateMeterVo> findCandidateMeterList(OwnerCandidateMeterQueryVo queryVo) {
        OwnerTypeEnum ownerType = parseOwnerType(queryVo.getOwnerType());

        OwnerCandidateMeterQueryDto queryDto = new OwnerCandidateMeterQueryDto()
                .setOwnerType(ownerType)
                .setOwnerId(queryVo.getOwnerId())
                .setSpaceNameLike(queryVo.getSpaceNameLike());

        List<AccountCandidateMeterDto> candidateMeterDtoList = accountAdditionalInfoService.findCandidateMeterList(queryDto);
        if (CollectionUtils.isEmpty(candidateMeterDtoList)) {
            return Collections.emptyList();
        }
        return candidateMeterDtoList.stream().map(this::toOwnerCandidateMeterVo).toList();
    }

    private OwnerCandidateMeterVo toOwnerCandidateMeterVo(AccountCandidateMeterDto candidateMeterDto) {
        return new OwnerCandidateMeterVo()
                .setId(candidateMeterDto.getId())
                .setMeterName(candidateMeterDto.getMeterName())
                .setMeterNo(candidateMeterDto.getMeterNo())
                .setMeterType(MeterTypeEnum.ELECTRIC.getInfo())
                .setSpaceId(candidateMeterDto.getSpaceId())
                .setSpaceName(candidateMeterDto.getSpaceName())
                .setSpaceParentNames(candidateMeterDto.getSpaceParentNames())
                .setIsOnline(candidateMeterDto.getIsOnline())
                .setOfflineDurationText(OfflineDurationUtil.format(
                        candidateMeterDto.getIsOnline(), candidateMeterDto.getLastOnlineTime()))
                .setIsPrepay(candidateMeterDto.getIsPrepay());
    }

    private OwnerTypeEnum parseOwnerType(Integer ownerTypeCode) {
        OwnerTypeEnum ownerType = CodeEnum.fromCode(ownerTypeCode, OwnerTypeEnum.class);
        if (ownerType == null) {
            throw new BusinessRuntimeException("主体类型不正确");
        }
        return ownerType;
    }
}
