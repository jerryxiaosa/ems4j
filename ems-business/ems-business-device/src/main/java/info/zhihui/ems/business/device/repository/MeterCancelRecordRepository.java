package info.zhihui.ems.business.device.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeterCancelRecordRepository extends BaseMapper<MeterCancelRecordEntity> {

    /**
     * 根据销户编号查询销表记录列表
     *
     * @param cancelNo 销户编号
     * @return 销表记录列表
     */
    List<MeterCancelRecordEntity> selectByCancelNo(String cancelNo);

    /**
     * 查询指定电表的最新销户记录（按读表时间排序）
     *
     * @param meterId 电表ID
     * @return 最近的销户记录
     */
    MeterCancelRecordEntity selectLatestByMeter(@Param("meterId") Integer meterId);

}
