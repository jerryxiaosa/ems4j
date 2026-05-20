package info.zhihui.ems.web.mini.biz;

import info.zhihui.ems.business.mobile.bo.MiniCurrentUserBo;
import info.zhihui.ems.business.mobile.service.MiniService;
import info.zhihui.ems.web.mini.mapstruct.MiniWebMapper;
import info.zhihui.ems.web.mini.vo.MiniCurrentUserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 小程序当前用户 Web 编排。
 */
@Service
@RequiredArgsConstructor
public class MiniMeBiz {

    private final MiniService miniService;
    private final MiniWebMapper miniWebMapper;

    public MiniCurrentUserVo getCurrentUser() {
        MiniCurrentUserBo currentUserBo = miniService.getCurrentUser();
        return miniWebMapper.toCurrentUserVo(currentUserBo);
    }
}
