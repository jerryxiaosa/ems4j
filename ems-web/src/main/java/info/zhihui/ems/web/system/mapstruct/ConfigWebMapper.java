package info.zhihui.ems.web.system.mapstruct;

import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.dto.ConfigQueryDto;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.web.system.vo.ConfigQueryVo;
import info.zhihui.ems.web.system.vo.ConfigUpdateVo;
import info.zhihui.ems.web.system.vo.ConfigVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * 系统配置 Web 映射器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConfigWebMapper {

    ConfigQueryDto toConfigQueryDto(ConfigQueryVo queryVo);

    @Mapping(target = "configKey", source = "configKey")
    ConfigUpdateDto toConfigUpdateDto(ConfigUpdateVo updateVo);

    ConfigVo toConfigVo(ConfigBo bo);

    List<ConfigVo> toConfigVoList(List<ConfigBo> bos);

    default PageResult<ConfigVo> toConfigVoPage(PageResult<ConfigBo> pageResult) {
        if (pageResult == null) {
            return new PageResult<ConfigVo>()
                    .setPageNum(0)
                    .setPageSize(0)
                    .setTotal(0L)
                    .setList(Collections.emptyList());
        }
        List<ConfigBo> bos = pageResult.getList();
        List<ConfigVo> vos = bos == null ? Collections.emptyList() : toConfigVoList(bos);
        return new PageResult<ConfigVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(vos);
    }
}
