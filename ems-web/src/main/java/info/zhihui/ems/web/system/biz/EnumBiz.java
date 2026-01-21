package info.zhihui.ems.web.system.biz;

import info.zhihui.ems.foundation.system.dto.EnumItemDto;
import info.zhihui.ems.foundation.system.handler.enums.EnumScanHandler;
import info.zhihui.ems.web.system.mapstruct.EnumItemMapper;
import info.zhihui.ems.web.system.vo.EnumItemVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 扫描所有实现了 CodeEnum 的枚举类型，并输出枚举名 -> 枚举项列表（值、描述）。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EnumBiz {
    private final EnumScanHandler enumScanHandler;
    private final EnumItemMapper enumItemMapper;

    /**
     * 获取全部枚举数据（内部缓存已在 EnumScanHandler 中处理）。
     */
    public Map<String, List<EnumItemVo>> getAll() {
        Map<String, List<EnumItemDto>> dtoMap = enumScanHandler.getAll();
        return enumItemMapper.mapDtoToVo(dtoMap);
    }
}