package info.zhihui.ems.web.account.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.*;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountManagerService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.account.mapstruct.AccountWebMapper;
import info.zhihui.ems.web.account.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 账户业务编排层
 */
@Service
@RequiredArgsConstructor
public class AccountBiz {

    private final AccountInfoService accountInfoService;
    private final AccountManagerService accountManagerService;
    private final AccountWebMapper accountWebMapper;
    private final ElectricMeterInfoService electricMeterInfoService;

    /**
     * 分页查询账户列表
     */
    public PageResult<AccountVo> findAccountPage(AccountQueryVo queryVo, Integer pageNum, Integer pageSize) {
        AccountQueryDto queryDto = accountWebMapper.toAccountQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new AccountQueryDto();
        }
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        return accountWebMapper.toAccountVoPage(accountInfoService.findPage(queryDto, pageParam));
    }

    /**
     * 根据ID获取账户详情
     */
    public AccountVo getAccount(Integer id) {
        AccountBo accountBo = accountInfoService.getById(id);
        AccountVo accountVo = accountWebMapper.toAccountVo(accountBo);

        List<ElectricMeterBo> meterBos = electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountId(id));
        accountVo.setMeterList(accountWebMapper.toAccountMeterVoList(meterBos));
        return accountVo;
    }

    /**
     * 分页查询销户记录
     */
    public PageResult<AccountCancelRecordVo> findCancelRecordPage(AccountCancelQueryVo queryVo, Integer pageNum, Integer pageSize) {
        AccountCancelQueryDto queryDto = accountWebMapper.toAccountCancelQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new AccountCancelQueryDto();
        }
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        return accountWebMapper.toAccountCancelRecordVoPage(accountInfoService.findCancelRecordPage(queryDto, pageParam));
    }

    /**
     * 获取销户详情
     */
    public AccountCancelDetailVo getCancelRecordDetail(String cancelNo) {
        return accountWebMapper.toAccountCancelDetailVo(accountInfoService.getCancelRecordDetail(cancelNo));
    }

    /**
     * 开户
     */
    public Integer openAccount(OpenAccountVo openAccountVo) {
        OpenAccountDto dto = accountWebMapper.toOpenAccountDto(openAccountVo);
        return accountManagerService.openAccount(dto);
    }

    /**
     * 追加绑定电表
     */
    public void appendMeters(Integer accountId, AccountMetersOpenVo accountMetersOpenVo) {
        AccountMetersOpenDto dto = accountWebMapper.toAccountMetersOpenDto(accountMetersOpenVo);
        dto.setAccountId(accountId);
        accountManagerService.appendMeters(dto);
    }

    /**
     * 更新账户配置
     */
    public void updateAccountConfig(Integer accountId, AccountConfigUpdateVo accountConfigUpdateVo) {
        AccountConfigUpdateDto dto = accountWebMapper.toAccountConfigUpdateDto(accountConfigUpdateVo);
        dto.setAccountId(accountId);
        accountManagerService.updateAccountConfig(dto);
    }

    /**
     * 销户
     */
    public CancelAccountResponseVo cancelAccount(CancelAccountVo cancelAccountVo) {
        CancelAccountDto dto = accountWebMapper.toCancelAccountDto(cancelAccountVo);
        CancelAccountResponseDto responseDto = accountManagerService.cancelAccount(dto);
        return accountWebMapper.toCancelAccountResponseVo(responseDto);
    }
}
