package info.zhihui.ems.business.lease;

import info.zhihui.ems.business.lease.qo.OwnerSpaceRelationQueryQo;
import info.zhihui.ems.business.lease.repository.OwnerSpaceRelationRepository;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OwnerSpaceRelationRepository 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OwnerSpaceRelationRepositoryIntegrationTest {

    @Autowired
    private OwnerSpaceRelationRepository ownerSpaceRelationRepository;

    @Test
    @DisplayName("按主体和空间查询应同时命中两个条件")
    void testFindListByOwnerAndSpaceIds_WithOwnerAndSpace_ShouldFilterByOwner() {
        OwnerSpaceRelationQueryQo queryQo = new OwnerSpaceRelationQueryQo()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerId(1001)
                .setSpaceIds(List.of(101, 103));

        assertThat(ownerSpaceRelationRepository.findListByOwnerAndSpaceIds(queryQo))
                .extracting("spaceId")
                .containsExactly(101);
    }

    @Test
    @DisplayName("仅按空间查询应返回所有主体的匹配记录")
    void testFindListByOwnerAndSpaceIds_WithSpaceOnly_ShouldReturnAllOwners() {
        OwnerSpaceRelationQueryQo queryQo = new OwnerSpaceRelationQueryQo()
                .setSpaceIds(List.of(101, 103));

        assertThat(ownerSpaceRelationRepository.findListByOwnerAndSpaceIds(queryQo))
                .extracting("spaceId")
                .containsExactlyInAnyOrder(101, 103);
    }

}
