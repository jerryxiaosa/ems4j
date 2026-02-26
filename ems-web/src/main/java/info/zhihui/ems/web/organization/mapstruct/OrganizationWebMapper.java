package info.zhihui.ems.web.organization.mapstruct;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationCreateDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationUpdateDto;
import info.zhihui.ems.foundation.organization.enums.OrganizationTypeEnum;
import info.zhihui.ems.web.organization.vo.OrganizationCreateVo;
import info.zhihui.ems.web.organization.vo.OrganizationOptionVo;
import info.zhihui.ems.web.organization.vo.OrganizationQueryVo;
import info.zhihui.ems.web.organization.vo.OrganizationUpdateVo;
import info.zhihui.ems.web.organization.vo.OrganizationVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 组织 Web 层映射器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationWebMapper {

    OrganizationQueryDto toOrganizationQueryDto(OrganizationQueryVo queryVo);

    @Mapping(target = "organizationType", expression = "java(mapOrganizationType(createVo.getOrganizationType()))")
    OrganizationCreateDto toOrganizationCreateDto(OrganizationCreateVo createVo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationType", expression = "java(mapOrganizationType(updateVo.getOrganizationType()))")
    OrganizationUpdateDto toOrganizationUpdateDto(OrganizationUpdateVo updateVo);

    @Mapping(target = "organizationName", source = "name")
    @Mapping(target = "organizationType", expression = "java(mapOrganizationTypeCode(bo.getOrganizationType()))")
    OrganizationVo toOrganizationVo(OrganizationBo bo);

    List<OrganizationVo> toOrganizationVoList(List<OrganizationBo> bos);

    @Mapping(target = "organizationName", source = "name")
    @Mapping(target = "organizationType", expression = "java(mapOrganizationTypeCode(bo.getOrganizationType()))")
    OrganizationOptionVo toOrganizationOptionVo(OrganizationBo bo);

    List<OrganizationOptionVo> toOrganizationOptionVoList(List<OrganizationBo> bos);

    default OrganizationTypeEnum mapOrganizationType(Integer code) {
        if (code == null) {
            return null;
        }
        return CodeEnum.fromCode(code, OrganizationTypeEnum.class);
    }

    default Integer mapOrganizationTypeCode(OrganizationTypeEnum typeEnum) {
        return typeEnum == null ? null : typeEnum.getCode();
    }
}
