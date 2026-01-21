package info.zhihui.ems.foundation.user.mapper;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserUpdateDto;
import info.zhihui.ems.foundation.user.entity.RoleEntity;
import info.zhihui.ems.foundation.user.entity.UserEntity;
import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import info.zhihui.ems.foundation.user.qo.UserQueryQo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "userGender", source = "userGender", qualifiedByName = "genderCodeToEnum")
    @Mapping(target = "certificatesType", source = "certificatesType", qualifiedByName = "certTypeCodeToEnum")
    UserBo entityToBo(UserEntity entity);

    List<UserBo> listEntityToBo(List<UserEntity> list);

    PageResult<UserBo> pageEntityToPageBo(PageInfo<UserEntity> pageInfo);

    @Mapping(target = "userGender", source = "userGender", qualifiedByName = "genderEnumToCode")
    @Mapping(target = "certificatesType", source = "certificatesType", qualifiedByName = "certTypeEnumToCode")
    @Mapping(target = "password", ignore = true)
    UserEntity createDtoToEntity(UserCreateDto dto);

    @Mapping(target = "userGender", source = "userGender", qualifiedByName = "genderEnumToCode")
    @Mapping(target = "certificatesType", source = "certificatesType", qualifiedByName = "certTypeEnumToCode")
    UserEntity updateDtoToEntity(UserUpdateDto dto);

    UserQueryQo queryDtoToQo(UserQueryDto dto);

    @Named("genderEnumToCode")
    default Integer genderEnumToCode(UserGenderEnum gender) {
        return gender == null ? null : gender.getCode();
    }

    @Named("genderCodeToEnum")
    default UserGenderEnum genderCodeToEnum(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, UserGenderEnum.class);
    }

    @Named("certTypeEnumToCode")
    default Integer certTypeEnumToCode(CertificatesTypeEnum type) {
        return type == null ? null : type.getCode();
    }

    @Named("certTypeCodeToEnum")
    default CertificatesTypeEnum certTypeCodeToEnum(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, CertificatesTypeEnum.class);
    }


    /**
     * 将角色实体列表转换为简单角色业务对象列表
     */
    List<RoleSimpleBo> listRoleEntityToSimpleBo(List<RoleEntity> entities);

    /**
     * 将角色业务对象列表转换为简单角色业务对象列表
     */
    List<RoleSimpleBo> listRoleBoToSimpleBo(List<RoleBo> roles);
}