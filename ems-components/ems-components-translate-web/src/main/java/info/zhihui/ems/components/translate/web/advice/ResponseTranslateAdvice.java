package info.zhihui.ems.components.translate.web.advice;

import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.components.translate.annotation.SkipResponseTranslate;
import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.engine.TranslateEngine;
import info.zhihui.ems.components.translate.web.config.TranslateResponseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

/**
 * HTTP响应转换增强
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(TranslateEngine.class)
@ConditionalOnProperty(prefix = "translate.response", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResponseTranslateAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectProvider<TranslateEngine> translateEngineProvider;
    private final ObjectProvider<TranslateResponseProperties> propertiesProvider;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (translateEngineProvider.getIfAvailable() == null) {
            return false;
        }
        if (!RestResult.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        return !isSkipTranslate(returnType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        TranslateEngine translateEngine = translateEngineProvider.getIfAvailable();
        if (translateEngine == null) {
            return body;
        }
        if (!(body instanceof RestResult<?> restResult)) {
            return body;
        }
        if (restResult.getData() == null) {
            return body;
        }

        String requestPath = resolveRequestPath(request);
        if (!isPathAllowed(requestPath)) {
            return body;
        }

        TranslateContext context = new TranslateContext()
                .setRequestPath(requestPath)
                .setRequestMethod(resolveRequestMethod(request));
        try {
            translateEngine.translate(restResult.getData(), context);
        } catch (RuntimeException e) {
            log.warn("响应转换执行失败，已降级放行。path={}", requestPath, e);
        }
        return body;
    }

    private boolean isSkipTranslate(MethodParameter returnType) {
        if (returnType.getMethod() != null
                && AnnotatedElementUtils.hasAnnotation(returnType.getMethod(), SkipResponseTranslate.class)) {
            return true;
        }
        return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), SkipResponseTranslate.class);
    }

    private String resolveRequestPath(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest().getRequestURI();
        }
        return request.getURI().getPath();
    }

    private String resolveRequestMethod(ServerHttpRequest request) {
        return request.getMethod().name();
    }

    private boolean isPathAllowed(String requestPath) {
        if (requestPath == null) {
            return true;
        }

        TranslateResponseProperties properties = propertiesProvider.getIfAvailable(TranslateResponseProperties::new);

        List<String> includes = properties.getIncludePathPatterns();
        if (!CollectionUtils.isEmpty(includes) && !isMatchAny(requestPath, includes)) {
            return false;
        }

        List<String> excludes = properties.getExcludePathPatterns();
        return CollectionUtils.isEmpty(excludes) || !isMatchAny(requestPath, excludes);
    }

    private boolean isMatchAny(String requestPath, List<String> patterns) {
        for (String pattern : patterns) {
            if (pattern != null && pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }
}
