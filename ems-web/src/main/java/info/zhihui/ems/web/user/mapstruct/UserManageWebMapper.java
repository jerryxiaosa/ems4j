package info.zhihui.ems.web.user.mapstruct;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserResetPasswordDto;
import info.zhihui.ems.foundation.user.dto.UserUpdateDto;
import info.zhihui.ems.foundation.user.dto.UserUpdatePasswordDto;
import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import info.zhihui.ems.web.user.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserManageWebMapper {

    UserQueryDto toUserQueryDto(UserQueryVo vo);

    @Mapping(target = "userGender", expression = "java(mapUserGender(vo.getUserGender()))")
    @Mapping(target = "certificatesType", expression = "java(mapCertificatesType(vo.getCertificatesType()))")
    UserCreateDto toUserCreateDto(UserCreateVo vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userGender", expression = "java(mapUserGender(vo.getUserGender()))")
    @Mapping(target = "certificatesType", expression = "java(mapCertificatesType(vo.getCertificatesType()))")
    UserUpdateDto toUserUpdateDto(UserUpdateVo vo);

    UserUpdatePasswordDto toUserUpdatePasswordDto(UserPasswordUpdateVo vo);

    UserResetPasswordDto toUserResetPasswordDto(UserPasswordResetVo vo);

    @Mapping(target = "userGender", expression = "java(mapUserGenderCode(bo.getUserGender()))")
    @Mapping(target = "certificatesType", expression = "java(mapCertificatesTypeCode(bo.getCertificatesType()))")
    UserVo toUserVo(UserBo bo);

    List<UserVo> toUserVoList(List<UserBo> bos);

    UserRoleVo toUserRoleVo(RoleSimpleBo bo);

    List<UserRoleVo> toUserRoleVoList(List<RoleSimpleBo> bos);

    default UserGenderEnum mapUserGender(Integer value) {
        if (value == null) {
            return null;
        }
        return CodeEnum.fromCode(value, UserGenderEnum.class);
    }

    default CertificatesTypeEnum mapCertificatesType(Integer value) {
        if (value == null) {
            return null;
        }
        return CodeEnum.fromCode(value, CertificatesTypeEnum.class);
    }

    default Integer mapUserGenderCode(UserGenderEnum value) {
        return value == null ? null : value.getCode();
    }

    default Integer mapCertificatesTypeCode(CertificatesTypeEnum value) {
        return value == null ? null : value.getCode();
    }
}
