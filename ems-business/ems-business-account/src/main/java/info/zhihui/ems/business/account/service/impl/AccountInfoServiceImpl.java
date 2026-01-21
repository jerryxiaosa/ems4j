package info.zhihui.ems.business.account.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountCancelDetailDto;
import info.zhihui.ems.business.account.dto.AccountCancelQueryDto;
import info.zhihui.ems.business.account.dto.AccountCancelRecordDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.entity.AccountCancelRecordEntity;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.mapper.AccountCancelMapper;
import info.zhihui.ems.business.account.mapper.AccountInfoMapper;
import info.zhihui.ems.business.account.qo.AccountCancelRecordQo;
import info.zhihui.ems.business.account.qo.AccountQo;
import info.zhihui.ems.business.account.repository.AccountCancelRecordRepository;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 账户基础信息接口
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccountInfoServiceImpl implements AccountInfoService {
    private final AccountRepository accountRepository;
    private final AccountInfoMapper accountInfoMapper;
    private final AccountCancelRecordRepository accountCancelRecordRepository;
    private final AccountCancelMapper accountCancelMapper;
    private final ElectricMeterInfoService electricMeterInfoService;

    /**
     * 查询账户列表
     *
     * @param query 查询条件
     * @return 账户列表
     */
    @Override
    public List<AccountBo> findList(@NotNull AccountQueryDto query) {
        AccountQo qo = accountInfoMapper.queryToQo(query);
        List<AccountEntity> entityList = accountRepository.findList(qo);
        return accountInfoMapper.listEntityToBo(entityList);
    }

    /**
     * 根据ID查询账户详情
     *
     * @param id 账户ID
     * @return 账户详情
     */
    @Override
    public AccountBo getById(@NotNull Integer id) {
        AccountEntity entity = accountRepository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("能耗账户数据不存在");
        }

        return accountInfoMapper.entityToBo(entity);
    }

    /**
     * 分页查询销户记录
     *
     * @param query 查询条件
     * @param pageParam 分页参数
     * @return 销户记录分页结果
     */
    @Override
    public PageResult<AccountCancelRecordDto> findCancelRecordPage(@NotNull AccountCancelQueryDto query, @NotNull PageParam pageParam) {
        // 转换查询条件
        AccountCancelRecordQo qo = accountCancelMapper.queryDtoToQo(query);

        // 分页查询
        try (Page<AccountCancelRecordEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<AccountCancelRecordEntity> pageInfo = page.doSelectPageInfo(() -> accountCancelRecordRepository.selectListByCondition(qo));
            return accountCancelMapper.pageEntityToBo(pageInfo);
        }

    }

    /**
     * 根据销户编号查询销户详情
     *
     * @param cancelNo 销户编号
     * @return 销户详情
     */
    @Override
    public AccountCancelDetailDto getCancelRecordDetail(@NotNull String cancelNo) {
        // 查询销户记录
        AccountCancelRecordEntity cancelRecord = accountCancelRecordRepository.selectByCancelNo(cancelNo);
        if (cancelRecord == null) {
            throw new NotFoundException("销户记录不存在");
        }

        // 转换为详情DTO
        AccountCancelDetailDto detailDto = accountCancelMapper.entityToDetailDto(cancelRecord);

        // 查询销表明细
        List<CanceledMeterDto> meterDtoList = electricMeterInfoService.findMetersByCancelNo(detailDto.getCancelNo());
        detailDto.setMeterList(meterDtoList);

        return detailDto;
    }

}
