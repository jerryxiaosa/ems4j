package info.zhihui.ems.common.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperatorInfo {
    private Integer createUser;
    private String createUserName;
    private LocalDateTime createTime;

    private Integer updateUser;
    private String updateUserName;
    private LocalDateTime updateTime;
}
