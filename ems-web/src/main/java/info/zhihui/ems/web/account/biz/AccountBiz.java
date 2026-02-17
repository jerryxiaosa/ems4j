package info.zhihui.ems.web.account.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.*;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountManagerService;
import info.zhihui.ems.business.account.service.AccountSpaceLeaseService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.web.account.mapstruct.AccountWebMapper;
import info.zhihui.ems.web.account.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 账户业务编排层
 */
@Service
@RequiredArgsConstructor
public class AccountBiz {

    private final AccountInfoService accountInfoService;
    private final AccountManagerService accountManagerService;
    private final AccountSpaceLeaseService accountSpaceLeaseService;
    private final AccountWebMapper accountWebMapper;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final OrganizationService organizationService;

    /**
     * 分页查询账户列表
     */
    public PageResult<AccountVo> findAccountPage(AccountQueryVo queryVo, Integer pageNum, Integer pageSize) {
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));

        AccountQueryDto queryDto = accountWebMapper.toAccountQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new AccountQueryDto();
        }

        List<Integer> ownerIds = findOwnerIdsByOwnerNameLike(queryVo);
        if (ownerIds != null) {
            if (ownerIds.isEmpty()) {
                return new PageResult<AccountVo>()
                        .setPageNum(pageParam.getPageNum())
                        .setPageSize(pageParam.getPageSize())
                        .setTotal(0L)
                        .setList(Collections.emptyList());
            }
            queryDto.setOwnerIds(ownerIds);
        }

        PageResult<AccountVo> pageResult = accountWebMapper.toAccountVoPage(accountInfoService.findPage(queryDto, pageParam));
        List<AccountVo> accountVoList = pageResult.getList();
        if (accountVoList == null || accountVoList.isEmpty()) {
            return pageResult;
        }
        List<Integer> accountIdList = extractAccountIdList(accountVoList);
        if (accountIdList.isEmpty()) {
            return pageResult;
        }

        fillOpenedMeterCount(accountVoList, accountIdList);
        fillTotalOpenableMeterCount(accountVoList, accountIdList);
        return pageResult;
    }

    /**
     * 根据ID获取账户详情
     */
    public AccountDetailVo getAccount(Integer id) {
        AccountBo accountBo = accountInfoService.getById(id);
        AccountDetailVo accountVo = accountWebMapper.toAccountDetailVo(accountBo);

        List<ElectricMeterBo> meterBos = electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountIds(List.of(id)));
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
     * 租赁空间
     */
    public void rentSpaces(Integer accountId, AccountSpaceRentVo accountSpaceRentVo) {
        AccountSpaceRentDto dto = accountWebMapper.toAccountSpaceRentDto(accountSpaceRentVo);
        dto.setAccountId(accountId);
        accountSpaceLeaseService.rentSpaces(dto);
    }

    /**
     * 退租空间
     */
    public void unrentSpaces(Integer accountId, AccountSpaceUnrentVo accountSpaceUnrentVo) {
        AccountSpaceUnrentDto dto = accountWebMapper.toAccountSpaceUnrentDto(accountSpaceUnrentVo);
        dto.setAccountId(accountId);
        accountSpaceLeaseService.unrentSpaces(dto);
    }

    /**
     * 更新账户配置
     */
    public void updateAccountConfig(Integer accountId, AccountConfigUpdateVo accountConfigUpdateVo) {
        AccountConfigUpdateDto dto = accountWebMapper.toAccountConfigUpdateDto(accountConfigUpdateVo);
        dto.setAccountId(accountId);
        accountManagerService.updateAccount(dto);
    }

    /**
     * 销户
     */
    public CancelAccountResponseVo cancelAccount(CancelAccountVo cancelAccountVo) {
        CancelAccountDto dto = accountWebMapper.toCancelAccountDto(cancelAccountVo);
        CancelAccountResponseDto responseDto = accountManagerService.cancelAccount(dto);
        return accountWebMapper.toCancelAccountResponseVo(responseDto);
    }

    /**
     * 填充账户电表数量
     */
    private void fillOpenedMeterCount(List<AccountVo> accountVoList, List<Integer> accountIdList) {
        List<ElectricMeterBo> meterBoList = electricMeterInfoService.findList(
                new ElectricMeterQueryDto().setAccountIds(accountIdList)
        );
        Map<Integer, Integer> meterCountMap = meterBoList.stream()
                .map(ElectricMeterBo::getAccountId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(accountId -> accountId, Collectors.summingInt(ignore -> 1)));

        for (AccountVo accountVo : accountVoList) {
            if (accountVo != null && accountVo.getId() != null) {
                accountVo.setOpenedMeterCount(meterCountMap.getOrDefault(accountVo.getId(), 0));
            }
        }
    }

    /**
     * 填充账户可开户电表总数（租赁空间内电表数）
     */
    private void fillTotalOpenableMeterCount(List<AccountVo> accountVoList, List<Integer> accountIdList) {
        Map<Integer, Integer> totalOpenableMeterCountMap = accountInfoService.countTotalOpenableMeterByAccountIds(accountIdList);
        for (AccountVo accountVo : accountVoList) {
            if (accountVo != null && accountVo.getId() != null) {
                accountVo.setTotalOpenableMeterCount(totalOpenableMeterCountMap.getOrDefault(accountVo.getId(), 0));
            }
        }
    }

    /**
     * 提取有效账户ID列表
     */
    private List<Integer> extractAccountIdList(List<AccountVo> accountVoList) {
        return accountVoList.stream()
                .filter(Objects::nonNull)
                .map(AccountVo::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 通过组织名称模糊查询拼装账户归属ID集合
     */
    private List<Integer> findOwnerIdsByOwnerNameLike(AccountQueryVo queryVo) {
        if (queryVo == null || !StringUtils.hasText(queryVo.getOwnerNameLike())) {
            return null;
        }

        return organizationService.findOrganizationList(
                        new OrganizationQueryDto().setOrganizationNameLike(queryVo.getOwnerNameLike().trim())
                ).stream()
                .map(OrganizationBo::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
