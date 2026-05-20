package info.zhihui.ems.web.mini.biz;

import info.zhihui.ems.business.mobile.bo.MiniLoginBo;
import info.zhihui.ems.business.mobile.bo.MiniLoginResultBo;
import info.zhihui.ems.business.mobile.service.MiniService;
import info.zhihui.ems.web.mini.mapstruct.MiniWebMapper;
import info.zhihui.ems.web.mini.vo.MiniLoginRequestVo;
import info.zhihui.ems.web.mini.vo.MiniLoginResponseVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 小程序认证 Web 编排。
 */
@Service
@RequiredArgsConstructor
public class MiniAuthBiz {

    private final MiniService miniService;
    private final MiniWebMapper miniWebMapper;

    public MiniLoginResponseVo login(MiniLoginRequestVo requestVo) {
        MiniLoginBo loginBo = miniWebMapper.toLoginBo(requestVo);
        MiniLoginResultBo resultBo = miniService.login(loginBo);
        return miniWebMapper.toLoginResponseVo(resultBo);
    }

    public void logout() {
        miniService.logout();
    }
}
