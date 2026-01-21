package info.zhihui.ems.web.space.mapstruct;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.web.space.vo.*;
import info.zhihui.ems.web.space.vo.SpaceWithChildrenVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 空间 Web 层映射器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpaceWebMapper {

    @Mapping(target = "type", expression = "java(mapTypeList(queryVo.getType()))")
    SpaceQueryDto toSpaceQueryDto(SpaceQueryVo queryVo);

    @Mapping(target = "type", expression = "java(mapType(createVo.getType()))")
    SpaceCreateDto toSpaceCreateDto(SpaceCreateVo createVo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(mapType(updateVo.getType()))")
    SpaceUpdateDto toSpaceUpdateDto(SpaceUpdateVo updateVo);

    @Mapping(target = "type", expression = "java(mapTypeCode(spaceBo.getType()))")
    SpaceVo toSpaceDetailVo(SpaceBo spaceBo);

    @Mapping(target = "type", expression = "java(mapTypeCode(spaceBo.getType()))")
    SpaceWithChildrenVo toSpaceVo(SpaceBo spaceBo);

    List<SpaceWithChildrenVo> toSpaceVoList(List<SpaceBo> bos);

    List<SpaceVo> toSpaceDetailVoList(List<SpaceBo> bos);

    default SpaceTypeEnum mapType(Integer code) {
        if (code == null) {
            return null;
        }
        return CodeEnum.fromCode(code, SpaceTypeEnum.class);
    }

    default List<SpaceTypeEnum> mapTypeList(List<Integer> codes) {
        if (codes == null) {
            return null;
        }
        return codes.stream()
                .map(this::mapType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default Integer mapTypeCode(SpaceTypeEnum typeEnum) {
        return typeEnum == null ? null : typeEnum.getCode();
    }
}
