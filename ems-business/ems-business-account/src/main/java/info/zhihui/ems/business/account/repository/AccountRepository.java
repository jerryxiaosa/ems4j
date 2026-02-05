package info.zhihui.ems.business.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.qo.AccountQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountRepository extends BaseMapper<AccountEntity> {

    List<AccountEntity> findList(AccountQo accountQo);

    int softDelete(@Param("id") Integer id,
                   @Param("deleteTime") LocalDateTime deleteTime,
                   @Param("updateUser") Integer updateUser,
                   @Param("updateUserName") String updateUserName,
                   @Param("updateTime") LocalDateTime updateTime);
}
