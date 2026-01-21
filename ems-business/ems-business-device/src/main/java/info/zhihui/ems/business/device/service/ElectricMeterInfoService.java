package info.zhihui.ems.business.device.service;

import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 电表数据信息服务接口
 *
 * @author jerryxiaosa
 */
public interface ElectricMeterInfoService {

    /**
     * 分页查询电表列表
     *
     * @param query     查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<ElectricMeterBo> findPage(@NotNull ElectricMeterQueryDto query, @NotNull PageParam pageParam);

    /**
     * 查询电表列表
     *
     * @param query 查询条件
     * @return 电表列表
     */
    List<ElectricMeterBo> findList(@NotNull ElectricMeterQueryDto query);

    /**
     * 根据ID获取电表详情
     *
     * @param id 电表ID
     * @return 电表详情
     * @throws NotFoundException 当电表不存在时
     */
    ElectricMeterBo getDetail(@NotNull Integer id);

    /**
     * 根据物联网设备ID获取电表信息
     *
     * @param iotId 物联网设备ID
     * @return 电表信息
     * @throws NotFoundException        当电表不存在时
     * @throws BusinessRuntimeException 当存在多个电表时
     */
    ElectricMeterBo getByIotId(@NotNull Integer iotId);

    /**
     * 根据销户编号查询销表明细
     *
     * @param cancelNo 销户编号
     * @return 销表明细列表
     */
    List<CanceledMeterDto> findMetersByCancelNo(@NotEmpty String cancelNo);

}
