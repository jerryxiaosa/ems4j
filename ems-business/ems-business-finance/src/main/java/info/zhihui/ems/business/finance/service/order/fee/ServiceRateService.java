package info.zhihui.ems.business.finance.service.order.fee;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
public interface ServiceRateService {
    /**
     * 获取默认服务费率
     * - 0-1之间的数值，即一个百分数
     * @return 服务费率
     */
    BigDecimal getDefaultServiceRate();

    /**
     * 更新默认服务费率
     *
     * @param defaultServiceRate 服务费率
     */
    void updateDefaultServiceRate(BigDecimal defaultServiceRate);
}
