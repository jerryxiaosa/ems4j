package info.zhihui.ems.business.report.service.build;

import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 日报统一构建服务。
 */
public interface DailyReportBuildService {

    /**
     * 根据请求参数构建日报。
     *
     * @param buildRequest 构建请求
     */
    void buildDailyReport(@NotNull @Valid DailyReportBuildRequestDto buildRequest);
}
