package info.zhihui.ems.business.finance.service.order.fee.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.business.finance.service.order.fee.ServiceRateService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.SERVICE_RATE_KEY;

/**
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
public class ServiceRateServiceImpl implements ServiceRateService {
    private final ConfigService configService;

    /**
     * 获取默认服务费率
     * - 0-1之间的数值，即一个百分数
     * @return 服务费率
     */
    @Override
    public BigDecimal getDefaultServiceRate() {
        BigDecimal serviceRate = configService.getValueByKey(SERVICE_RATE_KEY, new TypeReference<>() {});

        if (serviceRate.compareTo(BigDecimal.ZERO) < 0 || serviceRate.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessRuntimeException("数据异常，服务费比例需在0%-100%之间");
        }

        return serviceRate;
    }


    /**
     * 更新默认服务费率
     *
     * @param defaultServiceRate 服务费率
     */
    @Override
    public void updateDefaultServiceRate(BigDecimal defaultServiceRate) {
        if (defaultServiceRate.compareTo(BigDecimal.ZERO) < 0 || defaultServiceRate.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessRuntimeException("服务费比例需在0%-100%之间");
        }

        ConfigUpdateDto config = new ConfigUpdateDto();
        config.setConfigKey(SERVICE_RATE_KEY);
        config.setConfigValue(JacksonUtil.toJson(defaultServiceRate));
        configService.update(config);
    }
}
