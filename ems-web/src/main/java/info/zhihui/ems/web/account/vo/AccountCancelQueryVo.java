package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 销户记录查询 VO
 */
@Data
@Schema(name = "AccountCancelQueryVo", description = "账户销户记录查询条件")
public class AccountCancelQueryVo {

    @Schema(description = "归属名称模糊查询")
    private String ownerName;

    @Schema(description = "结算类型编码，参考 cleanBalanceType")
    private Integer cleanBalanceType;
}
