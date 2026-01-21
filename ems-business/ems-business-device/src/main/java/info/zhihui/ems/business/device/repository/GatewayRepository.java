package info.zhihui.ems.business.device.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.device.entity.GatewayEntity;
import info.zhihui.ems.business.device.qo.GatewayQo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatewayRepository extends BaseMapper<GatewayEntity> {

    List<GatewayEntity> findList(GatewayQo query);

    List<String> getCommunicationOption();

}