package info.zhihui.ems.foundation.space.mapstruct;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import info.zhihui.ems.foundation.space.entity.SpaceEntity;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 空间对象转换器
 *
 * @author jerryxiaosa
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpaceMapper {

    /**
     * SpaceCreateDto转换为SpaceEntity
     */
    @Mapping(target = "type", source = "type", qualifiedByName = "typeEnumToCode")
    SpaceEntity toEntity(SpaceCreateDto dto);

    /**
     * SpaceUpdateDto转换为SpaceEntity
     */
    @Mapping(target = "type", source = "type", qualifiedByName = "typeEnumToCode")
    SpaceEntity toEntity(SpaceUpdateDto dto);

    /**
     * SpaceEntity转换为SpaceBo
     */
    @Mapping(target = "type", source = "type", qualifiedByName = "typeCodeToEnum")
    SpaceBo toBo(SpaceEntity entity);

    /**
     * SpaceEntity列表转换为SpaceBo列表
     */
    List<SpaceBo> toBoList(List<SpaceEntity> entities);

    /**
     * 枚举转换为code
     */
    @Named("typeEnumToCode")
    default Integer typeEnumToCode(SpaceTypeEnum type) {
        return type == null ? null : type.getCode();
    }

    /**
     * code转换为枚举
     */
    @Named("typeCodeToEnum")
    default SpaceTypeEnum typeCodeToEnum(Integer code) {
        return CodeEnum.fromCode(code, SpaceTypeEnum.class);
    }
}