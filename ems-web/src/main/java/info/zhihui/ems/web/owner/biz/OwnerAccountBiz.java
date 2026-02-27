package info.zhihui.ems.web.owner.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.web.owner.vo.OwnerAccountStatusQueryVo;
import info.zhihui.ems.web.owner.vo.OwnerAccountStatusVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 主体账户状态业务编排
 */
@Service
@RequiredArgsConstructor
public class OwnerAccountBiz {

    private final AccountInfoService accountInfoService;

    /**
     * 查询主体账户状态
     */
    public OwnerAccountStatusVo getAccountStatus(OwnerAccountStatusQueryVo queryVo) {
        OwnerTypeEnum ownerType = parseOwnerType(queryVo.getOwnerType());
        Integer ownerId = queryVo.getOwnerId();

        List<AccountBo> accountBoList = accountInfoService.findList(new AccountQueryDto()
                .setOwnerType(ownerType)
                .setOwnerIds(List.of(ownerId)));

        OwnerAccountStatusVo statusVo = new OwnerAccountStatusVo()
                .setOwnerType(ownerType.getCode())
                .setOwnerId(ownerId);
        if (CollectionUtils.isEmpty(accountBoList)) {
            return statusVo.setHasAccount(false);
        }
        if (accountBoList.size() > 1) {
            throw new BusinessRuntimeException("主体存在多个账户，请联系管理员处理");
        }

        AccountBo accountBo = accountBoList.get(0);
        return statusVo
                .setHasAccount(true)
                .setAccountId(accountBo.getId())
                .setElectricAccountType(accountBo.getElectricAccountType() == null ? null : accountBo.getElectricAccountType().getCode())
                .setElectricPricePlanId(accountBo.getElectricPricePlanId())
                .setWarnPlanId(accountBo.getWarnPlanId())
                .setMonthlyPayAmount(accountBo.getMonthlyPayAmount());
    }

    private OwnerTypeEnum parseOwnerType(Integer ownerTypeCode) {
        OwnerTypeEnum ownerType = CodeEnum.fromCode(ownerTypeCode, OwnerTypeEnum.class);
        if (ownerType == null) {
            throw new BusinessRuntimeException("主体类型不正确");
        }
        return ownerType;
    }

}
