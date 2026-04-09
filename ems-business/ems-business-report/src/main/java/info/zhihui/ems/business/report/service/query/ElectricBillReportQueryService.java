package info.zhihui.ems.business.report.service.query;

import info.zhihui.ems.business.report.bo.ElectricBillReportDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportPageItemBo;
import info.zhihui.ems.business.report.dto.ElectricBillReportQueryDto;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.constraints.NotNull;

/**
 * 电费报表查询接口。
 */
public interface ElectricBillReportQueryService {

    /**
     * 分页查询电费报表列表。
     *
     * @param query 查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<ElectricBillReportPageItemBo> findPage(@NotNull ElectricBillReportQueryDto query,
                                                      @NotNull PageParam pageParam);

    /**
     * 查询单个账户的电费报表详情。
     *
     * @param accountId 账户ID
     * @param query 查询条件
     * @return 详情
     */
    ElectricBillReportDetailBo getDetail(@NotNull Integer accountId, @NotNull ElectricBillReportQueryDto query);
}
