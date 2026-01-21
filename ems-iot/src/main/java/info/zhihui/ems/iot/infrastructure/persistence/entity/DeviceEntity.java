package info.zhihui.ems.iot.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("iot_device")
public class DeviceEntity {

    private Integer id;
    private String deviceNo;
    private Integer portNo;
    private Integer meterAddress;
    private String deviceSecret;
    private Integer slaveAddress;
    private String productCode;
    private Integer parentId;
    private LocalDateTime lastOnlineAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Boolean isDeleted;
}
