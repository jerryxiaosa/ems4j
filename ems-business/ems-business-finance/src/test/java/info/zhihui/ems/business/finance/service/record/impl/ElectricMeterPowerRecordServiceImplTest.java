package info.zhihui.ems.business.finance.service.record.impl;

import info.zhihui.ems.business.finance.dto.ElectricMeterLatestPowerRecordDto;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerRecordEntity;
import info.zhihui.ems.business.finance.qo.ElectricMeterPowerRecordQo;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElectricMeterPowerRecordServiceImpl测试")
class ElectricMeterPowerRecordServiceImplTest {

    @Mock
    private ElectricMeterPowerRecordRepository electricMeterPowerRecordRepository;

    @InjectMocks
    private ElectricMeterPowerRecordServiceImpl electricMeterPowerRecordService;

    @Test
    @DisplayName("查询最新上报记录应返回最新一条")
    void testFindLatestRecord_WithLatestRecord_ShouldReturnDto() {
        Integer meterId = 1001;
        LocalDateTime recordTime = LocalDateTime.of(2026, 2, 28, 10, 58, 46);
        ElectricMeterPowerRecordEntity recordEntity = new ElectricMeterPowerRecordEntity()
                .setMeterId(meterId)
                .setRecordTime(recordTime)
                .setPower(new BigDecimal("1000.50"))
                .setPowerHigher(new BigDecimal("200.10"))
                .setPowerHigh(new BigDecimal("300.20"))
                .setPowerLow(new BigDecimal("250.30"))
                .setPowerLower(new BigDecimal("150.40"))
                .setPowerDeepLow(new BigDecimal("100.50"));
        when(electricMeterPowerRecordRepository.findRecordList(org.mockito.ArgumentMatchers.any(ElectricMeterPowerRecordQo.class)))
                .thenReturn(List.of(recordEntity));

        ElectricMeterLatestPowerRecordDto result = electricMeterPowerRecordService.findLatestRecord(meterId);

        assertNotNull(result);
        assertEquals(recordTime, result.getRecordTime());
        assertEquals(new BigDecimal("1000.50"), result.getPower());
        assertEquals(new BigDecimal("200.10"), result.getPowerHigher());
        assertEquals(new BigDecimal("300.20"), result.getPowerHigh());
        assertEquals(new BigDecimal("250.30"), result.getPowerLow());
        assertEquals(new BigDecimal("150.40"), result.getPowerLower());
        assertEquals(new BigDecimal("100.50"), result.getPowerDeepLow());

        ArgumentCaptor<ElectricMeterPowerRecordQo> qoCaptor = ArgumentCaptor.forClass(ElectricMeterPowerRecordQo.class);
        verify(electricMeterPowerRecordRepository).findRecordList(qoCaptor.capture());
        ElectricMeterPowerRecordQo capturedQo = qoCaptor.getValue();
        assertEquals(meterId, capturedQo.getMeterId());
        assertEquals(1, capturedQo.getLimit());
    }

    @Test
    @DisplayName("查询最新上报记录在无记录时应返回null")
    void testFindLatestRecord_WithoutRecord_ShouldReturnNull() {
        when(electricMeterPowerRecordRepository.findRecordList(org.mockito.ArgumentMatchers.any(ElectricMeterPowerRecordQo.class)))
                .thenReturn(Collections.emptyList());

        ElectricMeterLatestPowerRecordDto result = electricMeterPowerRecordService.findLatestRecord(1001);

        assertNull(result);
    }
}
