package info.zhihui.ems.components.translate.web.advice;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.SkipResponseTranslate;
import info.zhihui.ems.components.translate.engine.TranslateEngine;
import info.zhihui.ems.components.translate.engine.TranslateMetadataCache;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
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

import java.util.Collections;
import java.util.Map;

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

    private static TranslateEngine buildTranslateEngine() {
        return new TranslateEngine(
                new TranslateMetadataCache(),
                new EnumLabelResolver(),
                Collections.emptyList()
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
}
