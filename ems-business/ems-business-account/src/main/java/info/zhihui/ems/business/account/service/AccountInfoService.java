package info.zhihui.ems.business.account.service;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountCancelDetailDto;
import info.zhihui.ems.business.account.dto.AccountCancelQueryDto;
import info.zhihui.ems.business.account.dto.AccountCancelRecordDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 账户基础信息接口
 *
 * @author jerryxiaosa
 */
public interface AccountInfoService {

    /**
     * 查询账户列表
     *
     * @param query 查询条件
     * @return 账户列表
     */
    List<AccountBo> findList(@NotNull AccountQueryDto query);

    /**
     * 分页查询账户列表
     *
     * @param query 查询条件
     * @param pageParam 分页参数
     * @return 账户分页结果
     */
    PageResult<AccountBo> findPage(@NotNull AccountQueryDto query, @NotNull PageParam pageParam);

    /**
     * 根据ID查询账户详情
     *
     * @param id 账户ID
     * @return 账户详情
     */
    AccountBo getById(@NotNull Integer id);

    /**
     * 分页查询销户记录
     *
     * @param query 查询条件
     * @param pageParam 分页参数
     * @return 销户记录分页结果
     */
    PageResult<AccountCancelRecordDto> findCancelRecordPage(@NotNull AccountCancelQueryDto query, @NotNull PageParam pageParam);

    /**
     * 根据销户编号查询销户详情
     *
     * @param cancelNo 销户编号
     * @return 销户详情
     */
    AccountCancelDetailDto getCancelRecordDetail(@NotNull String cancelNo);

    /**
     * 按账户ID批量统计可开户电表总数（租赁空间内电表数）
     *
     * @param accountIds 账户ID列表
     * @return key=账户ID，value=可开户电表总数
     */
    Map<Integer, Integer> countTotalOpenableMeterByAccountIds(@NotNull List<Integer> accountIds);

}
