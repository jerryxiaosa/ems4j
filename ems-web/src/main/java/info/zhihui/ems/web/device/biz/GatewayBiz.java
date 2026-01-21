package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import info.zhihui.ems.business.device.service.GatewayService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.device.mapstruct.GatewayWebMapper;
import info.zhihui.ems.web.device.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 网关业务编排层
 */
@Service
@RequiredArgsConstructor
public class GatewayBiz {

    private final GatewayService gatewayService;
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
        return gatewayWebMapper.toGatewayVoPage(pageResult);
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
        return gatewayWebMapper.toGatewayVoList(bos);
    }

    /**
     * 获取网关详情
     *
     * @param id 网关ID
     * @return 网关详情
     */
    public GatewayDetailVo getGateway(Integer id) {
        return gatewayWebMapper.toGatewayDetailVo(gatewayService.getDetail(id));
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
     * 获取通信方式
     *
     * @return 通信方式列表
     */
    public List<String> findCommunicationOptions() {
        return gatewayService.getCommunicationOption();
    }

    /**
     * 同步网关在线状态
     *
     * @param onlineStatusVo 同步参数
     */
    public void syncOnlineStatus(GatewayOnlineStatusVo onlineStatusVo) {
        gatewayService.syncGatewayOnlineStatus(gatewayWebMapper.toGatewayOnlineStatusDto(onlineStatusVo));
    }
}
