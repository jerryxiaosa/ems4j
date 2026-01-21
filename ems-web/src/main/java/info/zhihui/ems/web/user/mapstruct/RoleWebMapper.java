package info.zhihui.ems.web.user.mapstruct;

import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleDetailBo;
import info.zhihui.ems.foundation.user.dto.RoleCreateDto;
import info.zhihui.ems.foundation.user.dto.RoleMenuSaveDto;
import info.zhihui.ems.foundation.user.dto.RoleQueryDto;
import info.zhihui.ems.foundation.user.dto.RoleUpdateDto;
import info.zhihui.ems.web.user.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 角色Web层映射器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleWebMapper {

    /**
     * 查询VO转换为查询DTO
     */
    RoleQueryDto toRoleQueryDto(RoleQueryVo vo);

    /**
     * 创建VO转换为创建DTO
     */
    RoleCreateDto toRoleCreateDto(RoleCreateVo vo);

    /**
     * 更新VO转换为更新DTO
     */
    @Mapping(target = "id", ignore = true)
    RoleUpdateDto toRoleUpdateDto(RoleUpdateVo vo);

    /**
     * 角色菜单保存VO转换为角色菜单保存DTO
     */
    RoleMenuSaveDto toRoleMenuSaveDto(RoleMenuSaveVo vo);

    /**
     * 角色BO转换为角色VO
     */
    RoleVo toRoleVo(RoleBo bo);

    /**
     * 角色BO列表转换为角色VO列表
     */
    List<RoleVo> toRoleVoList(List<RoleBo> bos);

    /**
     * 角色详情BO转换为角色详情VO
     */
    RoleDetailVo toRoleDetailVo(RoleDetailBo bo);
}