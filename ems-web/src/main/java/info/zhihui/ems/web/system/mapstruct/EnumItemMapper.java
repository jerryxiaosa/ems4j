package info.zhihui.ems.web.system.mapstruct;

import info.zhihui.ems.foundation.system.dto.EnumItemDto;
import info.zhihui.ems.web.system.vo.EnumItemVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;

/**
 * 枚举项映射器：负责 DTO 到 VO 的转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnumItemMapper {

    /**
     * 单个枚举项 DTO 转换为 VO
     */
    EnumItemVo dtoToVo(EnumItemDto dto);

    /**
     * 枚举项列表 DTO 转换为 VO 列表
     */
    List<EnumItemVo> listDtoToVo(List<EnumItemDto> dtoList);

    /**
     * 枚举映射 DTO 转换为 VO 映射
     */
    Map<String, List<EnumItemVo>> mapDtoToVo(Map<String, List<EnumItemDto>> dtoMap);
}