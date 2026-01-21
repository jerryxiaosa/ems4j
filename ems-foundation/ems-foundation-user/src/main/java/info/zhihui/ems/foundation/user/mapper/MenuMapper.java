package info.zhihui.ems.foundation.user.mapper;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.MenuDetailBo;
import info.zhihui.ems.foundation.user.dto.MenuCreateDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.dto.MenuUpdateDto;
import info.zhihui.ems.foundation.user.entity.MenuEntity;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import info.zhihui.ems.foundation.user.qo.MenuQueryQo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuMapper {

    @Mapping(target = "menuSource", source = "menuSource", qualifiedByName = "menuSourceCodeToEnum")
    @Mapping(target = "menuType", source = "menuType", qualifiedByName = "menuTypeCodeToEnum")
    MenuDetailBo entityToDetailBo(MenuEntity entity);

    // listEntityToBo需要
    @Mapping(target = "menuSource", source = "menuSource", qualifiedByName = "menuSourceCodeToEnum")
    @Mapping(target = "menuType", source = "menuType", qualifiedByName = "menuTypeCodeToEnum")
    MenuBo entityToBo(MenuEntity entity);

    List<MenuBo> listEntityToBo(List<MenuEntity> list);

    @Mapping(target = "menuSource", source = "menuSource", qualifiedByName = "menuSourceEnumToCode")
    @Mapping(target = "menuType", source = "menuType", qualifiedByName = "menuTypeEnumToCode")
    MenuEntity createDtoToEntity(MenuCreateDto dto);

    @Mapping(target = "menuSource", source = "menuSource", qualifiedByName = "menuSourceEnumToCode")
    @Mapping(target = "menuType", source = "menuType", qualifiedByName = "menuTypeEnumToCode")
    MenuEntity updateDtoToEntity(MenuUpdateDto dto);

    @Mapping(target = "menuSource", source = "menuSource", qualifiedByName = "menuSourceEnumToCode")
    MenuQueryQo dtoToQo(MenuQueryDto dto);

    @Named("menuSourceEnumToCode")
    default Integer menuSourceEnumToCode(MenuSourceEnum menuSource) {
        return menuSource == null ? null : menuSource.getCode();
    }

    @Named("menuSourceCodeToEnum")
    default MenuSourceEnum menuSourceCodeToEnum(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, MenuSourceEnum.class);
    }

    @Named("menuTypeEnumToCode")
    default Integer menuTypeEnumToCode(MenuTypeEnum menuType) {
        return menuType == null ? null : menuType.getCode();
    }

    @Named("menuTypeCodeToEnum")
    default MenuTypeEnum menuTypeCodeToEnum(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, MenuTypeEnum.class);
    }
}