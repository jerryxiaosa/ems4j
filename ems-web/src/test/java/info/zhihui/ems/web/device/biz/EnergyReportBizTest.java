package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.mq.api.constant.device.DeviceMqConstant;
import info.zhihui.ems.mq.api.message.device.StandardEnergyReportMessage;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.service.MqService;
import info.zhihui.ems.web.device.vo.StandardEnergyReportSaveVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnergyReportBizTest {

    @Mock
    private MqService mqService;

    @InjectMocks
    private EnergyReportBiz energyReportBiz;

    @Test
    @DisplayName("标准电量上报应发送MQ消息")
    void testAddStandardReport_ShouldSendMqMessage() {
        StandardEnergyReportSaveVo saveVo = new StandardEnergyReportSaveVo();
        saveVo.setSource("IOT");
        saveVo.setSourceReportId("RPT-20260309-001");
        saveVo.setDeviceNo("DEV-001");
        saveVo.setRecordTime(LocalDateTime.of(2026, 3, 9, 15, 0, 0));
        saveVo.setTotalEnergy(new BigDecimal("123.45"));
        saveVo.setHigherEnergy(new BigDecimal("11.11"));
        saveVo.setHighEnergy(new BigDecimal("22.22"));
        saveVo.setLowEnergy(new BigDecimal("33.33"));
        saveVo.setLowerEnergy(new BigDecimal("44.44"));
        saveVo.setDeepLowEnergy(new BigDecimal("12.35"));

        energyReportBiz.addStandardReport(saveVo);

        ArgumentCaptor<MqMessage> messageCaptor = ArgumentCaptor.forClass(MqMessage.class);
        verify(mqService).sendMessage(messageCaptor.capture());
        MqMessage mqMessage = messageCaptor.getValue();
        assertThat(mqMessage.getMessageDestination()).isEqualTo(DeviceMqConstant.DEVICE_DESTINATION);
        assertThat(mqMessage.getRoutingIdentifier()).isEqualTo(DeviceMqConstant.ROUTING_KEY_STANDARD_ENERGY_REPORT);
        assertThat(mqMessage.getPayload()).isInstanceOf(StandardEnergyReportMessage.class);

        StandardEnergyReportMessage payload = (StandardEnergyReportMessage) mqMessage.getPayload();
        assertThat(payload.getSource()).isEqualTo("IOT");
        assertThat(payload.getSourceReportId()).isEqualTo("RPT-20260309-001");
        assertThat(payload.getDeviceNo()).isEqualTo("DEV-001");
        assertThat(payload.getRecordTime()).isEqualTo(LocalDateTime.of(2026, 3, 9, 15, 0, 0));
        assertThat(payload.getTotalEnergy()).isEqualByComparingTo("123.45");
        assertThat(payload.getHigherEnergy()).isEqualByComparingTo("11.11");
        assertThat(payload.getHighEnergy()).isEqualByComparingTo("22.22");
        assertThat(payload.getLowEnergy()).isEqualByComparingTo("33.33");
        assertThat(payload.getLowerEnergy()).isEqualByComparingTo("44.44");
        assertThat(payload.getDeepLowEnergy()).isEqualByComparingTo("12.35");
    }
}
