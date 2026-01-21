package info.zhihui.ems.foundation.user.mapper;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleDetailBo;
import info.zhihui.ems.foundation.user.dto.RoleCreateDto;
import info.zhihui.ems.foundation.user.dto.RoleQueryDto;
import info.zhihui.ems.foundation.user.dto.RoleUpdateDto;
import info.zhihui.ems.foundation.user.entity.RoleEntity;
import info.zhihui.ems.foundation.user.qo.RoleQueryQo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleDetailBo entityToDetailBo(RoleEntity entity);

    List<RoleBo> listEntityToBo(List<RoleEntity> list);

    PageResult<RoleBo> pageEntityToPageBo(PageInfo<RoleEntity> pageInfo);

    RoleEntity createDtoToEntity(RoleCreateDto dto);

    RoleEntity updateDtoToEntity(RoleUpdateDto dto);

    RoleQueryQo queryDtoToQo(RoleQueryDto dto);
}