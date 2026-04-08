package info.zhihui.ems.business.account.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 账户开户快照记录
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_account_open_record")
public class AccountOpenRecordEntity extends BaseEntity {

    private Integer id;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 主体ID
     */
    private Integer ownerId;

    /**
     * 主体类型
     */
    private Integer ownerType;

    /**
     * 主体名称
     */
    private String ownerName;

    /**
     * 电费账户类型
     */
    private Integer electricAccountType;

    /**
     * 开户时间
     */
    private LocalDateTime openTime;
}
