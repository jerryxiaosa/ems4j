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

    UserThirdPartyBindEntity selectByUserPlatformAndAppId(@Param("platform") String platform,
                                                          @Param("userId") Integer userId,
                                                          @Param("appId") String appId);

    int updateByUserPlatform(@Param("entity") UserThirdPartyBindEntity entity);
}
