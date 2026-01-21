package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountCancelDetailDto;
import info.zhihui.ems.business.account.dto.AccountCancelQueryDto;
import info.zhihui.ems.business.account.dto.AccountCancelRecordDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.entity.AccountCancelRecordEntity;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import info.zhihui.ems.business.account.mapper.AccountCancelMapper;
import info.zhihui.ems.business.account.mapper.AccountInfoMapper;
import info.zhihui.ems.business.account.qo.AccountCancelRecordQo;
import info.zhihui.ems.business.account.qo.AccountQo;
import info.zhihui.ems.business.account.repository.AccountCancelRecordRepository;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AccountInfoServiceImplTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountInfoMapper accountInfoMapper;

    @Mock
    private AccountCancelRecordRepository accountCancelRecordRepository;

    @Mock
    private AccountCancelMapper accountCancelMapper;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @InjectMocks
    private AccountInfoServiceImpl accountInfoService;

    @Test
    void testFindList() {
        AccountQueryDto query = new AccountQueryDto().setOwnerId(1);
        AccountQo qo = new AccountQo().setOwnerId(1);
        List<AccountEntity> entityList = List.of(new AccountEntity());
        List<AccountBo> boList = List.of(new AccountBo());
        Mockito.when(accountInfoMapper.queryToQo(query)).thenReturn(qo);
        Mockito.when(accountRepository.findList(qo)).thenReturn(entityList);
        Mockito.when(accountInfoMapper.listEntityToBo(entityList)).thenReturn(boList);
        List<AccountBo> result = accountInfoService.findList(query);
        Assertions.assertEquals(boList, result);
    }

    @Test
    void testGetById_Success() {
        AccountEntity entity = new AccountEntity();
        entity.setDeleteTime(null);
        Mockito.when(accountRepository.selectById(1)).thenReturn(entity);
        AccountBo bo = new AccountBo();
        Mockito.when(accountInfoMapper.entityToBo(entity)).thenReturn(bo);
        assertEquals(bo, accountInfoService.getById(1));
    }

    @Test
    void testGetById_NotFound() {
        Mockito.when(accountRepository.selectById(1)).thenReturn(null);
        Assertions.assertThrows(NotFoundException.class, () -> accountInfoService.getById(1));
    }

    @Test
    void testFindCancelRecordPage_Success() {
        // 准备测试数据
        AccountCancelQueryDto queryDto = new AccountCancelQueryDto()
                .setOwnerName("测试企业")
                .setCleanBalanceType(CleanBalanceTypeEnum.REFUND);
        PageParam pageParam = new PageParam();

        AccountCancelRecordQo qo = new AccountCancelRecordQo()
                .setOwnerName("测试企业")
                .setCleanBalanceType(CleanBalanceTypeEnum.REFUND.getCode());

        AccountCancelRecordDto listDto = new AccountCancelRecordDto()
                .setCancelNo("CANCEL001")
                .setOwnerName("测试企业");

        PageResult<AccountCancelRecordDto> expectedResult = new PageResult<>();
        expectedResult.setList(List.of(listDto));
        expectedResult.setTotal(1L);

        // Mock 行为
        Mockito.when(accountCancelMapper.queryDtoToQo(queryDto)).thenReturn(qo);
        Mockito.when(accountCancelMapper.pageEntityToBo(ArgumentMatchers.any())).thenReturn(expectedResult);

        // 执行测试
        PageResult<AccountCancelRecordDto> result = accountInfoService.findCancelRecordPage(queryDto, pageParam);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getTotal());
        Assertions.assertEquals(1, result.getList().size());
        Assertions.assertEquals("CANCEL001", result.getList().get(0).getCancelNo());
        Assertions.assertEquals("测试企业", result.getList().get(0).getOwnerName());

        // 验证方法调用
        Mockito.verify(accountCancelMapper).queryDtoToQo(queryDto);
        Mockito.verify(accountCancelMapper).pageEntityToBo(ArgumentMatchers.any());
    }

    @Test
    void testFindCancelRecordPage_EmptyResult() {
        // 准备测试数据
        AccountCancelQueryDto queryDto = new AccountCancelQueryDto()
                .setOwnerName("不存在的企业")
                .setCleanBalanceType(CleanBalanceTypeEnum.PAY);
        PageParam pageParam = new PageParam();

        AccountCancelRecordQo qo = new AccountCancelRecordQo()
                .setOwnerName("不存在的企业")
                .setCleanBalanceType(CleanBalanceTypeEnum.PAY.getCode());

        PageResult<AccountCancelRecordDto> expectedResult = new PageResult<>();
        expectedResult.setList(List.of());
        expectedResult.setTotal(0L);

        // Mock 行为
        Mockito.when(accountCancelMapper.queryDtoToQo(queryDto)).thenReturn(qo);
        Mockito.when(accountCancelMapper.pageEntityToBo(ArgumentMatchers.any())).thenReturn(expectedResult);

        // 执行测试
        PageResult<AccountCancelRecordDto> result = accountInfoService.findCancelRecordPage(queryDto, pageParam);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0L, result.getTotal());
        Assertions.assertTrue(result.getList().isEmpty());

        // 验证方法调用
        Mockito.verify(accountCancelMapper).queryDtoToQo(queryDto);
        Mockito.verify(accountCancelMapper).pageEntityToBo(ArgumentMatchers.any());
    }

    @Test
    void testGetCancelRecordDetail_Success() {
        // 准备测试数据
        String cancelNo = "CANCEL001";

        AccountCancelRecordEntity entity = new AccountCancelRecordEntity()
                .setCancelNo(cancelNo)
                .setOwnerName("测试企业");

        AccountCancelDetailDto detailDto = new AccountCancelDetailDto()
                .setCancelNo(cancelNo)
                .setOwnerName("测试企业");

        CanceledMeterDto meterDto = new CanceledMeterDto()
                .setMeterNo("METER001")
                .setMeterName("测试电表");

        List<CanceledMeterDto> meterList = List.of(meterDto);

        // Mock 行为
        Mockito.when(accountCancelRecordRepository.selectByCancelNo(cancelNo)).thenReturn(entity);
        Mockito.when(accountCancelMapper.entityToDetailDto(entity)).thenReturn(detailDto);
        Mockito.when(electricMeterInfoService.findMetersByCancelNo(cancelNo)).thenReturn(meterList);

        // 执行测试
        AccountCancelDetailDto result = accountInfoService.getCancelRecordDetail(cancelNo);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(cancelNo, result.getCancelNo());
        Assertions.assertEquals("测试企业", result.getOwnerName());
        Assertions.assertNotNull(result.getMeterList());
        Assertions.assertEquals(1, result.getMeterList().size());
        Assertions.assertEquals("METER001", result.getMeterList().get(0).getMeterNo());

        // 验证方法调用
        Mockito.verify(accountCancelRecordRepository).selectByCancelNo(cancelNo);
        Mockito.verify(accountCancelMapper).entityToDetailDto(entity);
        Mockito.verify(electricMeterInfoService).findMetersByCancelNo(cancelNo);
    }

    @Test
    void testGetCancelRecordDetail_NotFound() {
        // 准备测试数据
        String cancelNo = "NONEXISTENT";

        // Mock 行为
        Mockito.when(accountCancelRecordRepository.selectByCancelNo(cancelNo)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> accountInfoService.getCancelRecordDetail(cancelNo)
        );

        Assertions.assertEquals("销户记录不存在", exception.getMessage());

        // 验证方法调用
        Mockito.verify(accountCancelRecordRepository).selectByCancelNo(cancelNo);
        Mockito.verify(accountCancelMapper, Mockito.never()).entityToDetailDto(ArgumentMatchers.any());
        Mockito.verify(electricMeterInfoService, Mockito.never()).findMetersByCancelNo(ArgumentMatchers.any());
    }

}