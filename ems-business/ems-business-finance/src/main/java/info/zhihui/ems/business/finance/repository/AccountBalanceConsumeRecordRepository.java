package info.zhihui.ems.business.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.finance.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.qo.AccountConsumeRecordQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 能耗包月消费记录 Repository
 *
 * @author jerryxiaosa
 */
@Repository
public interface AccountBalanceConsumeRecordRepository extends BaseMapper<AccountBalanceConsumeRecordEntity> {

    /**
     * 根据查询条件查询包月消费记录
     *
     * @param qo 查询条件
     * @return 包月消费记录列表
     */
    List<AccountBalanceConsumeRecordEntity> selectByQo(AccountConsumeRecordQo qo);

    /**
     * 统计账户在指定月份的包月消费次数
     *
     * @param accountId 账户ID
     * @param monthStart 月初时间（含）
     * @param monthEnd 下个月月初时间（不含）
     * @return 消费次数
     */
    Integer countMonthlyConsume(@Param("accountId") Integer accountId,
                                @Param("monthStart") LocalDateTime monthStart,
                                @Param("monthEnd") LocalDateTime monthEnd);
}
