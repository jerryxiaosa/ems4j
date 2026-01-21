package info.zhihui.ems.web.order.biz;

import info.zhihui.ems.business.finance.service.order.fee.ServiceRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 服务费率业务编排
 */
@Service
@RequiredArgsConstructor
public class ServiceRateBiz {

    private final ServiceRateService serviceRateService;

    public BigDecimal getDefaultServiceRate() {
        return serviceRateService.getDefaultServiceRate();
    }

    public void updateDefaultServiceRate(BigDecimal serviceRate) {
        serviceRateService.updateDefaultServiceRate(serviceRate);
    }
}
