package info.zhihui.ems.foundation.system;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.dto.ConfigQueryDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
class ConfigServiceImplIntegrationTest {

    @Autowired
    private ConfigService configService;

    @Test
    @DisplayName("findConfigPage - 按模块分页查询")
    void testFindConfigPage_ByModule() {
        ConfigQueryDto queryDto = new ConfigQueryDto().setConfigModuleName("electric");
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(5);

        PageResult<ConfigBo> result = configService.findConfigPage(queryDto, pageParam);

        assertThat(result).isNotNull();
        assertThat(result.getList()).isNotEmpty();
        assertThat(result.getList())
                .allMatch(item -> "electric".equals(item.getConfigModuleName()));
    }
}
