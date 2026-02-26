package info.zhihui.ems.web.owner.biz;

import info.zhihui.ems.business.account.dto.OwnerSpaceRentDto;
import info.zhihui.ems.business.account.dto.OwnerSpaceUnrentDto;
import info.zhihui.ems.business.account.service.OwnerSpaceLeaseService;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.web.owner.vo.OwnerSpaceRentVo;
import info.zhihui.ems.web.owner.vo.OwnerSpaceUnrentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 主体空间租赁业务编排
 */
@Service
@RequiredArgsConstructor
public class OwnerSpaceLeaseBiz {

    private final OwnerSpaceLeaseService ownerSpaceLeaseService;

    /**
     * 租赁空间
     */
    public void rentSpaces(OwnerSpaceRentVo rentVo) {
        OwnerTypeEnum ownerType = parseOwnerType(rentVo.getOwnerType());

        OwnerSpaceRentDto rentDto = new OwnerSpaceRentDto()
                .setOwnerType(ownerType)
                .setOwnerId(rentVo.getOwnerId())
                .setSpaceIds(rentVo.getSpaceIds());
        ownerSpaceLeaseService.rentSpaces(rentDto);
    }

    /**
     * 退租空间
     */
    public void unrentSpaces(OwnerSpaceUnrentVo unrentVo) {
        OwnerTypeEnum ownerType = parseOwnerType(unrentVo.getOwnerType());

        OwnerSpaceUnrentDto unrentDto = new OwnerSpaceUnrentDto()
                .setOwnerType(ownerType)
                .setOwnerId(unrentVo.getOwnerId())
                .setSpaceIds(unrentVo.getSpaceIds());
        ownerSpaceLeaseService.unrentSpaces(unrentDto);
    }

    private OwnerTypeEnum parseOwnerType(Integer ownerTypeCode) {
        OwnerTypeEnum ownerType = ownerTypeCode == null ? null : CodeEnum.fromCode(ownerTypeCode, OwnerTypeEnum.class);
        if (ownerType == null) {
            throw new BusinessRuntimeException("主体类型不正确");
        }
        return ownerType;
    }
}
