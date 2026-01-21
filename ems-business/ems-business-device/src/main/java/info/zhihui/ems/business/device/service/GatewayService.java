package info.zhihui.ems.business.device.service;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayOnlineStatusDto;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 网关服务接口
 * 提供网关设备的增删改查及相关业务操作
 *
 * @author jerryxiaosa
 */
public interface GatewayService {

    /**
     * 根据ID获取网关详情
     *
     * @param id 网关ID
     * @return 网关业务对象，如果不存在则返回null
     */
    GatewayBo getDetail(@NotNull Integer id);

    /**
     * 分页查询网关列表
     *
     * @param query 查询条件
     * @param pageParam 分页参数
     * @return 分页结果，包含网关业务对象列表
     */
    PageResult<GatewayBo> findPage(@NotNull GatewayQueryDto query, @NotNull PageParam pageParam);

    /**
     * 查询网关列表（不分页）
     *
     * @param query 查询条件
     * @return 网关业务对象列表
     */
    List<GatewayBo> findList(@NotNull GatewayQueryDto query);

    /**
     * 新增网关
     *
     * @param dto 网关保存数据传输对象
     * @return 新增网关的ID
     */
    Integer add(@Valid @NotNull GatewayCreateDto dto);

    /**
     * 更新网关信息
     *
     * @param dto 网关保存数据传输对象，必须包含ID
     */
    void update(@Valid @NotNull GatewayUpdateDto dto);

    /**
     * 删除网关
     *
     * @param id 网关ID
     */
    void delete(@NotNull Integer id);

    /**
     * 获取通信方式选项列表
     *
     * @return 通信方式选项列表
     */
    List<String> getCommunicationOption();

    /**
     * 同步网关在线状态
     *
     * @param onlineStatusDto 网关在线状态
     */
    void syncGatewayOnlineStatus(@Valid @NotNull GatewayOnlineStatusDto onlineStatusDto);
}
