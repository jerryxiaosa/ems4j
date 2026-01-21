package info.zhihui.ems.web.user.mapstruct;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.MenuDetailBo;
import info.zhihui.ems.foundation.user.dto.MenuCreateDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.dto.MenuUpdateDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import info.zhihui.ems.web.user.vo.*;
import info.zhihui.ems.web.user.vo.MenuWithChildrenVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 菜单 Web 映射器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuWebMapper {

    /**
     * MenuBo 转 MenuVo
     */
    @Mapping(target = "menuSource", expression = "java(menuBo.getMenuSource() != null ? menuBo.getMenuSource().name() : null)")
    @Mapping(target = "menuType", expression = "java(menuBo.getMenuType() != null ? menuBo.getMenuType().name() : null)")
    @Mapping(source = "isHidden", target = "hidden")
    MenuWithChildrenVo toMenuVo(MenuBo menuBo);

    /**
     * MenuBo 列表转 MenuVo 列表
     */
    List<MenuWithChildrenVo> toMenuVoList(List<MenuBo> menuBos);

    /**
     * MenuDetailBo 转 MenuDetailVo
     */
    @Mapping(source = "isHidden", target = "hidden")
    MenuVo toMenuDetailVo(MenuDetailBo menuDetailBo);

    /**
     * MenuCreateVo 转 MenuCreateDto
     */
    @Mapping(source = "hidden", target = "isHidden")
    @Mapping(target = "menuSource", expression = "java(mapMenuSource(menuCreateVo.getMenuSource()))")
    @Mapping(target = "menuType", expression = "java(mapMenuType(menuCreateVo.getMenuType()))")
    MenuCreateDto toMenuCreateDto(MenuCreateVo menuCreateVo);

    /**
     * MenuUpdateVo 转 MenuUpdateDto
     */
    @Mapping(source = "hidden", target = "isHidden")
    @Mapping(target = "menuSource", expression = "java(mapMenuSource(menuUpdateVo.getMenuSource()))")
    @Mapping(target = "menuType", expression = "java(mapMenuType(menuUpdateVo.getMenuType()))")
    MenuUpdateDto toMenuUpdateDto(MenuUpdateVo menuUpdateVo);

    /**
     * MenuQueryVo 转 MenuQueryDto
     */
    @Mapping(target = "menuSource", expression = "java(mapMenuSource(menuQueryVo.getMenuSource()))")
    MenuQueryDto toMenuQueryDto(MenuQueryVo menuQueryVo);

    default MenuSourceEnum mapMenuSource(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, MenuSourceEnum.class);
    }

    default MenuTypeEnum mapMenuType(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, MenuTypeEnum.class);
    }

}
