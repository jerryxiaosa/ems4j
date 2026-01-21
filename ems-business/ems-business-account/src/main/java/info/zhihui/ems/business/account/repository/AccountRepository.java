package info.zhihui.ems.business.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.qo.AccountQo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends BaseMapper<AccountEntity> {

    List<AccountEntity> findList(AccountQo accountQo);
}