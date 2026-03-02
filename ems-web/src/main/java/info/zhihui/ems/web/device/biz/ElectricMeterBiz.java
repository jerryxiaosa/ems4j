package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusDto;
import info.zhihui.ems.business.device.dto.ElectricMeterTimeDto;
import info.zhihui.ems.business.device.dto.ElectricMeterUpdateDto;
import info.zhihui.ems.business.finance.dto.ElectricMeterLatestPowerRecordDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.finance.service.record.ElectricMeterPowerRecordService;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.web.device.mapstruct.ElectricMeterWebMapper;
import info.zhihui.ems.web.common.util.OfflineDurationUtil;
import info.zhihui.ems.web.device.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 电表业务编排层
 */
@Service
@RequiredArgsConstructor
public class ElectricMeterBiz {

    private final ElectricMeterInfoService electricMeterInfoService;
    private final ElectricMeterManagerService electricMeterManagerService;
    private final ElectricMeterPowerRecordService electricMeterPowerRecordService;
    private final SpaceService spaceService;
    private final ElectricMeterWebMapper electricMeterWebMapper;

    /**
     * 分页查询电表列表
     *
     * @param queryVo  查询条件
     * @param pageNum  页码
     * @param pageSize 分页大小
     * @return 电表分页结果
     */
    public PageResult<ElectricMeterVo> findElectricMeterPage(ElectricMeterQueryVo queryVo, Integer pageNum, Integer pageSize) {
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        ElectricMeterQueryDto queryDto = electricMeterWebMapper.toElectricMeterQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new ElectricMeterQueryDto();
        }
        PageResult<ElectricMeterBo> pageResult = electricMeterInfoService.findPage(queryDto, pageParam);
        PageResult<ElectricMeterVo> result = electricMeterWebMapper.toElectricMeterVoPage(pageResult);
        fillDisplayInfo(result.getList(), pageResult.getList());
        return result;
    }

    /**
     * 查询电表列表
     *
     * @param queryVo 查询条件
     * @return 电表列表
     */
    public List<ElectricMeterVo> findElectricMeterList(ElectricMeterQueryVo queryVo) {
        ElectricMeterQueryDto queryDto = electricMeterWebMapper.toElectricMeterQueryDto(queryVo);

        List<ElectricMeterBo> bos = electricMeterInfoService.findList(queryDto);
        List<ElectricMeterVo> meterVoList = electricMeterWebMapper.toElectricMeterVoList(bos);
        fillDisplayInfo(meterVoList, bos);
        return meterVoList;
    }

    /**
     * 获取电表详情
     *
     * @param id 电表ID
     * @return 电表详情
     */
    public ElectricMeterDetailVo getElectricMeter(Integer id) {
        ElectricMeterBo meterBo = electricMeterInfoService.getDetail(id);
        ElectricMeterDetailVo detailVo = electricMeterWebMapper.toElectricMeterDetailVo(meterBo);
        fillDisplayInfo(Collections.singletonList(detailVo), Collections.singletonList(meterBo));
        return detailVo;
    }

    /**
     * 获取电表最近一次上报电量记录
     *
     * @param meterId 电表ID
     * @return 最近一次上报电量记录，无记录时返回 null
     */
    public ElectricMeterLatestPowerRecordVo getLatestPowerRecord(Integer meterId) {
        electricMeterInfoService.getDetail(meterId);
        ElectricMeterLatestPowerRecordDto latestRecordDto = electricMeterPowerRecordService.findLatestRecord(meterId);
        return latestRecordDto == null ? null : electricMeterWebMapper.toElectricMeterLatestPowerRecordVo(latestRecordDto);
    }

    /**
     * 新增电表
     *
     * @param createVo 新增参数
     * @return 新增电表ID
     */
    public Integer addElectricMeter(ElectricMeterCreateVo createVo) {
        return electricMeterManagerService.add(electricMeterWebMapper.toElectricMeterAddDto(createVo));
    }

    /**
     * 更新电表
     *
     * @param id        电表ID
     * @param updateVo  更新参数
     */
    public void updateElectricMeter(Integer id, ElectricMeterUpdateVo updateVo) {
        ElectricMeterUpdateDto updateDto = electricMeterWebMapper.toElectricMeterUpdateDto(updateVo);

        updateDto.setId(id);
        electricMeterManagerService.update(updateDto);
    }

    /**
     * 删除电表
     *
     * @param id 电表ID
     */
    public void deleteElectricMeter(Integer id) {
        electricMeterManagerService.delete(id);
    }

    /**
     * 设置电表开关状态
     *
     * @param switchStatusVo 参数
     */
    public void changeSwitchStatus(ElectricMeterSwitchStatusVo switchStatusVo) {
        ElectricMeterSwitchStatusDto dto = electricMeterWebMapper.toSwitchStatusDto(switchStatusVo);
        dto.setCommandSource(CommandSourceEnum.USER);

        electricMeterManagerService.setSwitchStatus(dto);
    }

    /**
     * 设置电价时间段
     *
     * @param timeVo 参数
     */
    public void updateElectricTime(ElectricMeterTimeVo timeVo) {
        ElectricMeterTimeDto dto = electricMeterWebMapper.toElectricMeterTimeDto(timeVo);
        dto.setCommandSource(CommandSourceEnum.USER);

        electricMeterManagerService.setElectricTime(dto);
    }

    /**
     * 设置保电模式
     *
     * @param protectVo 参数
     */
    public void updateProtectModel(ElectricMeterProtectVo protectVo) {
        electricMeterManagerService.setProtectModel(protectVo.getMeterIds(), Boolean.TRUE.equals(protectVo.getProtect()));
    }

    /**
     * 设置CT变比
     *
     * @param ctVo 参数
     * @return 新电表ID
     */
    public Integer updateMeterCt(ElectricMeterCtVo ctVo) {
        return electricMeterManagerService.setMeterCt(electricMeterWebMapper.toElectricMeterCtDto(ctVo));
    }

    /**
     * 同步在线状态
     *
     * @param onlineStatusVo 参数
     */
    public void syncOnlineStatus(ElectricMeterOnlineStatusVo onlineStatusVo) {
        electricMeterManagerService.syncMeterOnlineStatus(electricMeterWebMapper.toElectricMeterOnlineStatusDto(onlineStatusVo));
    }

    /**
     * 查询电量
     *
     * @param meterId 电表ID
     * @param queryVo 查询电量类型
     * @return 电量结果
     */
    public List<ElectricMeterPowerVo> getMeterPower(Integer meterId, ElectricMeterPowerQueryVo queryVo) {
        List<Integer> typeCodes = queryVo == null ? Collections.emptyList() : queryVo.getTypes();
        List<ElectricPricePeriodEnum> types = electricMeterWebMapper.toElectricDegreeTypeEnumList(typeCodes);
        return electricMeterWebMapper.toElectricMeterPowerVoList(
                electricMeterManagerService.getMeterPower(meterId, types));
    }

    private void fillDisplayInfo(List<? extends ElectricMeterVo> meterVoList, List<ElectricMeterBo> meterBoList) {
        if (meterVoList == null || meterVoList.isEmpty() || meterBoList == null || meterBoList.isEmpty()) {
            return;
        }

        Map<Integer, ElectricMeterVo> meterVoMap = meterVoList.stream()
                .filter(Objects::nonNull)
                .filter(meterVo -> meterVo.getId() != null)
                .collect(Collectors.toMap(ElectricMeterVo::getId, Function.identity(), (left, right) -> left));
        Map<Integer, SpaceBo> spaceBoMap = findSpaceBoMap(meterBoList);
        for (ElectricMeterBo meterBo : meterBoList) {
            if (meterBo == null) {
                continue;
            }
            ElectricMeterVo meterVo = meterVoMap.get(meterBo.getId());
            if (meterVo == null) {
                continue;
            }
            meterVo.setOfflineDurationText(OfflineDurationUtil.format(meterBo.getIsOnline(), meterBo.getLastOnlineTime()));
            fillSpaceInfo(meterVo, meterBo, spaceBoMap);
        }
    }

    private Map<Integer, SpaceBo> findSpaceBoMap(List<ElectricMeterBo> meterBoList) {
        Set<Integer> spaceIdSet = meterBoList.stream()
                .map(ElectricMeterBo::getSpaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (spaceIdSet.isEmpty()) {
            return Collections.emptyMap();
        }

        return spaceService.findSpaceList(new SpaceQueryDto().setIds(spaceIdSet)).stream()
                .collect(Collectors.toMap(SpaceBo::getId, Function.identity(), (left, right) -> left));
    }

    private void fillSpaceInfo(ElectricMeterVo meterVo, ElectricMeterBo meterBo, Map<Integer, SpaceBo> spaceBoMap) {
        if (meterBo.getSpaceId() == null || spaceBoMap.isEmpty()) {
            return;
        }
        SpaceBo spaceBo = spaceBoMap.get(meterBo.getSpaceId());
        if (spaceBo == null) {
            return;
        }
        meterVo.setSpaceName(spaceBo.getName());
        meterVo.setSpaceParentNames(spaceBo.getParentsNames());
    }

}
