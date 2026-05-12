package info.zhihui.ems.foundation.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.user.entity.UserThirdPartyBindEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 用户第三方身份绑定仓储。
 */
@Repository
public interface UserThirdPartyBindRepository extends BaseMapper<UserThirdPartyBindEntity> {

    UserThirdPartyBindEntity selectByIdentity(@Param("platform") String platform,
                                              @Param("appId") String appId,
                                              @Param("thirdPartyUserId") String thirdPartyUserId);

    int updateByIdentity(@Param("entity") UserThirdPartyBindEntity entity);

    int updateByUserPlatform(@Param("entity") UserThirdPartyBindEntity entity);
}
