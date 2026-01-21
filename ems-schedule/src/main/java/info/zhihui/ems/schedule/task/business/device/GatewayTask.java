package info.zhihui.ems.schedule.task.business.device;

import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.GatewayOnlineStatusDto;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.service.GatewayService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 网关定时任务
 *
 * @author jerryxiaosa
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayTask {

    private static final int PAGE_SIZE = 200;

    private final GatewayService gatewayService;

    /**
     * 每 10 分钟执行一次网关在线状态全量同步
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void syncOnlineStatus() {
        log.info("开始执行网关在线状态全量同步任务");

        int pageNum = 1;
        long successCount = 0;
        long failedCount = 0;
        GatewayQueryDto queryDto = new GatewayQueryDto();

        while (true) {
            PageParam pageParam = new PageParam()
                    .setPageNum(pageNum)
                    .setPageSize(PAGE_SIZE);
            PageResult<GatewayBo> pageResult = gatewayService.findPage(queryDto, pageParam);
            List<GatewayBo> gatewayList = pageResult == null ? null : pageResult.getList();

            if (CollectionUtils.isEmpty(gatewayList)) {
                break;
            }

            for (GatewayBo gateway : gatewayList) {
                try {
                    gatewayService.syncGatewayOnlineStatus(new GatewayOnlineStatusDto().setGatewayId(gateway.getId()));
                    successCount++;
                } catch (Exception ex) {
                    failedCount++;
                    log.warn("网关在线状态同步失败，gatewayId={}，原因：{}", gateway.getId(), ex.getMessage(), ex);
                }
            }

            if (gatewayList.size() < PAGE_SIZE) {
                break;
            }

            pageNum++;
        }

        log.info("网关在线状态全量同步任务完成，成功同步 {} 个网关，失败 {} 个网关", successCount, failedCount);
    }
}
