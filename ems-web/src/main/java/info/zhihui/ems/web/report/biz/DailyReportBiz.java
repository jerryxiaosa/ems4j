package info.zhihui.ems.web.report.biz;

import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import info.zhihui.ems.business.report.service.build.DailyReportBuildService;
import info.zhihui.ems.web.report.vo.DailyReportBuildVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 日报构建业务编排。
 */
@Service
@RequiredArgsConstructor
public class DailyReportBiz {

    private final DailyReportBuildService dailyReportBuildService;

    /**
     * 手工触发日报构建。
     *
     * @param buildVo 构建参数
     */
    public void buildDailyReport(DailyReportBuildVo buildVo) {
        dailyReportBuildService.buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(buildVo.getStartDate())
                .setEndDate(buildVo.getEndDate())
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode())
                .setTriggerBy("web"));
    }
}
