package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.GatewayService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.common.dto.SpaceDisplayDto;
import info.zhihui.ems.web.common.support.SpaceDisplaySupport;
import info.zhihui.ems.web.device.mapstruct.GatewayWebMapper;
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
 * 网关业务编排层
 */
@Service
@RequiredArgsConstructor
public class GatewayBiz {

    private final GatewayService gatewayService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final SpaceDisplaySupport spaceDisplaySupport;
    private final GatewayWebMapper gatewayWebMapper;

    /**
     * 分页查询网关
     *
     * @param queryVo  查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 网关分页结果
     */
    public PageResult<GatewayVo> findGatewayPage(GatewayQueryVo queryVo, Integer pageNum, Integer pageSize) {
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        GatewayQueryDto queryDto = gatewayWebMapper.toGatewayQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new GatewayQueryDto();
        }
        PageResult<GatewayBo> pageResult = gatewayService.findPage(queryDto, pageParam);
        PageResult<GatewayVo> result = gatewayWebMapper.toGatewayVoPage(pageResult);
        fillSpaceInfo(result.getList(), pageResult.getList());
        return result;
    }

    /**
     * 查询网关列表
     *
     * @param queryVo 查询条件
     * @return 网关列表
     */
    public List<GatewayVo> findGatewayList(GatewayQueryVo queryVo) {
        GatewayQueryDto queryDto = gatewayWebMapper.toGatewayQueryDto(queryVo);

        List<GatewayBo> bos = gatewayService.findList(queryDto);
        List<GatewayVo> gatewayVoList = gatewayWebMapper.toGatewayVoList(bos);
        fillSpaceInfo(gatewayVoList, bos);
        return gatewayVoList;
    }

    /**
     * 获取网关详情
     *
     * @param id 网关ID
     * @return 网关详情
     */
    public GatewayDetailVo getGateway(Integer id) {
        GatewayBo gatewayBo = gatewayService.getDetail(id);
        GatewayDetailVo detailVo = gatewayWebMapper.toGatewayDetailVo(gatewayBo);
        fillSpaceInfo(Collections.singletonList(detailVo), Collections.singletonList(gatewayBo));
        fillGatewayMeterList(detailVo, id);
        return detailVo;
    }

    /**
     * 新增网关
     *
     * @param addVo 新增参数
     * @return 网关ID
     */
    public Integer addGateway(GatewayAddVo addVo) {
        GatewayCreateDto dto = gatewayWebMapper.toGatewayCreateDto(addVo);

        return gatewayService.add(dto);
    }

    /**
     * 更新网关
     *
     * @param id     网关ID
     * @param saveVo 保存参数
     */
    public void updateGateway(Integer id, GatewayUpdateVo saveVo) {
        GatewayUpdateDto dto = gatewayWebMapper.toGatewayUpdateDto(saveVo);

        dto.setId(id);
        gatewayService.update(dto);
    }

    /**
     * 删除网关
     *
     * @param id 网关ID
     */
    public void deleteGateway(Integer id) {
        gatewayService.delete(id);
    }

    /**
     * 同步网关在线状态
     *
     * @param onlineStatusVo 同步参数
     */
    public void syncOnlineStatus(GatewayOnlineStatusVo onlineStatusVo) {
        gatewayService.syncGatewayOnlineStatus(gatewayWebMapper.toGatewayOnlineStatusDto(onlineStatusVo));
    }

    private void fillSpaceInfo(List<? extends GatewayVo> gatewayVoList, List<GatewayBo> gatewayBoList) {
        if (gatewayVoList == null || gatewayVoList.isEmpty() || gatewayBoList == null || gatewayBoList.isEmpty()) {
            return;
        }

        Map<Integer, GatewayVo> gatewayVoMap = gatewayVoList.stream()
                .filter(Objects::nonNull)
                .filter(gatewayVo -> gatewayVo.getId() != null)
                .collect(Collectors.toMap(GatewayVo::getId, Function.identity(), (left, right) -> left));
        Map<Integer, SpaceDisplayDto> spaceDisplayMap = findSpaceDisplayMap(gatewayBoList);
        for (GatewayBo gatewayBo : gatewayBoList) {
            if (gatewayBo == null) {
                continue;
            }
            GatewayVo gatewayVo = gatewayVoMap.get(gatewayBo.getId());
            if (gatewayVo == null) {
                continue;
            }
            fillGatewaySpaceInfo(gatewayVo, gatewayBo, spaceDisplayMap);
        }
    }

    private Map<Integer, SpaceDisplayDto> findSpaceDisplayMap(List<GatewayBo> gatewayBoList) {
        Set<Integer> spaceIdSet = gatewayBoList.stream()
                .map(GatewayBo::getSpaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return spaceDisplaySupport.findSpaceDisplayMap(spaceIdSet);
    }

    private void fillGatewaySpaceInfo(GatewayVo gatewayVo,
                                      GatewayBo gatewayBo,
                                      Map<Integer, SpaceDisplayDto> spaceDisplayMap) {
        if (gatewayBo.getSpaceId() == null || spaceDisplayMap.isEmpty()) {
            return;
        }
        SpaceDisplayDto spaceDisplayDto = spaceDisplayMap.get(gatewayBo.getSpaceId());
        if (spaceDisplayDto == null) {
            return;
        }
        gatewayVo.setSpaceName(spaceDisplayDto.getName());
        gatewayVo.setSpaceParentNames(spaceDisplayDto.getParentsNames());
    }

    private void fillGatewayMeterList(GatewayDetailVo detailVo, Integer gatewayId) {
        List<ElectricMeterBo> meterBoList = electricMeterInfoService.findList(new ElectricMeterQueryDto().setGatewayId(gatewayId));
        detailVo.setMeterList(gatewayWebMapper.toGatewayMeterVoList(meterBoList));
    }
}
