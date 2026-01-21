package info.zhihui.ems.foundation.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserUpdatePasswordDto {
    /**
     * 用户ID
     */
    @NotNull
    private Integer id;

    /**
     * 旧密码（6~32）
     */
    @NotBlank
    @Size(min = 6, max = 20)
    private String oldPassword;

    /**
     * 新密码（6~32）
     */
    @NotBlank
    @Size(min = 6, max = 20)
    private String newPassword;
}