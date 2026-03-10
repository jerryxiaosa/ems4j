package info.zhihui.ems.web.account.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountManagerService;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.billing.service.balance.BalanceService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.account.mapstruct.AccountWebMapper;
import info.zhihui.ems.web.account.vo.AccountOptionQueryVo;
import info.zhihui.ems.web.account.vo.AccountOptionVo;
import info.zhihui.ems.web.common.support.SpaceDisplaySupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBizTest {

    @InjectMocks
    private AccountBiz accountBiz;

    @Mock
    private AccountInfoService accountInfoService;

    @Mock
    private AccountManagerService accountManagerService;

    @Mock
    private AccountAdditionalInfoService accountAdditionalInfoService;

    @Mock
    private AccountWebMapper accountWebMapper;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private SpaceDisplaySupport spaceDisplaySupport;

    @Mock
    private BalanceService balanceService;

    @Test
    @DisplayName("查询账户下拉列表_应通过findPage查询并透传分页参数")
    void testFindAccountOptionList_ShouldUseFindPage() {
        AccountOptionQueryVo queryVo = new AccountOptionQueryVo()
                .setOwnerType(0)
                .setOwnerNameLike("企业")
                .setLimit(2);
        AccountQueryDto queryDto = new AccountQueryDto();
        List<AccountBo> accountBoList = List.of(new AccountBo().setId(1), new AccountBo().setId(2));
        PageResult<AccountBo> pageResult = new PageResult<AccountBo>()
                .setPageNum(1)
                .setPageSize(2)
                .setTotal(2L)
                .setList(accountBoList);
        List<AccountOptionVo> expected = List.of(
                new AccountOptionVo().setId(1),
                new AccountOptionVo().setId(2)
        );
        when(accountWebMapper.toAccountQueryDto(queryVo)).thenReturn(queryDto);
        when(accountInfoService.findPage(any(), any())).thenReturn(pageResult);
        when(accountWebMapper.toAccountOptionVoList(accountBoList)).thenReturn(expected);

        List<AccountOptionVo> result = accountBiz.findAccountOptionList(queryVo);

        assertThat(result).isSameAs(expected);
        ArgumentCaptor<PageParam> pageParamCaptor = ArgumentCaptor.forClass(PageParam.class);
        verify(accountInfoService).findPage(any(), pageParamCaptor.capture());
        verify(accountInfoService, never()).findList(any());
        assertThat(pageParamCaptor.getValue().getPageNum()).isEqualTo(1);
        assertThat(pageParamCaptor.getValue().getPageSize()).isEqualTo(2);
    }
}
