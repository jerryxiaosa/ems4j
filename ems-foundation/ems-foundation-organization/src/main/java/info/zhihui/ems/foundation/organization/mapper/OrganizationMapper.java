package info.zhihui.ems.foundation.organization.mapper;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationCreateDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationUpdateDto;
import info.zhihui.ems.foundation.organization.entity.OrganizationEntity;
import info.zhihui.ems.foundation.organization.enums.OrganizationTypeEnum;
import info.zhihui.ems.foundation.organization.qo.OrganizationQueryQo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {

    @Mapping(target = "name", source = "organizationName")
    @Mapping(target = "organizationType", source = "organizationType", qualifiedByName = "codeToEnum")
    OrganizationBo entityToBo(OrganizationEntity entity);

    List<OrganizationBo> listEntityToBo(List<OrganizationEntity> list);

    PageResult<OrganizationBo> pageEntityToPageBo(PageInfo<OrganizationEntity> pageInfo);

    @Mapping(target = "organizationType", expression = "java(dto.getOrganizationType() == null ? null : dto.getOrganizationType().getCode())")
    OrganizationEntity createDtoToEntity(OrganizationCreateDto dto);

    @Mapping(target = "organizationType", expression = "java(dto.getOrganizationType() == null ? null : dto.getOrganizationType().getCode())")
    OrganizationEntity updateDtoToEntity(OrganizationUpdateDto dto);

    OrganizationQueryQo queryDtoToQo(OrganizationQueryDto dto);

    @Named("codeToEnum")
    default OrganizationTypeEnum codeToEnum(Integer code) {
        return code != null ? CodeEnum.fromCode(code, OrganizationTypeEnum.class) : null;
    }
}