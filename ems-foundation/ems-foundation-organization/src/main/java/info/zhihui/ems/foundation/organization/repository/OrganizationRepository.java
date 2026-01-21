package info.zhihui.ems.foundation.organization.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.organization.entity.OrganizationEntity;
import info.zhihui.ems.foundation.organization.qo.OrganizationQueryQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends BaseMapper<OrganizationEntity> {

    List<OrganizationEntity> selectByQo(@Param("qo") OrganizationQueryQo qo);
}