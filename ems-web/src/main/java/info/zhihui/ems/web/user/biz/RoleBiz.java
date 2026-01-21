package info.zhihui.ems.web.user.biz;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleDetailBo;
import info.zhihui.ems.foundation.user.dto.RoleCreateDto;
import info.zhihui.ems.foundation.user.dto.RoleMenuSaveDto;
import info.zhihui.ems.foundation.user.dto.RoleQueryDto;
import info.zhihui.ems.foundation.user.dto.RoleUpdateDto;
import info.zhihui.ems.foundation.user.service.RoleService;
import info.zhihui.ems.web.user.mapstruct.RoleWebMapper;
import info.zhihui.ems.web.user.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色管理业务编排。
 */
@Service
@RequiredArgsConstructor
public class RoleBiz {

    private final RoleService roleService;
    private final RoleWebMapper roleWebMapper;

    /**
     * 分页查询角色。
     *
     * @param queryVo  查询条件
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    public PageResult<RoleVo> findRolePage(RoleQueryVo queryVo, Integer pageNum, Integer pageSize) {
        RoleQueryDto queryDto = roleWebMapper.toRoleQueryDto(queryVo);
        PageParam pageParam = new PageParam()
                .setPageNum(pageNum)
                .setPageSize(pageSize);
        PageResult<RoleBo> pageResult = roleService.findPage(queryDto, pageParam);
        List<RoleVo> roleVos = roleWebMapper.toRoleVoList(pageResult.getList());
        return new PageResult<RoleVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(roleVos);
    }

    /**
     * 查询角色列表。
     *
     * @param queryVo 查询条件
     * @return 角色列表
     */
    public List<RoleVo> findRoleList(RoleQueryVo queryVo) {
        RoleQueryDto queryDto = roleWebMapper.toRoleQueryDto(queryVo);
        List<RoleBo> roles = roleService.findList(queryDto);
        return roleWebMapper.toRoleVoList(roles);
    }

    /**
     * 根据 ID 查询角色详情。
     *
     * @param id 角色ID
     * @return 角色详情
     */
    public RoleDetailVo getRoleDetail(Integer id) {
        RoleDetailBo roleDetailBo = roleService.getDetail(id);
        return roleWebMapper.toRoleDetailVo(roleDetailBo);
    }

    /**
     * 新增角色。
     *
     * @param createVo 创建参数
     * @return 新增角色ID
     */
    public Integer createRole(RoleCreateVo createVo) {
        RoleCreateDto dto = roleWebMapper.toRoleCreateDto(createVo);
        return roleService.add(dto);
    }

    /**
     * 更新角色信息。
     *
     * @param id       角色ID
     * @param updateVo 更新参数
     */
    public void updateRole(Integer id, RoleUpdateVo updateVo) {
        RoleUpdateDto dto = roleWebMapper.toRoleUpdateDto(updateVo);
        dto.setId(id);
        roleService.update(dto);
    }

    /**
     * 保存角色菜单关联。
     *
     * @param roleId   角色ID
     * @param saveVo   保存参数
     */
    public void saveRoleMenu(Integer roleId, RoleMenuSaveVo saveVo) {
        RoleMenuSaveDto dto = roleWebMapper.toRoleMenuSaveDto(saveVo);
        dto.setRoleId(roleId);
        roleService.saveRoleMenu(dto);
    }

    /**
     * 删除角色。
     *
     * @param id 角色ID
     */
    public void deleteRole(Integer id) {
        roleService.delete(id);
    }
}