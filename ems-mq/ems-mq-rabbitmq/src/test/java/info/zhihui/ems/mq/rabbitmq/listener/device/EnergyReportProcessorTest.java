package info.zhihui.ems.mq.rabbitmq.listener.device;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.billing.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.billing.service.consume.MeterConsumeService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.enums.CalculateTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.mq.api.message.device.StandardEnergyReportMessage;
import info.zhihui.ems.mq.rabbitmq.exception.NonRetryableException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyReportProcessorTest {

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private AccountInfoService accountInfoService;

    @Mock
    private MeterConsumeService meterConsumeService;

    @InjectMocks
    private EnergyReportProcessor standardEnergyReportProcessor;

    @Test
    @DisplayName("标准电量上报应组装抄表DTO并保存")
    void testProcess_ShouldBuildRecordDtoAndSave() {
        StandardEnergyReportMessage message = buildMessage();
        when(electricMeterInfoService.getByDeviceNo("DEV-001")).thenReturn(new ElectricMeterBo()
                .setId(101)
                .setMeterName("1号电表")
                .setDeviceNo("DEV-001")
                .setSpaceId(301)
                .setIsCalculate(true)
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING)
                .setIsPrepay(true)
                .setPricePlanId(8)
                .setCt(15)
                .setAccountId(201)
                .setIsOnline(true)
                .setIsCutOff(false));
        when(accountInfoService.getById(201)).thenReturn(new AccountBo()
                .setId(201)
                .setOwnerId(401)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));
        doNothing().when(meterConsumeService).savePowerRecord(org.mockito.ArgumentMatchers.any(ElectricMeterPowerRecordDto.class));

        standardEnergyReportProcessor.process(message);

        ArgumentCaptor<ElectricMeterPowerRecordDto> recordCaptor = ArgumentCaptor.forClass(ElectricMeterPowerRecordDto.class);
        verify(meterConsumeService).savePowerRecord(recordCaptor.capture());
        ElectricMeterPowerRecordDto recordDto = recordCaptor.getValue();
        assertThat(recordDto.getOriginalReportId()).hasSize(64);
        assertThat(recordDto.getAccountId()).isEqualTo(201);
        assertThat(recordDto.getOwnerId()).isEqualTo(401);
        assertThat(recordDto.getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE);
        assertThat(recordDto.getOwnerName()).isEqualTo("测试企业");
        assertThat(recordDto.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.QUANTITY);
        assertThat(recordDto.getRecordTime()).isEqualTo(LocalDateTime.of(2026, 3, 9, 15, 0, 0));
        assertThat(recordDto.getNeedConsume()).isTrue();
        assertThat(recordDto.getPower()).isEqualByComparingTo("123.45");
        assertThat(recordDto.getPowerHigher()).isEqualByComparingTo("11.11");
        assertThat(recordDto.getPowerHigh()).isEqualByComparingTo("22.22");
        assertThat(recordDto.getPowerLow()).isEqualByComparingTo("33.33");
        assertThat(recordDto.getPowerLower()).isEqualByComparingTo("44.44");
        assertThat(recordDto.getPowerDeepLow()).isEqualByComparingTo("12.35");
        assertThat(recordDto.getElectricMeterDetailDto().getMeterId()).isEqualTo(101);
        assertThat(recordDto.getElectricMeterDetailDto().getMeterName()).isEqualTo("1号电表");
        assertThat(recordDto.getElectricMeterDetailDto().getDeviceNo()).isEqualTo("DEV-001");
        assertThat(recordDto.getElectricMeterDetailDto().getSpaceId()).isEqualTo(301);
        assertThat(recordDto.getElectricMeterDetailDto().getIsCalculate()).isTrue();
        assertThat(recordDto.getElectricMeterDetailDto().getCalculateType()).isEqualTo(CalculateTypeEnum.AIR_CONDITIONING);
        assertThat(recordDto.getElectricMeterDetailDto().getIsPrepay()).isTrue();
        assertThat(recordDto.getElectricMeterDetailDto().getPricePlanId()).isEqualTo(8);
        assertThat(recordDto.getElectricMeterDetailDto().getCt()).isEqualTo(15);
        assertThat(recordDto.getElectricMeterDetailDto().getIsOnline()).isTrue();
        assertThat(recordDto.getElectricMeterDetailDto().getIsCutOff()).isFalse();
    }

    @Test
    @DisplayName("电表未绑定账户时应只保存抄表记录")
    void testProcess_WhenMeterHasNoAccount_ShouldNotQueryAccount() {
        StandardEnergyReportMessage message = buildMessage();
        when(electricMeterInfoService.getByDeviceNo("DEV-001")).thenReturn(new ElectricMeterBo()
                .setId(101)
                .setMeterName("1号电表")
                .setDeviceNo("DEV-001")
                .setSpaceId(301)
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setAccountId(null));

        standardEnergyReportProcessor.process(message);

        verify(accountInfoService, never()).getById(org.mockito.ArgumentMatchers.anyInt());
        verify(meterConsumeService).savePowerRecord(org.mockito.ArgumentMatchers.any(ElectricMeterPowerRecordDto.class));
    }

    @Test
    @DisplayName("电表不存在时应抛出不可重试异常")
    void testProcess_WhenMeterNotFound_ShouldThrowNonRetryableException() {
        StandardEnergyReportMessage message = buildMessage();
        when(electricMeterInfoService.getByDeviceNo("DEV-001")).thenThrow(new NotFoundException("meter not found"));

        assertThatThrownBy(() -> standardEnergyReportProcessor.process(message))
                .isInstanceOf(NonRetryableException.class)
                .hasMessageContaining("DEV-001");

        verify(accountInfoService, never()).getById(org.mockito.ArgumentMatchers.anyInt());
        verifyNoInteractions(meterConsumeService);
    }

    @Test
    @DisplayName("账户不存在时应抛出不可重试异常")
    void testProcess_WhenAccountNotFound_ShouldThrowNonRetryableException() {
        StandardEnergyReportMessage message = buildMessage();
        when(electricMeterInfoService.getByDeviceNo("DEV-001")).thenReturn(new ElectricMeterBo()
                .setId(101)
                .setMeterName("1号电表")
                .setDeviceNo("DEV-001")
                .setAccountId(201));
        when(accountInfoService.getById(201)).thenThrow(new NotFoundException("account not found"));

        assertThatThrownBy(() -> standardEnergyReportProcessor.process(message))
                .isInstanceOf(NonRetryableException.class)
                .hasMessageContaining("201");

        verifyNoInteractions(meterConsumeService);
    }

    @Test
    @DisplayName("保存抄表记录的业务异常应继续抛出")
    void testProcess_WhenSavePowerRecordThrowsBusinessRuntimeException_ShouldRethrow() {
        StandardEnergyReportMessage message = buildMessage();
        when(electricMeterInfoService.getByDeviceNo("DEV-001")).thenReturn(new ElectricMeterBo()
                .setId(101)
                .setMeterName("1号电表")
                .setDeviceNo("DEV-001")
                .setAccountId(null));
        doThrow(new BusinessRuntimeException("save failed"))
                .when(meterConsumeService).savePowerRecord(org.mockito.ArgumentMatchers.any(ElectricMeterPowerRecordDto.class));

        assertThatThrownBy(() -> standardEnergyReportProcessor.process(message))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("save failed");
    }

    @Test
    @DisplayName("source为空时应使用默认值并生成固定长度幂等键")
    void testProcess_WhenSourceIsBlank_ShouldUseDefaultSource() {
        StandardEnergyReportMessage message = buildMessage().setSource(null);
        when(electricMeterInfoService.getByDeviceNo("DEV-001")).thenReturn(new ElectricMeterBo()
                .setId(101)
                .setMeterName("1号电表")
                .setDeviceNo("DEV-001")
                .setSpaceId(301)
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setAccountId(null));

        standardEnergyReportProcessor.process(message);

        ArgumentCaptor<ElectricMeterPowerRecordDto> recordCaptor = ArgumentCaptor.forClass(ElectricMeterPowerRecordDto.class);
        verify(meterConsumeService).savePowerRecord(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getOriginalReportId()).hasSize(64);
    }

    private StandardEnergyReportMessage buildMessage() {
        return new StandardEnergyReportMessage()
                .setSource("iot")
                .setSourceReportId("RPT-20260309-001")
                .setDeviceNo("DEV-001")
                .setRecordTime(LocalDateTime.of(2026, 3, 9, 15, 0, 0))
                .setTotalEnergy(new BigDecimal("123.45"))
                .setHigherEnergy(new BigDecimal("11.11"))
                .setHighEnergy(new BigDecimal("22.22"))
                .setLowEnergy(new BigDecimal("33.33"))
                .setLowerEnergy(new BigDecimal("44.44"))
                .setDeepLowEnergy(new BigDecimal("12.35"));
    }
}
