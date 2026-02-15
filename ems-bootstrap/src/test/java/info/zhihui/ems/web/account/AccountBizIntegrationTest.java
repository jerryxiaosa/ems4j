package info.zhihui.ems.web.account;

import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.account.biz.AccountBiz;
import info.zhihui.ems.web.account.vo.AccountDetailVo;
import info.zhihui.ems.web.account.vo.AccountQueryVo;
import info.zhihui.ems.web.account.vo.AccountVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class AccountBizIntegrationTest {

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private ElectricMeterInfoService electricMeterInfoService;

    @Test
    @DisplayName("分页查询应填充每个账户的电表数量")
    void testFindAccountPage_DefaultQuery_ShouldFillMeterCount() {
        PageResult<AccountVo> pageResult = accountBiz.findAccountPage(new AccountQueryVo(), 1, 10);

        assertNotNull(pageResult);
        assertNotNull(pageResult.getList());
        assertFalse(pageResult.getList().isEmpty());

        for (AccountVo accountVo : pageResult.getList()) {
            if (accountVo == null || accountVo.getId() == null) {
                continue;
            }
            int expectedMeterCount = electricMeterInfoService
                    .findList(new ElectricMeterQueryDto().setAccountIds(List.of(accountVo.getId())))
                    .size();
            assertEquals(expectedMeterCount, accountVo.getOpenMeterCount());
        }
    }

    @Test
    @DisplayName("账户详情应返回对应账户的电表列表")
    void testGetAccount_ExistingAccount_ShouldContainMeterList() {
        Integer accountId = 1;

        AccountDetailVo detailVo = accountBiz.getAccount(accountId);

        assertNotNull(detailVo);
        assertEquals(accountId, detailVo.getId());

        List<?> meterList = detailVo.getMeterList();
        int actualMeterCount = meterList == null ? 0 : meterList.size();
        int expectedMeterCount = electricMeterInfoService
                .findList(new ElectricMeterQueryDto().setAccountIds(List.of(accountId)))
                .size();
        assertEquals(expectedMeterCount, actualMeterCount);
    }
}
