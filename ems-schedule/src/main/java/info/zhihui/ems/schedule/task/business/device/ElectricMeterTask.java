package info.zhihui.ems.schedule.task.business.device;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterOnlineStatusDto;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusSyncDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 电表相关定时任务
 *
 * @author jerryxiaosa
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElectricMeterTask {

    private static final int PAGE_SIZE = 200;

    private final ElectricMeterInfoService electricMeterInfoService;
    private final ElectricMeterManagerService electricMeterManagerService;

    /**
     * 每 10 分钟执行一次电表在线状态全量同步
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void syncOnlineStatus() {
        log.info("开始执行电表在线状态全量同步任务");

        int pageNum = 1;
        SyncResult onlineResult = new SyncResult();
        SyncResult switchResult = new SyncResult();
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto();

        while (true) {
            PageParam pageParam = new PageParam()
                    .setPageNum(pageNum)
                    .setPageSize(PAGE_SIZE);
            PageResult<ElectricMeterBo> pageResult = electricMeterInfoService.findPage(queryDto, pageParam);
            List<ElectricMeterBo> meters = pageResult == null ? null : pageResult.getList();

            if (CollectionUtils.isEmpty(meters)) {
                break;
            }

            for (ElectricMeterBo meter : meters) {
                executeSyncOperation("在线状态", meter.getId(), onlineResult,
                        () -> electricMeterManagerService.syncMeterOnlineStatus(
                                new ElectricMeterOnlineStatusDto().setMeterId(meter.getId())
                        ));

                executeSyncOperation("开合闸状态", meter.getId(), switchResult,
                        () -> electricMeterManagerService.syncMeterSwitchStatus(
                                new ElectricMeterSwitchStatusSyncDto().setMeterId(meter.getId())
                        ));
            }

            if (meters.size() < PAGE_SIZE) {
                break;
            }

            pageNum++;
        }

        log.info("电表状态全量同步任务完成：在线状态成功 {} 个、失败 {} 个；开合闸状态成功 {} 个、失败 {} 个",
                onlineResult.success, onlineResult.failed, switchResult.success, switchResult.failed);
    }

    /**
     * 执行同步操作的通用方法
     *
     * @param operationType 操作类型描述
     * @param meterId       电表ID
     * @param result        结果统计对象
     * @param operation     具体的同步操作
     */
    private void executeSyncOperation(String operationType, Integer meterId, SyncResult result, Runnable operation) {
        try {
            operation.run();
            result.success++;
        } catch (Exception ex) {
            result.failed++;
            log.warn("电表{}同步失败，meterId={}，原因：{}", operationType, meterId, ex.getMessage(), ex);
        }
    }

    /**
     * 同步结果统计
     */
    private static class SyncResult {
        long success = 0;
        long failed = 0;
    }
}
