package info.zhihui.ems.business.device.service.impl;

import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.constant.DeviceConstant;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.DeviceStatusSyncRequestDto;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayOnlineStatusDto;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import info.zhihui.ems.business.device.entity.GatewayEntity;
import info.zhihui.ems.business.device.mapper.GatewayMapper;
import info.zhihui.ems.business.device.qo.GatewayQo;
import info.zhihui.ems.business.device.repository.GatewayRepository;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.GatewayService;
import info.zhihui.ems.business.device.service.DeviceStatusSynchronizer;
import info.zhihui.ems.business.device.utils.DeviceUtil;
import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.SerialNumberGeneratorUtil;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.BaseElectricDeviceDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceAddDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceUpdateDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.service.SpaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 网关服务接口
 * 提供网关设备的增删改查及相关业务操作
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class GatewayServiceImpl implements GatewayService {
    private final GatewayRepository repository;
    private final GatewayMapper mapper;
    private final DeviceModelService deviceModelService;
    private final SpaceService spaceService;
    private final DeviceModuleContext deviceModuleContext;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final DeviceStatusSynchronizer<GatewayEntity> gatewayOnlineStatusSynchronizer;

    /**
     * 根据ID获取网关详情
     *
     * @param id 网关ID
     * @return 网关业务对象，如果不存在则返回null
     */
    @Override
    public GatewayBo getDetail(Integer id) {
        GatewayEntity entity = getEntity(id);

        return mapper.entityToBo(entity);
    }

    private GatewayEntity getEntity(Integer id) {
        GatewayEntity entity = repository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("网关不存在，请确认");
        }
        return entity;
    }

    /**
     * 分页查询网关列表
     *
     * @param query     查询条件
     * @param pageParam 分页参数
     * @return 分页结果，包含网关业务对象列表
     */
    @Override
    public PageResult<GatewayBo> findPage(GatewayQueryDto query, PageParam pageParam) {
        GatewayQo queryQo = mapper.queryDtoToQo(query);
        PageInfo<GatewayEntity> pageInfo = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize()).doSelectPageInfo(() -> repository.findList(queryQo));
        return mapper.pageEntityToBo(pageInfo);
    }

    /**
     * 查询网关列表（不分页）
     *
     * @param query 查询条件
     * @return 网关业务对象列表
     */
    @Override
    public List<GatewayBo> findList(GatewayQueryDto query) {
        GatewayQo queryQo = mapper.queryDtoToQo(query);
        List<GatewayEntity> list = repository.findList(queryQo);
        return mapper.listEntityToBo(list);
    }

    /**
     * 新增网关
     *
     * @param dto 网关保存数据传输对象
     * @return 新增网关的ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer add(@Valid @NotNull GatewayCreateDto dto) {
        GatewayEntity entity = mapper.createDtoToEntity(dto);
        buildGatewayEntity(entity);

        // 临时设置网关编号，后续用生成规则来更新
        entity.setGatewayNo(IdUtil.fastSimpleUUID());

        repository.insert(entity);

        // 设置网关编号，只有新增时需要
        setGatewayNo(entity);

        // 同步到IoT平台
        syncToIotPlatform(entity);
        if (entity.getIotId() != null) {
            repository.updateById(new GatewayEntity().setId(entity.getId()).setIotId(entity.getIotId()));
        }

        return entity.getId();
    }

    /**
     * 更新网关信息
     *
     * @param dto 网关保存数据传输对象，必须包含ID
     */
    @Override
    public void update(@Valid @NotNull GatewayUpdateDto dto) {
        GatewayEntity old = getEntity(dto.getId());

        GatewayEntity entity = mapper.updateDtoToEntity(dto);
        buildGatewayEntity(entity);

        boolean configChanged = StringUtils.isNotBlank(dto.getConfigInfo()) && !dto.getConfigInfo().equals(old.getConfigInfo());
        boolean deviceNoChanged = StringUtils.isNotBlank(entity.getDeviceNo()) && !entity.getDeviceNo().equals(old.getDeviceNo());
        if (configChanged || deviceNoChanged) {
            if (old.getIotId() == null) {
                throw new BusinessRuntimeException("数据异常：网关没有对应的iot数据");
            }
            entity.setIotId(old.getIotId());
            // 同步到IoT平台
            syncToIotPlatform(entity);
        }

        repository.updateById(entity);
    }

    /**
     * 构建网关实体对象
     */
    private void buildGatewayEntity(GatewayEntity entity) {
        // 设置设备型号信息
        setDeviceModelInfo(entity);

        // 设置通讯模式和IMEI验证
        setCommunicationInfo(entity);

        // 设置区域信息
        setAreaInfo(entity, entity.getSpaceId());
    }

    /**
     * 设置设备型号信息
     */
    private void setDeviceModelInfo(GatewayEntity entity) {
        DeviceModelBo deviceModel = deviceModelService.getDetail(entity.getModelId());
        if (deviceModel == null) {
            throw new BusinessRuntimeException("网关型号不存在，请重新选择");
        }

        if (!DeviceTypeEnum.GATEWAY.getKey().equals(deviceModel.getTypeKey())) {
            throw new BusinessRuntimeException("网关型号设置错误");
        }

        entity.setProductCode(deviceModel.getProductCode());

        String communicateModel = DeviceUtil.getProperty(
                deviceModel.getModelProperty(),
                DeviceConstant.COMMUNICATE_MODE,
                String.class);
        entity.setCommunicateModel(communicateModel);
    }

    /**
     * 设置通讯信息并验证IMEI
     */
    private void setCommunicationInfo(GatewayEntity entity) {
        if (DeviceUtil.isNbCommunicateModel(entity.getCommunicateModel())) {
            if (StringUtils.isBlank(entity.getImei())) {
                throw new BusinessRuntimeException("NB模式网关IMEI不能为空");
            }
        } else {
            entity.setImei("");
        }
    }

    /**
     * 设置区域信息
     */
    private void setAreaInfo(GatewayEntity entity, Integer spaceId) {
        SpaceBo space = spaceService.getDetail(spaceId);
        if (space == null) {
            throw new BusinessRuntimeException("空间信息不存在，请重新选择");
        }
        entity.setOwnAreaId(space.getOwnAreaId());
    }

    /**
     * 更新网关编号
     */
    private void setGatewayNo(GatewayEntity gatewayEntity) {
        String gatewayNo = SerialNumberGeneratorUtil.genGatewayNo(gatewayEntity.getId());
        gatewayEntity.setGatewayNo(gatewayNo);

        GatewayEntity updateEntity = new GatewayEntity();
        updateEntity.setId(gatewayEntity.getId());
        updateEntity.setGatewayNo(gatewayNo);
        repository.updateById(updateEntity);
    }

    /**
     * 同步到IoT平台
     */
    private void syncToIotPlatform(GatewayEntity entity) {
        Integer newIotId;
        if (entity.getIotId() == null) {
            newIotId = createIotDevice(entity);
        } else {
            newIotId = updateIotDevice(entity, entity.getIotId());
        }

        entity.setIotId(newIotId);
    }

    /**
     * 创建IoT设备
     */
    private Integer createIotDevice(GatewayEntity entity) {
        EnergyService energyService = deviceModuleContext.getService(
                EnergyService.class,
                entity.getOwnAreaId());

        ElectricDeviceAddDto addDto = new ElectricDeviceAddDto()
                .setDeviceNo(entity.getDeviceNo())
                .setProductCode(entity.getProductCode())
                .setAreaId(entity.getOwnAreaId());

        return energyService.addDevice(addDto);
    }

    private Integer updateIotDevice(GatewayEntity entity, Integer oldIotId) {
        EnergyService energyService = deviceModuleContext.getService(
                EnergyService.class,
                entity.getOwnAreaId());

        ElectricDeviceUpdateDto updateDto = new ElectricDeviceUpdateDto();
        updateDto.setDeviceNo(entity.getDeviceNo())
                .setProductCode(entity.getProductCode())
                .setDeviceId(oldIotId)
                .setAreaId(entity.getOwnAreaId());

        return energyService.editDevice(updateDto);
    }

    /**
     * 删除网关
     *
     * @param id 网关ID
     */
    @Override
    public void delete(Integer id) {
        GatewayEntity old = getEntity(id);

        // 校验是否存在关联电表
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setGatewayId(id);
        List<ElectricMeterBo> associatedMeters = electricMeterInfoService.findList(queryDto);
        if (!associatedMeters.isEmpty()) {
            log.warn("该网关下还有关联电表，无法删除。网关id：{}", id);
            throw new BusinessRuntimeException("该网关下还有关联电表，请先处理这些电表后再删除网关");
        }

        // 同步iot平台
        if (old.getIotId() != null) {
            EnergyService energyService = deviceModuleContext.getService(EnergyService.class, old.getOwnAreaId());
            energyService.delDevice(new BaseElectricDeviceDto().setDeviceId(old.getIotId()).setAreaId(old.getOwnAreaId()));
        }

        repository.deleteById(id);
    }

    /**
     * 获取通信方式选项列表
     *
     * @return 通信方式选项列表
     */
    @Override
    public List<String> getCommunicationOption() {
        return repository.getCommunicationOption();
    }

    @Override
    public void syncGatewayOnlineStatus(@Valid @NotNull GatewayOnlineStatusDto onlineStatusDto) {
        gatewayOnlineStatusSynchronizer.syncStatus(
                () -> getEntity(onlineStatusDto.getGatewayId()),
                new DeviceStatusSyncRequestDto()
                        .setForce(onlineStatusDto.getForce())
                        .setOnlineStatus(onlineStatusDto.getOnlineStatus())
        );
    }
}
