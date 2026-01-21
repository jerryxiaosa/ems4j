package info.zhihui.ems.business.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.account.entity.AccountCancelRecordEntity;
import info.zhihui.ems.business.account.qo.AccountCancelRecordQo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 账号销户记录仓库接口
 *
 * @author jerryxiaosa
 */
@Repository
public interface AccountCancelRecordRepository extends BaseMapper<AccountCancelRecordEntity> {

    /**
     * 根据条件查询销户记录列表
     *
     * @param qo 查询条件
     * @return 销户记录列表
     */
    List<AccountCancelRecordEntity> selectListByCondition(AccountCancelRecordQo qo);

    /**
     * 根据销户编号查询销户记录
     *
     * @param cancelNo 销户编号
     * @return 销户记录
     */
    AccountCancelRecordEntity selectByCancelNo(String cancelNo);
}