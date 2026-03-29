package info.zhihui.ems.business.billing.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电表计费记录查询入参DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MeterBillingQueryDto {

    /**
     * 搜索关键词（电表名称/设备编号模糊匹配，可选）
     */
    private String searchKey;

    /**
     * 空间名称模糊匹配（可选）
     */
    private String spaceNameLike;

    /**
     * 消费时间范围开始时间（可选）
     */
    private LocalDateTime beginTime;

    /**
     * 消费时间范围结束时间（可选）
     */
    private LocalDateTime endTime;
}
