package info.zhihui.ems.business.device.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.mapper.ElectricMeterMapper;
import info.zhihui.ems.business.device.qo.ElectricMeterQo;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.device.repository.MeterCancelRecordRepository;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 电表数据信息服务接口
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class ElectricMeterInfoServiceImpl implements ElectricMeterInfoService {
    private final ElectricMeterRepository repository;
    private final ElectricMeterMapper mapper;
    private final MeterCancelRecordRepository meterCancelRecordRepository;

    /**
     * 分页查询电表列表
     *
     * @param query     查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<ElectricMeterBo> findPage(ElectricMeterQueryDto query, PageParam pageParam) {
        ElectricMeterQo qo = mapper.queryDtoToQo(query);

        try (Page<ElectricMeterEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<ElectricMeterEntity> pageInfo = page.doSelectPageInfo(() -> repository.findList(qo));
            return mapper.pageEntityToBo(pageInfo);
        }
    }

    /**
     * 查询电表列表
     *
     * @param query 查询条件
     * @return 电表列表
     */
    @Override
    public List<ElectricMeterBo> findList(ElectricMeterQueryDto query) {
        ElectricMeterQo qo = mapper.queryDtoToQo(query);
        List<ElectricMeterEntity> list = repository.findList(qo);
        return mapper.listEntityToBo(list);
    }

    /**
     * 根据ID获取电表详情
     *
     * @param id 电表ID
     * @return 电表详情
     * @throws NotFoundException 当电表不存在时
     */
    @Override
    public ElectricMeterBo getDetail(Integer id) {
        ElectricMeterEntity entity = getEntity(id);
        return mapper.entityToBo(entity);
    }

    private ElectricMeterEntity getEntity(Integer id) {
        ElectricMeterEntity entity = repository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("电表数据不存在或已被删除");
        }

        return entity;
    }

    /**
     * 根据物联网设备ID获取电表信息
     *
     * @param iotId 物联网设备ID
     * @return 电表信息
     * @throws NotFoundException        当电表不存在时
     * @throws BusinessRuntimeException 当存在多个电表时
     */
    @Override
    public ElectricMeterBo getByIotId(@NotBlank String iotId) {
        List<ElectricMeterEntity> meterList = repository.findList(new ElectricMeterQo().setIotId(iotId));

        if (meterList.isEmpty()) {
            throw new NotFoundException(String.format("能耗系统查询到iotId=%s没有匹配的电表", iotId));
        } else if (meterList.size() > 1) {
            throw new BusinessRuntimeException(String.format("能耗系统查询到iotId=%s的电表数量=%d无法匹配电表", iotId, meterList.size()));
        }

        return mapper.entityToBo(meterList.get(0));
    }

    /**
     * 根据销户编号查询销表明细
     *
     * @param cancelNo 销户编号
     * @return 销表明细列表
     */
    @Override
    public List<CanceledMeterDto> findMetersByCancelNo(@NotEmpty String cancelNo) {
        // 查询销表记录
        List<MeterCancelRecordEntity> meterRecords = meterCancelRecordRepository.selectByCancelNo(cancelNo);

        return mapper.listMeterEntityToDto(meterRecords);
    }

}
