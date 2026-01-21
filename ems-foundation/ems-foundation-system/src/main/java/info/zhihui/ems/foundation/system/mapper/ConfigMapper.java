package info.zhihui.ems.foundation.system.mapper;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.dto.ConfigQueryDto;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.entity.ConfigEntity;
import info.zhihui.ems.foundation.system.qo.ConfigQueryQo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConfigMapper {

    ConfigBo entityToBo(ConfigEntity entity);

    ConfigEntity updateBoToEntity(ConfigUpdateDto bo);

    List<ConfigBo> listEntityToBo(List<ConfigEntity> entities);

    default PageResult<ConfigBo> pageEntityToPageBo(PageInfo<ConfigEntity> pageInfo) {
        PageResult<ConfigBo> result = new PageResult<>();
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        List<ConfigBo> list = listEntityToBo(pageInfo.getList());
        result.setList(list == null ? Collections.emptyList() : list);
        return result;
    }

    ConfigQueryQo queryDtoToQo(ConfigQueryDto dto);
}
