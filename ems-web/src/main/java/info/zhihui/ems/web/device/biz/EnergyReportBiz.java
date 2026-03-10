package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.mq.api.constant.device.DeviceMqConstant;
import info.zhihui.ems.mq.api.message.device.StandardEnergyReportMessage;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.service.MqService;
import info.zhihui.ems.web.device.vo.StandardEnergyReportSaveVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 电量上报业务编排
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnergyReportBiz {

    private final MqService mqService;

    /**
     * 保存标准电量上报
     *
     * @param saveVo 上报参数
     */
    public void addStandardReport(StandardEnergyReportSaveVo saveVo) {
        log.info("收到上报请求，来源：{}，流水号: {}，详细信息：{}", saveVo.getSource(), saveVo.getSourceReportId(), saveVo);
        mqService.sendMessage(new MqMessage()
                .setMessageDestination(DeviceMqConstant.DEVICE_DESTINATION)
                .setRoutingIdentifier(DeviceMqConstant.ROUTING_KEY_STANDARD_ENERGY_REPORT)
                .setPayload(new StandardEnergyReportMessage()
                        .setSource(saveVo.getSource())
                        .setSourceReportId(saveVo.getSourceReportId())
                        .setDeviceNo(saveVo.getDeviceNo())
                        .setRecordTime(saveVo.getRecordTime())
                        .setTotalEnergy(saveVo.getTotalEnergy())
                        .setHigherEnergy(saveVo.getHigherEnergy())
                        .setHighEnergy(saveVo.getHighEnergy())
                        .setLowEnergy(saveVo.getLowEnergy())
                        .setLowerEnergy(saveVo.getLowerEnergy())
                        .setDeepLowEnergy(saveVo.getDeepLowEnergy())));
    }
}
