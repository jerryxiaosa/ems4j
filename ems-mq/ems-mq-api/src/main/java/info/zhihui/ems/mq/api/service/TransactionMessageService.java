package info.zhihui.ems.mq.api.service;

import info.zhihui.ems.mq.api.bo.TransactionMessageBo;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 事务消息服务接口
 * 提供事务消息的新增、状态更新和查询功能
 *
 * @author jerryxiaosa
 */
public interface TransactionMessageService {

    /**
     * 新增事务消息
     *
     * @param dto 事务消息新增数据传输对象
     */
    void add(@Valid @NotNull TransactionMessageDto dto);

    /**
     * 标记事务消息为成功状态
     *
     * @param businessType 业务类型
     * @param sn 序列号
     */
    void success(@NotNull TransactionMessageBusinessTypeEnum businessType, @NotBlank String sn);

    /**
     * 标记事务消息为失败状态
     *
     * @param businessType 业务类型
     * @param sn 序列号
     */
    void failure(@NotNull TransactionMessageBusinessTypeEnum businessType, @NotBlank String sn);

    /**
     * 获取最近两天的失败记录
     *
     * @return 失败记录列表
     */
    List<TransactionMessageBo> findRecentFailureRecords();
}
