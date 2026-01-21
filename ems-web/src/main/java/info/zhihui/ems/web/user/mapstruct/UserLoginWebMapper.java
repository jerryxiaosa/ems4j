package info.zhihui.ems.web.user.mapstruct;

import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.CaptchaDto;
import info.zhihui.ems.foundation.user.dto.LoginRequestDto;
import info.zhihui.ems.foundation.user.dto.LoginResponseDto;
import info.zhihui.ems.web.user.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserLoginWebMapper {

    LoginRequestDto toLoginRequestDto(LoginRequestVo vo);

    LoginResponseVo toLoginResponseVo(LoginResponseDto dto);

    CaptchaVo toCaptchaVo(CaptchaDto dto);

    UserVo toUserVo(UserBo userBo);

    List<UserMenuVo> toMenuVoList(List<MenuBo> menuBoList);
}
