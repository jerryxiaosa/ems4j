package info.zhihui.ems.business.billing.service.record.impl;

import info.zhihui.ems.business.billing.dto.ElectricMeterPowerConsumeTrendPointDto;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.billing.qo.ElectricMeterPowerConsumeRecordQo;
import info.zhihui.ems.business.billing.repository.ElectricMeterPowerConsumeRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElectricMeterPowerConsumeRecordServiceImpl测试")
class ElectricMeterPowerConsumeRecordServiceImplTest {

    @Mock
    private ElectricMeterPowerConsumeRecordRepository electricMeterPowerConsumeRecordRepository;

    @InjectMocks
    private ElectricMeterPowerConsumeRecordServiceImpl electricMeterPowerConsumeRecordService;

    @Test
    @DisplayName("查询区间耗电趋势应按消费时间升序查询并限制1000条")
    void testFindTrendRecordList_ShouldQueryWithMeterConsumeTimeAscAndLimit() {
        Integer meterId = 1001;
        LocalDateTime beginTime = LocalDateTime.of(2026, 3, 27, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 28, 23, 59, 59);
        ElectricMeterPowerConsumeRecordEntity firstRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setMeterId(meterId)
                .setBeginRecordTime(LocalDateTime.of(2026, 3, 27, 8, 0, 0))
                .setEndRecordTime(LocalDateTime.of(2026, 3, 27, 10, 0, 0))
                .setMeterConsumeTime(LocalDateTime.of(2026, 3, 27, 10, 0, 0))
                .setConsumePower(new BigDecimal("3.50"));
        ElectricMeterPowerConsumeRecordEntity secondRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setMeterId(meterId)
                .setBeginRecordTime(LocalDateTime.of(2026, 3, 28, 9, 0, 0))
                .setEndRecordTime(LocalDateTime.of(2026, 3, 28, 11, 30, 0))
                .setMeterConsumeTime(LocalDateTime.of(2026, 3, 28, 11, 30, 0))
                .setConsumePower(new BigDecimal("5.25"));
        when(electricMeterPowerConsumeRecordRepository.findTrendRecordList(any(ElectricMeterPowerConsumeRecordQo.class)))
                .thenReturn(List.of(firstRecord, secondRecord));

        List<ElectricMeterPowerConsumeTrendPointDto> result = electricMeterPowerConsumeRecordService.findTrendRecordList(
                meterId, beginTime, endTime);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(firstRecord.getMeterConsumeTime(), result.get(0).getMeterConsumeTime());
        assertEquals(secondRecord.getConsumePower(), result.get(1).getConsumePower());

        ArgumentCaptor<ElectricMeterPowerConsumeRecordQo> qoCaptor = ArgumentCaptor.forClass(ElectricMeterPowerConsumeRecordQo.class);
        verify(electricMeterPowerConsumeRecordRepository).findTrendRecordList(qoCaptor.capture());
        ElectricMeterPowerConsumeRecordQo capturedQo = qoCaptor.getValue();
        assertEquals(meterId, capturedQo.getMeterId());
        assertEquals(beginTime, capturedQo.getBeginTime());
        assertEquals(endTime, capturedQo.getEndTime());
        assertEquals(1000, capturedQo.getLimit());
    }
}
