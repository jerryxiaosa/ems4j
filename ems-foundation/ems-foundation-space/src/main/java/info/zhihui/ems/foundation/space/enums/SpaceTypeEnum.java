package info.zhihui.ems.foundation.space.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

@Getter
public enum SpaceTypeEnum implements CodeEnum<Integer> {
    // 园区
    MAIN(1, "主区域"),

    // 内部空间：比如楼栋，层
    INNER_SPACE(2, "内部区域"),

    // 房间
    ROOM(3, "房间"),

    // 自定义
    CUSTOM(4, "自定义区域");

    private final Integer code;
    private final String info;

    SpaceTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

    /**
     * 根据code获取枚举实例
     */
    public static SpaceTypeEnum ofCode(Integer code) {
        return CodeEnum.fromCode(code, SpaceTypeEnum.class);
    }
}
