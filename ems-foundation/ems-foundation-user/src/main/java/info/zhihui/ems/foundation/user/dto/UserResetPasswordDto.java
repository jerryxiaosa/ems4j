package info.zhihui.ems.foundation.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户重置密码参数
 */
@Data
@Accessors(chain = true)
public class UserResetPasswordDto {
    /**
     * 用户ID
     */
    @NotNull
    private Integer id;

    /**
     * 新密码（6~20）
     */
    @NotBlank
    @Size(min = 6, max = 20)
    private String newPassword;
}
