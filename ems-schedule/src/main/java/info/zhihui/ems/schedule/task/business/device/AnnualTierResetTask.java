package info.zhihui.ems.schedule.task.business.device;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.MeterStepResetDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 跨年阶梯归零任务，按年度重建阶梯起点并清除历史偏移
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnnualTierResetTask {

    private static final int PAGE_SIZE = 200;
    private static final String RESET_CRON = "0 0 0 1 1 ?"; // 每年1月1日0点执行

    private final ElectricMeterInfoService electricMeterInfoService;
    private final ElectricMeterManagerService electricMeterManagerService;

    @Scheduled(cron = RESET_CRON)
    public void executeAnnualReset() {
        int currentYear = LocalDateTime.now().getYear();
        log.info("开始执行跨年阶梯重建任务，当前年度：{}", currentYear);

        int pageNum = 1;
        int failed = 0;
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto()
                .setIsPrepay(true);

        while (true) {
            PageParam pageParam = new PageParam().setPageNum(pageNum).setPageSize(PAGE_SIZE);
            PageResult<ElectricMeterBo> pageResult = electricMeterInfoService.findPage(queryDto, pageParam);
            List<ElectricMeterBo> meterList = pageResult == null ? Collections.emptyList() : pageResult.getList();
            if (CollectionUtils.isEmpty(meterList)) {
                break;
            }

            for (ElectricMeterBo meter : meterList) {
                if (meter.getAccountId() == null) {
                    continue;
                }

                try {
                     electricMeterManagerService
                            .resetCurrentYearMeterStepRecord(new MeterStepResetDto()
                                    .setMeterId(meter.getId()));
                } catch (Exception ex) {
                    failed++;
                    log.error("电表{}跨年阶梯重建失败", meter.getMeterNo(), ex);
                }
            }

            if (meterList.size() < PAGE_SIZE) {
                break;
            }
            pageNum++;
        }

        log.info("跨年阶梯重建任务完成：失败{}个", failed);
    }

}
