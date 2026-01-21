package info.zhihui.ems.foundation.organization.service;

import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.organization.dto.OrganizationCreateDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author jerryxiaosa
 */
public interface OrganizationService {
    /**
     * 根据主键获取机构详细信息
     * - 若不存在则抛出 `NotFoundException`
     *
     * @param id 机构主键ID，不能为空
     * @return 机构业务对象
     */
    OrganizationBo getDetail(@NotNull Integer id);

    /**
     * 根据查询条件获取机构列表（不分页）
     * - 仅返回未删除的数据
     *
     * @param queryDto 查询条件
     * @return 机构业务对象列表
     */
    List<OrganizationBo> findOrganizationList(@Valid @NotNull OrganizationQueryDto queryDto);

    /**
     * 创建机构
     * - 直接插入数据库，若触发唯一索引冲突则抛出 `BusinessRuntimeException`
     *
     * @param dto 创建参数
     * @return 创建后的机构业务对象
     */
    Integer add(@Valid @NotNull OrganizationCreateDto dto);

    /**
     * 更新机构信息
     * - 先检查记录是否存在；更新时若触发唯一索引冲突则抛出 `BusinessRuntimeException`
     *
     * @param dto 更新参数（包含主键ID）
     */
    void update(@Valid @NotNull OrganizationUpdateDto dto);

    /**
     * 逻辑删除机构
     * - 若记录不存在则抛出 `NotFoundException`
     *
     * @param id 机构主键ID
     */
    void delete(@NotNull Integer id);

    /**
     * 分页查询机构列表
     * - 仅返回未删除的数据
     *
     * @param queryDto 查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<OrganizationBo> findOrganizationPage(@Valid @NotNull OrganizationQueryDto queryDto, @NotNull PageParam pageParam);
}
