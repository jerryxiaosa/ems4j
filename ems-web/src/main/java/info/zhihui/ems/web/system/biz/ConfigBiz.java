package info.zhihui.ems.web.system.biz;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.dto.ConfigQueryDto;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import info.zhihui.ems.web.system.mapstruct.ConfigWebMapper;
import info.zhihui.ems.web.system.vo.ConfigQueryVo;
import info.zhihui.ems.web.system.vo.ConfigUpdateVo;
import info.zhihui.ems.web.system.vo.ConfigVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统配置业务编排
 */
@Service
@RequiredArgsConstructor
public class ConfigBiz {

    private final ConfigService configService;
    private final ConfigWebMapper configWebMapper;

    /**
     * 分页查询系统配置
     */
    public PageResult<ConfigVo> findConfigPage(ConfigQueryVo queryVo, Integer pageNum, Integer pageSize) {
        ConfigQueryDto queryDto = configWebMapper.toConfigQueryDto(queryVo);
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        PageResult<ConfigBo> pageResult = configService.findConfigPage(queryDto, pageParam);
        return configWebMapper.toConfigVoPage(pageResult);
    }

    /**
     * 查询配置列表
     */
    public List<ConfigVo> findConfigList(ConfigQueryVo queryVo) {
        ConfigQueryDto queryDto = configWebMapper.toConfigQueryDto(queryVo);
        PageResult<ConfigBo> pageResult = configService.findConfigPage(queryDto, new PageParam().setPageNum(1).setPageSize(Integer.MAX_VALUE));
        List<ConfigBo> bos = pageResult == null ? Collections.emptyList() : pageResult.getList();
        if (ObjectUtils.isEmpty(bos)) {
            return Collections.emptyList();
        }
        return configWebMapper.toConfigVoList(bos);
    }

    /**
     * 获取配置详情
     */
    public ConfigVo getConfig(String key) {
        ConfigBo bo = configService.getByKey(key);
        return configWebMapper.toConfigVo(bo);
    }

    /**
     * 更新配置
     */
    public void updateConfig(ConfigUpdateVo updateVo) {
        ConfigUpdateDto updateDto = configWebMapper.toConfigUpdateDto(updateVo);
        configService.update(updateDto);
    }
}
