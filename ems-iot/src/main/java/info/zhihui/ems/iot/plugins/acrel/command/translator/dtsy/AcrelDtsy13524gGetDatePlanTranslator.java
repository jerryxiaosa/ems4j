package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.iot.enums.ProductEnum;
import org.springframework.stereotype.Component;

/**
 * DTSY-1352-4G 日期方案读取命令翻译器。
 */
@Component
public class AcrelDtsy13524gGetDatePlanTranslator extends AbstractAcrelDtsyGetDatePlanTranslator {

    @Override
    public String productCode() {
        return ProductEnum.ACREL_DTSY_1352_4G.getCode();
    }
}
