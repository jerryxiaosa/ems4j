package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeSaveDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
class DeviceTypeServiceImplIntegrationTest {

    @Autowired
    private DeviceTypeService deviceTypeService;

    @Test
    @DisplayName("add - 顶级类型应设置pid=0/ancestorId=0/level=1")
    void testAddTopLevelType() {
        String typeKey = "TEMP_TOP_LEVEL_TYPE";

        DeviceTypeSaveDto saveDto = new DeviceTypeSaveDto();
        saveDto.setTypeName("临时顶级类型");
        saveDto.setTypeKey(typeKey);
        saveDto.setPid(null);

        deviceTypeService.add(saveDto);

        DeviceTypeBo bo = deviceTypeService.getByKey(typeKey);
        assertThat(bo.getPid()).isEqualTo(0);
        assertThat(bo.getAncestorId()).isEqualTo("0");
        assertThat(bo.getLevel()).isEqualTo(1);
        assertThat(bo.getTypeName()).isEqualTo("临时顶级类型");
    }

    @Test
    @DisplayName("add/delete - 有子级时删除应抛异常，无子级可删除")
    void testDeleteWithChildrenAndWithoutChildren() {
        String parentKey = "TEMP_PARENT_TYPE";
        String childKey = "TEMP_CHILD_TYPE";

        // 新增父级
        DeviceTypeSaveDto parentDto = new DeviceTypeSaveDto();
        parentDto.setTypeName("父级类型");
        parentDto.setTypeKey(parentKey);
        parentDto.setPid(null);
        deviceTypeService.add(parentDto);
        DeviceTypeBo parent = deviceTypeService.getByKey(parentKey);

        // 新增子级
        DeviceTypeSaveDto childDto = new DeviceTypeSaveDto();
        childDto.setTypeName("子级类型");
        childDto.setTypeKey(childKey);
        childDto.setPid(parent.getId());
        deviceTypeService.add(childDto);

        // 删除父级期望抛异常
        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> deviceTypeService.delete(parent.getId()));
        assertThat(ex.getMessage()).isEqualTo("该品类下有子级，无法删除");

        // 删除子级成功
        DeviceTypeBo child = deviceTypeService.getByKey(childKey);
        deviceTypeService.delete(child.getId());

        // 删除父级成功
        deviceTypeService.delete(parent.getId());

        // 再次查询父级/子级都应不存在
        assertThrows(BusinessRuntimeException.class, () -> deviceTypeService.getByKey(parentKey));
        assertThrows(BusinessRuntimeException.class, () -> deviceTypeService.getByKey(childKey));
    }

    @Test
    @DisplayName("update - 更新类型名称应成功")
    void testUpdateTypeName() {
        String typeKey = "TEMP_UPDATE_TYPE";

        // 新增一个类型
        DeviceTypeSaveDto saveDto = new DeviceTypeSaveDto();
        saveDto.setTypeName("待更新类型");
        saveDto.setTypeKey(typeKey);
        saveDto.setPid(null);
        deviceTypeService.add(saveDto);
        DeviceTypeBo created = deviceTypeService.getByKey(typeKey);

        // 更新名称
        DeviceTypeSaveDto updateDto = new DeviceTypeSaveDto();
        updateDto.setId(created.getId());
        updateDto.setPid(created.getPid());
        updateDto.setTypeKey(typeKey); // 保持唯一键不变
        updateDto.setTypeName("已更新类型");
        deviceTypeService.update(updateDto);

        DeviceTypeBo updated = deviceTypeService.getDetail(created.getId());
        assertThat(updated.getTypeName()).isEqualTo("已更新类型");
    }
}