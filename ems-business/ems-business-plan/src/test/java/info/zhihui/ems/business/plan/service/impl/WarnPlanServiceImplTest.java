package info.zhihui.ems.business.plan.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import info.zhihui.ems.business.plan.entity.WarnPlanEntity;
import info.zhihui.ems.business.plan.mapper.WarningPlanMapper;
import info.zhihui.ems.business.plan.repository.WarnPlanRepository;
import info.zhihui.ems.business.plan.qo.WarnPlanQo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarnPlanServiceImplTest {
    @Mock
    WarningPlanMapper mapper;
    @Mock
    WarnPlanRepository repository;
    @InjectMocks
    WarnPlanServiceImpl service;

    @Test
    void testFindList() {
        WarnPlanQueryDto query = new WarnPlanQueryDto();
        WarnPlanQo qo = new WarnPlanQo();
        List<WarnPlanEntity> entityList = List.of(new WarnPlanEntity());
        List<WarnPlanBo> boList = List.of(new WarnPlanBo());
        when(mapper.queryDtoToQo(query)).thenReturn(qo);
        when(repository.getList(qo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);
        assertEquals(boList, service.findList(query));
    }

    @Test
    void testGetDetail_Success() {
        WarnPlanEntity entity = new WarnPlanEntity();
        WarnPlanBo bo = new WarnPlanBo();
        when(repository.selectById(anyInt())).thenReturn(entity);
        when(mapper.detailEntityToBo(entity)).thenReturn(bo);
        assertEquals(bo, service.getDetail(1));
    }

    @Test
    void testGetDetail_NotFound() {
        when(repository.selectById(anyInt())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.getDetail(1));
    }

    @Test
    void testAdd_Success() {
        WarnPlanSaveDto saveDto = new WarnPlanSaveDto();
        saveDto.setId(999);
        WarnPlanEntity entity = new WarnPlanEntity();
        entity.setId(123);
        entity.setFirstLevel(java.math.BigDecimal.valueOf(200));
        entity.setSecondLevel(java.math.BigDecimal.valueOf(100));
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        doAnswer(invocation -> {
            ((WarnPlanEntity) invocation.getArgument(0)).setId(123);
            return 1;
        }).when(repository).insert(any(WarnPlanEntity.class));
        Integer id = service.add(saveDto);
        assertEquals(123, id);
        assertNull(saveDto.getId());
    }

    @Test
    void testAdd_FirstLevelNotGreater() {
        WarnPlanSaveDto saveDto = new WarnPlanSaveDto();
        WarnPlanEntity entity = new WarnPlanEntity();
        entity.setFirstLevel(BigDecimal.valueOf(100));
        entity.setSecondLevel(BigDecimal.valueOf(200));
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        assertThrows(BusinessRuntimeException.class, () -> service.add(saveDto));
    }

    @Test
    void testEdit_Success() {
        WarnPlanSaveDto saveDto = new WarnPlanSaveDto();
        saveDto.setId(1);
        WarnPlanEntity old = new WarnPlanEntity();
        WarnPlanEntity entity = new WarnPlanEntity();
        entity.setFirstLevel(java.math.BigDecimal.valueOf(200));
        entity.setSecondLevel(java.math.BigDecimal.valueOf(100));
        when(repository.selectById(anyInt())).thenReturn(old);
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(repository.updateById(entity)).thenReturn(1);
        assertDoesNotThrow(() -> service.edit(saveDto));
    }

    @Test
    void testEdit_NotFound() {
        WarnPlanSaveDto saveDto = new WarnPlanSaveDto();
        saveDto.setId(1);
        when(repository.selectById(anyInt())).thenReturn(null);
        assertThrows(BusinessRuntimeException.class, () -> service.edit(saveDto));
    }

    @Test
    void testEdit_FirstLevelNotGreater() {
        WarnPlanSaveDto saveDto = new WarnPlanSaveDto();
        saveDto.setId(10);
        WarnPlanEntity old = new WarnPlanEntity();
        WarnPlanEntity entity = new WarnPlanEntity();
        entity.setFirstLevel(BigDecimal.valueOf(100));
        entity.setSecondLevel(BigDecimal.valueOf(150));
        when(repository.selectById(10)).thenReturn(old);
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        assertThrows(BusinessRuntimeException.class, () -> service.edit(saveDto));
        verify(repository, never()).updateById(any(WarnPlanEntity.class));
    }

    @Test
    void testDelete() {
        when(repository.deleteById(anyInt())).thenReturn(1);
        assertDoesNotThrow(() -> service.delete(1));
        verify(repository, times(1)).deleteById(1);
    }
}
