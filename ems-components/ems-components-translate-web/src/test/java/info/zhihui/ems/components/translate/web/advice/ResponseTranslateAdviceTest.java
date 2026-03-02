package info.zhihui.ems.components.translate.web.advice;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.annotation.SkipResponseTranslate;
import info.zhihui.ems.components.translate.formatter.AbsoluteMoneyScale2TextFormatter;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import info.zhihui.ems.components.translate.engine.TranslateEngine;
import info.zhihui.ems.components.translate.engine.TranslateMetadataCache;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import info.zhihui.ems.components.translate.web.config.TranslateResponseProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("ResponseTranslateAdvice测试")
class ResponseTranslateAdviceTest {

    @Test
    @DisplayName("缺失TranslateEngine时supports应返回false")
    void testSupports_MissingTranslateEngine_ShouldReturnFalse() throws Exception {
        ResponseTranslateAdvice advice = new ResponseTranslateAdvice(
                providerOf(TranslateEngine.class, null),
                providerOf(TranslateResponseProperties.class, null)
        );
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("query"), -1);

        boolean supports = advice.supports(returnType, MappingJackson2HttpMessageConverter.class);

        assertFalse(supports);
    }

    @Test
    @DisplayName("标记SkipResponseTranslate时supports应返回false")
    void testSupports_SkipAnnotation_ShouldReturnFalse() throws Exception {
        ResponseTranslateAdvice advice = new ResponseTranslateAdvice(
                providerOf(TranslateEngine.class, buildTranslateEngine()),
                providerOf(TranslateResponseProperties.class, null)
        );
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("querySkip"), -1);

        boolean supports = advice.supports(returnType, MappingJackson2HttpMessageConverter.class);

        assertFalse(supports);
    }

    @Test
    @DisplayName("缺失TranslateResponseProperties时应使用默认配置并正常翻译")
    void testBeforeBodyWrite_MissingProperties_ShouldUseDefaultProperties() throws Exception {
        ResponseTranslateAdvice advice = new ResponseTranslateAdvice(
                providerOf(TranslateEngine.class, buildTranslateEngine()),
                providerOf(TranslateResponseProperties.class, null)
        );
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("query"), -1);

        TestData data = new TestData();
        data.setStatus(1);

        RestResult<TestData> body = new RestResult<>();
        body.setSuccess(true);
        body.setData(data);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/translate/test");
        Object result = advice.beforeBodyWrite(
                body,
                returnType,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        assertSame(body, result);
        assertEquals("启用", data.getStatusName());
    }

    @Test
    @DisplayName("缺失TranslateEngine时beforeBodyWrite应直接放行")
    void testBeforeBodyWrite_MissingTranslateEngine_ShouldBypass() throws Exception {
        ResponseTranslateAdvice advice = new ResponseTranslateAdvice(
                providerOf(TranslateEngine.class, null),
                providerOf(TranslateResponseProperties.class, null)
        );
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("query"), -1);

        TestData data = new TestData();
        data.setStatus(1);

        RestResult<TestData> body = new RestResult<>();
        body.setSuccess(true);
        body.setData(data);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/translate/test");
        Object result = advice.beforeBodyWrite(
                body,
                returnType,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        assertSame(body, result);
        assertNull(data.getStatusName());
    }

    @Test
    @DisplayName("beforeBodyWrite应支持FormatText格式化分页数据")
    void testBeforeBodyWrite_PageResultWithFormatText_ShouldFormat() throws Exception {
        ResponseTranslateAdvice advice = new ResponseTranslateAdvice(
                providerOf(TranslateEngine.class, buildTranslateEngine()),
                providerOf(TranslateResponseProperties.class, null)
        );
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("queryPage"), -1);

        FormattedTestData data = new FormattedTestData();
        data.setAmount(new BigDecimal("12.3"));
        PageResult<FormattedTestData> page = new PageResult<FormattedTestData>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L)
                .setList(List.of(data));
        RestResult<PageResult<FormattedTestData>> body = new RestResult<>();
        body.setSuccess(true);
        body.setData(page);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/translate/test");
        Object result = advice.beforeBodyWrite(
                body,
                returnType,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        assertSame(body, result);
        assertEquals("12.30", data.getAmountText());
    }

    @Test
    @DisplayName("beforeBodyWrite应支持绝对值金额文本格式化")
    void testBeforeBodyWrite_WithAbsoluteMoneyScale2TextFormatter_ShouldFormatAbsoluteValue() throws Exception {
        ResponseTranslateAdvice advice = new ResponseTranslateAdvice(
                providerOf(TranslateEngine.class, buildTranslateEngine()),
                providerOf(TranslateResponseProperties.class, null)
        );
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("queryAbsoluteAmount"), -1);

        AbsoluteFormattedTestData data = new AbsoluteFormattedTestData();
        data.setAmount(new BigDecimal("-12.3"));

        RestResult<AbsoluteFormattedTestData> body = new RestResult<>();
        body.setSuccess(true);
        body.setData(data);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/translate/test");
        Object result = advice.beforeBodyWrite(
                body,
                returnType,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        assertSame(body, result);
        assertEquals("12.30", data.getAmountText());
    }

    @Test
    @DisplayName("beforeBodyWrite应支持分页数据中的枚举和业务名称翻译")
    void testBeforeBodyWrite_PageResultWithBizLabelAndEnumLabel_ShouldTranslate() throws Exception {
        ResponseTranslateAdvice advice = new ResponseTranslateAdvice(
                providerOf(TranslateEngine.class, buildTranslateEngine()),
                providerOf(TranslateResponseProperties.class, null)
        );
        MethodParameter returnType = new MethodParameter(TestController.class.getDeclaredMethod("queryMeterPage"), -1);

        MeterViewData meterViewData = new MeterViewData();
        meterViewData.setPricePlanId(11);
        meterViewData.setWarnPlanId(22);
        meterViewData.setWarnType("FIRST");
        PageResult<MeterViewData> page = new PageResult<MeterViewData>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L)
                .setList(List.of(meterViewData));
        RestResult<PageResult<MeterViewData>> body = new RestResult<>();
        body.setSuccess(true);
        body.setData(page);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/translate/test");
        Object result = advice.beforeBodyWrite(
                body,
                returnType,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        assertSame(body, result);
        assertEquals("居民电价", meterViewData.getPricePlanName());
        assertEquals("标准预警", meterViewData.getWarnPlanName());
        assertEquals("一级预警", meterViewData.getElectricWarnTypeName());
    }

    private static TranslateEngine buildTranslateEngine() {
        return new TranslateEngine(
                new TranslateMetadataCache(),
                new EnumLabelResolver(),
                List.of(new TestElectricPricePlanNameResolver(), new TestWarnPlanNameResolver()),
                List.of(new MoneyScale2TextFormatter(), new AbsoluteMoneyScale2TextFormatter())
        );
    }

    private static <T> ObjectProvider<T> providerOf(Class<T> beanClass, T bean) {
        if (bean == null) {
            return new StaticListableBeanFactory().getBeanProvider(beanClass);
        }
        Map<String, Object> beanMap = Collections.singletonMap(beanClass.getSimpleName(), bean);
        return new StaticListableBeanFactory(beanMap).getBeanProvider(beanClass);
    }

    private static class TestController {

        RestResult<TestData> query() {
            return new RestResult<>();
        }

        @SkipResponseTranslate
        RestResult<TestData> querySkip() {
            return new RestResult<>();
        }

        RestResult<PageResult<FormattedTestData>> queryPage() {
            return new RestResult<>();
        }

        RestResult<AbsoluteFormattedTestData> queryAbsoluteAmount() {
            return new RestResult<>();
        }

        RestResult<PageResult<MeterViewData>> queryMeterPage() {
            return new RestResult<>();
        }
    }

    private static class TestData {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;

        Integer getStatus() {
            return status;
        }

        void setStatus(Integer status) {
            this.status = status;
        }

        String getStatusName() {
            return statusName;
        }
    }

    private static class FormattedTestData {
        private BigDecimal amount;

        @FormatText(source = "amount", formatter = MoneyScale2TextFormatter.class)
        private String amountText;

        BigDecimal getAmount() {
            return amount;
        }

        void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        String getAmountText() {
            return amountText;
        }
    }

    private static class AbsoluteFormattedTestData {
        private BigDecimal amount;

        @FormatText(source = "amount", formatter = AbsoluteMoneyScale2TextFormatter.class)
        private String amountText;

        BigDecimal getAmount() {
            return amount;
        }

        void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        String getAmountText() {
            return amountText;
        }
    }

    private static class MeterViewData {
        private Integer pricePlanId;
        private Integer warnPlanId;
        private String warnType;

        @BizLabel(source = "pricePlanId", resolver = TestElectricPricePlanNameResolver.class)
        private String pricePlanName;

        @BizLabel(source = "warnPlanId", resolver = TestWarnPlanNameResolver.class)
        private String warnPlanName;

        @EnumLabel(source = "warnType", enumClass = TestWarnTypeEnum.class)
        private String electricWarnTypeName;

        Integer getPricePlanId() {
            return pricePlanId;
        }

        void setPricePlanId(Integer pricePlanId) {
            this.pricePlanId = pricePlanId;
        }

        Integer getWarnPlanId() {
            return warnPlanId;
        }

        void setWarnPlanId(Integer warnPlanId) {
            this.warnPlanId = warnPlanId;
        }

        String getWarnType() {
            return warnType;
        }

        void setWarnType(String warnType) {
            this.warnType = warnType;
        }

        String getPricePlanName() {
            return pricePlanName;
        }

        String getWarnPlanName() {
            return warnPlanName;
        }

        String getElectricWarnTypeName() {
            return electricWarnTypeName;
        }
    }

    private static class TestElectricPricePlanNameResolver implements BatchLabelResolver<Integer> {

        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, info.zhihui.ems.components.translate.engine.TranslateContext context) {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyMap();
            }
            return keys.contains(11) ? Collections.singletonMap(11, "居民电价") : Collections.emptyMap();
        }
    }

    private static class TestWarnPlanNameResolver implements BatchLabelResolver<Integer> {

        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, info.zhihui.ems.components.translate.engine.TranslateContext context) {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyMap();
            }
            return keys.contains(22) ? Collections.singletonMap(22, "标准预警") : Collections.emptyMap();
        }
    }

    private enum TestStatusEnum implements CodeEnum<Integer> {
        DISABLED(0, "停用"),
        ENABLED(1, "启用");

        private final Integer code;
        private final String info;

        TestStatusEnum(Integer code, String info) {
            this.code = code;
            this.info = info;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getInfo() {
            return info;
        }
    }

    private enum TestWarnTypeEnum implements CodeEnum<String> {
        FIRST("FIRST", "一级预警");

        private final String code;
        private final String info;

        TestWarnTypeEnum(String code, String info) {
            this.code = code;
            this.info = info;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getInfo() {
            return info;
        }
    }
}
