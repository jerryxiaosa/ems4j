package info.zhihui.ems.web.device.mapstruct;

import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.GatewayOnlineStatusDto;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.device.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * 网关 Web 层映射器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GatewayWebMapper {

    GatewayQueryDto toGatewayQueryDto(GatewayQueryVo queryVo);

    GatewayCreateDto toGatewayCreateDto(GatewayAddVo addVo);

    GatewayUpdateDto toGatewayUpdateDto(GatewayUpdateVo saveVo);

    GatewayVo toGatewayVo(GatewayBo bo);

    List<GatewayVo> toGatewayVoList(List<GatewayBo> bos);

    GatewayDetailVo toGatewayDetailVo(GatewayBo bo);

    GatewayOnlineStatusDto toGatewayOnlineStatusDto(GatewayOnlineStatusVo vo);

    default PageResult<GatewayVo> toGatewayVoPage(PageResult<GatewayBo> pageResult) {
        if (pageResult == null) {
            return new PageResult<GatewayVo>()
                    .setPageNum(0)
                    .setPageSize(0)
                    .setTotal(0L)
                    .setList(Collections.emptyList());
        }
        List<GatewayVo> list = toGatewayVoList(pageResult.getList());
        return new PageResult<GatewayVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(list == null ? Collections.emptyList() : list);
    }
}
