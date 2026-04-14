package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.iot.enums.ProductEnum;
import org.springframework.stereotype.Component;

/**
 * DTSY-1352 日期方案读取命令翻译器。
 */
@Component
public class AcrelDtsy1352GetDatePlanTranslator extends AbstractAcrelDtsyGetDatePlanTranslator {

    @Override
    public String productCode() {
        return ProductEnum.ACREL_DTSY_1352.getCode();
    }
}
