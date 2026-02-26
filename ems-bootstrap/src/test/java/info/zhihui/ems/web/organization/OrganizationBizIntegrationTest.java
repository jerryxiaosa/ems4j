package info.zhihui.ems.web.organization;

import info.zhihui.ems.foundation.organization.entity.OrganizationEntity;
import info.zhihui.ems.foundation.organization.enums.OrganizationTypeEnum;
import info.zhihui.ems.foundation.organization.repository.OrganizationRepository;
import info.zhihui.ems.web.organization.biz.OrganizationBiz;
import info.zhihui.ems.web.organization.vo.OrganizationOptionQueryVo;
import info.zhihui.ems.web.organization.vo.OrganizationOptionVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OrganizationBizIntegrationTest {

    @Autowired
    private OrganizationBiz organizationBiz;

    @Autowired
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        organizationRepository.delete(null);
    }

    @Test
    @DisplayName("查询组织下拉列表-默认返回20条")
    void testFindOrganizationOptionList_DefaultLimit_ShouldReturn20() {
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        for (int i = 1; i <= 25; i++) {
            insertOrganization("默认组织" + i, "负责人" + i, "1380000" + String.format("%04d", i), baseTime.plusSeconds(i));
        }

        List<OrganizationOptionVo> result = organizationBiz.findOrganizationOptionList(new OrganizationOptionQueryVo());

        assertNotNull(result);
        assertEquals(20, result.size());
        assertEquals("默认组织25", result.get(0).getOrganizationName());
        assertEquals("默认组织6", result.get(19).getOrganizationName());
    }

    @Test
    @DisplayName("查询组织下拉列表-按名称过滤并限制条数")
    void testFindOrganizationOptionList_FilterAndLimit_ShouldReturnMappedFields() {
        LocalDateTime baseTime = LocalDateTime.of(2025, 2, 1, 9, 0, 0);
        insertOrganization("Alpha能源", "张三", "13800000001", baseTime.plusSeconds(1));
        insertOrganization("Alpha科技", "李四", "13800000002", baseTime.plusSeconds(2));
        insertOrganization("Alpha园区", "王五", "13800000003", baseTime.plusSeconds(3));
        insertOrganization("Beta集团", "赵六", "13800000004", baseTime.plusSeconds(4));

        OrganizationOptionQueryVo queryVo = new OrganizationOptionQueryVo()
                .setOrganizationNameLike("Alpha")
                .setLimit(2);

        List<OrganizationOptionVo> result = organizationBiz.findOrganizationOptionList(queryVo);

        assertNotNull(result);
        assertEquals(2, result.size());

        OrganizationOptionVo first = result.get(0);
        assertNotNull(first.getId());
        assertEquals("Alpha园区", first.getOrganizationName());
        assertEquals(OrganizationTypeEnum.ENTERPRISE.getCode(), first.getOrganizationType());
        assertEquals("王五", first.getManagerName());
        assertEquals("13800000003", first.getManagerPhone());

        OrganizationOptionVo second = result.get(1);
        assertEquals("Alpha科技", second.getOrganizationName());
    }

    private void insertOrganization(String name, String managerName, String managerPhone, LocalDateTime createTime) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setOrganizationName(name);
        entity.setOrganizationType(OrganizationTypeEnum.ENTERPRISE.getCode());
        entity.setOrganizationAddress("测试地址");
        entity.setManagerName(managerName);
        entity.setManagerPhone(managerPhone);
        entity.setCreditCode("CC-" + name);
        entity.setOwnAreaId(1);
        entity.setCreateTime(createTime);
        entity.setUpdateTime(createTime);
        entity.setIsDeleted(false);
        organizationRepository.insert(entity);
    }
}
