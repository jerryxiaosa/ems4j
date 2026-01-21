package info.zhihui.ems.iot.application;

import info.zhihui.ems.iot.domain.event.DeviceEnergyReportEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EnergyReportAppService {

    public void handleEnergyReport(DeviceEnergyReportEvent event) {
        if (event == null) {
            return;
        }
        log.info("处理能耗上报: {}", event);
    }
}
