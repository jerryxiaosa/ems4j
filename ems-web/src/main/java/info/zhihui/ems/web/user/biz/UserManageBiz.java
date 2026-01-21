package info.zhihui.ems.web.user.biz;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserUpdateDto;
import info.zhihui.ems.foundation.user.dto.UserUpdatePasswordDto;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.web.user.mapstruct.UserManageWebMapper;
import info.zhihui.ems.web.user.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理业务编排。
 */
@Service
@RequiredArgsConstructor
public class UserManageBiz {

    private final UserService userService;
    private final UserManageWebMapper userManageWebMapper;

    /**
     * 分页查询用户。
     *
     * @param queryVo  查询条件
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页数据
     */
    public PageResult<UserVo> findUserPage(UserQueryVo queryVo, Integer pageNum, Integer pageSize) {
        UserQueryDto queryDto = userManageWebMapper.toUserQueryDto(queryVo);
        PageParam pageParam = new PageParam()
                .setPageNum(pageNum)
                .setPageSize(pageSize);
        PageResult<UserBo> pageResult = userService.findUserPage(queryDto, pageParam);
        List<UserVo> userVos = userManageWebMapper.toUserVoList(pageResult.getList());
        return new PageResult<UserVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(userVos);
    }

    /**
     * 查询用户列表。
     *
     * @param queryVo 查询条件
     * @return 用户列表
     */
    public List<UserVo> findUserList(UserQueryVo queryVo) {
        UserQueryDto queryDto = userManageWebMapper.toUserQueryDto(queryVo);
        List<UserBo> users = userService.findUserList(queryDto);
        return userManageWebMapper.toUserVoList(users);
    }

    /**
     * 根据 ID 查询用户。
     *
     * @param id 用户ID
     * @return 用户详情
     */
    public UserVo getUser(Integer id) {
        UserBo userBo = userService.getUserInfo(id);
        return userManageWebMapper.toUserVo(userBo);
    }

    /**
     * 新增用户。
     *
     * @param createVo 创建参数
     * @return 新增用户ID
     */
    public Integer createUser(UserCreateVo createVo) {
        UserCreateDto dto = userManageWebMapper.toUserCreateDto(createVo);
        return userService.add(dto);
    }

    /**
     * 更新用户信息。
     *
     * @param id       用户ID
     * @param updateVo 更新参数
     */
    public void updateUser(Integer id, UserUpdateVo updateVo) {
        UserUpdateDto dto = userManageWebMapper.toUserUpdateDto(updateVo);
        dto.setId(id);
        userService.update(dto);
    }

    /**
     * 删除用户。
     *
     * @param id 用户ID
     */
    public void deleteUser(Integer id) {
        userService.delete(id);
    }

    /**
     * 修改密码。
     *
     * @param id                用户ID
     * @param passwordUpdateVo  密码更新参数
     */
    public void updatePassword(Integer id, UserPasswordUpdateVo passwordUpdateVo) {
        UserUpdatePasswordDto dto = userManageWebMapper.toUserUpdatePasswordDto(passwordUpdateVo);
        dto.setId(id);
        userService.updatePassword(dto);
    }
}
