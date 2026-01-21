package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
class DeviceModelServiceImplIntegrationTest {

    @Autowired
    private DeviceModelService deviceModelService;

    @Test
    @DisplayName("findList - 按设备类型查询型号列表")
    void testFindList_ByTypeKey() {
        DeviceModelQueryDto queryDto = new DeviceModelQueryDto().setTypeKey("electricMeter");

        List<DeviceModelBo> list = deviceModelService.findList(queryDto);

        assertThat(list).isNotEmpty();
        assertThat(list).allMatch(item -> "electricMeter".equals(item.getTypeKey()));
        assertThat(list).extracting(DeviceModelBo::getManufacturerName)
                .contains("华立科技", "长沙威胜", "宁波三星");
        assertThat(list).extracting(DeviceModelBo::getProductCode).allSatisfy(productCode -> assertThat(productCode).isNotBlank());
    }

    @Test
    @DisplayName("findPage - 电表型号分页查询")
    void testFindPage_ElectricMeter() {
        DeviceModelQueryDto queryDto = new DeviceModelQueryDto().setTypeKey("electricMeter");
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(2);

        PageResult<DeviceModelBo> page = deviceModelService.findPage(queryDto, pageParam);

        assertThat(page).isNotNull();
        assertThat(page.getList()).hasSize(2);
        assertThat(page.getList()).allMatch(item -> "electricMeter".equals(item.getTypeKey()));
        assertThat(page.getList()).extracting(DeviceModelBo::getProductCode).allSatisfy(productCode -> assertThat(productCode).isNotBlank());
    }

    @Test
    @DisplayName("getDetail - 读取型号详情并校验属性映射")
    void testGetDetail_FirstElectricModel() {
        DeviceModelBo detail = deviceModelService.getDetail(1);

        assertThat(detail).isNotNull();
        assertThat(detail.getTypeKey()).isEqualTo("electricMeter");
        assertThat(detail.getManufacturerName()).isEqualTo("华立科技");
        assertThat(detail.getModelName()).isEqualTo("DDS102");
        assertThat(detail.getProductCode()).isEqualTo("DDS102");

        Map<String, Object> props = detail.getModelProperty();
        assertThat(props).isNotNull();
        assertThat(props.get("isCt")).isEqualTo(Boolean.TRUE);
        assertThat(props.get("phases")).isEqualTo("single");
    }
}
