package info.zhihui.ems.web.mini.mapstruct;

import info.zhihui.ems.business.mini.auth.bo.MiniLoginBo;
import info.zhihui.ems.business.mini.auth.bo.MiniLoginResultBo;
import info.zhihui.ems.business.mini.me.bo.MiniCurrentUserBo;
import info.zhihui.ems.web.mini.vo.MiniCurrentUserVo;
import info.zhihui.ems.web.mini.vo.MiniLoginRequestVo;
import info.zhihui.ems.web.mini.vo.MiniLoginResponseVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 小程序 Web 层映射器。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MiniWebMapper {

    MiniLoginBo toLoginBo(MiniLoginRequestVo vo);

    MiniLoginResponseVo toLoginResponseVo(MiniLoginResultBo bo);

    @Mapping(target = "electricAccountType", expression = "java(bo.getElectricAccountType() == null ? null : bo.getElectricAccountType().getCode())")
    MiniCurrentUserVo toCurrentUserVo(MiniCurrentUserBo bo);
}
